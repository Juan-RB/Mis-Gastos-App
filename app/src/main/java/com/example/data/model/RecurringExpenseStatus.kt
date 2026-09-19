package com.example.data.model

data class RecurringExpenseStatus(
    val item: RecurringExpenseWithCategory,
    val isRegisteredThisMonth: Boolean,
    val daysUntilDue: Int,
    val isDueToday: Boolean,
    val isOverdue: Boolean
)
