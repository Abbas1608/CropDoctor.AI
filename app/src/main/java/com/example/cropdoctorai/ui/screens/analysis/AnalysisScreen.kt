package com.example.cropdoctorai.ui.screens.analysis

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.cropdoctorai.ui.screens.analysis.components.AboutDiseaseCard
import com.example.cropdoctorai.ui.screens.analysis.components.RemedyCard
import com.example.cropdoctorai.ui.screens.analysis.components.ResultCardsRow
import com.example.cropdoctorai.ui.screens.analysis.components.UploadSection

/**
 * Main Analysis screen composable.
 * Handles the full flow: upload → analyze → results → PDF download.
 * All analysis results are real from TFLite + Gemini AI.
 */
@Composable
fun AnalysisScreen(
    onBack: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectImage(it) }
    }

    // Handle PDF download notification
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is AnalysisUiState.Results && state.pdfPath != null) {
            Toast.makeText(
                context,
                "📥 Report saved to Downloads: ${state.pdfPath}",
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearPdfPath()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top Bar ──
        TopBar(onBack = onBack)

        // ── Scrollable Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Header Section ──
            HeaderSection()

            Spacer(modifier = Modifier.height(20.dp))

            // ── Dynamic Content Based on State ──
            when (val state = uiState) {
                is AnalysisUiState.Idle -> {
                    UploadSection(
                        onChooseFile = { imagePickerLauncher.launch("image/*") }
                    )
                }

                is AnalysisUiState.ImageSelected -> {
                    ImagePreviewSection(
                        imageUri = state.imageUri,
                        fileName = state.fileName,
                        fileSize = state.fileSize,
                        mimeType = state.mimeType,
                        onClear = { viewModel.clearImage() },
                        onAnalyze = { viewModel.analyzeImage() }
                    )
                }

                is AnalysisUiState.Analyzing -> {
                    AnalyzingSection(
                        imageUri = state.imageUri,
                        progressMessage = state.progressMessage
                    )
                }

                is AnalysisUiState.Results -> {
                    ResultsSection(
                        state = state,
                        onDownloadPdf = { viewModel.generatePdf() },
                        onNewAnalysis = { viewModel.clearImage() }
                    )
                }

                is AnalysisUiState.Error -> {
                    ErrorSection(
                        message = state.message,
                        onRetry = {
                            viewModel.clearImage()
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Footer Disclaimer ──
            Text(
                text = "⚠ This report is AI-generated using Custom Crop_Model.tflite + Gemini AI. " +
                        "Always consult a certified agronomist before applying any treatment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
                lineHeight = 16.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════
// Sub-composables
// ═══════════════════════════════════════════

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF2E7D32)
            )
        }
        Text(
            text = "🌿 AI Crops Doctor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2E7D32))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "CUSTOM AI MODEL",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    Text(
        text = "Upload a crop photo — our custom AI model identifies the crop, " +
                "then Gemini AI provides disease diagnosis, treatment advice, " +
                "and a downloadable PDF report.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 18.sp
    )
}

@Composable
private fun ImagePreviewSection(
    imageUri: android.net.Uri,
    fileName: String,
    fileSize: String,
    mimeType: String,
    onClear: () -> Unit,
    onAnalyze: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image thumbnail with close button
        Box {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected crop image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            // Close button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF616161))
                    .clickable { onClear() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove image",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // File info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Size: $fileSize · Type: $mimeType",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Analyze button
            Button(
                onClick = onAnalyze,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF43A047),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analyze Crop Health",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AnalyzingSection(
    imageUri: android.net.Uri,
    progressMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color(0xFF43A047),
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = progressMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF43A047),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please wait while our AI analyzes your crop...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultsSection(
    state: AnalysisUiState.Results,
    onDownloadPdf: () -> Unit,
    onNewAnalysis: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(500, easing = EaseOutCubic)
        )
    ) {
        Column {
            // ── Results Header Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📊", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analysis Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Download PDF button
                if (state.geminiAnalysis != null) {
                    Button(
                        onClick = onDownloadPdf,
                        enabled = !state.isPdfGenerating,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B4D3E),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF90A4AE)
                        )
                    ) {
                        if (state.isPdfGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isPdfGenerating) "Generating..." else "Download Full Report",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Three Result Cards ──
            ResultCardsRow(
                prediction = state.prediction,
                alternatives = state.alternativePredictions
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── About Disease ──
            if (state.geminiAnalysis != null) {
                AboutDiseaseCard(aboutText = state.geminiAnalysis.aboutDisease)

                Spacer(modifier = Modifier.height(16.dp))

                // ── Organic Remedies ──
                RemedyCard(
                    title = "Organic / Biological Remedy",
                    emoji = "🟢",
                    items = state.geminiAnalysis.organicRemedies,
                    headerColor = Color(0xFF2E7D32),
                    bgColor = Color(0xFFF1F8E9)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Chemical Remedies ──
                RemedyCard(
                    title = "Chemical Remedy",
                    emoji = "🟠",
                    items = state.geminiAnalysis.chemicalRemedies,
                    headerColor = Color(0xFFEF6C00),
                    bgColor = Color(0xFFFFF3E0)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Prevention Tips ──
                RemedyCard(
                    title = "Prevention Tips",
                    emoji = "🔴",
                    items = state.geminiAnalysis.preventionTips,
                    headerColor = Color(0xFFC62828),
                    bgColor = Color(0xFFFFEBEE)
                )
            } else if (state.isGeminiLoading) {
                // Loading Gemini results
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF43A047),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Getting expert analysis from Gemini AI...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF43A047)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── New Analysis Button ──
            Button(
                onClick = onNewAnalysis,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start New Analysis",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ErrorSection(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "⚠️", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Analysis Failed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF5350),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Try Again", fontWeight = FontWeight.SemiBold)
        }
    }
}
