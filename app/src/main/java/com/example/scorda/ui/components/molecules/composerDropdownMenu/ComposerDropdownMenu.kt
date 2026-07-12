package com.example.scorda.ui.components.molecules.composerDropdownMenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.R
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.ui.components.atoms.searchableDropdownMenu.SearchableDropdownMenu
import com.example.scorda.util.getCommaSeparatedFullName

@Composable
fun ComposerDropdownMenu(
    currentComposer: Composer?,
    onClear: () -> Unit,
    onSelect: (composer: Composer) -> Unit,
    modifier: Modifier = Modifier,
    key: Any? = Unit
) {
    val viewModel: ComposerDropdownMenuViewModel =
        viewModel(factory = ComposerDropdownMenuViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key) {
        currentComposer?.let {
            viewModel.onQueryChange(getCommaSeparatedFullName(it))
        } ?: run {
            viewModel.onQueryChange("")
        }
    }

    SearchableDropdownMenu<Composer>(
        label = stringResource(R.string.score_composer),
        items = uiState.composers,
        convertItemToText = { getCommaSeparatedFullName(it) },
        searchQuery = uiState.searchQuery,
        onQueryChange = { viewModel.onQueryChange(it) },
        onSelect = { composer ->
            onSelect(composer)
            viewModel.onQueryChange(getCommaSeparatedFullName(composer))
        },
        onInsert = {
            viewModel.insertComposerFromSearch { newComposer ->
                onSelect(newComposer)
            }
        },
        valueToAdd = uiState.valToAdd,
        onClear = {
            onClear()
            viewModel.onQueryChange("")
        },
        modifier = modifier
    )
}
