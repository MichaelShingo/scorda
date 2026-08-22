package com.example.scorda.audio

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.scorda.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import nl.igorski.mwengine.MWEngine
import nl.igorski.mwengine.core.ADSR
import nl.igorski.mwengine.core.ChannelGroup
import nl.igorski.mwengine.core.Drivers
import nl.igorski.mwengine.core.JavaUtilities
import nl.igorski.mwengine.core.Notifications
import nl.igorski.mwengine.core.SampleEvent
import nl.igorski.mwengine.core.SampleManager
import nl.igorski.mwengine.core.SampledInstrument
import nl.igorski.mwengine.core.SequencerController
import nl.igorski.mwengine.core.SynthEvent
import nl.igorski.mwengine.core.SynthInstrument
import java.io.File
import java.io.FileOutputStream

data class AudioViewModelUiState(
    val isDronePlaying: Boolean = false,
    val isMetronomePlaying: Boolean = false,
    val metronomeBpm: Int = 120,
    val metronomeBeatsPerMeasure: Int = 4,
    val currentMetronomeBeat: Int = 0
)


class AudioViewModel(application: Application) : AndroidViewModel(application),
    DefaultLifecycleObserver {

    enum class MetronomeSample(val key: String, val resId: Int) {
        STRONG("metronome_strong", R.raw.metronome_1_strong),
        WEAK("metronome_weak", R.raw.metronome_1_weak);

        companion object {
            // Optional: helper to find by key if needed
            fun fromKey(key: String) = entries.find { it.key == key }
        }
    }

    private val LOG_TAG = "AudioViewModel"
    private val STEPS_PER_BEAT = 4

    private var mwEngine: MWEngine? = null
    private var isInitialized = false


    // DRONE
    private var droneSynthInstrument: SynthInstrument? = null
    private var droneAdsr: ADSR? = null
    private var droneToneEvent: SynthEvent? = null
    private val _isDronePlaying = MutableStateFlow(false)

    // METRONOME
    private var sequencerController: SequencerController? = null
    private var channelGroup: ChannelGroup? = null
    private var metronomeInstrument: SampledInstrument? = null
    private val metronomeEvents = mutableListOf<SampleEvent>()

    private var observer: MWEngine.IObserver? = null

    private val _isMetronomePlaying = MutableStateFlow(false)
    private val _metronomeTempo = MutableStateFlow(120)
    private val _metronomeBeatsPerMeasure = MutableStateFlow(4)
    private val _currentMetronomeBeat = MutableStateFlow(0)

    val uiState: StateFlow<AudioViewModelUiState> = combine(
        _isDronePlaying,
        _isMetronomePlaying,
        _metronomeTempo,
        _metronomeBeatsPerMeasure,
        _currentMetronomeBeat
    ) { drone, metro, bpm, beats, current ->
        AudioViewModelUiState(
            isDronePlaying = drone,
            isMetronomePlaying = metro,
            metronomeBpm = bpm,
            metronomeBeatsPerMeasure = beats,
            currentMetronomeBeat = current
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioViewModelUiState()
    )

    companion object {
        init {
            System.loadLibrary("mwengine_wrapped")
        }
    }

    private fun loadSampleFromRaw(key: String, resourceId: Int) {
        val context = getApplication<Application>().applicationContext
        val cacheDir = context.cacheDir
        val tempFile = File(cacheDir, "$key.wav")

        try {
            context.resources.openRawResource(resourceId).use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            JavaUtilities.createSampleFromFile(key, tempFile.absolutePath)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "loadSampleFromRaw($key) failed", e)
        }
    }

    fun initialize(activity: Activity) {
        /*
        the MainActivity is recreated on orientation change
        single optimizePerformance is passed the activity, it should run again on orientation change
        without reinitializing the entire engine, which survives along with the ViewModel
         */
        MWEngine.optimizePerformance(activity)

        if (isInitialized) return

        val context = getApplication<Application>().applicationContext
        val sampleRate = MWEngine.getRecommendedSampleRate(context)
        val bufferSize = MWEngine.getRecommendedBufferSize(context)

        observer = object : MWEngine.IObserver {
            override fun handleNotification(notificationId: Int) {
                if (notificationId == Notifications.ids.SEQUENCER_POSITION_UPDATED.swigValue()) {
                    // Map steps back to beats for the UI
                    val currentStep = sequencerController?.getStepPosition() ?: 0
                    _currentMetronomeBeat.value = currentStep / STEPS_PER_BEAT
                }
            }

            override fun handleNotification(notificationId: Int, notificationValue: Int) {
                if (notificationId == Notifications.ids.SEQUENCER_POSITION_UPDATED.swigValue()) {
                    val currentStep = sequencerController?.getStepPosition() ?: 0
                    _currentMetronomeBeat.value = currentStep / STEPS_PER_BEAT
                }
            }
        }

        // Instantiate the engine
        mwEngine = MWEngine(observer)

        // Create the output stream
        val outputChannels = 2
        val inputChannels = 0
        val audioDriver = Drivers.types.AAUDIO // low-latency AAudio driver
        mwEngine?.createOutput(sampleRate, bufferSize, outputChannels, inputChannels, audioDriver)

        // createOutput creates a new SequencerController instance
        // Must retrieve it after createOutput
        sequencerController = mwEngine?.sequencerController

        // Initialize channel group for shared control over volume, effects, panning
        channelGroup = ChannelGroup(1.0f)
        mwEngine?.addChannelGroup(channelGroup)

        // Initialize drone
        droneSynthInstrument = SynthInstrument()
        droneSynthInstrument?.registerInSequencer()
        droneSynthInstrument?.audioChannel?.let { channelGroup?.addAudioChannel(it) }
        droneAdsr = ADSR(0f, 0f, 1f, 0f)
        droneSynthInstrument?.adsr = droneAdsr
        droneSynthInstrument?.getOscillatorProperties(0)?.waveform = 0 // 0 = sine
        droneToneEvent = SynthEvent(440.0f, droneSynthInstrument)

        // Initialize metronome

        MetronomeSample.entries.forEach { sample ->
            loadSampleFromRaw(sample.key, sample.resId)
        }

        metronomeInstrument = SampledInstrument()
        metronomeInstrument?.registerInSequencer()
        metronomeInstrument?.audioChannel?.let { channelGroup?.addAudioChannel(it) }
        setupMetronomeEvents()

        isInitialized = true
        mwEngine?.start()
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

    // onCleared is used instead of onDestroy, because it doesn't run on screen orientation changes
    // prevents application crash on screen orientation
    override fun onCleared() {
        // cleanup drone
        droneToneEvent?.delete()
        droneToneEvent = null

        droneAdsr?.delete()
        droneAdsr = null

        droneSynthInstrument?.delete()
        droneSynthInstrument = null

        metronomeInstrument?.delete()
        metronomeInstrument = null

        metronomeEvents.forEach { it.delete() }
        metronomeEvents.clear()

        sequencerController?.delete()
        sequencerController = null

        channelGroup?.delete()
        channelGroup = null

        // Clean up the C++ memory!
        mwEngine?.dispose()
        mwEngine = null

        observer = null
    }

    // DRONE

    fun startDrone() {
        if (droneToneEvent != null) {
            droneToneEvent?.play()
            _isDronePlaying.value = true
        }
    }

    fun stopDrone() {
        if (droneToneEvent != null) {
            droneToneEvent?.stop()
            _isDronePlaying.value = false
        }
    }

    fun updateDroneFrequency(frequency: Double) {
        droneToneEvent?.frequency = frequency.toFloat()
    }

    // METRONOME
    fun startMetronome() {
        sequencerController?.rewind()
        sequencerController?.setPlaying(true)
        _isMetronomePlaying.value = true
    }

    fun stopMetronome() {
        sequencerController?.setPlaying(false)
        _isMetronomePlaying.value = false
        _currentMetronomeBeat.value = 0
    }

    fun setMetronomeBpm(bpm: Int) {
        _metronomeTempo.value = bpm
        updateSequencerTempo()
    }

    fun setMetronomeBeatsPerMeasure(beats: Int) {
        _metronomeBeatsPerMeasure.value = beats
        setupMetronomeEvents()
        updateSequencerTempo()
    }

    private fun updateSequencerTempo() {
        sequencerController?.setTempoNow(
            _metronomeTempo.value.toFloat(),
            _metronomeBeatsPerMeasure.value,
            4
        )
    }

    private fun setupMetronomeEvents() {
        val controller = sequencerController ?: return
        val beats = _metronomeBeatsPerMeasure.value

        // stop the engine thread during reconfiguration to prevent crashes
        mwEngine?.stop()

        controller.setPlaying(false)
        controller.rewind() // Reset to beat 1

        // cleanup existing events
        metronomeEvents.forEach {
            it.removeFromSequencer()
            it.delete()
        }
        metronomeEvents.clear()

        // update tempo and measure structure
        val totalSteps = beats * STEPS_PER_BEAT
        controller.updateMeasures(1, totalSteps)
        updateSequencerTempo()

        // set the loop range in samples
        val totalSamples = controller.samplesPerBar
        controller.setLoopRange(0, totalSamples - 1)

        // create and schedule new events
        for (i in 0 until beats) {
            val sampleEvent = SampleEvent(metronomeInstrument)
            val sampleKey = if (i == 0) MetronomeSample.STRONG else MetronomeSample.WEAK

            val sample = SampleManager.getSample(sampleKey.key)
            if (sample == null) {
                Log.e("METRONOME", "Sample $sampleKey NOT found during setup!")
            }
            sampleEvent.setSample(sample)

            sampleEvent.positionEvent(0, totalSteps, i * STEPS_PER_BEAT)
            sampleEvent.isSequenced = true
            sampleEvent.volume = 1.0f

            sampleEvent.addToSequencer()
            metronomeEvents.add(sampleEvent)
        }

        // restart engine thread
        mwEngine?.start()

        if (_isMetronomePlaying.value) {
            controller.setPlaying(true)
        }
    }

}

val LocalAudioViewModel = staticCompositionLocalOf<AudioViewModel> {
    error("No AudioViewModel provided")
}