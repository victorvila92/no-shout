package com.example.todeveu.ml

import com.example.todeveu.audio.FeatureExtractor
import kotlin.math.sqrt

/**
 * Model mock determinista per tenir el flux complet sense TFLite.
 * Usa estadístiques espectral (tipus MFCC-like) com a "pseudo-embedding".
 * Substituir per TFLiteEmbeddingModel quan es disposi de speaker_embedding.tflite.
 */
class MockEmbeddingModel(
    override val embeddingSize: Int = 32,
    private val sampleRate: Int = 16000,
) : SpeakerEmbeddingModel {

    override fun embed(audio16k: FloatArray): FloatArray {
        if (audio16k.size < 512) return FloatArray(embeddingSize)
        val mags = FeatureExtractor.fftMagnitudes(audio16k)
        val n = mags.size
        val bands = 8
        val bandSize = (n / bands).coerceAtLeast(1)
        val bandEnergies = FloatArray(bands) { b ->
            val start = b * bandSize
            var sum = 0f
            for (i in start until minOf(start + bandSize, n)) sum += mags[i] * mags[i]
            sqrt(sum)
        }
        val zcr = FeatureExtractor.zeroCrossingRate(audio16k)
        val rms = run {
            var sum = 0.0
            for (s in audio16k) sum += s * s
            sqrt(sum / audio16k.size).toFloat()
        }
        val out = FloatArray(embeddingSize)
        for (i in 0 until bands) out[i] = bandEnergies[i] * 10f
        out[bands] = zcr
        out[bands + 1] = rms * 10f
        for (i in bands + 2 until embeddingSize) out[i] = (bandEnergies[i % bands] * (i % 3 - 1)).toFloat() * 0.1f
        return l2Normalize(out)
    }

    private fun l2Normalize(x: FloatArray): FloatArray {
        var norm = 0.0
        for (v in x) norm += v * v
        norm = sqrt(norm).coerceAtLeast(1e-12)
        return FloatArray(x.size) { (x[it] / norm).toFloat() }
    }
}
