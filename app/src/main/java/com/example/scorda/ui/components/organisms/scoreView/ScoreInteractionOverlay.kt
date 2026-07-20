package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ScoreInteractionOverlay(
    onToggleNavbar: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width
                    val height = size.height
                    val x = offset.x
                    val y = offset.y

                    val column1Width = width * 0.25f
                    val column2Width = width * 0.50f
                    val topRegionHeight = height * 0.15f

                    when {
                        // Column 1: Left 25%
                        x < column1Width -> {
                            if (y < topRegionHeight) {
                                onToggleNavbar()
                            } else {
                                onPreviousPage()
                            }
                        }
                        // Column 2: Middle 50%
                        x < (column1Width + column2Width) -> {
                            onToggleNavbar()
                        }
                        // Column 3: Right 25%
                        else -> {
                            if (y < topRegionHeight) {
                                onToggleNavbar()
                            } else {
                                onNextPage()
                            }
                        }
                    }
                }
            }
    )
}
