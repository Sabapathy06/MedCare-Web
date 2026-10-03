package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MedicationEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.DoseRecordEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MedCareViewModel

@Composable
fun PatientDashboardScreen(
    viewModel: MedCareViewModel,
    onNavigateToMedications: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdherence: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToSos: () -> Unit,
    onNavigateToCaregiver: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val todayDoses by viewModel.todayDoses.collectAsState()
    val medications by viewModel.userMedications.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val caregivers by viewModel.caregivers.collectAsState()

    var showSosDialog by remember { mutableStateOf(false) }
    var refillMedication by remember { mutableStateOf<MedicationEntity?>(null) }

    // Computations
    val totalTodayDoses = todayDoses.size
    val takenTodayDoses = todayDoses.count { it.status == "TAKEN" }
    val skippedTodayDoses = todayDoses.count { it.status == "SKIPPED" }
    val adherencePercent = if (totalTodayDoses > 0) ((takenTodayDoses.toFloat() / totalTodayDoses) * 100).toInt() else 100

    val nextUpcomingDose = todayDoses.firstOrNull { it.status == "UPCOMING" || it.status == "SNOOZED" }
    val lowStockMeds = medications.filter { it.stockQuantity <= it.minimumStock }
    val expiringMeds = medications.filter {
        val days = (it.expiryDateMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
        days <= 30
    }
    val nearestAppt = appointments.firstOrNull { !it.isCompleted }

    if (showSosDialog) {
        SosEmergencyDialog(
            onConfirm = {
                showSosDialog = false
                viewModel.triggerSosEmergency()
            },
            onDismiss = { showSosDialog = false }
        )
    }

    if (refillMedication != null) {
        val med = refillMedication!!
        RefillDialog(
            medicineName = med.medicineName,
            currentStock = med.stockQuantity,
            onConfirm = { qty, pharmacy, cost, notes ->
                viewModel.addRefill(med.id, med.medicineName, qty, pharmacy, cost, notes)
                refillMedication = null
            },
            onDismiss = { refillMedication = null }
        )
    }

    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..20 -> "Good evening,"
        else -> "Good night,"
    }

    val isSimpleMode = currentUser?.isSimpleModeEnabled ?: false
    val userAge by viewModel.currentUserAge.collectAsState()

    if (isSimpleMode) {
        SimpleDashboardContent(
            viewModel = viewModel,
            currentUser = currentUser,
            todayDoses = todayDoses,
            onNavigateToScanner = onNavigateToScanner,
            onNavigateToSos = onNavigateToSos,
            onNavigateToMedications = onNavigateToMedications,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToCaregiver = onNavigateToCaregiver,
            onNavigateToProfile = onNavigateToProfile
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(ElegantDarkBackground)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = greeting, fontSize = 18.sp, color = ElegantTextSecondary, fontWeight = FontWeight.Medium)
                    Text(text = "${currentUser?.fullName ?: "User"}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = ElegantTextPrimary)
                    if (userAge > 0) {
                        Text(text = "Age: $userAge", fontSize = 14.sp, color = ElegantPurple, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Here's what you need to do today.", fontSize = 16.sp, color = ElegantPurple, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantPurpleContainer),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToAdherence() }
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Text(text = "$adherencePercent%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Today's Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "$takenTodayDoses of $totalTodayDoses doses taken", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionTile("Meds", Icons.Default.LocalPharmacy, onClick = onNavigateToMedications, modifier = Modifier.weight(1f))
                    QuickActionTile("Scan", Icons.Default.CameraAlt, tint = ElegantTeal, onClick = onNavigateToScanner, modifier = Modifier.weight(1f))
                    QuickActionTile("AI Bot", Icons.Default.AutoAwesome, onClick = onNavigateToAi, modifier = Modifier.weight(1f))
                    QuickActionTile("Visits", Icons.Default.CalendarMonth, onClick = onNavigateToAppointments, modifier = Modifier.weight(1f))
                    QuickActionTile("SOS", Icons.Default.Warning, tint = ElegantRed, onClick = onNavigateToSos, modifier = Modifier.weight(1f))
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Today's Doses", "$takenTodayDoses / $totalTodayDoses", "$skippedTodayDoses skipped", Icons.Default.CheckCircle, ElegantGreen, Modifier.weight(1f), onNavigateToHistory)
                    StatCard("Active Meds", "${medications.count { it.isActive }}", "${lowStockMeds.size} low stock", Icons.Default.LocalPharmacy, ElegantPurple, Modifier.weight(1f), onNavigateToMedications)
                }
            }

            if (lowStockMeds.isNotEmpty()) item { StockAlertBanner(lowStockMeds, { refillMedication = it }) }
            if (expiringMeds.isNotEmpty()) item { ExpiryAlertBanner(expiringMeds) }

            item {
                if (nextUpcomingDose != null) {
                    DoseActionCard(dose = nextUpcomingDose, onTake = { viewModel.takeDose(nextUpcomingDose.id) }, onSkip = { viewModel.skipDose(nextUpcomingDose.id) }, onSnooze = { viewModel.snoozeDose(nextUpcomingDose.id) }, isNextMedicationHighlight = true)
                } else if (todayDoses.isNotEmpty()) {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ElegantGreenContainer), border = androidx.compose.foundation.BorderStroke(1.dp, ElegantGreen.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantGreen, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("All scheduled doses completed!", fontWeight = FontWeight.Bold, color = ElegantGreen, fontSize = 15.sp)
                                Text("Great job keeping up with your medication routine today.", fontSize = 12.sp, color = ElegantTextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Your Schedule", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ElegantTextPrimary)
                    TextButton(onClick = onNavigateToHistory) { Text("See All", color = ElegantPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                }
            }

            if (todayDoses.isEmpty()) {
                item { EmptyStateView("No medications scheduled today", "Add a medicine or configure daily frequency to start tracking doses.", Icons.Default.LocalPharmacy, "+ Add Medicine", onNavigateToMedications) }
            } else {
                items(todayDoses, key = { it.id }) { dose ->
                    DoseActionCard(dose = dose, onTake = { viewModel.takeDose(dose.id) }, onSkip = { viewModel.skipDose(dose.id) }, onSnooze = { viewModel.snoozeDose(dose.id) }, isNextMedicationHighlight = false)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface), border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline), modifier = Modifier.weight(1f).clickable { onNavigateToAppointments() }) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ElegantTeal, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Next Visit", fontSize = 12.sp, color = ElegantTextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (nearestAppt != null) {
                                Text(nearestAppt.doctorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, color = ElegantTextPrimary)
                                Text("${nearestAppt.date} • ${nearestAppt.time}", fontSize = 11.sp, color = ElegantTeal)
                            } else {
                                Text("No visits scheduled", fontSize = 12.sp, color = ElegantTextMuted)
                            }
                        }
                    }
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface), border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, contentDescription = null, tint = ElegantGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Caregiver", fontSize = 12.sp, color = ElegantTextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (caregivers.isNotEmpty()) {
                                Text(caregivers.first().caregiverName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, color = ElegantTextPrimary)
                                Text("Connected & Synced", fontSize = 11.sp, color = ElegantGreen)
                            } else {
                                Text("Not connected", fontSize = 12.sp, color = ElegantTextMuted)
                            }
                        }
                    }
                }
            }

            item { MedicalDisclaimerCard() }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SimpleDashboardContent(
    viewModel: MedCareViewModel,
    currentUser: UserEntity?,
    todayDoses: List<DoseRecordEntity>,
    onNavigateToScanner: () -> Unit,
    onNavigateToSos: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCaregiver: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val age = viewModel.currentUserAge.collectAsState().value
        Text(
            text = "Hello, ${currentUser?.fullName?.split(" ")?.firstOrNull() ?: "User"}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )
        if (age > 0) {
            Text(
                text = "Age: $age",
                fontSize = 18.sp,
                color = ElegantPurpleLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start).offset(y = (-12).dp)
            )
        }

        // Giant SOS Button
        Button(
            onClick = onNavigateToSos,
            colors = ButtonDefaults.buttonColors(containerColor = ElegantRed),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(100.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("EMERGENCY SOS", fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }

        // Large Today's Meds Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantPurple),
            modifier = Modifier.fillMaxWidth().clickable { onNavigateToHistory() }
        ) {
            val remaining = todayDoses.count { it.status == "UPCOMING" || it.status == "SNOOZED" }
            Column(modifier = Modifier.padding(24.dp)) {
                Text("TODAY'S MEDICINES", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (remaining > 0) "$remaining doses remaining" else "All doses taken!",
                    color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToHistory,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ElegantPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VIEW SCHEDULE", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Scan Medicine Large Button
        Button(
            onClick = onNavigateToScanner,
            colors = ButtonDefaults.buttonColors(containerColor = ElegantTeal),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(80.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("SCAN NEW MEDICINE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // My Meds Large Button
        OutlinedButton(
            onClick = onNavigateToMedications,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, ElegantPurple),
            modifier = Modifier.fillMaxWidth().height(80.dp)
        ) {
            Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = ElegantPurple, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("MY MEDICINE LIST", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Caregiver
            Surface(
                color = ElegantDarkSurface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, ElegantGreen.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f).height(80.dp).clickable { onNavigateToCaregiver() }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.People, contentDescription = null, tint = ElegantGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CAREGIVER", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
                }
            }

            // Profile
            Surface(
                color = ElegantDarkSurface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, ElegantDarkOutline),
                modifier = Modifier.weight(1f).height(80.dp).clickable { onNavigateToProfile() }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = ElegantTextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PROFILE", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = ElegantPurple,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ElegantDarkSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElegantTextSecondary,
                maxLines = 1
            )
        }
    }
}
