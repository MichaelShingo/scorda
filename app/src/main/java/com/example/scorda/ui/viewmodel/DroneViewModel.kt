package com.example.scorda.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.audio.AudioEngine
import com.example.scorda.domain.model.drone.DroneState
import com.example.scorda.domain.model.drone.Pitch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DroneViewModel(
    private val audioEngine: AudioEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(DroneState())
    val uiState: StateFlow<DroneState> = _uiState.asStateFlow()

    fun setPitch(pitch: Pitch) {
        _uiState.update { it.copy(pitch = pitch) }
        updateEngine()
    }

    fun setOctave(octave: Int) {
        _uiState.update { it.copy(octave = octave) }
        updateEngine()
    }

    fun setTuning(tuningHz: Int) {
        _uiState.update { it.copy(tuningHz = tuningHz) }
        updateEngine()
    }

    fun togglePlay() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
        if (_uiState.value.isPlaying) {
            audioEngine.startDrone(_uiState.value.frequency)
        } else {
            audioEngine.stopDrone()
        }
    }

    private fun updateEngine() {
        if (_uiState.value.isPlaying) {
            audioEngine.updateFrequency(_uiState.value.frequency)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ScordaApplication)
                DroneViewModel(application.container.audioEngine)
            }
        }
    }
}
