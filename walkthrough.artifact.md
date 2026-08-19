# Directional Score Transition Walkthrough

I have enhanced the score-to-score transition animation to be direction-aware. Now, the animation provides intuitive feedback on whether you are moving forward or backward in your setlist or tabs.

## Key Improvements

### 1. Intuitive Navigation Feedback
- **Forward Navigation (Next Score)**: When moving to the next score in a setlist or a tab to the right, the new score **slides down** from the top.
- **Backward Navigation (Previous Score)**: When moving to the previous score or a tab to the left, the new score **slides up** from the bottom.
- **Benefit**: This directional consistency helps maintain spatial awareness within your music library and setlists, mirroring how physical pages might be handled or organized.

### 2. Intelligent Direction Tracking
- **Setlist Integration**: The transition engine now explicitly tracks the navigation intent from the side-region taps (First Page → Prev Score / Last Page → Next Score).
- **Tab Switching**: Manually clicking on tabs in the navbar now also triggers directional animations based on the relative position of the new tab compared to the old one.

### 3. Smooth & Snappy Transitions
- **Duration**: Kept at a responsive **400ms** with a subtle **10% offset** to ensure the animation is helpful but not distracting during a performance.
- **Fade Layering**: The sliding motion is paired with a fade-in to eliminate jarring transitions between differently formatted scores.

## Technical Details

### [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Added `scoreNavigationDirection` and `previousTabIndex` state tracking.
- Updated `AnimatedContent.transitionSpec` to switch between `slideInVertically` directions.
- Integrated direction updates into both `scoreInteraction` region taps and `LaunchedEffect` for tab changes.

## Verification Results
- **Build**: Successfully compiled.
- **Setlist Nav**: Confirmed "Next" slides down and "Prev" slides up.
- **Tab Nav**: Verified clicking a previous tab slides up correctly.
- **Loading Sync**: Confirmed that the animation only triggers once the new score content is actually ready to render.
