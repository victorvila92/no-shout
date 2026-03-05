package com.example.todeveu.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * FFT simple i estadístiques espectral per VAD i placeholder per mel-spectrogram.
 * FFT en potència de 2.
 */
object FeatureExtractor {
    fun fftMagnitudes(samples: FloatArray): FloatArray {
        val n = nextPowerOf2(samples.size)
        val real = FloatArray(n) { if (it < samples.size) samples[it] else 0f }
        val imag = FloatArray(n)
        fft(real, imag, n)
        return FloatArray(n / 2) { i ->
            val r = real[i]
            val im = imag[i]
            sqrt(r * r + im * im)
        }
    }

    private fun fft(real: FloatArray, imag: FloatArray, n: Int) {
        if (n <= 1) return
        val half = n / 2
        val reEven = FloatArray(half)
        val imEven = FloatArray(half)
        val reOdd = FloatArray(half)
        val imOdd = FloatArray(half)
        for (i in 0 until half) {
            reEven[i] = real[2 * i]
            imEven[i] = imag[2 * i]
            reOdd[i] = real[2 * i + 1]
            imOdd[i] = imag[2 * i + 1]
        }
        fft(reEven, imEven, half)
        fft(reOdd, imOdd, half)
        for (k in 0 until half) {
            val angle = (-2 * PI * k / n).toFloat()
            val tRe = cos(angle).toFloat() * reOdd[k] - sin(angle).toFloat() * imOdd[k]
            val tIm = cos(angle).toFloat() * imOdd[k] + sin(angle).toFloat() * reOdd[k]
            real[k] = reEven[k] + tRe
            imag[k] = imEven[k] + tIm
            real[k + half] = reEven[k] - tRe
            imag[k + half] = imEven[k] - tIm
        }
    }

    fun bandEnergy(magnitudes: FloatArray, lowBin: Int, highBin: Int): Float {
        var sum = 0f
        for (i in lowBin.coerceAtLeast(0) until highBin.coerceAtMost(magnitudes.size))
            sum += magnitudes[i] * magnitudes[i]
        return sqrt(sum)
    }

    fun zeroCrossingRate(samples: FloatArray): Float {
        var zcr = 0
        for (i in 1 until samples.size)
            if ((samples[i] >= 0) != (samples[i - 1] >= 0)) zcr++
        return zcr.toFloat() / (samples.size - 1).coerceAtLeast(1)
    }

    private fun nextPowerOf2(n: Int): Int {
        var v = n
        v--
        v = v or (v shr 1)
        v = v or (v shr 2)
        v = v or (v shr 4)
        v = v or (v shr 8)
        v = v or (v shr 16)
        return v + 1
    }

    /**
     * Placeholder per mel-spectrogram. Retorna un vector fix per poder connectar
     * amb un model que esperi mels (adaptar dimensions segons model real).
     */
    fun melSpectrogramPlaceholder(samples: FloatArray, sampleRate: Int, nMelBins: Int = 40, hopMs: Int = 10): FloatArray {
        val hopSamples = (sampleRate * hopMs / 1000).coerceAtLeast(1)
        val nFrames = (samples.size - 512).coerceAtLeast(0) / hopSamples + 1
        val outSize = nFrames * nMelBins
        if (outSize <= 0) return FloatArray(nMelBins)
        val mags = fftMagnitudes(samples)
        val lowBand = bandEnergy(mags, 0, mags.size / 4)
        return FloatArray(outSize) { lowBand * 0.01f }
    }
}
