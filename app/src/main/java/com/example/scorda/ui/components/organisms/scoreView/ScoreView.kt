package com.example.scorda.ui.components.organisms.scoreView

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.pdf.PdfDocument
import androidx.pdf.SandboxedPdfLoader
import androidx.pdf.compose.PdfViewer
import androidx.pdf.compose.PdfViewerState
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import java.io.File

@Composable
fun ScoreView() {
    val scoreViewModel = LocalScoreViewModel.current
    val uiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val selectedScore = uiState.selectedScore

    val context = LocalContext.current
    val pdfLoader = remember { SandboxedPdfLoader(context) }
    val pdfViewerState = remember { PdfViewerState() }

    val pdfDocument by produceState<PdfDocument?>(initialValue = null, selectedScore) {
        val path = selectedScore?.score?.filePath
        value = if (path != null) {
            try {
                pdfLoader.openDocument(Uri.fromFile(File(path)))
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (selectedScore != null) {
            val doc = pdfDocument
            if (doc != null) {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    PdfViewer(
                        modifier = Modifier.fillMaxSize(),
                        pdfDocument = doc,
                        state = pdfViewerState,
                        contentPadding = paddingValues
                    )
                }
            } else {
                CircularProgressIndicator()
            }
        } else {
            Text("Welcome to Scorda. Get started by importing a score.")
        }
    }
}
