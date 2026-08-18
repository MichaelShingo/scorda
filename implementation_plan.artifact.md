# Telephoto Library Integration Plan

This plan aims to simplify the gesture and zooming logic by integrating the `Telephoto` library, which is a highly-optimized solution for zoomable content in Jetpack Compose.

## User Review Required

> [!IMPORTANT]
> - **New External Dependency**: I will add `me.saket.telephoto:zoomable:0.19.0` to the project.
> - **Code Simplification**: This will allow us to remove about 100 lines of custom gesture handling code, significantly reducing the surface area for bugs.
> - **Standardized Interaction**: Telephoto provides industry-standard behaviors like flings, double-tap-to-zoom, and smooth transition between panning and paging.

## Proposed Changes

### 1. Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/apps/scorda/gradle/libs.versions.toml)
- Add `telephoto` version (`0.19.0`).
- Add `telephoto-zoomable` library definition.

#### [MODIFY] [app/build.gradle.kts](file:///D:/apps/scorda/app/build.gradle.kts)
- Add `implementation(libs.telephoto.zoomable)` dependency.

---

### 2. Zoomable Page Refactor

#### [MODIFY] [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)
- **State Management**: Replace the custom `PageState` class with `rememberZoomableState()`.
- **Gesture Handling**: Remove the complex `awaitEachGesture` block and the `transformable` modifier.
- **Layout**: Apply the `Modifier.zoomable(state)` to the parent container.
- **Coordinate Mapping**: Update the `PageTransform` implementation to use `zoomableState.contentTransformation` to map screen coordinates to PDF points.

---

### 3. ScoreView Integration

#### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Re-enable `userScrollEnabled = true` for the `HorizontalPager`.
- Telephoto handles the "gesture handover" automatically: it will pan the PDF when zoomed in and swipe to the next page when the user drags past the edge of a page.

## Verification Plan

### Manual Verification
- **Zooming**: Verify that pinch-to-zoom and double-tap-to-zoom work smoothly.
- **Paging Handover**: Verify that when zoomed in, you can pan to the edge of a page and then "pull" to swipe to the next page in the `HorizontalPager`.
- **Annotation Alignment**: Draw on the score, zoom/pan extensively, and verify the drawing stays exactly where it was placed.
- **Stability**: Rapidly switch tabs and zoom to ensure no `IllegalStateException` or crashes occur.
