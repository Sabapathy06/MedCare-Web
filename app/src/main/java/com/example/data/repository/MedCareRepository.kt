package com.example.data.repository

import com.example.data.ai.GeminiAiService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

import com.example.data.local.ReminderManager

class MedCareRepository(
    private val database: AppDatabase,
    val aiService: GeminiAiService = GeminiAiService()
) {
    private var reminderManager: ReminderManager? = null

    fun initReminderManager(context: android.content.Context) {
        reminderManager = ReminderManager(context)
    }
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val userDao = database.userDao()
    private val medicationDao = database.medicationDao()
    private val doseRecordDao = database.doseRecordDao()
    private val refillRecordDao = database.refillRecordDao()
    private val prescriptionDao = database.prescriptionDao()
    private val appointmentDao = database.appointmentDao()
    private val caregiverDao = database.caregiverRelationshipDao()
    private val notificationDao = database.notificationDao()
    private val sosDao = database.sosEventDao()

    val currentLoggedInUser: Flow<UserEntity?> = userDao.getCurrentLoggedInUser()

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun login(email: String, passwordRaw: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth.signInWithEmailAndPassword(email, passwordRaw).await()
                    val firebaseUser = authResult.user ?: throw Exception("User not found")
                    
                    var localUser = userDao.getUserById(firebaseUser.uid)
                    if (localUser == null) {
                        localUser = fetchUserFromFirestore(firebaseUser.uid)
                        if (localUser != null) {
                            userDao.insertOrUpdateUser(localUser)
                        } else {
                            localUser = UserEntity(
                                userId = firebaseUser.uid,
                                fullName = firebaseUser.displayName ?: "User",
                                email = firebaseUser.email ?: "",
                                phone = "",
                                isLoggedIn = true
                            )
                            userDao.insertOrUpdateUser(localUser)
                        }
                    } else {
                        userDao.insertOrUpdateUser(localUser.copy(isLoggedIn = true))
                    }
                    
                    syncDataFromFirestore(firebaseUser.uid)
                    return@withContext Result.success(localUser!!)
                } catch (e: Exception) {
                    // Fallback to local login if Firebase fails (e.g., no internet or user only exists locally)
                }
            }
            
            // Local Login Fallback
            val localUser = userDao.getUserByEmail(email)
            if (localUser != null && localUser.passwordHash == passwordRaw) { // Using raw comparison for simplicity in debug builds
                userDao.setActiveUser(localUser.userId)
                return@withContext Result.success(localUser)
            }
            
            throw Exception("Invalid credentials or user not found")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(user: UserEntity, passwordRaw: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val firebaseAuth = auth
            var finalUser = user.copy(passwordHash = passwordRaw)
            
            if (firebaseAuth != null) {
                try {
                    val authResult = firebaseAuth.createUserWithEmailAndPassword(user.email, passwordRaw).await()
                    val firebaseUser = authResult.user ?: throw Exception("Registration failed")
                    
                    finalUser = finalUser.copy(userId = firebaseUser.uid, isLoggedIn = true)
                    userDao.insertOrUpdateUser(finalUser)
                    
                    val userMap = mapOf(
                        "fullName" to finalUser.fullName,
                        "email" to finalUser.email,
                        "phone" to finalUser.phone,
                        "role" to finalUser.role,
                        "emergencyContactName" to finalUser.emergencyContactName,
                        "emergencyContactPhone" to finalUser.emergencyContactPhone,
                        "preferredLanguage" to finalUser.preferredLanguage,
                        "isOnboardingCompleted" to finalUser.isOnboardingCompleted,
                        "createdAt" to finalUser.createdAt
                    )
                    firestore?.collection("users")?.document(firebaseUser.uid)?.set(userMap)?.await()
                    
                    return@withContext Result.success(finalUser)
                } catch (e: Exception) {
                    // Fallback to local registration if Firebase fails
                }
            }
            
            // Local Registration Fallback
            finalUser = finalUser.copy(isLoggedIn = true)
            userDao.insertOrUpdateUser(finalUser)
            Result.success(finalUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val uid = auth?.currentUser?.uid
        if (uid != null) {
            val localUser = userDao.getUserById(uid)
            if (localUser != null) {
                userDao.insertOrUpdateUser(localUser.copy(isLoggedIn = false))
            }
        }
        auth?.signOut()
    }

    suspend fun switchUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.clearLoginSessions()
        userDao.setActiveUser(userId)
    }

    suspend fun completeOnboarding(userId: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        if (user != null) {
            userDao.insertOrUpdateUser(user.copy(isOnboardingCompleted = true))
            try {
                firestore?.collection("users")?.document(userId)?.update("isOnboardingCompleted", true)?.await()
            } catch (e: Exception) {}
        }
    }

    suspend fun updateUserProfile(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertOrUpdateUser(user)
        try {
            val userMap = mapOf(
                "fullName" to user.fullName,
                "phone" to user.phone,
                "emergencyContactName" to user.emergencyContactName,
                "emergencyContactPhone" to user.emergencyContactPhone,
                "preferredLanguage" to user.preferredLanguage
            )
            firestore?.collection("users")?.document(user.userId)?.update(userMap)?.await()
        } catch (e: Exception) {}
    }

    private suspend fun fetchUserFromFirestore(userId: String): UserEntity? {
        return try {
            val doc = firestore?.collection("users")?.document(userId)?.get()?.await()
            if (doc != null && doc.exists()) {
                UserEntity(
                    userId = userId,
                    fullName = doc.getString("fullName") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    role = doc.getString("role") ?: "PATIENT",
                    emergencyContactName = doc.getString("emergencyContactName") ?: "",
                    emergencyContactPhone = doc.getString("emergencyContactPhone") ?: "",
                    preferredLanguage = doc.getString("preferredLanguage") ?: "English",
                    isOnboardingCompleted = doc.getBoolean("isOnboardingCompleted") ?: false,
                    isLoggedIn = true
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun syncDataFromFirestore(userId: String) = withContext(Dispatchers.IO) {
        // Basic sync implementation for medications
        try {
            val meds = firestore?.collection("users")?.document(userId)?.collection("medications")?.get()?.await()
            if (meds != null) {
                for (doc in meds.documents) {
                    val med = MedicationEntity(
                        id = doc.id,
                        userId = userId,
                        medicineName = doc.getString("medicineName") ?: "",
                        genericName = doc.getString("genericName") ?: "",
                        brandName = doc.getString("brandName") ?: "",
                        dosage = doc.getString("dosage") ?: "",
                        dosageUnit = doc.getString("dosageUnit") ?: "mg",
                        dosageForm = doc.getString("dosageForm") ?: "Tablet",
                        frequency = doc.getString("frequency") ?: "Once daily",
                        scheduledTimes = doc.getString("scheduledTimes") ?: "08:00",
                        instructions = doc.getString("instructions") ?: "",
                        stockQuantity = doc.getLong("stockQuantity")?.toInt() ?: 0,
                        minimumStock = doc.getLong("minimumStock")?.toInt() ?: 5,
                        expiryDateMillis = doc.getLong("expiryDateMillis") ?: 0L,
                        category = doc.getString("category") ?: "General",
                        colorHex = doc.getString("colorHex") ?: "#1E88E5",
                        isActive = doc.getBoolean("isActive") ?: true
                    )
                    medicationDao.insertOrUpdateMedication(med)
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    // --- Medications ---
    fun getMedicationsForUser(userId: String): Flow<List<MedicationEntity>> =
        medicationDao.getMedicationsForUser(userId)

    suspend fun getMedicationById(id: String): MedicationEntity? = withContext(Dispatchers.IO) {
        medicationDao.getMedicationById(id)
    }

    suspend fun saveMedication(med: MedicationEntity) = withContext(Dispatchers.IO) {
        medicationDao.insertOrUpdateMedication(med)
        generateDosesForDate(med.userId, getTodayDateString())
        checkSingleMedicationAlerts(med)
        
        // Sync to Firestore
        try {
            firestore?.collection("users")?.document(med.userId)
                ?.collection("medications")?.document(med.id)
                ?.set(med)?.await()
        } catch (e: Exception) {}
    }

    private suspend fun checkSingleMedicationAlerts(med: MedicationEntity) {
        val now = System.currentTimeMillis()
        
        // 1. Stock check
        if (med.stockQuantity <= med.minimumStock) {
            val notif = NotificationEntity(
                id = "alert_stock_${med.id}_${now}",
                userId = med.userId,
                title = "Low Stock: ${med.medicineName}",
                message = "Only ${med.stockQuantity} units left. Please refill soon.",
                category = "LOW_STOCK",
                timestampMillis = now
            )
            notificationDao.insertNotification(notif)
        }

        // 2. Expiry check
        val daysToExpiry = (med.expiryDateMillis - now) / (1000 * 60 * 60 * 24)
        if (daysToExpiry < 0 && med.expiryDateMillis > 0) {
            val notif = NotificationEntity(
                id = "alert_expiry_expired_${med.id}_${now}",
                userId = med.userId,
                title = "EXPIRED: ${med.medicineName}",
                message = "This medicine expired on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(med.expiryDateMillis))}. Do not use.",
                category = "EXPIRY_WARNING",
                timestampMillis = now
            )
            notificationDao.insertNotification(notif)
        } else if (daysToExpiry <= 30 && med.expiryDateMillis > 0) {
            val notif = NotificationEntity(
                id = "alert_expiry_soon_${med.id}_${now}",
                userId = med.userId,
                title = "Expiring Soon: ${med.medicineName}",
                message = "Expires in $daysToExpiry days. Check your stock.",
                category = "EXPIRY_WARNING",
                timestampMillis = now
            )
            notificationDao.insertNotification(notif)
        }
    }

    suspend fun checkAllMedicationAlerts(userId: String) = withContext(Dispatchers.IO) {
        val meds = medicationDao.getActiveMedicationsForUserDirect(userId)
        for (med in meds) {
            checkSingleMedicationAlerts(med)
        }
    }

    suspend fun checkMissedDoses(userId: String) = withContext(Dispatchers.IO) {
        val today = getTodayDateString()
        val doses = doseRecordDao.getDoseRecordsForDateDirect(userId, today)
        val now = Calendar.getInstance()
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)

        for (dose in doses) {
            if (dose.status == "UPCOMING" || dose.status == "SNOOZED") {
                if (isTimePastThreshold(dose.scheduledTime, currentTimeStr, 120)) {
                    val updatedDose = dose.copy(status = "MISSED", notes = "Dose not confirmed within 2 hours")
                    doseRecordDao.insertOrUpdateDoseRecord(updatedDose)
                    
                    val notif = NotificationEntity(
                        id = "missed_${dose.id}_${System.currentTimeMillis()}",
                        userId = userId,
                        title = "Dose Not Confirmed",
                        message = "Your scheduled dose of ${dose.medicineName} at ${dose.scheduledTime} was not confirmed.",
                        category = "MISSED_DOSE",
                        timestampMillis = System.currentTimeMillis()
                    )
                    notificationDao.insertNotification(notif)
                }
            }
        }
    }

    private fun isTimePastThreshold(scheduledTime: String, currentTime: String, thresholdMinutes: Int): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val scheduled = sdf.parse(scheduledTime)
            val current = sdf.parse(currentTime)
            if (scheduled != null && current != null) {
                val diff = (current.time - scheduled.time) / (60 * 1000)
                diff > thresholdMinutes
            } else false
        } catch (e: Exception) { false }
    }

    suspend fun deleteMedication(id: String) = withContext(Dispatchers.IO) {
        medicationDao.deleteMedicationById(id)
    }

    suspend fun toggleMedicationActive(id: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        medicationDao.toggleActive(id, isActive)
    }

    // --- Dose Management with Idempotency & Stock Sync ---
    fun getDoseRecordsForDate(userId: String, date: String): Flow<List<DoseRecordEntity>> =
        doseRecordDao.getDoseRecordsForDate(userId, date)

    fun getAllDoseRecords(userId: String): Flow<List<DoseRecordEntity>> =
        doseRecordDao.getAllDoseRecordsForUser(userId)

    suspend fun generateDosesForDate(userId: String, date: String) = withContext(Dispatchers.IO) {
        val activeMeds = medicationDao.getActiveMedicationsForUserDirect(userId)
        for (med in activeMeds) {
            val times = med.scheduledTimes.split(",").map { it.trim() }.filter { it.isNotBlank() }
            for (time in times) {
                val existing = doseRecordDao.findExistingDose(userId, med.id, date, time)
                if (existing == null) {
                    val dose = DoseRecordEntity(
                        id = "dose_${med.id}_${date}_${time.replace(":", "")}",
                        medicationId = med.id,
                        userId = userId,
                        medicineName = med.medicineName,
                        dosage = med.dosage,
                        dosageUnit = med.dosageUnit,
                        scheduledDate = date,
                        scheduledTime = time,
                        status = "UPCOMING"
                    )
                    doseRecordDao.insertOrUpdateDoseRecord(dose)
                    reminderManager?.scheduleReminder(dose)
                }
            }
        }
    }

    suspend fun takeDose(doseId: String): Result<String> = withContext(Dispatchers.IO) {
        val dose = doseRecordDao.getDoseRecordById(doseId)
            ?: return@withContext Result.failure(Exception("Dose not found"))

        if (dose.status == "TAKEN") {
            return@withContext Result.success("Already marked as taken")
        }

        reminderManager?.cancelReminder(doseId)

        val now = System.currentTimeMillis()
        val updatedDose = dose.copy(
            status = "TAKEN", 
            actionTimeMillis = now, 
            notes = "Confirmed taken by patient"
        )
        doseRecordDao.insertOrUpdateDoseRecord(updatedDose)

        // Sync dose record
        try {
            firestore?.collection("users")?.document(dose.userId)
                ?.collection("doseRecords")?.document(dose.id)
                ?.set(updatedDose)?.await()
        } catch (e: Exception) {}

        // Decrease stock
        val med = medicationDao.getMedicationById(dose.medicationId)
        if (med != null) {
            val newStock = (med.stockQuantity - 1).coerceAtLeast(0)
            val updatedMed = med.copy(stockQuantity = newStock)
            saveMedication(updatedMed)
        }

        Result.success("Dose recorded as Taken. Stock updated.")
    }

    suspend fun skipDose(doseId: String, reason: String = ""): Result<String> = withContext(Dispatchers.IO) {
        val dose = doseRecordDao.getDoseRecordById(doseId)
            ?: return@withContext Result.failure(Exception("Dose not found"))

        reminderManager?.cancelReminder(doseId)

        val now = System.currentTimeMillis()
        doseRecordDao.updateDoseStatus(doseId, "SKIPPED", now, reason.ifBlank { "Skipped by user" })

        // Create alert for caregivers
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = dose.userId,
            title = "Dose Skipped: ${dose.medicineName}",
            message = "Scheduled dose at ${dose.scheduledTime} was skipped. Follow your doctor's instructions regarding skipped doses.",
            category = "CAREGIVER_ALERT",
            timestampMillis = now
        )
        notificationDao.insertNotification(notif)

        Result.success("Dose recorded as Skipped.")
    }

    suspend fun snoozeDose(doseId: String, minutes: Int = 15): Result<String> = withContext(Dispatchers.IO) {
        val dose = doseRecordDao.getDoseRecordById(doseId)
            ?: return@withContext Result.failure(Exception("Dose not found"))

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, minutes)
        val snoozeTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)

        val updatedDose = dose.copy(
            status = "SNOOZED",
            snoozeUntilTime = snoozeTimeStr,
            notes = "Snoozed for $minutes mins until $snoozeTimeStr"
        )
        doseRecordDao.insertOrUpdateDoseRecord(updatedDose)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = dose.userId,
            title = "Medication Snoozed: ${dose.medicineName}",
            message = "Reminder postponed by $minutes mins. Next reminder at $snoozeTimeStr.",
            category = "MEDICATION_REMINDER",
            timestampMillis = now
        )
        notificationDao.insertNotification(notif)

        Result.success("Snoozed for $minutes minutes.")
    }

    // --- Refills ---
    fun getRefillsForUser(userId: String): Flow<List<RefillRecordEntity>> =
        refillRecordDao.getRefillRecordsForUser(userId)

    suspend fun addRefill(
        medicationId: String,
        userId: String,
        medicineName: String,
        quantity: Int,
        pharmacy: String,
        cost: Double = 0.0,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val refill = RefillRecordEntity(
            id = UUID.randomUUID().toString(),
            medicationId = medicationId,
            userId = userId,
            medicineName = medicineName,
            quantityAdded = quantity,
            refillDateMillis = System.currentTimeMillis(),
            pharmacyName = pharmacy.ifBlank { "Local Pharmacy" },
            cost = cost,
            notes = notes
        )
        refillRecordDao.insertRefillRecord(refill)

        // Increment stock
        val med = medicationDao.getMedicationById(medicationId)
        if (med != null) {
            val newStock = med.stockQuantity + quantity
            medicationDao.updateStock(medicationId, newStock)
        }
    }

    // --- Prescriptions ---
    fun getPrescriptionsForUser(userId: String): Flow<List<PrescriptionEntity>> =
        prescriptionDao.getPrescriptionsForUser(userId)

    suspend fun savePrescription(prescription: PrescriptionEntity) = withContext(Dispatchers.IO) {
        prescriptionDao.insertOrUpdatePrescription(prescription)
    }

    suspend fun deletePrescription(id: String) = withContext(Dispatchers.IO) {
        prescriptionDao.deletePrescriptionById(id)
    }

    // --- Appointments ---
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getAppointmentsForPatient(patientId)

    suspend fun saveAppointment(appointment: AppointmentEntity) = withContext(Dispatchers.IO) {
        appointmentDao.insertOrUpdateAppointment(appointment)
    }

    suspend fun deleteAppointment(id: String) = withContext(Dispatchers.IO) {
        appointmentDao.deleteAppointmentById(id)
    }

    suspend fun toggleAppointmentCompleted(id: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        appointmentDao.setAppointmentCompleted(id, isCompleted)
    }

    // --- Caregivers ---
    fun getCaregiversForPatient(patientId: String): Flow<List<CaregiverRelationshipEntity>> =
        caregiverDao.getCaregiversForPatient(patientId)

    fun getPatientsForCaregiver(caregiverId: String): Flow<List<CaregiverRelationshipEntity>> =
        caregiverDao.getPatientsForCaregiver(caregiverId)

    suspend fun saveCaregiverRelationship(relationship: CaregiverRelationshipEntity) = withContext(Dispatchers.IO) {
        caregiverDao.insertOrUpdateRelationship(relationship)
    }

    suspend fun deleteCaregiverRelationship(id: String) = withContext(Dispatchers.IO) {
        caregiverDao.deleteRelationshipById(id)
    }

    // --- Notifications ---
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    fun getUnreadNotificationCount(userId: String): Flow<Int> =
        notificationDao.getUnreadCount(userId)

    suspend fun markAllNotificationsAsRead(userId: String) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun deleteNotification(id: String) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotificationById(id)
    }

    // --- SOS Emergency Workflow ---
    fun getSosEventsForUser(userId: String): Flow<List<SosEventEntity>> =
        sosDao.getSosEventsForUser(userId)

    fun getAllSosEvents(): Flow<List<SosEventEntity>> =
        sosDao.getAllSosEvents()

    suspend fun triggerSosEmergency(userId: String, userName: String, emergencyContact: String): Result<SosEventEntity> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val sos = SosEventEntity(
            id = "sos_${UUID.randomUUID()}",
            userId = userId,
            userName = userName,
            timestampMillis = now,
            simulatedLocation = "Home (13.0827° N, 80.2707° E)",
            status = "DISPATCHED",
            emergencyContactNotified = emergencyContact.ifBlank { "911 / Emergency Services + Caregivers" },
            notes = "Urgent SOS alert dispatched to all registered caregivers and emergency responders."
        )
        sosDao.insertSosEvent(sos)

        // Generate Urgent SOS notification
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = "EMERGENCY SOS DISPATCHED",
            message = "Emergency alert was triggered for $userName. Responders and Caregiver ($emergencyContact) notified.",
            category = "SOS_ALERT",
            timestampMillis = now
        )
        notificationDao.insertNotification(notif)

        Result.success(sos)
    }

    // --- Helper Utilities ---
    companion object {
        fun getTodayDateString(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        fun getPastDateString(daysAgo: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }
    }
}
