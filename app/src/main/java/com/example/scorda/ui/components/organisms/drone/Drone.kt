package com.example.scorda.ui.components.organisms.drone

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.ui.components.atoms.VerticalNumberSelector
import com.example.scorda.ui.components.molecules.drone.PitchWheel
import com.example.scorda.ui.viewmodel.DroneViewModel

@Composable
fun Drone(
    viewModel: DroneViewModel = viewModel(factory = DroneViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    DroneContent(
        uiState = uiState,
        onOctaveChange = viewModel::setOctave,
        onTuningChange = viewModel::setTuning,
        onPitchSelected = viewModel::setPitch,
        onTogglePlay = viewModel::togglePlay
    )
}

@Composable
fun DroneContent(
    uiState: com.example.scorda.domain.model.drone.DroneState,
    onOctaveChange: (Int) -> Unit,
    onTuningChange: (Int) -> Unit,
    onPitchSelected: (com.example.scorda.domain.model.drone.Pitch) -> Unit,
    onTogglePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Octave and Hz Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VerticalNumberSelector(
                value = uiState.octave,
                onValueChange = onOctaveChange,
                range = 0..8,
                label = "Octave"
            )

            VerticalNumberSelector(
                value = uiState.tuningHz,
                onValueChange = onTuningChange,
                range = 430..450,
                label = "A (Hz)"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pitch Wheel
        PitchWheel(
            selectedPitch = uiState.pitch,
            onPitchSelected = onPitchSelected,
            isPlaying = uiState.isPlaying,
            onTogglePlay = onTogglePlay,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DronePreview() {
    DroneContent(
        uiState = com.example.scorda.domain.model.drone.DroneState(
            pitch = com.example.scorda.domain.model.drone.Pitch.A,
            octave = 4,
            tuningHz = 440,
            isPlaying = false
        ),
        onOctaveChange = {},
        onTuningChange = {},
        onPitchSelected = {},
        onTogglePlay = {}
    )
}
