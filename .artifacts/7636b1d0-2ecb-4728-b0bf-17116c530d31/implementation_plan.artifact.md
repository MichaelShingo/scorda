# Implementation Plan - Hybrid Eraser & Rendering Fidelity Fix

Fix the eraser behavior to support both `WHOLE_STROKE` and `PARTIAL` modes, and resolve the blurriness issue while zooming.

## User Review Required

> [!IMPORTANT]
> The "Whole Stroke" eraser will be destructive (deletes strokes from the database), while the "Partial" eraser remains non-destructive (masks strokes using eraser paths). This hybrid approach preserves the legacy behavior for whole-stroke erasing while providing the new masking capability for precision work.

## Proposed Changes

### UI / Rendering Layer

#### [MODIFY] [DrawingCanvas.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/DrawingCanvas.kt)
- **Eraser Hybrid Logic**:
    - Re-introduce the `eraseAt` function for geometric intersection testing.
    - Update `pointerInput`:
        - If `isEraserMode` and `eraserMode == WHOLE_STROKE`, call `eraseAt` during the drag. Do **not** save an eraser stroke to the database.
        - If `isEraserMode` and `eraserMode == PARTIAL`, capture the drag as a `currentInputBatch` and save it as a `Stroke` with `isEraser = true` upon completion.
- **Rendering Fidelity**:
    - **Identify the Blur Cause**: The blurriness was likely caused by "Double Transformation" (applying scale to both the Canvas and the Renderer).
    - **The Fix**:
        - Keep `withMatrix(transformMatrix)` around the drawing operations to ensure the `Offscreen` layer coordinate system matches the screen.
        - Pass an **Identity Matrix** to `canvasStrokeRenderer.draw(...)`. This prevents the "double-scaling" while ensuring the renderer draws into the already-scaled canvas space.
        - I will also verify if the renderer requires the scale for LOD. If so, I'll pass a matrix that *only* contains the scale but has zero translation (since the translation is handled by `withMatrix`).

### Data Layer

#### [MODIFY] [InkConverters.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/InkConverters.kt)
- Adjust `epsilon` to `0.02f` (a middle ground) to ensure sharpness without excessive vertex count, but only if the user still reports issues after the transform fix.

## Verification Plan

### Manual Verification
- **Whole Stroke Eraser**: Select "Whole Stroke" mode. Verify that tapping or dragging over a stroke deletes it entirely.
- **Partial Eraser**: Select "Partial" mode. Verify that it masks strokes and feels pressure-sensitive.
- **Zoom Fidelity**: Zoom in to 400%. Verify that edges are sharp and the stroke thickness appears correct (not double-scaled).
