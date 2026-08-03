# Implementation Plan - Tuner Accuracy Fix

This plan addresses the "consistent margin" (offset) issue in the Tuner. The symptoms suggest a systematic error in frequency calculation or sample rate handling.

## User Review Required

> [!IMPORTANT]
> **Switch to Hz Output**: I am changing the JNI bridge to return raw frequency in Hz instead of MIDI. This allows the Kotlin layer to explicitly control the MIDI/Cents conversion logic using the user's `tuningHz` setting, eliminating any ambiguity about Aubio's internal MIDI reference.
> **Sample Rate Optimization**: I am switching the capture rate to 48,000 Hz. Most modern Android devices use 48kHz natively; using 44.1kHz often triggers a system-level resampler that can introduce pitch jitter or shifts.
> **Buffer Integrity**: I am adding a robust reading loop to ensure the pitch detector always receives a full window of fresh audio samples, preventing the "jump" artifacts that occur when processing partial buffers.

## Proposed Changes

### [Audio & JNI Layer]

#### [MODIFY] [aubio_jni.cpp](file:///D:/apps/scorda/app/src/main/cpp/aubio_jni.cpp)
- Set pitch unit to `"Hz"` in `nativeInit`.

### [ViewModel Layer]

#### [MODIFY] [TunerViewModel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/tuner/TunerViewModel.kt)
- Update `sampleRate` to 48000.
- Re-implement the `AudioRecord` read loop to ensure a full `hopSize` is collected before calling `process`.
- Update the pitch calculation logic:
    - Use `log2(detectedHz / tuningHz)` for high-precision semitone/cents detection.
    - Synchronize with the user-selected `tuningHz` reference.

## Verification Plan

### Manual Verification
- Test with A440 on a calibrated tone generator.
- Verify the needle is centered (0 cents) and the text display matches.
- Test with different `tuningHz` settings (e.g., 442Hz) and verify the tuner shifts accordingly.
- Verify no "lag" or sudden jumps in the needle position.
