package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.CaregiverRelationshipEntity
import com.example.data.local.entity.DoseRecordEntity
import com.example.data.local.entity.MedicationEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.RefillRecordEntity
import com.example.data.local.entity.SosEventEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getCurrentLoggedInUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getCurrentLoggedInUserDirect(): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun clearLoginSessions()

    @Query("UPDATE users SET isLoggedIn = 1 WHERE userId = :userId")
    suspend fun setActiveUser(userId: String)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE userId = :userId ORDER BY medicineName ASC")
    fun getMedicationsForUser(userId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: String): MedicationEntity?

    @Query("SELECT * FROM medications WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveMedicationsForUserDirect(userId: String): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMedication(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: String)

    @Query("UPDATE medications SET stockQuantity = :newStock, updatedAt = :timestamp WHERE id = :medicationId")
    suspend fun updateStock(medicationId: String, newStock: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE medications SET isActive = :isActive, updatedAt = :timestamp WHERE id = :medicationId")
    suspend fun toggleActive(medicationId: String, isActive: Boolean, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface DoseRecordDao {
    @Query("SELECT * FROM dose_records WHERE userId = :userId ORDER BY scheduledDate DESC, scheduledTime DESC")
    fun getAllDoseRecordsForUser(userId: String): Flow<List<DoseRecordEntity>>

    @Query("SELECT * FROM dose_records WHERE userId = :userId AND scheduledDate = :date ORDER BY scheduledTime ASC")
    fun getDoseRecordsForDate(userId: String, date: String): Flow<List<DoseRecordEntity>>

    @Query("SELECT * FROM dose_records WHERE userId = :userId AND scheduledDate = :date ORDER BY scheduledTime ASC")
    suspend fun getDoseRecordsForDateDirect(userId: String, date: String): List<DoseRecordEntity>

    @Query("SELECT * FROM dose_records WHERE id = :id")
    suspend fun getDoseRecordById(id: String): DoseRecordEntity?

    @Query("SELECT * FROM dose_records WHERE userId = :userId AND medicationId = :medicationId AND scheduledDate = :date AND scheduledTime = :time LIMIT 1")
    suspend fun findExistingDose(userId: String, medicationId: String, date: String, time: String): DoseRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDoseRecord(doseRecord: DoseRecordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDoseRecordsIfAbsent(doseRecords: List<DoseRecordEntity>)

    @Query("UPDATE dose_records SET status = :status, actionTimeMillis = :actionTime, notes = :notes WHERE id = :doseId")
    suspend fun updateDoseStatus(doseId: String, status: String, actionTime: Long, notes: String = "")
}

@Dao
interface RefillRecordDao {
    @Query("SELECT * FROM refill_records WHERE userId = :userId ORDER BY refillDateMillis DESC")
    fun getRefillRecordsForUser(userId: String): Flow<List<RefillRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefillRecord(refill: RefillRecordEntity)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPrescriptionsForUser(userId: String): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionById(id: String): PrescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrescription(prescription: PrescriptionEntity)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deletePrescriptionById(id: String)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY date ASC, time ASC")
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppointment(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: String)

    @Query("UPDATE appointments SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setAppointmentCompleted(id: String, isCompleted: Boolean)
}

@Dao
interface CaregiverRelationshipDao {
    @Query("SELECT * FROM caregiver_relationships WHERE patientId = :patientId")
    fun getCaregiversForPatient(patientId: String): Flow<List<CaregiverRelationshipEntity>>

    @Query("SELECT * FROM caregiver_relationships WHERE caregiverId = :caregiverId AND status = 'ACTIVE'")
    fun getPatientsForCaregiver(caregiverId: String): Flow<List<CaregiverRelationshipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRelationship(relationship: CaregiverRelationshipEntity)

    @Query("DELETE FROM caregiver_relationships WHERE id = :id")
    suspend fun deleteRelationshipById(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestampMillis DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: String)
}

@Dao
interface SosEventDao {
    @Query("SELECT * FROM sos_events ORDER BY timestampMillis DESC")
    fun getAllSosEvents(): Flow<List<SosEventEntity>>

    @Query("SELECT * FROM sos_events WHERE userId = :userId ORDER BY timestampMillis DESC")
    fun getSosEventsForUser(userId: String): Flow<List<SosEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosEvent(sosEvent: SosEventEntity)

    @Query("UPDATE sos_events SET status = :status WHERE id = :id")
    suspend fun updateSosStatus(id: String, status: String)
}
