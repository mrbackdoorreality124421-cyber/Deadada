package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Square
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object MoveArrow {

    fun drawArrow(
        drawScope: DrawScope,
        from: Square,
        to: Square,
        squareSize: Float,
        flipped: Boolean, // true if Black is at bottom
        color: Color = Color(0xCC00E676),
        alphaMultiplier: Float = 1.0f
    ) {
        val fromCenter = getSquareCenter(from, squareSize, flipped)
        val toCenter = getSquareCenter(to, squareSize, flipped)

        val dx = toCenter.x - fromCenter.x
        val dy = toCenter.y - fromCenter.y
        val angle = atan2(dy, dx)

        val headLength = squareSize * 0.38f
        val headWidth = squareSize * 0.32f
        val shaftWidth = squareSize * 0.16f

        // Shorten the end so it points nicely at the center of the destination square
        val arrowTip = Offset(
            toCenter.x - cos(angle) * (squareSize * 0.12f),
            toCenter.y - sin(angle) * (squareSize * 0.12f)
        )

        val shaftStart = fromCenter
        val shaftEnd = Offset(
            arrowTip.x - cos(angle) * headLength,
            arrowTip.y - sin(angle) * headLength
        )

        val effectiveColor = color.copy(alpha = color.alpha * alphaMultiplier)

        // Draw shaft line
        drawScope.drawLine(
            color = effectiveColor,
            start = shaftStart,
            end = shaftEnd,
            strokeWidth = shaftWidth,
            cap = StrokeCap.Round
        )

        // Draw arrowhead triangle
        val leftWing = Offset(
            shaftEnd.x + cos(angle + Math.PI / 2).toFloat() * (headWidth / 2f),
            shaftEnd.y + sin(angle + Math.PI / 2).toFloat() * (headWidth / 2f)
        )
        val rightWing = Offset(
            shaftEnd.x + cos(angle - Math.PI / 2).toFloat() * (headWidth / 2f),
            shaftEnd.y + sin(angle - Math.PI / 2).toFloat() * (headWidth / 2f)
        )

        val headPath = Path().apply {
            moveTo(arrowTip.x, arrowTip.y)
            lineTo(leftWing.x, leftWing.y)
            lineTo(rightWing.x, rightWing.y)
            close()
        }

        drawScope.drawPath(headPath, color = effectiveColor, style = Fill)
        drawScope.drawPath(headPath, color = Color(0x88FFFFFF), style = Stroke(width = squareSize * 0.02f, join = StrokeJoin.Round))
    }

    private fun getSquareCenter(square: Square, squareSize: Float, flipped: Boolean): Offset {
        val col = if (flipped) 7 - square.file else square.file
        val row = if (flipped) square.rank else 7 - square.rank
        return Offset(
            col * squareSize + squareSize / 2f,
            row * squareSize + squareSize / 2f
        )
    }
}
