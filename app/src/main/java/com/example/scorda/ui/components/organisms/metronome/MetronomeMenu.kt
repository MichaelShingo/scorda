package com.example.scorda.ui.components.organisms.metronome

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun MetronomeMenu(
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Rounded.Menu, contentDescription = "More options")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Save Preset") },
            onClick = { expanded = false }
        )
        DropdownMenuItem(
            text = { Text("Save setting to score") },
            onClick = { expanded = false }
        )
        DropdownMenuItem(
            text = { Text("Load score preset") },
            onClick = { expanded = false }
        )
        DropdownMenuItem(
            text = { Text("Fullscreen") },
            onClick = { expanded = false }
        )
    }
}
