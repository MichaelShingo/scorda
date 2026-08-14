# Metronome Reconfiguration Stability Plan

Address the persistent crash occurring when decreasing beats per measure by ensuring the audio rendering thread is completely halted during engine reconfiguration.

## User Review Required

> [!IMPORTANT]
> - **Full Engine Halt**: Instead of just pausing the sequencer, I will stop the entire `MWEngine` rendering thread (`mwEngine?.stop()`) while updating the measure structure and events. This eliminates any race conditions where the engine might attempt to render a buffer using inconsistent state.
> - **Clear-First Strategy**: I will explicitly remove all old events from the sequencer **before** changing the number of steps per measure.

## Proposed Changes

### Audio Engine Integration
#### [MODIFY] [AudioViewModel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/audio/AudioViewModel.kt)
- **Stop Engine Thread**: At the start of `setupMetronomeEvents()`, call `mwEngine?.stop()`.
- **Atomic Cleanup**:
    - Move the event clearing logic (`it.removeFromSequencer()`, `it.delete()`) to the very top, immediately after stopping the engine.
    - This ensures no "orphaned" events exist that exceed the new measure bounds.
- **Sequential Configuration**:
    - Call `rewind()`.
    - Update measure structure and tempo.
    - Set the new loop range.
    - Create and add new events.
- **Restart Engine**: Call `mwEngine?.start()` at the end of the method.
- **Playing State**: Restore the sequencer playing state (`setPlaying(true)`) only if the metronome was active.

## Verification Plan

### Manual Verification
- Deploy to device.
- Start metronome playback at a high beat count (e.g., 8).
- **Stress Test**: Rapidly decrease the beats to 1.
- **Verify**:
    - No app crash occurs.
    - Sound and UI restart cleanly from Beat 1 of the new measure.
