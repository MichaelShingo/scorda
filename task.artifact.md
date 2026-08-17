# Gestures and Zooming Fix Task List

- [ ] **PageState & Coordinate Mapping**
    - [ ] Update `PageState` to support 2D panning and accurate boundaries
    - [ ] Fix `PageTransform` to use unified 2D scale and offset
- [ ] **ZoomablePdfPage Refactor**
    - [ ] Wrap content in a zooming container so annotations scale with the PDF
    - [ ] Implement one-finger panning detector that consumes events when zoomed
- [ ] **Pager Integration**
    - [ ] Update `HorizontalPager.userScrollEnabled` based on current page scale
- [ ] **Verification**
    - [ ] Verify pinch-to-zoom works on all pages
    - [ ] Verify annotations stay aligned during zoom
    - [ ] Verify paging re-enables correctly at 1.0 scale
