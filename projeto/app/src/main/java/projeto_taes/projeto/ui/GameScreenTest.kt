package projeto_taes.projeto.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameScreenTest(navController: NavController, boardSize: String, onGameFinish: (Int, Int) -> Unit) {
    val (rows, columns) = when (boardSize) {
        "3x4" -> 3 to 4
        "4x4" -> 4 to 4
        "6x6" -> 6 to 6
        else -> 3 to 4
    }
    val totalPairs = (rows * columns) / 2

    var cards by remember { mutableStateOf(generateFixedCards(rows, columns)) }
    var selectedCards by remember { mutableStateOf<List<Card>>(emptyList()) }
    var moveCount by remember { mutableStateOf(0) }
    var matchedPairs by remember { mutableStateOf(0) }
    var elapsedTime by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var gameFinished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Start the timer when the game begins
    LaunchedEffect(Unit) {
        while (matchedPairs < totalPairs) {
            delay(1000)
            elapsedTime++
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Game Over") },
            text = { Text("Game Finished! Time: ${elapsedTime}s, Moves: $moveCount") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Memory Game Test ($boardSize)", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Time: ${elapsedTime}s", fontSize = 16.sp)
        Text(text = "Moves: $moveCount", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards) { card ->
                CardView(
                    card = card,
                    onCardClick = { flippedCard ->
                        handleCardClick(
                            flippedCard, cards, selectedCards, scope,
                            onUpdate = { updatedCards, updatedSelectedCards ->
                                cards = updatedCards
                                selectedCards = updatedSelectedCards
                            },
                            onMatch = {
                                matchedPairs++
                                if (matchedPairs == totalPairs && !gameFinished) {
                                    gameFinished = true
                                    showDialog = true
                                    onGameFinish(moveCount, elapsedTime)
                                }
                            }
                        )
                        moveCount++
                    }
                )
            }
        }
    }
}

fun generateFixedCards(rows: Int, columns: Int): List<Card> {
    val totalPairs = (rows * columns) / 2
    val fixedValues = ('A' until 'A' + totalPairs).map { it.toString() }

    val cards = MutableList(rows * columns) { Card(it, "") }

    var pairIndex = 0
    for (row in 0 until rows) {
        for (col in 0 until columns) {
            val position = row * columns + col

            // Assign pairs: first A, then A, then B, then B, etc.
            cards[position] = cards[position].copy(
                value = fixedValues[pairIndex / 2]
            )

            pairIndex++
        }
    }

    return cards
}
