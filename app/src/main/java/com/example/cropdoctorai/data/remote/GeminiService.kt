package com.example.cropdoctorai.data.remote

import com.example.cropdoctorai.data.ml.CropPrediction
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service that communicates with the Gemini AI API to generate
 * real, dynamic crop disease analysis based on TFLite predictions.
 *
 * All remedies, disease info, and prevention tips are generated
 * dynamically by Gemini — absolutely nothing is hardcoded.
 */
@Singleton
class GeminiService @Inject constructor(
    private val apiKey: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Serializable
    private data class GeminiJsonResponse(
        val aboutDisease: String = "",
        val organicRemedies: List<String> = emptyList(),
        val chemicalRemedies: List<String> = emptyList(),
        val preventionTips: List<String> = emptyList()
    )

    /**
     * Generate a comprehensive analysis for a detected crop disease.
     *
     * @param prediction The TFLite model's prediction result
     * @return GeminiAnalysis with real, AI-generated content
     */
    suspend fun analyzeDisease(prediction: CropPrediction): GeminiAnalysis {
        if (apiKey.isBlank()) {
            return GeminiAnalysis(
                aboutDisease = "Gemini API key not configured. Please add GEMINI_API_KEY to local.properties.",
                organicRemedies = listOf("Configure API key to get real recommendations."),
                chemicalRemedies = listOf("Configure API key to get real recommendations."),
                preventionTips = listOf("Configure API key to get real recommendations.")
            )
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 2048
                }
            )

            val prompt = buildAnalysisPrompt(prediction)
            val response = model.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response from Gemini")

            parseGeminiResponse(responseText)
        } catch (e: Exception) {
            GeminiAnalysis(
                aboutDisease = "Crop identified as ${prediction.cropName}. Condition: ${prediction.diseaseName}. " +
                        "(Gemini unavailable: ${e.message})",
                organicRemedies = listOf("Gemini AI service temporarily unavailable. Please try again later."),
                chemicalRemedies = listOf("Gemini AI service temporarily unavailable. Please try again later."),
                preventionTips = listOf("Gemini AI service temporarily unavailable. Please try again later.")
            )
        }
    }

    /**
     * Builds a structured prompt that instructs Gemini to return
     * a JSON response with specific fields.
     */
    private fun buildAnalysisPrompt(prediction: CropPrediction): String {
        val conditionDescription = if (prediction.isHealthy) {
            "The crop appears to be HEALTHY with no visible disease."
        } else {
            "The crop has been detected with the disease/condition: ${prediction.diseaseName}."
        }

        return """
            You are an expert agricultural scientist and plant pathologist. 
            Analyze the following crop disease detection result and provide detailed, practical advice.

            CROP IDENTIFIED: ${prediction.cropName}
            CONDITION: ${prediction.diseaseName}
            CONFIDENCE: ${(prediction.confidence * 100).toInt()}%
            MODEL CLASS: ${prediction.rawLabel}
            $conditionDescription

            Respond ONLY with a valid JSON object (no markdown, no code fences, no extra text) with these exact keys:
            {
                "aboutDisease": "A detailed 3-4 sentence paragraph about this specific disease/condition on ${prediction.cropName}. Include what causes it, how it spreads, and its impact on crop yield. If the crop is healthy, describe the ideal health condition and what signs to watch for.",
                "organicRemedies": [
                    "Provide 4-5 specific organic/biological treatment recommendations. Each should include exact product names, dosages (in g/litre or ml/litre), and application frequency. Be very specific and practical."
                ],
                "chemicalRemedies": [
                    "Provide 3-4 specific approved agrochemical treatments. Include exact chemical names, formulations (EC, WP, SC), dosages (in ml/litre or g/litre), and application timing. Be very specific."
                ],
                "preventionTips": [
                    "Provide 4-5 specific proactive prevention measures. Include crop management practices, resistant varieties, environmental controls, and monitoring advice."
                ]
            }

            IMPORTANT: 
            - Every recommendation must be REAL and scientifically accurate
            - Include specific product names, dosages, and application methods
            - Tailor all advice specifically to ${prediction.cropName} and ${prediction.diseaseName}
            - Do NOT include generic advice — be very specific to this exact crop-disease combination
            - Return ONLY the JSON object, nothing else
        """.trimIndent()
    }

    /**
     * Parses the Gemini response text into a GeminiAnalysis object.
     * Handles both clean JSON and JSON embedded in markdown code blocks.
     */
    private fun parseGeminiResponse(responseText: String): GeminiAnalysis {
        // Clean up response — remove markdown code fences if present
        var cleanedText = responseText.trim()
        if (cleanedText.startsWith("```")) {
            cleanedText = cleanedText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        }

        return try {
            val parsed = json.decodeFromString<GeminiJsonResponse>(cleanedText)
            GeminiAnalysis(
                aboutDisease = parsed.aboutDisease,
                organicRemedies = parsed.organicRemedies,
                chemicalRemedies = parsed.chemicalRemedies,
                preventionTips = parsed.preventionTips
            )
        } catch (e: Exception) {
            // If JSON parsing fails, try to extract content manually
            GeminiAnalysis(
                aboutDisease = cleanedText.take(500),
                organicRemedies = listOf("Unable to parse structured response. Raw AI output available in the About section."),
                chemicalRemedies = listOf("Unable to parse structured response. Please try analysis again."),
                preventionTips = listOf("Unable to parse structured response. Please try analysis again.")
            )
        }
    }
}
