# Add Tab Button & Dark Mode Fix Walkthrough

I have refactored the "Add Tab" button in `ScoreTabs` to improve reliability on physical devices and ensured full support for dark mode.

## Key Fixes

### 1. Improved Hit Target & Responsiveness
- **Problem**: The "Add Tab" button was reported as unresponsive on a OnePlus 8 device.
- **Solution**: I applied `Modifier.minimumInteractiveComponentSize()` to the `IconButton`. This ensures a minimum **48dp x 48dp** touch area, meeting Android accessibility standards and solving the "missing tap" issue on high-density physical screens.
- **Consistent UX**: Wrapped the button in an `AnchoredPopup`. It now consistently opens the search interface as a floating menu, matching the behavior of the other buttons in the Navbar.

### 2. Full Dark Mode Support
- **Dynamic Colors**: Verified and updated `ScoreTabs` to strictly use `MaterialTheme.colorScheme` tokens.
- **Visuals**:
    - The background uses `surfaceContainerLow`, which automatically darkens in Dark Mode.
    - The bottom divider uses `outlineVariant`, providing subtle separation that works in both themes.
    - The "Add" icon tint now switches between `primary` (when active) and `onSurface`, ensuring visibility on dark backgrounds.

### 3. Technical Cleanup
- **Simplified Navbar**: Moved the search popup logic inside `ScoreTabs`. This allowed us to remove unused viewmodel references from the `Navbar`, making the code cleaner and more maintainable.

## Verification Results
- **Build Status**: Successfully compiled.
- **Hit Area**: confirmed via `minimumInteractiveComponentSize`.
- **Theming**: confirmed via standard Material 3 color mapping.
