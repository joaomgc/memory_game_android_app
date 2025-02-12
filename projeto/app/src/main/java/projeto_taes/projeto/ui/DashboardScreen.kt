package projeto_taes.projeto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import projeto_taes.projeto.App
import androidx.compose.runtime.LaunchedEffect
import projeto_taes.projeto.R
import projeto_taes.projeto.history.GameHistoryEntry


@Composable
fun DashboardScreen(app: App) {
    // State to hold the user's coins value from App
    var coins by remember { mutableStateOf(app.coins) }

    // Fetch user data if logged in and coins is still 0
    LaunchedEffect(Unit) {
        if (app.isUserLoggedIn()) {
            app.fetchUserData { exception ->
                if (exception == null) {
                    // After successful fetch, update the coins state
                    coins = app.coins
                } else {
                    // Handle error (e.g., show error message)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(128.dp)

        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { app.navController.navigate("boardSelection") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
            Text("Start New Game")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { app.navController.navigate("scoreboards") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
            Text("Scoreboards")
        }

        if (app.isUserLoggedIn()) {
            Spacer(modifier = Modifier.height(16.dp))
            // Display coins value fetched from App class
            Text(text = "Game Coins: $coins",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { app.navController.navigate("gameHistory") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                ) {
                Text("Game History")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { app.navController.navigate("wallet") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                ) {
                Text("Wallet")
            }
        }
    }
}
