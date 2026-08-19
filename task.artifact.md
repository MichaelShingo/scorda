# Interaction Refinement & Revert Task List

- [x] **Simplified Interaction Overlay**
    - [x] Remove complex manual gesture tracking from `ScoreInteractionOverlay.kt`
    - [x] Use standard `waitForUpOrCancellation` for region taps
- [x] **Revert Unnecessary Fixes**
    - [x] Revert "queue-aware" navigation (`targetPage`) in `ScoreView.kt`
    - [x] Keep `currentPage` based navigation for stability
- [x] **Zero-Latency State Lookups**
    - [x] Keep `zoomableStates` map for instant scale detection
- [x] **Verification**
    - [x] Build and verify standard page turn reliability
    - [x] Verify `userScrollEnabled = false` prevents pager interference
