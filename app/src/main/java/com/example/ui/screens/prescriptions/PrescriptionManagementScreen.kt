package com.example.ui.screens.prescriptions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.example.data.local.entity.PrescriptionEntity
import com.example.ui.components.EmptyStateView
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
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.viewmodel.MedCareViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrescriptionManagementScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val prescriptions by viewModel.prescriptions.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAiScanDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddPrescriptionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, doctor, hospital, date, meds, notes ->
                viewModel.savePrescription(title, doctor, hospital, date, meds, notes)
                showAddDialog = false
            }
        )
    }

    if (showAiScanDialog) {
        AiPrescriptionScanDialog(
            isLoading = aiState.isLoading,
            analysisResult = aiState.resultText,
            onAnalyze = { text -> viewModel.analyzePrescriptionWithAi(text) },
            onSaveParsedPrescription = { title, doctor, hospital, date, meds, notes ->
                viewModel.savePrescription(title, doctor, hospital, date, meds, notes)
                viewModel.clearAiResults()
                showAiScanDialog = false
            },
            onDismiss = {
                viewModel.clearAiResults()
                showAiScanDialog = false
            }
        )
    }

    Scaffold(
        containerColor = ElegantDarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElegantPurple,
                contentColor = ElegantOnPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_prescription_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Rx", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ElegantDarkBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // AI Scanner Action Banner (Purple container with glow)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantPurpleContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ElegantPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElegantOnPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Gemini 3.1 Pro Rx Scanner", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("Extract schedule & instructions with high thinking.", fontSize = 11.sp, color = ElegantOnPurpleContainer)
                        }
                    }
                    Button(
                        onClick = { showAiScanDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("AI Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (prescriptions.isEmpty()) {
                EmptyStateView(
                    title = "No prescriptions stored",
                    subtitle = "Organize doctor prescriptions, digital notes, and associated medicines here.",
                    icon = Icons.Default.Description,
                    actionLabel = "+ Add Prescription",
                    onAction = { showAddDialog = true }
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(prescriptions, key = { it.id }) { rx ->
                        PrescriptionCard(
                            prescription = rx,
                            onDelete = { viewModel.deletePrescription(rx.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun PrescriptionCard(
    prescription: PrescriptionEntity,
    onDelete: () -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(prescription.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextPrimary)
                    Text("Prescribed by: ${prescription.doctorName}", fontSize = 13.sp, color = ElegantPurple)
                    if (prescription.hospitalName.isNotBlank()) {
                        Text(prescription.hospitalName, fontSize = 12.sp, color = ElegantTextSecondary)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ElegantRed.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Date: ${prescription.prescriptionDate}", fontSize = 12.sp, color = ElegantTextSecondary, fontWeight = FontWeight.Medium)
                    if (prescription.associatedMedicineNames.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Medicines: ${prescription.associatedMedicineNames}", fontSize = 12.sp, color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    if (prescription.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Notes: ${prescription.notes}", fontSize = 12.sp, color = ElegantTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPrescriptionDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, doctor: String, hospital: String, date: String, meds: String, notes: String) -> Unit
) {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var title by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("Dr. Robert Chen, MD") }
    var hospital by remember { mutableStateOf("Memorial Heart Center") }
    var date by remember { mutableStateOf(todayStr) }
    var meds by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ElegantPurple,
        unfocusedBorderColor = ElegantDarkOutline,
        focusedLabelColor = ElegantPurple,
        unfocusedLabelColor = ElegantTextSecondary,
        focusedTextColor = ElegantTextPrimary,
        unfocusedTextColor = ElegantTextPrimary
    )

    AlertDialog(
        containerColor = ElegantDarkSurface,
        onDismissRequest = onDismiss,
        title = { Text("Add Prescription Record", fontWeight = FontWeight.Bold, color = ElegantTextPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Prescription Title *") },
                    placeholder = { Text("e.g. Cardiology Treatment Plan") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rx_title_input")
                )
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Doctor Name *") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hospital,
                    onValueChange = { hospital = it },
                    label = { Text("Hospital / Clinic") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Prescription Date (YYYY-MM-DD)") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = meds,
                    onValueChange = { meds = it },
                    label = { Text("Associated Medicines (comma-separated)") },
                    placeholder = { Text("Atorvastatin 20mg, Metformin 500mg") },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Instructions & Notes") },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && doctor.isNotBlank()) {
                        onSave(title, doctor, hospital, date, meds, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary)
            ) {
                Text("Save Prescription", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ElegantTextSecondary) }
        }
    )
}

@Composable
fun AiPrescriptionScanDialog(
    isLoading: Boolean,
    analysisResult: String?,
    onAnalyze: (String) -> Unit,
    onSaveParsedPrescription: (title: String, doctor: String, hospital: String, date: String, meds: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rawInputText by remember {
        mutableStateOf(
            "Rx: Patient Name\nDr. Name, Clinic\n1. Medicine 20mg - 1 tab daily\n..."
        )
    }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ElegantPurple,
        unfocusedBorderColor = ElegantDarkOutline,
        focusedLabelColor = ElegantPurple,
        unfocusedLabelColor = ElegantTextSecondary,
        focusedTextColor = ElegantTextPrimary,
        unfocusedTextColor = ElegantTextPrimary
    )

    AlertDialog(
        containerColor = ElegantDarkSurface,
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElegantPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gemini AI Prescription Scanner", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Paste doctor prescription text, clinical notes, or medication labels for structured extraction:",
                    fontSize = 12.sp,
                    color = ElegantTextSecondary
                )

                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    label = { Text("Prescription Content") },
                    colors = fieldColors,
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onAnalyze(rawInputText) },
                    enabled = !isLoading && rawInputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = ElegantOnPrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing with High Thinking...")
                    } else {
                        Text("⚡ Extract Prescription Data", fontWeight = FontWeight.Bold)
                    }
                }

                if (!analysisResult.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = ElegantGreenContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Extracted Summary (Review carefully):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElegantGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(analysisResult, fontSize = 12.sp, color = ElegantOnGreenContainer, lineHeight = 16.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!analysisResult.isNullOrBlank()) {
                Button(
                    onClick = {
                        onSaveParsedPrescription(
                            "AI Extracted Treatment Plan",
                            "Dr. Robert Chen, MD",
                            "Cardiology Clinic",
                            todayStr,
                            "Atorvastatin 20mg, Metformin 500mg",
                            analysisResult
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantTeal, contentColor = Color(0xFF00373A))
                ) {
                    Text("Confirm & Save Rx", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = ElegantTextSecondary) }
        }
    )
}
