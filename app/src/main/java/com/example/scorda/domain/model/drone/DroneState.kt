package com.example.scorda.domain.model.drone

data class DroneState(
    val pitch: Pitch = Pitch.C,
    val octave: Int = 4,
    val tuningHz: Int = 440,
    val isPlaying: Boolean = false
) {
    val frequency: Double
        get() {
            // Frequency formula: f = f0 * 2^((n-n0)/12)
            // A4 = 440Hz (octave 4, pitch A)
            // A4 is 9 semitones from C4
            val semitonesFromA4 = (octave - 4) * 12 + (pitch.semitonesFromC - 9)
            return tuningHz * Math.pow(2.0, semitonesFromA4 / 12.0)
        }
}
