package com.example.scorda.ui.components.molecules.drone

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scorda.domain.model.drone.Pitch
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun PitchWheel(
    selectedPitch: Pitch,
    onPitchSelected: (Pitch) -> Unit,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pitches = Pitch.entries
    val angleStep = 360f / pitches.size

    val rotation = remember { Animatable(selectedPitch.semitonesFromC * -angleStep) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedPitch) {
        val targetRotation = selectedPitch.semitonesFromC * -angleStep
        var diff = targetRotation - (rotation.value % 360f)
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f
        rotation.animateTo(
            targetValue = rotation.value + diff,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // Outer container that respects the passed modifier (e.g., weight)
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Inner container that FORCES square aspect ratio
        Box(
            modifier = Modifier
                .fillMaxSize(0.95f)
                .aspectRatio(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Rotatable Part
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val pos = change.position
                                val prevPos = pos - dragAmount

                                val angle =
                                    atan2(pos.y - center.y, pos.x - center.x) * (180 / PI).toFloat()
                                val prevAngle = atan2(
                                    prevPos.y - center.y,
                                    prevPos.x - center.x
                                ) * (180 / PI).toFloat()

                                var delta = angle - prevAngle
                                if (delta > 180) delta -= 360
                                if (delta < -180) delta += 360

                                scope.launch {
                                    rotation.snapTo(rotation.value + delta)
                                }
                            },
                            onDragEnd = {
                                val finalRotation = rotation.value
                                val snappedSemitone =
                                    ((-finalRotation / angleStep).roundToInt() % 12 + 12) % 12
                                onPitchSelected(Pitch.fromSemitones(snappedSemitone))
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background Circle
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    border = BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {}

                // Pitch Labels
                pitches.forEachIndexed { index, pitch ->
                    val pitchAngle = index * angleStep
                    val currentRotation = rotation.value

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = pitchAngle + currentRotation
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = pitch.displayNameDrone,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .graphicsLayer {
                                    rotationZ = -(pitchAngle + currentRotation)
                                },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (pitch == selectedPitch) FontWeight.Bold else FontWeight.Normal,
                                fontSize = if (pitch == selectedPitch) 22.sp else 18.sp
                            ),
                            color = if (pitch == selectedPitch) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }

            // Static Center Button
            Surface(
                modifier = Modifier
                    .fillMaxSize(0.35f)
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable { onTogglePlay() },
                shape = CircleShape,
                color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        modifier = Modifier.fillMaxSize(0.6f),
                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Selection Needle
            val indicatorColor = MaterialTheme.colorScheme.primary
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-16).dp)
                    .size(24.dp)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, indicatorColor)
            }
        }
    }
}
