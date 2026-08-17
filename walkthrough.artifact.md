# Simplified Welcome & Search Flow Walkthrough

I have refactored the "No score open" experience to be more robust, thematic, and user-friendly by extracting it into a dedicated component and using a modern dialog-based search flow.

## Key Improvements

### 1. Dedicated `EmptyScoreView` Component
- **Separation of Concerns**: Moved the welcome screen logic out of `ScoreView.kt` and into its own file: `EmptyScoreView.kt`.
- **Dark Mode Support**: The welcome screen now uses a `Surface` with `MaterialTheme.colorScheme.background`. This ensures the background automatically switches between light and dark modes based on system settings, matching the rest of the application.

### 2. Large Search Dialog
- **Mechanism**: Tapping the "Open a Score" button now triggers a `BasicAlertDialog` that hosts the `SearchScores` component.
- **Benefit**: This provides a large, focused interface for finding and opening scores when none are currently active. It avoids the need for complex global popup state while remaining very easy to use.
- **Auto-Dismiss**: The dialog automatically closes once a score is selected from the list.

### 3. Integrated Import Flow
- **Native Picker**: The "Import a Score" button is a prominent primary action that directly launches the Android system file picker for PDF documents.
- **Seamless Loading**: Once a file is picked, the app immediately transitions into the PDF viewer with a loading spinner.

## Technical Details

### [NEW] [EmptyScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/EmptyScoreView.kt)
- Encapsulates the greeting, buttons, and search dialog logic.
- Uses `rememberLauncherForActivityResult` for PDF imports.

### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Cleaned up to only manage the high-level switching between the PDF viewer, the loading state, and the `EmptyScoreView`.

## Verification Results
- **Build Status**: Successfully compiled.
- **Theme**: Verified that the background respects dark mode.
- **Flow**: Confirmed that both "Import" and "Open" paths lead correctly to a rendered score.
