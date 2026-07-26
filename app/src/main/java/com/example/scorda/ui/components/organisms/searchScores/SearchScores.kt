package com.example.scorda.ui.components.organisms.searchScores

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.R
import com.example.scorda.ui.components.molecules.scoreListItem.ScoreListItem
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import com.example.scorda.ui.viewmodel.LocalSearchViewModel

enum class SearchScoresContext {
    Search,
    Setlist
}

@Composable
fun SearchScores(
    modifier: Modifier = Modifier,
    context: SearchScoresContext = SearchScoresContext.Search,
    addedScoreIds: Set<Long> = emptySet(),
    onScoreClick: (Long) -> Unit = {},
) {
    val vm = LocalSearchViewModel.current
    val scoreViewModel = LocalScoreViewModel.current

    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val searchResults by vm.searchResults.collectAsStateWithLifecycle()
    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val currentScoreId = scoreUiState.selectedScore?.score?.id

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = vm::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(R.string.search_scores_placeholder)) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { vm.onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(searchResults, key = { it.score.id }) { score ->
                val isSelected = when (context) {
                    SearchScoresContext.Search -> score.score.id == currentScoreId
                    SearchScoresContext.Setlist -> addedScoreIds.contains(score.score.id)
                }

                ScoreListItem(
                    scoreWithDetails = score,
                    isSelected = isSelected,
                    onClick = {
                        if (context == SearchScoresContext.Search) {
                            scoreViewModel.selectScore(score.score.id)
                        }
                        onScoreClick(score.score.id)
                    }
                )
            }
        }
    }
}
