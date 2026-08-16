# Material 3 Page Preview Slider Rework

This plan details the replacement of the thumbnail-row `PagePreviewSlider` with a Material 3 `Slider` component featuring a floating "Page Preview" tooltip.

## User Review Required

> [!IMPORTANT]
> - **Navigation Change**: The main view will now only switch pages when the user **releases** the slider. While dragging, a floating preview will show the target page. This prevents heavy rendering of the main view during quick scrubbing.
> - **Discrete Steps**: The slider will snap to integer page numbers.
> - **Preview Position**: The preview thumbnail will appear above the slider thumb, moving horizontally as the user drags.

## Proposed Changes

### 1. UI Components

#### [MODIFY] [PagePreviewSlider.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/PagePreviewSlider.kt)
- Replace `LazyRow` with a `Slider` (Material 3).
- Use `MutableInteractionSource` to detect when the slider is being touched (`isPressed` or `isDragged`).
- Implement a floating `PagePreviewTooltip` component that renders a small bitmap of the page.
- The tooltip will be positioned relative to the slider's value using `BoxWithConstraints`.
- Add a label showing "Page X of Y" near the preview.

### 2. Rendering Optimizations

- Reuse `PdfRendererCore` to generate the preview bitmaps.
- Since previews are small, they will be rendered at a low resolution to ensure the "scrubbing" feels smooth and responsive.

## Verification Plan

### Manual Verification
- Verify that the slider spans the full width of the screen.
- Verify that a thumbnail appears the moment the user touches the slider.
- Verify that the thumbnail updates in real-time as the slider is dragged.
- Verify that the main `ScoreView` page only changes once the user releases the slider.
- Verify that the aspect ratio of the preview thumbnail is correct.
