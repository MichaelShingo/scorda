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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.ui.components.atoms.VerticalNumberSelector
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Tuner() {
    val viewModel: TunerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Pitch Display
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = if (tunerResult.hasSignal) tunerResult.pitch.displayNameSharps else "--",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 64.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (tunerResult.hasSignal) tunerResult.octave.toString() else "",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meter
        TunerMeter(
            cents = tunerResult.cents,
            hasSignal = tunerResult.hasSignal,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(2f)
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
    hasSignal: Boolean,
    modifier: Modifier = Modifier
) {
    val angle = remember { Animatable(0f) }
    val targetAngle = if (hasSignal) (cents.coerceIn(-50, 50) * 1.8f) else 0f

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
    val centsSign = if (cents > 0) "+" else ""

    Text(
        text = if (hasSignal) "${centsSign}${cents}" else "",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )

    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height
        val radius = size.width / 2f


        // Draw Needle
        val needleRad = (angle.value - 90f) * PI.toFloat() / 180f
        val needleLength = radius * 0.85f
        val needleEnd = Offset(
            centerX + cos(needleRad) * needleLength,
            centerY + sin(needleRad) * needleLength
        )

        drawLine(
            color = if (abs(cents) < 5) colorPrimary else colorSecondary,
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

@Preview(showBackground = true)
@Composable
fun TunerMeterPreview() {
    TunerMeter(
        cents = 25,
        hasSignal = true,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .padding(16.dp)
    )
}
