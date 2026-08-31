package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BoardTheme
import com.example.model.ChessMove
import com.example.model.ChessPiece
import com.example.model.ChessPosition
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.Square
import kotlin.math.floor

@Composable
fun ChessBoard(
    position: ChessPosition,
    boardTheme: BoardTheme,
    selectedSquare: Square?,
    legalMovesForSelected: List<ChessMove>,
    lastMove: ChessMove?,
    stockfishArrowMove: Pair<Square, Square>?,
    isStockfishTurn: Boolean,
    flipped: Boolean, // true if Black at bottom
    isInteractive: Boolean,
    onSquareTapped: (Square) -> Unit,
    onMoveAttempt: (from: Square, to: Square) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggingSquare by remember { mutableStateOf<Square?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Pulsing animation for Stockfish's arrow and check glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowAlpha"
    )

    val checkGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "checkGlowAlpha"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(12.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .testTag("chess_board")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isInteractive, flipped) {
                    if (!isInteractive) return@pointerInput
                    detectTapGestures { offset ->
                        val squareSize = size.width / 8f
                        val col = floor(offset.x / squareSize).toInt().coerceIn(0, 7)
                        val row = floor(offset.y / squareSize).toInt().coerceIn(0, 7)

                        val file = if (flipped) 7 - col else col
                        val rank = if (flipped) row else 7 - row
                        onSquareTapped(Square(file, rank))
                    }
                }
                .pointerInput(isInteractive, flipped, position) {
                    if (!isInteractive) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            val squareSize = size.width / 8f
                            val col = floor(offset.x / squareSize).toInt().coerceIn(0, 7)
                            val row = floor(offset.y / squareSize).toInt().coerceIn(0, 7)
                            val file = if (flipped) 7 - col else col
                            val rank = if (flipped) row else 7 - row
                            val sq = Square(file, rank)
                            val piece = position.pieceAt(sq)
                            if (piece != null && piece.color == position.activeColor) {
                                draggingSquare = sq
                                dragOffset = offset
                                onSquareTapped(sq)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        },
                        onDragEnd = {
                            val from = draggingSquare
                            if (from != null) {
                                val squareSize = size.width / 8f
                                val col = floor(dragOffset.x / squareSize).toInt().coerceIn(0, 7)
                                val row = floor(dragOffset.y / squareSize).toInt().coerceIn(0, 7)
                                val file = if (flipped) 7 - col else col
                                val rank = if (flipped) row else 7 - row
                                val to = Square(file, rank)
                                if (from != to) {
                                    onMoveAttempt(from, to)
                                }
                            }
                            draggingSquare = null
                        },
                        onDragCancel = {
                            draggingSquare = null
                        }
                    )
                }
        ) {
            val squareSize = size.width / 8f

            // 1. Draw 8x8 Board Squares
            for (row in 0..7) {
                for (col in 0..7) {
                    val file = if (flipped) 7 - col else col
                    val rank = if (flipped) row else 7 - row
                    val isLight = (file + rank) % 2 != 0

                    val squareColor = if (isLight) boardTheme.lightSquare else boardTheme.darkSquare
                    drawRect(
                        color = squareColor,
                        topLeft = Offset(col * squareSize, row * squareSize),
                        size = Size(squareSize, squareSize)
                    )
                }
            }

            // 2. Draw Last Move Highlight
            if (lastMove != null) {
                val fromCol = if (flipped) 7 - lastMove.from.file else lastMove.from.file
                val fromRow = if (flipped) lastMove.from.rank else 7 - lastMove.from.rank
                val toCol = if (flipped) 7 - lastMove.to.file else lastMove.to.file
                val toRow = if (flipped) lastMove.to.rank else 7 - lastMove.to.rank

                drawRect(
                    color = boardTheme.lastMoveColor,
                    topLeft = Offset(fromCol * squareSize, fromRow * squareSize),
                    size = Size(squareSize, squareSize)
                )
                drawRect(
                    color = boardTheme.lastMoveColor,
                    topLeft = Offset(toCol * squareSize, toRow * squareSize),
                    size = Size(squareSize, squareSize)
                )
            }

            // 3. Draw Selected Square Highlight
            if (selectedSquare != null) {
                val col = if (flipped) 7 - selectedSquare.file else selectedSquare.file
                val row = if (flipped) selectedSquare.rank else 7 - selectedSquare.rank
                drawRect(
                    color = boardTheme.highlightColor,
                    topLeft = Offset(col * squareSize, row * squareSize),
                    size = Size(squareSize, squareSize)
                )
                drawRect(
                    color = Color(0xAAFFFFFF),
                    topLeft = Offset(col * squareSize + 2, row * squareSize + 2),
                    size = Size(squareSize - 4, squareSize - 4),
                    style = Stroke(width = squareSize * 0.04f)
                )
            }

            // 4. Draw King in Check Highlight
            val inCheckColor = if (position.isInCheck(PieceColor.WHITE)) PieceColor.WHITE
            else if (position.isInCheck(PieceColor.BLACK)) PieceColor.BLACK
            else null

            if (inCheckColor != null) {
                val kingSq = position.getKingSquare(inCheckColor)
                if (kingSq != null) {
                    val kCol = if (flipped) 7 - kingSq.file else kingSq.file
                    val kRow = if (flipped) kingSq.rank else 7 - kingSq.rank
                    val center = Offset(kCol * squareSize + squareSize / 2f, kRow * squareSize + squareSize / 2f)

                    drawCircle(
                        color = Color(0xEEFF3B30).copy(alpha = checkGlowAlpha),
                        radius = squareSize * 0.46f,
                        center = center,
                        style = Fill
                    )
                }
            }

            // 5. Draw Board Pieces (except the one actively dragging)
            for (r in 0..7) {
                for (f in 0..7) {
                    val sq = Square(f, r)
                    if (sq == draggingSquare) continue
                    val piece = position.pieceAt(sq) ?: continue

                    val col = if (flipped) 7 - f else f
                    val row = if (flipped) r else 7 - r
                    val topLeft = Offset(col * squareSize, row * squareSize)

                    PieceRenderer.drawPiece(
                        drawScope = this,
                        piece = piece,
                        topLeft = topLeft,
                        squareSize = squareSize
                    )
                }
            }

            // 6. Draw Legal Move Dots (when piece is selected)
            if (isInteractive && selectedSquare != null) {
                for (move in legalMovesForSelected) {
                    val isCapture = move.capturedPiece != null || move.isEnPassant
                    LegalMoveDots.drawHint(
                        drawScope = this,
                        square = move.to,
                        isCapture = isCapture,
                        squareSize = squareSize,
                        flipped = flipped,
                        hintColor = if (isCapture) Color(0x99D32F2F) else Color(0x661E2022)
                    )
                }
            }

            // 7. Draw Stockfish Best Move Arrow (ONLY on Stockfish's Turn)
            if (isStockfishTurn && stockfishArrowMove != null) {
                MoveArrow.drawArrow(
                    drawScope = this,
                    from = stockfishArrowMove.first,
                    to = stockfishArrowMove.second,
                    squareSize = squareSize,
                    flipped = flipped,
                    color = Color(0xEE00E676),
                    alphaMultiplier = arrowAlpha
                )
            }

            // 8. Draw Dragging Piece under touch point with elevation
            val dragSq = draggingSquare
            if (dragSq != null) {
                val dragPiece = position.pieceAt(dragSq)
                if (dragPiece != null) {
                    val dragTopLeft = Offset(
                        dragOffset.x - squareSize / 2f,
                        dragOffset.y - squareSize / 2f
                    )
                    // Drop shadow under dragging piece
                    drawCircle(
                        color = Color(0x44000000),
                        radius = squareSize * 0.40f,
                        center = Offset(dragOffset.x, dragOffset.y + squareSize * 0.15f)
                    )
                    PieceRenderer.drawPiece(
                        drawScope = this,
                        piece = dragPiece,
                        topLeft = dragTopLeft,
                        squareSize = squareSize * 1.15f // slight scale up for great tactile feel
                    )
                }
            }
        }
    }
}
