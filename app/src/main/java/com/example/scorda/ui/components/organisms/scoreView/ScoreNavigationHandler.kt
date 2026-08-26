package com.example.scorda.ui.components.organisms.scoreView

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomableState

/**
 * A consolidated modifier that handles all score interactions:
 * 1. Region-based touch taps (Previous, Next, Toggle Navbar)
 * 2. Hardware pedal / Keyboard events (Next, Previous)
 * 3. Focus management for key events
 * 4. Panning on page-turn in landscape mode
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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val viewportHeightPx = with(density) { viewportHeight.toPx() }
    val viewConfiguration = LocalViewConfiguration.current

    val handlePrevious = {
        scope.launch {
            val transform = zoomableState?.contentTransformation

            if (isLandscape && transform != null && transform.isSpecified && transform.offset.y < -10f) {
                zoomableState.panBy(Offset(0f, viewportHeightPx * 0.8f))
            } else if (currentPageIndex > 0) {
                onPageChange(currentPageIndex - 1)
            } else {
                onPreviousScore()
            }
        }
    }

    val handleNext = {
        scope.launch {
            val transform = zoomableState?.contentTransformation

            if (isLandscape && transform != null && transform.isSpecified) {
                @Suppress("DEPRECATION")
                val scaledContentHeight = transform.contentSize.height * transform.scale.scaleY
                val maxScrollOffset = scaledContentHeight - viewportHeightPx

                if (maxScrollOffset > 0f && transform.offset.y > (-maxScrollOffset + 10f)) {
                    zoomableState.panBy(Offset(0f, -viewportHeightPx * 0.8f))
                } else if (currentPageIndex < pageCount - 1) {
                    onPageChange(currentPageIndex + 1)
                } else {
                    onNextScore()
                }
            } else if (currentPageIndex < pageCount - 1) {
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
                awaitEachGesture {
                    // Peek at events before children (Initial pass)
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)

                    // Always try to regain focus on interaction
                    focusRequester.requestFocus()

                    if (!enabled) return@awaitEachGesture

                    val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)

                    if (up != null) {
                        val distance = (up.position - down.position).getDistance()

                        // Only trigger if it was a clean tap (minimal movement)
                        if (distance < viewConfiguration.touchSlop) {
                            val x = up.position.x
                            val y = up.position.y
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
                            // Consume to prevent further processing as a tap by children
                            up.consume()
                        }
                    }
                }
            }
    )
}
