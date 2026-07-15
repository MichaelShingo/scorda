package com.example.scorda.ui.components.organisms.setlists

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.scorda.data.database.entities.Setlist

@Composable
fun SetlistDialog(
    setlist: Setlist? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(setlist?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = if (setlist == null) "New Setlist" else "Edit Setlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
