# Chess Master Pro (Stockfish 18 Helper Mode)

Chess Master Pro is a native Android application built with Kotlin, Jetpack Compose, Material Design 3, and integrated with the official **Stockfish 18** engine for real-time calculation and Helper Mode gameplay.

## ✨ Key Features
- **Official Stockfish 18 Engine**: Bundled native ARM64, ARMv7, and x86_64 binaries communicating via the UCI protocol.
- **Helper Mode**: Stockfish auto-calculates and executes moves for its chosen color.
- **Dynamic Move Indicator Arrow**: Visual emerald green pulsing arrow indicating Stockfish's planned move on its turn.
- **Full Chess Rules**: Legal move generation, check/checkmate detection, castling, en passant, promotion dialog, and threefold repetition.
- **Board Themes & Customization**: Classic Wood, Modern Blue, Dark Charcoal themes with Staunton piece sets.
- **Controls**: Undo, Redo, 5 Difficulty presets (Beginner to Master), and Portrait/Landscape responsive layouts.

## 🚀 GitHub Actions Auto-Build CI/CD
Whenever you push code to any branch on GitHub, GitHub Actions will automatically:
1. Trigger the workflow defined in `.github/workflows/build.yml`.
2. Set up Java 17 and Gradle 9.3.1.
3. Download the official Stockfish 18 binaries.
4. Compile the project and build the debug APK.
5. Upload the compiled APK as a downloadable artifact (`ChessMasterPro-Stockfish18-APK`).

### How to download the APK from GitHub:
1. Push this repository to GitHub.
2. Go to the **Actions** tab in your GitHub repository.
3. Click on the latest workflow run (e.g. `Build Chess Master Pro APK`).
4. Scroll down to the **Artifacts** section at the bottom of the summary page.
5. Click on `ChessMasterPro-Stockfish18-APK` to download the zip containing the ready-to-install Android APK.
