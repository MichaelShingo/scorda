package com.example.scorda.ui.components.organisms.metronome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier) {
                    VerticalNumberSelector(
                        value = uiState.beatsPerMeasure,
                        onValueChange = onBeatsPerMeasureChange,
                        range = 1..128,
                        label = "Beats"
                    )
                }
                Box(
                    modifier = Modifier
                ) {
                    MetronomeMenu()
                }
                Box(modifier = Modifier) {
                    VerticalNumberSelector(
                        value = uiState.bpm,
                        onValueChange = onBpmChange,
                        range = 10..300,
                        label = "BPM"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBpmChange((uiState.bpm - 1).coerceAtLeast(10)) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "+1 BPM",
                        modifier = Modifier.size(48.dp)
                    )
                }
                MetronomeWheel(
                    bpm = uiState.bpm,
                    onBpmChange = onBpmChange,
                    beatsPerMeasure = uiState.beatsPerMeasure,
                    currentBeat = uiState.currentBeat,
                    isPlaying = uiState.isPlaying,
                    onTogglePlay = onTogglePlay,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onBpmChange((uiState.bpm + 1).coerceAtMost(300)) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "+1 BPM",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

        }
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
