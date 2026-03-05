package com.example.todeveu.ml

import kotlin.math.sqrt

/**
 * Verificador d'orador: compara embedding actual amb perfil (mitjana enrolada)
 * mitjançant similitud cosinus. Perfil ha d'estar normalitzat L2.
 */
class SpeakerVerifier(
    private val profileEmbedding: FloatArray,
    private val threshold: Float,
) {
    private val profile = l2Normalize(profileEmbedding.copyOf())

    /**
     * Similitud cosinus entre current i perfil [0, 1] (assumint vectors L2 normalitzats).
     */
    fun score(current: FloatArray): Float {
        val c = l2Normalize(current)
        if (c.size != profile.size) return 0f
        var dot = 0.0
        for (i in profile.indices) dot += profile[i] * c[i]
        return (dot.coerceIn(-1.0, 1.0).toFloat() + 1f) / 2f
    }

    fun isUser(current: FloatArray): Boolean = score(current) >= threshold

    private fun l2Normalize(x: FloatArray): FloatArray {
        var norm = 0.0
        for (v in x) norm += v * v
        norm = sqrt(norm).coerceAtLeast(1e-12)
        return FloatArray(x.size) { (x[it] / norm).toFloat() }
    }
}
