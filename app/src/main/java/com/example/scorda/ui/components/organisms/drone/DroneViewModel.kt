package com.example.scorda.ui.components.organisms.drone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.audio.AudioViewModel
import com.example.scorda.domain.model.drone.Pitch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class DroneViewModel(
    private val audioViewModel: AudioViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(DroneState())
    val uiState: StateFlow<DroneState> = _uiState.asStateFlow()

    init {
        // Sync playback state from AudioViewModel
        audioViewModel.uiState
            .onEach { audioState ->
                _uiState.update { it.copy(isPlaying = audioState.isDronePlaying) }
            }
            .launchIn(viewModelScope)
    }

    fun setPitch(pitch: Pitch) {
        _uiState.update { it.copy(pitch = pitch) }
        updateAudioFrequency()
    }

    fun setOctave(octave: Int) {
        _uiState.update { it.copy(octave = octave) }
        updateAudioFrequency()
    }

    fun setTuning(tuningHz: Int) {
        _uiState.update { it.copy(tuningHz = tuningHz) }
        updateAudioFrequency()
    }

    fun togglePlay() {
        if (uiState.value.isPlaying) {
            audioViewModel.stopDrone()
        } else {
            // Ensure frequency is correct before playing
            updateAudioFrequency()
            audioViewModel.startDrone()
        }
    }

    private fun updateAudioFrequency() {
        audioViewModel.updateDroneFrequency(_uiState.value.frequency)
    }


    companion object {
        fun provideFactory(audioViewModel: AudioViewModel): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as ScordaApplication
                    DroneViewModel(audioViewModel)
                }
            }
    }
}