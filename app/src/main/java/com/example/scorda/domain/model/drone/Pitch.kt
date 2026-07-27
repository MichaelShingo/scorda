package com.example.scorda.domain.model.drone

enum class Pitch(val displayName: String, val semitonesFromC: Int) {
    C("C", 0),
    C_SHARP("C#/Db", 1),
    D("D", 2),
    D_SHARP("D#/Eb", 3),
    E("E", 4),
    F("F", 5),
    F_SHARP("F#/Gb", 6),
    G("G", 7),
    G_SHARP("G#/Ab", 8),
    A("A", 9),
    A_SHARP("A#/Bb", 10),
    B("B", 11);

    companion object {
        fun fromSemitones(semitones: Int): Pitch {
            val normalized = ((semitones % 12) + 12) % 12
            return entries.first { it.semitonesFromC == normalized }
        }
    }
}
