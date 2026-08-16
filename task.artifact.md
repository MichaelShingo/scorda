# Page Preview Slider Rework Task List

- [x] **UI Component Rework**
    - [x] Replace `LazyRow` with Material 3 `Slider` in `PagePreviewSlider.kt`
    - [x] Implement `MutableInteractionSource` to detect touch/drag states
    - [x] Create `PagePreviewTooltip` for floating thumbnail display
- [x] **Rendering Logic**
    - [x] Implement deferred page selection (only on drag release)
    - [x] Optimize preview bitmap rendering for speed
- [x] **Integration**
    - [x] Verify positioning of the tooltip relative to the slider thumb
    - [x] Ensure correct aspect ratio for preview bitmaps
- [ ] **Verification**
    - [ ] Test scrubbing through large scores
    - [ ] Verify zero-lag UI during slider interaction
