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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cropdoctorai.data.ml.CropPrediction
import com.example.cropdoctorai.ui.theme.GlassBorder
import com.example.cropdoctorai.ui.theme.MintGlow
import com.example.cropdoctorai.ui.theme.TextPrimary
import com.example.cropdoctorai.ui.theme.TextSecondary
import com.example.cropdoctorai.ui.theme.TextMuted

/**
 * Compact three-card result row with glassmorphism dark theme:
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
        horizontalArrangement = Arrangement.spacedBy(10.dp)
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

// Glass card background color
private val GlassCardBg = Color(0xFF0D2B1A).copy(alpha = 0.85f)
private val GlassBorderColor = Color(0xFFFFFFFF).copy(alpha = 0.12f)

@Composable
private fun CropIdentifiedCard(
    prediction: CropPrediction,
    alternatives: List<CropPrediction>,
    modifier: Modifier = Modifier
) {
    val accentGreen = MintGlow

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassBorderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        // Top accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentGreen)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "CROP IDENTIFIED",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = prediction.cropName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(prediction.confidence * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = accentGreen,
            fontWeight = FontWeight.Bold
        )
        if (alternatives.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alternatives.joinToString(", ") {
                    it.cropName
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                lineHeight = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun HealthStatusCard(
    isHealthy: Boolean,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isHealthy) MintGlow else Color(0xFFEF5350)
    val statusText = if (isHealthy) "Healthy" else "Disease\nDetected"
    val statusIcon = if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Error
    val iconBgTint = if (isHealthy) MintGlow.copy(alpha = 0.15f) else Color(0xFFEF5350).copy(alpha = 0.15f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassBorderColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HEALTH STATUS",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Status indicator circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBgTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = statusColor,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun DiseasePestCard(
    prediction: CropPrediction,
    modifier: Modifier = Modifier
) {
    val accentColor = if (prediction.isHealthy) MintGlow else Color(0xFFEF5350)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassBorderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        // Top accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "DISEASE / PEST",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = prediction.diseaseName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Confidence bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(GlassBorderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = prediction.confidence)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(prediction.confidence * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
    }
}
