package projeto_taes.projeto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import projeto_taes.projeto.App

@Composable
fun BoardSelectionScreen(app: App) {
    var showDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select Board Size",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { app.navController.navigate("newGame/3x4") },
            modifier = Modifier.fillMaxWidth()) {
            Text("3x4 Board")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (app.coins > 0) {
                app.payCoin { exception ->
                    CoroutineScope(Dispatchers.Main).launch {
                        if (exception == null) {
                            app.navController.navigate("newGame/4x4")
                        } else {
                            showErrorDialog = true
                        }
                    }
                }
            } else {
                showDialog = true
            }
        },
            modifier = Modifier.fillMaxWidth()) {
            Text("4x4 Board")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (app.coins > 0) {
                app.payCoin { exception ->
                    CoroutineScope(Dispatchers.Main).launch {
                        if (exception == null) {
                            app.navController.navigate("newGame/6x6")
                        } else {
                            showErrorDialog = true
                        }
                    }
                }
            } else {
                showDialog = true
            }
        },
            modifier = Modifier.fillMaxWidth()) {
            Text("6x6 Board")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Not Enough Coins") },
            text = { Text("Please play the 3x4 board to earn more coins first.") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Transaction Error") },
            text = { Text("There was an error processing your transaction. Please try again.") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
