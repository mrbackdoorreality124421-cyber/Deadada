package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ChessPiece
import com.example.model.PieceColor
import com.example.model.PieceType

object PieceRenderer {

    fun drawPiece(
        drawScope: DrawScope,
        piece: ChessPiece,
        topLeft: Offset,
        squareSize: Float
    ) {
        val fillColor = if (piece.color == PieceColor.WHITE) Color(0xFFFBFBFB) else Color(0xFF222428)
        val strokeColor = if (piece.color == PieceColor.WHITE) Color(0xFF1E2022) else Color(0xFFEDEFEF)
        val shadowColor = if (piece.color == PieceColor.WHITE) Color(0x339E9E9E) else Color(0x66000000)

        val padding = squareSize * 0.12f
        val w = squareSize - (padding * 2)
        val h = squareSize - (padding * 2)
        val left = topLeft.x + padding
        val top = topLeft.y + padding

        when (piece.type) {
            PieceType.PAWN -> drawPawn(drawScope, left, top, w, h, fillColor, strokeColor, shadowColor)
            PieceType.KNIGHT -> drawKnight(drawScope, left, top, w, h, fillColor, strokeColor, shadowColor, piece.color)
            PieceType.BISHOP -> drawBishop(drawScope, left, top, w, h, fillColor, strokeColor, shadowColor)
            PieceType.ROOK -> drawRook(drawScope, left, top, w, h, fillColor, strokeColor, shadowColor)
            PieceType.QUEEN -> drawQueen(drawScope, left, top, w, h, fillColor, strokeColor, shadowColor)
            PieceType.KING -> drawKing(drawScope, left, top, w, h, fillColor, strokeColor, shadowColor)
        }
    }

    private fun drawPawn(
        scope: DrawScope,
        l: Float, t: Float, w: Float, h: Float,
        fill: Color, stroke: Color, shadow: Color
    ) {
        val cx = l + w / 2f
        val strokeWidth = w * 0.045f

        // Head (circle)
        scope.drawCircle(
            color = fill,
            radius = w * 0.20f,
            center = Offset(cx, t + h * 0.28f),
            style = Fill
        )
        scope.drawCircle(
            color = stroke,
            radius = w * 0.20f,
            center = Offset(cx, t + h * 0.28f),
            style = Stroke(width = strokeWidth)
        )

        // Body & Base
        val path = Path().apply {
            moveTo(cx - w * 0.14f, t + h * 0.44f)
            cubicTo(
                cx - w * 0.12f, t + h * 0.60f,
                cx - w * 0.28f, t + h * 0.78f,
                cx - w * 0.35f, t + h * 0.88f
            )
            lineTo(cx + w * 0.35f, t + h * 0.88f)
            cubicTo(
                cx + w * 0.28f, t + h * 0.78f,
                cx + w * 0.12f, t + h * 0.60f,
                cx + w * 0.14f, t + h * 0.44f
            )
            close()
        }
        scope.drawPath(path, fill, style = Fill)
        scope.drawPath(path, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Base bottom rounded rect
        val baseRect = Rect(cx - w * 0.38f, t + h * 0.85f, cx + w * 0.38f, t + h * 0.94f)
        scope.drawRoundRect(fill, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        scope.drawRoundRect(stroke, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f), style = Stroke(width = strokeWidth))
    }

    private fun drawKnight(
        scope: DrawScope,
        l: Float, t: Float, w: Float, h: Float,
        fill: Color, stroke: Color, shadow: Color,
        pieceColor: PieceColor
    ) {
        val cx = l + w / 2f
        val strokeWidth = w * 0.045f

        val path = Path().apply {
            moveTo(cx - w * 0.28f, t + h * 0.88f)
            lineTo(cx + w * 0.32f, t + h * 0.88f)
            cubicTo(
                cx + w * 0.28f, t + h * 0.65f,
                cx + w * 0.35f, t + h * 0.42f,
                cx + w * 0.18f, t + h * 0.22f
            )
            lineTo(cx + w * 0.12f, t + h * 0.12f) // ear tip
            lineTo(cx - w * 0.02f, t + h * 0.22f) // mane curve
            cubicTo(
                cx - w * 0.18f, t + h * 0.22f,
                cx - w * 0.38f, t + h * 0.32f,
                cx - w * 0.40f, t + h * 0.45f // snout tip
            )
            lineTo(cx - w * 0.32f, t + h * 0.54f) // chin
            cubicTo(
                cx - w * 0.18f, t + h * 0.52f,
                cx - w * 0.15f, t + h * 0.58f,
                cx - w * 0.24f, t + h * 0.72f
            )
            close()
        }

        scope.drawPath(path, fill, style = Fill)
        scope.drawPath(path, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Eye
        val eyeColor = if (pieceColor == PieceColor.WHITE) stroke else fill
        scope.drawCircle(
            color = eyeColor,
            radius = w * 0.04f,
            center = Offset(cx - w * 0.18f, t + h * 0.32f)
        )

        // Mane details
        val mane = Path().apply {
            moveTo(cx + w * 0.08f, t + h * 0.24f)
            lineTo(cx + w * 0.18f, t + h * 0.38f)
            moveTo(cx + w * 0.14f, t + h * 0.38f)
            lineTo(cx + w * 0.24f, t + h * 0.52f)
        }
        scope.drawPath(mane, stroke, style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round))

        // Base
        val baseRect = Rect(cx - w * 0.38f, t + h * 0.85f, cx + w * 0.38f, t + h * 0.94f)
        scope.drawRoundRect(fill, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        scope.drawRoundRect(stroke, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f), style = Stroke(width = strokeWidth))
    }

    private fun drawBishop(
        scope: DrawScope,
        l: Float, t: Float, w: Float, h: Float,
        fill: Color, stroke: Color, shadow: Color
    ) {
        val cx = l + w / 2f
        val strokeWidth = w * 0.045f

        // Top finial
        scope.drawCircle(fill, w * 0.06f, Offset(cx, t + h * 0.12f))
        scope.drawCircle(stroke, w * 0.06f, Offset(cx, t + h * 0.12f), style = Stroke(width = strokeWidth * 0.8f))

        // Mitre (head)
        val mitre = Path().apply {
            moveTo(cx, t + h * 0.16f)
            cubicTo(
                cx + w * 0.28f, t + h * 0.22f,
                cx + w * 0.26f, t + h * 0.44f,
                cx + w * 0.15f, t + h * 0.55f
            )
            lineTo(cx - w * 0.15f, t + h * 0.55f)
            cubicTo(
                cx - w * 0.26f, t + h * 0.44f,
                cx - w * 0.28f, t + h * 0.22f,
                cx, t + h * 0.16f
            )
            close()
        }
        scope.drawPath(mitre, fill, style = Fill)
        scope.drawPath(mitre, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Slit cut
        val cut = Path().apply {
            moveTo(cx - w * 0.10f, t + h * 0.28f)
            lineTo(cx + w * 0.08f, t + h * 0.40f)
        }
        scope.drawPath(cut, stroke, style = Stroke(width = strokeWidth * 1.1f, cap = StrokeCap.Round))

        // Body & Base
        val body = Path().apply {
            moveTo(cx - w * 0.15f, t + h * 0.55f)
            cubicTo(
                cx - w * 0.12f, t + h * 0.68f,
                cx - w * 0.28f, t + h * 0.78f,
                cx - w * 0.35f, t + h * 0.88f
            )
            lineTo(cx + w * 0.35f, t + h * 0.88f)
            cubicTo(
                cx + w * 0.28f, t + h * 0.78f,
                cx + w * 0.12f, t + h * 0.68f,
                cx + w * 0.15f, t + h * 0.55f
            )
            close()
        }
        scope.drawPath(body, fill, style = Fill)
        scope.drawPath(body, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        val baseRect = Rect(cx - w * 0.38f, t + h * 0.85f, cx + w * 0.38f, t + h * 0.94f)
        scope.drawRoundRect(fill, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        scope.drawRoundRect(stroke, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f), style = Stroke(width = strokeWidth))
    }

    private fun drawRook(
        scope: DrawScope,
        l: Float, t: Float, w: Float, h: Float,
        fill: Color, stroke: Color, shadow: Color
    ) {
        val cx = l + w / 2f
        val strokeWidth = w * 0.045f

        // Crenels / Castle Battlements
        val castle = Path().apply {
            moveTo(cx - w * 0.32f, t + h * 0.18f)
            lineTo(cx - w * 0.32f, t + h * 0.30f)
            lineTo(cx - w * 0.18f, t + h * 0.30f)
            lineTo(cx - w * 0.18f, t + h * 0.22f)
            lineTo(cx - w * 0.06f, t + h * 0.22f)
            lineTo(cx - w * 0.06f, t + h * 0.30f)
            lineTo(cx + w * 0.06f, t + h * 0.30f)
            lineTo(cx + w * 0.06f, t + h * 0.22f)
            lineTo(cx + w * 0.18f, t + h * 0.22f)
            lineTo(cx + w * 0.18f, t + h * 0.30f)
            lineTo(cx + w * 0.32f, t + h * 0.30f)
            lineTo(cx + w * 0.32f, t + h * 0.18f)
            lineTo(cx + w * 0.24f, t + h * 0.38f)
            lineTo(cx - w * 0.24f, t + h * 0.38f)
            close()
        }
        scope.drawPath(castle, fill, style = Fill)
        scope.drawPath(castle, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Tower Body
        val tower = Path().apply {
            moveTo(cx - w * 0.22f, t + h * 0.38f)
            lineTo(cx - w * 0.26f, t + h * 0.85f)
            lineTo(cx + w * 0.26f, t + h * 0.85f)
            lineTo(cx + w * 0.22f, t + h * 0.38f)
            close()
        }
        scope.drawPath(tower, fill, style = Fill)
        scope.drawPath(tower, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Base
        val baseRect = Rect(cx - w * 0.38f, t + h * 0.85f, cx + w * 0.38f, t + h * 0.94f)
        scope.drawRoundRect(fill, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        scope.drawRoundRect(stroke, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f), style = Stroke(width = strokeWidth))
    }

    private fun drawQueen(
        scope: DrawScope,
        l: Float, t: Float, w: Float, h: Float,
        fill: Color, stroke: Color, shadow: Color
    ) {
        val cx = l + w / 2f
        val strokeWidth = w * 0.045f

        // Crown points with small balls
        val crown = Path().apply {
            moveTo(cx - w * 0.35f, t + h * 0.22f)
            lineTo(cx - w * 0.20f, t + h * 0.38f)
            lineTo(cx - w * 0.12f, t + h * 0.18f)
            lineTo(cx, t + h * 0.36f)
            lineTo(cx + w * 0.12f, t + h * 0.18f)
            lineTo(cx + w * 0.20f, t + h * 0.38f)
            lineTo(cx + w * 0.35f, t + h * 0.22f)
            cubicTo(
                cx + w * 0.28f, t + h * 0.44f,
                cx + w * 0.22f, t + h * 0.50f,
                cx + w * 0.18f, t + h * 0.54f
            )
            lineTo(cx - w * 0.18f, t + h * 0.54f)
            cubicTo(
                cx - w * 0.22f, t + h * 0.50f,
                cx - w * 0.28f, t + h * 0.44f,
                cx - w * 0.35f, t + h * 0.22f
            )
            close()
        }
        scope.drawPath(crown, fill, style = Fill)
        scope.drawPath(crown, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // 5 Jewels on Crown
        val jewelRadius = w * 0.04f
        val jewelOffsets = listOf(
            Offset(cx - w * 0.35f, t + h * 0.20f),
            Offset(cx - w * 0.12f, t + h * 0.16f),
            Offset(cx, t + h * 0.12f),
            Offset(cx + w * 0.12f, t + h * 0.16f),
            Offset(cx + w * 0.35f, t + h * 0.20f)
        )
        for (pt in jewelOffsets) {
            scope.drawCircle(fill, jewelRadius, pt)
            scope.drawCircle(stroke, jewelRadius, pt, style = Stroke(width = strokeWidth * 0.7f))
        }

        // Body & Base
        val body = Path().apply {
            moveTo(cx - w * 0.18f, t + h * 0.54f)
            cubicTo(
                cx - w * 0.14f, t + h * 0.68f,
                cx - w * 0.28f, t + h * 0.78f,
                cx - w * 0.35f, t + h * 0.88f
            )
            lineTo(cx + w * 0.35f, t + h * 0.88f)
            cubicTo(
                cx + w * 0.28f, t + h * 0.78f,
                cx + w * 0.14f, t + h * 0.68f,
                cx + w * 0.18f, t + h * 0.54f
            )
            close()
        }
        scope.drawPath(body, fill, style = Fill)
        scope.drawPath(body, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        val baseRect = Rect(cx - w * 0.38f, t + h * 0.85f, cx + w * 0.38f, t + h * 0.94f)
        scope.drawRoundRect(fill, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        scope.drawRoundRect(stroke, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f), style = Stroke(width = strokeWidth))
    }

    private fun drawKing(
        scope: DrawScope,
        l: Float, t: Float, w: Float, h: Float,
        fill: Color, stroke: Color, shadow: Color
    ) {
        val cx = l + w / 2f
        val strokeWidth = w * 0.045f

        // Cross on top
        val cross = Path().apply {
            moveTo(cx, t + h * 0.08f)
            lineTo(cx, t + h * 0.22f)
            moveTo(cx - w * 0.07f, t + h * 0.14f)
            lineTo(cx + w * 0.07f, t + h * 0.14f)
        }
        scope.drawPath(cross, stroke, style = Stroke(width = strokeWidth * 1.2f, cap = StrokeCap.Square))

        // Crown arch
        val crown = Path().apply {
            moveTo(cx - w * 0.28f, t + h * 0.28f)
            cubicTo(
                cx - w * 0.15f, t + h * 0.20f,
                cx - w * 0.06f, t + h * 0.26f,
                cx, t + h * 0.22f
            )
            cubicTo(
                cx + w * 0.06f, t + h * 0.26f,
                cx + w * 0.15f, t + h * 0.20f,
                cx + w * 0.28f, t + h * 0.28f
            )
            lineTo(cx + w * 0.22f, t + h * 0.50f)
            lineTo(cx - w * 0.22f, t + h * 0.50f)
            close()
        }
        scope.drawPath(crown, fill, style = Fill)
        scope.drawPath(crown, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Body & Base
        val body = Path().apply {
            moveTo(cx - w * 0.22f, t + h * 0.50f)
            cubicTo(
                cx - w * 0.14f, t + h * 0.68f,
                cx - w * 0.28f, t + h * 0.78f,
                cx - w * 0.35f, t + h * 0.88f
            )
            lineTo(cx + w * 0.35f, t + h * 0.88f)
            cubicTo(
                cx + w * 0.28f, t + h * 0.78f,
                cx + w * 0.14f, t + h * 0.68f,
                cx + w * 0.22f, t + h * 0.50f
            )
            close()
        }
        scope.drawPath(body, fill, style = Fill)
        scope.drawPath(body, stroke, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        val baseRect = Rect(cx - w * 0.38f, t + h * 0.85f, cx + w * 0.38f, t + h * 0.94f)
        scope.drawRoundRect(fill, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        scope.drawRoundRect(stroke, Offset(baseRect.left, baseRect.top), Size(baseRect.width, baseRect.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f), style = Stroke(width = strokeWidth))
    }
}
