package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalAmount: Long,
    val paidAmount: Long = 0L,
    val totalInstallments: Int = 1,
    val paidInstallments: Int = 0,
    val dueDateMillis: Long? = null,
    val isPaid: Boolean = false,
    val note: String = ""
) {
    val remainingAmount: Long
        get() = (totalAmount - paidAmount).coerceAtLeast(0L)

    val progress: Float
        get() = if (totalAmount > 0) (paidAmount.toFloat() / totalAmount.toFloat()).coerceIn(0f, 1f) else 0f
}
