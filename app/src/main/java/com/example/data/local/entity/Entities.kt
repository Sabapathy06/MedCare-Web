package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String = "",
    val gender: String = "Other",
    val role: String = "PATIENT", // PATIENT, CAREGIVER, DOCTOR, PHARMACIST, ADMIN
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val preferredLanguage: String = "English",
    val profileImageUrl: String = "",
    val isLoggedIn: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val isSimpleModeEnabled: Boolean = false,
    val passwordHash: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val medicineName: String,
    val genericName: String = "",
    val brandName: String = "",
    val dosage: String,
    val dosageUnit: String = "mg", // mg, ml, tablet, capsule, drops, puffs, units
    val dosageForm: String = "Tablet", // Tablet, Capsule, Syrup, Drops, Injection, Inhaler, Ointment
    val frequency: String = "Once daily", // Once daily, Twice daily, Three times daily, Four times daily, Custom
    val scheduledTimes: String = "08:00", // Comma-separated HH:mm, e.g. "08:00,20:00"
    val startDate: String = "",
    val endDate: String = "",
    val instructions: String = "Take with water after meal",
    val stockQuantity: Int = 30,
    val minimumStock: Int = 5,
    val expiryDateMillis: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000), // default 90 days ahead
    val expiryDate: String = "", // Formatted expiry date e.g. "2027-08-15" or "August 2027"
    val manufacturingDate: String = "", // Formatted mfg date e.g. "2025-08-10" or "August 2025"
    val manufacturer: String = "",
    val batchNumber: String = "",
    val composition: String = "",
    val imageUrl: String = "",
    val prescriptionId: String? = null,
    val category: String = "General",
    val colorHex: String = "#1E88E5",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dose_records")
data class DoseRecordEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val userId: String,
    val medicineName: String,
    val dosage: String,
    val dosageUnit: String,
    val scheduledDate: String, // YYYY-MM-DD
    val scheduledTime: String, // HH:mm
    val actionTimeMillis: Long? = null,
    val status: String = "UPCOMING", // UPCOMING, TAKEN, SKIPPED, SNOOZED, MISSED
    val snoozeUntilTime: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "refill_records")
data class RefillRecordEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val userId: String,
    val medicineName: String,
    val quantityAdded: Int,
    val refillDateMillis: Long = System.currentTimeMillis(),
    val pharmacyName: String = "Local Pharmacy",
    val cost: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val doctorName: String,
    val hospitalName: String = "",
    val prescriptionDate: String,
    val notes: String = "",
    val fileUri: String = "",
    val associatedMedicineNames: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val doctorName: String,
    val specialty: String = "General Physician",
    val hospitalName: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val reason: String = "Routine Checkup",
    val notes: String = "",
    val reminderMinutesBefore: Int = 60,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "caregiver_relationships")
data class CaregiverRelationshipEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val patientName: String,
    val caregiverId: String,
    val caregiverName: String,
    val caregiverPhone: String,
    val relationshipType: String = "Family Caregiver",
    val status: String = "ACTIVE", // ACTIVE, PENDING, REVOKED
    val canViewDoses: Boolean = true,
    val canReceiveMissedAlerts: Boolean = true,
    val canReceiveSos: Boolean = true,
    val canManageRefills: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val category: String = "MEDICATION_REMINDER", // MEDICATION_REMINDER, MISSED_DOSE, LOW_STOCK, EXPIRY_WARNING, APPOINTMENT_REMINDER, CAREGIVER_ALERT, SOS_ALERT
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionData: String = ""
)

@Entity(tableName = "sos_events")
data class SosEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val simulatedLocation: String = "Home - Lat: 13.0827, Lon: 80.2707",
    val status: String = "DISPATCHED", // DISPATCHED, ACKNOWLEDGED, RESOLVED
    val emergencyContactNotified: String = "",
    val notes: String = ""
)
