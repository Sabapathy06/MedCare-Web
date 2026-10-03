package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.CaregiverRelationshipEntity
import com.example.data.local.entity.DoseRecordEntity
import com.example.data.local.entity.MedicationEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.SosEventEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MedCareRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

import android.graphics.Bitmap
import com.example.data.ai.MedicineScannerService
import com.example.data.ai.ScanValidationResult
import com.example.data.ai.ScannedMedicineInfo

data class MedicineScannerUiState(
    val isScanning: Boolean = false,
    val scannedInfo: ScannedMedicineInfo? = null,
    val validationResult: ScanValidationResult? = null,
    val error: String? = null,
    val isReviewing: Boolean = false,
    val isSavedSuccess: Boolean = false
)

data class AiAnalysisState(
    val isLoading: Boolean = false,
    val resultText: String? = null,
    val interactionText: String? = null,
    val summaryText: String? = null,
    val translationText: String? = null,
    val error: String? = null
)

class MedCareViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = MedCareRepository(database).apply {
        initReminderManager(application)
    }

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow = _messageFlow.asSharedFlow()

    val currentUser: StateFlow<UserEntity?> = repository.currentLoggedInUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentUserAge: StateFlow<Int> = currentUser.flatMapLatest { user ->
        flowOf(user?.let { calculateAge(it.dateOfBirth) } ?: 0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userMedications: StateFlow<List<MedicationEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getMedicationsForUser(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val todayDate = MedCareRepository.getTodayDateString()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayDoses: StateFlow<List<DoseRecordEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getDoseRecordsForDate(user.userId, todayDate) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allDoseHistory: StateFlow<List<DoseRecordEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getAllDoseRecords(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val refills: StateFlow<List<com.example.data.local.entity.RefillRecordEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getRefillsForUser(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val prescriptions: StateFlow<List<PrescriptionEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getPrescriptionsForUser(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val appointments: StateFlow<List<AppointmentEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getAppointmentsForPatient(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val caregivers: StateFlow<List<CaregiverRelationshipEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getCaregiversForPatient(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val caregiverPatients: StateFlow<List<CaregiverRelationshipEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getPatientsForCaregiver(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<NotificationEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotificationsForUser(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val unreadNotifCount: StateFlow<Int> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUnreadNotificationCount(user.userId) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userSosEvents: StateFlow<List<SosEventEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getSosEventsForUser(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Patient for Caregiver view
    private val _selectedPatientId = MutableStateFlow<String?>(null)
    val selectedPatientId: StateFlow<String?> = _selectedPatientId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedPatientDoses: StateFlow<List<DoseRecordEntity>> = _selectedPatientId.flatMapLatest { pId ->
        if (pId != null) repository.getDoseRecordsForDate(pId, todayDate) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedPatientMeds: StateFlow<List<MedicationEntity>> = _selectedPatientId.flatMapLatest { pId ->
        if (pId != null) repository.getMedicationsForUser(pId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI State
    private val _aiState = MutableStateFlow(AiAnalysisState())
    val aiState: StateFlow<AiAnalysisState> = _aiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.checkAllMedicationAlerts(user.userId)
                    repository.checkMissedDoses(user.userId)
                    repository.syncDataFromFirestore(user.userId)
                }
            }
        }
    }

    fun selectPatientForCaregiver(patientId: String) {
        _selectedPatientId.value = patientId
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchUser(userId)
            val user = repository.allUsers.firstOrNull()?.find { it.userId == userId }
            _messageFlow.emit("Switched profile to ${user?.fullName ?: "User"}")
        }
    }

    fun registerNewUser(
        fullName: String,
        email: String,
        passwordRaw: String,
        dob: String,
        emergencyContact: String,
        emergencyPhone: String
    ) {
        viewModelScope.launch {
            val age = calculateAge(dob)
            val shouldEnableSimpleMode = age >= 65
            
            val newUser = UserEntity(
                userId = "user_${UUID.randomUUID().toString().take(8)}",
                fullName = fullName,
                email = email,
                phone = "",
                dateOfBirth = dob,
                role = "PATIENT",
                emergencyContactName = emergencyContact,
                emergencyContactPhone = emergencyPhone,
                preferredLanguage = "English",
                isLoggedIn = true,
                isOnboardingCompleted = false,
                isSimpleModeEnabled = shouldEnableSimpleMode
            )
            val result = repository.registerUser(newUser, passwordRaw)
            result.onSuccess {
                _messageFlow.emit("Account created successfully. Welcome, $fullName!")
            }.onFailure { err ->
                _messageFlow.emit("Registration failed: ${err.message}")
            }
        }
    }

    private fun calculateAge(dob: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = sdf.parse(dob) ?: return 0
            val today = Calendar.getInstance()
            val birth = Calendar.getInstance()
            birth.time = birthDate
            
            var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            0
        }
    }

    fun login(email: String, passwordRaw: String) {
        viewModelScope.launch {
            val result = repository.login(email, passwordRaw)
            result.onSuccess { user ->
                _messageFlow.emit("Welcome back, ${user.fullName}!")
            }.onFailure { err ->
                _messageFlow.emit("Login failed: ${err.message}")
            }
        }
    }

    fun qaBypassLogin() {
        viewModelScope.launch {
            val email = "qa@test.com"
            val password = "qa123456"
            val fullName = "QA Tester"

            // Try to login first
            val loginResult = repository.login(email, password)
            if (loginResult.isSuccess) {
                _messageFlow.emit("QA Bypass: Welcome back, $fullName!")
                return@launch
            }

            // If login fails (user doesn't exist), register them
            val newUser = UserEntity(
                userId = "user_qa_${UUID.randomUUID().toString().take(6)}",
                fullName = fullName,
                email = email,
                phone = "555-0199",
                role = "PATIENT",
                emergencyContactName = "QA Support",
                emergencyContactPhone = "555-9111",
                preferredLanguage = "English",
                isLoggedIn = true,
                isOnboardingCompleted = true
            )
            val regResult = repository.registerUser(newUser, password)
            regResult.onSuccess {
                _messageFlow.emit("QA Bypass: Account created and logged in!")
            }.onFailure { err ->
                _messageFlow.emit("QA Bypass failed: ${err.message}")
            }
        }
    }

    fun completeOnboarding() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.completeOnboarding(user.userId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.updateUserProfile(user.copy(isLoggedIn = false))
                _messageFlow.emit("Logged out.")
            }
        }
    }

    fun toggleSimpleMode(enabled: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user.copy(isSimpleModeEnabled = enabled))
            _messageFlow.emit(if (enabled) "Simple Mode enabled" else "Standard Mode enabled")
        }
    }

    fun saveMedication(
        id: String?,
        name: String,
        genericName: String,
        dosage: String,
        dosageUnit: String,
        frequency: String,
        scheduledTimes: String,
        instructions: String,
        stock: Int,
        minStock: Int,
        expiryMillis: Long,
        category: String,
        colorHex: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val medId = id ?: "med_${UUID.randomUUID().toString().take(8)}"
            val med = MedicationEntity(
                id = medId,
                userId = user.userId,
                medicineName = name,
                genericName = genericName,
                dosage = dosage,
                dosageUnit = dosageUnit,
                frequency = frequency,
                scheduledTimes = scheduledTimes,
                instructions = instructions,
                stockQuantity = stock,
                minimumStock = minStock,
                expiryDateMillis = expiryMillis,
                category = category,
                colorHex = colorHex
            )
            repository.saveMedication(med)
            _messageFlow.emit("Medication '$name' saved successfully.")
        }
    }

    fun deleteMedication(id: String) {
        viewModelScope.launch {
            repository.deleteMedication(id)
            _messageFlow.emit("Medication removed.")
        }
    }

    fun takeDose(doseId: String) {
        viewModelScope.launch {
            val res = repository.takeDose(doseId)
            res.onSuccess { msg -> _messageFlow.emit(msg) }
                .onFailure { err -> _messageFlow.emit("Error: ${err.message}") }
        }
    }

    fun skipDose(doseId: String, reason: String = "") {
        viewModelScope.launch {
            val res = repository.skipDose(doseId, reason)
            res.onSuccess { msg -> _messageFlow.emit(msg) }
                .onFailure { err -> _messageFlow.emit("Error: ${err.message}") }
        }
    }

    fun snoozeDose(doseId: String, minutes: Int = 15) {
        viewModelScope.launch {
            val res = repository.snoozeDose(doseId, minutes)
            res.onSuccess { msg -> _messageFlow.emit(msg) }
                .onFailure { err -> _messageFlow.emit("Error: ${err.message}") }
        }
    }

    fun addRefill(medicationId: String, medicineName: String, quantity: Int, pharmacy: String, cost: Double = 0.0, notes: String = "") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addRefill(
                medicationId = medicationId,
                userId = user.userId,
                medicineName = medicineName,
                quantity = quantity,
                pharmacy = pharmacy,
                cost = cost,
                notes = notes
            )
            _messageFlow.emit("Refill recorded: +$quantity units of $medicineName")
        }
    }

    fun savePrescription(title: String, doctor: String, hospital: String, date: String, medicines: String, notes: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val rx = PrescriptionEntity(
                id = "rx_${UUID.randomUUID().toString().take(8)}",
                userId = user.userId,
                title = title,
                doctorName = doctor,
                hospitalName = hospital,
                prescriptionDate = date,
                associatedMedicineNames = medicines,
                notes = notes
            )
            repository.savePrescription(rx)
            _messageFlow.emit("Prescription '$title' saved.")
        }
    }

    fun deletePrescription(id: String) {
        viewModelScope.launch {
            repository.deletePrescription(id)
            _messageFlow.emit("Prescription deleted.")
        }
    }

    fun saveAppointment(doctor: String, specialty: String, hospital: String, date: String, time: String, reason: String, notes: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val appt = AppointmentEntity(
                id = "appt_${UUID.randomUUID().toString().take(8)}",
                patientId = user.userId,
                doctorName = doctor,
                specialty = specialty,
                hospitalName = hospital,
                date = date,
                time = time,
                reason = reason,
                notes = notes
            )
            repository.saveAppointment(appt)
            _messageFlow.emit("Appointment scheduled with $doctor.")
        }
    }

    fun deleteAppointment(id: String) {
        viewModelScope.launch {
            repository.deleteAppointment(id)
            _messageFlow.emit("Appointment removed.")
        }
    }

    fun toggleAppointmentCompleted(id: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleAppointmentCompleted(id, isCompleted)
            _messageFlow.emit(if (isCompleted) "Appointment marked completed" else "Appointment marked upcoming")
        }
    }

    fun addCaregiver(name: String, phone: String, relation: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val caregiverRel = CaregiverRelationshipEntity(
                id = "rel_${UUID.randomUUID().toString().take(8)}",
                patientId = user.userId,
                patientName = user.fullName,
                caregiverId = "caregiver_${UUID.randomUUID().toString().take(6)}",
                caregiverName = name,
                caregiverPhone = phone,
                relationshipType = relation,
                status = "ACTIVE"
            )
            repository.saveCaregiverRelationship(caregiverRel)
            _messageFlow.emit("Caregiver '$name' connected successfully.")
        }
    }

    fun removeCaregiver(id: String) {
        viewModelScope.launch {
            repository.deleteCaregiverRelationship(id)
            _messageFlow.emit("Caregiver connection removed.")
        }
    }

    fun triggerSosEmergency() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.triggerSosEmergency(
                userId = user.userId,
                userName = user.fullName,
                emergencyContact = "${user.emergencyContactName} (${user.emergencyContactPhone})"
            )
            res.onSuccess {
                _messageFlow.emit("🚨 SOS DISPATCHED to Caregivers & Responders!")
            }
        }
    }

    fun markNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.userId)
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // --- AI Features with High Thinking ---
    fun analyzePrescriptionWithAi(rawText: String) {
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isLoading = true, error = null)
            val res = repository.aiService.analyzePrescriptionOrText(rawText)
            res.onSuccess { output ->
                _aiState.value = _aiState.value.copy(isLoading = false, resultText = output)
            }.onFailure { err ->
                _aiState.value = _aiState.value.copy(isLoading = false, error = err.message)
            }
        }
    }

    fun checkDrugInteractionsWithAi(meds: List<String>) {
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isLoading = true, error = null)
            val res = repository.aiService.checkMedicationInteractions(meds)
            res.onSuccess { output ->
                _aiState.value = _aiState.value.copy(isLoading = false, interactionText = output)
            }.onFailure { err ->
                _aiState.value = _aiState.value.copy(isLoading = false, error = err.message)
            }
        }
    }

    fun generateAdherenceSummaryWithAi(taken: Int, total: Int, missed: Int, language: String = "English") {
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isLoading = true, error = null)
            val res = repository.aiService.generateAdherenceSummary(taken, total, missed, language)
            res.onSuccess { output ->
                _aiState.value = _aiState.value.copy(isLoading = false, summaryText = output)
            }.onFailure { err ->
                _aiState.value = _aiState.value.copy(isLoading = false, error = err.message)
            }
        }
    }

    fun translateInstructionsWithAi(text: String, language: String) {
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isLoading = true, error = null)
            val res = repository.aiService.translateOrExplain(text, language)
            res.onSuccess { output ->
                _aiState.value = _aiState.value.copy(isLoading = false, translationText = output)
            }.onFailure { err ->
                _aiState.value = _aiState.value.copy(isLoading = false, error = err.message)
            }
        }
    }

    fun clearAiResults() {
        _aiState.value = AiAnalysisState()
    }

    // ==========================================
    // INTELLIGENT AI MEDICINE SCANNER
    // ==========================================
    val scannerService = MedicineScannerService(application)
    private val _scannerState = MutableStateFlow(MedicineScannerUiState())
    val scannerState: StateFlow<MedicineScannerUiState> = _scannerState.asStateFlow()

    fun scanMedicineBitmaps(front: Bitmap, back: Bitmap?) {
        viewModelScope.launch {
            _scannerState.value = _scannerState.value.copy(
                isScanning = true,
                error = null,
                isReviewing = false,
                isSavedSuccess = false
            )
            try {
                // Analyze front
                val frontInfo = scannerService.analyzeMedicineBitmap(front)
                
                // If back is provided, analyze and merge
                val finalInfo = if (back != null) {
                    val backInfo = scannerService.analyzeMedicineBitmap(back)
                    mergeScannedInfo(frontInfo, backInfo)
                } else {
                    frontInfo
                }

                val existingList = userMedications.value
                val validation = scannerService.validateMedicineInformation(finalInfo, existingList)

                _scannerState.value = _scannerState.value.copy(
                    isScanning = false,
                    scannedInfo = finalInfo,
                    validationResult = validation,
                    isReviewing = true
                )
            } catch (e: Exception) {
                _scannerState.value = _scannerState.value.copy(
                    isScanning = false,
                    error = "Failed to scan medicine: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    private fun mergeScannedInfo(front: ScannedMedicineInfo, back: ScannedMedicineInfo): ScannedMedicineInfo {
        // Preference for identity fields from front, safety fields from back
        return front.copy(
            genericName = if (front.genericName.isNotBlank()) front.genericName else back.genericName,
            manufacturer = if (back.manufacturer.isNotBlank()) back.manufacturer else front.manufacturer,
            batchNumber = if (back.batchNumber.isNotBlank()) back.batchNumber else front.batchNumber,
            manufacturingDate = if (back.manufacturingDate.isNotBlank()) back.manufacturingDate else front.manufacturingDate,
            expiryDate = if (back.expiryDate.isNotBlank()) back.expiryDate else front.expiryDate,
            composition = if (back.composition.isNotBlank()) back.composition else front.composition,
            confidence = (front.confidence + back.confidence) / 2f
        )
    }

    fun scanMedicineSampleText(sampleOcrText: String, imagePath: String? = null) {
        viewModelScope.launch {
            _scannerState.value = _scannerState.value.copy(
                isScanning = true,
                error = null,
                isReviewing = false,
                isSavedSuccess = false
            )
            try {
                val info = scannerService.parseTextToMedicineInfo(sampleOcrText, imagePath)
                val existingList = userMedications.value
                val validation = scannerService.validateMedicineInformation(info, existingList)

                _scannerState.value = _scannerState.value.copy(
                    isScanning = false,
                    scannedInfo = info,
                    validationResult = validation,
                    isReviewing = true
                )
            } catch (e: Exception) {
                _scannerState.value = _scannerState.value.copy(
                    isScanning = false,
                    error = "Extraction error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun updateScannedMedicineInfo(updatedInfo: ScannedMedicineInfo) {
        val existingList = userMedications.value
        val validation = scannerService.validateMedicineInformation(updatedInfo, existingList)
        _scannerState.value = _scannerState.value.copy(
            scannedInfo = updatedInfo,
            validationResult = validation
        )
    }

    fun confirmAndSaveScannedMedicine(
        info: ScannedMedicineInfo,
        frequency: String,
        scheduledTimes: String,
        stockQuantity: Int,
        minimumStock: Int,
        instructions: String,
        category: String,
        updateExistingId: String? = null
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val med = scannerService.createMedicineFromScan(
                info = info,
                userId = user.userId,
                frequency = frequency,
                scheduledTimes = scheduledTimes,
                stockQuantity = stockQuantity,
                minimumStock = minimumStock,
                instructions = instructions,
                category = category
            )

            val finalMed = if (!updateExistingId.isNullOrBlank()) {
                med.copy(id = updateExistingId)
            } else {
                med
            }

            repository.saveMedication(finalMed)
            _scannerState.value = _scannerState.value.copy(
                isReviewing = false,
                isSavedSuccess = true
            )
            _messageFlow.emit("Medicine '${finalMed.medicineName}' added to your schedule.")
        }
    }

    fun resetScanner() {
        _scannerState.value = MedicineScannerUiState()
    }
}
