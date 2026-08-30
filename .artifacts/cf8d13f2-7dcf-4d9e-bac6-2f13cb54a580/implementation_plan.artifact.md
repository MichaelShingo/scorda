# Implementation Plan - Restore Landscape Fix and Resolve Visibility

The user identified the `centeringOffset` as the critical fix for landscape panning. I will restore this logic while resolving the visibility issue caused by "double padding."

The visibility issue occurred because the external `modifier` (containing navbar padding) was applied to both the root `BoxWithConstraints` and the inner `zoomable` `Box`. This caused the content to be pushed twice as far as intended, often hiding it.

## Proposed Changes

### [Component] UI Organisms - scoreView

#### [MODIFY] [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)

- **Restore `centeringOffset`**: Recalculate the vertical shift: `(contentHeight - viewportHeight) / 2`.
- **Apply Correct Offset**: Restore the `.offset(y = ...)` on the inner PDF `Box`.
- **Remove Double Padding**: Ensure the external `modifier` is ONLY applied to the root `BoxWithConstraints`.
- **Alignment Consistency**: Use `Alignment.Center` for the layout, with the offset providing the specific top-alignment needed for `telephoto` in landscape.
- **Maintain Overlays**: Ensure the high-res bitmap and `DrawingCanvas` remain correctly layered within the offset container.

## Verification Plan

### Manual Verification
- **Landscape Panning**:
    - Rotate to landscape. Verify the PDF starts at the top.
    - Verify you can pan all the way down and back up.
- **Navbar Visibility**:
    - Toggle the navbar and slider. Verify the PDF remains visible in the padded viewport and isn't "pushed" out of the screen.
- **Drawing Alignment**:
    - Verify that ink remains aligned with the PDF content.
- **High-Res Display**:
    - Verify that high-res bitmaps still load and overlay correctly when zoomed.
