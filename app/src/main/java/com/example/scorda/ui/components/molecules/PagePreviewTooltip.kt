package com.example.scorda.ui.components.molecules

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scorda.util.PdfRendererCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PagePreviewTooltip(
    pdfRendererCore: PdfRendererCore,
    pageIndex: Int?,
    modifier: Modifier = Modifier
) {
    val actualPageIndex = pageIndex ?: 0
    val thumbnail by produceState<Bitmap?>(initialValue = null, pdfRendererCore, actualPageIndex) {
        value = withContext(Dispatchers.IO) {
            try {
                val dimensions = pdfRendererCore.getPageDimensions(actualPageIndex) ?: (100 to 140)
                val ratio = dimensions.first.toFloat() / dimensions.second.toFloat()

                // Low res for fast scrubbing/preview
                val targetWidth = 120
                val targetHeight = (targetWidth / ratio).toInt()

                pdfRendererCore.renderPage(actualPageIndex, targetWidth, targetHeight)
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(120.dp)
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1.414f)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        if (pageIndex != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Page ${pageIndex + 1}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
