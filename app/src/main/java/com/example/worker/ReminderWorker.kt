package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = appContext.getSharedPreferences("mis_gastos_reminders", Context.MODE_PRIVATE)
        val remindersEnabled = prefs.getBoolean("reminders_enabled", true)
        if (!remindersEnabled) {
            return Result.success()
        }

        val daysInAdvance = prefs.getInt("days_in_advance", 3)

        val database = AppDatabase.getDatabase(appContext)
        val recurringDao = database.recurringExpenseDao()
        val expenseDao = database.expenseDao()
        val debtDao = database.debtDao()

        val currentYear = DateUtils.getCurrentYear()
        val currentMonth = DateUtils.getCurrentMonth()
        val currentDay = DateUtils.getCurrentDayOfMonth()

        // 1. Verificar Gastos Fijos próximos a vencer
        try {
            val recurringList = recurringDao.getAllRecurringExpenses().first()
            val startOfMonth = DateUtils.getStartOfMonth(currentYear, currentMonth)
            val endOfMonth = DateUtils.getEndOfOfMonth(currentYear, currentMonth)
            val monthExpenses = expenseDao.getExpensesByDateRange(startOfMonth, endOfMonth).first()

            for (recWithCat in recurringList) {
                val item = recWithCat.recurringExpense
                if (!item.isActive) continue

                // Verificar si ya fue registrado este mes
                val isRegistered = monthExpenses.any { expItem ->
                    val expense = expItem.expense
                    expense.note.contains("[Fijo: ${item.name}]", ignoreCase = true) ||
                            (expense.categoryId == item.categoryId &&
                                    expense.amount == item.amount &&
                                    expense.note.contains(item.name, ignoreCase = true))
                }

                if (!isRegistered) {
                    val daysLeft = item.dueDayOfMonth - currentDay
                    if (daysLeft in 0..daysInAdvance) {
                        val title = when (daysLeft) {
                            0 -> "¡Hoy vence tu gasto fijo!"
                            1 -> "Gasto fijo vence mañana"
                            else -> "Gasto fijo por vencer en $daysLeft días"
                        }
                        val message = "${item.name} (${CurrencyUtils.formatClp(item.amount)}) vence el día ${item.dueDayOfMonth} de este mes."

                        NotificationHelper.showReminderNotification(
                            context = appContext,
                            notificationId = (10000 + item.id).toInt(),
                            title = title,
                            message = message,
                            subText = "Gasto Fijo Mensual"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Continuar con deudas si falla gastos fijos
        }

        // 2. Verificar Deudas pendientes con fecha límite próxima
        try {
            val allDebts = debtDao.getAllDebts().first()
            val pendingDebts = allDebts.filter { !it.isPaid && it.dueDateMillis != null }

            val todayStartMillis = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            for (debt in pendingDebts) {
                val dueMillis = debt.dueDateMillis ?: continue
                val diffMillis = dueMillis - todayStartMillis
                val daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

                if (daysLeft in 0..daysInAdvance) {
                    val pendingAmount = (debt.totalAmount - debt.paidAmount).coerceAtLeast(0L)
                    val title = when (daysLeft) {
                        0 -> "¡Hoy vence tu deuda!"
                        1 -> "Deuda por vencer mañana"
                        else -> "Deuda por vencer en $daysLeft días"
                    }
                    val message = "${debt.title} (Saldo: ${CurrencyUtils.formatClp(pendingAmount)}) vence el ${DateUtils.formatShortDate(dueMillis)}."

                    NotificationHelper.showReminderNotification(
                        context = appContext,
                        notificationId = (20000 + debt.id).toInt(),
                        title = title,
                        message = message,
                        subText = "Deuda Pendiente"
                    )
                }
            }
        } catch (e: Exception) {
            // Continuar
        }

        return Result.success()
    }
}
