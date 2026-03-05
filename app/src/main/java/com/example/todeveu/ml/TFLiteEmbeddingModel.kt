package com.example.todeveu.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt

/**
 * Model d'embeddings d'orador via TFLite.
 * Carrega assets/speaker_embedding.tflite.
 * Cal adaptar inputShape, outputSize i preprocessament segons el model real
 * (sampleRate, mel bins, window/hop, etc.) — veure README.
 */
class TFLiteEmbeddingModel(
    context: Context,
    private val assetPath: String = "speaker_embedding.tflite",
    private val sampleRate: Int = 16000,
    private val expectedInputSamples: Int = 16000 * 2, // 2 s per defecte
) : SpeakerEmbeddingModel {

    private val modelBuffer: ByteBuffer
    private val interpreter: Interpreter
    private val inputBuffer: FloatBuffer
    private val outputBuffer: Array<FloatArray>

    override val embeddingSize: Int
        get() = outputBuffer[0].size

    init {
        val modelBytes = context.assets.open(assetPath).use { it.readBytes() }
        modelBuffer = ByteBuffer.allocateDirect(modelBytes.size).apply {
            order(ByteOrder.nativeOrder())
            put(modelBytes)
            rewind()
        }
        val options = Interpreter.Options().setNumThreads(2)
        interpreter = Interpreter(modelBuffer, options)

        val inputShape = interpreter.getInputTensor(0).shape()
        val inputSize = inputShape.fold(1) { a, b -> a * b }
        inputBuffer = ByteBuffer.allocateDirect(inputSize * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        val outputShape = interpreter.getOutputTensor(0).shape()
        val outputSize = outputShape.fold(1) { a, b -> a * b }
        outputBuffer = arrayOf(FloatArray(outputSize))
    }

    override fun embed(audio16k: FloatArray): FloatArray {
        val inputSize = interpreter.getInputTensor(0).numElements()
        inputBuffer.rewind()
        when {
            audio16k.size >= inputSize -> {
                for (i in 0 until inputSize) inputBuffer.put(audio16k[i])
            }
            else -> {
                for (i in audio16k.indices) inputBuffer.put(audio16k[i])
                while (inputBuffer.position() < inputSize) inputBuffer.put(0f)
            }
        }
        inputBuffer.rewind()
        interpreter.run(inputBuffer, outputBuffer)
        return l2Normalize(outputBuffer[0])
    }

    private fun l2Normalize(x: FloatArray): FloatArray {
        var norm = 0.0
        for (v in x) norm += v * v
        norm = sqrt(norm).coerceAtLeast(1e-12)
        return FloatArray(x.size) { (x[it] / norm).toFloat() }
    }

    fun close() {
        interpreter.close()
    }
}
