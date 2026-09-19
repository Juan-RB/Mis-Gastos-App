package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.ExpenseWithCategory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun generateCsv(expenses: List<ExpenseWithCategory>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sb = StringBuilder()
        
        // UTF-8 BOM so spreadsheet apps (Excel, Sheets) detect UTF-8 accents correctly
        sb.append('\uFEFF')
        // Encabezados
        sb.append("ID;Fecha;Categoría;Monto (CLP);Nota\n")
        
        for (item in expenses) {
            val exp = item.expense
            val catName = item.category?.name ?: "Sin categoría"
            val dateStr = sdf.format(Date(exp.dateMillis))
            val noteEscaped = exp.note.replace("\"", "\"\"")
            
            sb.append("${exp.id};$dateStr;\"$catName\";${exp.amount};\"$noteEscaped\"\n")
        }
        return sb.toString()
    }

    fun exportAndShare(context: Context, expenses: List<ExpenseWithCategory>): Intent? {
        val csvContent = generateCsv(expenses)
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "mis_gastos_$timestamp.csv")
            
            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }
            
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Respaldo Mis Gastos - $timestamp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
