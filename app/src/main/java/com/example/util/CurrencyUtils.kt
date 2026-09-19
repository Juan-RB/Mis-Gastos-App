package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val clpSymbols = DecimalFormatSymbols(Locale("es", "CL")).apply {
        groupingSeparator = '.'
        currencySymbol = "$"
    }

    private val clpFormatter = DecimalFormat("$#,##0", clpSymbols)

    /**
     * Formatea un monto en pesos chilenos sin decimales con punto de miles.
     * Ejemplos:
     * 15000 -> "$15.000"
     * 1250000 -> "$1.250.000"
     * 0 -> "$0"
     */
    fun formatClp(amount: Long): String {
        return clpFormatter.format(amount)
    }

    /**
     * Limpia la entrada de texto para conservar sólo números enteros.
     */
    fun parseClpDigits(text: String): Long {
        val digitsOnly = text.filter { it.isDigit() }
        return digitsOnly.toLongOrNull() ?: 0L
    }

    fun parseClpInput(text: String): Long = parseClpDigits(text)
}
