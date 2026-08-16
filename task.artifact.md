# WindowSizeClass Integration Task List

- [x] **Build Configuration**
    - [x] Add `material3-window-size-class` to `libs.versions.toml`
    - [x] Add dependency to `app/build.gradle.kts`
- [x] **Infrastructure Setup**
    - [x] Create `WindowSizeClassProvider.kt` with `LocalWindowSizeClass`
- [x] **Global Integration**
    - [x] Update `MainActivity.kt` to calculate and provide `WindowSizeClass`
- [x] **Component Refactor**
    - [x] Refactor `Navbar.kt` to use `LocalWindowSizeClass.current`
    - [x] Refactor `ScoreView.kt` to use `LocalWindowSizeClass.current`
- [x] **Verification**
    - [x] Verify build and correct responsive behavior on multiple window sizes
