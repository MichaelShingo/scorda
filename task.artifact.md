# Dynamic Resolution Zooming Task List

- [x] **High-Res Rendering Logic**
    - [x] Add `highResBitmap` state to `ZoomablePdfPage.kt`
    - [x] Implement `LaunchedEffect` to detect zoom "settle" events (stable scale + no animation)
    - [x] Calculate target resolution based on current zoom level (with a safe cap)
    - [x] Request high-res render from `PdfRendererCore`
- [x] **UI Overlay & Transition**
    - [x] Overlay `highResBitmap` on top of the base bitmap when ready
    - [x] Clear `highResBitmap` when zooming back out or turning page
- [ ] **Verification**
    - [ ] Verify sharpness after zooming
    - [ ] Verify memory usage stability
    - [ ] Verify no "flashes" during the bitmap swap
