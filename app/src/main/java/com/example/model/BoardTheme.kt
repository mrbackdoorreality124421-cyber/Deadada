package com.example.model

import androidx.compose.ui.graphics.Color

enum class BoardTheme(
    val displayName: String,
    val lightSquare: Color,
    val darkSquare: Color,
    val highlightColor: Color,
    val lastMoveColor: Color
) {
    CLASSIC_WOOD(
        displayName = "Classic Wood",
        lightSquare = Color(0xFFE8C97A),
        darkSquare = Color(0xFFB87D3A),
        highlightColor = Color(0x664CAF50),
        lastMoveColor = Color(0x55FBC02D)
    ),
    MODERN_BLUE(
        displayName = "Modern Blue",
        lightSquare = Color(0xFFDEE3E6),
        darkSquare = Color(0xFF8CA2AD),
        highlightColor = Color(0x6600BCD4),
        lastMoveColor = Color(0x5564B5F6)
    ),
    DARK_CHARCOAL(
        displayName = "Dark Charcoal",
        lightSquare = Color(0xFF769656),
        darkSquare = Color(0xFF4A6B3E),
        highlightColor = Color(0x66FFEB3B),
        lastMoveColor = Color(0x55CDDC39)
    );

    companion object {
        val ALL = listOf(CLASSIC_WOOD, MODERN_BLUE, DARK_CHARCOAL)
    }
}
