package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DoseRecordEntity
import com.example.data.local.entity.MedicationEntity
import com.example.ui.theme.ElegantAmber
import com.example.ui.theme.ElegantAmberContainer
import com.example.ui.theme.ElegantBlue
import com.example.ui.theme.ElegantBlueContainer
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantEmergencyRed
import com.example.ui.theme.ElegantGreen
import com.example.ui.theme.ElegantGreenContainer
import com.example.ui.theme.ElegantOnAmberContainer
import com.example.ui.theme.ElegantOnGreenContainer
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantOnPurpleContainer
import com.example.ui.theme.ElegantOnRedContainer
import com.example.ui.theme.ElegantPurple
import com.example.ui.theme.ElegantPurpleContainer
import com.example.ui.theme.ElegantPurpleLight
import com.example.ui.theme.ElegantRed
import com.example.ui.theme.ElegantRedContainer
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTealContainer
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DoseStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label, icon) = when (status.uppercase()) {
        "TAKEN" -> Quadruple(ElegantGreenContainer, ElegantGreen, "TAKEN", Icons.Default.Check)
        "SKIPPED" -> Quadruple(ElegantAmberContainer, ElegantAmber, "SKIPPED", Icons.Default.Close)
        "SNOOZED" -> Quadruple(ElegantBlueContainer, ElegantBlue, "SNOOZED", Icons.Default.Alarm)
        "MISSED" -> Quadruple(ElegantRedContainer, ElegantRed, "MISSED", Icons.Default.Warning)
        else -> Quadruple(ElegantDarkSurfaceVariant, ElegantTextSecondary, "UPCOMING", Icons.Default.AccessTime)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun DoseActionCard(
    dose: DoseRecordEntity,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier,
    isNextMedicationHighlight: Boolean = false
) {
    val isTaken = dose.status == "TAKEN"
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNextMedicationHighlight) Color(0xFF332D41) else ElegantDarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNextMedicationHighlight) 6.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isNextMedicationHighlight) Modifier.border(2.dp, ElegantPurple, RoundedCornerShape(24.dp))
                else Modifier.border(1.dp, ElegantDarkOutline, RoundedCornerShape(24.dp))
            )
            .testTag("dose_card_${dose.id}")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (isNextMedicationHighlight && !isTaken) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = ElegantPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEXT DOSE",
                        color = ElegantPurple,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTaken) ElegantGreenContainer
                                else if (isNextMedicationHighlight) ElegantPurpleContainer
                                else ElegantDarkSurfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = if (isTaken) ElegantGreen else ElegantPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = dose.medicineName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = ElegantTextPrimary
                        )
                        Text(
                            text = "${dose.dosage} ${dose.dosageUnit} • ${dose.scheduledTime}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = ElegantTextSecondary
                        )
                    }
                }
                DoseStatusBadge(status = dose.status)
            }

            if (!dose.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = ElegantDarkSurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ${dose.notes}",
                        fontSize = 13.sp,
                        color = ElegantTextSecondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Dose Action Buttons
            if (dose.status == "UPCOMING" || dose.status == "SNOOZED") {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTake,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(60.dp) // Large touch target
                            .testTag("take_button_${dose.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TAKEN", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantPurple),
                        border = androidx.compose.foundation.BorderStroke(2.dp, ElegantPurple.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .testTag("snooze_button_${dose.id}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("WAIT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantAmber),
                        border = androidx.compose.foundation.BorderStroke(2.dp, ElegantAmber.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .testTag("skip_button_${dose.id}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("SKIP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else if (isTaken) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = ElegantGreenContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Medicine taken correctly", color = ElegantGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpiryStatusBadge(expiryMillis: Long, modifier: Modifier = Modifier) {
    val daysToExpiry = ((expiryMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    
    val (bgColor, textColor, label, icon) = when {
        expiryMillis <= 0 -> Quadruple(ElegantDarkSurfaceVariant, ElegantTextSecondary, "UNKNOWN STATUS", Icons.Default.Info)
        daysToExpiry < 0 -> Quadruple(ElegantRedContainer, ElegantRed, "RED ALERT: EXPIRED", Icons.Default.Warning)
        daysToExpiry <= 30 -> Quadruple(ElegantAmberContainer, ElegantAmber, "EXPIRING SOON", Icons.Default.WarningAmber)
        else -> Quadruple(ElegantGreenContainer, ElegantGreen, "SAFE TO USE", Icons.Default.CheckCircle)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (expiryMillis > 0) {
                    val months = daysToExpiry / 30
                    val subLabel = when {
                        daysToExpiry < 0 -> "Expired ${-daysToExpiry} days ago. DO NOT TAKE."
                        daysToExpiry == 0 -> "Expires today!"
                        months >= 1 -> "Expires in approx. $months months"
                        else -> "Expires in $daysToExpiry days"
                    }
                    Text(
                        text = subLabel,
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationItemCard(
    med: MedicationEntity,
    onEdit: () -> Unit,
    onRefill: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isLowStock = med.stockQuantity <= med.minimumStock

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElegantDarkOutline, RoundedCornerShape(20.dp))
            .testTag("med_card_${med.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(med.colorHex))
                                } catch (e: Exception) {
                                    ElegantPurple
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = med.medicineName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ElegantTextPrimary
                        )
                        if (med.genericName.isNotBlank()) {
                            Text(
                                text = med.genericName,
                                fontSize = 13.sp,
                                color = ElegantTextSecondary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = ElegantTextSecondary)
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(ElegantDarkSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Record Refill", color = ElegantTextPrimary) },
                            onClick = {
                                menuExpanded = false
                                onRefill()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Medicine", color = ElegantTextPrimary) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = ElegantRed) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            ExpiryStatusBadge(expiryMillis = med.expiryDateMillis)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dosage",
                        fontSize = 11.sp,
                        color = ElegantTextSecondary
                    )
                    Text(
                        text = "${med.dosage} ${med.dosageUnit}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Schedule",
                        fontSize = 11.sp,
                        color = ElegantTextSecondary
                    )
                    Text(
                        text = med.scheduledTimes,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Stock indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock: ${med.stockQuantity} ${med.dosageUnit}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLowStock) ElegantRed else ElegantGreen
                )
                if (isLowStock) {
                    Surface(
                        color = ElegantRedContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "LOW STOCK",
                            color = ElegantRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val progress = (med.stockQuantity.toFloat() / (med.minimumStock * 3).coerceAtLeast(10)).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                color = if (isLowStock) ElegantRed else ElegantGreen,
                trackColor = ElegantDarkOutline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .border(1.dp, ElegantDarkOutline, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElegantTextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = ElegantTextSecondary
                )
            }
        }
    }
}

@Composable
fun StockAlertBanner(
    lowStockMeds: List<MedicationEntity>,
    onRefillClick: (MedicationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (lowStockMeds.isEmpty()) return

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF382A0F)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElegantAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ElegantAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Refill Required (${lowStockMeds.size} Medicines Low)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ElegantAmber
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            lowStockMeds.take(2).forEach { med ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${med.medicineName} (${med.stockQuantity} left, min: ${med.minimumStock})",
                        fontSize = 13.sp,
                        color = ElegantOnAmberContainer
                    )
                    TextButton(
                        onClick = { onRefillClick(med) },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+ Refill", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElegantAmber)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpiryAlertBanner(
    expiringMeds: List<MedicationEntity>,
    modifier: Modifier = Modifier
) {
    if (expiringMeds.isEmpty()) return

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3F1918)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElegantRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = ElegantRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Medication Expiry Warning",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ElegantRed
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            expiringMeds.take(2).forEach { med ->
                val days = ((med.expiryDateMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                val statusText = if (days < 0) "EXPIRED" else "Expires in $days days"
                Text(
                    text = "• ${med.medicineName}: $statusText",
                    fontSize = 13.sp,
                    color = ElegantOnRedContainer
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⚠️ Do not consume expired medications. Dispose safely at your local pharmacy take-back program.",
                fontSize = 11.sp,
                color = ElegantTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MedicalDisclaimerCard(
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    Surface(
        color = ElegantDarkSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElegantDarkOutline, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = ElegantPurple,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = customText ?: "Medical Disclaimer: MedCare is an assistive medication management and reminder tool. It does not provide medical diagnosis, prescribe medicines, or replace professional medical advice. For questions about medication, dosage, or missed doses, consult a qualified healthcare professional.",
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = ElegantTextSecondary
            )
        }
    }
}

@Composable
fun SosEmergencyDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = ElegantDarkSurface,
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = ElegantRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Emergency SOS", color = ElegantRed, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to send an urgent emergency alert to your registered caregivers and emergency services?",
                    fontSize = 14.sp,
                    color = ElegantTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = ElegantRedContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Notice: This feature notifies your family and emergency contacts, but does not replace emergency medical services (911/108/112).",
                        fontSize = 11.sp,
                        color = ElegantOnRedContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ElegantEmergencyRed, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_sos_btn")
            ) {
                Text("SEND EMERGENCY ALERT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RefillDialog(
    medicineName: String,
    currentStock: Int,
    onConfirm: (quantity: Int, pharmacy: String, cost: Double, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var quantityText by remember { mutableStateOf("30") }
    var pharmacyText by remember { mutableStateOf("City Health Pharmacy") }
    var costText by remember { mutableStateOf("15.00") }
    var notesText by remember { mutableStateOf("") }

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
                Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = ElegantPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Refill: $medicineName", color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current stock: $currentStock units", fontSize = 13.sp, color = ElegantTextSecondary)

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity to Add") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("refill_qty_input")
                )

                OutlinedTextField(
                    value = pharmacyText,
                    onValueChange = { pharmacyText = it },
                    label = { Text("Pharmacy / Source") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Cost ($)") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (Optional)") },
                    colors = fieldColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 30
                    val cost = costText.toDoubleOrNull() ?: 0.0
                    onConfirm(qty, pharmacyText, cost, notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_refill_btn")
            ) {
                Text("Add Refill", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ElegantTextSecondary) }
        }
    )
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ElegantDarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ElegantPurple, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = ElegantTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = ElegantTextSecondary,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}
