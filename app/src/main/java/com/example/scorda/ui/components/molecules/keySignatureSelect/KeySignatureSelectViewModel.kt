package com.example.scorda.ui.components.molecules.keySignatureSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.data.database.entities.KeySignature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class Pitch(val value: String) {
    NONE("NONE"),
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    F("F"),
    G("G")
}

enum class Accidental {
    SHARP,
    FLAT,
    NATURAL
}

enum class Mode(val displayName: String) {
    MAJOR("Major"),
    MINOR("Minor")
}

data class KeySignatureSelectUiState(
    val pitch: Pitch = Pitch.NONE,
    val accidental: Accidental = Accidental.NATURAL,
    val mode: Mode = Mode.MAJOR
)

class KeySignatureSelectViewModel : ViewModel() {
    private val _pitch = MutableStateFlow(Pitch.NONE)
    private val _accidental = MutableStateFlow(Accidental.NATURAL)
    private val _mode = MutableStateFlow(Mode.MAJOR)

    val uiState: StateFlow<KeySignatureSelectUiState> = combine(
        _pitch,
        _accidental,
        _mode
    ) { pitch, accidental, mode ->
        KeySignatureSelectUiState(
            pitch,
            accidental,
            mode
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        KeySignatureSelectUiState(),
    )

    fun initialize(keySignature: KeySignature?) {
        if (keySignature == null) {
            _pitch.value = Pitch.NONE
            return
        }
        try {
            val split = keySignature.name.split("_")
            if (split.size == 3) {
                _pitch.value = Pitch.valueOf(split[0])
                _accidental.value = Accidental.valueOf(split[1])
                _mode.value = Mode.valueOf(split[2])
            }
        } catch (e: Exception) {
            _pitch.value = Pitch.NONE
        }
    }

    fun onChangePitch(pitch: Pitch) {
        _pitch.value = pitch
    }

    fun onChangeAccidental(accidental: Accidental) {
        _accidental.value = accidental
    }

    fun onChangeMode(mode: Mode) {
        _mode.value = mode
    }

    fun convertPitchAccidentalModeToKeySignature(
        pitch: Pitch,
        accidental: Accidental,
        mode: Mode
    ): KeySignature? {
        if (pitch == Pitch.NONE) return null
        val keySignatureKey = "${pitch.name}_${accidental.name}_${mode.name}"
        return try {
            KeySignature.valueOf(keySignatureKey)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                KeySignatureSelectViewModel()
            }
        }
    }

}