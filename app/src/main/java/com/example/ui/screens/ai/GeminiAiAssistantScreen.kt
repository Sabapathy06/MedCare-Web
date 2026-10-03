package com.example.ui.screens.ai

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.ElegantAmber
import com.example.ui.theme.ElegantAmberContainer
import com.example.ui.theme.ElegantBlue
import com.example.ui.theme.ElegantBlueContainer
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreen
import com.example.ui.theme.ElegantGreenContainer
import com.example.ui.theme.ElegantOnGreenContainer
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantOnPurpleContainer
import com.example.ui.theme.ElegantOnRedContainer
import com.example.ui.theme.ElegantPurple
import com.example.ui.theme.ElegantPurpleContainer
import com.example.ui.theme.ElegantRed
import com.example.ui.theme.ElegantRedContainer
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTealContainer
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.viewmodel.MedCareViewModel

@Composable
fun GeminiAiAssistantScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val medications by viewModel.userMedications.collectAsState()
    val allDoses by viewModel.allDoseHistory.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    var selectedTool by remember { mutableStateOf("Safety & Interactions") }
    val tools = listOf("Safety & Interactions", "Rx Schedule Extractor", "Multi-Language Explainer", "Adherence Review")

    // Input States
    var customMedListText by remember {
        mutableStateOf(
            if (medications.isNotEmpty()) medications.joinToString(", ") { "${it.medicineName} ${it.dosage}${it.dosageUnit}" }
            else "Atorvastatin 20mg, Metformin 500mg, Lisinopril 10mg"
        )
    }

    var prescriptionInputText by remember {
        mutableStateOf("Rx: Dr. Robert Chen\n1. Atorvastatin 20mg - 1 tab at bedtime (for cholesterol)\n2. Metformin 500mg - 1 tab twice daily with meals (breakfast & dinner)\nTake with full glass of water.")
    }

    var textToTranslate by remember {
        mutableStateOf("Take 1 tablet twice daily with food. Avoid consuming grapefruit juice or high fat meals.")
    }
    var targetLanguage by remember { mutableStateOf("Spanish") }
    val languages = listOf("Spanish", "Hindi", "Tamil", "Telugu", "French", "German", "Mandarin")

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ElegantPurple,
        unfocusedBorderColor = ElegantDarkOutline,
        focusedLabelColor = ElegantPurple,
        unfocusedLabelColor = ElegantTextSecondary,
        focusedTextColor = ElegantTextPrimary,
        unfocusedTextColor = ElegantTextPrimary
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // AI Header Card with High Thinking Badge (Purple Container)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantPurpleContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ElegantPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElegantOnPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Gemini 3.1 Pro Assistant", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("Clinical High Thinking Mode", fontSize = 12.sp, color = ElegantOnPurpleContainer)
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = ElegantPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("High Thinking", color = ElegantPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Clinical-grade reasoning on drug interactions, prescription schedules, and multi-language patient education.",
                    fontSize = 12.sp,
                    color = ElegantOnPurpleContainer.copy(alpha = 0.85f)
                )
            }
        }

        // Tool Selector Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tools) { tool ->
                val isSelected = selectedTool == tool
                Surface(
                    color = if (isSelected) ElegantPurple else ElegantDarkSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ElegantPurple else ElegantDarkOutline),
                    modifier = Modifier.clickable { selectedTool = tool }
                ) {
                    Text(
                        text = tool,
                        color = if (isSelected) ElegantOnPrimary else ElegantTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Active Tool Card
        when (selectedTool) {
            "Safety & Interactions" -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = ElegantPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Drug-Drug & Meal Interaction Checker", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextPrimary)
                        }
                        Text(
                            "Enter multiple medications to check for known pharmacological interactions, timing precautions, and dietary contraindications.",
                            fontSize = 12.sp,
                            color = ElegantTextSecondary
                        )

                        OutlinedTextField(
                            value = customMedListText,
                            onValueChange = { customMedListText = it },
                            label = { Text("Medications (comma-separated)") },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("ai_med_list_input")
                        )

                        Button(
                            onClick = {
                                val meds = customMedListText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                viewModel.checkDrugInteractionsWithAi(meds)
                            },
                            enabled = !aiState.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("run_interaction_check_btn")
                        ) {
                            if (aiState.isLoading) {
                                CircularProgressIndicator(color = ElegantOnPrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing Pharmacological Profiles...")
                            } else {
                                Text("Run Interaction Analysis", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!aiState.interactionText.isNullOrBlank()) {
                            Surface(
                                color = ElegantDarkSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Interaction Analysis Report:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ElegantPurple)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(aiState.interactionText!!, fontSize = 13.sp, lineHeight = 18.sp, color = ElegantTextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            "Rx Schedule Extractor" -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = ElegantTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prescription Text & Schedule Parsing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextPrimary)
                        }

                        OutlinedTextField(
                            value = prescriptionInputText,
                            onValueChange = { prescriptionInputText = it },
                            label = { Text("Clinical Rx Text") },
                            colors = textFieldColors,
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("ai_rx_input")
                        )

                        Button(
                            onClick = { viewModel.analyzePrescriptionWithAi(prescriptionInputText) },
                            enabled = !aiState.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantTeal, contentColor = Color(0xFF00373A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("run_rx_extract_btn")
                        ) {
                            if (aiState.isLoading) {
                                CircularProgressIndicator(color = Color(0xFF00373A), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reasoning with Gemini 3.1 Pro...")
                            } else {
                                Text("Extract Medicine Schedule", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!aiState.resultText.isNullOrBlank()) {
                            Surface(
                                color = ElegantDarkSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantTeal.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Structured Output:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ElegantTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(aiState.resultText!!, fontSize = 13.sp, lineHeight = 18.sp, color = ElegantTextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            "Multi-Language Explainer" -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = ElegantPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Multilingual Patient Instructions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextPrimary)
                        }

                        OutlinedTextField(
                            value = textToTranslate,
                            onValueChange = { textToTranslate = it },
                            label = { Text("Instructions to Translate") },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Select Target Language:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElegantTextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(languages) { lang ->
                                val isSelected = targetLanguage == lang
                                Surface(
                                    color = if (isSelected) ElegantPurple else ElegantDarkSurfaceVariant,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ElegantPurple else ElegantDarkOutline),
                                    modifier = Modifier.clickable { targetLanguage = lang }
                                ) {
                                    Text(
                                        text = lang,
                                        color = if (isSelected) ElegantOnPrimary else ElegantTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.translateInstructionsWithAi(textToTranslate, targetLanguage) },
                            enabled = !aiState.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("run_translate_btn")
                        ) {
                            if (aiState.isLoading) {
                                CircularProgressIndicator(color = ElegantOnPrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Translating...")
                            } else {
                                Text("Translate & Simplify ($targetLanguage)", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!aiState.translationText.isNullOrBlank()) {
                            Surface(
                                color = ElegantDarkSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Translated Instructions ($targetLanguage):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ElegantPurple)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(aiState.translationText!!, fontSize = 13.sp, lineHeight = 18.sp, color = ElegantTextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            "Adherence Review" -> {
                val total = allDoses.size
                val taken = allDoses.count { it.status == "TAKEN" }
                val missed = allDoses.count { it.status == "MISSED" } + allDoses.count { it.status == "SKIPPED" }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElegantGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Intelligent Routine & Lifestyle Feedback", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextPrimary)
                        }

                        Text(
                            "Current profile stats: $taken doses taken out of $total scheduled ($missed missed/skipped).",
                            fontSize = 13.sp,
                            color = ElegantTextSecondary
                        )

                        Button(
                            onClick = { viewModel.generateAdherenceSummaryWithAi(taken, total, missed) },
                            enabled = !aiState.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen, contentColor = Color(0xFF18320C)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("run_ai_summary_btn")
                        ) {
                            if (aiState.isLoading) {
                                CircularProgressIndicator(color = Color(0xFF18320C), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing Routine Trends...")
                            } else {
                                Text("Generate Routine Feedback", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!aiState.summaryText.isNullOrBlank()) {
                            Surface(
                                color = ElegantDarkSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Routine Feedback:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ElegantGreen)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(aiState.summaryText!!, fontSize = 13.sp, lineHeight = 18.sp, color = ElegantTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (aiState.error != null) {
            Surface(
                color = ElegantRedContainer,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantRed.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Notice: ${aiState.error}",
                    color = ElegantOnRedContainer,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        MedicalDisclaimerCard()
        Spacer(modifier = Modifier.height(24.dp))
    }
}
