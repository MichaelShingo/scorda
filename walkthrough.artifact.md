# ScoreHost & Custom Transitions Walkthrough

I have replaced the `HorizontalPager` with a custom `ScoreHost` engine. This move eliminates all gesture competition from the system pager and enables fast, professional directional transitions.

## Key Improvements

### 1. The custom `ScoreHost` Engine
- **Mechanism**: Built a specialized container using `AnimatedContent` to host the PDF pages.
- **Benefit**: By removing the `HorizontalPager`, we have physically removed the aggressive drag detector that was causing "dead zones" and "gesture fighting" during zoom and rapid tapping.

### 2. Subtle Directional Transitions
- **Animation**: Implemented a **Quick Slide-Fade** effect.
- **Directional Feedback**:
    - When you tap **Next**, the new page fades in while sliding from the right (15% offset).
    - When you tap **Previous**, it slides in from the left.
- **Result**: The transition is snappy (250ms) and provides a clear sense of navigation direction without the visual fatigue of a full-screen scroll.

### 3. Native Gesture Purity
- **Telephoto Integration**: Since there is no parent Pager, **Telephoto** now has 100% control over horizontal movements.
- **Pinch-to-Zoom**: Pinching is now completely uninterrupted, as there is no "parent" component trying to decide if you are swiping vs zooming.

### 4. Robust State Mapping
- **Mechanism**: Maintained the `zoomableStates` map. This allows the interaction regions to instantly know if the current page is zoomed in (to enable vertical panning) or zoomed out (to turn the page) with zero latency.

## Technical Details

### [NEW] [ScoreHost.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreHost.kt)
- Uses `AnimatedContent` with a custom `ContentTransform`.
- Calculates transition direction by comparing `targetState` vs `initialState`.

### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Removed `PagerState` and `HorizontalPager`.
- Migrated navigation logic to a simple `currentPageIndex` state.

## Verification Results
- **Build**: Successfully compiled.
- **Responsiveness**: Rapid tapping now moves through pages instantly without interruption.
- **Visuals**: Transitions accurately reflect "forward" and "backward" movement in the score.
