package com.example.util

import androidx.compose.ui.graphics.Color

object BudgetUtils {
    // Colores semafóricos exclusivos para estados de presupuesto según las especificaciones:
    // Verde (#55DB9C): 0% a 69% del límite usado
    // Amarillo / Naranja (#FFD731 / #FB4903): 70% a 89%
    // Rojo (#E53935): 90% a 100% y alerta si supera el 100%
    val GreenColor = Color(0xFF55DB9C)
    val OrangeColor = Color(0xFFFB4903)
    val YellowColor = Color(0xFFFFD731)
    val RedColor = Color(0xFFE53935)

    fun getStatusColor(fraction: Float): Color {
        return when {
            fraction < 0.70f -> GreenColor
            fraction < 0.90f -> OrangeColor
            else -> RedColor
        }
    }

    fun getStatusColor(spent: Long, limit: Long?): Color {
        if (limit == null || limit <= 0L) return GreenColor
        val fraction = spent.toFloat() / limit.toFloat()
        return getStatusColor(fraction)
    }

    fun calculatePercent(spent: Long, limit: Long?): Int {
        if (limit == null || limit <= 0L) return 0
        return ((spent.toDouble() / limit.toDouble()) * 100).toInt()
    }
}
