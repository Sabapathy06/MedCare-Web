package com.example.ui.screens.stock

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MedicationEntity
import com.example.data.local.entity.RefillRecordEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RefillDialog
import com.example.ui.theme.ElegantAmber
import com.example.ui.theme.ElegantAmberContainer
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreen
import com.example.ui.theme.ElegantGreenContainer
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockRefillScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val medications by viewModel.userMedications.collectAsState()
    val refills by viewModel.refills.collectAsState()

    var selectedTab by remember { mutableStateOf("Inventory") } // Inventory, Refill History, Expiry Tracking
    var refillMedication by remember { mutableStateOf<MedicationEntity?>(null) }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Tabs
        val tabs = listOf("Inventory", "Refill History", "Expiry Tracking")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tabs) { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    color = if (isSelected) ElegantPurple else ElegantDarkSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ElegantPurple else ElegantDarkOutline),
                    modifier = Modifier.clickable { selectedTab = tab }
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) ElegantOnPrimary else ElegantTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            "Inventory" -> {
                if (medications.isEmpty()) {
                    EmptyStateView(
                        title = "No medicines in stock",
                        subtitle = "Add medicines to start monitoring stock levels.",
                        icon = Icons.Default.LocalPharmacy
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(medications, key = { it.id }) { med ->
                            StockInventoryCard(
                                med = med,
                                onRefillClick = { refillMedication = med }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }

            "Refill History" -> {
                if (refills.isEmpty()) {
                    EmptyStateView(
                        title = "No refill records yet",
                        subtitle = "When you log a medicine refill, it will be cataloged here.",
                        icon = Icons.Default.History
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(refills, key = { it.id }) { refill ->
                            RefillHistoryCard(refill = refill)
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }

            "Expiry Tracking" -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = ElegantPurpleContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Responsible Medication Disposal", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Expired drugs lose potency and can be harmful. Please do not flush medicines down household drains. Use pharmacy take-back drop boxes.",
                                    fontSize = 12.sp,
                                    color = ElegantTextSecondary
                                )
                            }
                        }
                    }

                    items(medications, key = { it.id }) { med ->
                        ExpiryDetailCard(med = med)
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun StockInventoryCard(
    med: MedicationEntity,
    onRefillClick: () -> Unit
) {
    val isLowStock = med.stockQuantity <= med.minimumStock
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLowStock) ElegantAmber else ElegantDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(med.medicineName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextPrimary)
                    Text("Min threshold: ${med.minimumStock} ${med.dosageUnit}s", fontSize = 12.sp, color = ElegantTextSecondary)
                }

                Button(
                    onClick = onRefillClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple, contentColor = ElegantOnPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Refill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Current: ${med.stockQuantity} ${med.dosageUnit}s", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isLowStock) ElegantRed else ElegantGreen)
                Text(if (isLowStock) "LOW STOCK — REFILL REQUIRED" else "Adequate Stock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isLowStock) ElegantRed else ElegantGreen)
            }

            Spacer(modifier = Modifier.height(6.dp))
            val progress = (med.stockQuantity.toFloat() / (med.minimumStock * 3).coerceAtLeast(10)).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                color = if (isLowStock) ElegantRed else ElegantGreen,
                trackColor = ElegantDarkOutline,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun RefillHistoryCard(refill: RefillRecordEntity) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(refill.medicineName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ElegantTextPrimary)
                Text("+${refill.quantityAdded} units", fontWeight = FontWeight.Bold, color = ElegantGreen, fontSize = 14.sp)
            }
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(refill.refillDateMillis))
            Text("Refill Date: $dateStr", fontSize = 12.sp, color = ElegantTextSecondary)
            Text("Pharmacy: ${refill.pharmacyName}", fontSize = 12.sp, color = ElegantTeal)
            if (refill.cost > 0) {
                Text("Cost: $${String.format(Locale.getDefault(), "%.2f", refill.cost)}", fontSize = 12.sp, color = ElegantPurple)
            }
            if (refill.notes.isNotBlank()) {
                Text("Notes: ${refill.notes}", fontSize = 11.sp, color = ElegantTextMuted)
            }
        }
    }
}

@Composable
fun ExpiryDetailCard(med: MedicationEntity) {
    val days = ((med.expiryDateMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    val expiryFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(med.expiryDateMillis))

    val (bgColor, textColor, statusText) = when {
        days < 0 -> Triple(ElegantRedContainer, ElegantRed, "EXPIRED ($expiryFormatted)")
        days <= 7 -> Triple(ElegantRedContainer, ElegantRed, "Expires in $days days ($expiryFormatted)")
        days <= 30 -> Triple(ElegantAmberContainer, ElegantAmber, "Expires in $days days ($expiryFormatted)")
        else -> Triple(ElegantGreenContainer, ElegantGreen, "Safe: Expires on $expiryFormatted")
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(med.medicineName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                Text(statusText, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Category: ${med.category} • Dosage: ${med.dosage} ${med.dosageUnit}", fontSize = 12.sp, color = ElegantTextSecondary)
        }
    }
}
