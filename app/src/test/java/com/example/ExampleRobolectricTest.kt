package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ChessMove
import com.example.model.ChessPiece
import com.example.model.ChessPosition
import com.example.model.GameStatus
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Chess Master Pro", appName)
    }

    @Test
    fun `initial board has 20 legal moves for white`() {
        val initial = ChessPosition.initial()
        val legalMoves = initial.getLegalMoves()
        assertEquals(20, legalMoves.size)
    }

    @Test
    fun `fool checkmate detected correctly`() {
        var pos = ChessPosition.initial()
        // 1. f3 e5 2. g4 Qh4#
        val f3 = pos.getLegalMoves().find { it.from == Square.fromAlgebraic("f2") && it.to == Square.fromAlgebraic("f3") }
        assertNotNull(f3)
        pos = pos.makeMove(f3!!)

        val e5 = pos.getLegalMoves().find { it.from == Square.fromAlgebraic("e7") && it.to == Square.fromAlgebraic("e5") }
        assertNotNull(e5)
        pos = pos.makeMove(e5!!)

        val g4 = pos.getLegalMoves().find { it.from == Square.fromAlgebraic("g2") && it.to == Square.fromAlgebraic("g4") }
        assertNotNull(g4)
        pos = pos.makeMove(g4!!)

        val qh4 = pos.getLegalMoves().find { it.from == Square.fromAlgebraic("d8") && it.to == Square.fromAlgebraic("h4") }
        assertNotNull(qh4)
        pos = pos.makeMove(qh4!!)

        val status = pos.evaluateGameStatus(emptyList())
        assertEquals(GameStatus.CHECKMATE, status)
    }
}
