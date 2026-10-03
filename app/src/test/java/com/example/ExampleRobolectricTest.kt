package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.MedicationEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MedCareRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: MedCareRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MedCareRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAppResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MedCare", appName)
    }

    @Test
    fun testSeedAndAuthenticationLifecycle() = runBlocking {
        // 1. Initial seed
        repository.initializeDemoDataIfEmpty()

        val allUsers = repository.allUsers.first()
        assertTrue("Users should be populated", allUsers.isNotEmpty())

        val currentUser = repository.currentLoggedInUser.first()
        assertNotNull("Default logged in user exists", currentUser)
        assertEquals("user_patient_101", currentUser?.userId)
        assertEquals("PATIENT", currentUser?.role)

        // 2. Register New User
        val newUserId = "user_test_999"
        val newUser = UserEntity(
            userId = newUserId,
            fullName = "Jane Doe",
            email = "jane.doe@example.com",
            phone = "+1 (555) 012-3456",
            role = "PATIENT",
            emergencyContactName = "John Doe",
            emergencyContactPhone = "+1 (555) 999-8888",
            preferredLanguage = "Spanish",
            isLoggedIn = true
        )
        repository.registerUser(newUser)

        val updatedUser = repository.currentLoggedInUser.first()
        assertNotNull(updatedUser)
        assertEquals(newUserId, updatedUser?.userId)
        assertEquals("Jane Doe", updatedUser?.fullName)

        // 3. Switch User back to Demo Caregiver
        val caregiver = allUsers.find { it.role == "CAREGIVER" }
        assertNotNull(caregiver)
        repository.switchUser(caregiver!!.userId)

        val activeCaregiver = repository.currentLoggedInUser.first()
        assertEquals(caregiver.userId, activeCaregiver?.userId)
        assertEquals("CAREGIVER", activeCaregiver?.role)
    }

    @Test
    fun testMedicationAndDoseLifecycle() = runBlocking {
        repository.initializeDemoDataIfEmpty()
        val patientId = "user_patient_101"

        // 1. Check seeded medications
        val initialMeds = repository.getMedicationsForUser(patientId).first()
        assertTrue("Should have seeded medications", initialMeds.isNotEmpty())

        val initialCount = initialMeds.size

        // 2. Add a new medication
        val newMed = MedicationEntity(
            id = "med_test_${UUID.randomUUID().toString().take(6)}",
            userId = patientId,
            medicineName = "Amoxicillin",
            dosage = "500",
            dosageUnit = "mg",
            instructions = "Take with food",
            scheduledTimes = "08:00,20:00",
            frequency = "Twice Daily",
            stockQuantity = 20,
            minimumStock = 5,
            category = "Antibiotic"
        )
        repository.saveMedication(newMed)

        val updatedMeds = repository.getMedicationsForUser(patientId).first()
        assertEquals(initialCount + 1, updatedMeds.size)

        // 3. Test Dose Operations
        val today = MedCareRepository.getTodayDateString()
        val doses = repository.getDoseRecordsForDate(patientId, today).first()
        assertTrue("Should have generated dose records for today", doses.isNotEmpty())

        val targetDose = doses.first()
        assertEquals("UPCOMING", targetDose.status)

        // Mark as TAKEN
        repository.takeDose(targetDose.id)
        val updatedDoses = repository.getDoseRecordsForDate(patientId, today).first()
        val updatedTargetDose = updatedDoses.find { it.id == targetDose.id }
        assertEquals("TAKEN", updatedTargetDose?.status)

        // Mark as SNOOZED
        if (doses.size > 1) {
            val secondDose = doses[1]
            repository.snoozeDose(secondDose.id, 15)
            val refreshed = repository.getDoseRecordsForDate(patientId, today).first()
            assertEquals("SNOOZED", refreshed.find { it.id == secondDose.id }?.status)
        }
    }

    @Test
    fun testRefillsAndSosDispatch() = runBlocking {
        repository.initializeDemoDataIfEmpty()
        val patient = repository.currentLoggedInUser.first()
        assertNotNull(patient)

        // 1. Refill request
        val meds = repository.getMedicationsForUser(patient!!.userId).first()
        val targetMed = meds.first()
        val stockBefore = targetMed.stockQuantity

        repository.addRefill(
            medicationId = targetMed.id,
            userId = patient.userId,
            medicineName = targetMed.medicineName,
            quantity = 30,
            pharmacy = "Central Rx"
        )

        val refreshedMeds = repository.getMedicationsForUser(patient.userId).first()
        val refreshedMed = refreshedMeds.find { it.id == targetMed.id }
        assertEquals(stockBefore + 30, refreshedMed?.stockQuantity)

        // 2. SOS Emergency trigger
        repository.triggerSosEmergency(
            userId = patient.userId,
            userName = patient.fullName,
            emergencyContact = patient.emergencyContactName
        )

        val sosEvents = repository.getSosEventsForUser(patient.userId).first()
        assertTrue("SOS event should be logged", sosEvents.isNotEmpty())
        assertEquals("DISPATCHED", sosEvents.first().status)

        val notifs = repository.getNotificationsForUser(patient.userId).first()
        val sosNotif = notifs.find { it.category == "SOS_ALERT" }
        assertNotNull("SOS notification should be created", sosNotif)
    }
}
