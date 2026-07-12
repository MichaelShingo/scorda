package com.example.scorda.ui.components.molecules.composerDropdownMenu.instrumentMultiSelect

import InstrumentMultiSelectViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.R
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.ui.components.atoms.searchableDropdownMenu.SearchableDropdownMenu

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstrumentMultiSelect(
    modifier: Modifier = Modifier,
    currentInstruments: List<Instrument>,
    onSelect: (instrument: Instrument) -> Unit,
    onRemove: (Instrument) -> Unit,
) {
    val viewModel: InstrumentMultiSelectViewModel =
        viewModel(factory = InstrumentMultiSelectViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (currentInstruments.isNotEmpty()) {
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    currentInstruments.forEach { instrument ->
                        InputChip(
                            selected = true,
                            onClick = { onRemove(instrument) },
                            label = { Text(instrument.name) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.remove_instrument),
                                    modifier = Modifier.size(InputChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            }
        }
        SearchableDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.score_instruments),
            items = uiState.instruments.filter { instrument ->
                !currentInstruments.contains(instrument)
            },
            convertItemToText = { it.name },
            searchQuery = uiState.searchQuery,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSelect = { instrument ->
                onSelect(instrument)
                viewModel.onQueryChange("")
            },
            onInsert = {
                viewModel.insertInstrumentFromSearch { newInstrument ->
                    onSelect(newInstrument)
                    viewModel.onQueryChange("")
                }
            },
            onClear = {
                viewModel.onQueryChange("")
            },
            closeOnSelectItem = false
        )

    }
}
