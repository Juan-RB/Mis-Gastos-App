package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class RecurringExpenseWithCategory(
    @Embedded
    val recurringExpense: RecurringExpense,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category?
)
