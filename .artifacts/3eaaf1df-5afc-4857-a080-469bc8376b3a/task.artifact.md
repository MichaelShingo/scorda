# Metronome Reconfiguration Stability Tasks

- [x] **Phase 1: Robust Reconfiguration**
    - [x] Update `AudioViewModel.kt` to halt the engine thread during `setupMetronomeEvents`.
    - [x] Move event cleanup to the beginning of the setup process.
- [ ] **Phase 2: Verification**
    - [ ] Stress test beat reduction on device to ensure no crashes occur.
