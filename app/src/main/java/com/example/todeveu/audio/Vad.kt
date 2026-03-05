package com.example.todeveu.audio

import com.example.todeveu.audio.AudioRecorder.AudioFrame

/**
 * VAD lleuger: energia + ZCR + band-energy (FFT) per donar un score 0..1 "probable veu".
 * No envia res a xarxa; tot local.
 */
class Vad(
    private val energyWeight: Float = 0.5f,
    private val zcrWeight: Float = 0.2f,
    private val spectralWeight: Float = 0.3f,
) {
    private var energySmoothed = 0.01f
    private val alpha = 0.3f

    /**
     * Score en [0, 1]: probabilitat que hi hagi veu.
     */
    fun score(frame: AudioFrame): Float {
        val rms = frame.rms.coerceIn(1e-10f, 1f)
        energySmoothed = alpha * energySmoothed + (1 - alpha) * rms
        val energyNorm = (energySmoothed * 50f).coerceIn(0f, 1f)

        val zcr = FeatureExtractor.zeroCrossingRate(frame.samples)
        // La veu típica té ZCR moderat (no molt alt com soroll blanc, no zero com silenci)
        val zcrNorm = (zcr * 3f).coerceIn(0f, 1f)

        val mags = FeatureExtractor.fftMagnitudes(frame.samples)
        val lowBand = FeatureExtractor.bandEnergy(mags, 0, mags.size / 4)
        val midBand = FeatureExtractor.bandEnergy(mags, mags.size / 4, mags.size / 2)
        val spectral = (lowBand + midBand) * 0.01f
        val spectralNorm = spectral.coerceIn(0f, 1f)

        return (energyWeight * energyNorm + zcrWeight * zcrNorm + spectralWeight * spectralNorm).coerceIn(0f, 1f)
    }
}
