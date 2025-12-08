# Dark Mode Fixes & Refactoring Summary

## Issues Addressed
1.  **Reading Lesson Text Visibility**: Fixed invisible text in dark mode by applying correct theme colors to options and vocabulary items.
2.  **Missing Dark Mode in Other Lessons**: Implemented dark mode support for Listening, Quiz, and Speaking lessons.

## Changes Implemented

### 1. Global Theme Sync
-   **Created `PaperThemeProvider`**: A new provider that syncs the custom app theme with `react-native-paper`'s theme. This ensures that all Paper components (Card, Text, Surface, etc.) automatically adapt to the current theme (Light/Dark).
-   **Updated `app/_layout.tsx`**: Wrapped the app with `PaperThemeProvider`.

### 2. Reading Lesson Fixes (`ReadingLesson.tsx`)
-   Applied `lessonStyles.text` to `optionText`, `vocabWord`, and `vocabDefinition`.
-   Applied `lessonStyles.secondaryText` to `vocabExample`.
-   Ensured radio buttons use the correct theme colors.

### 3. Listening Lesson Refactor (`ListeningLesson.tsx`)
-   Integrated `useLessonStyles` hook.
-   Applied dynamic styles to:
    -   Container (ScrollView)
    -   Cards (Player, Transcript, Questions)
    -   Text (Questions, Options, Transcript, Vocabulary)
    -   Surfaces (Score, Vocabulary items)
-   Implemented dynamic styling for correct/incorrect/selected options.

### 4. Quiz Lesson Refactor (`QuizLesson.tsx`)
-   Integrated `useLessonStyles` hook.
-   Applied dynamic styles to:
    -   Container & Cards
    -   Question text & Options
    -   True/False buttons
    -   Results screen
    -   Start screen
-   Implemented dynamic styling for correct/incorrect states.

### 5. Speaking Lesson Refactor (`SpeakingLesson.tsx`)
-   Integrated `useLessonStyles` hook.
-   Applied dynamic styles to:
    -   Prompt cards
    -   Context & Role Play boxes
    -   Sample answers
    -   Playback controls
-   Ensured all text elements use theme colors.

## Verification
-   All lesson types now use the centralized `useLessonStyles` hook.
-   Hardcoded colors in `StyleSheet` are overridden by dynamic theme styles.
-   Text contrast in dark mode is ensured by `colors.text.primary` (Light Gray on Dark Background).
