package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

/**
 * Hosts PDF pages with custom paging functionality and animations
 */
@Composable
fun ScoreHost(
    currentPageIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    AnimatedContent(
        targetState = currentPageIndex,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            // Determine direction: Next (index increases) vs Previous (index decreases)
            val isNext = targetState > initialState

            val enterTransition = if (isNext) {
                // Slide in from right (15% width) + Fade In
                slideIn(
                    initialOffset = { IntOffset(it.width / 6, 0) },
                    animationSpec = tween(250)
                ) + fadeIn(animationSpec = tween(250))
            } else {
                // Slide in from left (15% width) + Fade In
                slideIn(
                    initialOffset = { IntOffset(-it.width / 6, 0) },
                    animationSpec = tween(250)
                ) + fadeIn(animationSpec = tween(250))
            }

            val exitTransition = if (isNext) {
                // Slide out to left + Fade Out
                slideOut(
                    targetOffset = { IntOffset(-it.width / 6, 0) },
                    animationSpec = tween(250)
                ) + fadeOut(animationSpec = tween(250))
            } else {
                // Slide out to right + Fade Out
                slideOut(
                    targetOffset = { IntOffset(it.width / 6, 0) },
                    animationSpec = tween(250)
                ) + fadeOut(animationSpec = tween(250))
            }

            enterTransition togetherWith exitTransition
        },
        label = "ScorePageTransition"
    ) { targetIndex ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(targetIndex)
        }
    }
}
