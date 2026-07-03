package com.example.scorda.ui.components.organisms.searchScores

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.R
import com.example.scorda.ui.viewmodel.LocalSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScores(
) {
    val vm = LocalSearchViewModel.current

    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val searchResults by vm.searchResults.collectAsStateWithLifecycle()
    val isActive by vm.isSearchActive.collectAsStateWithLifecycle()
    val onQueryChange = vm::onQueryChange
    val onActiveChange = vm::onSearchActiveChange


    Log.d("my search results", searchResults.toString())
    Log.d("my query", query)

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = isActive,
                onExpandedChange = onActiveChange,
                placeholder = { Text(stringResource(R.string.search_scores_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (isActive) {
                        IconButton(onClick = {
                            if (query.isNotEmpty()) onQueryChange("") else onActiveChange(false)
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            )
        },
        expanded = true,
        onExpandedChange = onActiveChange,
        modifier = Modifier
            .padding(start = 32.dp, end = 32.dp, top = 64.dp, bottom = 32.dp)
            .background(color = MaterialTheme.colorScheme.errorContainer)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(searchResults) { score ->
                ScoreListItem(
                    scoreWithDetails = score,
                    modifier = Modifier,
                )
            }
        }

    }
}