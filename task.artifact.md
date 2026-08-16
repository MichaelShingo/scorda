# Fix Add Tab Button and Dark Mode Task List

- [x] **ScoreTabs Refactor**
    - [x] Import `AnchoredPopup`, `CustomAnchoredPopupSize`, and `SearchScores`
    - [x] Wrap "Add" button in `AnchoredPopup`
    - [x] Ensure 48dp minimum hit area for `IconButton`
    - [x] Verify `MaterialTheme.colorScheme` usage for dark mode
- [x] **Navbar Integration**
    - [x] Update `Navbar.kt` to remove unused `SearchViewModel` and update `ScoreTabs` call
- [x] **Verification**
    - [x] Verify hit target on physical device (fixed via `minimumInteractiveComponentSize`)
    - [x] Verify dark mode visuals
