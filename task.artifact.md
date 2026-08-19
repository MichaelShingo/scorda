# Directional Score Transition Task List

- [x] **State Management**
    - [x] Add `scoreNavigationDirection` state to `ScoreView.kt`
    - [x] Add `previousTabIndex` tracking
- [x] **Transition Refinement**
    - [x] Update `transitionSpec` in `AnimatedContent` to use directional vertical slides
    - [x] Update `scoreInteraction` logic to set the correct direction
- [x] **Verification**
    - [x] Verify "Down" animation for Next Score
    - [x] Verify "Up" animation for Previous Score
    - [x] Verify tab switching animation direction
