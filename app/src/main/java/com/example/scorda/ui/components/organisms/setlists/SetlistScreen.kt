package com.example.scorda.ui.components.organisms.setlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.data.database.entities.Setlist
import com.example.scorda.ui.viewmodel.SetlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistScreen(
    modifier: Modifier = Modifier,
    viewModel: SetlistViewModel = viewModel(factory = SetlistViewModel.Factory)
) {
    val setlists by viewModel.setlists.collectAsStateWithLifecycle()
    var selectedSetlist by remember { mutableStateOf<Setlist?>(null) }
    var isAddingSetlist by remember { mutableStateOf(false) }
    var editingSetlist by remember { mutableStateOf<Setlist?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = selectedSetlist?.name ?: "Setlists")
                },
                navigationIcon = {
                    if (selectedSetlist != null) {
                        IconButton(onClick = { selectedSetlist = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Setlists"
                            )
                        }
                    }
                },
                actions = {
                    if (selectedSetlist == null) {
                        IconButton(onClick = { isAddingSetlist = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Setlist")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedSetlist != null) {
                val setlistWithDetails by viewModel.getSetlist(selectedSetlist!!.id)
                    .collectAsStateWithLifecycle(initialValue = null)

                setlistWithDetails?.let {
                    SetlistDetail(setlistWithDetails = it)
                }
            } else {
                SetlistList(
                    setlists = setlists,
                    onSetlistClick = { selectedSetlist = it },
                    onMoreClick = { editingSetlist = it }
                )
            }
        }
    }

    if (isAddingSetlist) {
        SetlistDialog(
            onDismissRequest = { isAddingSetlist = false },
            onConfirm = { name ->
                viewModel.addSetlist(name)
                isAddingSetlist = false
            }
        )
    }

    if (editingSetlist != null) {
        SetlistDialog(
            setlist = editingSetlist,
            onDismissRequest = { editingSetlist = null },
            onConfirm = { name ->
                viewModel.updateSetlist(editingSetlist!!.copy(name = name))
                editingSetlist = null
            }
        )
    }
}
