package com.example.ui.screens.adherence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.StatCard
import com.example.ui.theme.ElegantAmber
import com.example.ui.theme.ElegantAmberContainer
import com.example.ui.theme.ElegantBlue
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreen
import com.example.ui.theme.ElegantGreenContainer
import com.example.ui.theme.ElegantOnGreenContainer
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantOnPurpleContainer
import com.example.ui.theme.ElegantPurple
import com.example.ui.theme.ElegantPurpleContainer
import com.example.ui.theme.ElegantRed
import com.example.ui.theme.ElegantRedContainer
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.viewmodel.MedCareViewModel

@Composable
fun AdherenceAnalyticsScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val allDoses by viewModel.allDoseHistory.collectAsState()
    val todayDoses by viewModel.todayDoses.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    // Calculations
    val totalDoses = allDoses.size
    val takenDoses = allDoses.count { it.status == "TAKEN" }
    val skippedDoses = allDoses.count { it.status == "SKIPPED" }
    val missedDoses = allDoses.count { it.status == "MISSED" }

    val overallAdherencePercent = if (totalDoses > 0) ((takenDoses.toFloat() / totalDoses) * 100).toInt() else 100
    val todayTotal = todayDoses.size
    val todayTaken = todayDoses.count { it.status == "TAKEN" }
    val todayAdherencePercent = if (todayTotal > 0) ((todayTaken.toFloat() / todayTotal) * 100).toInt() else 100

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Main Adherence Score Card (Elegant Purple Container)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantPurpleContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MEDICATION ADHERENCE SCORE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantOnPurpleContainer,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(124.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { (overallAdherencePercent / 100f).coerceIn(0f, 1f) },
                        color = ElegantGreen,
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeWidth = 10.dp,
                        modifier = Modifier.size(116.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$overallAdherencePercent%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (overallAdherencePercent >= 90) "Optimal" else "Good Routine",
                            fontSize = 11.sp,
                            color = ElegantGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Formula: (Confirmed Taken Doses ÷ Total Scheduled Doses) × 100",
                    fontSize = 11.sp,
                    color = ElegantOnPurpleContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Today",
                value = "$todayAdherencePercent%",
                subtitle = "$todayTaken of $todayTotal taken",
                icon = Icons.Default.CheckCircle,
                color = ElegantGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Logged",
                value = "$totalDoses",
                subtitle = "$takenDoses confirmed",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = ElegantPurple,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Skipped Doses",
                value = "$skippedDoses",
                subtitle = "Logged by user",
                icon = Icons.Default.Close,
                color = ElegantAmber,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Missed Doses",
                value = "$missedDoses",
                subtitle = "Past scheduled time",
                icon = Icons.Default.Warning,
                color = ElegantRed,
                modifier = Modifier.weight(1f)
            )
        }

        // Target Outcomes for Pilot Validation Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = ElegantPurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Target Outcomes for Pilot Validation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ElegantTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Adherence Target (>90%): Current $overallAdherencePercent%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ElegantTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (overallAdherencePercent / 100f).coerceIn(0f, 1f) },
                    color = if (overallAdherencePercent >= 90) ElegantGreen else ElegantPurple,
                    trackColor = ElegantDarkOutline,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Missed Dose Reduction Target (70–80%): Pilot benchmark",
                    fontSize = 12.sp,
                    color = ElegantTextSecondary
                )
                Text(
                    text = "* Targets are intended for prospective healthcare pilot evaluations and not clinical certifications.",
                    fontSize = 11.sp,
                    color = ElegantTextMuted
                )
            }
        }

        // Gemini AI Adherence Summary Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElegantGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI Routine Review",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ElegantGreen
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.generateAdherenceSummaryWithAi(takenDoses, totalDoses, missedDoses + skippedDoses)
                        },
                        enabled = !aiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen, contentColor = Color(0xFF18320C)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("generate_ai_adherence_btn")
                    ) {
                        if (aiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Generate Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!aiState.summaryText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = ElegantDarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = aiState.summaryText!!,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = ElegantTextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        MedicalDisclaimerCard()
        Spacer(modifier = Modifier.height(80.dp))
    }
}
