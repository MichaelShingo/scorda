package com.example.scorda.audio

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import nl.igorski.mwengine.MWEngine
import nl.igorski.mwengine.core.Drivers
import nl.igorski.mwengine.core.SynthEvent
import nl.igorski.mwengine.core.SynthInstrument

class AudioViewModel(application: Application) : AndroidViewModel(application),
    DefaultLifecycleObserver {

    // The main engine instance is of type MWEngine
    private var mwEngine: MWEngine? = null
    private var isInitialized = false


    // DRONE
    private var synthInstrument: SynthInstrument? = null
    private var adsr: nl.igorski.mwengine.core.ADSR? = null
    private var liveToneEvent: SynthEvent? = null

    companion object {
        init {
            System.loadLibrary("mwengine_wrapped")
        }
    }

    fun initialize(activity: Activity) {
        if (isInitialized) return

        // 1. Optimize performance (Requires the Activity)
        MWEngine.optimizePerformance(activity)

        // 2. Get device-specific recommended buffer and sample rate
        // We can safely use the Application context here
        val context = getApplication<Application>().applicationContext
        val sampleRate = MWEngine.getRecommendedSampleRate(context)
        val bufferSize = MWEngine.getRecommendedBufferSize(context)

        // 3. Create the observer with the correct two-parameter signature
        val observer = object : MWEngine.IObserver {

            // Overload 1: Single integer
            override fun handleNotification(notificationId: Int) {
                // Handle simple events
            }

            // Overload 2: Two integers
            override fun handleNotification(notificationId: Int, notificationValue: Int) {
                // Handle events with extra data attached
            }
        }

        // 4. Instantiate the engine
        mwEngine = MWEngine(observer)

        // 5. Create the output stream
        // The Java code passes 5 arguments: sampleRate, bufferSize, outputChannels, inputChannels, driver
// 5. Create the output stream using the strongly-typed enum
        val outputChannels = 2
        val inputChannels = 0
        val audioDriver = Drivers.types.AAUDIO // Uses the low-latency AAudio driver

        mwEngine?.createOutput(sampleRate, bufferSize, outputChannels, inputChannels, audioDriver)

        // Initialize drone
        synthInstrument = SynthInstrument()
        adsr = nl.igorski.mwengine.core.ADSR(0f, 0f, 1f, 0f)
        synthInstrument?.setAdsr(adsr)
        
        synthInstrument?.getOscillatorProperties(0)?.waveform = 0 // 0 = sine
        liveToneEvent = SynthEvent(440.0f, synthInstrument)

        isInitialized = true


    }

    override fun onResume(owner: LifecycleOwner) {
        // Start the engine when the app is in the foreground
        mwEngine?.start()
    }

    override fun onPause(owner: LifecycleOwner) {
        // Stop the engine when the app goes to the background to save battery
        // Note: If stop() throws an unresolved reference, the wrapper might not expose it directly.
        // If it does, you can safely remove this line and just rely on dispose() in onDestroy().
        mwEngine?.stop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        // cleanup drone
        liveToneEvent?.delete()
        liveToneEvent = null
        
        adsr?.delete()
        adsr = null
        
        synthInstrument?.delete()
        synthInstrument = null

        // Clean up the C++ memory!
        mwEngine?.dispose()
        mwEngine = null
    }

    // DRONE

    fun playTone() {
        liveToneEvent?.play()
    }

    fun stopTone() {
        liveToneEvent?.stop()
    }


}

val LocalAudioViewModel = staticCompositionLocalOf<AudioViewModel> {
    error("No AnnotationViewModel provided")
}