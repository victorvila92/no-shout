package com.example.todeveu.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.example.todeveu.R
import com.example.todeveu.audio.AudioRecorder
import com.example.todeveu.audio.Vad
import com.example.todeveu.data.AppSettings
import com.example.todeveu.data.AppDatabase
import com.example.todeveu.data.EventEntity
import com.example.todeveu.data.SettingsDataStore
import com.example.todeveu.data.createAppDatabase
import com.example.todeveu.data.defaultSettings
import com.example.todeveu.ml.MockEmbeddingModel
import com.example.todeveu.ml.SpeakerEmbeddingModel
import com.example.todeveu.ml.SpeakerVerifier
import com.example.todeveu.notif.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceMonitorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var settingsDataStore: SettingsDataStore? = null
    private var database: AppDatabase? = null
    private var embeddingModel: SpeakerEmbeddingModel? = null
    private var verifier: SpeakerVerifier? = null
    private var recorder: AudioRecorder? = null
    private var vad: Vad? = null

    private val _state = MutableStateFlow(MonitorState())
    val state: StateFlow<MonitorState> = _state.asStateFlow()

    companion object {
        const val ACTION_START = "com.example.todeveu.START"
        const val ACTION_STOP = "com.example.todeveu.STOP"
        private const val SAMPLE_RATE = 16000
        private const val FRAME_MS = 25
        private const val INFERENCE_CHUNK_MS = 1000
        val globalState = MutableStateFlow(MonitorState())
        fun stateFlow(): StateFlow<MonitorState> = globalState.asStateFlow()
        @Volatile
        var instance: VoiceMonitorService? = null
            private set
    }

    private var silenceUntilMillis: Long = 0L
    private var lastInferenceTime = 0L
    private var highDbStartTime = 0L
    private var lastDbAboveThreshold = false

    data class MonitorState(
        val isListening: Boolean = false,
        val dbRelatiu: Float = -80f,
        val similarityScore: Float = 0f,
        val vadScore: Float = 0f,
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsDataStore = SettingsDataStore(this)
        database = createAppDatabase(this)
        embeddingModel = MockEmbeddingModel(embeddingSize = 32, sampleRate = SAMPLE_RATE)
        recorder = AudioRecorder(sampleRate = SAMPLE_RATE, frameSizeMs = FRAME_MS)
        vad = Vad()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
            NotificationHelper.ACTION_SILENCE_5MIN -> {
                silenceUntilMillis = System.currentTimeMillis() + 5 * 60 * 1000
                NotificationHelper.cancelShoutNotification(this)
            }
            NotificationHelper.ACTION_DISMISS -> {
                silenceUntilMillis = Long.MAX_VALUE
                NotificationHelper.cancelShoutNotification(this)
            }
            else -> if (_state.value.isListening) startForegroundWithNotification() else stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        instance = null
        stopMonitoring()
        scope.cancel()
        super.onDestroy()
    }

    private fun startMonitoring() {
        if (_state.value.isListening) return
        NotificationHelper.createChannels(this)
        startForegroundWithNotification()
        scope.launch {
            loadVerifier()
            val rec = recorder ?: return@launch
            val v = vad ?: return@launch
            if (!rec.start()) {
                _state.update { it.copy(isListening = false) }
                stopSelf()
                return@launch
            }
            _state.update { it.copy(isListening = true) }
            Companion.globalState.value = _state.value
            val model = embeddingModel ?: return@launch
            val frameMs = FRAME_MS
            val sampleRate = SAMPLE_RATE
            val samplesPerInference = sampleRate * INFERENCE_CHUNK_MS / 1000
            var buffer = FloatArray(0)
            rec.frameFlow().collect { frame ->
                if (!_state.value.isListening) return@collect
                val vadS = v.score(frame)
                _state.update { it.copy(dbRelatiu = frame.dbRelatiu, vadScore = vadS) }
                Companion.globalState.value = _state.value
                buffer = buffer + frame.samples
                val settings = (settingsDataStore?.settingsFlow?.first() ?: defaultSettings)
                while (buffer.size >= samplesPerInference) {
                    val chunk = buffer.take(samplesPerInference).toFloatArray()
                    buffer = buffer.drop(samplesPerInference).toFloatArray()
                    val now = System.currentTimeMillis()
                    if (now - lastInferenceTime >= settings.inferenceIntervalMs.toLong()) {
                        lastInferenceTime = now
                        val emb = model.embed(chunk)
                        val sim = verifier?.score(emb) ?: 0.5f
                        _state.update { it.copy(similarityScore = sim) }
                        Companion.globalState.value = _state.value
                        checkShout(
                            dbRelatiu = frame.dbRelatiu,
                            vadScore = _state.value.vadScore,
                            similarityScore = sim,
                            settings = settings,
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadVerifier() {
        val profile = settingsDataStore?.getEnrollmentEmbedding()
        val settings = settingsDataStore?.settingsFlow?.first() ?: defaultSettings
        verifier = if (profile != null && profile.isNotEmpty())
            SpeakerVerifier(profile, settings.speakerThreshold)
        else
            null
    }

    private suspend fun checkShout(
        dbRelatiu: Float,
        vadScore: Float,
        similarityScore: Float,
        settings: AppSettings,
    ) {
        if (System.currentTimeMillis() < silenceUntilMillis) return
        val dbThreshold = if (settings.hysteresisEnabled) {
            if (lastDbAboveThreshold) settings.hysteresisUmbralOff else settings.hysteresisUmbralOn
        } else settings.dbThreshold
        val aboveDb = dbRelatiu >= dbThreshold
        if (aboveDb) {
            if (highDbStartTime == 0L) highDbStartTime = System.currentTimeMillis()
            val sustained = System.currentTimeMillis() - highDbStartTime >= settings.sustainMs
            val vadOk = vadScore >= settings.vadThreshold
            val speakerOk = verifier == null || similarityScore >= settings.speakerThreshold
            if (sustained && vadOk && speakerOk) {
                triggerShout(dbRelatiu, vadScore, similarityScore, settings)
                highDbStartTime = 0L
                silenceUntilMillis = System.currentTimeMillis() + settings.cooldownMs
            }
        } else {
            highDbStartTime = 0L
        }
        lastDbAboveThreshold = aboveDb
    }

    private suspend fun triggerShout(
        dbRelatiu: Float,
        vadScore: Float,
        similarityScore: Float,
        settings: AppSettings,
    ) {
        withContext(Dispatchers.Main) {
            NotificationHelper.showShoutNotification(this@VoiceMonitorService, settings.vibrationEnabled, silenceUntilMillis)
        }
        val event = EventEntity(
            timestamp = System.currentTimeMillis(),
            dbRelatiu = dbRelatiu,
            similarityScore = similarityScore,
            vadScore = vadScore,
            tipusEvent = "CRIT",
            sustainMs = settings.sustainMs,
            cooldownMs = settings.cooldownMs,
            dbThreshold = settings.dbThreshold,
            speakerThreshold = settings.speakerThreshold,
            vadThreshold = settings.vadThreshold,
        )
        database?.eventDao()?.insert(event)
    }

    private fun stopMonitoring() {
        _state.update { it.copy(isListening = false) }
        Companion.globalState.value = _state.value
        recorder?.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") stopForeground(true)
        }
        stopSelf()
    }

    private fun startForegroundWithNotification() {
        val builder = NotificationHelper.buildListeningNotification(this)
        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.NOTIFICATION_LISTENING_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_LISTENING_ID, notification)
        }
    }
}
