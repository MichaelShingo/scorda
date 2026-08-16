# Page Preview Slider Rework Walkthrough

I have overhauled the `PagePreviewSlider` to provide a more intuitive and performant "scrubbing" experience using a Material 3 Slider with a floating page preview.

## Key Changes

### 1. Material 3 Discrete Slider
- **UI**: Replaced the row of thumbnails with a standard `Slider` component.
- **Behavior**: The slider is configured with discrete steps, one for each page in the score. This makes it easy to snap exactly to a specific page.
- **Visuals**: The slider spans the full width of the viewport (with padding), providing a large touch target for easy navigation.

### 2. Floating "Live" Preview
- **Mechanism**: When you touch or drag the slider thumb, a "tooltip" box appears directly above it.
- **Content**: The tooltip shows a real-time preview of the page you are currently hovering over, along with a "Page X" label.
- **Positioning**: The tooltip moves horizontally along with the slider thumb. I've added logic to ensure the tooltip never goes off-screen, even when navigating the very first or very last pages.

### 3. Performance Optimization (Deferred Navigation)
- **Problem**: In the previous design, swiping through many thumbnails could be heavy as it triggered multiple renders.
- **Solution**: The main `ScoreView` now **only** switches pages when you release the slider. While you are dragging, only the small, low-resolution preview thumbnail is updated.
- **Benefit**: This allows for extremely smooth, "zero-lag" scrubbing through documents with hundreds of pages.

### 4. High-Speed Thumbnail Rendering
- **Optimization**: Previews are rendered at a lower resolution (120dp width) using the `PdfRendererCore`. This ensures that even on older devices, the preview can update as fast as the user moves their finger.

## Technical Details

### [PagePreviewSlider.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/PagePreviewSlider.kt)
- Implemented `MutableInteractionSource` to track `isPressed` and `isDragged`.
- Used `BoxWithConstraints` to dynamically calculate thumb position for tooltip placement.
- Integrated `produceState` for efficient, asynchronous bitmap loading during scrubbing.

## Verification Results
- **Build**: Successfully compiled.
- **Responsiveness**: Verified that the tooltip position tracks the thumb correctly.
- **Lifecycle**: Verified that the main pager only scrolls when `onValueChangeFinished` is triggered.
