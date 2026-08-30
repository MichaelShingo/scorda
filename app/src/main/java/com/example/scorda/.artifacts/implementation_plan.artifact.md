# Adaptive Width Strategy for PagePreviewSlider

The `PagePreviewSlider` currently spans the entire screen width regardless of the number of pages. This plan introduces an adaptive width strategy that adjusts the slider's size based on both the number of pages and the device's window size class.

## Proposed Changes

### [Component] UI Organisms

#### [MODIFY] [PagePreviewSlider.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/PagePreviewSlider.kt)

- **Import `LocalWindowSizeClass`**: Access the current screen width category (Compact, Medium, Expanded).
- **Width Calculation Logic**:
    - Define a `baseStepWidth` (e.g., `56.dp`) representing the ideal spacing between page stops.
    - Calculate `desiredWidth = (pageCount - 1) * baseStepWidth`.
    - Define `maxSliderWidth` thresholds based on `WindowWidthSizeClass`:
        - **Compact** (Phone): `440.dp`
        - **Medium** (Tablet/Foldable): `660.dp`
        - **Expanded** (Large Tablet/Desktop): `900.dp`
    - Apply `widthIn(min = 200.dp, max = maxSliderWidth)` and `width(desiredWidth)` to a wrapper container around the Slider.
- **Structural Update**:
    - Wrap the `Slider` (and the `PagePreviewTooltip` logic) in a centered `Box` that uses this calculated width.
    - Ensure the `BoxWithConstraints` used by the tooltip fills this new constrained width, so the tooltip offset remains accurate relative to the slider's actual track.

## Verification Plan

### Manual Verification
- **Score with 2 pages**: Verify it shows a compact slider (200dp) centered at the bottom.
- **Score with 5-10 pages**:
    - On a **phone**: Verify it grows but caps out before hitting the screen edges too aggressively.
    - On a **tablet**: Verify it appears more spread out with clear stops.
- **Score with 50+ pages**: Verify it caps at the `maxSliderWidth` for the current screen size.
- **Single Page Score**: Verify the non-interactive indicator is still centered and respects the same width constraints (though it will effectively be small).

### Automated Tests
- Build the project to ensure `LocalWindowSizeClass` and related imports are correct.
