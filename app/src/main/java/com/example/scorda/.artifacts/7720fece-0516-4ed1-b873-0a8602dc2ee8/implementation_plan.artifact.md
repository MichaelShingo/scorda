# Implementation Plan - Score List Item Long-Press Preview

Add a preview of the first page of a score when long-pressing a `ScoreListItem` in the list, similar to the `PagePreviewSlider` tooltip.

## Proposed Changes

### [Component Name] Utilities

#### [MODIFY] [PdfRendererCore.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/PdfRendererCore.kt) -> [NEW] [PdfRendererCore.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/app/src/main/java/com/example/scorda/util/PdfRendererCore.kt)
- Move `PdfRendererCore` to the `util` package to make it more accessible across the project.
- Update package declaration.

### [Component Name] Molecules

#### [NEW] [PagePreviewTooltip.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/PagePreviewTooltip.kt)
- Extract `PagePreviewTooltip` from `PagePreviewSlider.kt` into its own file.
- Modify `pageIndex` to be `Int?`.
- If `pageIndex` is `null`, hide the "Page X" label.
- Keep the styling (shadow, border, etc.) consistent with the original implementation.

#### [MODIFY] [ScoreListItem.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/scoreListItem/ScoreListItem.kt)
- Add long-press detection using `interactionSource` and `LaunchedEffect`.
- On long-press (e.g., 500ms hold):
    - Instantiate `PdfRendererCore` for the score's file.
    - Show a `Popup` containing `PagePreviewTooltip` with `pageIndex = 0`.
- On release:
    - Dismiss the `Popup`.
    - Close the `PdfRendererCore`.
- Ensure this doesn't interfere with the existing click or drag actions.

### [Component Name] Organisms

#### [MODIFY] [PagePreviewSlider.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/PagePreviewSlider.kt)
- Remove the local `PagePreviewTooltip` implementation.
- Import and use the new shared `PagePreviewTooltip`.
- Update imports for `PdfRendererCore`.

## Verification Plan

### Automated Tests
- N/A (UI-heavy change, manual verification preferred)

### Manual Verification
- Long-press a score in the search list or setlist.
- Verify that a preview of the first page appears after a short delay.
- Verify that the preview disappears when the finger is lifted.
- Verify that the preview does NOT show a page number (as per requirements).
- Verify that normal clicks still open the score.
- Verify that dragging (swiping to remove) still works.
- Check `PagePreviewSlider` in the score view to ensure it still works as expected and shows page numbers.
