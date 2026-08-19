# Interaction Refinement & Revert Walkthrough

I have reverted the "queue-aware" navigation logic and simplified the interaction overlay, focusing on the discovered root cause of the tapping issue.

## Key Changes

### 1. Simplified Interaction Overlay
- **Mechanism**: Removed the complex manual multi-touch tracking loop from `ScoreInteractionOverlay.kt`.
- **New Approach**: Uses Compose's standard `awaitFirstDown` and `waitForUpOrCancellation` logic.
- **Benefit**: This provides a much "cleaner" tap detection cycle with zero artificial overhead, while still peeking at the `Initial` pass to allow Telephoto zoom gestures to take precedence.

### 2. Standardized Page Navigation
- **Revert**: Reverted the use of `pagerState.targetPage` back to `pagerState.currentPage` for page turns.
- **Reasoning**: Since the true cause of the "slow tap" was identified as `HorizontalPager`'s `userScrollEnabled` state interfering with touches, the complex queue-aware logic is no longer necessary for stability.
- **Current Behavior**: Tapping turns the page based on the currently active page index.

### 3. Pager Isolation
- **Fix**: Kept `userScrollEnabled = false` for the `HorizontalPager`.
- **Impact**: This ensures that all touch events are handled by our custom overlay and the Telephoto zoom engine, preventing the pager from "stealing" taps during rapid interactions or ongoing animations.

### 4. Retained Performance Optimizations
- **Hoisting**: Kept the `zoomableStates` map in `ScoreView.kt`.
- **Benefit**: This allows the UI to instantly know the scale and offset of any page without waiting for a re-composition cycle, ensuring the interaction regions always know when to pan vertically vs. turn pages.

## Verification Results
- **Build**: Successfully compiled.
- **Tapping**: Verified that standard region-based taps are responsive and reliable.
- **Zooming**: Confirmed that Telephoto continues to handle pinch-to-zoom and panning without interference.
