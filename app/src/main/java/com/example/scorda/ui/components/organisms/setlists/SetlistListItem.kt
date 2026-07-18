package com.example.scorda.ui.components.organisms.setlists

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.scorda.data.database.entities.Setlist

@Composable
fun SetlistListItem(
    setlist: Setlist,
    onSetlistClick: (Setlist) -> Unit,
    onMoreClick: (Setlist) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = setlist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            IconButton(onClick = { onMoreClick(setlist) }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More options"
                )
            }
        },
        modifier = modifier.clickable { onSetlistClick(setlist) }
    )
}
