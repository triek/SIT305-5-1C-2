package com.example.a5_1c_2.ui.auth

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.a5_1c_2.data.PlaylistItem
import com.example.a5_1c_2.data.User

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (username: String, password: String) -> Unit,
    onSignUpClick: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthContainer(title = "Login") {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onLoginClick(username, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Logging in..." else "Login")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSignUpClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }
    }
}

@Composable
fun SignUpScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignUpClick: (fullName: String, username: String, password: String, confirmPassword: String) -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    AuthContainer(title = "Sign Up") {
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onSignUpClick(fullName, username, password, confirmPassword) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Creating account..." else "Register")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBackToLoginClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Login")
        }
    }
}

@Composable
fun HomeScreen(
    user: User?,
    homeError: String?,
    currentVideoId: String?,
    currentVideoUrl: String?,
    playlistItems: List<PlaylistItem>,
    onPlayClick: (String) -> Unit,
    onAddToPlaylistClick: (String) -> Unit,
    onOpenPlaylistClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var videoUrl by rememberSaveable { mutableStateOf(user?.lastPlayedUrl.orEmpty()) }
    var videoName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(currentVideoUrl) {
        if (!currentVideoUrl.isNullOrBlank()) {
            videoUrl = currentVideoUrl
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome, ${user?.fullName ?: "User"}", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Username: ${user?.username ?: "Unknown"}")

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            label = { Text("YouTube URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = videoName,
            onValueChange = { videoName = it },
            label = { Text("Video name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onPlayClick(videoUrl) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Play")
        }

        if (homeError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = homeError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (currentVideoId != null) {
            YoutubeVideoPlayer(videoId = currentVideoId)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { onAddToPlaylistClick(videoName) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add to Playlist")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onOpenPlaylistClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("My Playlist (${playlistItems.size})")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun PlaylistScreen(
    user: User?,
    playlistItems: List<PlaylistItem>,
    onItemClick: (PlaylistItem) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "${user?.fullName ?: "User"}'s Playlist",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        PlaylistList(items = playlistItems, onItemClick = onItemClick)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}

@Composable
private fun PlaylistList(
    items: List<PlaylistItem>,
    onItemClick: (PlaylistItem) -> Unit
) {
    if (items.isEmpty()) {
        Text(text = "No videos added yet.")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        items(items) { item ->
            Card(
                onClick = { onItemClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Column {
                        Text(
                            text = item.videoName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = item.videoUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YoutubeVideoPlayer(videoId: String) {
    val html = """
        <html>
            <body style="margin:0;padding:0;">
                <iframe
                    width="100%"
                    height="100%"
                    src="https://www.youtube.com/embed/$videoId"
                    frameborder="0"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowfullscreen>
                </iframe>
            </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
        }
    )
}

@Composable
private fun AuthContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))
        content()
    }
}
