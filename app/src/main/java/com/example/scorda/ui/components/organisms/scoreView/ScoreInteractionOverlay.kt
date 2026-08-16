package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
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
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Main)
                    val up = waitForUpOrCancellation(pass = PointerEventPass.Main)

                    if (up != null && up.id == down.id) {
                        // It's a tap. Check if it's a single pointer tap
                        if (currentEvent.changes.size == 1) {
                            val offset = up.position
                            val width = size.width
                            val height = size.height
                            val x = offset.x
                            val y = offset.y

                            val column1Width = width * 0.25f
                            val column2Width = width * 0.50f
                            val topRegionHeight = height * 0.15f

                            when {
                                x < column1Width -> {
                                    if (y < topRegionHeight) onToggleNavbar() else onPreviousPage()
                                }
                                x < (column1Width + column2Width) -> {
                                    onToggleNavbar()
                                }
                                else -> {
                                    if (y < topRegionHeight) onToggleNavbar() else onNextPage()
                                }
                            }
                            // Consume the tap so it doesn't trigger PDF selection/etc if we don't want it
                            up.consume()
                        }
                    }
                }
            }
    )
}
