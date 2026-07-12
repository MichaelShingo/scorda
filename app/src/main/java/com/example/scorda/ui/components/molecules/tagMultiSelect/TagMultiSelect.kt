package com.example.scorda.ui.components.molecules.tagMultiSelect

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.R
import com.example.scorda.data.database.entities.Tag
import com.example.scorda.ui.components.atoms.searchableMultiSelect.SearchableMultiSelect

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagMultiSelect(
    modifier: Modifier = Modifier,
    currentTags: List<Tag>,
    onSelect: (Tag) -> Unit,
    onRemove: (Tag) -> Unit,
) {
    val viewModel: TagMultiSelectViewModel =
        viewModel(factory = TagMultiSelectViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchableMultiSelect(
        modifier = modifier,
        label = stringResource(R.string.score_tags),
        items = uiState.tags.filter { tag ->
            !currentTags.contains(tag)
        },
        currentItems = currentTags,
        convertItemToText = { it.name },
        searchQuery = uiState.searchQuery,
        onQueryChange = { viewModel.onQueryChange(it) },
        onSelect = { tag ->
            onSelect(tag)
            viewModel.onQueryChange("")
        },
        onInsert = {
            viewModel.insertTagFromSearch { newTag ->
                onSelect(newTag)
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
