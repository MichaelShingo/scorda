package com.example.scorda.ui.components.organisms.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.audio.AudioViewModel
import com.example.scorda.domain.model.metronome.MetronomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update


class MetronomeViewModel(
    private val audioViewModel: AudioViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetronomeState())
    val uiState: StateFlow<MetronomeState> = _uiState.asStateFlow()

    init {
        // Sync with AudioViewModel
        audioViewModel.uiState
            .onEach { audioState ->
                _uiState.update {
                    it.copy(
                        isPlaying = audioState.isMetronomePlaying,
                        bpm = audioState.metronomeBpm,
                        beatsPerMeasure = audioState.metronomeBeatsPerMeasure,
                        currentBeat = audioState.currentMetronomeBeat
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun togglePlay() {
        if (uiState.value.isPlaying) {
            audioViewModel.stopMetronome()
        } else {
            audioViewModel.startMetronome()
        }
    }

    fun setBpm(bpm: Int) {
        audioViewModel.setMetronomeBpm(bpm)
    }

    fun setBeatsPerMeasure(beats: Int) {
        audioViewModel.setMetronomeBeatsPerMeasure(beats)
    }


    companion object {
        fun provideFactory(audioViewModel: AudioViewModel): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    MetronomeViewModel(audioViewModel)
                }
            }
    }
}
