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

class TunerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TunerState())
    val uiState = _uiState.asStateFlow()

    private val pitchDetector = AubioPitchDetector()
    private var analysisJob: Job? = null

    // Most modern Android devices are natively 48kHz. 
    // Using 44.1kHz often triggers a resampler that introduces pitch error.
    private val sampleRate = 48000
    private val bufferSize = 4096
    private val hopSize = 2048

    init {
        pitchDetector.initialize(sampleRate, bufferSize, hopSize)
        pitchDetector.setSilence(-70.0f)
        pitchDetector.setTolerance(0.15f)
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
            _uiState.update { it.copy(isListening = true) }

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
                                // Calculate high-precision MIDI value from detected frequency
                                // 69 is MIDI for A4. 
                                val midi = (69.0 + 12.0 * log2(detectedHz.toDouble() / _uiState.value.tuningHz)).toFloat()
                                
                                val tunerResult = TunerResult.fromMidi(midi, confidence)
                                _uiState.update { it.copy(tunerResult = tunerResult) }
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
