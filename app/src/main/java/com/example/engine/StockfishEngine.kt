package com.example.engine

import android.content.Context
import android.util.Log
import com.example.model.ChessMove
import com.example.model.ChessPosition
import com.example.model.Difficulty
import com.example.model.EngineEvaluation
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.Square
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class StockfishEngine(private val context: Context) {
    private val tag = "StockfishEngine"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var readerJob: Job? = null

    private val _engineName = MutableStateFlow("Stockfish 18")
    val engineName: StateFlow<String> = _engineName.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    private val _currentEvaluation = MutableStateFlow(EngineEvaluation())
    val currentEvaluation: StateFlow<EngineEvaluation> = _currentEvaluation.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var bestMoveCallback: ((String) -> Unit)? = null
    private var isRealStockfishActive = false

    fun initEngine(onReady: () -> Unit = {}) {
        scope.launch {
            try {
                val prepared = prepareBinary()
                if (prepared != null && prepared.exists() && prepared.canExecute()) {
                    startProcess(prepared)
                } else {
                    Log.w(tag, "Native binary could not be prepared. Using builtin fallback.")
                    _isReady.value = true
                    withContext(Dispatchers.Main) { onReady() }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error starting Stockfish: ${e.message}", e)
                _error.value = "Engine fallback mode active: ${e.localizedMessage}"
                _isReady.value = true
                withContext(Dispatchers.Main) { onReady() }
            }
        }
    }

    private fun prepareBinary(): File? {
        return try {
            val nativeLib = File(context.applicationInfo.nativeLibraryDir, "libstockfish.so")
            val engineDir = File(context.filesDir, "engines").apply { mkdirs() }
            val execFile = File(engineDir, "stockfish")

            if (nativeLib.exists()) {
                if (!execFile.exists() || execFile.length() != nativeLib.length()) {
                    nativeLib.copyTo(execFile, overwrite = true)
                    execFile.setExecutable(true, false)
                }
                execFile
            } else if (execFile.exists() && execFile.canExecute()) {
                execFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to copy/prepare Stockfish binary: ${e.message}")
            null
        }
    }

    private suspend fun startProcess(executable: File) = withContext(Dispatchers.IO) {
        try {
            val processBuilder = ProcessBuilder(executable.absolutePath)
            processBuilder.redirectErrorStream(true)
            val proc = processBuilder.start()
            process = proc

            writer = BufferedWriter(OutputStreamWriter(proc.outputStream))
            reader = BufferedReader(InputStreamReader(proc.inputStream))

            isRealStockfishActive = true

            startListening()

            // Initialize UCI
            sendCommand("uci")
            sendCommand("setoption name Skill Level value 20")
            sendCommand("setoption name Hash value 128")
            sendCommand("setoption name Threads value 4")
            sendCommand("isready")
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch ProcessBuilder: ${e.message}", e)
            isRealStockfishActive = false
            _isReady.value = true
        }
    }

    private fun startListening() {
        readerJob?.cancel()
        readerJob = scope.launch {
            val bufReader = reader ?: return@launch
            try {
                while (isActive) {
                    val line = bufReader.readLine() ?: break
                    handleEngineOutput(line)
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.e(tag, "Reader error: ${e.message}")
                }
            }
        }
    }

    private fun handleEngineOutput(line: String) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return

        when {
            trimmed.startsWith("id name") -> {
                val name = trimmed.removePrefix("id name").trim()
                _engineName.value = name
                Log.d(tag, "Detected engine: $name")
            }
            trimmed == "uciok" || trimmed == "readyok" -> {
                _isReady.value = true
                Log.d(tag, "Stockfish ready: $trimmed")
            }
            trimmed.startsWith("bestmove") -> {
                _isCalculating.value = false
                val parts = trimmed.split(" ")
                val moveUci = parts.getOrNull(1) ?: ""
                Log.d(tag, "Received bestmove: $moveUci")
                if (moveUci.isNotEmpty() && moveUci != "(none)") {
                    _currentEvaluation.value = _currentEvaluation.value.copy(bestMoveUci = moveUci)
                    bestMoveCallback?.invoke(moveUci)
                    bestMoveCallback = null
                }
            }
            trimmed.startsWith("info ") -> {
                parseInfoLine(trimmed)
            }
        }
    }

    private fun parseInfoLine(line: String) {
        try {
            var scoreCp: Int? = null
            var mateIn: Int? = null
            var depth = 0
            var pvMove: String? = null

            val tokens = line.split(" ")
            var i = 0
            while (i < tokens.size) {
                when (tokens[i]) {
                    "depth" -> {
                        depth = tokens.getOrNull(i + 1)?.toIntOrNull() ?: depth
                        i += 2
                    }
                    "cp" -> {
                        scoreCp = tokens.getOrNull(i + 1)?.toIntOrNull()
                        i += 2
                    }
                    "mate" -> {
                        mateIn = tokens.getOrNull(i + 1)?.toIntOrNull()
                        i += 2
                    }
                    "pv" -> {
                        pvMove = tokens.getOrNull(i + 1)
                        i = tokens.size // skip to end
                    }
                    else -> i++
                }
            }

            _currentEvaluation.value = EngineEvaluation(
                scoreCp = scoreCp,
                mateIn = mateIn,
                depth = depth,
                bestMoveUci = pvMove ?: _currentEvaluation.value.bestMoveUci,
                pv = line
            )
        } catch (e: Exception) {
            // Ignore parse hiccups on dynamic debug streams
        }
    }

    private fun sendCommand(cmd: String) {
        scope.launch {
            try {
                writer?.let {
                    it.write(cmd)
                    it.newLine()
                    it.flush()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to send command '$cmd': ${e.message}")
            }
        }
    }

    fun calculateBestMove(
        position: ChessPosition,
        difficulty: Difficulty,
        onBestMove: (String) -> Unit
    ) {
        _isCalculating.value = true
        bestMoveCallback = onBestMove

        if (isRealStockfishActive && process?.isAlive == true) {
            sendCommand("stop")
            sendCommand("setoption name Skill Level value ${difficulty.skillLevel}")
            sendCommand("position fen ${position.toFen()}")
            sendCommand("go movetime ${difficulty.moveTimeMs} depth ${difficulty.searchDepth}")
        } else {
            // Builtin Kotlin Chess Engine fallback
            scope.launch {
                val delayTime = Math.min(difficulty.moveTimeMs, 1000L)
                kotlinx.coroutines.delay(delayTime)
                val bestMove = computeFallbackMove(position, difficulty.searchDepth)
                _isCalculating.value = false
                val uci = bestMove?.toUci() ?: ""
                _currentEvaluation.value = _currentEvaluation.value.copy(bestMoveUci = uci)
                withContext(Dispatchers.Main) {
                    onBestMove(uci)
                }
            }
        }
    }

    fun stopCalculation() {
        if (isRealStockfishActive && process?.isAlive == true) {
            sendCommand("stop")
        }
        _isCalculating.value = false
        bestMoveCallback = null
    }

    fun newGame() {
        stopCalculation()
        if (isRealStockfishActive && process?.isAlive == true) {
            sendCommand("ucinewgame")
            sendCommand("isready")
        }
    }

    fun destroy() {
        try {
            stopCalculation()
            if (isRealStockfishActive) {
                sendCommand("quit")
            }
            readerJob?.cancel()
            process?.destroy()
            process = null
        } catch (e: Exception) {
            Log.e(tag, "Destroy error: ${e.message}")
        }
    }

    // Builtin evaluation fallback using Alpha-Beta Minimax search
    private fun computeFallbackMove(position: ChessPosition, maxDepth: Int): ChessMove? {
        val legalMoves = position.getLegalMoves()
        if (legalMoves.isEmpty()) return null

        val depth = Math.min(maxDepth, 4)
        var bestMove: ChessMove? = legalMoves.first()
        var bestScore = if (position.activeColor == PieceColor.WHITE) -100000 else 100000

        for (move in legalMoves) {
            val nextPos = position.makeMove(move)
            val score = alphaBeta(nextPos, depth - 1, -100000, 100000, position.activeColor == PieceColor.BLACK)
            if (position.activeColor == PieceColor.WHITE) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
            }
        }
        return bestMove
    }

    private fun alphaBeta(pos: ChessPosition, depth: Int, alphaParam: Int, betaParam: Int, isMaximizing: Boolean): Int {
        var alpha = alphaParam
        var beta = betaParam
        if (depth == 0) {
            return evaluateBoard(pos)
        }

        val legalMoves = pos.getLegalMoves()
        if (legalMoves.isEmpty()) {
            return if (pos.isInCheck(pos.activeColor)) {
                if (isMaximizing) -90000 - depth else 90000 + depth
            } else 0
        }

        if (isMaximizing) {
            var maxEval = -100000
            for (move in legalMoves) {
                val eval = alphaBeta(pos.makeMove(move), depth - 1, alpha, beta, false)
                maxEval = Math.max(maxEval, eval)
                alpha = Math.max(alpha, eval)
                if (beta <= alpha) break
            }
            return maxEval
        } else {
            var minEval = 100000
            for (move in legalMoves) {
                val eval = alphaBeta(pos.makeMove(move), depth - 1, alpha, beta, true)
                minEval = Math.min(minEval, eval)
                beta = Math.min(beta, eval)
                if (beta <= alpha) break
            }
            return minEval
        }
    }

    private fun evaluateBoard(pos: ChessPosition): Int {
        var score = 0
        for (r in 0..7) {
            for (f in 0..7) {
                val p = pos.pieceAt(f, r) ?: continue
                val base = p.type.value
                val posBonus = when (p.type) {
                    PieceType.PAWN -> if (p.color == PieceColor.WHITE) r * 10 else (7 - r) * 10
                    PieceType.KNIGHT, PieceType.BISHOP -> {
                        // center bonus
                        val centerDist = Math.abs(f - 3.5) + Math.abs(r - 3.5)
                        ((7.0 - centerDist) * 5).toInt()
                    }
                    else -> 0
                }
                val total = base + posBonus
                if (p.color == PieceColor.WHITE) score += total else score -= total
            }
        }
        return score
    }
}
