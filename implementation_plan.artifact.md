# Simple Welcome Screen & Search Dialog Implementation Plan

This plan aims to simplify the "no score open" experience by extracting the welcome screen into its own component and using a full-screen dialog for searching scores.

## User Review Required

> [!NOTE]
> - **Full Screen Search**: Tapping "Open a Score" on the welcome screen will now open the search interface in a dedicated dialog that covers most of the screen, rather than a small anchored popup.
> - **Theme Support**: The welcome screen will now correctly adapt to Dark Mode using the standard `MaterialTheme.colorScheme.background`.

## Proposed Changes

### 1. New Components

#### [NEW] [EmptyScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/EmptyScoreView.kt)
- **Root**: `Surface` with `color = MaterialTheme.colorScheme.background`.
- **Content**: The centered "Welcome to Scorda" text and action buttons.
- **Search Dialog**: A `BasicAlertDialog` (Experimental) that renders the `SearchScores` component when triggered.
- **Actions**:
    - **Import**: Triggers the system file picker.
    - **Open**: Toggles the local `isSearchDialogVisible` state.

---

### 2. ScoreView Cleanup

#### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Remove the inline `EmptyScoreView` and `WelcomeButton` composables.
- Reference the new `EmptyScoreView` component from the separate file.

## Verification Plan

### Manual Verification
- **Dark Mode**: Verify that the welcome screen background turns dark when system dark mode is toggled.
- **Search Flow**: Verify that "Open a Score" opens a large dialog and that selecting a score from the list correctly opens it in the viewer.
- **Import Flow**: Verify that "Import a Score" still triggers the PDF file picker.
- **Layout**: Ensure the welcome screen is perfectly centered and looks good on both phones and tablets.
