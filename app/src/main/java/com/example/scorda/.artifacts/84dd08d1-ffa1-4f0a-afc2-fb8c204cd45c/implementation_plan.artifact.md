# Implementation Plan - Fix Drone Crash (SIGSEGV in MWEngine)

The crash is caused by a `SIGSEGV` in `MWEngine::ADSR::getAttackTime()` when `playTone()` is called. This indicates that the `SynthEvent` is attempting to access a null or garbage-collected `ADSR` object associated with the `SynthInstrument`.

## User Review Required

> [!IMPORTANT]
> The `SynthInstrument` was being created as a local variable in `AudioViewModel.initialize()`, making it eligible for garbage collection. When the C++ object is destroyed, the `SynthEvent` is left with a dangling pointer.
>
> Additionally, `AudioViewModel.playTone()` was being called directly in the `Drone` composable's body, causing it to execute on every recomposition.

## Proposed Changes

### [Audio Module]

#### [MODIFY] [AudioViewModel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/audio/AudioViewModel.kt)
- Keep a strong reference to `SynthInstrument` to prevent it from being garbage collected.
- Explicitly initialize the `ADSR` envelope for the instrument to ensure it is not null in native code.
- Properly clean up the instrument and ADSR in `onDestroy`.

### [Drone UI]

#### [MODIFY] [Drone.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/drone/Drone.kt)
- Remove the direct call to `audioViewModel.playTone()` from the composable body.
- Note: The current `DroneViewModel` still uses `AudioTrackEngine` via the `AppContainer`. We should eventually unify this, but the immediate goal is to stop the crash.

## Verification Plan

### Manual Verification
- Launch the app and open the Drone tool.
- Verify that it no longer crashes immediately.
- Test toggling the tone (if buttons are wired up to `AudioViewModel`).
