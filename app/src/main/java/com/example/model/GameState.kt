package com.example.model

enum class GameStatus {
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_50_MOVES,
    DRAW_REPETITION,
    DRAW_INSUFFICIENT_MATERIAL
}

data class Difficulty(
    val level: Int,
    val name: String,
    val moveTimeMs: Long,
    val searchDepth: Int,
    val skillLevel: Int
) {
    companion object {
        val BEGINNER = Difficulty(1, "Beginner", 500L, 8, 3)
        val EASY = Difficulty(2, "Easy", 1500L, 12, 8)
        val MEDIUM = Difficulty(3, "Medium", 3000L, 18, 14)
        val HARD = Difficulty(4, "Hard", 5000L, 25, 18)
        val MASTER = Difficulty(5, "Master", 10000L, 30, 20)

        val ALL = listOf(BEGINNER, EASY, MEDIUM, HARD, MASTER)
    }
}

data class EngineEvaluation(
    val scoreCp: Int? = null,
    val mateIn: Int? = null,
    val depth: Int = 0,
    val bestMoveUci: String? = null,
    val pv: String? = null
) {
    fun toDisplayText(): String {
        return when {
            mateIn != null -> if (mateIn > 0) "M+$mateIn" else "M$mateIn"
            scoreCp != null -> {
                val pawns = scoreCp / 100.0
                if (pawns > 0) "+%.1f".format(pawns) else "%.1f".format(pawns)
            }
            else -> "0.0"
        }
    }
}
