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
    confidence = 0f
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
        pitchDetector.setSilence(-70.0f)
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
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
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

            try {
                while (isActive) {
                    var totalRead = 0
                    // Ensure we read exactly hopSize samples for the detector
                    while (totalRead < hopSize && isActive) {
                        val read = audioRecord.read(shortBuffer, totalRead, hopSize - totalRead)
                        if (read < 0) break
                        totalRead += read
                    }

                    if (totalRead == hopSize) {
                        for (i in 0 until hopSize) {
                            floatBuffer[i] = shortBuffer[i] / 32768.0f
                        }

                        val result = pitchDetector.process(floatBuffer)
                        if (result != null) {
                            val detectedHz = result[0]
                            val confidence = result[1]

                            if (confidence > 0.6f && detectedHz > 20f) {
                                val detectedHzDouble = detectedHz.toDouble()
                                val tuningHzDouble = uiState.value.tuningHz.toDouble()

                                // 1. Calculate MIDI (floating point) where 69.0 is A4
                                val midi = 69.0 + 12.0 * log2(detectedHzDouble / tuningHzDouble)

                                // 2. Find nearest semitone
                                val roundedMidi = midi.roundToInt()

                                // 3. Extract Pitch and Octave
                                val pitch = Pitch.fromSemitones(roundedMidi % 12)
                                val octave = (roundedMidi / 12) - 1

                                // 4. Calculate Cents offset
                                val cents = ((midi - roundedMidi) * 100).roundToInt()

                                Log.d(
                                    "TunerViewModel",
                                    "Hz: $detectedHz, MIDI: $midi, Cents: $cents, Conf: $confidence"
                                )

                                val tunerResult = TunerResult(
                                    pitch = pitch,
                                    hertz = detectedHz,
                                    octave = octave,
                                    cents = cents,
                                    midi = midi.toFloat(),
                                    confidence = confidence
                                )
                                _tunerResult.value = tunerResult
                            }
                        }
                    } else if (totalRead < 0) {
                        break
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

    override fun onCleared() {
        stopTuning()
        pitchDetector.release()
    }
}
