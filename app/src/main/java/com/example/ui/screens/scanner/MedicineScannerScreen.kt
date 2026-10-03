package com.example.ui.screens.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.ui.theme.*
import com.example.viewmodel.MedCareViewModel
import java.io.File

enum class ScanStep {
    FRONT, BACK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScannerScreen(
    viewModel: MedCareViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToManualAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scannerState by viewModel.scannerState.collectAsState()

    var scanStep by remember { mutableStateOf(ScanStep.FRONT) }
    var frontBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val tempFile = remember { File(context.cacheDir, "temp_scan.jpg") }
    val tempUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            try {
                val bitmap = loadBitmapFromUri(context, tempUri)
                if (bitmap != null) {
                    if (scanStep == ScanStep.FRONT) {
                        frontBitmap = bitmap
                        scanStep = ScanStep.BACK
                    } else {
                        viewModel.scanMedicineBitmaps(frontBitmap!!, bitmap)
                    }
                }
            } catch (e: Exception) {
                Log.e("OCR_DEBUG", "Failed to load captured image: ${e.message}")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(tempUri)
        }
    }

    if (scannerState.isReviewing && scannerState.scannedInfo != null) {
        MedicineReviewScreen(
            viewModel = viewModel,
            scannedInfo = scannerState.scannedInfo!!,
            validationResult = scannerState.validationResult,
            onRescan = { 
                viewModel.resetScanner()
                scanStep = ScanStep.FRONT
                frontBitmap = null
            },
            onCancel = {
                viewModel.resetScanner()
                onNavigateBack()
            },
            onSaveComplete = {
                viewModel.resetScanner()
                onNavigateBack()
            }
        )
        return
    }

    Scaffold(
        containerColor = ElegantDarkBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ElegantDarkBackground,
                    titleContentColor = ElegantTextPrimary
                ),
                title = {
                    Column {
                        Text("Medicine Scanner", fontWeight = FontWeight.Black, fontSize = 22.sp)
                        Text(
                            if (scanStep == ScanStep.FRONT) "Step 1: Scan Front of Package" else "Step 2: Scan Back / Side",
                            fontSize = 12.sp, color = ElegantPurple, fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (scanStep == ScanStep.BACK) {
                            scanStep = ScanStep.FRONT
                            frontBitmap = null
                        } else {
                            onNavigateBack()
                        }
                    }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(28.dp), tint = ElegantTextPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Scanner Guidance Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(ElegantDarkSurfaceElevated)
                    .border(BorderStroke(2.dp, ElegantPurple.copy(alpha = 0.5f)), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (scannerState.isScanning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ElegantPurple, modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("ANALYZING...", fontWeight = FontWeight.Black, color = ElegantPurple, fontSize = 18.sp)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ReticleCorner(rotation = 0f)
                            ReticleCorner(rotation = 90f)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (scanStep == ScanStep.FRONT) Icons.Default.FlipToFront else Icons.Default.FlipToBack,
                                contentDescription = null, tint = ElegantPurple, modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (scanStep == ScanStep.FRONT) "ALIGN FRONT OF BOX" else "ALIGN SIDE WITH DATES",
                                fontWeight = FontWeight.Black, color = ElegantTextPrimary, fontSize = 16.sp
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ReticleCorner(rotation = 270f)
                            ReticleCorner(rotation = 180f)
                        }
                    }
                }
            }

            Text(
                text = if (scanStep == ScanStep.FRONT) "Scan the front to identify the medicine name and strength." 
                       else "Scan the back or side to find Expiry Date, MFG Date, and Batch Number.",
                color = ElegantTextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (hasCameraPermission) cameraLauncher.launch(tempUri)
                        else permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (scanStep == ScanStep.FRONT) "CAPTURE FRONT" else "CAPTURE BACK", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }

                if (scanStep == ScanStep.BACK && frontBitmap != null) {
                    OutlinedButton(
                        onClick = { viewModel.scanMedicineBitmaps(frontBitmap!!, null) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, ElegantTeal.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTeal)
                    ) {
                        Text("SKIP BACK SCAN", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToManualAdd,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, ElegantDarkOutline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTextPrimary)
                ) {
                    Text("ENTER MANUALLY", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReticleCorner(rotation: Float) {
    Box(
        modifier = Modifier
            .rotate(rotation)
            .size(32.dp)
            .border(
                BorderStroke(4.dp, ElegantPurple),
                RoundedCornerShape(topStart = 8.dp)
            )
    )
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}
