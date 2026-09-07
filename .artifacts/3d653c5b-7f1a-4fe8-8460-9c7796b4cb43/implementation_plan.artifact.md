# Color Preset System

Implement a persistent color preset system using `SettingsRepository` and `AnnotationViewModel`, featuring a scrollable row of modern color choices with the ability to add and delete presets.

## Proposed Changes

### Data Layer

#### [MODIFY] [SettingsRepository.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/SettingsRepository.kt)
- Add `_colorPresets` as a `stringPreferencesKey`.
- Define a list of default modern hex colors:
    - Charcoal: `#2C3E50`
    - Slate: `#7F8C8D`
    - Muted Red: `#E74C3C`
    - Soft Pink: `#EC407A`
    - Amber: `#F39C12`
    - Emerald: `#27AE60`
    - Sky Blue: `#3498DB`
- Add `colorPresets: Flow<List<Int>>` which parses the stored JSON/String or returns defaults.
- Add `saveColorPreset(color: Int)` and `deleteColorPreset(color: Int)` methods.

### ViewModel Layer

#### [MODIFY] [AnnotationViewModel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/viewmodel/AnnotationViewModel.kt)
- Expose `colorPresets` from the repository in the `uiState`.
- Add `addColorPreset(color: Int)` and `deleteColorPreset(color: Int)` wrapper methods.

### UI Layer

#### [MODIFY] [BrushSettingsPopup.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/drawing/BrushSettingsPopup.kt)
- Add a `ColorPresetRow` component:
    - Uses `LazyRow` for horizontal scrolling.
    - Displays each preset as a circular button (reusing/adapting `ColorButton`).
    - Clicking a preset updates the current tool color and the `ColorPickerController`.
    - Long-pressing a preset opens a `DropdownMenu` with a "Delete" option.
    - Add a "+" button at the end of the row that adds the current color from `controller.selectedColor.value` to the presets.
- Position the `ColorPresetRow` between the thickness controls and the main color picker.

## Verification Plan

### Automated Tests
- Build the project to ensure no regressions.

### Manual Verification
- Open the Brush Settings popup.
- Verify the initial set of modern color presets is visible.
- Select a color from the wheel and click the "+" button. Verify it appears in the preset row.
- Long-press a preset and select "Delete". Verify it is removed.
- Click a preset and verify the current tool color and color wheel update to match.
- Restart the app and verify presets are persisted.
