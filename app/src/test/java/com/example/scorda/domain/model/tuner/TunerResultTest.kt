package com.example.scorda.domain.model.tuner

import com.example.scorda.domain.model.drone.Pitch
import org.junit.Assert.assertEquals
import org.junit.Test

class TunerResultTest {

    @Test
    fun `midi 69 is A4 with 0 cents`() {
        val result = TunerResult.fromMidi(69.0f, 1.0f)
        assertEquals(Pitch.A, result.pitch)
        assertEquals(4, result.octave)
        assertEquals(0, result.cents)
    }

    @Test
    fun `midi 69_5 is A4 with 50 cents`() {
        val result = TunerResult.fromMidi(69.5f, 1.0f)
        assertEquals(Pitch.A, result.pitch)
        assertEquals(4, result.octave)
        assertEquals(50, result.cents)
    }

    @Test
    fun `midi 68_5 is A4 with -50 cents`() {
        val result = TunerResult.fromMidi(68.5f, 1.0f)
        assertEquals(Pitch.A, result.pitch)
        assertEquals(4, result.octave)
        assertEquals(-50, result.cents)
    }

    @Test
    fun `midi 60 is C4`() {
        val result = TunerResult.fromMidi(60.0f, 1.0f)
        assertEquals(Pitch.C, result.pitch)
        assertEquals(4, result.octave)
    }
}
