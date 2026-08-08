package com.example.scorda.ui.components.organisms.metronome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.audio.LocalAudioViewModel
import com.example.scorda.domain.model.metronome.MetronomeState
import com.example.scorda.ui.components.atoms.VerticalNumberSelector
import com.example.scorda.ui.components.molecules.metronome.MetronomeMenu
import com.example.scorda.ui.components.molecules.metronome.MetronomeWheel

@Composable
fun Metronome() {
    val audioViewModel = LocalAudioViewModel.current
    val viewModel: MetronomeViewModel = viewModel(
        factory = MetronomeViewModel.provideFactory(audioViewModel)
    )
    val uiState by viewModel.uiState.collectAsState()

    MetronomeContent(
        uiState = uiState,
        onBpmChange = viewModel::setBpm,
        onBeatsPerMeasureChange = viewModel::setBeatsPerMeasure,
        onTogglePlay = viewModel::togglePlay,
    )
}

@Composable
fun MetronomeContent(
    uiState: MetronomeState,
    onBpmChange: (Int) -> Unit,
    onBeatsPerMeasureChange: (Int) -> Unit,
    onTogglePlay: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Top Row: Selectors and Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VerticalNumberSelector(
                    value = uiState.beatsPerMeasure,
                    onValueChange = onBeatsPerMeasureChange,
                    range = 1..128,
                    label = "Beats"
                )

                VerticalNumberSelector(
                    value = uiState.bpm,
                    onValueChange = onBpmChange,
                    range = 10..300,
                    label = "BPM"
                )
            }

            MetronomeMenu()
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Metronome Wheel
        MetronomeWheel(
            bpm = uiState.bpm,
            onBpmChange = onBpmChange,
            beatsPerMeasure = uiState.beatsPerMeasure,
            currentBeat = uiState.currentBeat,
            isPlaying = uiState.isPlaying,
            onTogglePlay = onTogglePlay,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MetronomePreview() {
    MetronomeContent(
        uiState = MetronomeState(
            bpm = 120,
            beatsPerMeasure = 4,
            currentBeat = 0,
            isPlaying = false
        ),
        onBpmChange = {},
        onBeatsPerMeasureChange = {},
        onTogglePlay = {},
    )
}
