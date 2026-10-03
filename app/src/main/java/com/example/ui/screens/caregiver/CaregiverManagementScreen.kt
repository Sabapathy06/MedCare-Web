package com.example.ui.screens.caregiver

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CaregiverRelationshipEntity
import com.example.data.local.entity.DoseRecordEntity
import com.example.ui.components.DoseStatusBadge
import com.example.ui.components.EmptyStateView
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
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.viewmodel.MedCareViewModel

@Composable
fun CaregiverManagementScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val caregivers by viewModel.caregivers.collectAsState()
    val caregiverPatients by viewModel.caregiverPatients.collectAsState()
    val selectedPatientId by viewModel.selectedPatientId.collectAsState()
    val selectedPatientDoses by viewModel.selectedPatientDoses.collectAsState()
    val selectedPatientMeds by viewModel.selectedPatientMeds.collectAsState()

    var showAddCaregiverDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(if (currentUser?.role == "CAREGIVER") "Monitor Patients" else "My Caregivers") }

    LaunchedEffect(caregiverPatients) {
        if (selectedPatientId == null && caregiverPatients.isNotEmpty()) {
            viewModel.selectPatientForCaregiver(caregiverPatients.first().patientId)
        }
    }

    if (showAddCaregiverDialog) {
        AddCaregiverDialog(
            onDismiss = { showAddCaregiverDialog = false },
            onSave = { name, phone, relation ->
                viewModel.addCaregiver(name, phone, relation)
                showAddCaregiverDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Tab Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = if (selectedTab == "My Caregivers") ElegantPurple else ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedTab == "My Caregivers") ElegantPurple else ElegantDarkOutline),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = "My Caregivers" }
            ) {
                Text(
                    text = "My Caregivers",
                    color = if (selectedTab == "My Caregivers") ElegantOnPrimary else ElegantTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                color = if (selectedTab == "Monitor Patients") ElegantPurple else ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedTab == "Monitor Patients") ElegantPurple else ElegantDarkOutline),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = "Monitor Patients" }
            ) {
                Text(
                    text = "Remote Monitor",
                    color = if (selectedTab == "Monitor Patients") ElegantOnPrimary else ElegantTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == "My Caregivers") {
            // Patient's linked caregivers
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantPurpleContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = ElegantPurple)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Caregiver Safety Network", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Linked caregivers receive immediate push notifications for missed doses and emergency SOS alerts with full adherence visibility.",
                                fontSize = 12.sp,
                                color = ElegantOnPurpleContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                if (caregivers.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No linked caregivers",
                            subtitle = "Add a family member or nurse to keep them updated on your medication progress.",
                            icon = Icons.Default.People,
                            actionLabel = "+ Connect Caregiver",
                            onAction = { showAddCaregiverDialog = true }
                        )
                    }
                } else {
                    items(caregivers, key = { it.id }) { rel ->
                        CaregiverCard(
                            relationship = rel,
                            onDelete = { viewModel.removeCaregiver(rel.id) }
                        )
                    }

                    item {
                        Button(
                            onClick = { showAddCaregiverDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("add_caregiver_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect Another Caregiver", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        } else {
            // Caregiver Remote Monitoring View
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Monitored Patients",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElegantTextPrimary
                    )
                }

                // Patient selection chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(caregiverPatients) { rel ->
                            val isSelected = selectedPatientId == rel.patientId
                            Surface(
                                color = if (isSelected) ElegantGreen else ElegantDarkSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ElegantGreen else ElegantDarkOutline),
                                modifier = Modifier
                                    .clickable { viewModel.selectPatientForCaregiver(rel.patientId) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF18320C) else ElegantTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rel.patientName,
                                        color = if (isSelected) Color(0xFF18320C) else ElegantTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                val totalDoses = selectedPatientDoses.size
                val takenDoses = selectedPatientDoses.count { it.status == "TAKEN" }
                val missedDoses = selectedPatientDoses.count { it.status == "MISSED" }
                val adherence = if (totalDoses > 0) ((takenDoses.toFloat() / totalDoses) * 100).toInt() else 100

                // Patient Overview Card
                item {
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
                                Column {
                                    Text(
                                        text = "Today's Remote Status",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = ElegantTextPrimary
                                    )
                                    Text(
                                        text = "$takenDoses of $totalDoses doses taken • $missedDoses missed",
                                        fontSize = 12.sp,
                                        color = ElegantTextSecondary
                                    )
                                }
                                Surface(
                                    color = if (adherence >= 80) ElegantGreenContainer else ElegantAmberContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "$adherence% Adherence",
                                        fontWeight = FontWeight.Bold,
                                        color = if (adherence >= 80) ElegantGreen else ElegantAmber,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (adherence / 100f).coerceIn(0f, 1f) },
                                color = ElegantGreen,
                                trackColor = ElegantDarkOutline,
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }

                // Doses Breakdown
                item {
                    Text("Patient's Doses Today", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextPrimary)
                }

                if (selectedPatientDoses.isEmpty()) {
                    item {
                        Text("No scheduled doses for selected patient today.", fontSize = 13.sp, color = ElegantTextMuted)
                    }
                } else {
                    items(selectedPatientDoses, key = { it.id }) { dose ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(dose.medicineName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextPrimary)
                                    Text("${dose.dosage} ${dose.dosageUnit} at ${dose.scheduledTime}", fontSize = 12.sp, color = ElegantTextSecondary)
                                }
                                DoseStatusBadge(status = dose.status)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun CaregiverCard(
    relationship: CaregiverRelationshipEntity,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElegantPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ElegantPurple)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(relationship.caregiverName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextPrimary)
                        Text("${relationship.relationshipType} • ${relationship.caregiverPhone}", fontSize = 12.sp, color = ElegantTextSecondary)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ElegantRed.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("✓ Active Permissions: Dose Tracking, Missed Alerts, SOS Dispatch", fontSize = 11.sp, color = ElegantGreen, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun AddCaregiverDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, relation: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+1 (555) 345-6789") }
    var relation by remember { mutableStateOf("Daughter") }

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
        title = { Text("Connect Caregiver", fontWeight = FontWeight.Bold, color = ElegantTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Caregiver Name *") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("caregiver_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Relationship (e.g., Daughter, Son, Nurse, Spouse)") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, phone, relation)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary)
            ) {
                Text("Connect", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ElegantTextSecondary) }
        }
    )
}
