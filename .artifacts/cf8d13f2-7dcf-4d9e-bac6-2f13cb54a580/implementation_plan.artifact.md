# Implementation Plan - Fix Landscape Panning via Offset Synchronization

The goal is to fix the unreachable top edge of the PDF in landscape mode. The current `Box(contentAlignment = Alignment.Center)` centers the PDF, which pushes its top boundary into negative Y space relative to the viewport. Since `telephoto` is configured to align to the top, it treats `Y=0` as the limit, locking the "top" half of the page out of reach.

We will calculate the exact vertical offset caused by the centering and apply it as a correction, ensuring the PDF's top edge starts exactly at the viewport's top edge.

## Proposed Changes

### [Component] UI Organisms - scoreView

#### [MODIFY] [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)

- **Calculate Dynamic Offset**: Compute the vertical centering offset: `(contentHeight - viewportHeight) / 2`.
- **Apply Correction Offset**: Use `.offset(y = centeringOffset)` on the inner content `Box` to shift the page down so its top aligns with the viewport's top.
- **Synchronize Alignment**: Set the root `Box`'s `contentAlignment` and `zoomableState.contentAlignment` both to `Alignment.TopCenter` in landscape.
- **Restore DrawingCanvas**: Uncomment the `DrawingCanvas` and ensure it uses the updated `pageTransform`.
- **Cleanup**: Remove hardcoded trial offsets (like `250.dp`) and the redundant `modifier` on the inner `Box`.

## Verification Plan

### Manual Verification
- **Landscape View**:
    - Rotate to landscape. The PDF should fill the width and start exactly at the top edge.
    - Verify you can pan all the way down to the bottom.
    - Verify you can pan all the way back up to the top.
    - Verify there is no excessive gray overscroll at either end.
- **Drawing**:
    - Verify that drawing alignment remains accurate even with the new offset.
- **Portrait View**:
    - Verify the PDF remains centered and fits the screen.
