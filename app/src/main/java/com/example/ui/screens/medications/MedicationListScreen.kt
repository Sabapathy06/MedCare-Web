package com.example.ui.screens.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MedicationItemCard
import com.example.ui.components.RefillDialog
import com.example.ui.theme.*
import com.example.viewmodel.MedCareViewModel

@Composable
fun MedicationListScreen(
    viewModel: MedCareViewModel,
    onNavigateToScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val medications by viewModel.userMedications.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var editingMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    var refillMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    var showWizard by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Active", "Expiring Soon", "Expired", "Low Stock")

    val filteredList = remember(medications, searchQuery, selectedFilter) {
        medications.filter { med ->
            val matchesSearch = med.medicineName.contains(searchQuery, ignoreCase = true) ||
                    med.genericName.contains(searchQuery, ignoreCase = true) ||
                    med.category.contains(searchQuery, ignoreCase = true)
            
            val daysToExpiry = ((med.expiryDateMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
            
            val matchesFilter = when (selectedFilter) {
                "Active" -> med.isActive
                "Low Stock" -> med.stockQuantity <= med.minimumStock
                "Expiring Soon" -> daysToExpiry in 0..30
                "Expired" -> daysToExpiry < 0 && med.expiryDateMillis > 0
                else -> true
            }
            matchesSearch && matchesFilter
        }
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

    if (showWizard || editingMedication != null) {
        MedicineWizardScreen(
            viewModel = viewModel,
            initialMed = editingMedication,
            onComplete = {
                showWizard = false
                editingMedication = null
            },
            onCancel = {
                showWizard = false
                editingMedication = null
            }
        )
    } else {
        Scaffold(
            containerColor = ElegantDarkBackground,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showWizard = true },
                    containerColor = ElegantPurple,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(64.dp).padding(bottom = 8.dp).testTag("add_medication_fab")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD MEDICINE", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(ElegantDarkBackground)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "My Medicines",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = ElegantTextPrimary
                )
                Text(
                    text = "Manage your prescriptions and stock",
                    fontSize = 16.sp,
                    color = ElegantTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Prominent Intelligent Scanner Hero Action Banner
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantPurple.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToScanner() }
                        .testTag("prominent_scan_medicine_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElegantPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = ElegantPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Scan Medicine Package",
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = ElegantPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "AI OCR",
                                            color = ElegantPurple,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Auto-extract name, strength, expiry & batch",
                                    fontSize = 12.sp,
                                    color = ElegantTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToScanner,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantPurple,
                                contentColor = ElegantOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("prominent_scan_btn")
                        ) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search medicine, generic, category...", color = ElegantTextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElegantPurple) },
                    trailingIcon = {
                        IconButton(onClick = onNavigateToScanner) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Scan with Camera",
                                tint = ElegantPurple
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantPurple,
                        unfocusedBorderColor = ElegantDarkOutline,
                        focusedTextColor = ElegantTextPrimary,
                        unfocusedTextColor = ElegantTextPrimary,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions) { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            color = if (isSelected) ElegantPurple else ElegantDarkSurfaceVariant,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ElegantPurple else ElegantDarkOutline),
                            modifier = Modifier.clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) ElegantOnPrimary else ElegantTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredList.isEmpty()) {
                    EmptyStateView(
                        title = "No medications found",
                        subtitle = if (searchQuery.isNotBlank()) "No matches for '$searchQuery'" else "You haven't registered any medications yet. Scan a package to get started!",
                        icon = Icons.Default.LocalPharmacy,
                        actionLabel = "📷 Scan Medicine Package",
                        onAction = onNavigateToScanner
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.id }) { med ->
                            MedicationItemCard(
                                med = med,
                                onEdit = { editingMedication = med },
                                onRefill = { refillMedication = med },
                                onDelete = { viewModel.deleteMedication(med.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}
