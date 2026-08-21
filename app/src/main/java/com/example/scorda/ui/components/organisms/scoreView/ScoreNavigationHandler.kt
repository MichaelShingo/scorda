package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomableState

/**
 * A consolidated modifier that handles all score interactions:
 * 1. Region-based touch taps (Previous, Next, Toggle Navbar)
 * 2. Hardware pedal / Keyboard events (Next, Previous)
 * 3. Focus management for key events
 * 4. Zoom-aware paging (pans up before turning page if zoomed)
 */
@Composable
fun Modifier.scoreNavigationHandler(
    enabled: Boolean,
    currentPageIndex: Int,
    pageCount: Int,
    viewportHeight: Dp,
    zoomableState: ZoomableState?,
    focusRequester: FocusRequester,
    onPageChange: (Int) -> Unit,
    onNextScore: () -> Unit,
    onPreviousScore: () -> Unit,
    onToggleNavbar: () -> Unit,
): Modifier {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val viewportHeightPx = with(density) { viewportHeight.toPx() }

    val handlePrevious = {
        scope.launch {
            if (zoomableState != null) {
                val transform = zoomableState.contentTransformation
                // If zoomed and not at top, pan up first
                if (transform.scale.scaleY > 1.05f && transform.offset.y < -10f) {
                    zoomableState.panBy(Offset(0f, viewportHeightPx * 0.8f))
                } else {
                    if (currentPageIndex > 0) {
                        onPageChange(currentPageIndex - 1)
                    } else {
                        onPreviousScore()
                    }
                }
            } else {
                if (currentPageIndex > 0) {
                    onPageChange(currentPageIndex - 1)
                } else {
                    onPreviousScore()
                }
            }
        }
    }

    val handleNext = {
        scope.launch {
            if (currentPageIndex < pageCount - 1) {
                onPageChange(currentPageIndex + 1)
            } else {
                onNextScore()
            }
        }
    }

    return this.then(
        Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (!enabled || keyEvent.type != KeyEventType.KeyDown) {
                    return@onKeyEvent false
                }

                when (keyEvent.key) {
                    Key.PageDown,
                    Key.DirectionRight,
                    Key.DirectionDown,
                    Key.Spacebar,
                    Key.Enter,
                    Key.Tab -> {
                        handleNext()
                        true
                    }

                    Key.PageUp,
                    Key.DirectionLeft,
                    Key.DirectionUp -> {
                        handlePrevious()
                        true
                    }

                    else -> false
                }
            }
            .pointerInput(enabled, currentPageIndex, pageCount) {
                detectTapGestures { offset ->
                    // Always try to regain focus on tap
                    focusRequester.requestFocus()

                    if (!enabled) return@detectTapGestures

                    val x = offset.x
                    val y = offset.y
                    val width = size.width
                    val height = size.height

                    val column1Width = width * 0.25f
                    val column2Width = width * 0.50f
                    val topRegionHeight = height * 0.15f

                    when {
                        // Left column
                        x < column1Width -> {
                            if (y < topRegionHeight) onToggleNavbar() else handlePrevious()
                        }
                        // Middle column
                        x < (column1Width + column2Width) -> {
                            onToggleNavbar()
                        }
                        // Right column
                        else -> {
                            if (y < topRegionHeight) onToggleNavbar() else handleNext()
                        }
                    }
                }
            }
    )
}
