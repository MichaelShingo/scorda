# Metronome UI Refinement Plan

Refine the metronome UI to improve layout, usability, and aesthetics as requested by the user.

## Proposed Changes

### 1. Fix "More" Menu Position
Move the `MetronomeMenu` from the top row to a fixed position at the top right of the `MetronomeContent`.

#### [MODIFY] [Metronome.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/metronome/Metronome.kt)
- Wrap the main `Column` in `MetronomeContent` with a `Box`.
- Place `MetronomeMenu` as a direct child of the `Box` with `Modifier.align(Alignment.TopEnd)`.
- Remove `MetronomeMenu` from the existing `Row`.

### 2. Relocate Play Button and BPM Arrows
Move the play button to the absolute center of the wheel and move the BPM adjustment arrows outside the wheel.

#### [MODIFY] [MetronomeWheel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/metronome/MetronomeWheel.kt)
- **Center Controls**: Remove the `Row` containing the arrows and play button.
- **Play Button**: Place the play button `Surface` directly in the center of the wheel `Box`.
- **Arrows**: Wrap the wheel's main `Box` in a `Row` and place `IconButton`s for BPM adjustment on the left and right sides of the wheel.

### 3. Curved Beat Indicators
Modify the beat indicators to follow the circular path of the wheel exactly by using arcs instead of rotated rectangles.

#### [MODIFY] [MetronomeWheel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/metronome/MetronomeWheel.kt)
- In the `Canvas` drawing logic, replace `drawRect` with `drawArc`.
- Use `DrawStyle.Stroke` for the arc to give it thickness.
- Calculate the `startAngle` and `sweepAngle` for each beat based on the `beatsPerMeasure`.
- Ensure the arcs are centered on the wheel's radius.

## Verification Plan

### Manual Verification
- Deploy to device/emulator.
- Verify the "More" menu is in the top-right corner.
- Verify the play button is centered in the wheel.
- Verify the BPM arrows are outside the wheel and function correctly.
- Verify the beat indicators are curved and follow the circle.
- Test with different `beatsPerMeasure` to ensure indicators scale correctly.
