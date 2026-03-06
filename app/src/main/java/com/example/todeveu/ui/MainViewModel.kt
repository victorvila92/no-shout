package com.example.todeveu.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todeveu.data.AppSettings
import com.example.todeveu.data.SettingsDataStore
import com.example.todeveu.data.createAppDatabase
import com.example.todeveu.ml.SpeakerEmbeddingModel
import com.example.todeveu.ml.createEmbeddingModel
import com.example.todeveu.service.VoiceMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private val settingsDataStore = SettingsDataStore(app)
    private val database = createAppDatabase(app)

    private val _settings = MutableStateFlow<AppSettings?>(null)
    val settings: StateFlow<AppSettings?> = _settings.asStateFlow()

    /** Mateix model que el servei (TFLite si hi ha asset, sinó Mock). Per enrolament. */
    private val embeddingModel: SpeakerEmbeddingModel by lazy { createEmbeddingModel(app) }

    fun embedForEnrollment(audio16k: FloatArray): FloatArray = embeddingModel.embed(audio16k)

    init {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { _settings.value = it }
        }
    }

    val monitorState = VoiceMonitorService.stateFlow()

    fun startMonitoring() {
        app.startService(Intent(app, VoiceMonitorService::class.java).setAction(VoiceMonitorService.ACTION_START))
    }

    fun stopMonitoring() {
        app.startService(Intent(app, VoiceMonitorService::class.java).setAction(VoiceMonitorService.ACTION_STOP))
    }

    fun updateDbThreshold(value: Float) { viewModelScope.launch { settingsDataStore.updateDbThreshold(value) } }
    fun updateSustainMs(value: Int) { viewModelScope.launch { settingsDataStore.updateSustainMs(value) } }
    fun updateCooldownMs(value: Int) { viewModelScope.launch { settingsDataStore.updateCooldownMs(value) } }
    fun updateSpeakerThreshold(value: Float) { viewModelScope.launch { settingsDataStore.updateSpeakerThreshold(value) } }
    fun updateVadThreshold(value: Float) { viewModelScope.launch { settingsDataStore.updateVadThreshold(value) } }
    fun updateVibrationEnabled(value: Boolean) { viewModelScope.launch { settingsDataStore.updateVibrationEnabled(value) } }
    fun updateInferenceIntervalMs(value: Int) { viewModelScope.launch { settingsDataStore.updateInferenceIntervalMs(value) } }
    fun updateDebugMode(value: Boolean) { viewModelScope.launch { settingsDataStore.updateDebugMode(value) } }
    fun updateHysteresisEnabled(value: Boolean) { viewModelScope.launch { settingsDataStore.updateHysteresisEnabled(value) } }
    fun updateHysteresisUmbralOff(value: Float) { viewModelScope.launch { settingsDataStore.updateHysteresisUmbralOff(value) } }
    fun updateHysteresisUmbralOn(value: Float) { viewModelScope.launch { settingsDataStore.updateHysteresisUmbralOn(value) } }

    fun eventsToday(startOfDay: Long) = database.eventDao().eventsToday(startOfDay)
    fun allEvents() = database.eventDao().allEvents()
    suspend fun allEventsList() = database.eventDao().allEventsList()

    fun saveEnrollmentEmbedding(embedding: FloatArray) {
        viewModelScope.launch { settingsDataStore.setEnrollmentEmbedding(embedding) }
    }
}
