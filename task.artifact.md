# Unified Gesture & Zooming Task List

- [ ] **Unified Gesture Implementation**
    - [ ] Remove `transformable` and `detectDragGestures` from `ZoomablePdfPage.kt`
    - [ ] Implement a custom `pointerInput` block that handles both one-finger pan and two-finger transform simultaneously
    - [ ] Implement logic to allow horizontal swipe to pass to pager ONLY when `scale == 1.0` and starting a horizontal drag
- [ ] **State & Transformation Refinement**
    - [ ] Ensure `PageState` updates are fluid and handle the "smooth handoff" between 1 and 2 fingers
    - [ ] Verify `DrawingCanvas` still receives correct transformed coordinates
- [ ] **Verification**
    - [ ] Verify pinch-to-zoom is highly responsive even when already zoomed in
    - [ ] Verify one-finger pan is fluid
    - [ ] Verify horizontal paging works at 1.0x scale
