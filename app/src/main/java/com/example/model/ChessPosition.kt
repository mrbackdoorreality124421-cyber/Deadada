package com.example.model

class ChessPosition(
    val board: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } },
    var activeColor: PieceColor = PieceColor.WHITE,
    var whiteKingsideCastle: Boolean = true,
    var whiteQueensideCastle: Boolean = true,
    var blackKingsideCastle: Boolean = true,
    var blackQueensideCastle: Boolean = true,
    var enPassantSquare: Square? = null,
    var halfmoveClock: Int = 0,
    var fullmoveNumber: Int = 1
) {
    fun pieceAt(square: Square): ChessPiece? = board[square.rank][square.file]
    fun pieceAt(file: Int, rank: Int): ChessPiece? = board[rank][file]

    fun setPieceAt(square: Square, piece: ChessPiece?) {
        board[square.rank][square.file] = piece
    }

    fun setPieceAt(file: Int, rank: Int, piece: ChessPiece?) {
        board[rank][file] = piece
    }

    fun copy(): ChessPosition {
        val newBoard = Array(8) { r -> Array(8) { f -> board[r][f] } }
        return ChessPosition(
            board = newBoard,
            activeColor = activeColor,
            whiteKingsideCastle = whiteKingsideCastle,
            whiteQueensideCastle = whiteQueensideCastle,
            blackKingsideCastle = blackKingsideCastle,
            blackQueensideCastle = blackQueensideCastle,
            enPassantSquare = enPassantSquare,
            halfmoveClock = halfmoveClock,
            fullmoveNumber = fullmoveNumber
        )
    }

    fun toFen(): String {
        val sb = StringBuilder()
        // 1. Piece placement (from rank 8 down to rank 1)
        for (r in 7 downTo 0) {
            var emptyCount = 0
            for (f in 0..7) {
                val piece = board[r][f]
                if (piece == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    sb.append(piece.fenChar())
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount)
            }
            if (r > 0) sb.append('/')
        }

        // 2. Active color
        sb.append(' ')
        sb.append(if (activeColor == PieceColor.WHITE) 'w' else 'b')

        // 3. Castling availability
        sb.append(' ')
        val castling = StringBuilder()
        if (whiteKingsideCastle) castling.append('K')
        if (whiteQueensideCastle) castling.append('Q')
        if (blackKingsideCastle) castling.append('k')
        if (blackQueensideCastle) castling.append('q')
        if (castling.isEmpty()) castling.append('-')
        sb.append(castling)

        // 4. En passant target square
        sb.append(' ')
        sb.append(enPassantSquare?.toAlgebraic() ?: "-")

        // 5. Halfmove clock
        sb.append(' ')
        sb.append(halfmoveClock)

        // 6. Fullmove number
        sb.append(' ')
        sb.append(fullmoveNumber)

        return sb.toString()
    }

    fun getKingSquare(color: PieceColor): Square? {
        for (r in 0..7) {
            for (f in 0..7) {
                val piece = board[r][f]
                if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                    return Square(f, r)
                }
            }
        }
        return null
    }

    fun isSquareAttacked(square: Square, attackingColor: PieceColor): Boolean {
        // 1. Pawn attacks
        val pawnDir = if (attackingColor == PieceColor.WHITE) 1 else -1
        val pawnRank = square.rank - pawnDir
        if (pawnRank in 0..7) {
            for (df in listOf(-1, 1)) {
                val pf = square.file + df
                if (pf in 0..7) {
                    val p = board[pawnRank][pf]
                    if (p != null && p.color == attackingColor && p.type == PieceType.PAWN) {
                        return true
                    }
                }
            }
        }

        // 2. Knight attacks
        val knightOffsets = listOf(
            Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
            Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
        )
        for ((df, dr) in knightOffsets) {
            val f = square.file + df
            val r = square.rank + dr
            if (f in 0..7 && r in 0..7) {
                val p = board[r][f]
                if (p != null && p.color == attackingColor && p.type == PieceType.KNIGHT) {
                    return true
                }
            }
        }

        // 3. King attacks (1 step)
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val f = square.file + df
                val r = square.rank + dr
                if (f in 0..7 && r in 0..7) {
                    val p = board[r][f]
                    if (p != null && p.color == attackingColor && p.type == PieceType.KING) {
                        return true
                    }
                }
            }
        }

        // 4. Straight line attacks (Rook & Queen)
        val straightDirs = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
        for ((df, dr) in straightDirs) {
            var f = square.file + df
            var r = square.rank + dr
            while (f in 0..7 && r in 0..7) {
                val p = board[r][f]
                if (p != null) {
                    if (p.color == attackingColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                f += df
                r += dr
            }
        }

        // 5. Diagonal attacks (Bishop & Queen)
        val diagDirs = listOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1))
        for ((df, dr) in diagDirs) {
            var f = square.file + df
            var r = square.rank + dr
            while (f in 0..7 && r in 0..7) {
                val p = board[r][f]
                if (p != null) {
                    if (p.color == attackingColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                f += df
                r += dr
            }
        }

        return false
    }

    fun isInCheck(color: PieceColor): Boolean {
        val kingSq = getKingSquare(color) ?: return false
        return isSquareAttacked(kingSq, color.opposite())
    }

    fun generatePseudoLegalMoves(forColor: PieceColor = activeColor): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()

        for (r in 0..7) {
            for (f in 0..7) {
                val piece = board[r][f] ?: continue
                if (piece.color != forColor) continue
                val from = Square(f, r)

                when (piece.type) {
                    PieceType.PAWN -> generatePawnMoves(from, piece, moves)
                    PieceType.KNIGHT -> generateKnightMoves(from, piece, moves)
                    PieceType.BISHOP -> generateSlidingMoves(from, piece, listOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)), moves)
                    PieceType.ROOK -> generateSlidingMoves(from, piece, listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)), moves)
                    PieceType.QUEEN -> generateSlidingMoves(from, piece, listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1), Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)), moves)
                    PieceType.KING -> generateKingMoves(from, piece, moves)
                }
            }
        }

        return moves
    }

    private fun generatePawnMoves(from: Square, piece: ChessPiece, moves: MutableList<ChessMove>) {
        val dir = if (piece.color == PieceColor.WHITE) 1 else -1
        val startRank = if (piece.color == PieceColor.WHITE) 1 else 6
        val promoRank = if (piece.color == PieceColor.WHITE) 7 else 0

        // Single step forward
        val nextRank = from.rank + dir
        if (nextRank in 0..7 && board[nextRank][from.file] == null) {
            val to = Square(from.file, nextRank)
            if (nextRank == promoRank) {
                listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT).forEach { promo ->
                    moves.add(ChessMove(from, to, piece, promotion = promo))
                }
            } else {
                moves.add(ChessMove(from, to, piece))
                // Double step forward from initial rank
                val doubleRank = from.rank + 2 * dir
                if (from.rank == startRank && doubleRank in 0..7 && board[doubleRank][from.file] == null) {
                    moves.add(ChessMove(from, Square(from.file, doubleRank), piece))
                }
            }
        }

        // Diagonal captures
        for (df in listOf(-1, 1)) {
            val captureFile = from.file + df
            if (captureFile in 0..7 && nextRank in 0..7) {
                val targetPiece = board[nextRank][captureFile]
                val to = Square(captureFile, nextRank)
                if (targetPiece != null && targetPiece.color != piece.color) {
                    if (nextRank == promoRank) {
                        listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT).forEach { promo ->
                            moves.add(ChessMove(from, to, piece, capturedPiece = targetPiece, promotion = promo))
                        }
                    } else {
                        moves.add(ChessMove(from, to, piece, capturedPiece = targetPiece))
                    }
                } else if (enPassantSquare != null && enPassantSquare == to) {
                    // En passant capture
                    val capturedPawn = board[from.rank][captureFile]
                    moves.add(ChessMove(from, to, piece, capturedPiece = capturedPawn, isEnPassant = true))
                }
            }
        }
    }

    private fun generateKnightMoves(from: Square, piece: ChessPiece, moves: MutableList<ChessMove>) {
        val offsets = listOf(
            Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
            Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
        )
        for ((df, dr) in offsets) {
            val f = from.file + df
            val r = from.rank + dr
            if (f in 0..7 && r in 0..7) {
                val dest = board[r][f]
                if (dest == null) {
                    moves.add(ChessMove(from, Square(f, r), piece))
                } else if (dest.color != piece.color) {
                    moves.add(ChessMove(from, Square(f, r), piece, capturedPiece = dest))
                }
            }
        }
    }

    private fun generateSlidingMoves(from: Square, piece: ChessPiece, dirs: List<Pair<Int, Int>>, moves: MutableList<ChessMove>) {
        for ((df, dr) in dirs) {
            var f = from.file + df
            var r = from.rank + dr
            while (f in 0..7 && r in 0..7) {
                val dest = board[r][f]
                if (dest == null) {
                    moves.add(ChessMove(from, Square(f, r), piece))
                } else {
                    if (dest.color != piece.color) {
                        moves.add(ChessMove(from, Square(f, r), piece, capturedPiece = dest))
                    }
                    break
                }
                f += df
                r += dr
            }
        }
    }

    private fun generateKingMoves(from: Square, piece: ChessPiece, moves: MutableList<ChessMove>) {
        // Normal 1-step moves
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val f = from.file + df
                val r = from.rank + dr
                if (f in 0..7 && r in 0..7) {
                    val dest = board[r][f]
                    if (dest == null) {
                        moves.add(ChessMove(from, Square(f, r), piece))
                    } else if (dest.color != piece.color) {
                        moves.add(ChessMove(from, Square(f, r), piece, capturedPiece = dest))
                    }
                }
            }
        }

        // Castling moves (only checked if king not in check)
        if (piece.color == PieceColor.WHITE && from.file == 4 && from.rank == 0) {
            val opp = PieceColor.BLACK
            // Kingside: e1 -> g1 (files 5, 6 empty and not attacked, e1 not attacked)
            if (whiteKingsideCastle && board[0][5] == null && board[0][6] == null &&
                !isSquareAttacked(Square(4, 0), opp) &&
                !isSquareAttacked(Square(5, 0), opp) &&
                !isSquareAttacked(Square(6, 0), opp)
            ) {
                moves.add(ChessMove(from, Square(6, 0), piece, isCastling = true))
            }
            // Queenside: e1 -> c1 (files 1, 2, 3 empty, 4, 3, 2 not attacked)
            if (whiteQueensideCastle && board[0][1] == null && board[0][2] == null && board[0][3] == null &&
                !isSquareAttacked(Square(4, 0), opp) &&
                !isSquareAttacked(Square(3, 0), opp) &&
                !isSquareAttacked(Square(2, 0), opp)
            ) {
                moves.add(ChessMove(from, Square(2, 0), piece, isCastling = true))
            }
        } else if (piece.color == PieceColor.BLACK && from.file == 4 && from.rank == 7) {
            val opp = PieceColor.WHITE
            // Kingside: e8 -> g8 (files 5, 6 empty and not attacked, e8 not attacked)
            if (blackKingsideCastle && board[7][5] == null && board[7][6] == null &&
                !isSquareAttacked(Square(4, 7), opp) &&
                !isSquareAttacked(Square(5, 7), opp) &&
                !isSquareAttacked(Square(6, 7), opp)
            ) {
                moves.add(ChessMove(from, Square(6, 7), piece, isCastling = true))
            }
            // Queenside: e8 -> c8 (files 1, 2, 3 empty, 4, 3, 2 not attacked)
            if (blackQueensideCastle && board[7][1] == null && board[7][2] == null && board[7][3] == null &&
                !isSquareAttacked(Square(4, 7), opp) &&
                !isSquareAttacked(Square(3, 7), opp) &&
                !isSquareAttacked(Square(2, 7), opp)
            ) {
                moves.add(ChessMove(from, Square(2, 7), piece, isCastling = true))
            }
        }
    }

    fun makeMove(move: ChessMove): ChessPosition {
        val next = this.copy()
        val piece = move.piece
        val from = move.from
        val to = move.to

        // Update halfmove clock
        if (piece.type == PieceType.PAWN || move.capturedPiece != null) {
            next.halfmoveClock = 0
        } else {
            next.halfmoveClock++
        }

        // Fullmove number increment on Black's turn completion
        if (next.activeColor == PieceColor.BLACK) {
            next.fullmoveNumber++
        }

        // Clear piece at source
        next.setPieceAt(from, null)

        // Handle Castling rook movement
        if (move.isCastling) {
            if (to.rank == 0 && to.file == 6) {
                // White kingside: rook h1 -> f1
                val rook = next.pieceAt(7, 0)
                next.setPieceAt(7, 0, null)
                next.setPieceAt(5, 0, rook)
            } else if (to.rank == 0 && to.file == 2) {
                // White queenside: rook a1 -> d1
                val rook = next.pieceAt(0, 0)
                next.setPieceAt(0, 0, null)
                next.setPieceAt(3, 0, rook)
            } else if (to.rank == 7 && to.file == 6) {
                // Black kingside: rook h8 -> f8
                val rook = next.pieceAt(7, 7)
                next.setPieceAt(7, 7, null)
                next.setPieceAt(5, 7, rook)
            } else if (to.rank == 7 && to.file == 2) {
                // Black queenside: rook a8 -> d8
                val rook = next.pieceAt(0, 7)
                next.setPieceAt(0, 7, null)
                next.setPieceAt(3, 7, rook)
            }
        }

        // Handle En Passant capture removal
        if (move.isEnPassant) {
            val epPawnRank = if (piece.color == PieceColor.WHITE) to.rank - 1 else to.rank + 1
            next.setPieceAt(to.file, epPawnRank, null)
        }

        // Set piece at destination (handling promotion)
        val finalPiece = if (move.promotion != null) {
            ChessPiece(move.promotion, piece.color)
        } else {
            piece
        }
        next.setPieceAt(to, finalPiece)

        // Set en passant square for next move if 2-square pawn push
        if (piece.type == PieceType.PAWN && Math.abs(to.rank - from.rank) == 2) {
            val epRank = (to.rank + from.rank) / 2
            next.enPassantSquare = Square(from.file, epRank)
        } else {
            next.enPassantSquare = null
        }

        // Update castling rights
        if (piece.type == PieceType.KING) {
            if (piece.color == PieceColor.WHITE) {
                next.whiteKingsideCastle = false
                next.whiteQueensideCastle = false
            } else {
                next.blackKingsideCastle = false
                next.blackQueensideCastle = false
            }
        }
        // If rook moves or is captured, lose corresponding castling right
        if (from.rank == 0 && from.file == 0) next.whiteQueensideCastle = false
        if (from.rank == 0 && from.file == 7) next.whiteKingsideCastle = false
        if (from.rank == 7 && from.file == 0) next.blackQueensideCastle = false
        if (from.rank == 7 && from.file == 7) next.blackKingsideCastle = false

        if (to.rank == 0 && to.file == 0) next.whiteQueensideCastle = false
        if (to.rank == 0 && to.file == 7) next.whiteKingsideCastle = false
        if (to.rank == 7 && to.file == 0) next.blackQueensideCastle = false
        if (to.rank == 7 && to.file == 7) next.blackKingsideCastle = false

        // Switch active color
        next.activeColor = next.activeColor.opposite()

        return next
    }

    fun getLegalMoves(): List<ChessMove> {
        val pseudoMoves = generatePseudoLegalMoves(activeColor)
        val legalMoves = mutableListOf<ChessMove>()

        for (move in pseudoMoves) {
            val after = makeMove(move)
            // If the king of the side that moved is in check, move is illegal
            if (!after.isInCheck(activeColor)) {
                // Enrich with SAN notation
                val isCheck = after.isInCheck(activeColor.opposite())
                val san = formatSan(move, pseudoMoves, isCheck)
                legalMoves.add(move.copy(sanNotation = san))
            }
        }

        return legalMoves
    }

    private fun formatSan(move: ChessMove, pseudoMoves: List<ChessMove>, leadsToCheck: Boolean): String {
        if (move.isCastling) {
            val base = if (move.to.file == 6) "O-O" else "O-O-O"
            return if (leadsToCheck) "$base+" else base
        }

        val sb = StringBuilder()
        val piece = move.piece

        if (piece.type == PieceType.PAWN) {
            if (move.capturedPiece != null || move.isEnPassant) {
                sb.append(('a'.code + move.from.file).toChar())
                sb.append('x')
            }
            sb.append(move.to.toAlgebraic())
            if (move.promotion != null) {
                sb.append('=')
                sb.append(move.promotion.notation.ifEmpty { "Q" })
            }
        } else {
            sb.append(piece.type.notation)
            // Disambiguation
            val duplicates = pseudoMoves.filter {
                it.piece == piece && it.to == move.to && it.from != move.from
            }
            if (duplicates.isNotEmpty()) {
                val sameFile = duplicates.any { it.from.file == move.from.file }
                val sameRank = duplicates.any { it.from.rank == move.from.rank }
                if (!sameFile) {
                    sb.append(('a'.code + move.from.file).toChar())
                } else if (!sameRank) {
                    sb.append(('1'.code + move.from.rank).toChar())
                } else {
                    sb.append(move.from.toAlgebraic())
                }
            }
            if (move.capturedPiece != null) {
                sb.append('x')
            }
            sb.append(move.to.toAlgebraic())
        }

        if (leadsToCheck) {
            sb.append('+')
        }

        return sb.toString()
    }

    fun isInsufficientMaterial(): Boolean {
        val pieces = mutableListOf<ChessPiece>()
        for (r in 0..7) {
            for (f in 0..7) {
                board[r][f]?.let { pieces.add(it) }
            }
        }

        // King vs King
        if (pieces.size == 2) return true

        // King + Minor Piece vs King
        if (pieces.size == 3) {
            val nonKings = pieces.filter { it.type != PieceType.KING }
            if (nonKings.size == 1) {
                val p = nonKings.first()
                if (p.type == PieceType.BISHOP || p.type == PieceType.KNIGHT) return true
            }
        }

        return false
    }

    fun evaluateGameStatus(positionHistory: List<String>): GameStatus {
        val legalMoves = getLegalMoves()
        val inCheck = isInCheck(activeColor)

        if (legalMoves.isEmpty()) {
            return if (inCheck) GameStatus.CHECKMATE else GameStatus.STALEMATE
        }

        if (halfmoveClock >= 100) {
            return GameStatus.DRAW_50_MOVES
        }

        if (isInsufficientMaterial()) {
            return GameStatus.DRAW_INSUFFICIENT_MATERIAL
        }

        // Repetition check (3-fold)
        val currentFenCore = toFen().substringBeforeLast(' ').substringBeforeLast(' ')
        val repetitionCount = positionHistory.count { it.substringBeforeLast(' ').substringBeforeLast(' ') == currentFenCore }
        if (repetitionCount >= 3) {
            return GameStatus.DRAW_REPETITION
        }

        return if (inCheck) GameStatus.CHECK else GameStatus.IN_PROGRESS
    }

    fun parseUciMove(uci: String): ChessMove? {
        val cleanUci = uci.trim()
        if (cleanUci.length < 4) return null
        val fromAlg = cleanUci.substring(0, 2)
        val toAlg = cleanUci.substring(2, 4)
        val promoChar = if (cleanUci.length >= 5) cleanUci[4].lowercaseChar() else null

        val from = Square.fromAlgebraic(fromAlg) ?: return null
        val to = Square.fromAlgebraic(toAlg) ?: return null
        val promoType = when (promoChar) {
            'q' -> PieceType.QUEEN
            'r' -> PieceType.ROOK
            'b' -> PieceType.BISHOP
            'n' -> PieceType.KNIGHT
            else -> null
        }

        val legalMoves = getLegalMoves()
        return legalMoves.find { it.from == from && it.to == to && (promoType == null || it.promotion == promoType) }
    }

    companion object {
        fun initial(): ChessPosition {
            return fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        }

        fun fromFen(fen: String): ChessPosition {
            val parts = fen.trim().split(" ")
            val board = Array(8) { Array<ChessPiece?>(8) { null } }

            val ranks = parts[0].split("/")
            for (r in 0..7) {
                val rankStr = ranks[7 - r]
                var f = 0
                for (c in rankStr) {
                    if (c.isDigit()) {
                        f += c.digitToInt()
                    } else {
                        board[r][f] = ChessPiece.fromFenChar(c)
                        f++
                    }
                }
            }

            val activeColor = if (parts.getOrNull(1) == "b") PieceColor.BLACK else PieceColor.WHITE
            val castling = parts.getOrNull(2) ?: "KQkq"
            val whiteKingsideCastle = castling.contains('K')
            val whiteQueensideCastle = castling.contains('Q')
            val blackKingsideCastle = castling.contains('k')
            val blackQueensideCastle = castling.contains('q')

            val ep = parts.getOrNull(3)
            val enPassantSquare = if (ep != null && ep != "-") Square.fromAlgebraic(ep) else null
            val halfmoveClock = parts.getOrNull(4)?.toIntOrNull() ?: 0
            val fullmoveNumber = parts.getOrNull(5)?.toIntOrNull() ?: 1

            return ChessPosition(
                board = board,
                activeColor = activeColor,
                whiteKingsideCastle = whiteKingsideCastle,
                whiteQueensideCastle = whiteQueensideCastle,
                blackKingsideCastle = blackKingsideCastle,
                blackQueensideCastle = blackQueensideCastle,
                enPassantSquare = enPassantSquare,
                halfmoveClock = halfmoveClock,
                fullmoveNumber = fullmoveNumber
            )
        }
    }
}
