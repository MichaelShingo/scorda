package com.example.scorda.domain.model.drone

import org.junit.Assert.assertEquals
import org.junit.Test

class DroneStateTest {

    @Test
    fun `frequency of A4 with 440Hz tuning is 440Hz`() {
        val state = DroneState(pitch = Pitch.A, octave = 4, tuningHz = 440)
        assertEquals(440.0, state.frequency, 0.001)
    }

    @Test
    fun `frequency of A3 with 440Hz tuning is 220Hz`() {
        val state = DroneState(pitch = Pitch.A, octave = 3, tuningHz = 440)
        assertEquals(220.0, state.frequency, 0.001)
    }

    @Test
    fun `frequency of A5 with 440Hz tuning is 880Hz`() {
        val state = DroneState(pitch = Pitch.A, octave = 5, tuningHz = 440)
        assertEquals(880.0, state.frequency, 0.001)
    }

    @Test
    fun `frequency of C4 with 440Hz tuning is approximately 261_63Hz`() {
        val state = DroneState(pitch = Pitch.C, octave = 4, tuningHz = 440)
        assertEquals(261.625, state.frequency, 0.001)
    }

    @Test
    fun `tuningHz shifts the base frequency`() {
        val state = DroneState(pitch = Pitch.A, octave = 4, tuningHz = 442)
        assertEquals(442.0, state.frequency, 0.001)
    }
}
