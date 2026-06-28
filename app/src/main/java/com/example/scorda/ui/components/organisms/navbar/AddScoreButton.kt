package com.example.scorda.ui.components.organisms.navbar

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.scorda.ui.viewmodel.ScoreViewModel

@Composable
fun AddScoreButton(
    viewModel: ScoreViewModel
) {
    val scores by viewModel.scores.collectAsStateWithLifecycle()

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let(viewModel::onDocumentPicked)
    }

    IconButton(onClick = { pickerLauncher.launch(arrayOf("application/pdf")) }) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add scores"
        )
    }
}