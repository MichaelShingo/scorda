package com.example.scorda.ui.components.organisms.searchScores

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.scorda.R
import com.example.scorda.data.database.relations.ScoreWithDetails

@Composable
fun ScoreListItem(
    scoreWithDetails: ScoreWithDetails,
    modifier: Modifier,
) {
    val score = scoreWithDetails.score

    fun onClick() {
        // set open score
    }
    
    ListItem(
        headlineContent = {
            Text(
                text = score.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge

            )
        },
        supportingContent = {
            val composer = scoreWithDetails.composer
            val fullName = "${composer?.firstName} ${composer?.lastName}".trim()
            Text(
                text = fullName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = stringResource(R.string.search_scores_info),
                tint = MaterialTheme.colorScheme.outline,
            )
        },
        modifier = modifier.clickable { onClick() }
    )
}