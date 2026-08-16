# Fix Add Tab Button and Dark Mode Support

This plan addresses the unresponsiveness of the "Add Tab" button on physical devices and ensures proper dark mode support for the `ScoreTabs` component.

## User Review Required

> [!IMPORTANT]
> - **Functional Fix**: I am wrapping the "Add Tab" button in an `AnchoredPopup` consistent with the other Navbar buttons. This will not only fix the hit target issues but also actually show the Search UI when clicked, which was previously missing.
> - **Hit Target**: I will ensure the `IconButton` has a minimum 48dp hit area to comply with accessibility standards and improve reliability on physical devices like the OnePlus 8.

## Proposed Changes

### UI Components

#### [MODIFY] [ScoreTabs.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/scoreTabs/ScoreTabs.kt)
- Import `AnchoredPopup` and `CustomAnchoredPopupSize`.
- Import `SearchScores` to use as the popup content.
- Wrap the "Add" `IconButton` in an `AnchoredPopup`.
- Remove the `onAddTabClick` parameter from `ScoreTabs` as it will now handle its own popup (or pass it down if needed, but internalizing the popup is cleaner for the Navbar).
- Update the `IconButton` modifier to have consistent padding and a reliable hit area.
- Ensure `SecondaryScrollableTabRow` colors and the `HorizontalDivider` color properly utilize `MaterialTheme.colorScheme` for dark mode compatibility.

#### [MODIFY] [Navbar.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/navbar/Navbar.kt)
- Update the call to `ScoreTabs` to remove the `onAddTabClick` lambda if I decide to internalize it.

## Verification Plan

### Manual Verification
- **OnePlus 8 Test**: Verify that tapping the "+" button reliably opens the Search popup.
- **Dark Mode Toggle**: Verify that the `ScoreTabs` background, tab text, and close icons all correctly transition between light and dark themes.
- **Tab Switching**: Ensure that clicking existing tabs still works as expected.
- **Large Tab Counts**: Verify that the row remains scrollable and the "Add" button stays accessible.
