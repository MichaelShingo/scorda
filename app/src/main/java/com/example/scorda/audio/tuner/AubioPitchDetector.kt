package com.example.scorda.audio.tuner

class AubioPitchDetector {

    private var handle: Long = 0

    companion object {
        init {
            System.loadLibrary("aubio_jni")
        }
    }

    fun initialize(sampleRate: Int, bufSize: Int, hopSize: Int) {
        if (handle != 0L) return
        handle = nativeInit(sampleRate, bufSize, hopSize)
    }

    fun process(audioData: FloatArray): FloatArray? {
        if (handle == 0L) return null
        return nativeProcess(handle, audioData)
    }

    fun release() {
        if (handle != 0L) {
            nativeCleanup(handle)
            handle = 0
        }
    }

    fun setSilence(silence: Float) {
        if (handle != 0L) nativeSetSilence(handle, silence)
    }

    fun setTolerance(tolerance: Float) {
        if (handle != 0L) nativeSetTolerance(handle, tolerance)
    }

    private external fun nativeInit(sampleRate: Int, bufSize: Int, hopSize: Int): Long
    private external fun nativeProcess(handle: Long, audioData: FloatArray): FloatArray
    private external fun nativeSetSilence(handle: Long, silence: Float)
    private external fun nativeSetTolerance(handle: Long, tolerance: Float)
    private external fun nativeCleanup(handle: Long)
}
