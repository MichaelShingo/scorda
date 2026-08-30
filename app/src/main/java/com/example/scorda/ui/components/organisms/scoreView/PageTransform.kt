package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.ui.geometry.Offset

/**
 * Interface for coordinate mapping between screen space and PDF point space.
 */
interface PageTransform {
    val zoom: Float
    fun screenToPdf(offset: Offset): Offset?
    fun pdfToScreen(pdfOffset: Offset): Offset?
}
