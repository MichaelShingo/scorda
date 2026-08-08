# Metronome Sequencer Fix Tasks

- [x] **Phase 1: Audio Routing & Control**
    - [x] Update `AudioViewModel.kt` to use `ChannelGroup` for instrument routing.
    - [x] Add explicit `setIsSequenced(true)` to metronome events.
    - [x] Add `rewind()` to metronome start logic.
    - [x] Fix loop range and musical positioning in sequencer.
- [x] **Phase 2: Diagnostics & Cleanup**
    - [x] Implement full notification logging in `IObserver`.
    - [x] Remove main UI debug button and keep menu debug option.
- [ ] **Phase 3: Verification**
    - [ ] Verify sequencer progression and audio playback on device via Logcat and audio output.
