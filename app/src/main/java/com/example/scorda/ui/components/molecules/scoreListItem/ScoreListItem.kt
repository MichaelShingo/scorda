package com.example.scorda.ui.components.molecules.scoreListItem

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.scorda.R
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.ui.components.organisms.scoreDetailDialog.ScoreDetailDialog

@Composable
fun ScoreListItem(
    scoreWithDetails: ScoreWithDetails,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val score = scoreWithDetails.score
    var isOpenScoreDetailDialog by remember { mutableStateOf<Boolean>(false) }

    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                Color.Transparent
        ),
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
        trailingContent = {
            IconButton(
                onClick = { isOpenScoreDetailDialog = true }

            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = stringResource(R.string.search_scores_info),
                    tint = MaterialTheme.colorScheme.outline,
                )
            }

        },
    )
    if (isOpenScoreDetailDialog) {
        ScoreDetailDialog(
            scoreWithDetails = scoreWithDetails,
            onDismissRequest = { isOpenScoreDetailDialog = false },
        )
    }
}