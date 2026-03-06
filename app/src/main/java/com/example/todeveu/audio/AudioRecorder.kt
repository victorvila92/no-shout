package com.example.todeveu.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.sqrt

/**
 * Captura àudio contínu en PCM 16-bit mono 16 kHz.
 * Proporciona frames amb RMS i dB relatiu (ref = 32768).
 */
class AudioRecorder(
    private val sampleRate: Int = 16000,
    private val frameSizeMs: Int = 25,
) {
    companion object {
        private const val TAG = "AudioRecorder"
    }
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val frameSamples = (sampleRate * frameSizeMs / 1000).coerceAtLeast(1)
    private val bufferSizeSamples = frameSamples * 4 // buffer robust

    private var audioRecord: AudioRecord? = null

    val minBufferSize: Int
        get() = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    fun isRecording(): Boolean = audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING

    fun start(): Boolean {
        if (audioRecord != null) return isRecording()
        val size = maxOf(minBufferSize, bufferSizeSamples * 2)
        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                size
            )
        } catch (e: SecurityException) {
            return false
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return false
        }
        record.startRecording()
        audioRecord = record
        Log.d(TAG, "start: OK rate=$sampleRate frameSamples=$frameSamples")
        return true
    }

    fun stop() {
        Log.d(TAG, "stop")
        audioRecord?.apply {
            if (recordingState == AudioRecord.RECORDSTATE_RECORDING) stop()
            release()
        }
        audioRecord = null
    }

    /**
     * Llegeix frames contínuament. Cada element és (samples FloatArray, rms, dbRelatiu).
     * dB relatiu: 20*log10(rms/32768). No calibrat amb un SPL real.
     */
    fun frameFlow(): Flow<AudioFrame> = flow {
        val record = audioRecord ?: return@flow
        val shortBuffer = ShortArray(frameSamples)
        while (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            val read = record.read(shortBuffer, 0, frameSamples)
            if (read <= 0) continue
            val samples = FloatArray(read) { shortBuffer[it] / 32768f }
            val rms = rms(samples)
            val dbRelatiu = if (rms > 1e-10f) 20f * kotlin.math.log10(rms) else -80f
            emit(AudioFrame(samples, rms, dbRelatiu, read))
        }
    }.flowOn(Dispatchers.IO)

    private fun rms(samples: FloatArray): Float {
        var sum = 0.0
        for (s in samples) sum += s * s
        return sqrt((sum / samples.size).toFloat()).toFloat()
    }

    data class AudioFrame(
        val samples: FloatArray,
        val rms: Float,
        val dbRelatiu: Float,
        val length: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as AudioFrame
            return samples.contentEquals(other.samples) && rms == other.rms && dbRelatiu == other.dbRelatiu && length == other.length
        }
        override fun hashCode(): Int = samples.contentHashCode() + 31 * (rms.hashCode() + 31 * (dbRelatiu.hashCode() + 31 * length))
    }
}
