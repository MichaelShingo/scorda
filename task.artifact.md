# Welcome Screen & Search Dialog Task List

- [x] **Component Extraction**
    - [x] Create `EmptyScoreView.kt` in its own file
    - [x] Use `Surface` with `MaterialTheme.colorScheme.background` for dark mode support
- [x] **Search Dialog Implementation**
    - [x] Add `isSearchDialogVisible` state to `EmptyScoreView`
    - [x] Implement a large `BasicAlertDialog` to host the `SearchScores` component
    - [x] Connect "Open a Score" button to trigger the dialog
- [x] **ScoreView Integration**
    - [x] Reference the new `EmptyScoreView` in `ScoreView.kt`
    - [x] Clean up redundant code and imports in `ScoreView.kt`
- [x] **Verification**
    - [x] Verify dark mode background switching
    - [x] Verify full-screen-style search dialog functionality
    - [x] Verify PDF import flow still works from the welcome screen
