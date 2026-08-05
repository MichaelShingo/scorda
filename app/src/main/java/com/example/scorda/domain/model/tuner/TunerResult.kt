package com.example.scorda.domain.model.tuner

import com.example.scorda.domain.model.drone.Pitch
import kotlin.math.pow
import kotlin.math.roundToInt

data class TunerResult(
    val pitch: Pitch,
    val hertz: Float,
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
            val cents =
                ((midi - roundedMidi) * 100).roundToInt() // this calculation is very suspicious 
            val hertz = 440.0f * 2.0f.pow((midi - 69) / 12.0f)

            return TunerResult(
                pitch = pitch,
                hertz = hertz,
                octave = octave,
                cents = cents,
                midi = midi,
                confidence = confidence
            )
        }

        val EMPTY = TunerResult(Pitch.A, 0f, 4, 0, 0f, 0f)
    }
}
