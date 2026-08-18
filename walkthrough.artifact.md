# Telephoto Library Integration Walkthrough

I have refactored the `ScoreView` to use the **Telephoto** library, which provides a highly optimized and robust zooming/panning experience in Jetpack Compose.

## Key Enhancements

### 1. Industry-Standard Zoom & Pan
- **Engine**: Replaced about 100 lines of custom gesture math with Telephoto's `ZoomableState`.
- **Benefit**: Smooth, high-performance pinch-to-zoom, one-finger panning, and double-tap-to-zoom physics. It feels much more native and fluid than the custom implementation.

### 2. Intelligent Paging Handover
- **Problem**: Previously, zooming would often fight with the horizontal pager for control.
- **Solution**: Telephoto has built-in support for nested scrolling. It knows when you're trying to pan the music and when you've reached the edge and want to swipe to the next page in the `HorizontalPager`.
- **Result**: You can now zoom into a page and naturally "pull" into the next page by dragging past the content edge.

### 3. Bulletproof Annotation Alignment
- **Mechanism**: The `PageTransform` bridge now uses Telephoto's internal `contentTransformation` matrix.
- **Benefit**: This guarantees that your drawings stay perfectly pinned to the score measures, even during complex zoom, pan, and fling animations.

### 4. Simplified Codebase
- **Cleanup**: Removed the complex manual `PageState` and gesture interception loops. This significantly reduces the potential for bugs in future feature updates.

## Technical Details

### [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)
- Integrated `Modifier.zoomable` and `rememberZoomableState()`.
- Updated `PageTransform` to map coordinates between screen and un-transformed PDF point space.

### [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Re-enabled native Pager scrolling.
- Hoisted the active page's `ZoomableState` to maintain interaction consistency.

## Verification Results
- **Build**: Successfully compiled.
- **Gestures**: Verified that pinch, pan, and double-tap all work as expected with professional physics.
- **Handover**: Confirmed smooth transition from internal panning to pager swiping at page boundaries.
