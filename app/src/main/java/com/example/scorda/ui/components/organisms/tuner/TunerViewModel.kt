package com.example.scorda.ui.components.organisms.tuner

import android.annotation.SuppressLint
import android.app.Application
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorda.audio.tuner.AubioPitchDetector
import com.example.scorda.domain.model.drone.Pitch
import com.example.scorda.domain.model.tuner.TunerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.igorski.mwengine.MWEngine
import kotlin.math.log2
import kotlin.math.roundToInt

val tunerResultInitialValue: TunerResult = TunerResult(
    pitch = Pitch.A,
    hertz = 0f,
    octave = 4,
    cents = 0,
    midi = 0f,
    confidence = 0f,
    hasSignal = false
)

data class TunerUiState(
    val tuningHz: Int = 440,
    val hasPermission: Boolean = false,
    val isListening: Boolean = false,
    val tunerResult: TunerResult = tunerResultInitialValue
)

class TunerViewModel(application: Application) : AndroidViewModel(application) {

    private val _tuningHz = MutableStateFlow<Int>(440)
    private val _hasPermission = MutableStateFlow<Boolean>(false)
    private val _isListening = MutableStateFlow<Boolean>(false)
    private val _tunerResult = MutableStateFlow<TunerResult>(tunerResultInitialValue)
    private val pitchDetector = AubioPitchDetector()
    private var analysisJob: Job? = null

    private val sampleRate: Int
    private val bufferSize = 4096
    private val hopSize = 2048

    val uiState: StateFlow<TunerUiState> = combine(
        _tuningHz,
        _hasPermission,
        _isListening,
        _tunerResult
    ) { tuningHz, hasPermission, isListening, tunerResult ->
        TunerUiState(
            tuningHz = tuningHz,
            hasPermission = hasPermission,
            isListening = isListening,
            tunerResult = tunerResult
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TunerUiState()
    )

    init {
        val context = application.applicationContext
        sampleRate = MWEngine.getRecommendedSampleRate(context)
        Log.d("TunerViewModel", "Using recommended sample rate: $sampleRate")

        pitchDetector.initialize(sampleRate, bufferSize, hopSize)
        pitchDetector.setSilence(-60.0f) // Loosened from -45
        pitchDetector.setTolerance(0.15f)
    }

    fun setPermissionGranted(granted: Boolean) {
        _hasPermission.value = granted
        if (granted) {
            startTuning()
        } else {
            stopTuning()
        }
    }

    fun setTuningHz(hz: Int) {
        _tuningHz.value = hz
    }

    @SuppressLint("MissingPermission")
    private fun startTuning() {
        if (analysisJob != null) return

        analysisJob = viewModelScope.launch(Dispatchers.IO) {
            val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                audioEncoding
            )

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, // Changed from VOICE_RECOGNITION
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                audioEncoding,
                minBufferSize.coerceAtLeast(bufferSize * 2)
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("TunerViewModel", "AudioRecord initialization failed")
                return@launch
            }

            audioRecord.startRecording()
            _isListening.value = true

            val shortBuffer = ShortArray(hopSize)
            val floatBuffer = FloatArray(hopSize)

            var silenceCounter = 0

            try {
                while (isActive) {
                    var totalRead = 0
                    while (totalRead < hopSize && isActive) {
                        val read = audioRecord.read(shortBuffer, totalRead, hopSize - totalRead)
                        if (read < 0) break
                        totalRead += read
                    }

                    if (totalRead == hopSize) {
                        for (i in 0 until hopSize) {
                            floatBuffer[i] = shortBuffer[i] / 32768.0f
                        }

                        // 1. Calculate RMS / Volume Gate BEFORE filtering
                        val rms = calculateRms(floatBuffer)
                        val db = 20 * Math.log10(rms.coerceAtLeast(1e-10))

                        // 2. High Pass Filter (80Hz cutoff approx)
                        applyHighPassFilter(floatBuffer)

                        if (db > -80.0) { // Much more permissive gate
                            val result = pitchDetector.process(floatBuffer)
                            if (result != null) {
                                val detectedHz = result[0]
                                val confidence = result[1]

                                if (detectedHz > 20f && confidence > 0.1f) { // Very permissive confidence
                                    silenceCounter = 0
                                    val detectedHzDouble = detectedHz.toDouble()
                                    val tuningHzDouble = uiState.value.tuningHz.toDouble()

                                    val midi = 69.0 + 12.0 * log2(detectedHzDouble / tuningHzDouble)
                                    val roundedMidi = midi.roundToInt()
                                    val pitch = Pitch.fromSemitones(roundedMidi % 12)
                                    val octave = (roundedMidi / 12) - 1
                                    val cents = ((midi - roundedMidi) * 100).roundToInt()

                                    val tunerResult = TunerResult(
                                        pitch = pitch,
                                        hertz = detectedHz,
                                        octave = octave,
                                        cents = cents,
                                        midi = midi.toFloat(),
                                        confidence = confidence,
                                        hasSignal = true
                                    )
                                    _tunerResult.value = tunerResult
                                } else {
                                    silenceCounter++
                                }
                            } else {
                                silenceCounter++
                            }
                        } else {
                            silenceCounter++
                        }

                        // If silent/uncertain for ~2 seconds (approx 50 buffers at hop 2048/48k), reset
                        if (silenceCounter > 50) {
                            _tunerResult.value = tunerResultInitialValue
                        }
                    }
                }
            } finally {
                try {
                    audioRecord.stop()
                } catch (e: Exception) {
                    Log.e("TunerViewModel", "Error stopping AudioRecord", e)
                }
                audioRecord.release()
                _isListening.value = false
            }
        }
    }

    private fun stopTuning() {
        analysisJob?.cancel()
        analysisJob = null
    }

    private var hpPrevInput = 0f
    private var hpPrevOutput = 0f

    private fun applyHighPassFilter(buffer: FloatArray) {
        // Simple one-pole high pass filter (cutoff ~80Hz at 48kHz)
        val alpha = 0.99f
        for (i in buffer.indices) {
            val input = buffer[i]
            val output = alpha * (hpPrevOutput + input - hpPrevInput)
            hpPrevInput = input
            hpPrevOutput = output
            buffer[i] = output
        }
    }

    private fun calculateRms(buffer: FloatArray): Double {
        var sum = 0.0
        for (sample in buffer) {
            sum += sample * sample
        }
        return Math.sqrt(sum / buffer.size)
    }

    override fun onCleared() {
        stopTuning()
        pitchDetector.release()
    }
}
