package com.example.cropdoctorai.data.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device TFLite classifier for crop disease prediction.
 *
 * Loads the Crop_Model_.tflite model and labels.txt from the assets folder,
 * preprocesses input images, and returns real predictions with confidence scores.
 */
@Singleton
class CropClassifier @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    // Default model input dimensions — will be read from model
    private var inputWidth = 224
    private var inputHeight = 224
    private var inputChannels = 3
    private var isQuantized = false
    private var outputClasses = 0

    companion object {
        private const val MODEL_FILENAME = "Crop_Model_.tflite"
        private const val LABELS_FILENAME = "labels.txt"
    }

    /**
     * Initialize the TFLite interpreter and load labels.
     * Must be called before running inference.
     */
    fun initialize() {
        if (interpreter != null) return

        try {
            // Load model
            val modelBuffer = loadModelFile(MODEL_FILENAME)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)

            // Read input tensor shape to determine model's expected dimensions
            val inputTensor = interpreter!!.getInputTensor(0)
            val inputShape = inputTensor.shape()
            // Typical shape: [1, height, width, channels]
            if (inputShape.size == 4) {
                inputHeight = inputShape[1]
                inputWidth = inputShape[2]
                inputChannels = inputShape[3]
            }

            // Check if model is quantized
            isQuantized = inputTensor.dataType() == org.tensorflow.lite.DataType.UINT8

            // Read output tensor shape to avoid mismatch with labels.size
            val outputTensor = interpreter!!.getOutputTensor(0)
            outputClasses = outputTensor.shape().last()

            // Load labels
            labels = loadLabels(LABELS_FILENAME)
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize CropClassifier: ${e.message}", e)
        }
    }

    /**
     * Run inference on a bitmap image.
     *
     * @param bitmap The crop image to classify
     * @param topK Number of top predictions to return (default 3)
     * @return List of CropPredictions sorted by confidence (highest first)
     */
    fun classify(bitmap: Bitmap, topK: Int = 3): List<CropPrediction> {
        val safeInterpreter = interpreter
            ?: throw IllegalStateException("CropClassifier not initialized. Call initialize() first.")

        // Resize bitmap to model's expected input size
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)

        // Convert bitmap to ByteBuffer
        val inputBuffer = if (isQuantized) {
            bitmapToByteBufferQuantized(resizedBitmap)
        } else {
            bitmapToByteBufferFloat(resizedBitmap)
        }

        // Prepare output buffer
        val outputArray = if (isQuantized) {
            Array(1) { ByteArray(outputClasses) }
        } else {
            Array(1) { FloatArray(outputClasses) }
        }

        // Run inference
        safeInterpreter.run(inputBuffer, outputArray)

        // Free resized bitmap if it's a new instance
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }

        // Parse results
        val confidences = if (isQuantized) {
            (outputArray as Array<ByteArray>)[0].map { (it.toInt() and 0xFF) / 255f }
        } else {
            (outputArray as Array<FloatArray>)[0].toList()
        }

        // Build predictions sorted by confidence
        return confidences
            .mapIndexed { index, confidence ->
                val label = if (index < labels.size) labels[index] else "Unknown_$index"
                CropPrediction.fromRawLabel(label, confidence)
            }
            .sortedByDescending { it.confidence }
            .take(topK)
    }

    /**
     * Get the total number of classes the model can predict.
     */
    fun getClassCount(): Int = outputClasses

    /**
     * Release TFLite resources.
     */
    fun close() {
        interpreter?.close()
        interpreter = null
    }

    // ─── Private Helpers ─────────────────────────────────────

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels(filename: String): List<String> {
        val labels = mutableListOf<String>()
        val reader = BufferedReader(InputStreamReader(context.assets.open(filename)))
        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    labels.add(trimmed)
                }
            }
        }
        return labels
    }

    private fun bitmapToByteBufferFloat(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputWidth * inputHeight * inputChannels)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in pixels) {
            // Normalize to [0, 1]
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f) // R
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)  // G
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)          // B
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    private fun bitmapToByteBufferQuantized(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * inputChannels)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in pixels) {
            byteBuffer.put(((pixel shr 16) and 0xFF).toByte()) // R
            byteBuffer.put(((pixel shr 8) and 0xFF).toByte())  // G
            byteBuffer.put((pixel and 0xFF).toByte())           // B
        }

        byteBuffer.rewind()
        return byteBuffer
    }
}
