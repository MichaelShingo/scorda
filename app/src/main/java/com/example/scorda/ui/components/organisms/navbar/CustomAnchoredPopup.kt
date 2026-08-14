package com.example.scorda.ui.components.organisms.navbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

sealed interface CustomAnchoredPopupSize {
    val width: Dp
    val maxHeight: Dp

    data object Small : CustomAnchoredPopupSize {
        override val width = 200.dp
        override val maxHeight = 280.dp
    }

    data object Medium : CustomAnchoredPopupSize {
        override val width = 300.dp
        override val maxHeight = 400.dp
    }

    data object Large : CustomAnchoredPopupSize {
        override val width = 360.dp
        override val maxHeight = 480.dp
    }

    data class Custom(
        override val width: Dp,
        override val maxHeight: Dp
    ) : CustomAnchoredPopupSize
}

@Composable
fun AnchoredPopup(
    modifier: Modifier = Modifier,
    size: CustomAnchoredPopupSize = CustomAnchoredPopupSize.Medium,
    anchor: @Composable (onOpen: () -> Unit, isExpanded: Boolean) -> Unit,
    content: @Composable (onDismiss: () -> Unit) -> Unit,
) {
    var isPopupVisible by remember { mutableStateOf(false) }
    val expandedState = remember { MutableTransitionState(false) }

    LaunchedEffect(isPopupVisible) {
        expandedState.targetState = isPopupVisible
    }

    val density = LocalDensity.current
    var caretXOffset by remember { mutableStateOf(size.width / 2) }
    val caretWidth = 16.dp

    val popupPositionProvider = remember(density, size.width) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val idealX = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
                val x = idealX.coerceIn(0, windowSize.width - popupContentSize.width)
                val y = anchorBounds.top + with(density) { 56.dp.roundToPx() }
                val anchorCenterX = anchorBounds.left + anchorBounds.width / 2
                val minCaretX = with(density) { (caretWidth / 2 + 12.dp).roundToPx() }
                val maxCaretX = with(density) { (size.width - caretWidth / 2 - 12.dp).roundToPx() }
                val relativeCaretX = (anchorCenterX - x).coerceIn(minCaretX, maxCaretX)
                caretXOffset = with(density) { relativeCaretX.toDp() }
                return IntOffset(x, y)
            }
        }
    }

    Box(modifier = modifier) {
        anchor({ isPopupVisible = true }, isPopupVisible)

        if (expandedState.currentState || expandedState.targetState) {
            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = { isPopupVisible = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = false
                )
            ) {
                AnimatedVisibility(
                    visibleState = expandedState,
                    enter = fadeIn(animationSpec = tween(200)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(200),
                        transformOrigin = TransformOrigin(
                            caretXOffset.value / size.width.value,
                            0f
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(150),
                        transformOrigin = TransformOrigin(
                            caretXOffset.value / size.width.value,
                            0f
                        )
                    )
                ) {
                    Column {
                        Caret(
                            modifier = Modifier
                                .offset(x = caretXOffset - (caretWidth / 2))
                                .size(width = caretWidth, height = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        )
                        Surface(
                            modifier = Modifier
                                .width(size.width)
                                .heightIn(min = size.width, max = size.maxHeight),
                            shape = MaterialTheme.shapes.extraLarge,
                            tonalElevation = 6.dp,
                            shadowElevation = 12.dp,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            content { isPopupVisible = false }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Caret(
    modifier: Modifier = Modifier,
    color: Color
) {
    val caretShape = GenericShape { size, _ ->
        moveTo(size.width / 2f, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    Box(
        modifier = modifier.background(color = color, shape = caretShape)
    )
}
