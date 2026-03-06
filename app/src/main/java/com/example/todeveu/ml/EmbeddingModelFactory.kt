package com.example.todeveu.ml

import android.content.Context
import android.util.Log
import java.io.FileNotFoundException

private const val TAG = "EmbeddingModel"
private const val TFLITE_ASSET = "speaker_embedding.tflite"
private const val DEFAULT_SAMPLE_RATE = 16000
/** Mostres per defecte: 2 s a 16 kHz (molts models esperen 1–3 s). */
private const val DEFAULT_INPUT_SAMPLES = DEFAULT_SAMPLE_RATE * 2

/**
 * Crea el model d'embeddings a usar a l'app.
 * Si existeix `assets/speaker_embedding.tflite`, s'usa [TFLiteEmbeddingModel];
 * si no, es fa servir [MockEmbeddingModel].
 * Cal que enrolament i servei usin el mateix tipus de model (mateix asset o sense).
 */
fun createEmbeddingModel(context: Context): SpeakerEmbeddingModel {
    return try {
        context.assets.open(TFLITE_ASSET).use { }
        val model = TFLiteEmbeddingModel(
            context = context,
            assetPath = TFLITE_ASSET,
            sampleRate = DEFAULT_SAMPLE_RATE,
            expectedInputSamples = DEFAULT_INPUT_SAMPLES,
        )
        Log.i(TAG, "createEmbeddingModel: using TFLite (embeddingSize=${model.embeddingSize})")
        model
    } catch (e: FileNotFoundException) {
        Log.i(TAG, "createEmbeddingModel: asset not found, using MockEmbeddingModel")
        MockEmbeddingModel(embeddingSize = 32, sampleRate = DEFAULT_SAMPLE_RATE)
    } catch (e: Exception) {
        Log.w(TAG, "createEmbeddingModel: TFLite failed, using Mock", e)
        MockEmbeddingModel(embeddingSize = 32, sampleRate = DEFAULT_SAMPLE_RATE)
    }
}
