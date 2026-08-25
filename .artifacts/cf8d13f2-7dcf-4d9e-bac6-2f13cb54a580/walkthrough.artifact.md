# Walkthrough - Landscape Panning via Offset Synchronization

I have implemented a dynamic offset correction that perfectly synchronizes the layout alignment with `telephoto`'s panning logic. This ensures that the PDF's top edge is fully reachable in landscape mode and eliminates excessive gray space at the bottom.

## Changes Made

### UI Components

#### [ZoomablePdfPage.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ZoomablePdfPage.kt)

- **Dynamic Centering Offset**:
    - I added a calculation for `centeringOffset`. When the content is taller than the viewport (common in landscape), Compose's default centering logic would push the top of the content above the screen.
    - This offset calculates the exact distance needed to "pull" the content back down so its top edge starts at `Y=0`.
- **Applied Offset and Alignment**:
    - The outer `Box` now uses `Alignment.TopCenter` in landscape.
    - The inner content `Box` uses the dynamic `.offset(y = centeringOffset)` to align its top boundary with the viewport's top boundary.
- **Restored Drawing**:
    - Re-enabled `DrawingCanvas` and ensured it's correctly layered within the panned container.
- **Cleanup**:
    - Removed hardcoded test offsets and red debug borders.

## Verification Results

### Manual Verification
- **Landscape View**:
    - The PDF starts exactly at the top of the screen.
    - Vertical panning is enabled for the entire height of the page.
    - Panning stops exactly at the top and bottom edges of the PDF paper.
- **Portrait View**:
    - The PDF remains centered and fits the screen correctly.
- **Drawing**:
    - Verified that ink remains aligned with the PDF content during pan and zoom.
