# PdfRenderer Rework Task List

- [x] **Cleanup**
    - [x] Delete `IsolatedPagePdfDocument.kt`
    - [x] Delete `PdfPage.kt`
- [x] **Core Rendering**
    - [x] Implement `PdfRendererCore.kt` with Mutex synchronization
- [x] **UI Components**
    - [x] Implement `ZoomablePdfPage.kt` with pinch-to-zoom and pan
    - [x] Refactor `DrawingCanvas.kt` to use custom coordinate mapping
- [x] **ScoreView Integration**
    - [x] Update `ScoreView.kt` to use `HorizontalPager` + `ZoomablePdfPage`
    - [x] Implement bitmap pre-caching logic (via `beyondViewportPageCount`)
    - [x] Handle `PdfRenderer` lifecycle (close on score switch)
- [x] **Verification**
    - [x] Verify build
    - [x] Verify instant page turns
    - [x] Verify landscape "half-page" scrolling
    - [x] Verify annotation persistence and accuracy
