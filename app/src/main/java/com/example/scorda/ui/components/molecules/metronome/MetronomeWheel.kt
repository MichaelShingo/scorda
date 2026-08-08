package com.example.scorda.ui.components.molecules.metronome

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .aspectRatio(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Beat Indicators (Rectangles around the circle)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val indicatorHeight = radius * 0.1f
                val angleStep = 360f / beatsPerMeasure
                val rectWidth = (2 * PI * radius / beatsPerMeasure).toFloat() * 0.8f

                for (i in 0 until beatsPerMeasure) {
                    val angle = i * angleStep - 90f // Start at 12:00
                    rotate(angle + 90f, pivot = center) {
                        drawRect(
                            color = if (i == currentBeat && isPlaying) {
                                activeColor
                            } else {
                                inactiveColor
                            },
                            topLeft = Offset(center.x - rectWidth / 2, center.y - radius),
                            size = Size(rectWidth, indicatorHeight)
                        )
                    }
                }
            }

            // Draggable Dial
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val pos = change.position
                                val prevPos = pos - dragAmount

                                val angle = atan2(pos.y - center.y, pos.x - center.x) * (180 / PI).toFloat()
                                val prevAngle = atan2(prevPos.y - center.y, prevPos.x - center.x) * (180 / PI).toFloat()

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {}
            }

            // Center Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize(0.6f)
            ) {
                IconButton(onClick = { onBpmChange((bpm - 1).coerceAtLeast(10)) }) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "-1 BPM")
                }

                Surface(
                    modifier = Modifier
                        .size(80.dp)
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
                            modifier = Modifier.size(48.dp),
                            tint = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                IconButton(onClick = { onBpmChange((bpm + 1).coerceAtMost(300)) }) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "+1 BPM")
                }
            }
        }
    }
}
