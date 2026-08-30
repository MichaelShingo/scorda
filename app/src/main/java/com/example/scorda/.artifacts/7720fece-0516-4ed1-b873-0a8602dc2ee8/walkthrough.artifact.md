# Walkthrough - Score List Item Long-Press Preview

I have implemented a long-press preview feature for scores in the list. This allows users to quickly peek at the first page of a score without opening it.

## Changes Made

### Shared Components & Utilities
- **[PdfRendererCore.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/util/PdfRendererCore.kt)**: Moved to the `util` package to allow shared access from both the main score view and the list components. Updated all existing references in `ScoreView`, `ZoomablePdfPage`, and `PagePreviewSlider`.
- **[PagePreviewTooltip.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/PagePreviewTooltip.kt)**: Extracted from `PagePreviewSlider.kt` into a standalone molecule. Modified it to handle a `null` `pageIndex`, in which case it hides the page number label (perfect for quick peeks).

### Score List Item
- **[ScoreListItem.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/scoreListItem/ScoreListItem.kt)**:
    - Added long-press detection logic using `interactionSource` and `LaunchedEffect`.
    - When an item is held for more than 500ms, it initializes a temporary `PdfRendererCore` and shows a `Popup` with the page preview.
    - The preview is automatically dismissed and resources are cleaned up when the user releases the item.
    - Used `Alignment.TopCenter` with an offset to position the preview above the list item.

### Score View
- **[PagePreviewSlider.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/PagePreviewSlider.kt)**: Refactored to use the new shared `PagePreviewTooltip` molecule, reducing code duplication.

## Verification Results

### Manual Verification Path
1.  **List Preview**: Long-press any score in the search list. A preview of the first page should appear above the item after ~500ms. Lift your finger; the preview should disappear.
2.  **Slider Preview**: Open a score and use the page slider at the bottom. The tooltips should still appear as before, including the "Page X" label.
3.  **Interaction Consistency**: Ensure that a quick tap still opens the score and that the swipe-to-remove action (where applicable) still works correctly.

> [!NOTE]
> The preview popup is positioned using a fixed offset above the item. Depending on the screen position of the item (e.g., at the very top of the list), the popup might be partially obscured by the status bar or top app bar. Standard `Popup` behavior usually handles some level of window constraint, but further refinement of `PopupPositionProvider` could be done if perfect positioning is required.
