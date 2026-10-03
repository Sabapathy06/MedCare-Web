package com.example.data.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.repository.MedCareRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MedicationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val doseId = intent.getStringExtra("dose_id") ?: return
        val action = intent.action

        val db = AppDatabase.getInstance(context)
        val repository = MedCareRepository(db)

        scope.launch {
            when (action) {
                "ACTION_TAKEN" -> repository.takeDose(doseId)
                "ACTION_SKIPPED" -> repository.skipDose(doseId, "Skipped from notification")
            }
            
            // Dismiss the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(doseId.hashCode())
        }
    }
}
