package com.example.ui.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AppointmentEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreen
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantPurple
import com.example.ui.theme.ElegantRed
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.viewmodel.MedCareViewModel

@Composable
fun AppointmentManagementScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val appointments by viewModel.appointments.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val upcomingAppts = appointments.filter { !it.isCompleted }
    val pastAppts = appointments.filter { it.isCompleted }

    if (showAddDialog) {
        AddAppointmentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { doctor, specialty, hospital, date, time, reason, notes ->
                viewModel.saveAppointment(doctor, specialty, hospital, date, time, reason, notes)
                showAddDialog = false
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
                modifier = Modifier.testTag("add_appointment_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule Visit", fontWeight = FontWeight.Bold)
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

            if (appointments.isEmpty()) {
                EmptyStateView(
                    title = "No clinic visits scheduled",
                    subtitle = "Keep track of upcoming physician consultations and hospital checkups.",
                    icon = Icons.Default.CalendarMonth,
                    actionLabel = "+ Schedule First Visit",
                    onAction = { showAddDialog = true }
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (upcomingAppts.isNotEmpty()) {
                        item {
                            Text("Upcoming Appointments (${upcomingAppts.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextPrimary)
                        }
                        items(upcomingAppts, key = { it.id }) { appt ->
                            AppointmentCard(
                                appointment = appt,
                                onToggleComplete = { viewModel.toggleAppointmentCompleted(appt.id, true) },
                                onDelete = { viewModel.deleteAppointment(appt.id) }
                            )
                        }
                    }

                    if (pastAppts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Completed / Past Visits", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextSecondary)
                        }
                        items(pastAppts, key = { it.id }) { appt ->
                            AppointmentCard(
                                appointment = appt,
                                onToggleComplete = { viewModel.toggleAppointmentCompleted(appt.id, false) },
                                onDelete = { viewModel.deleteAppointment(appt.id) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    onToggleComplete: () -> Unit,
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Checkbox(
                        checked = appointment.isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ElegantGreen,
                            uncheckedColor = ElegantDarkOutline,
                            checkmarkColor = Color(0xFF18320C)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = appointment.doctorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ElegantTextPrimary
                        )
                        Text(
                            text = "${appointment.specialty} • ${appointment.date} at ${appointment.time}",
                            fontSize = 12.sp,
                            color = ElegantTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ElegantRed.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(appointment.hospitalName, fontSize = 12.sp, color = ElegantTextSecondary)
            }

            if (appointment.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Reason: ${appointment.reason}", fontSize = 12.sp, color = ElegantTextPrimary)
            }

            if (appointment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = ElegantDarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Prep notes: ${appointment.notes}", fontSize = 11.sp, color = ElegantTextMuted, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(
    onDismiss: () -> Unit,
    onSave: (doctor: String, specialty: String, hospital: String, date: String, time: String, reason: String, notes: String) -> Unit
) {
    var doctor by remember { mutableStateOf("Dr. Robert Chen, MD") }
    var specialty by remember { mutableStateOf("Cardiology") }
    var hospital by remember { mutableStateOf("Memorial Heart Center - Suite 402") }
    var date by remember { mutableStateOf("2026-08-20") }
    var time by remember { mutableStateOf("10:30") }
    var reason by remember { mutableStateOf("Quarterly BP & Lipid Followup") }
    var notes by remember { mutableStateOf("Bring recent fasting lab report.") }

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
        title = { Text("Schedule Doctor Appointment", fontWeight = FontWeight.Bold, color = ElegantTextPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Doctor Name *") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("appt_doctor_input")
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Specialty") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hospital,
                    onValueChange = { hospital = it },
                    label = { Text("Hospital / Clinic Location") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        colors = fieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (HH:mm)") },
                        colors = fieldColors,
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Visit") },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Preparation Notes (Optional)") },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (doctor.isNotBlank()) {
                        onSave(doctor, specialty, hospital, date, time, reason, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary)
            ) {
                Text("Schedule Visit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ElegantTextSecondary) }
        }
    )
}
