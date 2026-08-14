# Tuner Calibration Fix Task List

- `[x]` **Calibration Fix**
    - `[x]` Update `TunerViewModel` to use `MWEngine.getRecommendedSampleRate`
    - `[x]` Refine `AudioRecord` processing loop for better stability
    - `[x]` Add debug logging for detected Hz and MIDI
    - `[x]` Verify needle centering with tone generator
- `[x]` **Noise Mitigation**
    - `[x]` Implement High-Pass Filter (80Hz)
    - `[x]` Implement RMS Volume Gate (-45dB)
    - `[x]` Add auto-reset logic for silence
