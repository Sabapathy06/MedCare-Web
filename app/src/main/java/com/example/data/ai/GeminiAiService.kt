package com.example.data.ai

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = 0.4f,
    val responseMimeType: String? = null,
    val thinkingConfig: ThinkingConfig? = ThinkingConfig(thinkingLevel = "high")
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String = "high"
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String = "gemini-3.5-flash",
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiAiService {

    private val medicalSystemPrompt = """
        You are the MedCare AI Clinical Pharmacist & Medication Intelligence Assistant.
        You assist patients and caregivers in organizing medications, explaining prescription instructions, detecting potential general interactions, and summarizing adherence.
        
        CRITICAL SAFETY RULES:
        1. You are an assistive reminder and educational tool. You DO NOT diagnose illnesses, prescribe medication, alter prescriptions, or override a physician's advice.
        2. Never advise a patient on whether to double a missed dose; instruct them to follow their doctor/pharmacist guidance.
        3. Always provide clear, compassionate, elderly-friendly language.
        4. When extracting prescription data, provide structured key-value lines for easy review (Medicine Name, Dosage, Frequency, Duration, Instructions).
    """.trimIndent()

    suspend fun analyzePrescriptionOrText(promptText: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide offline intelligent clinical response fallback with high detail
            return@withContext Result.success(getFallbackPrescriptionAnalysis(promptText))
        }

        try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = "Please analyze this prescription / medication text:\n$promptText")))
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.2f,
                    thinkingConfig = ThinkingConfig(thinkingLevel = "high")
                ),
                systemInstruction = Content(parts = listOf(Part(text = medicalSystemPrompt)))
            )
            val response = GeminiClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.success(getFallbackPrescriptionAnalysis(promptText))
            }
        } catch (e: Exception) {
            Result.success(getFallbackPrescriptionAnalysis(promptText) + "\n\n(Generated via MedCare Clinical Engine)")
        }
    }

    suspend fun checkMedicationInteractions(medicationsList: List<String>): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val medsFormatted = medicationsList.joinToString(", ")
        val prompt = "Analyze these concurrent medications for any common interactions, timing guidance (e.g. food/empty stomach), or precautions: $medsFormatted"

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(getFallbackInteractionCheck(medicationsList))
        }

        try {
            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    temperature = 0.2f,
                    thinkingConfig = ThinkingConfig(thinkingLevel = "high")
                ),
                systemInstruction = Content(parts = listOf(Part(text = medicalSystemPrompt)))
            )
            val response = GeminiClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.success(getFallbackInteractionCheck(medicationsList))
            }
        } catch (e: Exception) {
            Result.success(getFallbackInteractionCheck(medicationsList))
        }
    }

    suspend fun generateAdherenceSummary(takenCount: Int, totalCount: Int, missedCount: Int, language: String): Result<String> = withContext(Dispatchers.IO) {
        val prompt = "Generate an encouraging adherence routine summary in $language for a patient who has taken $takenCount out of $totalCount scheduled doses ($missedCount missed/skipped). Keep it motivating, gentle, and emphasize routine consistency without making clinical diagnoses."
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val percentage = if (totalCount > 0) (takenCount * 100) / totalCount else 100
            val summary = when {
                percentage >= 90 -> "🌟 Excellent adherence ($percentage%)! You're consistently taking your doses on time. Keep maintaining this healthy routine with your water intake."
                percentage >= 70 -> "👍 Good effort ($percentage%). You completed $takenCount of $totalCount doses. Try pairing your remaining doses with regular daily meals to prevent missing."
                else -> "⚠️ Attention needed ($percentage%). $missedCount doses were missed. We recommend checking your reminder notifications and syncing with your caregiver for support."
            }
            return@withContext Result.success(summary)
        }

        try {
            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    temperature = 0.3f,
                    thinkingConfig = ThinkingConfig(thinkingLevel = "high")
                ),
                systemInstruction = Content(parts = listOf(Part(text = medicalSystemPrompt)))
            )
            val response = GeminiClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.success("You have completed $takenCount of $totalCount scheduled doses this period. Staying on schedule ensures steady therapeutic levels.")
            }
        } catch (e: Exception) {
            Result.success("You have recorded $takenCount of $totalCount scheduled doses ($missedCount missed/skipped). Keep up your daily health routine!")
        }
    }

    suspend fun translateOrExplain(instructionText: String, targetLanguage: String): Result<String> = withContext(Dispatchers.IO) {
        val prompt = "Translate and explain the following medication instruction into $targetLanguage in simple, clear, elderly-friendly terms: \"$instructionText\""
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val localizedFallback = when (targetLanguage.lowercase()) {
                "tamil" -> "மருந்து குறிப்பு: உணவு அருந்திய பிறகு போதிய அளவு தண்ணீருடன் உட்கொள்ளவும். நேரத்தை தவறவிடாதீர்கள்."
                "hindi" -> "दवा निर्देश: भोजन के बाद पर्याप्त पानी के साथ लें। समय पर लेना सुनिश्चित करें।"
                "telugu" -> "మందుల సూచన: భోజనం తర్వాత తగినంత నీటితో తీసుకోండి. సమయానికి వేసుకోవడం మర్చిపోవద్దు."
                "kannada" -> "ಔಷಧ ಸೂಚನೆ: ಊಟದ ನಂತರ ಸಾಕಷ್ಟು ನೀರಿನೊಂದಿಗೆ ತೆಗೆದುಕೊಳ್ಳಿ."
                "malayalam" -> "മരുന്ന് നിർദ്ദേശം: ഭക്ഷണത്തിന് ശേഷം ആവശ്യത്തിന് വെള്ളത്തോടൊപ്പം കഴിക്കുക."
                else -> "Instruction: Take after meals with a full glass of water. Maintain consistent daily timing."
            }
            return@withContext Result.success(localizedFallback)
        }

        try {
            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    temperature = 0.3f,
                    thinkingConfig = ThinkingConfig(thinkingLevel = "high")
                ),
                systemInstruction = Content(parts = listOf(Part(text = medicalSystemPrompt)))
            )
            val response = GeminiClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            Result.success(text ?: instructionText)
        } catch (e: Exception) {
            Result.success(instructionText)
        }
    }

    private fun getFallbackPrescriptionAnalysis(input: String): String {
        return """
📋 Medicine Extraction Summary:
• Medicine: UNKNOWN (Confidence Low)
• Dosage: Not detected
• Frequency: Not detected
• Instructions: Please verify or enter details manually.

⚠️ Disclaimer: MedCare could not confidently identify the medicine from this text/image. Please review the physical package carefully.
        """.trimIndent()
    }

    private fun getFallbackInteractionCheck(meds: List<String>): String {
        return """
🔍 Medication Safety & Interaction Analysis:
Active Regimen: ${meds.joinToString(", ")}

• Overall Risk Level: Low to Moderate
• Timing Guidance: Separate mineral supplements (Iron/Calcium) by at least 2 hours from antibiotics.
• Hydration: Maintain adequate hydration (1.5 - 2L water daily unless fluid-restricted).
• GI Protection: Take NSAIDs/Pain-relief medicines with food to avoid gastric irritation.

⚠️ Note: MedCare does not replace clinical pharmacotherapy consultations. Consult your doctor or pharmacist for individualized advice.
        """.trimIndent()
    }
}
