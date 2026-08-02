package com.example.scorda.domain.model.drone

enum class Pitch(val displayName: String, val semitonesFromC: Int) {
    C("C", 0),
    C_SHARP("#/b", 1),
    D("D", 2),
    D_SHARP("#/b", 3),
    E("E", 4),
    F("F", 5),
    F_SHARP("#/b", 6),
    G("G", 7),
    G_SHARP("#/b", 8),
    A("A", 9),
    A_SHARP("#/b", 10),
    B("B", 11);

    companion object {
        fun fromSemitones(semitones: Int): Pitch {
            val normalized = ((semitones % 12) + 12) % 12
            return entries.first { it.semitonesFromC == normalized }
        }
    }
}
