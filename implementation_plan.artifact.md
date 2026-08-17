# Unified Gesture & Zooming Implementation Plan

This plan addresses the difficulty in triggering pinch-to-zoom when already zoomed in, by unifying all touch interactions into a single, cohesive gesture detector.

## User Review Required

> [!IMPORTANT]
> - **Unified Gesture Detector**: I will replace the separate `transformable` and `detectDragGestures` modifiers with a single custom `pointerInput` block. This eliminates "gesture fighting" and ensures that the system can smoothly transition between one-finger panning and two-finger zooming.
> - **Sensitivity Adjustments**: The new logic will be more "forgiving" when adding a second finger during an active pan, allowing for a more natural transition to zooming.

## Proposed Changes

### 1. Unified Gesture Engine

#### [MODIFY] [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)
- Remove `.transformable(...)` and `.pointerInput { detectDragGestures { ... } }`.
- Implement a single `.pointerInput` using a unified `detectTransformAndPan` logic:
    - **One Finger**: If `scale > 1.0`, it acts as a pan (moving the score around). If `scale == 1.0`, it allows events to pass through (so the Pager can swipe).
    - **Two Fingers**: Calculates the centroid, zoom change (pinch), and pan change simultaneously.
    - **Smooth Handoff**: If a user is panning with one finger and touches down a second finger, the system will switch to "zoom mode" without interrupting the movement.

### 2. State Mapping Stability

#### [MODIFY] [PageState.kt] (part of ZoomablePdfPage.kt)
- Ensure that `scrollBy` and `scale` updates are atomic to prevent visual jitter during the handoff between gesture types.

## Verification Plan

### Manual Verification
- **Zoom-to-Zoom Transition**: Zoom in 2x, then immediately pinch again to zoom to 4x. Verify it feels responsive and doesn't get "stuck" in a pan.
- **One-Finger Panning**: Verify that moving around a zoomed-in page is fluid.
- **Pager Integration**: Verify that swiping to the next page still works perfectly when at 1.0x zoom.
- **Annotation Alignment**: Verify that drawings stay perfectly pinned to the PDF notes throughout the zoom/pan gestures.
