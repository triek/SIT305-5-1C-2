package com.example.a5_1c_2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a5_1c_2.data.PlaylistItem
import com.example.a5_1c_2.data.User
import com.example.a5_1c_2.data.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val youtubeRegex = Regex(
    pattern = """(?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/embed/)([A-Za-z0-9_-]{11}).*""",
    options = setOf(RegexOption.IGNORE_CASE)
)

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val signUpError: String? = null,
    val homeError: String? = null,
    val currentVideoId: String? = null,
    val currentVideoUrl: String? = null,
    val playlistItems: List<PlaylistItem> = emptyList()
)

class AuthViewModel(private val userDao: UserDao) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(loginError = "Please fill in all fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null, signUpError = null) }

            val user = withContext(Dispatchers.IO) {
                userDao.login(username.trim(), password)
            }
            if (user == null) {
                _uiState.update {
                    it.copy(isLoading = false, loginError = "Invalid username or password.")
                }
            } else {
                val playlist = withContext(Dispatchers.IO) {
                    userDao.getPlaylistItemsForUser(user.id)
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        loginError = null,
                        homeError = null,
                        currentVideoId = extractVideoId(user.lastPlayedUrl ?: ""),
                        currentVideoUrl = user.lastPlayedUrl,
                        playlistItems = playlist
                    )
                }
                onSuccess()
            }
        }
    }

    fun signUp(
        fullName: String,
        username: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (fullName.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.update { it.copy(signUpError = "Please fill in all fields.") }
            return
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(signUpError = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, signUpError = null, loginError = null) }

            val existingUser = withContext(Dispatchers.IO) {
                userDao.getUserByUsername(username.trim())
            }
            if (existingUser != null) {
                _uiState.update {
                    it.copy(isLoading = false, signUpError = "Username is already taken.")
                }
                return@launch
            }

            val newUser = User(fullName = fullName.trim(), username = username.trim(), password = password)
            val createdUser = withContext(Dispatchers.IO) {
                userDao.insertUser(newUser)
                userDao.login(username.trim(), password)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentUser = createdUser,
                    signUpError = null,
                    homeError = null,
                    currentVideoId = null,
                    currentVideoUrl = null,
                    playlistItems = emptyList()
                )
            }
            onSuccess()
        }
    }

    fun playVideo(url: String) {
        val trimmed = url.trim()
        val videoId = extractVideoId(trimmed)

        if (videoId == null) {
            _uiState.update { it.copy(homeError = "Please enter a valid YouTube URL.") }
            return
        }

        _uiState.update {
            it.copy(
                currentVideoId = videoId,
                currentVideoUrl = normalizedWatchUrl(videoId),
                homeError = null
            )
        }
    }

    fun addCurrentVideoToPlaylist() {
        val user = _uiState.value.currentUser
        val videoUrl = _uiState.value.currentVideoUrl

        if (user == null || videoUrl.isNullOrBlank()) {
            _uiState.update { it.copy(homeError = "Play a valid video before adding it to playlist.") }
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userDao.updateLastPlayedUrl(user.id, videoUrl)
                userDao.addPlaylistItem(PlaylistItem(userId = user.id, videoUrl = videoUrl))
            }

            val updatedUser = withContext(Dispatchers.IO) {
                userDao.getUserById(user.id)
            }
            val updatedPlaylist = withContext(Dispatchers.IO) {
                userDao.getPlaylistItemsForUser(user.id)
            }

            _uiState.update {
                it.copy(
                    currentUser = updatedUser ?: it.currentUser,
                    playlistItems = updatedPlaylist,
                    homeError = null
                )
            }
        }
    }

    fun loadPlaylistForCurrentUser() {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val playlist = withContext(Dispatchers.IO) {
                userDao.getPlaylistItemsForUser(user.id)
            }
            _uiState.update { it.copy(playlistItems = playlist) }
        }
    }

    fun playPlaylistItem(url: String) {
        playVideo(url)

        val user = _uiState.value.currentUser ?: return
        val normalizedUrl = _uiState.value.currentVideoUrl ?: return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userDao.updateLastPlayedUrl(user.id, normalizedUrl)
            }
            val updatedUser = withContext(Dispatchers.IO) {
                userDao.getUserById(user.id)
            }
            _uiState.update { it.copy(currentUser = updatedUser ?: it.currentUser) }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }

    fun clearLoginError() {
        _uiState.update { it.copy(loginError = null) }
    }

    fun clearSignUpError() {
        _uiState.update { it.copy(signUpError = null) }
    }

    private fun extractVideoId(url: String): String? {
        if (url.isBlank()) return null

        youtubeRegex.find(url)?.let { return it.groupValues[1] }

        return if (url.matches(Regex("^[A-Za-z0-9_-]{11}$"))) {
            url
        } else {
            null
        }
    }

    private fun normalizedWatchUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"
}

class AuthViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
