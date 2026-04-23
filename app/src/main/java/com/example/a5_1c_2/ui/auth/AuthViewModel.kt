package com.example.a5_1c_2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a5_1c_2.data.User
import com.example.a5_1c_2.data.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val signUpError: String? = null
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
                _uiState.update {
                    it.copy(isLoading = false, currentUser = user, loginError = null)
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
                it.copy(isLoading = false, currentUser = createdUser, signUpError = null)
            }
            onSuccess()
        }
    }

    fun clearLoginError() {
        _uiState.update { it.copy(loginError = null) }
    }

    fun clearSignUpError() {
        _uiState.update { it.copy(signUpError = null) }
    }
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
