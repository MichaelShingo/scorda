package com.example.scorda.ui.components.atoms.composerDropdownMenu

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scorda.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdownMenu(
    label: String,
    items: List<T>,
    convertItemToText: (item: T) -> String,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSelect: (item: T) -> Unit,
    modifier: Modifier = Modifier,
    onInsert: () -> Unit, // inserts based query in ViewModel
    valueToAdd: String?,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val addDisplayText = valueToAdd ?: searchQuery

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                onQueryChange(it)
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onClear()
                            onQueryChange("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = ""
                        )
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            Modifier
                .exposedDropdownSize()
                .heightIn(max = 280.dp)
        ) {
            items.forEach { item ->
                val text = convertItemToText(item)
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("""${stringResource(R.string.score_add_dropdown_field)} "$addDisplayText"""") },
                onClick = {
                    onInsert()
                    expanded = false
                },
            )
        }
    }
}
