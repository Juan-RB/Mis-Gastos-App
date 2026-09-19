package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["category_id"])]
)
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Ej: Luz, Agua, Arriendo, Internet
    val amount: Long, // Monto en CLP
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    val dueDayOfMonth: Int, // Día del mes en que vence (1 - 31)
    val note: String = "",
    val isActive: Boolean = true
)
