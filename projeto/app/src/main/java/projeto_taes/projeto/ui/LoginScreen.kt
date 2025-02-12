package projeto_taes.projeto.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import projeto_taes.projeto.App
import projeto_taes.projeto.scoreboards.Score

@Composable
fun LoginScreen(app: App) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLogged by remember { mutableStateOf(false) }
    var isLoadingUserInfo by remember { mutableStateOf(false) }

    LaunchedEffect(isLogged) {
        if (isLogged) {
            isLoadingUserInfo = true
            val success = loadUserInfo(app)
            isLoadingUserInfo = false
            if (success) {
                app.navController.navigate("dashboard")
            } else {
                errorMessage = "Failed to load user data. Try again."
                isLogged = false
            }
        }
    }

    if (isLoadingUserInfo) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("E-mail")
            TextField(
                email,
                onValueChange = { s -> email = s },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Password")
            TextField(
                password,
                onValueChange = { s -> password = s },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                onClick = {
                    app.tryLogin(email, password) { exception ->
                        if (exception != null) {
                            errorMessage = exception.message ?: "Unknown error"
                        } else {
                            isLogged = true
                        }
                    }
                }
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    app.logout()
                    app.navController.navigate("dashboard")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Play as anonymous")
            }
        }
    }
}

suspend fun loadUserInfo(app: App): Boolean = coroutineScope {
    try {
        // If test mode, set the score to a know value for test the changes in katalon tests
        if (App.TESTING) {
            val testScoresSuccess = CompletableDeferred<Boolean>()
            app.saveTestScores(app, testScoresSuccess)
            if (!testScoresSuccess.await()) {
                return@coroutineScope false
            }
        }

        val globalScoresSuccess = CompletableDeferred<Boolean>()
        val personalScoresSuccess = CompletableDeferred<Boolean>()
        val userDataSuccess = CompletableDeferred<Boolean>()

        // Global Scores
        launch {
            if (!app.scoreManager.isGlobalScoresLoaded()) {
                globalScoresSuccess.complete(app.scoreManager.loadGlobalScores())
            } else {
                globalScoresSuccess.complete(true)
            }
        }

        // Personal Scores
        launch {
            if (!app.scoreManager.isPersonalScoreLoaded()) {
                personalScoresSuccess.complete(app.scoreManager.loadPersonalScores())
            } else {
                personalScoresSuccess.complete(true)
            }
        }

        // User Data
        launch {
            app.fetchUserData { exception ->
                if (exception == null) {
                    userDataSuccess.complete(true)
                } else {
                    Log.e("fetchUserData", "Failed to fetch user data: ${exception.message}", exception)
                    userDataSuccess.complete(false)
                }
            }
        }

        // Wait for all operations to complete and check if all were successful
        val allLoadedSuccessfully = listOf(globalScoresSuccess, personalScoresSuccess, userDataSuccess)
            .map { it.await() }
            .all { it }

        allLoadedSuccessfully
    } catch (e: Exception) {
        Log.e("loadUserInfo", "Error loading user info: ${e.message}", e)
        false
    }
}