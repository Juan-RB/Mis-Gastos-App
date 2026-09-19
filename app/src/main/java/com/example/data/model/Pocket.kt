package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PocketType {
    SAVINGS,
    APARTADO
}

@Entity(tableName = "pockets")
data class Pocket(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Long? = null,
    val currentAmount: Long = 0L,
    val colorHex: Long = 0xFF4DA2FFL,
    val iconName: String = "savings",
    val type: String = PocketType.SAVINGS.name
) {
    val isSavings: Boolean get() = type == PocketType.SAVINGS.name || type == "SAVINGS"
    val isApartado: Boolean get() = type == PocketType.APARTADO.name || type == "APARTADO" || type == "EXPENSE_ALLOCATION"
    val hasTarget: Boolean get() = targetAmount != null && targetAmount > 0L
    val progressFraction: Float
        get() = if (hasTarget) (currentAmount.toFloat() / targetAmount!!.toFloat()).coerceIn(0f, 1f) else 0f
    val progressPercent: Int
        get() = if (hasTarget) ((currentAmount.toDouble() / targetAmount!!.toDouble()) * 100).toInt() else 0
    val isTargetReached: Boolean
        get() = hasTarget && currentAmount >= targetAmount!!
}

@Entity(
    tableName = "pocket_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Pocket::class,
            parentColumns = ["id"],
            childColumns = ["pocketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pocketId")]
)
data class PocketTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pocketId: Long,
    val amount: Long,
    val dateMillis: Long = System.currentTimeMillis(),
    val type: String // "DEPOSITAR", "RETIRAR"
)
