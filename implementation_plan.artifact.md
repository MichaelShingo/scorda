# Score Navigation Architecture Overhaul (ScoreHost)

This plan moves the PDF navigation from `HorizontalPager` to a custom `ScoreHost` using `AnimatedContent`. This allows for faster, more customizable transitions and eliminates gesture competition with the system pager.

## Proposed Animation: "The Subtle Directional Slide"

Instead of a full-screen scroll, I suggest a **Subtle Slide-Fade**:
- **Next Page**: The new page fades in while sliding in from the right by only **15%** of the screen width. The current page fades out while sliding to the left.
- **Previous Page**: The new page fades in while sliding from the left (15%).
- **Why?**: It clearly indicates "which direction" you are moving in the score without the visually taxing movement of a full-width scroll. It feels "snappy" and professional, similar to high-end sheet music readers.

## User Review Required

> [!IMPORTANT]
> - **Removing HorizontalPager**: Built-in horizontal swiping will be gone. Navigation will be strictly region-taps (and eventually custom swiping).
> - **State Management**: I will shift the `currentPage` source of truth from `PagerState` to a simple `Int` state in `ScoreView`.

## Proposed Changes

### 1. The custom `ScoreHost` Component

#### [NEW] [ScoreHost.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreHost.kt)
- Uses `AnimatedContent` to host the `ZoomablePdfPage`.
- Defines a `ContentTransform` based on the direction of the page turn (determined by comparing the new index vs the old index).
- Handles the entry/exit transitions as described above.

### 2. Migration in `ScoreView`

#### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Remove `HorizontalPager` and `PagerState`.
- Add `var currentPageIndex by remember { mutableIntStateOf(...) }`.
- Replace Pager with `ScoreHost`.
- Update `scoreInteraction` handlers to simply increment/decrement `currentPageIndex`.

### 3. Rendering Integration

#### [MODIFY] [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)
- Ensure Telephoto's `ZoomableState` is reset or managed correctly when the page "switches" via `AnimatedContent`.

## Verification Plan

### Manual Verification
- **Rapid Tapping**: Spam the page turn regions. Verify it is significantly faster than the Pager.
- **Directional Check**: Confirm that "Next" slides from the right and "Previous" slides from the left.
- **Gesture Purity**: Confirm pinching and panning work perfectly without any "stuck" states from a parent scroller.
- **Annotation Check**: Ensure drawings stay pinned during the transition.
