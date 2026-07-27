package com.example.scorda.audio

interface AudioEngine {
    fun startDrone(frequency: Double)
    fun stopDrone()
    fun updateFrequency(frequency: Double)
    fun release()
}
