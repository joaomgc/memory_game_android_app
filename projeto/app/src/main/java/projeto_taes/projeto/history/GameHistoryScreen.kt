package projeto_taes.projeto.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import projeto_taes.projeto.App

@Composable
fun GameHistoryScreen(gameHistoryManager: GameHistoryManager) {
    val gameHistory = remember { mutableStateOf<List<GameHistoryEntry>>(listOf()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        gameHistoryManager.loadGameHistory()
        gameHistory.value = gameHistoryManager.getGameHistory()
        loading = false
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text("Your Game History", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(8.dp))
                if (gameHistory.value.isEmpty()) {
                    Text("No game history available.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(gameHistory.value) { game ->
                            GameHistoryItem(game)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameHistoryItem(game: GameHistoryEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Board: ${game.boardSize}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text("Played on: ${game.date}", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Moves: ${game.moves}", style = MaterialTheme.typography.bodyLarge)
                Text("Time: ${game.timeInSeconds} secs", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}