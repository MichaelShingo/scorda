package com.example.scorda.ui.components.organisms.searchScores

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scorda.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScores(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    resultsContent: @Composable () -> Unit = {}
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = active,
                onExpandedChange = onActiveChange,
                placeholder = { Text(stringResource(R.string.search_scores_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (active) {
                        IconButton(onClick = {
                            if (query.isNotEmpty()) onQueryChange("") else onActiveChange(false)
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            )
        },
        expanded = active,
        onExpandedChange = onActiveChange,
        modifier = modifier
            .fillMaxWidth()
            // Standard padding when not expanded
            .padding(horizontal = if (!active) 16.dp else 0.dp)
    ) {
        // This trailing lambda is the MANDATORY 'content' parameter
        // for search results/suggestions
        resultsContent()
    }
}