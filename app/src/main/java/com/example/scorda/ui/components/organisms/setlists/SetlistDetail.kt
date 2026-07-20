package com.example.scorda.ui.components.organisms.setlists

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.scorda.data.database.relations.SetlistWithDetails
import com.example.scorda.ui.components.molecules.scoreListItem.ScoreListItem

@Composable
fun SetlistDetail(
    setlistWithDetails: SetlistWithDetails,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(setlistWithDetails.scores) { scoreWithDetails ->
            ScoreListItem(
                scoreWithDetails = scoreWithDetails,
                modifier = Modifier,
                setlistId = setlistWithDetails.setlist.id
            )
        }
    }
}
