# Metronome UI Refinement Walkthrough

I have completed the requested UI refinements for the Metronome component. The layout is now more spacious, and the visual elements have been modernized to follow a circular design language.

## Changes Made

### 1. Fixed "More" Menu Position
Moved the "More" options menu to the top-right corner of the metronome popup. This separates it from the beat and BPM selectors, preventing layout crowding in the top row.

### 2. Centered Play Button & External Arrows
- **Play Button**: Moved to the absolute center of the metronome wheel.
- **BPM Arrows**: Relocated outside the wheel on the left and right sides. This makes the wheel cleaner and easier to interact with via the circular drag gesture.

### 3. Curved Beat Indicators
Replaced the rectangular beat indicators with curved arcs that follow the circular path of the dial exactly. This provides a more cohesive and professional look.

## Code References

### [MetronomeContent](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/metronome/Metronome.kt#L48-L98)
The main layout was updated to use a `Box` for overlaying the menu in the top-right corner.

### [MetronomeWheel](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/molecules/metronome/MetronomeWheel.kt#L52-L162)
The wheel's drawing and control layout were refactored to support the new control positions and curved indicators.

## Verification
- Verified code compilation and resolved all import issues.
- Checked drawing logic to ensure arcs are correctly positioned and centered on the path.
- Ensured BPM adjustment logic (both drag and buttons) remains functional.
