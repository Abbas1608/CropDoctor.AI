package com.example.cropdoctorai.ui.screens.analysis

import android.net.Uri
import com.example.cropdoctorai.data.ml.CropPrediction
import com.example.cropdoctorai.data.remote.GeminiAnalysis

/**
 * Represents the complete UI state of the Analysis screen.
 */
sealed class AnalysisUiState {

    /** Initial state — no image selected */
    data object Idle : AnalysisUiState()

    /** User has selected an image but hasn't started analysis */
    data class ImageSelected(
        val imageUri: Uri,
        val fileName: String,
        val fileSize: String,
        val mimeType: String
    ) : AnalysisUiState()

    /** Analysis is in progress */
    data class Analyzing(
        val imageUri: Uri,
        val progressMessage: String
    ) : AnalysisUiState()

    /** Analysis complete — showing results */
    data class Results(
        val imageUri: Uri,
        val prediction: CropPrediction,
        val alternativePredictions: List<CropPrediction>,
        val geminiAnalysis: GeminiAnalysis?,
        val isGeminiLoading: Boolean,
        val pdfPath: String? = null,
        val isPdfGenerating: Boolean = false
    ) : AnalysisUiState()

    /** An error occurred */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : AnalysisUiState()
}
