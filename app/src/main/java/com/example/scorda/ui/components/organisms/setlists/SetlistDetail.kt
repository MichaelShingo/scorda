package com.example.scorda.ui.components.organisms.setlists

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.data.database.relations.SetlistEntry
import com.example.scorda.data.database.relations.SetlistWithDetails
import com.example.scorda.ui.components.molecules.scoreListItem.ScoreListItem

@Composable
fun SetlistDetail(
    setlistWithDetails: SetlistWithDetails,
    onScoreClick: (ScoreWithDetails) -> Unit,
    modifier: Modifier = Modifier,
    currentScoreId: Long? = null,
    onRemoveScoreClick: ((SetlistEntry) -> Unit)? = null,
) {
    val listState = rememberLazyListState()

    val sortedEntries = remember(setlistWithDetails.entries) {
        setlistWithDetails.entries.sortedBy { it.crossRef.position }
    }

    LaunchedEffect(currentScoreId, sortedEntries) {
        val index = sortedEntries.indexOfFirst { it.scoreWithDetails.score.id == currentScoreId }
        if (index != -1) {
            val viewportHeight = listState.layoutInfo.viewportSize.height
            if (viewportHeight > 0) {
                // Offset by half the viewport to center the top of the item
                listState.animateScrollToItem(index, scrollOffset = -viewportHeight / 2)
            } else {
                listState.scrollToItem(index)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = sortedEntries,
            key = { entry: SetlistEntry -> entry.crossRef.id }
        ) { entry ->
            ScoreListItem(
                scoreWithDetails = entry.scoreWithDetails,
                modifier = Modifier,
                isSelected = entry.scoreWithDetails.score.id == currentScoreId,
                leadingContent = onRemoveScoreClick?.let { onRemove ->
                    {
                        IconButton(
                            onClick = { onRemove(entry) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = "Remove from setlist",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                onClick = { onScoreClick(entry.scoreWithDetails) }
            )
        }
    }
}
