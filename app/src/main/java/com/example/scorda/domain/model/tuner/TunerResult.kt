package com.example.scorda.domain.model.tuner

import com.example.scorda.domain.model.drone.Pitch
import kotlin.math.roundToInt

data class TunerResult(
    val pitch: Pitch,
    val octave: Int,
    val cents: Int,
    val midi: Float,
    val confidence: Float
) {
    companion object {
        fun fromMidi(midi: Float, confidence: Float): TunerResult {
            val roundedMidi = midi.roundToInt()
            val pitch = Pitch.fromSemitones(roundedMidi % 12)
            val octave = (roundedMidi / 12) - 1 // MIDI 60 is C4
            val cents = ((midi - roundedMidi) * 100).roundToInt()

            return TunerResult(
                pitch = pitch,
                octave = octave,
                cents = cents,
                midi = midi,
                confidence = confidence
            )
        }

        val EMPTY = TunerResult(Pitch.A, 4, 0, 0f, 0f)
    }
}
