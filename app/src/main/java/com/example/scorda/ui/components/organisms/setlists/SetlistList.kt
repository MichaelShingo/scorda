package com.example.scorda.ui.components.organisms.setlists

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.scorda.data.database.entities.Setlist

@Composable
fun SetlistList(
    setlists: List<Setlist>,
    onSetlistClick: (Setlist) -> Unit,
    onMoreClick: (Setlist) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(setlists) { setlist ->
            SetlistListItem(
                setlist = setlist,
                onSetlistClick = onSetlistClick,
                onMoreClick = onMoreClick
            )
        }
    }
}
