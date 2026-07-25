package com.example.scorda.ui.components.organisms.setlists

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.data.database.relations.SetlistEntry
import com.example.scorda.data.database.relations.SetlistWithDetails
import com.example.scorda.ui.components.molecules.scoreListItem.ScoreListItem
import com.example.scorda.ui.viewmodel.SetlistViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SetlistDetail(
    modifier: Modifier = Modifier,
    viewModel: SetlistViewModel,
    onScoreClick: ((ScoreWithDetails) -> Unit)? = null,
    currentScoreId: Long? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.selectedSetlist?.let { details ->
        SetlistDetailContent(
            setlistWithDetails = details,
            onScoreClick = onScoreClick,
            modifier = modifier,
            currentScoreId = currentScoreId,
            onRemoveScoreClick = { entry ->
                viewModel.removeScoreFromSetlist(entry.crossRef.id)
            },
            onMoveScore = { from, to ->
                viewModel.moveScore(from, to)
            }
        )
    }
}

/**
 * Stateless content for displaying setlist details.
 */
@Composable
fun SetlistDetailContent(
    setlistWithDetails: SetlistWithDetails,
    modifier: Modifier = Modifier,
    onScoreClick: ((ScoreWithDetails) -> Unit)? = null,
    currentScoreId: Long? = null,
    onRemoveScoreClick: ((SetlistEntry) -> Unit)? = null,
    onMoveScore: ((from: Int, to: Int) -> Unit)? = null,
) {
    val entries = setlistWithDetails.entries

    val draggingList = remember(entries) {
        entries.toMutableStateList()
    }

    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        draggingList.apply {
            add(to.index, removeAt(from.index))
        }
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LaunchedEffect(currentScoreId, entries) {
        val index = entries.indexOfFirst { it.scoreWithDetails.score.id == currentScoreId }
        if (index != -1) {
            val viewportHeight = listState.layoutInfo.viewportSize.height
            if (viewportHeight > 0) {
                listState.animateScrollToItem(index, scrollOffset = -viewportHeight / 2)
            } else {
                listState.scrollToItem(index)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = draggingList,
            key = { _, entry -> entry.crossRef.id }
        ) { _, entry ->
            ReorderableItem(reorderableState, key = entry.crossRef.id) { isDragging ->
                ScoreListItem(
                    scoreWithDetails = entry.scoreWithDetails,
                    modifier = Modifier.animateItem(),
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
                    trailingContent = onMoveScore?.let { moveCallback ->
                        {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Drag to reorder",
                                tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.draggableHandle(
                                    onDragStarted = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragStopped = {
                                        val fromIndex =
                                            entries.indexOfFirst { it.crossRef.id == entry.crossRef.id }
                                        val toIndex =
                                            draggingList.indexOfFirst { it.crossRef.id == entry.crossRef.id }
                                        if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                                            moveCallback(fromIndex, toIndex)
                                        }
                                    }
                                )
                            )
                        }
                    },
                    onClick = {
                        onScoreClick?.invoke(entry.scoreWithDetails)
                    }
                )
            }
        }
    }
}
