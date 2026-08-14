package com.example.scorda.domain.model.metronome

data class MetronomeState(
    val bpm: Int = 120,
    val beatsPerMeasure: Int = 4,
    val currentBeat: Int = 0,
    val isPlaying: Boolean = false
)
