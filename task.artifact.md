# ScoreHost Migration Task List

- [x] **Core Architecture**
    - [x] Create `ScoreHost.kt` with `AnimatedContent` logic
    - [x] Implement directional slide-fade transition
- [x] **ScoreView Migration**
    - [x] Remove `PagerState` and `HorizontalPager`
    - [x] Implement `currentPageIndex` state tracking
    - [x] Integrate `ScoreHost` as the primary content viewer
- [x] **Gesture & State Refinement**
    - [x] Ensure `ScoreInteractionOverlay` updates the new index state
    - [x] Fix navbar visibility logic to work with the new index
- [x] **Verification**
    - [x] Verify transition direction (Next vs Prev)
    - [x] Verify rapid tapping performance
    - [x] Verify Telephoto stability in the new host
