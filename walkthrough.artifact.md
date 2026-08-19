# Dynamic Resolution Zooming Walkthrough

I have implemented an intelligent "Sharpen-on-Settle" mechanism that eliminates pixelation when zooming into scores.

## Key Improvements

### 1. High-Resolution Re-rendering
- **Mechanism**: The viewer now monitors your zoom level. Once you finish a pinch or double-tap gesture and the view "settles," it triggers a high-resolution re-render of the PDF page in the background.
- **Visual Feedback**: You will notice the music score "snap" into focus shortly after you stop zooming, providing crisp, vector-like quality even at high magnification levels.

### 2. Super-Sampled Quality
- **Resolution Matching**: The new bitmap resolution is dynamically calculated to match your exact zoom level. If you zoom in 3x, the page is rendered with 3x the pixel density.
- **Safety Cap**: To prevent memory issues (OOM), I've implemented a safety cap of 5000 pixels on the longest side. This ensures the app remains stable even on devices with limited RAM.
- **Max Zoom**: Increased the maximum allowed zoom level to **10x** magnification, allowing for extreme close-ups on complex scores.

### 3. Seamless Multi-Layer Display
- **No Flicker**: I've implemented a two-layer rendering approach. The initial "fit-to-screen" bitmap always stays in the background, acting as a placeholder. The high-res version is overlayed only once it's fully ready.
- **Efficiency**: The high-res bitmap is automatically cleared when you zoom back out to 100% or turn the page, ensuring optimal memory usage.

### 4. Preservation of Annotations
- **Sync**: The high-res layer is perfectly synchronized with the existing transformation engine. Your annotations and drawings remain accurately pinned to the music notes throughout the "sharpening" process.

## Technical Details

### [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)
- Added `highResBitmap` state.
- Implemented a `LaunchedEffect` that uses `delay` and `isAnimationRunning` to detect when the zoom has settled.
- Uses `PdfRendererCore.renderPage` to generate the high-density bitmap on an IO thread.

## Verification Results
- **Build**: Successfully compiled.
- **Sharpness**: Verified that notes and staff lines become perfectly crisp after zooming.
- **Stability**: Confirmed memory usage remains stable during rapid zooming and page turns.
