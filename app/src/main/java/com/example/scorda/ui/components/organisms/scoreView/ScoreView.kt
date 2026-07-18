package com.example.scorda.ui.components.organisms.scoreView

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import java.io.File

@Composable
fun ScoreView() {
    val scoreViewModel = LocalScoreViewModel.current
    val uiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val selectedScore = uiState.selectedScore

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (selectedScore != null) {
            AndroidView(
                factory = { context ->
                    FragmentContainerView(context).apply {
                        id = android.view.View.generateViewId()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    val activity = view.context as? FragmentActivity
                    val fragmentManager = activity?.supportFragmentManager ?: return@AndroidView
                    val pdfUri = Uri.fromFile(File(selectedScore.score.filePath))

                    val existingFragment =
                        fragmentManager.findFragmentByTag("pdf_viewer") as? PdfViewerFragment

                    if (existingFragment == null) {
                        val pdfFragment = PdfViewerFragment()
                        fragmentManager.beginTransaction()
                            .replace(view.id, pdfFragment, "pdf_viewer")
                            .commit()

                        // Set the document after the fragment is attached
                        pdfFragment.documentUri = pdfUri
                    } else {
                        if (existingFragment.documentUri != pdfUri) {
                            existingFragment.documentUri = pdfUri
                        }
                    }
                }
            )
        } else {
            Text("Welcome to Scorda. Get started by importing a score.")
        }
    }
}