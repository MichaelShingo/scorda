# Standard Adaptive Layout with WindowSizeClass & CompositionLocal

This plan refactors the adaptive layout logic to use the standard Material 3 `WindowSizeClass` system, provided globally via `CompositionLocal` to avoid parameter drilling.

## User Review Required

> [!IMPORTANT]
> - **Global Availability**: `WindowSizeClass` will be available to **all** composables via `LocalWindowSizeClass.current`. This follows modern Compose best practices.
> - **Dependency Update**: I will add `androidx.compose.material3:material3-window-size-class` to the project.

## Proposed Changes

### 1. Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/apps/scorda/gradle/libs.versions.toml)
- Add `androidx-compose-material3-windowSizeClass` library definition.

#### [MODIFY] [app/build.gradle.kts](file:///D:/apps/scorda/app/build.gradle.kts)
- Add `implementation(libs.androidx.compose.material3.windowSizeClass)` dependency.

---

### 2. Infrastructure

#### [NEW] [WindowSizeClassProvider.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/theme/WindowSizeClassProvider.kt)
- Define `LocalWindowSizeClass` using `staticCompositionLocalOf`.

---

### 3. MainActivity Integration

#### [MODIFY] [MainActivity.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/MainActivity.kt)
- Import `calculateWindowSizeClass`.
- Calculate `windowSizeClass` in `setContent`.
- Wrap the entire UI in `CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass)`.

---

### 4. UI Components Refactor

#### [MODIFY] [Navbar.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/navbar/Navbar.kt)
- Access `WindowSizeClass` via `LocalWindowSizeClass.current`.
- Replace `LocalWindowInfo` logic with size class checks:
    - `widthSizeClass == WindowWidthSizeClass.Compact` -> Show Info Icon.
    - Otherwise -> Show Title.

#### [MODIFY] [ScoreView.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/ScoreView.kt)
- Access `WindowSizeClass` via `LocalWindowSizeClass.current`.
- Use size classes to inform layout decisions if needed (retaining existing landscape logic where appropriate).

## Verification Plan

### Automated Tests
- Build verification to ensure the new dependency and `CompositionLocal` are correctly integrated.

### Manual Verification
- Verify that the Navbar title correctly switches to the info icon on small screens/windows.
- Verify that the layout remains responsive in split-screen mode on various devices.
- Verify that no regressions were introduced in the ScoreView's centering or navigation.
