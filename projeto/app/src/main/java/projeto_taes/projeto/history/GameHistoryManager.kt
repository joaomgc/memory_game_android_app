package projeto_taes.projeto.history

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await
import projeto_taes.projeto.App

class GameHistoryManager(private val app: App) {
    private val gameHistory: MutableList<GameHistoryEntry> = mutableListOf()

    // Function to add a game record to the local history
    fun addGameToHistory(entry: GameHistoryEntry) {
        gameHistory.add(entry)
    }

    // Function to load game history from the server
    suspend fun loadGameHistory() {
        val completionDeferred = CompletableDeferred<Pair<List<GameHistoryEntry>?, Exception?>>()
        app.fetchGameHistoryFromServer { history, exception ->
            completionDeferred.complete(Pair(history, exception))
        }
        val (fetchedHistory, fetchException) = completionDeferred.await()
        fetchException?.let { throw it }
        fetchedHistory?.let {
            gameHistory.clear()
            gameHistory.addAll(it)
        }
    }

    // Function to send the current game history to the server
    suspend fun pushUpdatesToServer() {
        val completionDeferred = CompletableDeferred<Exception?>()
        app.pushGameHistoryToServer(gameHistory) { exception ->
            completionDeferred.complete(exception)
        }
        completionDeferred.await()?.let { throw it }
    }

    // Function to get the current game history
    fun getGameHistory(): List<GameHistoryEntry> {
        return gameHistory.toList()
    }
}