package projeto_taes.projeto.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import projeto_taes.projeto.App

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameScreen(navController: NavController, boardSize: String, onGameFinish: (Int, Int) -> Unit,app: App) {
    val (rows, columns) = when (boardSize) {
        "3x4" -> 3 to 4
        "4x4" -> 4 to 4
        "6x6" -> 6 to 6
        else -> 3 to 4 // Default to 3x4 if no size is specified
    }
    val totalPairs = (rows * columns) / 2

    var coins by remember { mutableStateOf(app.coins) }

    var cards by remember { mutableStateOf(generateCards(totalPairs)) }
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
        Text(text = "Memory Game ($boardSize)", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Time: ${elapsedTime}s", fontSize = 16.sp)
        Text(text = "Moves: $moveCount", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Hint Button
        Button(
            onClick = {
                app.payCoin { exception ->
                    if (exception == null) {
                        // Successfully paid coins; now reveal the hint
                        revealHintPair(scope, cards, onUpdate = { updatedCards -> cards = updatedCards })
                        app.fetchUserData { exception ->
                            if (exception == null) {
                                coins = app.coins
                            } else {
                                // Handle error, such as showing a message to the user
                                Log.e("GameScreen", "Failed to fetch user data: ${exception.message}")
                            }
                        }
                    } else {
                        // Handle error, such as showing a message to the user
                        Log.e("GameScreen", "Failed to pay coin: ${exception.message}")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.primary      // White text
            ),
            shape = RoundedCornerShape(16.dp),      // Rounded corners
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Hint",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                Text(
                    text = "Use Hint",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Coins",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$coins",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            )
        }

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

// Hint function to reveal one matching pair
fun revealHintPair(scope: CoroutineScope, cards: List<Card>, onUpdate: (List<Card>) -> Unit) {
    val unmatchedPairs = cards.filter { !it.isMatched }
        .groupBy { it.value }
        .values
        .firstOrNull { it.size >= 2 } // Get the first pair if available

    if (unmatchedPairs != null) {
        val (firstCard, secondCard) = unmatchedPairs
        val updatedCards = cards.map {
            when (it.id) {
                firstCard.id, secondCard.id -> it.copy(isFaceUp = true)
                else -> it
            }
        }
        onUpdate(updatedCards)

        // Flip the cards back down after a delay
        scope.launch {
            delay(2000) // Show the hint for 2 seconds
            val resetCards = updatedCards.map {
                when (it.id) {
                    firstCard.id, secondCard.id -> it.copy(isFaceUp = false)
                    else -> it
                }
            }
            onUpdate(resetCards)
        }
    }
}

data class Card(val id: Int, val value: String, val isFaceUp: Boolean = false, val isMatched: Boolean = false)

fun generateCards(totalPairs: Int): List<Card> {
    val values = ('A' until 'A' + totalPairs).map { it.toString() }
    return (values + values).shuffled().mapIndexed { index, value -> Card(id = index, value = value) }
}

@Composable
fun CardView(card: Card, onCardClick: (Card) -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                if (card.isFaceUp || card.isMatched) Color.Gray else Color.Cyan,
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !card.isFaceUp && !card.isMatched) { onCardClick(card) },
        contentAlignment = Alignment.Center
    ) {
        if (card.isFaceUp || card.isMatched) {
            Text(text = card.value, fontSize = 24.sp, textAlign = TextAlign.Center, color = Color.White)
        } else {
            Text(text = "?", fontSize = 24.sp, textAlign = TextAlign.Center, color = Color.White)
        }
    }
}

fun handleCardClick(
    card: Card,
    cards: List<Card>,
    selectedCards: List<Card>,
    scope: CoroutineScope,
    onUpdate: (List<Card>, List<Card>) -> Unit,
    onMatch: () -> Unit
) {
    // Update the list to flip the selected card
    val updatedCards = cards.map { if (it.id == card.id) it.copy(isFaceUp = true) else it }
    val newSelectedCards = selectedCards + card
    onUpdate(updatedCards, newSelectedCards)

    // Check for match if two cards are selected
    if (newSelectedCards.size == 2) {
        scope.launch {
            delay(1000)  // Delay to show both cards

            val (first, second) = newSelectedCards
            val matched = first.value == second.value

            val finalCards = updatedCards.map {
                when (it.id) {
                    first.id, second.id -> it.copy(isFaceUp = matched, isMatched = matched)
                    else -> it
                }
            }

            onUpdate(finalCards, emptyList()) // Reset selected cards after match check
            if (matched) {
                onMatch()
            }
        }
    }
}
