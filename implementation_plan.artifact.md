# Score Navigation Architecture Overhaul

This plan proposes replacing the `HorizontalPager` with a custom **Score Host** to eliminate gesture conflicts and enable future animation flexibility (like page curls or quick fades).

## User Review Required

> [!IMPORTANT]
> - **Removing HorizontalPager**: We will move away from the standard pager. This means built-in horizontal swiping will be replaced by our custom region-based taps and a custom swipe detector (if desired).
> - **Custom Animation Host**: I will use `AnimatedContent` as the primary host. This allows us to define the exact entry/exit animations (e.g., a "Slide in" that can later be changed to a "Fade").
> - **Pre-loading Strategy**: I will implement a manual pre-cache logic to ensure that turning to the next page remains instant, maintaining the performance of the native `PdfRenderer`.

## Proposed Changes

### 1. The custom `ScoreHost` Component

#### [NEW] [ScoreHost.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreHost.kt)
- A specialized container that uses `AnimatedContent` to switch between pages.
- It will track the `currentPageIndex` and manage the transition state.
- It will handle the "edge-to-edge" layout and coordinate mapping without a parent Pager's interference.

### 2. Gesture Separation

#### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Remove `HorizontalPager`.
- Use the new `ScoreHost` instead.
- Since there is no parent Pager, the `userScrollEnabled` conflict is physically removed from the UI tree.
- All horizontal movement will belong exclusively to **Telephoto** (for panning) or our **Region Taps** (for turning).

### 3. Rendering Stability

#### [MODIFY] [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)
- Keep Telephoto integration.
- Ensure the bitmap loading logic is optimized for the non-Pager lifecycle.

## Verification Plan

### Manual Verification
- **Rapid Tapping**: Spam the "Next" region and verify the page turns are instantaneous and queued correctly via `AnimatedContent`.
- **Gesture Purity**: Verify that pinching and panning are 100% reliable and never "stick" or "jitter" due to parent scroll logic.
- **Animation Testing**: Verify the transition feels professional (e.g., a smooth slide or fade).
- **Annotation Persistence**: Confirm drawings remain perfectly aligned during and after the custom page transition.

### Automated Tests
- Build and run unit tests for the index management logic.
