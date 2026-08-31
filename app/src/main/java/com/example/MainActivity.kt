package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ChessViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val chessViewModel: ChessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChessApp(viewModel = chessViewModel)
                }
            }
        }
    }
}

@Composable
fun ChessApp(viewModel: ChessViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Crossfade(targetState = uiState.hasStartedGame, label = "ScreenTransition") { hasStarted ->
        if (!hasStarted) {
            SplashScreen(
                engineName = uiState.engineName,
                isEngineReady = uiState.isEngineReady,
                selectedColor = uiState.stockfishColor,
                selectedDifficulty = uiState.difficulty,
                onColorSelected = viewModel::selectStockfishColor,
                onDifficultySelected = viewModel::selectDifficulty,
                onStartGame = viewModel::startGame
            )
        } else {
            GameScreen(
                uiState = uiState,
                onSquareTapped = viewModel::onSquareTapped,
                onMoveAttempt = viewModel::onMoveAttempt,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onDifficultyChanged = viewModel::selectDifficulty,
                onThemeChanged = viewModel::selectBoardTheme,
                onRestartGame = viewModel::startGame,
                onBackToSetup = viewModel::resetGame,
                onSelectPromotion = viewModel::completePromotion,
                onCancelPromotion = viewModel::cancelPromotion
            )
        }
    }
}
