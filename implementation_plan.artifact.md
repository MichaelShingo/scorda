# PdfRenderer-Based Zero-Latency Score View Rework

This plan outlines the replacement of the `androidx.pdf` library with a custom, high-performance PDF rendering engine using the native `android.graphics.pdf.PdfRenderer` and `HorizontalPager`.

## User Review Required

> [!IMPORTANT]
> - **Removal of `androidx.pdf`**: We will completely stop using the Jetpack PDF library for rendering. This means we lose its built-in text selection and link handling, but gain absolute control over rendering performance and discrete page turns.
> - **Custom Zoom Implementation**: We will implement a custom pinch-to-zoom and pan logic for the rendered bitmaps.
> - **Coordinate Mapping**: I will implement a bridge to ensure the existing `DrawingCanvas` (which works in PDF points) remains fully compatible with the new bitmap-based view.

## Proposed Changes

### 1. Rendering Engine

#### [NEW] [PdfRendererCore.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/PdfRendererCore.kt)
- Manages the `ParcelFileDescriptor` and `PdfRenderer` instance.
- Provides a thread-safe `renderPage` method using a `Mutex` to ensure the one-page-at-a-time restriction of `PdfRenderer`.
- Handles bitmap creation and rendering with the `PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY` flag.

### 2. UI Components

#### [NEW] [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)
- A component that displays the rendered bitmap.
- Implements pinch-to-zoom and pan gestures.
- Calculates the mapping from screen coordinates to PDF points.
- Integrates the `DrawingCanvas`.

#### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Replaces `PdfViewer` and `IsolatedPagePdfDocument` with a `HorizontalPager` using `ZoomablePdfPage`.
- Manages the lifecycle of `PdfRendererCore`.
- Implements pre-caching by pre-rendering adjacent pages into a bitmap cache.

#### [MODIFY] [DrawingCanvas.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/DrawingCanvas.kt)
- Update to accept a coordinate mapping interface instead of `PdfViewerState`.

### 3. Cleanup

#### [DELETE] [IsolatedPagePdfDocument.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/IsolatedPagePdfDocument.kt)
#### [DELETE] [PdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/PdfPage.kt)

## Verification Plan

### Manual Verification
- **Page Turn Latency**: Verify that page turns are instantaneous with zero "loading" flicker.
- **Landscape Scrolling**: Verify that the "half-page" logic still works by scrolling the zoomed bitmap before turning.
- **Annotation Accuracy**: Verify that strokes made on a zoomed bitmap are saved and rendered at the correct PDF coordinates.
- **Memory Usage**: Monitor for OOM issues when scrolling through large scores.
