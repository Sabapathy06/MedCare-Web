package com.example.ui.screens.medications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MedicationEntity
import com.example.ui.theme.*
import com.example.viewmodel.MedCareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineWizardScreen(
    viewModel: MedCareViewModel,
    initialMed: MedicationEntity? = null,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // Step 1: Identity
    var name by remember { mutableStateOf(initialMed?.medicineName ?: "") }
    var genericName by remember { mutableStateOf(initialMed?.genericName ?: "") }
    var brandName by remember { mutableStateOf(initialMed?.brandName ?: "") }
    var strength by remember { mutableStateOf(initialMed?.dosage ?: "") }
    var strengthUnit by remember { mutableStateOf(initialMed?.dosageUnit ?: "mg") }
    var dosageForm by remember { mutableStateOf(initialMed?.dosageForm ?: "Tablet") }

    // Step 2: Safety
    var manufacturer by remember { mutableStateOf(initialMed?.manufacturer ?: "") }
    var composition by remember { mutableStateOf(initialMed?.composition ?: "") }
    var mfgDate by remember { mutableStateOf(initialMed?.manufacturingDate ?: "") }
    var expDate by remember { mutableStateOf(initialMed?.expiryDate ?: "") }
    var batchNumber by remember { mutableStateOf(initialMed?.batchNumber ?: "") }

    // Step 3: Schedule
    var frequency by remember { mutableStateOf(initialMed?.frequency ?: "Once daily") }
    var scheduledTimes by remember { mutableStateOf(initialMed?.scheduledTimes ?: "08:00") }
    var instructions by remember { mutableStateOf(initialMed?.instructions ?: "Take after meal") }

    // Step 4: Stock
    var stockQuantity by remember { mutableStateOf(initialMed?.stockQuantity?.toString() ?: "30") }
    var minStock by remember { mutableStateOf(initialMed?.minimumStock?.toString() ?: "5") }
    var category by remember { mutableStateOf(initialMed?.category ?: "General") }

    Scaffold(
        containerColor = ElegantDarkBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ElegantDarkBackground),
                title = {
                    Column {
                        Text(
                            text = if (initialMed != null) "Edit Medicine" else "Add Medicine",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ElegantTextPrimary
                        )
                        Text(
                            text = "Step $currentStep of $totalSteps",
                            fontSize = 12.sp,
                            color = ElegantPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) currentStep-- else onCancel() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ElegantTextPrimary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = ElegantDarkSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancel) {
                        Text("CANCEL", color = ElegantTextSecondary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                viewModel.saveMedication(
                                    id = initialMed?.id,
                                    name = name,
                                    genericName = genericName,
                                    dosage = strength,
                                    dosageUnit = strengthUnit,
                                    frequency = frequency,
                                    scheduledTimes = scheduledTimes,
                                    instructions = instructions,
                                    stock = stockQuantity.toIntOrNull() ?: 30,
                                    minStock = minStock.toIntOrNull() ?: 5,
                                    expiryMillis = initialMed?.expiryDateMillis ?: (System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000),
                                    category = category,
                                    colorHex = initialMed?.colorHex ?: "#BB86FC"
                                )
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp).width(160.dp),
                        enabled = name.isNotBlank() || currentStep > 1
                    ) {
                        Text(
                            text = if (currentStep == totalSteps) "CONFIRM" else "NEXT",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(if (currentStep == totalSteps) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = ElegantPurple,
                trackColor = ElegantDarkOutline
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                }, label = "wizard_step"
            ) { step ->
                when (step) {
                    1 -> StepIdentity(name, { name = it }, genericName, { genericName = it }, brandName, { brandName = it }, strength, { strength = it }, strengthUnit, { strengthUnit = it }, dosageForm, { dosageForm = it })
                    2 -> StepSafety(manufacturer, { manufacturer = it }, composition, { composition = it }, mfgDate, { mfgDate = it }, expDate, { expDate = it }, batchNumber, { batchNumber = it })
                    3 -> StepSchedule(frequency, { frequency = it }, scheduledTimes, { scheduledTimes = it }, instructions, { instructions = it })
                    4 -> StepStock(stockQuantity, { stockQuantity = it }, minStock, { minStock = it }, category, { category = it })
                    5 -> StepReview(name, strength, strengthUnit, frequency, scheduledTimes, stockQuantity, expDate)
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun StepIdentity(
    name: String, onNameChange: (String) -> Unit,
    gen: String, onGenChange: (String) -> Unit,
    brand: String, onBrandChange: (String) -> Unit,
    strength: String, onStrengthChange: (String) -> Unit,
    unit: String, onUnitChange: (String) -> Unit,
    form: String, onFormChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Medicine Identity", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ElegantTextPrimary)
        Text("What medicine would you like to add?", fontSize = 14.sp, color = ElegantTextSecondary)

        WizardTextField(name, onNameChange, "Medicine Name *", Icons.Default.MedicalServices)
        WizardTextField(brand, onBrandChange, "Brand Name", Icons.AutoMirrored.Filled.Label)
        WizardTextField(gen, onGenChange, "Generic Name", Icons.Default.Science)
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WizardTextField(strength, onStrengthChange, "Strength", Icons.Default.Bolt, Modifier.weight(1f))
            WizardTextField(unit, onUnitChange, "Unit", Icons.Default.Straighten, Modifier.weight(1f))
        }

        Text("Dosage Form", color = ElegantTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        val forms = listOf("Tablet", "Capsule", "Syrup", "Drops", "Injection", "Inhaler", "Ointment")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(forms) { f ->
                val selected = form == f
                Surface(
                    color = if (selected) ElegantPurple else ElegantDarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { onFormChange(f) }.border(1.dp, if (selected) ElegantPurple else ElegantDarkOutline, RoundedCornerShape(12.dp))
                ) {
                    Text(f, color = if (selected) Color.White else ElegantTextSecondary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StepSafety(
    man: String, onManChange: (String) -> Unit,
    comp: String, onCompChange: (String) -> Unit,
    mfg: String, onMfgChange: (String) -> Unit,
    exp: String, onExpChange: (String) -> Unit,
    batch: String, onBatchChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Safety & Verification", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ElegantTextPrimary)
        Text("Important manufacturing details.", fontSize = 14.sp, color = ElegantTextSecondary)

        WizardTextField(man, onManChange, "Manufacturer", Icons.Default.Business)
        WizardTextField(comp, onCompChange, "Composition", Icons.AutoMirrored.Filled.FormatListBulleted)
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WizardTextField(mfg, onMfgChange, "Mfg Date", Icons.Default.CalendarToday, Modifier.weight(1f))
            WizardTextField(exp, onExpChange, "Expiry Date *", Icons.Default.EventBusy, Modifier.weight(1f))
        }
        
        WizardTextField(batch, onBatchChange, "Batch / Lot Number", Icons.Default.Numbers)
    }
}

@Composable
fun StepSchedule(
    freq: String, onFreqChange: (String) -> Unit,
    times: String, onTimesChange: (String) -> Unit,
    ins: String, onInsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Dose Schedule", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ElegantTextPrimary)
        Text("When should you take this medicine?", fontSize = 14.sp, color = ElegantTextSecondary)

        val frequencies = listOf("Once daily", "Twice daily", "Three times daily", "Four times daily", "As needed")
        frequencies.forEach { f ->
            val selected = freq == f
            Surface(
                color = if (selected) ElegantPurple.copy(alpha = 0.2f) else ElegantDarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, if (selected) ElegantPurple else ElegantDarkOutline),
                modifier = Modifier.fillMaxWidth().clickable {
                    onFreqChange(f)
                    val newTimes = when(f) {
                        "Twice daily" -> "08:00, 20:00"
                        "Three times daily" -> "08:00, 14:00, 20:00"
                        "Four times daily" -> "08:00, 12:00, 16:00, 20:00"
                        else -> "08:00"
                    }
                    onTimesChange(newTimes)
                }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = ElegantPurple))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(f, color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        WizardTextField(times, onTimesChange, "Scheduled Times (HH:mm)", Icons.Default.AccessTime)
        WizardTextField(ins, onInsChange, "Instructions", Icons.Default.Info)
    }
}

@Composable
fun StepStock(
    stock: String, onStockChange: (String) -> Unit,
    min: String, onMinChange: (String) -> Unit,
    cat: String, onCatChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Stock & Inventory", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ElegantTextPrimary)
        Text("Track how much medicine you have left.", fontSize = 14.sp, color = ElegantTextSecondary)

        WizardTextField(stock, onStockChange, "Current Stock", Icons.Default.Inventory)
        WizardTextField(min, onMinChange, "Low Stock Alert Level", Icons.Default.NotificationImportant)
        
        Text("Category", color = ElegantTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        val cats = listOf("General", "Diabetes", "Heart", "Vitamins", "Pain", "Lungs")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cats) { c ->
                val selected = cat == c
                Surface(
                    color = if (selected) ElegantTeal else ElegantDarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { onCatChange(c) }.border(1.dp, if (selected) ElegantTeal else ElegantDarkOutline, RoundedCornerShape(12.dp))
                ) {
                    Text(c, color = if (selected) Color.White else ElegantTextSecondary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StepReview(
    name: String, str: String, unit: String,
    freq: String, times: String, stock: String, exp: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Review Details", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ElegantTextPrimary)
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantPurple.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewItem("Medicine", name)
                ReviewItem("Dosage", "$str $unit")
                ReviewItem("Frequency", freq)
                ReviewItem("Times", times)
                ReviewItem("Stock", "$stock left")
                ReviewItem("Expiry", exp.ifBlank { "Not set" })
            }
        }
        
        Surface(
            color = ElegantPurpleContainer.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = ElegantPurple)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Information is verified and safe to save.", fontSize = 12.sp, color = ElegantPurple, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReviewItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = ElegantTextSecondary, fontSize = 14.sp)
        Text(value, color = ElegantTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun WizardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = ElegantPurple) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElegantPurple,
            unfocusedBorderColor = ElegantDarkOutline,
            focusedLabelColor = ElegantPurple,
            focusedTextColor = ElegantTextPrimary,
            unfocusedTextColor = ElegantTextPrimary,
            focusedContainerColor = ElegantDarkSurface,
            unfocusedContainerColor = ElegantDarkSurface
        )
    )
}
