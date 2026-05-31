package com.example.cropdoctorai.ui.screens.analysis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cropdoctorai.data.ml.CropPrediction

/**
 * Three-card result row showing:
 * 1. Crop Identified (green accent)
 * 2. Health Status (red/green indicator)
 * 3. Disease/Pest (red accent)
 */
@Composable
fun ResultCardsRow(
    prediction: CropPrediction,
    alternatives: List<CropPrediction>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Crop Identified
        CropIdentifiedCard(
            prediction = prediction,
            alternatives = alternatives,
            modifier = Modifier.weight(1f)
        )

        // Card 2: Health Status
        HealthStatusCard(
            isHealthy = prediction.isHealthy,
            confidence = prediction.confidence,
            modifier = Modifier.weight(1f)
        )

        // Card 3: Disease/Pest
        DiseasePestCard(
            prediction = prediction,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CropIdentifiedCard(
    prediction: CropPrediction,
    alternatives: List<CropPrediction>,
    modifier: Modifier = Modifier
) {
    val accentGreen = Color(0xFF4CAF50)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(start = 4.dp)
    ) {
        // Left accent border effect
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(120.dp)
                    .background(accentGreen, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "CROP IDENTIFIED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = prediction.cropName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(prediction.confidence * 100).toInt()}% confidence",
                    style = MaterialTheme.typography.bodySmall,
                    color = accentGreen,
                    fontWeight = FontWeight.Medium
                )
                if (alternatives.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Also: ${alternatives.joinToString(", ") { it.rawLabel.replace("___", " ") }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E9E9E),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthStatusCard(
    isHealthy: Boolean,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isHealthy) Color(0xFF4CAF50) else Color(0xFFE53935)
    val statusText = if (isHealthy) "Healthy" else "Disease Detected"
    val statusIcon = if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Error
    val bgTint = if (isHealthy) Color(0xFFF1F8E9) else Color(0xFFFFF3F0)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HEALTH STATUS",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF757575),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Status indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bgTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = statusText,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = statusColor,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(confidence * 100).toInt()}% disease probability",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun DiseasePestCard(
    prediction: CropPrediction,
    modifier: Modifier = Modifier
) {
    val accentColor = if (prediction.isHealthy) Color(0xFF4CAF50) else Color(0xFFE53935)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(end = 4.dp)
    ) {
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = "DISEASE / PEST",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = prediction.diseaseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Confidence bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = prediction.confidence)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(prediction.confidence * 100).toInt()}% confidence",
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(120.dp)
                    .background(accentColor, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
            )
        }
    }
}
