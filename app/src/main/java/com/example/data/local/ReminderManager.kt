package com.example.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.entity.DoseRecordEntity
import com.example.data.receiver.DoseReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

class ReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(dose: DoseRecordEntity) {
        val intent = Intent(context, DoseReminderReceiver::class.java).apply {
            putExtra("dose_id", dose.id)
            putExtra("medicine_name", dose.medicineName)
            putExtra("dosage", "${dose.dosage} ${dose.dosageUnit}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dose.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val doseTime = sdf.parse("${dose.scheduledDate} ${dose.scheduledTime}") ?: return
        
        calendar.time = doseTime

        // If time is in the past, don't schedule for today
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("ReminderManager", "Scheduled reminder for ${dose.medicineName} at ${sdf.format(calendar.time)}")
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to schedule alarm: ${e.message}")
        }
    }

    fun cancelReminder(doseId: String) {
        val intent = Intent(context, DoseReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
