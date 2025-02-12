package projeto_taes.projeto.scoreboards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@Composable
fun GlobalScoreScreen(scoreManager: ScoreManager) {
    val coroutineScope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            scoreManager.loadGlobalScores()
            loading = false
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Global Scores", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            if (loading) {
                CircularProgressIndicator()
            } else {
                // Buttons to show scores for each board size
                ScoreButton("3x4 Board", scoreManager.getGlobalScores("3x4"))
                ScoreButton("4x4 Board", scoreManager.getGlobalScores("4x4"))
                ScoreButton("6x6 Board", scoreManager.getGlobalScores("6x6"))
            }
        }
    }
}

@Composable
fun ScoreButton(boardSize: String, scores: List<ScoreEntry>) {
    var showScores by remember { mutableStateOf(false) }

    Button(onClick = { showScores = true }) {
        Text(boardSize)
    }

    if (showScores) {
        ScoreDialog(boardSize, scores) { showScores = false }
    }
}

@Composable
fun ScoreDialog(boardSize: String, scores: List<ScoreEntry>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .heightIn(min = 250.dp)
        ) {
            Text(
                text = boardSize,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp, bottom = 20.dp)
            )
            if (scores.isNotEmpty()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(scores) { scoreEntry ->
                        ScoreItem(scoreEntry)
                    }
                }
            } else {
                Text(
                    "No scores available",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                        .weight(1f)
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun ScoreItem(scoreEntry: ScoreEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(scoreEntry.username, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
        Text("${scoreEntry.score.moves} moves, ${scoreEntry.score.time} secs", style = MaterialTheme.typography.bodyMedium)
    }
}