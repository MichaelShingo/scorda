package com.example.scorda.ui.components.organisms.navbar

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import com.example.scorda.ui.viewmodel.ScoreViewModel

@Composable
fun AddScoreButton(
    viewModel: ScoreViewModel
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let(viewModel::onDocumentPicked)
    }

    NavbarButton(
        imageVector = Icons.Rounded.Add,
        contentDescription = "Add scores",
        onClick = { pickerLauncher.launch(arrayOf("application/pdf")) }
    )
}