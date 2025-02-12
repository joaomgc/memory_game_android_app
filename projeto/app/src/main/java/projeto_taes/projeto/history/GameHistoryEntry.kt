package projeto_taes.projeto.history

data class GameHistoryEntry(
    val date: String,
    val boardSize: String,
    val moves: Int,
    val timeInSeconds: Int,
)