package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.Bitmap
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PagePreviewSlider(
    pdfRendererCore: PdfRendererCore,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentPage) {
        listState.animateScrollToItem(currentPage)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
    ) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(pdfRendererCore.pageCount) { index ->
                PageThumbnail(
                    pdfRendererCore = pdfRendererCore,
                    pageIndex = index,
                    isSelected = index == currentPage,
                    onClick = { onPageSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun PageThumbnail(
    pdfRendererCore: PdfRendererCore,
    pageIndex: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val thumbnail by produceState<Bitmap?>(initialValue = null, pdfRendererCore, pageIndex) {
        value = withContext(Dispatchers.IO) {
            try {
                // Request a small bitmap for thumbnail, maintaining aspect ratio
                val dimensions = pdfRendererCore.getPageDimensions(pageIndex) ?: (100 to 140)
                val ratio = dimensions.first.toFloat() / dimensions.second.toFloat()
                
                val targetWidth = 100
                val targetHeight = (targetWidth / ratio).toInt()
                
                pdfRendererCore.renderPage(pageIndex, targetWidth, targetHeight)
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(60.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f / 1.414f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = "Page ${pageIndex + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Text(
            text = (pageIndex + 1).toString(),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
