package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.ExpenseWithCategory
import com.example.ui.theme.BowlbyOneFontFamily
import com.example.util.BudgetUtils
import com.example.util.CategoryIcons
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun ExpensesListScreen(
    uiState: ExpensesUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
    onToggleMonthFilter: (Boolean) -> Unit,
    onSelectCategoryFilter: (Long?) -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onEditExpenseClick: (ExpenseWithCategory) -> Unit,
    onDeleteExpenseClick: (ExpenseWithCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isFabMenuExpanded) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            isFabMenuExpanded = false
                            onAddIncomeClick()
                        },
                        shape = RoundedCornerShape(percent = 50),
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        text = { Text("Añadir Ingreso", fontWeight = FontWeight.Bold) },
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(percent = 50))
                            .testTag("floating_add_income_btn")
                    )

                    ExtendedFloatingActionButton(
                        onClick = {
                            isFabMenuExpanded = false
                            onAddExpenseClick()
                        },
                        shape = RoundedCornerShape(percent = 50),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                        text = { Text("Añadir gasto", fontWeight = FontWeight.Bold) },
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(percent = 50))
                            .testTag("floating_add_expense_btn")
                    )
                }

                FloatingActionButton(
                    onClick = { isFabMenuExpanded = !isFabMenuExpanded },
                    shape = RoundedCornerShape(percent = 50),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(percent = 50))
                        .testTag("add_expense_fab")
                ) {
                    Icon(
                        imageVector = if (isFabMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isFabMenuExpanded) "Cerrar opciones" else "Añadir gasto o ingreso"
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Selector de mes unificado (fuera de la tarjeta financiera)
            MonthHeaderBar(
                selectedYear = uiState.selectedYear,
                selectedMonth = uiState.selectedMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onResetMonth = onResetMonth,
                testTagPrefix = "expenses",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tarjeta Financiera: Sueldo Base, Saldo Libre y Total Gastos del Mes
            HeroMonthCard(
                uiState = uiState,
                onToggleMonthFilter = onToggleMonthFilter,
                onAddIncomeClick = onAddIncomeClick
            )

            // Filtros de categoría horizontales estilo sticker
            CategoryFilterBar(
                categories = uiState.categories,
                categorySummaries = uiState.categorySummaries,
                selectedCategoryId = uiState.selectedCategoryId,
                onSelectCategory = onSelectCategoryFilter,
                totalCount = uiState.filteredExpenses.size
            )

            // Lista de transacciones
            if (uiState.filteredExpenses.isEmpty()) {
                EmptyExpensesState(
                    isFilterActive = uiState.selectedCategoryId != null || uiState.isMonthFilterActive,
                    onAddExpenseClick = onAddExpenseClick
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("expenses_list")
                ) {
                    items(
                        items = uiState.filteredExpenses,
                        key = { it.expense.id }
                    ) { item ->
                        ExpenseItemCard(
                            item = item,
                            onClick = { onEditExpenseClick(item) },
                            onEditClick = { onEditExpenseClick(item) },
                            onDeleteClick = { onDeleteExpenseClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroMonthCard(
    uiState: ExpensesUiState,
    onToggleMonthFilter: (Boolean) -> Unit,
    onAddIncomeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ========================================================
            // SECCIÓN FINANCIERA PRINCIPAL: FORMATO APARTADOS DE BOLSILLO
            // Fila superior: Sueldo Base (Lavanda sólido) y Saldo Libre
            // Fila inferior: Total Gastos del Mes (Verde tipo 'Queda sin asignar')
            // ========================================================
            val isDark = isSystemInDarkTheme()
            val salaryBg = Color(0xFF8F7BBE)
            val freeBalanceBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val totalGastosBg = if (isDark) Color(0xFF55DB9C).copy(alpha = 0.18f) else Color(0xFF55DB9C).copy(alpha = 0.25f)
            val isPositive = uiState.freeAvailableBalance >= 0L

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sueldo Base (Lavanda Sólido - Todo el box es botón de ajuste con texto negro)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = salaryBg,
                    border = BorderStroke(1.5.dp, Color(0xFF000000)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onAddIncomeClick() }
                        .testTag("base_salary_header_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SUELDO BASE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000).copy(alpha = 0.85f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatClp(uiState.baseSalary),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Saldo Libre (Estilo 'Saldo Libre' de Apartados Hero Card, sin 'Disponible')
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = freeBalanceBg,
                    border = BorderStroke(1.5.dp, Color(0xFF000000)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SALDO LIBRE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatClp(uiState.freeAvailableBalance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("free_balance_text")
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Fila Inferior destacada: TOTAL GASTOS DEL MES (Mismo formato y color verde que 'QUEDA SIN ASIGNAR')
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = totalGastosBg,
                border = BorderStroke(2.dp, Color(0xFF000000)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL GASTOS DEL MES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = CurrencyUtils.formatClp(uiState.filteredTotal),
                            fontFamily = BowlbyOneFontFamily,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("monthly_total_text")
                        )
                    }

                    if (uiState.totalPocketsAmount > 0L) {
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            border = BorderStroke(1.5.dp, Color(0xFF000000))
                        ) {
                            Text(
                                text = "En bolsillos: ${CurrencyUtils.formatClp(uiState.totalPocketsAmount)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Selector de filtro por mes (activo / ver todo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.filteredExpenses.size} transacciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Surface(
                    onClick = { onToggleMonthFilter(!uiState.isMonthFilterActive) },
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.secondary,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.testTag("toggle_month_filter_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isMonthFilterActive) "Filtrando por mes" else "Viendo todos",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilterBar(
    categories: List<Category>,
    categorySummaries: List<CategorySummary> = emptyList(),
    selectedCategoryId: Long?,
    onSelectCategory: (Long?) -> Unit,
    totalCount: Int
) {
    // Organizar de manera predeterminada en orden de la que más se está usando en ese mes (De mayor a menor gasto)
    val sortedCategories = remember(categories, categorySummaries) {
        val amountsMap = categorySummaries.associate { it.category.id to it.totalAmount }
        val countMap = categorySummaries.associate { it.category.id to it.count }
        categories.sortedWith(
            compareByDescending<Category> { amountsMap[it.id] ?: 0L }
                .thenByDescending { countMap[it.id] ?: 0 }
                .thenBy { it.name }
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag("category_filter_row")
    ) {
        item {
            val isSelected = selectedCategoryId == null
            Surface(
                onClick = { onSelectCategory(null) },
                shape = RoundedCornerShape(percent = 50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.testTag("category_filter_all")
            ) {
                Text(
                    text = "Todas ($totalCount)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        items(sortedCategories, key = { it.id }) { category ->
            val isSelected = category.id == selectedCategoryId
            val catColor = CategoryIcons.getColor(category.colorHex)
            val summary = categorySummaries.firstOrNull { it.category.id == category.id }
            val hasLimit = summary?.hasLimit == true || (category.monthlyLimit != null && category.monthlyLimit > 0L)
            val fraction = summary?.budgetUsageFraction ?: 0f
            val percent = summary?.budgetUsagePercent ?: 0
            val statusColor = BudgetUtils.getStatusColor(fraction)

            Surface(
                onClick = {
                    if (isSelected) onSelectCategory(null) else onSelectCategory(category.id)
                },
                shape = RoundedCornerShape(percent = 50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.testTag("category_filter_${category.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    // Sticker badge circular
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(catColor)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIcons.getIcon(category.iconName),
                            contentDescription = null,
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                    )

                    if (hasLimit) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = statusColor.copy(alpha = if (isSelected) 0.35f else 0.2f),
                            border = BorderStroke(1.dp, statusColor)
                        ) {
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else statusColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    item: ExpenseWithCategory,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val expense = item.expense
    val category = item.category ?: Category(name = "Sin categoría", colorHex = 0xFF4DA2FFL, iconName = "receipt")
    val catColor = CategoryIcons.getColor(category.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("expense_card_${expense.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sticker de categoría: círculo saturado con borde negro de 1.5dp
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(catColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIcons.getIcon(category.iconName),
                    contentDescription = category.name,
                    tint = Color(0xFF000000),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Datos del gasto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (expense.note.isNotBlank()) {
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = DateUtils.formatDisplayDate(expense.dateMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Monto en CLP y acciones
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatClp(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_expense_${expense.id}")
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar gasto",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_expense_${expense.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar gasto",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyExpensesState(
    isFilterActive: Boolean,
    onAddExpenseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Sticker circular decorativo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9CCFF))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = Color(0xFF000000),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (isFilterActive) "Sin gastos con los filtros actuales" else "Sin gastos registrados aún",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Presiona el botón para ingresar tu primer gasto en pesos chilenos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onAddExpenseClick,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.testTag("empty_add_expense_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Registrar gasto", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
