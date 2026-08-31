package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Square

object LegalMoveDots {

    fun drawHint(
        drawScope: DrawScope,
        square: Square,
        isCapture: Boolean,
        squareSize: Float,
        flipped: Boolean,
        hintColor: Color = Color(0x77333333)
    ) {
        val col = if (flipped) 7 - square.file else square.file
        val row = if (flipped) square.rank else 7 - square.rank
        val center = Offset(
            col * squareSize + squareSize / 2f,
            row * squareSize + squareSize / 2f
        )

        if (isCapture) {
            // Draw capture ring
            val radius = squareSize * 0.44f
            val strokeWidth = squareSize * 0.08f
            drawScope.drawCircle(
                color = hintColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
        } else {
            // Draw filled dot
            val radius = squareSize * 0.16f
            drawScope.drawCircle(
                color = hintColor,
                radius = radius,
                center = center
            )
        }
    }
}
