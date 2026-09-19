package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.util.NotificationHelper
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val TAG = "ReminderScheduler"
    private const val UNIQUE_PERIODIC_WORK_NAME = "mis_gastos_daily_reminders"
    private const val UNIQUE_IMMEDIATE_WORK_NAME = "mis_gastos_immediate_reminder"

    private fun getWorkManagerSafely(context: Context): WorkManager? {
        return try {
            WorkManager.getInstance(context.applicationContext)
        } catch (e: IllegalStateException) {
            try {
                val config = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
                WorkManager.initialize(context.applicationContext, config)
                WorkManager.getInstance(context.applicationContext)
            } catch (initEx: Exception) {
                Log.w(TAG, "Unable to initialize WorkManager", initEx)
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to access WorkManager", e)
            null
        }
    }

    fun scheduleDailyReminder(context: Context) {
        try {
            val workManager = getWorkManagerSafely(context) ?: return

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                24, TimeUnit.HOURS,
                6, TimeUnit.HOURS // flex interval
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling daily reminder", e)
        }
    }

    fun cancelReminders(context: Context) {
        try {
            val workManager = getWorkManagerSafely(context) ?: return
            workManager.cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminders", e)
        }
    }

    fun triggerImmediateCheck(context: Context) {
        try {
            val workManager = getWorkManagerSafely(context) ?: return
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .build()

            workManager.enqueueUniqueWork(
                UNIQUE_IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering immediate check", e)
        }
    }

    fun sendTestNotification(context: Context) {
        NotificationHelper.showReminderNotification(
            context = context,
            notificationId = 99999,
            title = "Notificación de prueba - Mis Gastos",
            message = "¡Los recordatorios están configurados correctamente! Te avisaremos antes de que venzan tus deudas o gastos fijos.",
            subText = "Mis Gastos"
        )
    }
}
