package com.example.scorda.domain.model.tuner

import com.example.scorda.domain.model.drone.Pitch

data class TunerResult(
    val pitch: Pitch,
    val hertz: Float,
    val octave: Int,
    val cents: Int,
    val midi: Float,
    val confidence: Float,
    val hasSignal: Boolean = false
) {
    companion object {
        val EMPTY = TunerResult(Pitch.A, 0f, 4, 0, 0f, 0f, false)
    }
}
