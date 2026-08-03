package com.example.scorda.domain.model.drone

import kotlin.math.pow

data class DroneState(
    val pitch: Pitch = Pitch.A,
    val octave: Int = 4,
    val tuningHz: Int = 440,
    val isPlaying: Boolean = false
) {
    val frequency: Double
        get() = tuningHz * 2.0.pow((octave - 4) + (pitch.semitonesFromC - 9) / 12.0)
}
