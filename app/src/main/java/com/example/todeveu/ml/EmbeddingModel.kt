package com.example.todeveu.ml

/**
 * Interfície per a models d'embeddings d'orador.
 * Entrada: àudio mono 16 kHz (FloatArray normalitzat -1..1).
 * Sortida: vector d'embedding normalitzat L2 (per cosine similarity).
 */
interface SpeakerEmbeddingModel {
    /** Dimensió de l'embedding. */
    val embeddingSize: Int

    /**
     * Genera l'embedding per un segment d'àudio.
     * @param audio16k Àudio mono 16 kHz, normalitzat [-1, 1]
     * @return Vector normalitzat L2, length = embeddingSize
     */
    fun embed(audio16k: FloatArray): FloatArray
}
