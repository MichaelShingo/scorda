# Color Preset System & Dynamic Popup Height

Implement a persistent color preset system and enhance `AnchoredPopup` to dynamically calculate its maximum height based on available screen space.

## Proposed Changes

### UI Components (Shared)

#### [MODIFY] [CustomAnchoredPopup.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/navbar/CustomAnchoredPopup.kt)
- Introduce a `dynamicMaxHeight` state variable using `remember { mutableStateOf(500.dp) }`.
- Update `PopupPositionProvider.calculatePosition` to calculate the distance from the popup's top position (`y`) to the bottom of the window.
- Update `dynamicMaxHeight` inside `calculatePosition` using the calculated available space (minus some padding).
- Apply `dynamicMaxHeight` to the `Surface`'s `heightIn(max = ...)` modifier instead of the static `size.maxHeight`.

### Data Layer

#### [MODIFY] [SettingsRepository.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/SettingsRepository.kt)
- Add `_colorPresets` as a `stringPreferencesKey`.
- Define default modern hex colors (e.g., Charcoal, Slate, Muted Red, Soft Pink, Amber, Emerald, Sky Blue).
- Add `colorPresets: Flow<List<Int>>` for parsing stored presets.
- Add `saveColorPreset(color: Int)` and `deleteColorPreset(color: Int)`.

### ViewModel Layer

#### [MODIFY] [AnnotationViewModel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/viewmodel/AnnotationViewModel.kt)
- Expose `colorPresets` in `uiState`.
- Add methods `addColorPreset(color: Int)` and `deleteColorPreset(color: Int)`.

### UI Layer (Brush Settings)

#### [MODIFY] [BrushSettingsPopup.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/drawing/BrushSettingsPopup.kt)
- Implement `ColorPresetRow`:
    - `LazyRow` of circular color buttons.
    - Long-press to show a delete menu.
    - "+" button to save the current selection from the HSV wheel.
- Position the row between thickness controls and the HSV picker.

## Verification Plan

### Automated Tests
- Build successful: `gradlew :app:assembleDebug`.

### Manual Verification
- **Dynamic Height**: Rotate the device or open the popup and verify it uses the full height down to the screen edge without being cut off or having unnecessary scroll space if content is short.
- **Color Presets**:
    - Verify defaults are seeded.
    - Add a custom color from the wheel.
    - Long-press and delete a color.
    - Click a preset and ensure the wheel and tool update.
