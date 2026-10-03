package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.MedicationEntity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

enum class ConfidenceLevel {
    HIGH, MEDIUM, LOW, UNKNOWN
}

data class ScannedMedicineInfo(
    val medicineName: String = "",
    val genericName: String = "",
    val brandName: String = "",
    val strength: String = "",
    val dosageForm: String = "",
    val manufacturer: String = "",
    val batchNumber: String = "",
    val manufacturingDate: String = "",
    val expiryDate: String = "",
    val composition: String = "",
    val confidence: Float = 0.0f,
    val sourceText: String = "",
    val confidenceMap: Map<String, ConfidenceLevel> = emptyMap(),
    val imagePath: String? = null
)

data class ScanValidationResult(
    val isExpired: Boolean = false,
    val isExpiringSoon: Boolean = false,
    val daysUntilExpiry: Long? = null,
    val expiryStatusMessage: String = "",
    val isMfgAfterExpiry: Boolean = false,
    val mfgStatusMessage: String = "",
    val hasMissingName: Boolean = false,
    val lowConfidenceWarnings: List<String> = emptyList(),
    val matchedExistingMedicine: MedicationEntity? = null
)

@JsonClass(generateAdapter = true)
data class GeminiScannerResponse(
    val medicineName: String? = null,
    val genericName: String? = null,
    val brandName: String? = null,
    val strength: String? = null,
    val dosageForm: String? = null,
    val manufacturer: String? = null,
    val batchNumber: String? = null,
    val manufacturingDate: String? = null,
    val expiryDate: String? = null,
    val composition: String? = null,
    val confidence: Float? = null,
    val sourceText: String? = null
)

class MedicineScannerService(private val context: Context) {

    private val TAG = "OCR_DEBUG"
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(GeminiScannerResponse::class.java)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Complete pipeline for medicine recognition.
     */
    suspend fun analyzeMedicineBitmap(bitmap: Bitmap): ScannedMedicineInfo = withContext(Dispatchers.IO) {
        Log.d(TAG, "OCR_DEBUG: Starting analysis. Bitmap: ${bitmap.width}x${bitmap.height}")
        val savedPath = saveBitmapLocally(bitmap)
        
        // 1. Perform Local OCR (ML Kit)
        val visionText = try {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image).await()
        } catch (e: Exception) {
            Log.e(TAG, "OCR_DEBUG: Local ML Kit failed: ${e.message}")
            null
        }

        val rawText = visionText?.text ?: ""
        Log.d(TAG, "OCR_DEBUG: Local OCR Raw Text: \n$rawText")

        // 2. Multimodal AI Extraction (Gemini) - High reasoning
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                Log.d(TAG, "OCR_DEBUG: Calling Gemini Multimodal Vision...")
                val base64Image = bitmapToBase64(bitmap)
                val geminiInfo = callGeminiVision(apiKey, base64Image, rawText, savedPath)
                if (geminiInfo != null && geminiInfo.medicineName.isNotBlank() && geminiInfo.confidence > 0.5f) {
                    Log.d(TAG, "OCR_DEBUG: Gemini succeeded identifying: ${geminiInfo.medicineName}")
                    return@withContext geminiInfo
                }
            } catch (e: Exception) {
                Log.e(TAG, "OCR_DEBUG: Gemini Vision failed: ${e.message}")
            }
        }

        // 3. Fallback to Local Heuristic Parser - Enhanced with geometry
        Log.d(TAG, "OCR_DEBUG: Falling back to enhanced local heuristic parser.")
        return@withContext parseVisionTextToInfo(visionText, savedPath)
    }

    private suspend fun callGeminiVision(apiKey: String, base64Image: String, ocrContext: String, imagePath: String): ScannedMedicineInfo? {
        val systemPrompt = """
            You are MedCare Intelligent Medicine Scanner OCR & Pharmaceutical Information Extraction Engine.
            Extract all relevant medicine packaging information from the image.
            
            OCR Context detected from device:
            $ocrContext
            
            RULES:
            1. Extract the primary trade/brand name as 'medicineName'.
            2. Extract chemical/active salt as 'genericName'.
            3. Extract strength (e.g. 500mg, 10mg, 20mg).
            4. Extract dosageForm (e.g. Tablet, Capsule, Syrup).
            5. Extract manufacturer, batch number, manufacturing date, and expiry date.
            6. NEVER hallucinate. If a field is not clearly visible, return null or empty string.
            7. DO NOT guess common medicines like Paracetamol unless it is explicitly written.
            8. Return ONLY valid JSON with keys: medicineName, genericName, brandName, strength, dosageForm, manufacturer, batchNumber, manufacturingDate, expiryDate, composition, confidence (0.0 to 1.0).
        """.trimIndent()

        val prompt = "Identify this medication from the package image. Extract Name, Strength, and Expiry Date. Return JSON only."

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.1f,
                responseMimeType = "application/json",
                thinkingConfig = ThinkingConfig(thinkingLevel = "high")
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        val response = GeminiClient.service.generateContent("gemini-3.5-flash", apiKey, request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return null
        Log.d(TAG, "OCR_DEBUG: Gemini Raw Response: \n$responseText")

        val parsed = parseJsonResponse(responseText) ?: return null

        val confidenceMap = buildConfidenceMap(
            parsed.medicineName,
            parsed.genericName,
            parsed.strength,
            parsed.expiryDate,
            parsed.batchNumber
        )

        return ScannedMedicineInfo(
            medicineName = parsed.medicineName?.trim() ?: "",
            genericName = parsed.genericName?.trim() ?: "",
            brandName = parsed.brandName?.trim() ?: parsed.medicineName?.trim() ?: "",
            strength = cleanStrength(parsed.strength),
            dosageForm = normalizeDosageForm(parsed.dosageForm),
            manufacturer = parsed.manufacturer?.trim() ?: "",
            batchNumber = cleanBatchNumber(parsed.batchNumber),
            manufacturingDate = normalizeDateString(parsed.manufacturingDate),
            expiryDate = normalizeDateString(parsed.expiryDate),
            composition = parsed.composition?.trim() ?: "",
            confidence = parsed.confidence ?: 0.9f,
            sourceText = ocrContext,
            confidenceMap = confidenceMap,
            imagePath = imagePath
        )
    }

    private fun parseJsonResponse(jsonStr: String): GeminiScannerResponse? {
        return try {
            val cleanJson = jsonStr.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            jsonAdapter.fromJson(cleanJson)
        } catch (e: Exception) {
            try {
                val jsonObject = JSONObject(jsonStr)
                GeminiScannerResponse(
                    medicineName = jsonObject.optString("medicineName", ""),
                    genericName = jsonObject.optString("genericName", ""),
                    brandName = jsonObject.optString("brandName", ""),
                    strength = jsonObject.optString("strength", ""),
                    dosageForm = jsonObject.optString("dosageForm", ""),
                    manufacturer = jsonObject.optString("manufacturer", ""),
                    batchNumber = jsonObject.optString("batchNumber", ""),
                    manufacturingDate = jsonObject.optString("manufacturingDate", ""),
                    expiryDate = jsonObject.optString("expiryDate", ""),
                    composition = jsonObject.optString("composition", ""),
                    confidence = jsonObject.optDouble("confidence", 0.8).toFloat(),
                    sourceText = jsonObject.optString("sourceText", "")
                )
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Enhanced local parser using ML Kit's structure.
     */
    fun parseVisionTextToInfo(visionText: Text?, imagePath: String? = null): ScannedMedicineInfo {
        if (visionText == null || visionText.text.isBlank()) {
            return ScannedMedicineInfo(
                medicineName = "",
                confidence = 0.0f,
                sourceText = "No text detected",
                imagePath = imagePath
            )
        }

        val ocrText = visionText.text
        var genericName = ""
        var strength = ""
        var dosageForm = ""
        var manufacturer = ""
        var batchNumber = ""
        var mfgDate = ""
        var expDate = ""

        // Geometry-based Name Detection: Find the text block with largest average line height
        var maxLineHeight = 0
        var medName = ""

        // Specific pharmaceutical label patterns
        val labelKeywords = listOf("EXP", "MFG", "BATCH", "LOT", "B.NO", "MFD", "PACKED", "USE BEFORE", "MFG DATE")

        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val rect = line.boundingBox
                if (rect != null) {
                    val height = rect.height()
                    val text = line.text.trim()
                    
                    // Filter out labels and noise
                    val isLabel = labelKeywords.any { text.contains(it, ignoreCase = true) }
                    val isNoise = text.length <= 2 || text.all { !it.isLetter() }
                    val isLong = text.length > 50
                    
                    if (!isLabel && !isNoise && !isLong && height > maxLineHeight) {
                        maxLineHeight = height
                        medName = text
                    }
                }
            }
        }

        // Generic Name detection (often in parentheses or smaller text near the name)
        val genericRegex = Pattern.compile("(?i)\\(([^)]+)\\)")
        val genericMatcher = genericRegex.matcher(ocrText)
        if (genericMatcher.find()) {
            genericName = genericMatcher.group(1) ?: ""
        }

        // Enhanced Date Detection Logic
        val dateRegexPattern = "(?:\\d{1,2}[/\\-.]\\d{2,4}|[A-Za-z]{3,9}\\.?\\s*\\d{2,4})"
        
        // Expiry Date Detection
        val expPatterns = listOf(
            "(?i)(?:exp(?:iry)?\\.?|use\\s*before|best\\s*before)\\s*:?\\s*($dateRegexPattern)",
            "(?i)E\\s*X\\s*P\\.?\\s*:?\\s*($dateRegexPattern)"
        )
        for (pattern in expPatterns) {
            val matcher = Pattern.compile(pattern).matcher(ocrText)
            if (matcher.find()) {
                expDate = normalizeDateString(matcher.group(1))
                break
            }
        }

        // Mfg Date Detection
        val mfgPatterns = listOf(
            "(?i)(?:mfg|mfd|packed|manufacturing|manufactured)\\s*(?:date)?\\.?\\s*:?\\s*($dateRegexPattern)",
            "(?i)M\\s*F\\s*G\\.?\\s*:?\\s*($dateRegexPattern)"
        )
        for (pattern in mfgPatterns) {
            val matcher = Pattern.compile(pattern).matcher(ocrText)
            if (matcher.find()) {
                mfgDate = normalizeDateString(matcher.group(1))
                break
            }
        }

        // Strength Logic - Improved to catch common variations
        val strengthRegex = Pattern.compile("(?i)\\b(\\d+(?:\\.\\d+)?)\\s*(mg|ml|mcg|g|iu|%|mcg/ml|mg/ml)\\b")
        val strengthMatcher = strengthRegex.matcher(ocrText)
        if (strengthMatcher.find()) {
            strength = strengthMatcher.group(0) ?: ""
        }

        // Batch Logic
        val batchRegex = Pattern.compile("(?i)(?:b(?:atch)?\\.?\\s*no\\.?|lot\\.?|b\\/no\\.?|batch\\s*number)\\s*:?\\s*([A-Za-z0-9\\-_]+)")
        val batchMatcher = batchRegex.matcher(ocrText)
        if (batchMatcher.find()) {
            batchNumber = batchMatcher.group(1) ?: ""
        }

        // Form Logic
        val forms = listOf("Tablet", "Capsule", "Syrup", "Suspension", "Drops", "Injection", "Ointment", "Cream", "Gel", "Inhaler", "Spray", "Patch")
        for (form in forms) {
            if (ocrText.contains(form, ignoreCase = true)) {
                dosageForm = form
                break
            }
        }

        val confidenceMap = buildConfidenceMap(medName, genericName, strength, expDate, batchNumber)

        return ScannedMedicineInfo(
            medicineName = medName,
            genericName = genericName,
            strength = strength,
            dosageForm = dosageForm,
            manufacturer = manufacturer,
            batchNumber = batchNumber,
            manufacturingDate = mfgDate,
            expiryDate = expDate,
            confidence = if (medName.isNotBlank() && expDate.isNotBlank()) 0.7f else if (medName.isNotBlank()) 0.5f else 0.1f,
            sourceText = ocrText,
            confidenceMap = confidenceMap,
            imagePath = imagePath
        )
    }

    fun parseTextToMedicineInfo(ocrText: String, imagePath: String? = null): ScannedMedicineInfo {
        // Fallback for string-only input (not recommended for production)
        val info = ScannedMedicineInfo(sourceText = ocrText, imagePath = imagePath)
        val lines = ocrText.lines().filter { it.isNotBlank() }
        val name = lines.firstOrNull() ?: ""
        
        return info.copy(medicineName = name, confidence = if (name.isNotBlank()) 0.3f else 0.0f)
    }

    private fun buildConfidenceMap(
        name: String?,
        generic: String?,
        strength: String?,
        exp: String?,
        batch: String?
    ): Map<String, ConfidenceLevel> {
        val map = mutableMapOf<String, ConfidenceLevel>()
        map["Medicine Name"] = if (!name.isNullOrBlank()) ConfidenceLevel.HIGH else ConfidenceLevel.UNKNOWN
        map["Generic Name"] = if (!generic.isNullOrBlank()) ConfidenceLevel.HIGH else ConfidenceLevel.LOW
        map["Strength"] = if (!strength.isNullOrBlank()) ConfidenceLevel.HIGH else ConfidenceLevel.MEDIUM
        map["Expiry Date"] = if (!exp.isNullOrBlank()) ConfidenceLevel.HIGH else ConfidenceLevel.UNKNOWN
        map["Batch Number"] = if (!batch.isNullOrBlank()) ConfidenceLevel.HIGH else ConfidenceLevel.LOW
        return map
    }

    private fun normalizeDosageForm(form: String?): String {
        if (form.isNullOrBlank()) return ""
        val lower = form.lowercase(Locale.ROOT)
        return when {
            lower.contains("tablet") || lower.contains("tab") -> "Tablet"
            lower.contains("capsule") || lower.contains("cap") -> "Capsule"
            lower.contains("syrup") || lower.contains("liquid") -> "Syrup"
            lower.contains("drop") -> "Drops"
            lower.contains("inject") -> "Injection"
            lower.contains("inhal") -> "Inhaler"
            lower.contains("ointment") || lower.contains("cream") -> "Ointment"
            else -> form.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
    }

    fun normalizeDateString(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        return dateStr.trim().removeSuffix(".")
    }

    fun cleanBatchNumber(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.replace(Regex("(?i)^(?:b(?:atch)?\\.?\\s*no\\.?|lot\\.?|b\\/no\\.?)\\s*:?\\s*"), "").trim()
    }

    fun cleanStrength(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.trim()
    }

    private fun parseDateToMillis(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        val clean = dateStr.trim().uppercase()
            .replace(".", "/")
            .replace("-", "/")
            .replace(" ", "/")
        
        val formats = listOf(
            "MM/yy", "MM/yyyy", "dd/MM/yyyy", 
            "MMM/yyyy", "MMMM/yyyy", "MMM/yy"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                val date = sdf.parse(clean)
                if (date != null) return date.time
            } catch (e: Exception) {}
        }
        return null
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val scaled = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val scale = 1024f / Math.max(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun saveBitmapLocally(bitmap: Bitmap): String {
        return try {
            val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    fun validateMedicineInformation(
        info: ScannedMedicineInfo,
        existingMeds: List<MedicationEntity>
    ): ScanValidationResult {
        val warnings = mutableListOf<String>()
        var isExpired = false
        var isExpiringSoon = false
        var daysUntilExpiry: Long? = null

        val expMillis = parseDateToMillis(info.expiryDate)
        if (expMillis != null) {
            val diff = expMillis - System.currentTimeMillis()
            daysUntilExpiry = diff / (1000 * 60 * 60 * 24)
            if (daysUntilExpiry < 0) isExpired = true
            else if (daysUntilExpiry <= 30) isExpiringSoon = true
        } else {
            if (info.expiryDate.isNotBlank()) warnings.add("Expiry date format unrecognized: ${info.expiryDate}")
        }
        
        if (info.medicineName.isBlank()) {
            warnings.add("Medicine name could not be identified.")
        }

        val matched = existingMeds.find { it.medicineName.equals(info.medicineName, ignoreCase = true) }

        return ScanValidationResult(
            isExpired = isExpired,
            isExpiringSoon = isExpiringSoon,
            daysUntilExpiry = daysUntilExpiry,
            hasMissingName = info.medicineName.isBlank(),
            lowConfidenceWarnings = warnings,
            matchedExistingMedicine = matched
        )
    }

    fun createMedicineFromScan(
        info: ScannedMedicineInfo,
        userId: String,
        frequency: String = "Once daily",
        scheduledTimes: String = "08:00",
        stockQuantity: Int = 30,
        minimumStock: Int = 5,
        instructions: String = "",
        category: String = "General"
    ): MedicationEntity {
        val expMillis = parseDateToMillis(info.expiryDate) ?: (System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000))
        return MedicationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            medicineName = info.medicineName.ifBlank { "Unknown Medication" },
            genericName = info.genericName,
            brandName = info.brandName.ifBlank { info.medicineName },
            dosage = info.strength.filter { it.isDigit() || it == '.' }.ifBlank { "500" },
            dosageUnit = info.strength.filter { it.isLetter() }.ifBlank { "mg" },
            dosageForm = info.dosageForm.ifBlank { "Tablet" },
            frequency = frequency,
            scheduledTimes = scheduledTimes,
            instructions = instructions,
            stockQuantity = stockQuantity,
            minimumStock = minimumStock,
            expiryDateMillis = expMillis,
            expiryDate = info.expiryDate,
            manufacturingDate = info.manufacturingDate,
            manufacturer = info.manufacturer,
            batchNumber = info.batchNumber,
            category = category,
            isActive = true
        )
    }
}
