# Tuner Implementation Walkthrough

I have implemented a professional-grade Tuner using the `aubio` C++ library for high-accuracy pitch detection.

## Changes Summary

### 1. Audio Analysis & Auburn Integration
- **`aubio_jni.cpp`**: Implemented a JNI bridge to the `aubio` library, specifically using the pitch detection module with `yinfft` as the default method.
- **`AubioPitchDetector.kt`**: A Kotlin wrapper for the JNI methods to initialize, process, and cleanup the pitch detection engine.
- **`TunerResult.kt`**: A domain model that converts MIDI values from `aubio` into musical Pitch, Octave, and Cents offset.

### 2. ViewModel & Logic
- **`TunerViewModel.kt`**:
    - Manages the `AudioRecord` lifecycle on a background thread (`Dispatchers.IO`).
    - Handles microphone permission state.
    - Implements logic to adjust pitch detection based on a configurable reference frequency (A4 = 430-450Hz).
    - Filters results based on the algorithm's confidence level.

### 3. User Interface
- **`Tuner.kt`**:
    - Implements the main Tuner screen with Material 3 styling.
    - **Pitch Display**: Large note name and octave indicator.
    - **TunerMeter**: A custom Canvas-based speedometer UI. The needle rotates from $-90^\circ$ to $+90^\circ$ to indicate pitch offset from $-50$ to $+50$ cents.
    - **Reference Hz Selector**: Uses the existing `VerticalNumberSelector` for tuning settings.

### 4. System & Domain Maintenance
- **`AndroidManifest.xml`**: Added `RECORD_AUDIO` permission.
- **`DroneState.kt`**: Restored the domain model and fixed broken unit tests in the drone module that were causing build failures.

## Testing & Verification
- **Compilation**: Verified that the project compiles successfully with all new components and restored domain models.
- **Unit Tests**: Added `TunerResultTest.kt` to verify the MIDI-to-note/cents conversion logic.
- **UI Design**: The meter follows the requested design with a bottom-anchored indicator and accurate cent-to-angle mapping.

## Optimization & Debugging (Aug 3)

I've updated the Tuner to solve start-up delays and range issues:
- **Stability**: Replaced `PCM_FLOAT` with `PCM_16BIT` to fix the 15-20s start-up delay caused by hardware/driver incompatibility.
- **Accuracy**: Switched to the `yin` pitch detection algorithm and increased the window size to 4096 samples, enabling accurate detection of low frequencies (B0-C2).
- **Signal Quality**: Used the `VOICE_RECOGNITION` audio source to get raw, unfiltered audio data, avoiding system-level processing that can distort musical pitches.
- **Sensitivity**: Lowered the silence threshold to -70dB and tuned the detection tolerance.

## Accuracy & Calibration Fix (Aug 3)

I've fixed the centering issue (systematic flat/sharp offset) in the Tuner:
- **Sample Rate Alignment**: Switched to a **48,000 Hz** sample rate. Most modern Android hardware is 48kHz native; recording at 44.1kHz often triggers a system resampler that can introduce a slight but consistent pitch shift (often ~25-30 cents).
- **Raw Frequency Processing**: Modified the JNI bridge to return raw **Hz** instead of MIDI. This ensures the Kotlin layer has full control over the mathematical conversion to cents based on the user's specific `tuningHz` setting, eliminating library-internal reference errors.
- **Buffer Stabilization**: Re-implemented the `AudioRecord` read loop to ensure the detector always processes a perfectly aligned, full window of audio samples.

## Noise Mitigation & Signal Conditioning (Aug 6)

I've added signal processing to prevent the tuner from locking onto background hum and to improve responsiveness:
- **High-Pass Filter**: Implemented an 80Hz high-pass filter in the ViewModel. This removes electrical hum (50/60Hz) and low-frequency rumble before analysis, while keeping musical notes (E2 and up) intact.
- **RMS Volume Gate**: Added a -45dB energy gate. The tuner now ignores any audio buffers where the average volume is below this threshold, effectively filtering out quiet room noise.
- **Auto-Reset Logic**: Added a "release" mechanism. If the room is silent for more than 500ms, the UI automatically resets to its initial state, preventing it from "sticking" on the last detected note.
- **Improved Sensitivity**: Fine-tuned the silence threshold in both JNI and Kotlin layers to balance noise rejection with detection accuracy.
