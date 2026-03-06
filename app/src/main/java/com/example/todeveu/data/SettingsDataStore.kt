package com.example.todeveu.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val DB_THRESHOLD = floatPreferencesKey("db_threshold")
    val SUSTAIN_MS = intPreferencesKey("sustain_ms")
    val COOLDOWN_MS = intPreferencesKey("cooldown_ms")
    val SPEAKER_THRESHOLD = floatPreferencesKey("speaker_threshold")
    val VAD_THRESHOLD = floatPreferencesKey("vad_threshold")
    val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    val INFERENCE_INTERVAL_MS = intPreferencesKey("inference_interval_ms")
    val DEBUG_MODE = booleanPreferencesKey("debug_mode")
    val HYSTERESIS_ENABLED = booleanPreferencesKey("hysteresis_enabled")
    val HYSTERESIS_UMBRAL_OFF = floatPreferencesKey("hysteresis_umbral_off")
    val HYSTERESIS_UMBRAL_ON = floatPreferencesKey("hysteresis_umbral_on")
    val ENROLLMENT_EMBEDDING = longPreferencesKey("enrollment_embedding") // stored as bytes -> we use separate prefs
}

data class AppSettings(
    val dbThreshold: Float,
    val sustainMs: Int,
    val cooldownMs: Int,
    val speakerThreshold: Float,
    val vadThreshold: Float,
    val vibrationEnabled: Boolean,
    val inferenceIntervalMs: Int,
    val debugMode: Boolean,
    val hysteresisEnabled: Boolean,
    val hysteresisUmbralOff: Float,
    val hysteresisUmbralOn: Float,
)

val defaultSettings = AppSettings(
    dbThreshold = -22f,
    sustainMs = 500,
    cooldownMs = 10_000,
    speakerThreshold = 0.5f,
    vadThreshold = 0.35f,
    vibrationEnabled = true,
    inferenceIntervalMs = 500,
    debugMode = false,
    hysteresisEnabled = false,
    hysteresisUmbralOff = -18f,
    hysteresisUmbralOn = -10f,
)

class SettingsDataStore(private val context: Context) {
    private val floatKeys = listOf(
        SettingsKeys.DB_THRESHOLD to defaultSettings.dbThreshold,
        SettingsKeys.SPEAKER_THRESHOLD to defaultSettings.speakerThreshold,
        SettingsKeys.VAD_THRESHOLD to defaultSettings.vadThreshold,
        SettingsKeys.HYSTERESIS_UMBRAL_OFF to defaultSettings.hysteresisUmbralOff,
        SettingsKeys.HYSTERESIS_UMBRAL_ON to defaultSettings.hysteresisUmbralOn,
    )
    private val intKeys = listOf(
        SettingsKeys.SUSTAIN_MS to defaultSettings.sustainMs,
        SettingsKeys.COOLDOWN_MS to defaultSettings.cooldownMs,
        SettingsKeys.INFERENCE_INTERVAL_MS to defaultSettings.inferenceIntervalMs,
    )
    private val boolKeys = listOf(
        SettingsKeys.VIBRATION_ENABLED to defaultSettings.vibrationEnabled,
        SettingsKeys.DEBUG_MODE to defaultSettings.debugMode,
        SettingsKeys.HYSTERESIS_ENABLED to defaultSettings.hysteresisEnabled,
    )

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            dbThreshold = prefs[SettingsKeys.DB_THRESHOLD] ?: defaultSettings.dbThreshold,
            sustainMs = prefs[SettingsKeys.SUSTAIN_MS] ?: defaultSettings.sustainMs,
            cooldownMs = prefs[SettingsKeys.COOLDOWN_MS] ?: defaultSettings.cooldownMs,
            speakerThreshold = prefs[SettingsKeys.SPEAKER_THRESHOLD] ?: defaultSettings.speakerThreshold,
            vadThreshold = prefs[SettingsKeys.VAD_THRESHOLD] ?: defaultSettings.vadThreshold,
            vibrationEnabled = prefs[SettingsKeys.VIBRATION_ENABLED] ?: defaultSettings.vibrationEnabled,
            inferenceIntervalMs = prefs[SettingsKeys.INFERENCE_INTERVAL_MS] ?: defaultSettings.inferenceIntervalMs,
            debugMode = prefs[SettingsKeys.DEBUG_MODE] ?: defaultSettings.debugMode,
            hysteresisEnabled = prefs[SettingsKeys.HYSTERESIS_ENABLED] ?: defaultSettings.hysteresisEnabled,
            hysteresisUmbralOff = prefs[SettingsKeys.HYSTERESIS_UMBRAL_OFF] ?: defaultSettings.hysteresisUmbralOff,
            hysteresisUmbralOn = prefs[SettingsKeys.HYSTERESIS_UMBRAL_ON] ?: defaultSettings.hysteresisUmbralOn,
        )
    }

    suspend fun updateDbThreshold(value: Float) = context.dataStore.edit { it[SettingsKeys.DB_THRESHOLD] = value }
    suspend fun updateSustainMs(value: Int) = context.dataStore.edit { it[SettingsKeys.SUSTAIN_MS] = value }
    suspend fun updateCooldownMs(value: Int) = context.dataStore.edit { it[SettingsKeys.COOLDOWN_MS] = value }
    suspend fun updateSpeakerThreshold(value: Float) = context.dataStore.edit { it[SettingsKeys.SPEAKER_THRESHOLD] = value }
    suspend fun updateVadThreshold(value: Float) = context.dataStore.edit { it[SettingsKeys.VAD_THRESHOLD] = value }
    suspend fun updateVibrationEnabled(value: Boolean) = context.dataStore.edit { it[SettingsKeys.VIBRATION_ENABLED] = value }
    suspend fun updateInferenceIntervalMs(value: Int) = context.dataStore.edit { it[SettingsKeys.INFERENCE_INTERVAL_MS] = value }
    suspend fun updateDebugMode(value: Boolean) = context.dataStore.edit { it[SettingsKeys.DEBUG_MODE] = value }
    suspend fun updateHysteresisEnabled(value: Boolean) = context.dataStore.edit { it[SettingsKeys.HYSTERESIS_ENABLED] = value }
    suspend fun updateHysteresisUmbralOff(value: Float) = context.dataStore.edit { it[SettingsKeys.HYSTERESIS_UMBRAL_OFF] = value }
    suspend fun updateHysteresisUmbralOn(value: Float) = context.dataStore.edit { it[SettingsKeys.HYSTERESIS_UMBRAL_ON] = value }

    suspend fun setEnrollmentEmbedding(embedding: FloatArray) {
        context.dataStore.edit { prefs ->
            val keyPrefix = "emb_"
            embedding.forEachIndexed { i, v ->
                prefs[floatPreferencesKey(keyPrefix + i)] = v
            }
            prefs[intPreferencesKey("emb_size")] = embedding.size
        }
    }

    suspend fun getEnrollmentEmbedding(): FloatArray? {
        val prefs = context.dataStore.data.first()
        val size = prefs[intPreferencesKey("emb_size")] ?: return null
        return FloatArray(size) { i -> prefs[floatPreferencesKey("emb_$i")] ?: 0f }
    }

    fun enrollmentEmbeddingFlow(): Flow<FloatArray?> = context.dataStore.data.map { prefs ->
        val size = prefs[intPreferencesKey("emb_size")] ?: return@map null
        FloatArray(size) { i -> prefs[floatPreferencesKey("emb_$i")] ?: 0f }
    }

    suspend fun clearEnrollment() {
        context.dataStore.edit { prefs ->
            val size = prefs[intPreferencesKey("emb_size")] ?: 0
            repeat(size) { i -> prefs.remove(floatPreferencesKey("emb_$i")) }
            prefs.remove(intPreferencesKey("emb_size"))
        }
    }
}
