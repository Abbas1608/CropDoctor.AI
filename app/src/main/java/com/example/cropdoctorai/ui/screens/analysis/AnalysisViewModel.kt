package com.example.cropdoctorai.ui.screens.analysis

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropdoctorai.data.ml.CropClassifier
import com.example.cropdoctorai.data.pdf.CropReportGenerator
import com.example.cropdoctorai.data.remote.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the Analysis screen.
 * Manages the full flow: image selection → TFLite inference → Gemini analysis → PDF generation.
 * All results are real and dynamic — nothing is hardcoded.
 */
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    application: Application,
    private val cropClassifier: CropClassifier,
    private val geminiService: GeminiService,
    private val reportGenerator: CropReportGenerator
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private var currentBitmap: Bitmap? = null
    private var isClassifierInitialized = false

    init {
        // Initialize the TFLite classifier on a background thread
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cropClassifier.initialize()
                isClassifierInitialized = true
            } catch (e: Exception) {
                // Classifier will fail gracefully when used
                e.printStackTrace()
            }
        }
    }

    /**
     * Called when user selects an image from gallery or camera.
     */
    fun selectImage(uri: Uri) {
        val context = getApplication<Application>()

        // Get file info
        var fileName = "image.jpg"
        var fileSize = "Unknown"
        var mimeType = "image/jpeg"

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex >= 0) {
                        val bytes = cursor.getLong(sizeIndex)
                        fileSize = formatFileSize(bytes)
                    }
                }
            }
            mimeType = context.contentResolver.getType(uri) ?: mimeType
        } catch (e: Exception) {
            // Use defaults
        }

        _uiState.value = AnalysisUiState.ImageSelected(
            imageUri = uri,
            fileName = fileName,
            fileSize = fileSize,
            mimeType = mimeType
        )
    }

    /**
     * Starts the full analysis pipeline:
     * 1. Load image as bitmap
     * 2. Run TFLite model for crop/disease prediction
     * 3. Send prediction to Gemini for detailed analysis
     */
    fun analyzeImage() {
        val currentState = _uiState.value
        val imageUri = when (currentState) {
            is AnalysisUiState.ImageSelected -> currentState.imageUri
            is AnalysisUiState.Results -> currentState.imageUri
            else -> return
        }

        viewModelScope.launch {
            try {
                // Step 1: Loading image
                _uiState.value = AnalysisUiState.Analyzing(imageUri, "Loading image...")

                val bitmap = withContext(Dispatchers.IO) {
                    loadBitmapFromUri(imageUri)
                } ?: throw Exception("Failed to load image. Please try a different image.")

                currentBitmap = bitmap

                // Step 2: Running TFLite inference
                _uiState.value = AnalysisUiState.Analyzing(imageUri, "Analyzing crop with AI model...")

                if (!isClassifierInitialized) {
                    withContext(Dispatchers.IO) {
                        cropClassifier.initialize()
                        isClassifierInitialized = true
                    }
                }

                val predictions = withContext(Dispatchers.IO) {
                    cropClassifier.classify(bitmap, topK = 3)
                }

                if (predictions.isEmpty()) {
                    throw Exception("Model could not identify the crop. Please try a clearer image.")
                }

                val topPrediction = predictions.first()
                val alternatives = predictions.drop(1)

                // Step 3: Show initial results, then start Gemini analysis
                _uiState.value = AnalysisUiState.Results(
                    imageUri = imageUri,
                    prediction = topPrediction,
                    alternativePredictions = alternatives,
                    geminiAnalysis = null,
                    isGeminiLoading = true
                )

                // Step 4: Get Gemini analysis (runs in parallel with UI update)
                _uiState.value = AnalysisUiState.Analyzing(imageUri, "Getting expert analysis from Gemini AI...")

                val geminiAnalysis = withContext(Dispatchers.IO) {
                    geminiService.analyzeDisease(topPrediction)
                }

                _uiState.value = AnalysisUiState.Results(
                    imageUri = imageUri,
                    prediction = topPrediction,
                    alternativePredictions = alternatives,
                    geminiAnalysis = geminiAnalysis,
                    isGeminiLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = AnalysisUiState.Error(
                    message = e.message ?: "An unexpected error occurred.",
                    canRetry = true
                )
            }
        }
    }

    /**
     * Generate and save the PDF report.
     */
    fun generatePdf() {
        val currentState = _uiState.value
        if (currentState !is AnalysisUiState.Results) return
        if (currentState.geminiAnalysis == null) return

        // Mark as generating
        _uiState.value = currentState.copy(isPdfGenerating = true)

        viewModelScope.launch {
            val pdfPath = withContext(Dispatchers.IO) {
                reportGenerator.generateReport(
                    prediction = currentState.prediction,
                    analysis = currentState.geminiAnalysis,
                    cropImage = currentBitmap
                )
            }

            _uiState.value = currentState.copy(
                isPdfGenerating = false,
                pdfPath = pdfPath
            )
        }
    }

    /**
     * Reset to idle state for a new analysis.
     */
    fun clearImage() {
        currentBitmap = null
        _uiState.value = AnalysisUiState.Idle
    }

    /**
     * Reset just the PDF path (after showing download notification).
     */
    fun clearPdfPath() {
        val currentState = _uiState.value
        if (currentState is AnalysisUiState.Results) {
            _uiState.value = currentState.copy(pdfPath = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cropClassifier.close()
        currentBitmap = null
    }

    // ─── Private Helpers ─────────────────────────────────────

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        val context = getApplication<Application>()
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
