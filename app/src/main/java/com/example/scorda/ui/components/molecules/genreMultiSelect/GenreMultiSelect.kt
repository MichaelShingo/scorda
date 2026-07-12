package com.example.scorda.ui.components.molecules.genreMultiSelect

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.R
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.ui.components.atoms.searchableMultiSelect.SearchableMultiSelect

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreMultiSelect(
    modifier: Modifier = Modifier,
    currentGenres: List<Genre>,
    onSelect: (genre: Genre) -> Unit,
    onRemove: (Genre) -> Unit,
) {
    val viewModel: GenreMultiSelectViewModel =
        viewModel(factory = GenreMultiSelectViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchableMultiSelect(
        modifier = modifier,
        label = stringResource(R.string.score_genres),
        items = uiState.genres.filter { genre ->
            !currentGenres.contains(genre)
        },
        currentItems = currentGenres,
        convertItemToText = { it.name },
        searchQuery = uiState.searchQuery,
        onQueryChange = { viewModel.onQueryChange(it) },
        onSelect = { genre ->
            onSelect(genre)
            viewModel.onQueryChange("")
        },
        onInsert = {
            viewModel.insertGenreFromSearch { newGenre ->
                onSelect(newGenre)
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
