package com.example.scorda.domain.model.tuner

data class TunerState(
    val tuningHz: Int = 440,
    val tunerResult: TunerResult = TunerResult.EMPTY,
    val isListening: Boolean = false,
    val hasPermission: Boolean = false
)
