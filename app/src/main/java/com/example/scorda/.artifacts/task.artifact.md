# Tasks: Implement Adaptive Width for PagePreviewSlider

- [x] Implement adaptive width calculation in `PagePreviewSlider`
    - [x] Import `LocalWindowSizeClass` and `WindowWidthSizeClass`
    - [x] Calculate `desiredWidth` and `maxSliderWidth` based on screen size
    - [x] Wrap UI in a centered, width-constrained `Box`
- [x] Implement conditional rendering in `PagePreviewSlider` for 1-page scores
    - [x] Add `pageCount` check
    - [x] Create non-interactive centered UI for `pageCount == 1`
    - [x] Maintain existing `Slider` for `pageCount > 1`
- [x] Verify visual consistency with Material 3 Slider thumb
- [x] Ensure tooltip is not shown for 1-page scores (as it's non-interactive)
