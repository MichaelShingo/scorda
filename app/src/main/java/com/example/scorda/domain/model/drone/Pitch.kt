package com.example.scorda.domain.model.drone

enum class Pitch(
    val displayNameDrone: String,
    val semitonesFromC: Int,
    val displayNameSharps: String,
    val displayNameFlats: String
) {
    C("C", 0, "C", "C"),
    C_SHARP("#/b", 1, "C#", "Db"),
    D("D", 2, "D", "D"),
    D_SHARP("#/b", 3, "D#", "Eb"),
    E("E", 4, "E", "E"),
    F("F", 5, "F", "F"),
    F_SHARP("#/b", 6, "F#", "Gb"),
    G("G", 7, "G", "G"),
    G_SHARP("#/b", 8, "G#", "Ab"),
    A("A", 9, "A", "A"),
    A_SHARP("#/b", 10, "A#", "Bb"),
    B("B", 11, "B", "B");

    companion object {
        fun fromSemitones(semitones: Int): Pitch {
            val normalized = ((semitones % 12) + 12) % 12
            return entries.first { it.semitonesFromC == normalized }
        }
    }
}
