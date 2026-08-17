# Multi-Touch & Tap Interaction Task List

- [ ] **ScoreInteractionOverlay Refactor**
    - [ ] Update `pointerInput` to use `PointerEventPass.Initial`
    - [ ] Implement multi-pointer detection to bypass tap logic during zoom
    - [ ] Add touch slop and time-based tap verification
    - [ ] Consume single-finger tap events to prevent underlying layer triggers
- [ ] **ZoomablePdfPage Cleanup**
    - [ ] Remove redundant `detectTapGestures` for navbar toggling
- [ ] **Verification**
    - [ ] Verify pinch-to-zoom is no longer blocked
    - [ ] Verify page turn regions (Left/Right) still work reliably
    - [ ] Verify navbar toggle (Center) still works reliably
    - [ ] Ensure no accidental triggers during multi-touch gestures
