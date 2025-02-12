package projeto_taes.projeto.scoreboards;

import android.util.Log
import projeto_taes.projeto.App

class ScoreManager(val app: App) {
    private val personalScores: MutableMap<String, Score> = mutableMapOf(
        "3x4" to Score.NOT_SET,
        "4x4" to Score.NOT_SET,
        "6x6" to Score.NOT_SET
    )

    private val globalScores: MutableMap<String, MutableList<ScoreEntry>> = mutableMapOf(
        "3x4" to mutableListOf(),
        "4x4" to mutableListOf(),
        "6x6" to mutableListOf()
    )

    suspend fun loadPersonalScores(): Boolean {
        return try {
            val scoresFromServer = app.fetchPersonalScores()

            scoresFromServer.forEach { (boardSize, scoreDetails) ->
                val score = Score(
                    moves = scoreDetails["moves"] ?: Score.INITIAL_SCORE, // DEFAULT
                    time = scoreDetails["time"] ?: Score.INITIAL_SCORE  // DEFAULT
                )
                personalScores[boardSize] = score
            }

            val boardSizes = listOf("3x4", "4x4", "6x6")
            boardSizes.forEach { boardSize ->
                personalScores.putIfAbsent(boardSize, Score.DEFAULT)
            }

            true
        } catch (e: Exception) {
            Log.e("debug", e.toString(), e)
            false
        }
    }

    suspend fun loadGlobalScores(): Boolean {
        return try {
            val globalScoresFromServer = app.fetchGlobalScores()

            // Update existing scores
            globalScoresFromServer.forEach { (boardSize, scoreEntries) ->
                globalScores[boardSize] = scoreEntries.toMutableList().apply {
                    sortWith(compareBy({ it.score.moves }, { it.score.time }))
                }
            }

            // Ensure that all board sizes have lists, even if not returned by the server
            val boardSizes = listOf("3x4", "4x4", "6x6")
            boardSizes.forEach { boardSize ->
                globalScores.putIfAbsent(boardSize, mutableListOf())
            }

            true
        } catch (e: Exception) {
            Log.e("debug", e.toString(), e)
            false
        }
    }

    fun updatePersonalScore(boardSize: String, newScore: Score, onComplete: (Exception?) -> Unit) {
        personalScores[boardSize] = newScore
        app.savePersonalScore(boardSize, newScore, onComplete)
    }

    fun updateGlobalScore(username: String, boardSize: String, newScore: Score, onComplete: (Exception?) -> Unit) {
        val scores = globalScores.getOrPut(boardSize) { mutableListOf() }
        val existingScoreEntry = scores.find { it.username == username }

        if (existingScoreEntry != null) {
            existingScoreEntry.score = newScore
        } else {
            scores.add(ScoreEntry(username, newScore))
        }

        scores.sortWith(compareBy({ it.score.moves }, { it.score.time }))

        // Save the updated global scores to Firebase
        app.saveIndividualGlobalScore(boardSize, ScoreEntry(username, newScore), onComplete)
    }

    fun getPersonalScore(boardSize: String): Score = personalScores.getOrDefault(boardSize, Score.DEFAULT)
    fun getGlobalScores(boardSize: String): List<ScoreEntry> = globalScores.getOrDefault(boardSize, mutableListOf())
    fun getGlobalScoreFor3x4(): List<ScoreEntry> = getGlobalScores("3x4")
    fun getGlobalScoreFor4x4(): List<ScoreEntry> = getGlobalScores("4x4")
    fun getGlobalScoreFor6x6(): List<ScoreEntry> = getGlobalScores("6x6")


    fun isTopThreeGlobalScore(boardSize: String, newScore: Score): Boolean {
        val scores = globalScores[boardSize] ?: return true // If no scores exist, newScore qualifies
        return scores.size < 3 || newScore.isBetterThan(scores.last().score)
    }

    fun isGlobalScoresLoaded(): Boolean {
        val boardSizes = listOf("3x4", "4x4", "6x6")
        return boardSizes.all { boardSize -> globalScores.containsKey(boardSize) }
    }

    fun isPersonalScoreLoaded(): Boolean {
        val boardSizes = listOf("3x4", "4x4", "6x6")
        return boardSizes.all { boardSize -> personalScores[boardSize] != Score.NOT_SET }
    }

    fun resetPersonalScore() {
        personalScores.keys.forEach { boardSize ->
            personalScores[boardSize] = Score.NOT_SET
        }
    }
}
