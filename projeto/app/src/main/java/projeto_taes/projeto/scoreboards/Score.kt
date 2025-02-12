package projeto_taes.projeto.scoreboards

class Score(var moves: Int, var time: Int) {
    companion object {
        fun bestOfBoth(newScore: Score, oldScore: Score): Score {
            return Score(
                moves = minOf(newScore.moves, oldScore.moves), // The best move score
                time = minOf(newScore.time, oldScore.time)     // The best time score
            )
        }

        const val NO_SCORE = -1
        const val INITIAL_SCORE = Int.MAX_VALUE
        val NOT_SET = Score(NO_SCORE, NO_SCORE)
        val DEFAULT = Score(INITIAL_SCORE, INITIAL_SCORE)
    }

    fun isBetterThan(other: Score): Boolean {
        return this.moves < other.moves || this.time < other.time
    }

    // Override equals to compare values instead of reference
    override fun equals(other: Any?): Boolean {
        if (this === other) return true // Same reference, return true
        if (other !is Score) return false // If not a Score, return false

        // Compare by values
        return this.moves == other.moves && this.time == other.time
    }

    // Override hashCode to ensure consistency with equals
    override fun hashCode(): Int {
        return 31 * moves + time // A basic hash function
    }
}