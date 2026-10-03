package com.example.ui.screens.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.ai.ConfidenceLevel
import com.example.data.ai.ScanValidationResult
import com.example.data.ai.ScannedMedicineInfo
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.*
import com.example.viewmodel.MedCareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineReviewScreen(
    viewModel: MedCareViewModel,
    scannedInfo: ScannedMedicineInfo,
    validationResult: ScanValidationResult?,
    onRescan: () -> Unit,
    onCancel: () -> Unit,
    onSaveComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Form States
    var medicineName by remember { mutableStateOf(scannedInfo.medicineName) }
    var genericName by remember { mutableStateOf(scannedInfo.genericName) }
    var brandName by remember { mutableStateOf(scannedInfo.brandName) }
    var strength by remember { mutableStateOf(scannedInfo.strength) }
    var dosageForm by remember { mutableStateOf(scannedInfo.dosageForm) }
    var manufacturer by remember { mutableStateOf(scannedInfo.manufacturer) }
    var batchNumber by remember { mutableStateOf(scannedInfo.batchNumber) }
    var manufacturingDate by remember { mutableStateOf(scannedInfo.manufacturingDate) }
    var expiryDate by remember { mutableStateOf(scannedInfo.expiryDate) }
    var composition by remember { mutableStateOf(scannedInfo.composition) }

    // Schedule & Stock Configuration States
    var frequency by remember { mutableStateOf("Once daily") }
    var scheduledTimes by remember { mutableStateOf("08:00") }
    var stockQuantity by remember { mutableStateOf("30") }
    var minimumStock by remember { mutableStateOf("5") }
    var instructions by remember { mutableStateOf("Take after meals with water") }
    var category by remember { mutableStateOf("General") }

    val dosageForms = listOf("Tablet", "Capsule", "Syrup", "Drops", "Injection", "Inhaler", "Ointment", "Chewable")
    val frequencyOptions = listOf("Once daily", "Twice daily", "Three times daily", "Four times daily", "As needed")
    val categories = listOf("General", "Cardiovascular", "Diabetes", "Vitamins", "Antibiotic", "Pain Relief")

    Scaffold(
        containerColor = ElegantDarkBackground,
        modifier = Modifier.navigationBarsPadding().imePadding(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ElegantDarkBackground,
                    titleContentColor = ElegantTextPrimary
                ),
                title = {
                    Column {
                        Text(
                            text = "Verify Details",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = ElegantTextPrimary
                        )
                        if (scannedInfo.confidence > 0) {
                            Text(
                                text = "Confidence: ${(scannedInfo.confidence * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = ElegantPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.size(56.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel",
                            modifier = Modifier.size(28.dp),
                            tint = ElegantTextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onRescan, modifier = Modifier.height(56.dp)) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Rescan",
                            tint = ElegantPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESCAN", color = ElegantPurple, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ElegantDarkBackground)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Alerts based on validation
            if (validationResult != null) {
                if (validationResult.isExpired) {
                    ValidationAlert("⚠ MEDICINE EXPIRED: Do not use.", ElegantRed)
                } else if (validationResult.isExpiringSoon) {
                    ValidationAlert("⚠ EXPIRING SOON: Expires in ${validationResult.daysUntilExpiry} days.", ElegantAmber)
                }
                
                validationResult.lowConfidenceWarnings.forEach { warning ->
                    Surface(
                        color = ElegantDarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = ElegantPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(warning, fontSize = 12.sp, color = ElegantTextSecondary)
                        }
                    }
                }
            }

            // Medicine Identity
            Text("IDENTIFICATION", fontWeight = FontWeight.Bold, color = ElegantPurple, fontSize = 13.sp, letterSpacing = 1.sp)
            
            ReviewTextField(medicineName, { medicineName = it }, "Medicine Name *", isError = medicineName.isBlank(), 
                confidence = scannedInfo.confidenceMap["Medicine Name"])
            
            ReviewTextField(genericName, { genericName = it }, "Generic Name",
                confidence = scannedInfo.confidenceMap["Generic Name"])

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewTextField(brandName, { brandName = it }, "Brand", Modifier.weight(1f))
                ReviewTextField(strength, { strength = it }, "Strength", Modifier.weight(1f),
                    confidence = scannedInfo.confidenceMap["Strength"])
            }

            // Dosage Form Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dosage Form", color = ElegantTextSecondary, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dosageForms) { form ->
                        val isSelected = dosageForm == form
                        Surface(
                            color = if (isSelected) ElegantPurple else ElegantDarkSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) ElegantPurple else ElegantDarkOutline),
                            modifier = Modifier.clickable { dosageForm = form }
                        ) {
                            Text(
                                text = form,
                                color = if (isSelected) Color.White else ElegantTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            ReviewTextField(manufacturer, { manufacturer = it }, "Manufacturer")
            ReviewTextField(composition, { composition = it }, "Composition")

            // Expiry Priority
            Text("SAFETY DATES & BATCH", fontWeight = FontWeight.Bold, color = ElegantPurple, fontSize = 13.sp, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewTextField(expiryDate, { expiryDate = it }, "Expiry Date *", Modifier.weight(1f), 
                    isError = expiryDate.isBlank(), confidence = scannedInfo.confidenceMap["Expiry Date"])
                
                ReviewTextField(manufacturingDate, { manufacturingDate = it }, "Mfg Date", Modifier.weight(1f))
            }
            ReviewTextField(batchNumber, { batchNumber = it }, "Batch / Lot Number",
                confidence = scannedInfo.confidenceMap["Batch Number"])

            // Personal Schedule
            Text("YOUR SCHEDULE", fontWeight = FontWeight.Bold, color = ElegantPurple, fontSize = 13.sp, letterSpacing = 1.sp)
            
            // Frequency Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dose Frequency", color = ElegantTextSecondary, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(frequencyOptions) { opt ->
                        val isSelected = frequency == opt
                        Surface(
                            color = if (isSelected) ElegantPurple else ElegantDarkSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) ElegantPurple else ElegantDarkOutline),
                            modifier = Modifier.clickable { frequency = opt }
                        ) {
                            Text(
                                text = opt,
                                color = if (isSelected) Color.White else ElegantTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            ReviewTextField(scheduledTimes, { scheduledTimes = it }, "Scheduled Times (HH:mm)")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewTextField(stockQuantity, { stockQuantity = it }, "Current Stock", Modifier.weight(1f))
                ReviewTextField(minimumStock, { minimumStock = it }, "Alert Level", Modifier.weight(1f))
            }

            ReviewTextField(instructions, { instructions = it }, "Instructions")

            // Category Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Medicine Category", color = ElegantTextSecondary, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Surface(
                            color = if (isSelected) ElegantPurple else ElegantDarkSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) ElegantPurple else ElegantDarkOutline),
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else ElegantTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ADD Button
            Button(
                onClick = {
                    if (medicineName.isNotBlank() && expiryDate.isNotBlank()) {
                        val info = scannedInfo.copy(
                            medicineName = medicineName.trim(),
                            genericName = genericName.trim(),
                            brandName = brandName.trim(),
                            strength = strength.trim(),
                            dosageForm = dosageForm.trim(),
                            manufacturer = manufacturer.trim(),
                            batchNumber = batchNumber.trim(),
                            manufacturingDate = manufacturingDate.trim(),
                            expiryDate = expiryDate.trim(),
                            composition = composition.trim()
                        )
                        viewModel.confirmAndSaveScannedMedicine(
                            info, frequency, scheduledTimes, 
                            stockQuantity.toIntOrNull() ?: 30, 
                            minimumStock.toIntOrNull() ?: 5, 
                            instructions, category
                        )
                        onSaveComplete()
                    }
                },
                enabled = medicineName.isNotBlank() && expiryDate.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("confirm_and_add_medicine_btn"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("ADD TO MY MEDICINES", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, ElegantDarkOutline)
            ) {
                Text("DISCARD SCAN", color = ElegantTextSecondary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ReviewTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    modifier: Modifier = Modifier, 
    isError: Boolean = false,
    confidence: ConfidenceLevel? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        shape = RoundedCornerShape(14.dp),
        trailingIcon = {
            if (confidence != null) {
                ConfidenceIndicator(confidence)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElegantPurple,
            unfocusedBorderColor = ElegantDarkOutline,
            focusedContainerColor = ElegantDarkSurface,
            unfocusedContainerColor = ElegantDarkSurface
        )
    )
}

@Composable
fun ConfidenceIndicator(level: ConfidenceLevel) {
    val (color, text) = when (level) {
        ConfidenceLevel.HIGH -> Pair(ElegantGreen, "High")
        ConfidenceLevel.MEDIUM -> Pair(ElegantAmber, "Medium")
        ConfidenceLevel.LOW -> Pair(ElegantRed, "Low")
        ConfidenceLevel.UNKNOWN -> Pair(ElegantTextMuted, "None")
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ValidationAlert(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, color = color, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
    }
}
