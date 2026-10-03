package com.example.ui.screens.sustainability

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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatCard
import com.example.ui.theme.ElegantAmber
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreen
import com.example.ui.theme.ElegantGreenContainer
import com.example.ui.theme.ElegantOnGreenContainer
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantPurple
import com.example.ui.theme.ElegantPurpleContainer
import com.example.ui.theme.ElegantRed
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.viewmodel.MedCareViewModel

@Composable
fun SustainabilityImpactScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val allDoses by viewModel.allDoseHistory.collectAsState()
    val refills by viewModel.refills.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val totalDoses = allDoses.size
    val takenDoses = allDoses.count { it.status == "TAKEN" }
    val adherencePercent = if (totalDoses > 0) ((takenDoses.toFloat() / totalDoses) * 100).toInt() else 100

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Hero Header Card: TRL-4 & Pilot Target
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantPurpleContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUSTAINABILITY & PILOT METRICS",
                        color = ElegantTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        color = ElegantGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = ElegantGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TRL-4 Lab Validated", color = ElegantGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Five-Dimensional Sustainability Impact Framework",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "MedCare bridges clinical adherence, caregiver collaboration, and responsible medication management for long-term health outcomes.",
                    fontSize = 12.sp,
                    color = ElegantTextSecondary
                )
            }
        }

        // Live Real-Time Pilot Impact Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Adherence Rate",
                value = "$adherencePercent%",
                subtitle = "Target: >90%",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = ElegantGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Doses Logged",
                value = "$totalDoses",
                subtitle = "$takenDoses verified taken",
                icon = Icons.Default.Shield,
                color = ElegantPurple,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Refills Tracked",
                value = "${refills.size}",
                subtitle = "Stock depletion avoided",
                icon = Icons.Default.Eco,
                color = ElegantTeal,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Alerts Triggered",
                value = "${notifications.size}",
                subtitle = "Proactive intervention",
                icon = Icons.Default.People,
                color = ElegantAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Five Sustainability Dimensions",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = ElegantTextPrimary
        )

        // 1. Human Dimension (45%)
        DimensionCard(
            title = "1. Human Dimension (45%)",
            subtitle = "Cognitive Relief & Quality of Life",
            description = "Reduces cognitive burden on chronic care and elderly patients by automating schedule reminders and simplifying multi-drug instructions.",
            percentage = 45,
            icon = Icons.Default.Favorite,
            accentColor = Color(0xFFF43F5E)
        )

        // 2. Social Dimension (25%)
        DimensionCard(
            title = "2. Social Dimension (25%)",
            subtitle = "Caregiver Transparency & Burnout Reduction",
            description = "Enables family caregivers and nursing staff to monitor doses remotely, reducing caregiver stress and missed-dose panic.",
            percentage = 25,
            icon = Icons.Default.Diversity3,
            accentColor = Color(0xFF38BDF8)
        )

        // 3. Economic Dimension (15%)
        DimensionCard(
            title = "3. Economic Dimension (15%)",
            subtitle = "Hospitalization & Readmission Prevention",
            description = "Preventable medication non-adherence costs billions in emergency admissions. Consistent adherence preserves patient independence and avoids costly emergency visits.",
            percentage = 15,
            icon = Icons.Default.AttachMoney,
            accentColor = ElegantGreen
        )

        // 4. Environmental Dimension (10%)
        DimensionCard(
            title = "4. Environmental Dimension (10%)",
            subtitle = "Responsible Disposal & Zero Waste",
            description = "Active expiry tracking guides users to safely return unused medicines to pharmacy take-back kiosks, avoiding chemical water contamination.",
            percentage = 10,
            icon = Icons.Default.Eco,
            accentColor = ElegantTeal
        )

        // 5. Cultural Dimension (5%)
        DimensionCard(
            title = "5. Cultural Dimension (5%)",
            subtitle = "Health Literacy & Multi-Language Equity",
            description = "Empowers non-English speaking elderly patients with multi-language AI translations in regional dialects to ensure safe medicine consumption.",
            percentage = 5,
            icon = Icons.Default.Language,
            accentColor = ElegantPurple
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DimensionCard(
    title: String,
    subtitle: String,
    description: String,
    percentage: Int,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextPrimary)
                        Text(subtitle, fontSize = 12.sp, color = ElegantTextSecondary)
                    }
                }
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$percentage% Weight",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = ElegantTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { percentage / 50f },
                color = accentColor,
                trackColor = ElegantDarkOutline,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )
        }
    }
}
