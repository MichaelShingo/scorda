package com.example.scorda.ui.components.molecules.instrumentMultiSelect

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.R
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.ui.components.atoms.searchableMultiSelect.SearchableMultiSelect

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

    SearchableMultiSelect(
        modifier = modifier,
        label = stringResource(R.string.score_instruments),
        items = uiState.instruments.filter { instrument ->
            !currentInstruments.contains(instrument)
        },
        currentItems = currentInstruments,
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
        closeOnSelectItem = false,
        onRemove = { onRemove(it) }
    )
}
