package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration

/**
 * A modifier that handles score interactions (page turns, navbar toggle)
 * without blocking multi-touch gestures like zoom.
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
                var isMultiTouch = false
                var initialDown: PointerInputChange? = null

                // 1. Peek at the first finger down
                val down = awaitPointerEvent(pass = PointerEventPass.Initial).changes.firstOrNull()
                if (down != null) {
                    initialDown = down
                }

                // 2. Observe the gesture lifecycle without consuming yet
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                    // Detect if more than one finger is touching the screen
                    if (event.changes.size > 1) {
                        isMultiTouch = true
                    }

                    // Check for completion (all fingers up)
                    val allUp = event.changes.all { it.changedToUp() }

                    if (allUp) {
                        val up = event.changes.firstOrNull()
                        if (!isMultiTouch && up != null && initialDown != null) {
                            val duration = up.uptimeMillis - initialDown.uptimeMillis
                            val distance = (up.position - initialDown.position).getDistance()

                            // If it's a quick tap without much movement
                            if (duration < viewConfiguration.doubleTapTimeoutMillis &&
                                distance < viewConfiguration.touchSlop
                            ) {

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
                                // CONSUME the up event so children (like PDF viewer) don't trigger their own taps
                                event.changes.forEach { it.consume() }
                            }
                        }
                        break
                    }
                }
            }
        }
    )
}
