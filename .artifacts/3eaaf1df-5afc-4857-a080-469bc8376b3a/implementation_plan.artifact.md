# Metronome Sequencer Fix Plan

Fix the metronome sequencer progression and audio routing issues. While manual click tests work, the scheduled sequencer events are not producing sound or advancing the UI.

## User Review Required

> [!IMPORTANT]
> - **Audio Routing**: In `mwengine`, sequenced instruments must be part of a `ChannelGroup` added to the `MWEngine` instance to be included in the rendering loop. I will implement this routing.
> - **Sequencer Clocking**: I will add full notification logging to verify if the engine is actually firing beat updates.
> - **Event Properties**: I will explicitly set `setIsSequenced(true)` and `volume` on all `SampleEvent` instances to ensure they are visible to the sequencer and mixer.

## Proposed Changes

### Audio Engine Logic
#### [MODIFY] [AudioViewModel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/audio/AudioViewModel.kt)
- **Implement ChannelGroup**:
    - Add `private var channelGroup: ChannelGroup? = null`.
    - In `initialize`, create the group and add the drone and metronome instrument audio channels to it.
    - Register the group with `mwEngine?.addChannelGroup(channelGroup)`.
- **Enhanced Observer Logging**:
    - Add `Log.d` for all received `notificationId`s to track engine activity.
- **Sequencer Control**:
    - Call `sequencerController?.rewind()` when starting the metronome to ensure it begins at a valid position.
- **Event Refinement**:
    - Explicitly set `setIsSequenced(true)` and `volume = 1.0f` on each scheduled `SampleEvent`.

## Verification Plan

### Manual Verification
- Deploy to device.
- Start Metronome.
- **Monitor Logcat**: Check for `Notification: X` logs. If `SEQUENCER_POSITION_UPDATED` (usually ID 0) is firing, the sequencer is running.
- **Listen**: Check if audio plays in sync with UI indicators.
