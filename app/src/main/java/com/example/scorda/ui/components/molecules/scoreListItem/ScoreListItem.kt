package com.example.scorda.ui.components.molecules.scoreListItem

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.scorda.R
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.ui.components.organisms.scoreDetailDialog.ScoreDetailDialog
import com.example.scorda.ui.theme.WarningYellow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class DragValue {
    Settled,
    Revealed
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScoreListItem(
    scoreWithDetails: ScoreWithDetails,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    var isOpenScoreDetailDialog by remember { mutableStateOf<Boolean>(false) }
    val scope = rememberCoroutineScope()

    if (onRemove == null) {
        ScoreListItemContent(
            scoreWithDetails = scoreWithDetails,
            modifier = modifier,
            isSelected = isSelected,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            onClick = onClick,
            onShowInfo = { isOpenScoreDetailDialog = true }
        )
    } else {
        val density = LocalDensity.current
        val actionWidth = 80.dp
        val actionWidthPx = with(density) { actionWidth.toPx() }

        // Non-deprecated AnchoredDraggableState constructor
        val state = remember {
            AnchoredDraggableState(
                initialValue = DragValue.Settled
            )
        }

        SideEffect {
            state.updateAnchors(
                DraggableAnchors {
                    DragValue.Settled at 0f
                    DragValue.Revealed at -actionWidthPx
                }
            )
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(WarningYellow)
        ) {
            // Background Action
            Box(
                modifier = Modifier
                    .width(actionWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .clickable {
                        scope.launch {
                            state.animateTo(DragValue.Settled)
                        }
                        onRemove()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "Remove",
                    tint = Color.White
                )
            }

            // Foreground Content
            ScoreListItemContent(
                scoreWithDetails = scoreWithDetails,
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        val offset = try {
                            state.requireOffset()
                        } catch (e: IllegalStateException) {
                            0f
                        }
                        IntOffset(x = offset.roundToInt(), y = 0)
                    }
                    .anchoredDraggable(
                        state = state,
                        orientation = Orientation.Horizontal,
                        flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                            state = state,
                            positionalThreshold = { distance -> distance * 0.5f },
                            animationSpec = tween()
                        )
                    )
                    .background(MaterialTheme.colorScheme.surface),
                isSelected = isSelected,
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                onClick = {
                    if (state.currentValue == DragValue.Revealed) {
                        scope.launch {
                            state.animateTo(DragValue.Settled)
                        }
                    } else {
                        onClick()
                    }
                },
                onShowInfo = { isOpenScoreDetailDialog = true }
            )
        }
    }

    if (isOpenScoreDetailDialog) {
        ScoreDetailDialog(
            scoreWithDetails = scoreWithDetails,
            onDismissRequest = { isOpenScoreDetailDialog = false },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScoreListItemPreview() {
    val score = Score(
        id = 1,
        title = "Symphony No. 5",
        filePath = ""
    )
    val composer = Composer(
        id = 1,
        firstName = "Ludwig van",
        lastName = "Beethoven"
    )
    val scoreWithDetails = ScoreWithDetails(
        score = score,
        composer = composer,
        genres = emptyList(),
        instruments = emptyList(),
        tags = emptyList()
    )

    MaterialTheme {
        ScoreListItem(
            scoreWithDetails = scoreWithDetails,
            onRemove = {},
            onClick = {}
        )
    }
}

@Composable
private fun ScoreListItemContent(
    scoreWithDetails: ScoreWithDetails,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
    onShowInfo: () -> Unit = {},
) {
    val score = scoreWithDetails.score

    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                Color.Transparent
        ),
        leadingContent = leadingContent,
        headlineContent = {
            Text(
                text = score.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        supportingContent = {
            val composer = scoreWithDetails.composer
            val fullName = "${composer?.firstName} ${composer?.lastName}".trim()
            Text(
                text = fullName,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = trailingContent ?: {
            IconButton(
                onClick = onShowInfo
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = stringResource(R.string.search_scores_info),
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        },
    )
}
