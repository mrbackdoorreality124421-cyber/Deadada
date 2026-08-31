package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardTheme
import com.example.model.ChessMove
import com.example.model.Difficulty
import com.example.model.GameStatus
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.Square
import com.example.ui.ChessUiState
import com.example.ui.components.ChessBoard
import com.example.ui.components.ControlBar
import com.example.ui.components.PromotionDialog

@Composable
fun GameScreen(
    uiState: ChessUiState,
    onSquareTapped: (Square) -> Unit,
    onMoveAttempt: (Square, Square) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onDifficultyChanged: (Difficulty) -> Unit,
    onThemeChanged: (BoardTheme) -> Unit,
    onRestartGame: () -> Unit,
    onBackToSetup: () -> Unit,
    onSelectPromotion: (PieceType) -> Unit,
    onCancelPromotion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF121318)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(8.dp)
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // Landscape Layout
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Pane: Board
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1.1f),
                        contentAlignment = Alignment.Center
                    ) {
                        ChessBoard(
                            position = uiState.position,
                            boardTheme = uiState.boardTheme,
                            selectedSquare = uiState.selectedSquare,
                            legalMovesForSelected = uiState.legalMovesForSelected,
                            lastMove = uiState.lastMove,
                            stockfishArrowMove = uiState.stockfishArrowMove,
                            isStockfishTurn = uiState.isStockfishTurn,
                            flipped = uiState.isFlipped,
                            isInteractive = uiState.isUserTurn && !uiState.isStockfishThinking,
                            onSquareTapped = onSquareTapped,
                            onMoveAttempt = onMoveAttempt,
                            modifier = Modifier.fillMaxHeight(0.96f)
                        )
                    }

                    // Right Pane: Info, History & Controls
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.9f)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        TopGameHeader(
                            uiState = uiState,
                            onBack = onBackToSetup
                        )

                        StatusEvaluationCard(
                            uiState = uiState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        MoveHistoryPanel(
                            moveHistory = uiState.moveHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )

                        ControlBar(
                            canUndo = uiState.moveHistory.isNotEmpty() && !uiState.isStockfishThinking,
                            canRedo = uiState.redoStack.isNotEmpty() && !uiState.isStockfishThinking,
                            onUndo = onUndo,
                            onRedo = onRedo,
                            currentDifficulty = uiState.difficulty,
                            onDifficultyChanged = onDifficultyChanged,
                            currentTheme = uiState.boardTheme,
                            onThemeChanged = onThemeChanged,
                            onNewGame = onRestartGame
                        )
                    }
                }
            } else {
                // Portrait Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    TopGameHeader(
                        uiState = uiState,
                        onBack = onBackToSetup
                    )

                    StatusEvaluationCard(
                        uiState = uiState,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    ChessBoard(
                        position = uiState.position,
                        boardTheme = uiState.boardTheme,
                        selectedSquare = uiState.selectedSquare,
                        legalMovesForSelected = uiState.legalMovesForSelected,
                        lastMove = uiState.lastMove,
                        stockfishArrowMove = uiState.stockfishArrowMove,
                        isStockfishTurn = uiState.isStockfishTurn,
                        flipped = uiState.isFlipped,
                        isInteractive = uiState.isUserTurn && !uiState.isStockfishThinking,
                        onSquareTapped = onSquareTapped,
                        onMoveAttempt = onMoveAttempt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    MoveHistoryTape(
                        moveHistory = uiState.moveHistory,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    ControlBar(
                        canUndo = uiState.moveHistory.isNotEmpty() && !uiState.isStockfishThinking,
                        canRedo = uiState.redoStack.isNotEmpty() && !uiState.isStockfishThinking,
                        onUndo = onUndo,
                        onRedo = onRedo,
                        currentDifficulty = uiState.difficulty,
                        onDifficultyChanged = onDifficultyChanged,
                        currentTheme = uiState.boardTheme,
                        onThemeChanged = onThemeChanged,
                        onNewGame = onRestartGame
                    )
                }
            }
        }
    }

    // Promotion Dialog
    if (uiState.pendingPromotionMove != null) {
        PromotionDialog(
            color = uiState.position.activeColor,
            onSelectPiece = onSelectPromotion,
            onDismiss = onCancelPromotion
        )
    }

    // Game Over Dialog
    if (uiState.isGameOver) {
        val (title, msg) = when (uiState.gameStatus) {
            GameStatus.CHECKMATE -> {
                val winner = uiState.position.activeColor.opposite()
                val winnerName = if (winner == uiState.stockfishColor) "Stockfish (${winner.name})" else "You (${winner.name})"
                Pair("Checkmate!", "$winnerName won the game!")
            }
            GameStatus.STALEMATE -> Pair("Stalemate!", "Game drawn by stalemate.")
            GameStatus.DRAW_50_MOVES -> Pair("Draw!", "Drawn by 50-move rule.")
            GameStatus.DRAW_REPETITION -> Pair("Draw!", "Drawn by threefold repetition.")
            GameStatus.DRAW_INSUFFICIENT_MATERIAL -> Pair("Draw!", "Drawn by insufficient material.")
            else -> Pair("Game Over", "The game has concluded.")
        }

        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = onRestartGame,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.testTag("game_over_play_again")
                ) {
                    Text("Play Again")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onBackToSetup,
                    modifier = Modifier.testTag("game_over_change_settings")
                ) {
                    Text("Settings")
                }
            }
        )
    }
}

@Composable
private fun TopGameHeader(
    uiState: ChessUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_to_setup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Setup",
                    tint = Color(0xFFE8EAED)
                )
            }
            Text(
                text = "CHESS MASTER PRO",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFFF1F3F5)
            )
        }

        // Engine badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E212B))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = uiState.engineName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE8EAED)
            )
        }
    }
}

@Composable
private fun StatusEvaluationCard(
    uiState: ChessUiState,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1E26)
        ),
        modifier = modifier.border(1.dp, Color(0xFF2B2E3C), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isStockfishThinking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF00E676)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Stockfish calculating move...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00E676)
                    )
                } else {
                    val isCheck = uiState.gameStatus == GameStatus.CHECK
                    val statusText = when {
                        isCheck -> "Check! ${uiState.position.activeColor.name} king attacked"
                        uiState.isStockfishTurn -> "Stockfish's Turn (${uiState.stockfishColor.name})"
                        else -> "Your Turn (${uiState.position.activeColor.name})"
                    }
                    val statusColor = if (isCheck) Color(0xFFFF5252) else Color(0xFFE8EAED)

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (isCheck) Color(0xFFFF5252)
                                else if (uiState.isStockfishTurn) Color(0xFF00E676)
                                else Color(0xFF64B5F6),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            // Evaluation tag
            val eval = uiState.engineEvaluation
            if (eval.depth > 0 || eval.scoreCp != null || eval.mateIn != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262A38))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "D${eval.depth} ${eval.toDisplayText()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                }
            }
        }
    }
}

@Composable
private fun MoveHistoryTape(
    moveHistory: List<ChessMove>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF181A22)
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (moveHistory.isEmpty()) {
                Text(
                    text = "Game started • Moves will appear here",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            } else {
                for (i in moveHistory.indices step 2) {
                    val moveNumber = (i / 2) + 1
                    val whiteMove = moveHistory[i]
                    val blackMove = moveHistory.getOrNull(i + 1)

                    Text(
                        text = "$moveNumber.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = whiteMove.sanNotation.ifEmpty { whiteMove.toUci() },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE8EAED),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    if (blackMove != null) {
                        Text(
                            text = blackMove.sanNotation.ifEmpty { blackMove.toUci() },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB0BEC5),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveHistoryPanel(
    moveHistory: List<ChessMove>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181A22)),
        modifier = modifier.border(1.dp, Color(0xFF282B38), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = "MOVE HISTORY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (moveHistory.isEmpty()) {
                    Text(
                        text = "No moves played yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                } else {
                    for (i in moveHistory.indices step 2) {
                        val moveNumber = (i / 2) + 1
                        val whiteMove = moveHistory[i]
                        val blackMove = moveHistory.getOrNull(i + 1)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "$moveNumber.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9E9E9E),
                                modifier = Modifier.width(32.dp)
                            )
                            Text(
                                text = whiteMove.sanNotation.ifEmpty { whiteMove.toUci() },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE8EAED),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = blackMove?.sanNotation?.ifEmpty { blackMove.toUci() } ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFB0BEC5),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
