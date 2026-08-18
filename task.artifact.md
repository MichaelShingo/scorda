# Telephoto Integration Task List

- [x] **Dependency Management**
    - [x] Add `me.saket.telephoto:zoomable` to `libs.versions.toml`
    - [x] Add dependency to `app/build.gradle.kts`
- [x] **ZoomablePdfPage Refactor**
    - [x] Replace custom `PageState` with `rememberZoomableState()`
    - [x] Remove manual gesture detection and `graphicsLayer`
    - [x] Apply `Modifier.zoomable` to the page container
    - [x] Update `PageTransform` to use Telephoto's `contentTransformation`
- [x] **ScoreView Integration**
    - [x] Re-enable `HorizontalPager` user scrolling
    - [x] Ensure `ScoreInteractionOverlay` (taps) still works correctly with Telephoto
- [x] **Verification**
    - [x] Verify pinch-to-zoom and double-tap-to-zoom
    - [x] Verify smooth paging handover at zoomed edges
    - [x] Verify annotation alignment
