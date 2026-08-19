package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration

/**
 * A simplified modifier that handles score interactions (page turns, navbar toggle).
 * It peeks at touches to detect region-based taps without blocking multi-touch gestures.
 */
@Composable
fun Modifier.scoreInteraction(
    onToggleNavbar: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
): Modifier {
    val viewConfiguration = LocalViewConfiguration.current

    return this.then(
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                // 1. Peek at the initial down
                val down = awaitFirstDown(pass = PointerEventPass.Initial)

                // 2. Wait for a clean up (cancels if second pointer goes down or significant move)
                val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)

                if (up != null) {
                    val distance = (up.position - down.position).getDistance()

                    // tap with minimal movement
                    if (distance < viewConfiguration.touchSlop) {
                        val x = up.position.x
                        val y = up.position.y
                        val width = size.width
                        val height = size.height

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
                        // prevent up event propagation
                        up.consume()
                    }
                }
            }
        }
    )
}
