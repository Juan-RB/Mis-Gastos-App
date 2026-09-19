package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.Income
import com.example.data.model.RecurringExpense
import com.example.data.model.RecurringExpenseWithCategory
import com.example.util.CategoryIcons
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseDialog(
    expenseToEdit: ExpenseWithCategory?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (amount: Long, categoryId: Long, dateMillis: Long, note: String, id: Long) -> Unit,
    onAddNewCategoryClick: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = expenseToEdit != null
    val initialExpense = expenseToEdit?.expense

    var rawAmountString by remember {
        mutableStateOf(initialExpense?.amount?.toString() ?: "")
    }
    var selectedCategoryId by remember {
        mutableLongStateOf(
            initialExpense?.categoryId ?: (categories.firstOrNull()?.id ?: 1L)
        )
    }
    var selectedDateMillis by remember {
        mutableStateOf<Long?>(initialExpense?.dateMillis)
    }
    var noteText by remember {
        mutableStateOf(initialExpense?.note ?: "")
    }
    var amountError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    val calendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply {
            if (selectedDateMillis != null) {
                timeInMillis = selectedDateMillis!!
            }
        }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                selectedDateMillis = newCal.timeInMillis
                dateError = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar gasto" else "Nuevo gasto",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monto en CLP
                Column {
                    OutlinedTextField(
                        value = rawAmountString,
                        onValueChange = { newValue ->
                            val digits = newValue.filter { it.isDigit() }
                            rawAmountString = digits
                            amountError = digits.isEmpty() || (digits.toLongOrNull() ?: 0L) <= 0L
                        },
                        label = { Text("Monto en CLP *") },
                        prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("15000") },
                        isError = amountError,
                        supportingText = {
                            val amount = rawAmountString.toLongOrNull() ?: 0L
                            if (amountError) {
                                Text("Ingresa un monto válido mayor a $0")
                            } else if (amount > 0L) {
                                Text("Total: ${CurrencyUtils.formatClp(amount)}")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input")
                    )
                }

                // Categoría
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categoría *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(
                            onClick = onAddNewCategoryClick,
                            modifier = Modifier.testTag("dialog_new_category_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Nueva")
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = category.id == selectedCategoryId
                            val catColor = CategoryIcons.getColor(category.colorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = category.id },
                                label = { Text(category.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = CategoryIcons.getIcon(category.iconName),
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else catColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("category_chip_${category.id}")
                            )
                        }
                    }
                }

                // Selector de Fecha
                Column {
                    Text(
                        text = "Fecha *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Card(
                        onClick = { datePickerDialog.show() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = if (dateError) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("date_picker_trigger")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                if (selectedDateMillis != null) {
                                    Text(
                                        text = DateUtils.formatDisplayDate(selectedDateMillis!!),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = DateUtils.formatShortDate(selectedDateMillis!!),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "Seleccionar fecha *",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Toca aquí para indicar el día",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Seleccionar fecha",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (dateError) {
                        Text(
                            text = "Debes seleccionar una fecha",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }

                // Nota opcional
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Nota (opcional)") },
                    placeholder = { Text("Ej: Almuerzo de trabajo") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = rawAmountString.toLongOrNull() ?: 0L
                    var hasError = false
                    if (amount <= 0L) {
                        amountError = true
                        hasError = true
                    }
                    if (selectedDateMillis == null) {
                        dateError = true
                        hasError = true
                    }
                    if (hasError) return@Button

                    onSave(
                        amount,
                        selectedCategoryId,
                        selectedDateMillis!!,
                        noteText,
                        initialExpense?.id ?: 0L
                    )
                },
                modifier = Modifier.testTag("save_expense_button")
            ) {
                Text(if (isEditing) "Actualizar" else "Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_expense_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteExpenseConfirmationDialog(
    expenseWithCategory: ExpenseWithCategory,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val amountFormatted = CurrencyUtils.formatClp(expenseWithCategory.expense.amount)
    val categoryName = expenseWithCategory.category?.name ?: "Sin categoría"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar gasto?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "¿Estás seguro de que deseas eliminar este gasto de $amountFormatted ($categoryName)?",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (expenseWithCategory.expense.note.isNotBlank()) {
                    Text(
                        text = "\"${expenseWithCategory.expense.note}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: Long, iconName: String, monthlyLimit: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColorHex by remember {
        mutableLongStateOf(CategoryIcons.AVAILABLE_COLORS.first())
    }
    var selectedIconName by remember {
        mutableStateOf(CategoryIcons.AVAILABLE_ICONS.first().first)
    }
    var rawMonthlyLimit by remember { mutableStateOf("") }
    val parsedMonthlyLimit = CurrencyUtils.parseClpInput(rawMonthlyLimit)
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva categoría",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nombre de categoría *") },
                    placeholder = { Text("Ej: Mascotas, Gimnasio") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("El nombre no puede estar vacío")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                // Límite de gasto mensual (opcional)
                OutlinedTextField(
                    value = rawMonthlyLimit,
                    onValueChange = { input ->
                        rawMonthlyLimit = input.filter { it.isDigit() }
                    },
                    label = { Text("Límite mensual (CLP, opcional)") },
                    placeholder = { Text("Ej: 150000 (sin límite si vacío)") },
                    prefix = { Text("$ ") },
                    supportingText = {
                        if (parsedMonthlyLimit > 0L) {
                            Text(
                                text = "Tope mensual: ${CurrencyUtils.formatClp(parsedMonthlyLimit)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text("Opcional. Permite monitorear alertas de presupuesto.")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_monthly_limit_input")
                )

                // Selector de color
                Column {
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CategoryIcons.AVAILABLE_COLORS) { colorHex ->
                            val color = Color(colorHex)
                            val isSelected = colorHex == selectedColorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColorHex = colorHex }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Selector de ícono
                Column {
                    Text(
                        text = "Ícono",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 44.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(CategoryIcons.AVAILABLE_ICONS) { (iconKey, vector) ->
                                val isSelected = iconKey == selectedIconName
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { selectedIconName = iconKey }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = vector,
                                            contentDescription = iconKey,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    onSave(
                        name.trim(),
                        selectedColorHex,
                        selectedIconName,
                        if (parsedMonthlyLimit > 0L) parsedMonthlyLimit else null
                    )
                },
                modifier = Modifier.testTag("save_category_button")
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_category_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditCategoryLimitDialog(
    category: Category,
    onDismiss: () -> Unit,
    onSaveLimit: (Long?) -> Unit
) {
    var rawMonthlyLimit by remember { mutableStateOf("") }
    val parsedMonthlyLimit = CurrencyUtils.parseClpInput(rawMonthlyLimit)
    val catColor = CategoryIcons.getColor(category.colorHex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.2f))
                        .border(1.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryIcons.getIcon(category.iconName),
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Límite: ${category.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Define un monto máximo de gasto mensual (en pesos chilenos) para esta categoría. Verás el avance en tiempo real con alertas de semáforo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rawMonthlyLimit,
                    onValueChange = { input ->
                        rawMonthlyLimit = input.filter { it.isDigit() }
                    },
                    label = { Text("Límite mensual (CLP)") },
                    placeholder = {
                        Text(
                            if (category.monthlyLimit != null && category.monthlyLimit!! > 0L) {
                                "Actual: ${CurrencyUtils.formatClp(category.monthlyLimit!!)}"
                            } else {
                                "Ej: 100000"
                            }
                        )
                    },
                    prefix = { Text("$ ") },
                    supportingText = {
                        if (parsedMonthlyLimit > 0L) {
                            Text(
                                text = "Nuevo presupuesto: ${CurrencyUtils.formatClp(parsedMonthlyLimit)} al mes",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (category.monthlyLimit != null && category.monthlyLimit!! > 0L) {
                            Text("Límite actual asignado: ${CurrencyUtils.formatClp(category.monthlyLimit!!)}")
                        } else {
                            Text("Deja vacío o ingresa 0 para quitar el límite.")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_category_limit_input")
                )

                if (category.monthlyLimit != null && category.monthlyLimit > 0L) {
                    OutlinedButton(
                        onClick = {
                            rawMonthlyLimit = ""
                            onSaveLimit(null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("remove_category_limit_button")
                    ) {
                        Text("Quitar límite (sin restricción)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalLimit = if (parsedMonthlyLimit > 0L) parsedMonthlyLimit else null
                    onSaveLimit(finalLimit)
                },
                modifier = Modifier.testTag("save_category_limit_button")
            ) {
                Text("Guardar límite")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_category_limit_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteCategoryConfirmationDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar categoría?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de eliminar la categoría '${category.name}'? Si existen gastos registrados en ella, se reasignarán automáticamente a otra categoría para no perder tus datos.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_category_button")
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_category_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditRecurringExpenseDialog(
    itemToEdit: RecurringExpenseWithCategory?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Long, categoryId: Long, dueDayOfMonth: Int, note: String, id: Long) -> Unit,
    onAddNewCategoryClick: () -> Unit
) {
    val isEditing = itemToEdit != null
    val initial = itemToEdit?.recurringExpense

    var nameInput by remember { mutableStateOf(initial?.name ?: "") }
    var rawAmountString by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var selectedCategoryId by remember {
        mutableLongStateOf(initial?.categoryId ?: (categories.firstOrNull()?.id ?: 1L))
    }
    var dueDayInput by remember {
        mutableStateOf(initial?.dueDayOfMonth?.toString() ?: "")
    }
    var noteText by remember { mutableStateOf(initial?.note ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var dayError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_edit_recurring_dialog"),
        title = {
            Text(
                text = if (isEditing) "Editar gasto fijo" else "Nuevo gasto fijo mensual",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Nombre del gasto fijo
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        nameError = false
                    },
                    label = { Text("Nombre (ej: Arriendo, Luz, Agua, Internet)") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("Ingresa el nombre del servicio o cuenta", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recurring_name_input")
                )

                // Monto en CLP
                OutlinedTextField(
                    value = rawAmountString,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 11) {
                            rawAmountString = digits
                            amountError = false
                        }
                    },
                    label = { Text("Monto mensual en pesos (CLP)") },
                    prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError,
                    supportingText = {
                        val parsed = rawAmountString.toLongOrNull()
                        if (amountError) {
                            Text("Ingresa un monto válido mayor a $0", color = MaterialTheme.colorScheme.error)
                        } else if (parsed != null && parsed > 0) {
                            Text("Formato: ${CurrencyUtils.formatClp(parsed)}", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recurring_amount_input")
                )

                // Día del mes en que vence
                OutlinedTextField(
                    value = dueDayInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 2) {
                            dueDayInput = digits
                            dayError = false
                        }
                    },
                    label = { Text("Día del mes en que vence (1 - 31)") },
                    placeholder = { Text("Ej: 5 o 10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = dayError,
                    supportingText = {
                        if (dayError) {
                            Text("El día debe ser entre 1 y 31", color = MaterialTheme.colorScheme.error)
                        } else if (dueDayInput.isNotBlank()) {
                            Text("Se recordará cada mes el día $dueDayInput")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recurring_day_input")
                )

                // Categoría
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categoría",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onAddNewCategoryClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Nueva categoría", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            val catColor = CategoryIcons.getColor(cat.colorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = CategoryIcons.getIcon(cat.iconName),
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else catColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // Nota opcional
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Nota opcional") },
                    placeholder = { Text("Ej: Número de cliente o enlace de pago") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recurring_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = rawAmountString.toLongOrNull() ?: 0L
                    val day = dueDayInput.toIntOrNull() ?: 0

                    var hasError = false
                    if (nameInput.trim().isEmpty()) {
                        nameError = true
                        hasError = true
                    }
                    if (amount <= 0) {
                        amountError = true
                        hasError = true
                    }
                    if (day !in 1..31) {
                        dayError = true
                        hasError = true
                    }

                    if (!hasError) {
                        onSave(
                            nameInput.trim(),
                            amount,
                            selectedCategoryId,
                            day,
                            noteText.trim(),
                            initial?.id ?: 0L
                        )
                    }
                },
                modifier = Modifier.testTag("save_recurring_button")
            ) {
                Text(if (isEditing) "Actualizar" else "Guardar gasto fijo")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_recurring_button")) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ConfirmRegisterRecurringDialog(
    item: RecurringExpenseWithCategory,
    monthName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val rec = item.recurringExpense
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Registrar en $monthName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "¿Deseas registrar este gasto fijo como una transacción real en tu lista de gastos de $monthName?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = rec.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Monto: ${CurrencyUtils.formatClp(rec.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Fecha: Día ${rec.dueDayOfMonth} de $monthName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_register_recurring_btn")
            ) {
                Text("Registrar ahora")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_register_recurring_btn")) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteRecurringConfirmationDialog(
    item: RecurringExpenseWithCategory,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar gasto fijo?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de eliminar el gasto fijo '${item.recurringExpense.name}'? Las transacciones que ya se hayan registrado previamente en los meses anteriores no se borrarán.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_recurring_btn")
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_delete_recurring_btn")) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// Diálogos para Ingreso / Sueldo Base
// ==========================================
@Composable
fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Long, dateMillis: Long, isBaseSalary: Boolean, note: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var rawAmount by remember { mutableStateOf("") }
    var isBaseSalary by remember { mutableStateOf(false) }
    var dateMillis by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }

    var amountError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    val parsedAmount = CurrencyUtils.parseClpInput(rawAmount)

    val initialDateCal = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                dateMillis = DateUtils.toMillis(selectedYear, selectedMonth, selectedDay)
                dateError = false
            },
            initialDateCal.get(Calendar.YEAR),
            initialDateCal.get(Calendar.MONTH),
            initialDateCal.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Añadir Ingreso",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Monto
                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        rawAmount = digits
                        amountError = (digits.toLongOrNull() ?: 0L) <= 0L
                    },
                    label = { Text("Monto * (CLP)") },
                    placeholder = { Text("Ej: 850000") },
                    prefix = { Text("$ ") },
                    supportingText = {
                        if (amountError) {
                            Text("Ingresa un monto válido mayor a 0", color = MaterialTheme.colorScheme.error)
                        } else if (parsedAmount > 0L) {
                            Text(CurrencyUtils.formatClp(parsedAmount), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    isError = amountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_amount_input")
                )

                // Nombre / Concepto
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre o concepto") },
                    placeholder = { Text("Ej: Sueldo base, Honorarios, Bono") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_name_input")
                )

                // Es sueldo base
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isBaseSalary = !isBaseSalary }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isBaseSalary,
                        onCheckedChange = { isBaseSalary = it }
                    )
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Fijar como sueldo base principal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Se mostrará en la cabecera superior y restará los gastos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Selector de Fecha
                OutlinedTextField(
                    value = dateMillis?.let { DateUtils.formatFullDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha *") },
                    placeholder = { Text("Seleccionar fecha") },
                    isError = dateError,
                    supportingText = {
                        if (dateError) Text("Debes seleccionar una fecha", color = MaterialTheme.colorScheme.error)
                    },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("income_date_picker_button")
                )

                // Notas opcionales
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota (opcional)") },
                    placeholder = { Text("Detalle o recordatorio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (parsedAmount <= 0L) {
                        amountError = true
                        hasError = true
                    }
                    if (dateMillis == null) {
                        dateError = true
                        hasError = true
                    }
                    if (hasError) return@Button

                    val finalName = if (name.isBlank()) "Ingreso" else name.trim()
                    onSave(finalName, parsedAmount, dateMillis!!, isBaseSalary, note.trim())
                },
                modifier = Modifier.testTag("save_income_button")
            ) {
                Text("Guardar Ingreso")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// Diálogos para Deudas
// ==========================================
@Composable
fun AddEditDebtDialog(
    debtToEdit: Debt?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        totalAmount: Long,
        paidAmount: Long,
        totalInstallments: Int,
        paidInstallments: Int,
        dueDateMillis: Long?,
        note: String,
        id: Long
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(debtToEdit?.title ?: "") }
    var rawTotalAmount by remember { mutableStateOf(debtToEdit?.totalAmount?.toString() ?: "") }
    var rawPaidAmount by remember { mutableStateOf(if (debtToEdit != null) debtToEdit.paidAmount.toString() else "") }
    var rawTotalInstallments by remember { mutableStateOf(if (debtToEdit != null) debtToEdit.totalInstallments.toString() else "") }
    var rawPaidInstallments by remember { mutableStateOf(if (debtToEdit != null) debtToEdit.paidInstallments.toString() else "") }
    var dueDateMillis by remember { mutableStateOf(debtToEdit?.dueDateMillis) }
    var note by remember { mutableStateOf(debtToEdit?.note ?: "") }

    var titleError by remember { mutableStateOf(false) }
    var totalAmountError by remember { mutableStateOf(false) }

    val parsedTotalAmount = CurrencyUtils.parseClpInput(rawTotalAmount)
    val parsedPaidAmount = CurrencyUtils.parseClpInput(rawPaidAmount)

    val datePickerDialog = remember {
        val ymd = DateUtils.getYearMonthDay(dueDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                dueDateMillis = DateUtils.toMillis(selectedYear, selectedMonth, selectedDay)
            },
            ymd.first,
            ymd.second,
            ymd.third
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (debtToEdit != null) "Editar Cuota" else "Nueva Cuota",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Título / Concepto
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    label = { Text("Concepto de la cuota *") },
                    placeholder = { Text("Ej: Tarjeta Santander, Préstamo auto") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text("El nombre o concepto es obligatorio")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debt_title_input")
                )

                // Monto Total
                OutlinedTextField(
                    value = rawTotalAmount,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        rawTotalAmount = digits
                        totalAmountError = (digits.toLongOrNull() ?: 0L) <= 0L
                    },
                    label = { Text("Monto total de la deuda * (CLP)") },
                    placeholder = { Text("Ej: 500000") },
                    prefix = { Text("$ ") },
                    supportingText = {
                        if (totalAmountError) {
                            Text("Ingresa un monto válido", color = MaterialTheme.colorScheme.error)
                        } else if (parsedTotalAmount > 0L) {
                            Text(CurrencyUtils.formatClp(parsedTotalAmount), color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    isError = totalAmountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debt_total_amount_input")
                )

                // Monto ya pagado
                OutlinedTextField(
                    value = rawPaidAmount,
                    onValueChange = { input ->
                        rawPaidAmount = input.filter { it.isDigit() }
                    },
                    label = { Text("Monto que llevas pagado (CLP)") },
                    placeholder = { Text("Ej: 0") },
                    prefix = { Text("$ ") },
                    supportingText = {
                        if (parsedPaidAmount > 0L) {
                            Text("Abonado: ${CurrencyUtils.formatClp(parsedPaidAmount)}")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debt_paid_amount_input")
                )

                // Cuotas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = rawTotalInstallments,
                        onValueChange = { input ->
                            rawTotalInstallments = input.filter { it.isDigit() }
                        },
                        label = { Text("Total Cuotas") },
                        placeholder = { Text("Ej: 1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("debt_total_installments_input")
                    )

                    OutlinedTextField(
                        value = rawPaidInstallments,
                        onValueChange = { input ->
                            rawPaidInstallments = input.filter { it.isDigit() }
                        },
                        label = { Text("Cuotas Pagadas") },
                        placeholder = { Text("Ej: 0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("debt_paid_installments_input")
                    )
                }

                // Fecha límite opcional
                OutlinedTextField(
                    value = dueDateMillis?.let { DateUtils.formatFullDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha límite / Vencimiento") },
                    placeholder = { Text("Sin fecha límite (opcional)") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (dueDateMillis != null) {
                                IconButton(onClick = { dueDateMillis = null }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Quitar fecha límite", modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("debt_due_date_picker")
                )

                // Notas
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota (opcional)") },
                    placeholder = { Text("Detalles, tasa de interés, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debt_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    if (parsedTotalAmount <= 0L) {
                        totalAmountError = true
                        return@Button
                    }
                    val totalInstallments = rawTotalInstallments.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val paidInstallments = rawPaidInstallments.toIntOrNull()?.coerceIn(0, totalInstallments) ?: 0

                    onSave(
                        title.trim(),
                        parsedTotalAmount,
                        parsedPaidAmount,
                        totalInstallments,
                        paidInstallments,
                        dueDateMillis,
                        note.trim(),
                        debtToEdit?.id ?: 0L
                    )
                },
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("save_debt_button")
            ) {
                Text(if (debtToEdit != null) "Guardar Cambios" else "Crear Cuota", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                    width = 1.dp
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun PayDebtDialog(
    debt: Debt,
    availableBalance: Long,
    onDismiss: () -> Unit,
    onConfirm: (paymentAmount: Long, advanceInstallments: Int) -> Unit
) {
    // Monto sugerido: si tiene cuotas y faltan cuotas, sugerir monto de 1 cuota
    val remainingInstallments = (debt.totalInstallments - debt.paidInstallments).coerceAtLeast(1)
    val suggestedCuota = if (debt.totalInstallments > 1 && remainingInstallments > 0) {
        (debt.remainingAmount / remainingInstallments).coerceAtLeast(1000L)
    } else {
        debt.remainingAmount
    }

    var rawAmount by remember { mutableStateOf("") }
    var advanceInstallment by remember { mutableStateOf(debt.totalInstallments > 1) }
    var amountError by remember { mutableStateOf(false) }

    val parsedAmount = CurrencyUtils.parseClpInput(rawAmount)
    val hasInsufficientFunds = availableBalance <= 0L
    val exceedsBalance = parsedAmount > availableBalance
    val isAmountValid = parsedAmount > 0L && !exceedsBalance && !hasInsufficientFunds

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Abonar a ${debt.title}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info resumida de deuda y dinero en cuenta
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dinero en tu cuenta:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                CurrencyUtils.formatClp(availableBalance),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (availableBalance > 0L) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Saldo pendiente deuda:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                CurrencyUtils.formatClp(debt.remainingAmount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (debt.totalInstallments > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cuotas:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${debt.paidInstallments} de ${debt.totalInstallments} pagadas",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (hasInsufficientFunds) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Dinero insuficiente en tu cuenta (${CurrencyUtils.formatClp(availableBalance)}). No puedes realizar un abono hasta registrar ingresos o ajustar tu sueldo base.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Monto a pagar
                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        rawAmount = digits
                        amountError = (digits.toLongOrNull() ?: 0L) <= 0L
                    },
                    enabled = !hasInsufficientFunds,
                    label = { Text("Monto a abonar * (CLP)") },
                    placeholder = {
                        val suggested = minOf(suggestedCuota, debt.remainingAmount)
                        Text("Ej: ${CurrencyUtils.formatClp(suggested)}")
                    },
                    prefix = { Text("$ ") },
                    supportingText = {
                        if (hasInsufficientFunds) {
                            Text("Dinero en cuenta insuficiente", color = MaterialTheme.colorScheme.error)
                        } else if (exceedsBalance) {
                            Text(
                                "Supera el dinero en tu cuenta (${CurrencyUtils.formatClp(availableBalance)})",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (amountError) {
                            Text("Ingresa un monto mayor a 0", color = MaterialTheme.colorScheme.error)
                        } else if (parsedAmount > debt.remainingAmount) {
                            Text(
                                "Supera el saldo de la deuda (${CurrencyUtils.formatClp(debt.remainingAmount)})",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (parsedAmount > 0L) {
                            Text(
                                "Abono: ${CurrencyUtils.formatClp(parsedAmount)} • Quedarán ${CurrencyUtils.formatClp(availableBalance - parsedAmount)} en cuenta",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    isError = amountError || exceedsBalance || (hasInsufficientFunds && rawAmount.isNotEmpty()),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pay_debt_amount_input")
                )

                if (debt.totalInstallments > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { advanceInstallment = !advanceInstallment },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = advanceInstallment,
                            onCheckedChange = { advanceInstallment = it },
                            enabled = !hasInsufficientFunds
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Contar como 1 cuota pagada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isAmountValid) {
                        amountError = parsedAmount <= 0L
                        return@Button
                    }
                    onConfirm(parsedAmount, if (advanceInstallment) 1 else 0)
                },
                enabled = isAmountValid,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("confirm_pay_debt_btn")
            ) {
                Text("Registrar Pago", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                    width = 1.dp
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun DeleteDebtConfirmationDialog(
    debt: Debt,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar cuota?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar '${debt.title}' de tu registro de cuotas? Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_debt_btn")
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun MarkDebtAsPaidConfirmationDialog(
    debt: Debt,
    availableBalance: Long,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val remainingAmount = debt.remainingAmount
    val hasInsufficientFunds = remainingAmount > 0L && remainingAmount > availableBalance

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (hasInsufficientFunds) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "¿Marcar deuda como pagada?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (remainingAmount > 0L) {
                    Text(
                        text = "Al marcar '${debt.title}' como totalmente pagada, se registrará el saldo restante de ${CurrencyUtils.formatClp(remainingAmount)} como un gasto automático en la categoría Créditos y se descontará de tu sueldo base.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dinero en tu cuenta:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    CurrencyUtils.formatClp(availableBalance),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasInsufficientFunds) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monto a descontar:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    CurrencyUtils.formatClp(remainingAmount),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    if (hasInsufficientFunds) {
                        Text(
                            text = "⚠️ Dinero insuficiente en tu cuenta para cubrir los ${CurrencyUtils.formatClp(remainingAmount)}. No puedes marcarla como pagada hasta contar con saldo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = "¿Deseas marcar '${debt.title}' como totalmente pagada?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !hasInsufficientFunds,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("confirm_mark_debt_paid_btn")
            ) {
                Text("Confirmar y Pagar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    )
}


