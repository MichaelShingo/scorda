package com.example.scorda.ui.components.organisms.metronome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun MetronomeWheel(
    bpm: Int,
    onBpmChange: (Int) -> Unit,
    beatsPerMeasure: Int,
    currentBeat: Int,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val centerOffset = Offset(size.width / 2f, size.height / 2f)
                            val pos = change.position
                            val prevPos = pos - dragAmount

                            val angle = atan2(
                                pos.y - centerOffset.y,
                                pos.x - centerOffset.x
                            ) * (180 / PI).toFloat()
                            val prevAngle = atan2(
                                prevPos.y - centerOffset.y,
                                prevPos.x - centerOffset.x
                            ) * (180 / PI).toFloat()

                            var delta = angle - prevAngle
                            if (delta > 180) delta -= 360
                            if (delta < -180) delta += 360

                            val newBpm = (bpm + delta / 2f).roundToInt().coerceIn(10, 300)
                            if (newBpm != bpm) {
                                onBpmChange(newBpm)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val wheelSize = minOf(maxWidth, maxHeight)
            val playButtonSize = (wheelSize * 0.3f).coerceIn(64.dp, 100.dp)

            // Visual Dial Background
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                border = BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            ) {}

            // Beat Indicators (Curved arcs around the circle)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val outerRadius = size.minDimension / 2f
                val strokeWidth = outerRadius * 0.12f
                val pathRadius = outerRadius - (strokeWidth / 2f)

                val angleStep = 360f / beatsPerMeasure
                val sweepAngle = angleStep * 0.8f

                for (i in 0 until beatsPerMeasure) {
                    val startAngle = -90f - (sweepAngle / 2f) + (i * angleStep)
                    drawArc(
                        color = if (i == currentBeat && isPlaying) activeColor else inactiveColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - pathRadius, center.y - pathRadius),
                        size = Size(pathRadius * 2, pathRadius * 2),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }

            // Center Play Button
            Surface(
                modifier = Modifier
                    .size(playButtonSize)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable { onTogglePlay() },
                shape = CircleShape,
                color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        modifier = Modifier.size(playButtonSize * 0.6f),
                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
