package com.example.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class DoseReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val doseId = intent.getStringExtra("dose_id") ?: return
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val dosage = intent.getStringExtra("dosage") ?: ""

        showNotification(context, doseId, medicineName, dosage)
    }

    private fun showNotification(context: Context, doseId: String, name: String, dosage: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent reminders for your medications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Buttons
        val takeIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = "ACTION_TAKEN"
            putExtra("dose_id", doseId)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.hashCode() + 1,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = "ACTION_SKIPPED"
            putExtra("dose_id", doseId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.hashCode() + 2,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using system icon for now
            .setContentTitle("💊 Time for your medicine!")
            .setContentText("Take $name $dosage now.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_save, "TAKEN", takePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "SKIP", skipPendingIntent)
            .build()

        notificationManager.notify(doseId.hashCode(), notification)
    }
}
