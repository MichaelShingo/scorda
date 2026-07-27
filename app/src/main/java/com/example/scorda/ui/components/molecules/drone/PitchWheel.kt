package com.example.scorda.ui.components.molecules.drone

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scorda.domain.model.drone.Pitch
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
    
    // Sync rotation if pitch changes externally
    LaunchedEffect(selectedPitch) {
        val targetRotation = selectedPitch.semitonesFromC * -angleStep
        // Find shortest path
        var diff = targetRotation - (rotation.value % 360f)
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f
        rotation.animateTo(
            targetValue = rotation.value + diff,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDrag = { change, dragAmount ->
                        val center = size.toCenter()
                        val pos = change.position
                        val prevPos = pos - dragAmount
                        
                        val angle = atan2(pos.y - center.y, pos.x - center.x) * (180 / PI).toFloat()
                        val prevAngle = atan2(prevPos.y - center.y, prevPos.x - center.x) * (180 / PI).toFloat()
                        
                        var delta = angle - prevAngle
                        if (delta > 180) delta -= 360
                        if (delta < -180) delta += 360
                        
                        scope.launch {
                            rotation.snapTo(rotation.value + delta)
                        }
                    },
                    onDragEnd = {
                        val finalRotation = rotation.value
                        val snappedSemitone = ((-finalRotation / angleStep).roundToInt() % 12 + 12) % 12
                        onPitchSelected(Pitch.fromSemitones(snappedSemitone))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer circle / Wheel background
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shadowElevation = 4.dp
        ) {}

        // Pitch markers
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
                    text = pitch.displayName,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .graphicsLayer {
                            // Keep text upright
                            rotationZ = -(pitchAngle + currentRotation)
                        },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (pitch == selectedPitch) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (pitch == selectedPitch) 20.sp else 16.sp
                    ),
                    color = if (pitch == selectedPitch) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        // Center Play/Stop button
        Surface(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .clickable { onTogglePlay() },
            shape = CircleShape,
            color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 8.dp
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
        
        // Selection Indicator at the top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
                .size(width = 4.dp, height = 20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

private fun androidx.compose.ui.unit.IntSize.toCenter() = androidx.compose.ui.geometry.Offset(width / 2f, height / 2f)
