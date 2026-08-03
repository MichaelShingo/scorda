package com.example.scorda.ui.components.organisms.tuner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.ui.components.atoms.VerticalNumberSelector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Tuner() {
    val viewModel: TunerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.setPermissionGranted(isGranted)
        }
    )

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.setPermissionGranted(true)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    TunerContent(
        tuningHz = uiState.tuningHz,
        tunerResult = uiState.tunerResult,
        onTuningHzChange = viewModel::setTuningHz
    )
}

@Composable
fun TunerContent(
    tuningHz: Int,
    tunerResult: com.example.scorda.domain.model.tuner.TunerResult,
    onTuningHzChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Pitch Display
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = tunerResult.pitch.displayName,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 80.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tunerResult.octave.toString(),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Meter
        TunerMeter(
            cents = tunerResult.cents,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(2f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cents Indicator
        Text(
            text = if (tunerResult.cents >= 0) "+${tunerResult.cents}" else tunerResult.cents.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
            color = if (Math.abs(tunerResult.cents) < 5) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        // Hz Selector
        VerticalNumberSelector(
            value = tuningHz,
            onValueChange = onTuningHzChange,
            range = 430..450,
            label = "A (Hz)"
        )
    }
}

@Composable
fun TunerMeter(
    cents: Int,
    modifier: Modifier = Modifier
) {
    val angle = remember { Animatable(0f) }
    val targetAngle = (cents.coerceIn(-50, 50) * 1.8f) // -90 to 90

    LaunchedEffect(targetAngle) {
        angle.animateTo(
            targetValue = targetAngle,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val greenColor = Color(0xFF4CAF50)

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height
        val radius = size.width / 2f

        // Draw scale
        for (i in -5..5) {
            val tickAngle = i * 18f // 18 degrees per 10 cents
            val rad = (tickAngle - 90f) * PI.toFloat() / 180f
            val startRadius = radius * 0.9f
            val endRadius = radius

            val start = Offset(
                centerX + cos(rad) * startRadius,
                centerY + sin(rad) * startRadius
            )
            val end = Offset(
                centerX + cos(rad) * endRadius,
                centerY + sin(rad) * endRadius
            )

            drawCircle(
                color = if (i == 0) greenColor else onSurfaceColor.copy(alpha = 0.5f),
                radius = 2.dp.toPx(),
                center = end
            )
        }

        // Draw Arc background
        drawArc(
            color = onSurfaceColor.copy(alpha = 0.1f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Needle
        val needleRad = (angle.value - 90f) * PI.toFloat() / 180f
        val needleLength = radius * 0.85f
        val needleEnd = Offset(
            centerX + cos(needleRad) * needleLength,
            centerY + sin(needleRad) * needleLength
        )

        drawLine(
            color = if (Math.abs(cents) < 5) greenColor else primaryColor,
            start = Offset(centerX, centerY),
            end = needleEnd,
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center dot
        drawCircle(
            color = primaryColor,
            radius = 6.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}
