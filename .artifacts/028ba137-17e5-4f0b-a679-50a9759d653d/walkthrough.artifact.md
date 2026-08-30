# Walkthrough - Targeted Safe Area Handling

I have updated the `ScoreTabs` and `ScoreView` components to ensure interactive elements respect the safe area while allowing backgrounds to fill the margins. This was achieved using targeted `windowInsetsPadding` instead of a global `Scaffold`.

## Key Changes

### [ScoreTabs Component]

#### [ScoreTabs.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/scoreTabs/ScoreTabs.kt)
- Applied `windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))` to the inner `Row` **after** the background modifier.
- This allows the tab bar's background color to flow all the way to the screen edges, while the tabs and the "Add" button are padded away from side-positioned navigation buttons in landscape mode.

### [ScoreView Component]

#### [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Applied `windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))` to the `BoxWithConstraints` that wraps the PDF content and overlays.
- This ensures that the PDF pages, navigation hotzones, and the bottom page slider respect the safe drawing area on the sides, preventing them from being obscured by system navigation buttons in landscape.
- The root `Box` background remains full-screen, providing a clean white background behind the system bars.

## Verification

- **Backgrounds**: The background colors and white "paper" area now flow behind the system bars as intended.
- **Interactivity**: All interactive elements (tabs, close buttons, "+" button, PDF navigation, and the page slider) are now safely contained within the viewport and are fully accessible.
