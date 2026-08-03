package com.example.scorda.ui.components.organisms.tuner

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorda.audio.tuner.AubioPitchDetector
import com.example.scorda.domain.model.tuner.TunerResult
import com.example.scorda.domain.model.tuner.TunerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log2
import kotlin.math.pow

class TunerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TunerState())
    val uiState = _uiState.asStateFlow()

    private val pitchDetector = AubioPitchDetector()
    private var analysisJob: Job? = null

    private val sampleRate = 44100
    private val bufferSize = 4096 // Increased for better low-freq detection
    private val hopSize = 2048

    init {
        pitchDetector.initialize(sampleRate, bufferSize, hopSize)
        pitchDetector.setSilence(-70.0f) // Allow quieter signals
        pitchDetector.setTolerance(0.15f) // Standard YIN tolerance
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted) {
            startTuning()
        } else {
            stopTuning()
        }
    }

    fun setTuningHz(hz: Int) {
        _uiState.update { it.copy(tuningHz = hz) }
    }

    @SuppressLint("MissingPermission")
    private fun startTuning() {
        if (analysisJob != null) return

        analysisJob = viewModelScope.launch(Dispatchers.IO) {
            // Using PCM_16BIT for better hardware compatibility and stability
            val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                audioEncoding
            )

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION, // Cleaner audio source
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                audioEncoding,
                minBufferSize.coerceAtLeast(hopSize * 2)
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("TunerViewModel", "AudioRecord initialization failed")
                return@launch
            }

            audioRecord.startRecording()
            _uiState.update { it.copy(isListening = true) }

            val shortBuffer = ShortArray(hopSize)
            val floatBuffer = FloatArray(hopSize)

            try {
                while (isActive) {
                    val read = audioRecord.read(shortBuffer, 0, hopSize)
                    if (read > 0) {
                        // Convert Short to Float (-1.0 to 1.0)
                        for (i in 0 until read) {
                            floatBuffer[i] = shortBuffer[i] / 32768.0f
                        }

                        val result = pitchDetector.process(floatBuffer)
                        if (result != null) {
                            val midi = result[0]
                            val confidence = result[1]

                            // Threshold confidence to filter out noise
                            if (confidence > 0.6f && midi > 0) {
                                // Convert Aubio MIDI (reference 440Hz) to Frequency
                                val freq = 440.0 * 2.0.pow((midi - 69.0) / 12.0)
                                // Convert Frequency to Adjusted MIDI (reference tuningHz)
                                val adjustedMidi =
                                    (69.0 + 12.0 * log2(freq / _uiState.value.tuningHz)).toFloat()

                                val tunerResult = TunerResult.fromMidi(adjustedMidi, confidence)
                                _uiState.update { it.copy(tunerResult = tunerResult) }
                            }
                        }
                    } else if (read < 0) {
                        Log.e("TunerViewModel", "AudioRecord read error: $read")
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
                _uiState.update { it.copy(isListening = false) }
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
