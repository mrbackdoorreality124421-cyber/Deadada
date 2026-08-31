package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.StockfishEngine
import com.example.model.BoardTheme
import com.example.model.ChessMove
import com.example.model.ChessPosition
import com.example.model.Difficulty
import com.example.model.EngineEvaluation
import com.example.model.GameStatus
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.Square
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChessUiState(
    val position: ChessPosition = ChessPosition.initial(),
    val stockfishColor: PieceColor = PieceColor.WHITE,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val boardTheme: BoardTheme = BoardTheme.DARK_CHARCOAL,
    val selectedSquare: Square? = null,
    val legalMovesForSelected: List<ChessMove> = emptyList(),
    val lastMove: ChessMove? = null,
    val stockfishArrowMove: Pair<Square, Square>? = null,
    val moveHistory: List<ChessMove> = emptyList(),
    val redoStack: List<ChessMove> = emptyList(),
    val positionHistory: List<String> = listOf(ChessPosition.initial().toFen()),
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS,
    val isStockfishThinking: Boolean = false,
    val engineName: String = "Stockfish 18",
    val isEngineReady: Boolean = false,
    val engineEvaluation: EngineEvaluation = EngineEvaluation(),
    val pendingPromotionMove: Pair<Square, Square>? = null,
    val hasStartedGame: Boolean = false
) {
    val isStockfishTurn: Boolean
        get() = position.activeColor == stockfishColor && !isGameOver

    val isUserTurn: Boolean
        get() = position.activeColor != stockfishColor && !isGameOver

    val isGameOver: Boolean
        get() = gameStatus in listOf(
            GameStatus.CHECKMATE,
            GameStatus.STALEMATE,
            GameStatus.DRAW_50_MOVES,
            GameStatus.DRAW_REPETITION,
            GameStatus.DRAW_INSUFFICIENT_MATERIAL
        )

    val isFlipped: Boolean
        get() = stockfishColor == PieceColor.BLACK
}

class ChessViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = StockfishEngine(application.applicationContext)

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    private var stockfishJob: Job? = null

    init {
        engine.initEngine {
            _uiState.value = _uiState.value.copy(
                isEngineReady = true,
                engineName = engine.engineName.value
            )
        }

        viewModelScope.launch {
            engine.engineName.collect { name ->
                _uiState.value = _uiState.value.copy(engineName = name)
            }
        }

        viewModelScope.launch {
            engine.currentEvaluation.collect { eval ->
                _uiState.value = _uiState.value.copy(engineEvaluation = eval)
                // If Stockfish is thinking, update the live planned arrow if available
                val bestUci = eval.bestMoveUci
                if (_uiState.value.isStockfishTurn && !bestUci.isNullOrEmpty() && bestUci.length >= 4) {
                    val from = Square.fromAlgebraic(bestUci.substring(0, 2))
                    val to = Square.fromAlgebraic(bestUci.substring(2, 4))
                    if (from != null && to != null) {
                        _uiState.value = _uiState.value.copy(stockfishArrowMove = Pair(from, to))
                    }
                }
            }
        }
    }

    fun selectStockfishColor(color: PieceColor) {
        _uiState.value = _uiState.value.copy(stockfishColor = color)
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _uiState.value = _uiState.value.copy(difficulty = difficulty)
    }

    fun selectBoardTheme(theme: BoardTheme) {
        _uiState.value = _uiState.value.copy(boardTheme = theme)
    }

    fun startGame() {
        engine.newGame()
        val initialPos = ChessPosition.initial()
        _uiState.value = _uiState.value.copy(
            position = initialPos,
            selectedSquare = null,
            legalMovesForSelected = emptyList(),
            lastMove = null,
            stockfishArrowMove = null,
            moveHistory = emptyList(),
            redoStack = emptyList(),
            positionHistory = listOf(initialPos.toFen()),
            gameStatus = GameStatus.IN_PROGRESS,
            isStockfishThinking = false,
            pendingPromotionMove = null,
            hasStartedGame = true
        )

        // If Stockfish plays White, it makes the first move automatically
        if (_uiState.value.stockfishColor == PieceColor.WHITE) {
            triggerStockfishMove()
        }
    }

    fun onSquareTapped(square: Square) {
        val state = _uiState.value
        if (!state.isUserTurn || state.isStockfishThinking || state.isGameOver) return

        val selected = state.selectedSquare
        val currentPiece = state.position.pieceAt(square)

        if (selected == null) {
            // Select user's piece
            if (currentPiece != null && currentPiece.color == state.position.activeColor) {
                val legalMoves = state.position.getLegalMoves().filter { it.from == square }
                _uiState.value = state.copy(
                    selectedSquare = square,
                    legalMovesForSelected = legalMoves
                )
            }
        } else {
            // Check if tapped square is a legal destination
            val move = state.legalMovesForSelected.find { it.to == square }
            if (move != null) {
                handleUserMove(selected, square, move)
            } else if (currentPiece != null && currentPiece.color == state.position.activeColor) {
                // Change selection to other friendly piece
                val legalMoves = state.position.getLegalMoves().filter { it.from == square }
                _uiState.value = state.copy(
                    selectedSquare = square,
                    legalMovesForSelected = legalMoves
                )
            } else {
                // Deselect
                _uiState.value = state.copy(
                    selectedSquare = null,
                    legalMovesForSelected = emptyList()
                )
            }
        }
    }

    fun onMoveAttempt(from: Square, to: Square) {
        val state = _uiState.value
        if (!state.isUserTurn || state.isStockfishThinking || state.isGameOver) return

        val legalMoves = state.position.getLegalMoves().filter { it.from == from }
        val move = legalMoves.find { it.to == to }
        if (move != null) {
            handleUserMove(from, to, move)
        } else {
            _uiState.value = state.copy(
                selectedSquare = null,
                legalMovesForSelected = emptyList()
            )
        }
    }

    private fun handleUserMove(from: Square, to: Square, candidateMove: ChessMove) {
        val state = _uiState.value
        val piece = candidateMove.piece

        // Check if pawn promotion
        val isPromotion = piece.type == PieceType.PAWN &&
                (to.rank == 7 && piece.color == PieceColor.WHITE || to.rank == 0 && piece.color == PieceColor.BLACK)

        if (isPromotion) {
            _uiState.value = state.copy(
                pendingPromotionMove = Pair(from, to),
                selectedSquare = null,
                legalMovesForSelected = emptyList()
            )
        } else {
            applyMove(candidateMove)
        }
    }

    fun completePromotion(promoType: PieceType) {
        val state = _uiState.value
        val pending = state.pendingPromotionMove ?: return
        val legalMoves = state.position.getLegalMoves().filter { it.from == pending.first && it.to == pending.second }
        val promoMove = legalMoves.find { it.promotion == promoType } ?: legalMoves.firstOrNull()

        _uiState.value = state.copy(pendingPromotionMove = null)
        if (promoMove != null) {
            applyMove(promoMove)
        }
    }

    fun cancelPromotion() {
        _uiState.value = _uiState.value.copy(
            pendingPromotionMove = null,
            selectedSquare = null,
            legalMovesForSelected = emptyList()
        )
    }

    private fun applyMove(move: ChessMove) {
        val state = _uiState.value
        val nextPos = state.position.makeMove(move)
        val newMoveHistory = state.moveHistory + move
        val newPosHistory = state.positionHistory + nextPos.toFen()
        val newStatus = nextPos.evaluateGameStatus(newPosHistory)

        _uiState.value = state.copy(
            position = nextPos,
            selectedSquare = null,
            legalMovesForSelected = emptyList(),
            lastMove = move,
            stockfishArrowMove = null, // Clear arrow on user's turn
            moveHistory = newMoveHistory,
            redoStack = emptyList(),
            positionHistory = newPosHistory,
            gameStatus = newStatus
        )

        // If next turn is Stockfish, calculate and play automatically
        if (nextPos.activeColor == state.stockfishColor && newStatus == GameStatus.IN_PROGRESS || newStatus == GameStatus.CHECK) {
            triggerStockfishMove()
        }
    }

    private fun triggerStockfishMove() {
        stockfishJob?.cancel()
        stockfishJob = viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isStockfishThinking = true)

            // Small natural delay so UI reflects thinking state smoothly
            delay(150)

            engine.calculateBestMove(
                position = _uiState.value.position,
                difficulty = _uiState.value.difficulty
            ) { bestUci ->
                viewModelScope.launch {
                    val currentState = _uiState.value
                    if (!currentState.isStockfishTurn) return@launch

                    val parsedMove = currentState.position.parseUciMove(bestUci)
                        ?: currentState.position.getLegalMoves().firstOrNull()

                    if (parsedMove != null) {
                        // Display arrow for the selected best move
                        _uiState.value = _uiState.value.copy(
                            stockfishArrowMove = Pair(parsedMove.from, parsedMove.to)
                        )

                        // Brief pause to let user see the calculated arrow before executing
                        delay(250)

                        val nextPos = currentState.position.makeMove(parsedMove)
                        val newMoveHistory = currentState.moveHistory + parsedMove
                        val newPosHistory = currentState.positionHistory + nextPos.toFen()
                        val newStatus = nextPos.evaluateGameStatus(newPosHistory)

                        _uiState.value = currentState.copy(
                            position = nextPos,
                            selectedSquare = null,
                            legalMovesForSelected = emptyList(),
                            lastMove = parsedMove,
                            stockfishArrowMove = Pair(parsedMove.from, parsedMove.to), // Arrow shown on Stockfish's move
                            moveHistory = newMoveHistory,
                            redoStack = emptyList(),
                            positionHistory = newPosHistory,
                            gameStatus = newStatus,
                            isStockfishThinking = false
                        )
                    } else {
                        _uiState.value = currentState.copy(isStockfishThinking = false)
                    }
                }
            }
        }
    }

    fun undo() {
        val state = _uiState.value
        if (state.isStockfishThinking || state.moveHistory.isEmpty()) return

        engine.stopCalculation()
        stockfishJob?.cancel()

        // In Helper Mode, undoing steps back so it's user's turn again (2 moves if both moved, or 1 move)
        val movesToPop = if (state.moveHistory.size >= 2 && state.isUserTurn) 2 else 1
        val remainingMoves = state.moveHistory.dropLast(movesToPop)
        val poppedMoves = state.moveHistory.takeLast(movesToPop)

        var replayPos = ChessPosition.initial()
        val newPosHistory = mutableListOf(replayPos.toFen())
        for (m in remainingMoves) {
            replayPos = replayPos.makeMove(m)
            newPosHistory.add(replayPos.toFen())
        }

        val newStatus = replayPos.evaluateGameStatus(newPosHistory)
        _uiState.value = state.copy(
            position = replayPos,
            selectedSquare = null,
            legalMovesForSelected = emptyList(),
            lastMove = remainingMoves.lastOrNull(),
            stockfishArrowMove = null,
            moveHistory = remainingMoves,
            redoStack = poppedMoves + state.redoStack,
            positionHistory = newPosHistory,
            gameStatus = newStatus,
            isStockfishThinking = false
        )

        // If after undo it is Stockfish's turn, trigger calculation
        if (replayPos.activeColor == state.stockfishColor && !state.isGameOver) {
            triggerStockfishMove()
        }
    }

    fun redo() {
        val state = _uiState.value
        if (state.isStockfishThinking || state.redoStack.isEmpty()) return

        val nextMove = state.redoStack.first()
        val remainingRedo = state.redoStack.drop(1)

        val nextPos = state.position.makeMove(nextMove)
        val newMoveHistory = state.moveHistory + nextMove
        val newPosHistory = state.positionHistory + nextPos.toFen()
        val newStatus = nextPos.evaluateGameStatus(newPosHistory)

        _uiState.value = state.copy(
            position = nextPos,
            selectedSquare = null,
            legalMovesForSelected = emptyList(),
            lastMove = nextMove,
            stockfishArrowMove = null,
            moveHistory = newMoveHistory,
            redoStack = remainingRedo,
            positionHistory = newPosHistory,
            gameStatus = newStatus
        )

        if (nextPos.activeColor == state.stockfishColor && (newStatus == GameStatus.IN_PROGRESS || newStatus == GameStatus.CHECK)) {
            triggerStockfishMove()
        }
    }

    fun resetGame() {
        engine.stopCalculation()
        stockfishJob?.cancel()
        _uiState.value = _uiState.value.copy(hasStartedGame = false)
    }

    override fun onCleared() {
        super.onCleared()
        engine.destroy()
    }
}
