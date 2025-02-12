package projeto_taes.projeto.scoreboards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PersonalScoreScreen(scoreManager: ScoreManager) {
    val coroutineScope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            scoreManager.loadPersonalScores()
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
            Text("Your Personal Scores", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            if (loading) {
                CircularProgressIndicator()
            } else {
                // Display personal score for each board size
                ScoreItem("3x4 Board", scoreManager.getPersonalScore("3x4"))
                ScoreItem("4x4 Board", scoreManager.getPersonalScore("4x4"))
                ScoreItem("6x6 Board", scoreManager.getPersonalScore("6x6"))
            }
        }
    }
}

@Composable
fun ScoreItem(boardSize: String, score: Score) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(boardSize, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (score == Score.DEFAULT || score == Score.NOT_SET) {
            Text(
                "No score yet",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
            )
        } else {
            Text(
                "Moves: ${score.moves}, Time: ${score.time} secs",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
    }
}