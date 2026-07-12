package com.example.scorda.ui.components.atoms.searchableMultiSelect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scorda.R
import com.example.scorda.ui.components.atoms.searchableDropdown.SearchableDropdown

@Composable
fun <T> SearchableMultiSelect(
    modifier: Modifier = Modifier,
    items: List<T>,
    currentItems: List<T>,
    onRemove: (item: T) -> Unit,
    onInsert: () -> Unit,
    convertItemToText: (item: T) -> String,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSelect: (item: T) -> Unit,
    onClear: () -> Unit,
    closeOnSelectItem: Boolean? = true

) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (currentItems.isNotEmpty()) {
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    currentItems.forEach { item ->
                        InputChip(
                            selected = true,
                            onClick = { onRemove(item) },
                            label = { Text(convertItemToText(item)) },
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
        SearchableDropdown<T>(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.score_instruments),
            items = items,
            convertItemToText = convertItemToText,
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            onSelect = onSelect,
            onInsert = onInsert,
            onClear = onClear,
            closeOnSelectItem = closeOnSelectItem
        )

    }
}