package projeto_taes.projeto.scoreboards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import projeto_taes.projeto.App

@Composable
fun ScoreboardScreen(app: App) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (app.isUserLoggedIn()) {
                Button(
                    onClick = { app.navController.navigate("personalScore") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Personal Score")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            Button(
                onClick = { app.navController.navigate("globalScore") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Global Score")
            }
        }
    }
}