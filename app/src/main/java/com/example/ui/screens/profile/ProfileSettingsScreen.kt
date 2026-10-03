package com.example.ui.screens.profile

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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreen
import com.example.ui.theme.ElegantOnPrimary
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
fun ProfileSettingsScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val scope = rememberCoroutineScope()

    var showSwitchUserDialog by remember { mutableStateOf(false) }

    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var caregiverAlertsEnabled by remember { mutableStateOf(true) }

    if (showSwitchUserDialog) {
        AlertDialog(
            containerColor = ElegantDarkSurface,
            onDismissRequest = { showSwitchUserDialog = false },
            title = { Text("Switch Active Profile", fontWeight = FontWeight.Bold, color = ElegantTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allUsers.forEach { user ->
                        val isCurrent = user.userId == currentUser?.userId
                        Surface(
                            color = if (isCurrent) ElegantPurpleContainer else ElegantDarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchUser(user.userId)
                                    showSwitchUserDialog = false
                                }
                                .border(1.dp, if (isCurrent) ElegantPurple else ElegantDarkOutline, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ElegantTextPrimary)
                                    Text(user.email, fontSize = 12.sp, color = ElegantTextSecondary)
                                }
                                if (isCurrent) {
                                    Text("ACTIVE", color = ElegantPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchUserDialog = false }) { Text("Close", color = ElegantTextSecondary) }
            }
        )
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Profile Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(ElegantPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ElegantOnPrimary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = currentUser?.fullName ?: "User Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Role: ${currentUser?.role ?: "PATIENT"}",
                                fontSize = 13.sp,
                                color = ElegantTeal
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showSwitchUserDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("switch_profile_btn")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch", color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📧 ${currentUser?.email ?: "user@medcare.app"}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        Text("📞 ${currentUser?.phone ?: "+1 (555) 234-5678"}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        Text("🚨 Emergency: ${currentUser?.emergencyContactName} (${currentUser?.emergencyContactPhone})", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                        Text("🌐 Language: ${currentUser?.preferredLanguage ?: "English"}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                }
            }
        }

        // Reminders & Alarm Preferences
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Reminder Preferences", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextPrimary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = ElegantPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audible Dose Reminders", fontSize = 14.sp, color = ElegantTextPrimary)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElegantPurple,
                            uncheckedThumbColor = ElegantTextSecondary,
                            uncheckedTrackColor = ElegantDarkSurfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = ElegantTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Haptic / Vibration Alerts", fontSize = 14.sp, color = ElegantTextPrimary)
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElegantPurple,
                            uncheckedThumbColor = ElegantTextSecondary,
                            uncheckedTrackColor = ElegantDarkSurfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = ElegantTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simple Mode (Elderly-Friendly)", fontSize = 14.sp, color = ElegantTextPrimary)
                    }
                    Switch(
                        checked = currentUser?.isSimpleModeEnabled ?: false,
                        onCheckedChange = { viewModel.toggleSimpleMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElegantPurple,
                            uncheckedThumbColor = ElegantTextSecondary,
                            uncheckedTrackColor = ElegantDarkSurfaceVariant
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = ElegantGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto-Notify Caregivers on Missed Dose", fontSize = 14.sp, color = ElegantTextPrimary)
                    }
                    Switch(
                        checked = caregiverAlertsEnabled,
                        onCheckedChange = { caregiverAlertsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElegantPurple,
                            uncheckedThumbColor = ElegantTextSecondary,
                            uncheckedTrackColor = ElegantDarkSurfaceVariant
                        )
                    )
                }
            }
        }

        // Data & Privacy / Sync
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = ElegantTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cloud Backup & Sync", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextPrimary)
                }

                Text(
                    "Your medicine data is automatically backed up to MedCare Cloud. This allows you to restore it if you switch phones or reinstall the app.",
                    fontSize = 13.sp,
                    color = ElegantTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Trigger sync manually */ },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BACKUP NOW", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { 
                            val uid = currentUser?.userId
                            if (uid != null) {
                                scope.launch {
                                    viewModel.repository.syncDataFromFirestore(uid)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RESTORE DATA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        MedicalDisclaimerCard()

        // Logout
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = ElegantRedContainer, contentColor = ElegantRed),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, ElegantRed.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("LOG OUT / SIGN OUT", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

