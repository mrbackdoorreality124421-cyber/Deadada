package com.example.model

data class Square(
    val file: Int, // 0 = 'a', 7 = 'h'
    val rank: Int  // 0 = '1', 7 = '8'
) {
    init {
        require(file in 0..7 && rank in 0..7) { "Square coordinate out of range: file=$file, rank=$rank" }
    }

    val isLight: Boolean
        get() = (file + rank) % 2 != 0

    fun toAlgebraic(): String {
        val fileChar = ('a'.code + file).toChar()
        val rankChar = ('1'.code + rank).toChar()
        return "$fileChar$rankChar"
    }

    companion object {
        fun fromAlgebraic(alg: String): Square? {
            if (alg.length != 2) return null
            val fileChar = alg[0].lowercaseChar()
            val rankChar = alg[1]
            if (fileChar !in 'a'..'h' || rankChar !in '1'..'8') return null
            return Square(fileChar - 'a', rankChar - '1')
        }

        fun at(file: Int, rank: Int): Square = Square(file, rank)
    }
}

data class ChessMove(
    val from: Square,
    val to: Square,
    val piece: ChessPiece,
    val capturedPiece: ChessPiece? = null,
    val promotion: PieceType? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false,
    val sanNotation: String = ""
) {
    fun toUci(): String {
        val base = "${from.toAlgebraic()}${to.toAlgebraic()}"
        return if (promotion != null) {
            val promoChar = when (promotion) {
                PieceType.QUEEN -> "q"
                PieceType.ROOK -> "r"
                PieceType.BISHOP -> "b"
                PieceType.KNIGHT -> "n"
                else -> "q"
            }
            "$base$promoChar"
        } else {
            base
        }
    }
}
