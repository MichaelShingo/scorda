package com.example.scorda.ui.components.molecules.keySignatureSelect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.R
import com.example.scorda.data.database.entities.KeySignature

@Composable
fun KeySignatureSelect(
    currentKeySignature: KeySignature?,
    onChange: (key: KeySignature) -> Unit,
) {
    val viewModel: KeySignatureSelectViewModel =
        viewModel(factory = KeySignatureSelectViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pitchOptions = Pitch.entries.filter { it != Pitch.NONE }
    val accidentalOptions = Accidental.entries
    val modeOptions = Mode.entries


    LaunchedEffect(currentKeySignature) {
        viewModel.initialize(currentKeySignature)
    }

    LaunchedEffect(uiState) {
        viewModel.convertPitchAccidentalModeToKeySignature(
            uiState.pitch,
            uiState.accidental,
            uiState.mode
        )?.let {
            onChange(it)
        }
    }

    Column(
        modifier = Modifier
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            pitchOptions.forEachIndexed { index, pitch ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = pitchOptions.size
                    ),
                    onClick = { viewModel.onChangePitch(pitch) },
                    selected = pitch == uiState.pitch,
                    label = { Text(pitch.name) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SingleChoiceSegmentedButtonRow {
                accidentalOptions.forEachIndexed { index, accidental ->
                    val iconRes = when (accidental) {
                        Accidental.SHARP -> R.drawable.ic_sharp
                        Accidental.FLAT -> R.drawable.ic_flat
                        Accidental.NATURAL -> R.drawable.ic_natural
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = accidentalOptions.size
                        ),
                        onClick = { viewModel.onChangeAccidental(accidental) },
                        selected = accidental == uiState.accidental,
                        label = {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = accidental.name,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                modeOptions.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = modeOptions.size
                        ),
                        onClick = { viewModel.onChangeMode(mode) },
                        selected = mode == uiState.mode,
                        label = { Text(mode.displayName) }
                    )
                }
            }
        }
    }

}
