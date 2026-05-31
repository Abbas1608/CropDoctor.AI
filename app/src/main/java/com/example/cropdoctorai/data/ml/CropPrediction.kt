package com.example.cropdoctorai.data.ml

/**
 * Represents a single prediction result from the TFLite crop disease model.
 *
 * @param cropName Extracted crop name (e.g., "Apple", "Wheat")
 * @param diseaseName Extracted disease name (e.g., "Black Rot", "Brown Rust") or "Healthy"
 * @param confidence Model confidence score between 0.0 and 1.0
 * @param rawLabel Original model label (e.g., "Apple___Black_rot")
 * @param isHealthy True if the prediction indicates a healthy crop
 */
data class CropPrediction(
    val cropName: String,
    val diseaseName: String,
    val confidence: Float,
    val rawLabel: String,
    val isHealthy: Boolean
) {
    companion object {
        /**
         * Parses a raw model label in the format "CropName___DiseaseName"
         * into a structured CropPrediction.
         *
         * Handles various label formats:
         * - "Apple___Black_rot" → crop=Apple, disease=Black Rot
         * - "Tomato___healthy" → crop=Tomato, disease=Healthy, isHealthy=true
         * - "Wheat___Brown_Rust" → crop=Wheat, disease=Brown Rust
         */
        fun fromRawLabel(rawLabel: String, confidence: Float): CropPrediction {
            val parts = rawLabel.split("___")
            val cropName = if (parts.isNotEmpty()) {
                parts[0].replace("_", " ").trim()
            } else {
                "Unknown"
            }
            val rawDisease = if (parts.size > 1) {
                parts[1].replace("_", " ").trim()
            } else {
                "Unknown"
            }
            val isHealthy = rawDisease.equals("healthy", ignoreCase = true)
            val diseaseName = if (isHealthy) "Healthy" else rawDisease.capitalizeWords()

            return CropPrediction(
                cropName = cropName.capitalizeWords(),
                diseaseName = diseaseName,
                confidence = confidence,
                rawLabel = rawLabel,
                isHealthy = isHealthy
            )
        }

        private fun String.capitalizeWords(): String {
            return split(" ").joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
        }
    }
}
