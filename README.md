# Article Trainer (German)

## Purpose
Article Trainer is an Android app to practice German nouns and their definite articles. It loads noun data from CSV files (per CEFR level), presents randomized quizzes, tracks correct/incorrect answers, and supports retrying failed words.

## Key Features
- Select practice level (A1, A2, ...)
- Load nouns from assets CSV files (e.g. `german_nouns_a1.csv`, `german_nouns_a2.csv`)
- Randomized quiz selection with configurable word count
- Progress tracking and results screen with failed-words practice
- Hint toggle and answer feedback
- Preserves quiz state across configuration changes

## High-level architecture
- Platform: Android (Kotlin + Java), Gradle build
- Architectural pattern: MVVM
    - UI layer (Activities / Fragments / Composables)
        - Observes `StateFlow<QuizUiState>` exposed by the ViewModel
        - Presents start screen, quiz UI, and results
    - ViewModel layer
        - `QuizViewModel` (see `app/src/main/java/com/trainig/articletrainer/viewmodel/QuizViewModel.kt`)
        - Manages app state, loads CSVs, starts quizzes, handles user actions (submit, next, hint, practice)
        - Uses `viewModelScope` coroutines and `MutableStateFlow` / `StateFlow` for UI updates
    - Data layer
        - `CsvParser` responsible for parsing CSV files from `assets`
        - `NounEntry` model representing a noun row (article, word forms, translations, etc.)
        - CSV assets located in `app/src/main/assets/` (example names: `german_nouns_a1.csv`, `german_nouns_a2.csv`)
- Concurrency & state
    - Loading/parsing happens on coroutine dispatcher via `viewModelScope.launch`
    - State changes emitted as `QuizUiState` (Loading, Start, Quiz, Result)
    - `QuizUiState.Quiz` exposes `progress` (`Word X of Y`) and `totalWords`

## Important files
- `app/src/main/java/com/trainig/articletrainer/viewmodel/QuizViewModel.kt` — main quiz logic and state management
- `app/src/main/java/com/trainig/articletrainer/data/CsvParser.kt` — CSV parsing utility
- `app/src/main/java/com/trainig/articletrainer/data/NounEntry.kt` — data model for noun entries
- `app/src/main/assets/german_nouns_a1.csv` — example word list for A1
- `app/src/main/assets/german_nouns_a2.csv` — example word list for A2

## Data flow (simplified)
1. User selects level or starts quiz.
2. ViewModel loads CSV via `CsvParser.parseFromAssets(...)`.
3. Parsed `List<NounEntry>` stored in `allNouns`.
4. ViewModel selects `wordCount` nouns (shuffled) and emits `QuizUiState.Quiz`.
5. UI observes state and renders questions, progress, and results.
6. On completion, `QuizUiState.Result` is emitted; user can practice failed words.

## Build & run
- Open project in Android Studio (Android Studio Meerkat | 2024.3.1 Patch 2).
- Run on a device or emulator (Windows host supported).
- Ensure CSV assets exist and follow the expected format used by `CsvParser`.

## Troubleshooting & notes
- If level change does not update max counts in UI, ensure the UI observes `QuizUiState.Start` emitted after CSV load. `QuizViewModel` sets `Start(maxWords = allNouns.size, currentLevel = currentLevel)` after loading.
- CSV format must be consistent across levels; mismatched columns can reduce parsed rows.
- Logs: `QuizViewModel` writes debug logs indicating which CSV was loaded and how many nouns were parsed.

## License
- Add project license as needed.
