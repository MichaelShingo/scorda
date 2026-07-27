package com.example.scorda.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

class AudioTrackEngine : AudioEngine {
    private var audioTrack: AudioTrack? = null
    private var isRunning = false
    private var frequency = 440.0
    private val sampleRate = 44100
    private var phase = 0.0

    override fun startDrone(frequency: Double) {
        if (isRunning) {
            updateFrequency(frequency)
            return
        }

        this.frequency = frequency
        isRunning = true

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        Thread {
            val buffer = ShortArray(1024)
            while (isRunning) {
                for (i in buffer.indices) {
                    buffer[i] = (sin(phase) * Short.MAX_VALUE * 0.5).toInt().toShort()
                    phase += 2.0 * PI * this.frequency / sampleRate
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }.start()
    }

    override fun stopDrone() {
        isRunning = false
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        phase = 0.0
    }

    override fun updateFrequency(frequency: Double) {
        this.frequency = frequency
    }

    override fun release() {
        stopDrone()
    }
}
