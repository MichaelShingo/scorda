package com.example.scorda.ui.components.organisms.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.example.scorda.ui.components.organisms.navbar.musictools.MusicTools

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navbar() {
    TopAppBar(
        title = {
            Text(
                "Waltz for Stark and Frieren"
            )
        },
        actions = {
//            IconButton(onClick = {}) {
//                Icon(
//                    imageVector = Icons.Rounded.MusicNote,
//                    contentDescription = "Browse all scores"
//                )
//            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add scores"
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.FormatListNumbered,
                    contentDescription = "Search Scores"
                )
            }

            // score details
            // global state setup
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.Gesture,
                    contentDescription = "Annotate"
                )
            }
            CustomAnchoredPopup(
                icon = Icons.Rounded.GraphicEq,
                contentDescription = "Metronome, Tuner, Drone",
            ) {
                MusicTools()
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search Scores"
                )
            }

            MoreDropdownMenu()
        },
    )
}