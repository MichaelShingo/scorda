package com.example.scorda.ui.components.organisms.setlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.ui.components.organisms.searchScores.SearchScores
import com.example.scorda.ui.components.organisms.searchScores.SearchScoresContext
import com.example.scorda.ui.viewmodel.SetlistViewModel

@Composable
fun AddScoreToSetlistScreen(
    setlistId: Long,
    onClose: () -> Unit,
    setlistViewModel: SetlistViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by setlistViewModel.uiState.collectAsStateWithLifecycle()
    val setlistWithDetails = uiState.selectedSetlist

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Scores to ${setlistWithDetails?.setlist?.name ?: ""}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider()

            Row(modifier = Modifier.weight(1f)) {
                // Left half: Current scores in setlist
                Box(modifier = Modifier.weight(1f)) {
                    SetlistDetail(
                        viewModel = setlistViewModel,
                        showLeadingRemoveButton = true
                    )
                }

                VerticalDivider()

                // Right half: Search and add
                Box(modifier = Modifier.weight(1f)) {
                    val addedScoreIds =
                        setlistWithDetails?.entries?.map { it.crossRef.scoreId }?.toSet()
                            ?: emptySet()
                    SearchScores(
                        context = SearchScoresContext.Setlist,
                        addedScoreIds = addedScoreIds,
                        onScoreClick = { scoreId ->
                            setlistViewModel.addScoreToSetlist(scoreId, setlistId)
                        }
                    )
                }
            }
        }
    }
}
