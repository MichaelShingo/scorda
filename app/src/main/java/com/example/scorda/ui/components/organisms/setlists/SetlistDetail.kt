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
import androidx.compose.ui.Modifier
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.data.database.relations.SetlistWithDetails
import com.example.scorda.ui.components.molecules.scoreListItem.ScoreListItem

@Composable
fun SetlistDetail(
    setlistWithDetails: SetlistWithDetails,
    onScoreClick: (ScoreWithDetails) -> Unit,
    modifier: Modifier = Modifier,
    currentScoreId: Long? = null,
    onRemoveScoreClick: ((ScoreWithDetails) -> Unit)? = null,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentScoreId, setlistWithDetails.scores) {
        val index = setlistWithDetails.scores.indexOfFirst { it.score.id == currentScoreId }
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
        items(setlistWithDetails.scores) { scoreWithDetails ->
            ScoreListItem(
                scoreWithDetails = scoreWithDetails,
                modifier = Modifier,
                isSelected = scoreWithDetails.score.id == currentScoreId,
                leadingContent = onRemoveScoreClick?.let {
                    {
                        IconButton(
                            onClick = { it(scoreWithDetails) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = "Remove from setlist",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                onClick = { onScoreClick(scoreWithDetails) }
            )
        }
    }
}
