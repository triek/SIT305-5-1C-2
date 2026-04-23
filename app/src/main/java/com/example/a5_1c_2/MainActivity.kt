package com.example.a5_1c_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a5_1c_2.data.AppDatabase
import com.example.a5_1c_2.ui.auth.AuthViewModel
import com.example.a5_1c_2.ui.auth.AuthViewModelFactory
import com.example.a5_1c_2.ui.auth.HomeScreen
import com.example.a5_1c_2.ui.auth.LoginScreen
import com.example.a5_1c_2.ui.auth.SignUpScreen
import com.example.a5_1c_2.ui.theme._51C2Theme

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AppDatabase.getInstance(applicationContext).userDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _51C2Theme {
                val navController = rememberNavController()
                val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

                NavHost(
                    navController = navController,
                    startDestination = "login",
                    modifier = Modifier
                ) {
                    composable("login") {
                        LoginScreen(
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.loginError,
                            onLoginClick = { username, password ->
                                authViewModel.login(username, password) {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            onSignUpClick = {
                                authViewModel.clearLoginError()
                                navController.navigate("signup")
                            }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.signUpError,
                            onSignUpClick = { fullName, username, password, confirmPassword ->
                                authViewModel.signUp(fullName, username, password, confirmPassword) {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            onBackToLoginClick = {
                                authViewModel.clearSignUpError()
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            user = uiState.currentUser,
                            homeError = uiState.homeError,
                            currentVideoId = uiState.currentVideoId,
                            playlistItems = uiState.playlistItems,
                            onPlayClick = authViewModel::playVideo,
                            onAddToPlaylistClick = authViewModel::addCurrentVideoToPlaylist,
                            onLogoutClick = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
