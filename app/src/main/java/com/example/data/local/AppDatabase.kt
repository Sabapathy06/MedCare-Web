package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AppointmentDao
import com.example.data.local.dao.CaregiverRelationshipDao
import com.example.data.local.dao.DoseRecordDao
import com.example.data.local.dao.MedicationDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PrescriptionDao
import com.example.data.local.dao.RefillRecordDao
import com.example.data.local.dao.SosEventDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.CaregiverRelationshipEntity
import com.example.data.local.entity.DoseRecordEntity
import com.example.data.local.entity.MedicationEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.RefillRecordEntity
import com.example.data.local.entity.SosEventEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        MedicationEntity::class,
        DoseRecordEntity::class,
        RefillRecordEntity::class,
        PrescriptionEntity::class,
        AppointmentEntity::class,
        CaregiverRelationshipEntity::class,
        NotificationEntity::class,
        SosEventEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao
    abstract fun doseRecordDao(): DoseRecordDao
    abstract fun refillRecordDao(): RefillRecordDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun caregiverRelationshipDao(): CaregiverRelationshipDao
    abstract fun notificationDao(): NotificationDao
    abstract fun sosEventDao(): SosEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medcare_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
