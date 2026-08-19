# Score-to-Score Vertical Transition Plan

This plan adds a subtle vertical "drop-in" animation when switching between different scores in a setlist.

## User Review Required

> [!NOTE]
> **Animation Logic**: The transition will be triggered whenever the `selectedScore` ID changes.
> - **Entry**: The new score slides down by 10% of its height and fades in.
> - **Exit**: The previous score (or loading state) fades out.
> - **Loading Sync**: We will ensure the animation only considers the score "ready" when the `PdfRendererCore` has successfully opened the new file, preventing jarring transitions to empty states.

## Proposed Changes

### 1. Score-Level Transition

#### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Replace the high-level `if/else` block (Score vs Loading vs Empty) with `AnimatedContent`.
- Key the transition on a combined state: `selectedScore?.score?.id`.
- Define a `transitionSpec` that uses `slideInVertically` and `fadeIn` for the "ready" score state.

### 2. State Scoping

#### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Ensure `currentPageIndex` is correctly preserved and reset only when a *different* score is loaded.

## Verification Plan

### Manual Verification
- **Setlist Switch**: Navigate "Next Score" in a setlist. Verify the new score slides down smoothly.
- **Initial Open**: Open a score from the library. Verify the drop-in animation.
- **Back to Library**: Close the score. Verify the empty state fades in gracefully.
- **Rapid Navigation**: Quickly skip through 3-4 scores. Verify animations queue correctly.
