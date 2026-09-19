package com.example.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.DateUtils
import com.example.util.FontSizeScale
import com.example.util.NotificationHelper
import com.example.worker.ReminderScheduler

enum class MainNavigationTab(val title: String) {
    EXPENSES("Gastos"),
    POCKETS("Bolsillos"),
    INSTALLMENTS("Cuotas"),
    SUMMARY("Resumen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val expenseToEdit by viewModel.expenseToEdit.collectAsStateWithLifecycle()
    val isAddExpenseDialogOpen by viewModel.isAddExpenseDialogOpen.collectAsStateWithLifecycle()
    val expenseToDelete by viewModel.expenseToDelete.collectAsStateWithLifecycle()
    val categoryToDelete by viewModel.categoryToDelete.collectAsStateWithLifecycle()
    val isAddCategoryDialogOpen by viewModel.isAddCategoryDialogOpen.collectAsStateWithLifecycle()
    val categoryToEditLimit by viewModel.categoryToEditLimit.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()

    // Recurring expenses states
    val recurringStatuses by viewModel.recurringStatuses.collectAsStateWithLifecycle()
    val isAddRecurringDialogOpen by viewModel.isAddRecurringDialogOpen.collectAsStateWithLifecycle()
    val recurringToEdit by viewModel.recurringToEdit.collectAsStateWithLifecycle()
    val recurringToDelete by viewModel.recurringToDelete.collectAsStateWithLifecycle()
    val recurringToConfirmRegister by viewModel.recurringToConfirmRegister.collectAsStateWithLifecycle()

    // Income & Debt states
    val isAddIncomeDialogOpen by viewModel.isAddIncomeDialogOpen.collectAsStateWithLifecycle()
    val isAddDebtDialogOpen by viewModel.isAddDebtDialogOpen.collectAsStateWithLifecycle()
    val debtToEdit by viewModel.debtToEdit.collectAsStateWithLifecycle()
    val debtToDelete by viewModel.debtToDelete.collectAsStateWithLifecycle()
    val debtToPay by viewModel.debtToPay.collectAsStateWithLifecycle()
    val debtToMarkPaid by viewModel.debtToMarkPaid.collectAsStateWithLifecycle()

    // Pocket states
    val isAddPocketDialogOpen by viewModel.isAddPocketDialogOpen.collectAsStateWithLifecycle()
    val addPocketInitialType by viewModel.addPocketInitialType.collectAsStateWithLifecycle()
    val pocketToDeposit by viewModel.pocketToDeposit.collectAsStateWithLifecycle()
    val pocketToWithdraw by viewModel.pocketToWithdraw.collectAsStateWithLifecycle()
    val pocketToDelete by viewModel.pocketToDelete.collectAsStateWithLifecycle()

    // Reminder states & permissions
    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val reminderDaysInAdvance by viewModel.reminderDaysInAdvance.collectAsStateWithLifecycle()
    val accessibilitySettings by viewModel.accessibilitySettings.collectAsStateWithLifecycle()
    var hasNotificationPermission by remember {
        mutableStateOf(NotificationHelper.hasNotificationPermission(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
            ReminderScheduler.scheduleDailyReminder(context)
        } else {
            Toast.makeText(context, "Permiso no otorgado. No se podrán mostrar alertas.", Toast.LENGTH_SHORT).show()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(MainNavigationTab.EXPENSES.ordinal) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    val outlineColor = MaterialTheme.colorScheme.outline
    val accentColor = Color(accessibilitySettings.accentColor)
    val isHighContrast = accessibilitySettings.isHighContrast
    val activeIndicatorColor = if (isHighContrast) MaterialTheme.colorScheme.primary else accentColor
    // Contrast check for text/icon on accent: calculate luminance
    val accentLuminance = (0.299 * accentColor.red.toDouble() + 0.587 * accentColor.green.toDouble() + 0.114 * accentColor.blue.toDouble())
    val onAccentColor = if (accentLuminance > 0.55) Color.Black else Color.White

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = onAccentColor,
        selectedTextColor = MaterialTheme.colorScheme.onSurface,
        indicatorColor = activeIndicatorColor,
        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSettingsOpen) "Ajustes" else "Mis Gastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    if (isSettingsOpen) {
                        IconButton(
                            onClick = { isSettingsOpen = false },
                            modifier = Modifier.testTag("back_from_settings_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (!isSettingsOpen) {
                        IconButton(
                            onClick = { isSettingsOpen = true },
                            modifier = Modifier.testTag("settings_top_bar_button")
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Ajustes",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .drawBehind {
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .testTag("app_top_bar")
            )
        },
        bottomBar = {
            if (!isSettingsOpen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .drawBehind {
                            drawLine(
                                color = outlineColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .testTag("bottom_nav_bar")
                ) {
                    val navLabelStyle = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Gastos (Variables / Fijos)
                    NavigationBarItem(
                        selected = selectedTab == MainNavigationTab.EXPENSES.ordinal,
                        onClick = {
                            selectedTab = MainNavigationTab.EXPENSES.ordinal
                            isSettingsOpen = false
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Gastos") },
                        label = {
                            Text(
                                text = "Gastos",
                                style = navLabelStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_expenses")
                    )

                    // 2. Bolsillos (nuevo módulo)
                    NavigationBarItem(
                        selected = selectedTab == MainNavigationTab.POCKETS.ordinal,
                        onClick = {
                            selectedTab = MainNavigationTab.POCKETS.ordinal
                            isSettingsOpen = false
                        },
                        icon = { Icon(Icons.Default.Savings, contentDescription = "Bolsillos") },
                        label = {
                            Text(
                                text = "Bolsillos",
                                style = navLabelStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_pockets")
                    )

                    // 3. Cuotas (antes Deudas)
                    NavigationBarItem(
                        selected = selectedTab == MainNavigationTab.INSTALLMENTS.ordinal,
                        onClick = {
                            selectedTab = MainNavigationTab.INSTALLMENTS.ordinal
                            isSettingsOpen = false
                        },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = "Cuotas") },
                        label = {
                            Text(
                                text = "Cuotas",
                                style = navLabelStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_installments")
                    )

                    // 4. Resumen
                    NavigationBarItem(
                        selected = selectedTab == MainNavigationTab.SUMMARY.ordinal,
                        onClick = {
                            selectedTab = MainNavigationTab.SUMMARY.ordinal
                            isSettingsOpen = false
                        },
                        icon = { Icon(Icons.Default.PieChart, contentDescription = "Resumen") },
                        label = {
                            Text(
                                text = "Resumen",
                                style = navLabelStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_summary")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSettingsOpen) {
                SettingsScreen(
                    categories = uiState.categories,
                    totalExpensesCount = uiState.allExpenses.size,
                    onAddCategoryClick = { viewModel.openAddCategoryDialog() },
                    onDeleteCategoryClick = { viewModel.promptDeleteCategory(it) },
                    onExportCsvClick = { ctx ->
                        val intent = viewModel.exportExpensesCsv(ctx)
                        if (intent != null) {
                            val chooser = Intent.createChooser(intent, "Exportar respaldo Mis Gastos (CSV)")
                            ctx.startActivity(chooser)
                        } else {
                            Toast.makeText(ctx, "No hay gastos para exportar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    remindersEnabled = remindersEnabled,
                    daysInAdvance = reminderDaysInAdvance,
                    hasNotificationPermission = hasNotificationPermission,
                    onToggleReminders = { enabled ->
                        if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.setRemindersEnabled(enabled)
                    },
                    onSelectDaysInAdvance = { days -> viewModel.setReminderDaysInAdvance(days) },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onSendTestNotification = {
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.sendTestReminderNotification()
                        }
                    },
                    onTriggerCheckNow = { viewModel.triggerReminderCheckNow() },
                    accessibilitySettings = accessibilitySettings,
                    onSelectFontSizeScale = { scale -> viewModel.setFontSizeScale(scale) },
                    onToggleHighContrast = { isHigh -> viewModel.setHighContrast(isHigh) },
                    onSelectAppTheme = { theme -> viewModel.setAppTheme(theme) },
                    onSelectAccentColor = { color -> viewModel.setAccentColor(color) },
                    categorySummaries = uiState.categorySummaries,
                    onEditCategoryLimit = { category -> viewModel.openEditCategoryLimitDialog(category) }
                )
            } else {
                when (selectedTab) {
                    MainNavigationTab.EXPENSES.ordinal -> {
                        ExpensesUnifiedScreen(
                            uiState = uiState,
                            recurringStatuses = recurringStatuses,
                            onPreviousMonth = { viewModel.setPreviousMonth() },
                            onNextMonth = { viewModel.setNextMonth() },
                            onResetMonth = { viewModel.resetToCurrentMonth() },
                            onToggleMonthFilter = { viewModel.toggleMonthFilter(it) },
                            onSelectCategoryFilter = { viewModel.selectCategoryFilter(it) },
                            onAddExpenseClick = { viewModel.openAddExpenseDialog() },
                            onAddIncomeClick = { viewModel.openAddIncomeDialog() },
                            onEditExpenseClick = { viewModel.openEditExpenseDialog(it) },
                            onDeleteExpenseClick = { viewModel.promptDeleteExpense(it) },
                            onAddRecurringClick = { viewModel.openAddRecurringDialog() },
                            onEditRecurringClick = { viewModel.openEditRecurringDialog(it) },
                            onDeleteRecurringClick = { viewModel.promptDeleteRecurring(it) },
                            onRegisterRecurringClick = { viewModel.promptConfirmRegisterRecurring(it) }
                        )
                    }

                    MainNavigationTab.POCKETS.ordinal -> {
                        PocketsScreen(
                            pockets = uiState.pockets,
                            totalPocketsAmount = uiState.totalPocketsAmount,
                            totalSavingsAmount = uiState.totalSavingsAmount,
                            totalAllocatedAmount = uiState.totalAllocatedAmount,
                            unallocatedBalance = uiState.unallocatedBalance,
                            freeAvailableBalance = uiState.freeAvailableBalance,
                            onAddPocketClick = { pocketType -> viewModel.openAddPocketDialog(pocketType) },
                            onDepositClick = { pocket -> viewModel.openDepositPocketDialog(pocket) },
                            onWithdrawClick = { pocket -> viewModel.openWithdrawPocketDialog(pocket) },
                            onQuickAssignClick = { pocket, amount -> viewModel.depositToPocket(pocket.id, amount) },
                            onQuickWithdrawClick = { pocket, amount -> viewModel.withdrawFromPocket(pocket.id, amount) },
                            onDeleteClick = { pocket -> viewModel.promptDeletePocket(pocket) }
                        )
                    }

                    MainNavigationTab.INSTALLMENTS.ordinal -> {
                        DebtsScreen(
                            debts = uiState.debts,
                            totalDebtAmount = uiState.totalDebtAmount,
                            totalDebtPaid = uiState.totalDebtPaid,
                            pendingDebtAmount = uiState.pendingDebtAmount,
                            onAddDebtClick = { viewModel.openAddDebtDialog() },
                            onEditDebtClick = { viewModel.openEditDebtDialog(it) },
                            onDeleteDebtClick = { viewModel.promptDeleteDebt(it) },
                            onPayDebtClick = { viewModel.promptPayDebt(it) },
                            onToggleStatusClick = { viewModel.toggleDebtPaidStatus(it) }
                        )
                    }

                    MainNavigationTab.SUMMARY.ordinal -> {
                        SummaryScreen(
                            uiState = uiState,
                            onPreviousMonth = { viewModel.setPreviousMonth() },
                            onNextMonth = { viewModel.setNextMonth() },
                            onResetMonth = { viewModel.resetToCurrentMonth() },
                            onEditCategoryLimit = { category -> viewModel.openEditCategoryLimitDialog(category) }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para Agregar / Editar Gasto
    if (isAddExpenseDialogOpen) {
        AddEditExpenseDialog(
            expenseToEdit = expenseToEdit,
            categories = uiState.categories,
            onDismiss = { viewModel.closeExpenseDialog() },
            onSave = { amount, categoryId, dateMillis, note, id ->
                viewModel.saveExpense(amount, categoryId, dateMillis, note, id)
            },
            onAddNewCategoryClick = { viewModel.openAddCategoryDialog() }
        )
    }

    // Diálogo de Confirmación para Eliminar Gasto
    expenseToDelete?.let { targetExpense ->
        DeleteExpenseConfirmationDialog(
            expenseWithCategory = targetExpense,
            onDismiss = { viewModel.cancelDeleteExpense() },
            onConfirm = { viewModel.confirmDeleteExpense() }
        )
    }

    // Diálogo para Agregar / Editar Gasto Fijo
    if (isAddRecurringDialogOpen) {
        AddEditRecurringExpenseDialog(
            itemToEdit = recurringToEdit,
            categories = uiState.categories,
            onDismiss = { viewModel.closeRecurringDialog() },
            onSave = { name, amount, categoryId, dueDayOfMonth, note, id ->
                viewModel.saveRecurringExpense(name, amount, categoryId, dueDayOfMonth, note, id)
            },
            onAddNewCategoryClick = { viewModel.openAddCategoryDialog() }
        )
    }

    // Diálogo de Confirmación para Registrar Gasto Fijo en el Mes Actual
    recurringToConfirmRegister?.let { targetRecurring ->
        ConfirmRegisterRecurringDialog(
            item = targetRecurring,
            monthName = DateUtils.formatMonthYear(uiState.selectedYear, uiState.selectedMonth),
            onDismiss = { viewModel.cancelRegisterRecurring() },
            onConfirm = { viewModel.confirmRegisterRecurringExpense() }
        )
    }

    // Diálogo de Confirmación para Eliminar Gasto Fijo
    recurringToDelete?.let { targetRecurring ->
        DeleteRecurringConfirmationDialog(
            item = targetRecurring,
            onDismiss = { viewModel.cancelDeleteRecurring() },
            onConfirm = { viewModel.confirmDeleteRecurring() }
        )
    }

    // Diálogo para Agregar Categoría
    if (isAddCategoryDialogOpen) {
        AddCategoryDialog(
            onDismiss = { viewModel.closeAddCategoryDialog() },
            onSave = { name, colorHex, iconName, limit ->
                viewModel.saveCategory(name, colorHex, iconName, limit)
            }
        )
    }

    // Diálogo para Configurar / Editar Límite de Categoría
    categoryToEditLimit?.let { targetCategory ->
        EditCategoryLimitDialog(
            category = targetCategory,
            onDismiss = { viewModel.closeEditCategoryLimitDialog() },
            onSaveLimit = { newLimit ->
                viewModel.updateCategoryLimit(targetCategory, newLimit)
            }
        )
    }

    // Diálogo de Confirmación para Eliminar Categoría
    categoryToDelete?.let { targetCategory ->
        DeleteCategoryConfirmationDialog(
            category = targetCategory,
            onDismiss = { viewModel.cancelDeleteCategory() },
            onConfirm = { viewModel.confirmDeleteCategory() }
        )
    }

    // Diálogo para Agregar Ingreso
    if (isAddIncomeDialogOpen) {
        AddIncomeDialog(
            onDismiss = { viewModel.closeAddIncomeDialog() },
            onSave = { name, amount, dateMillis, isBaseSalary, note ->
                viewModel.saveIncome(name, amount, dateMillis, isBaseSalary, note)
            }
        )
    }

    // Diálogo para Crear / Editar Deuda
    if (isAddDebtDialogOpen) {
        AddEditDebtDialog(
            debtToEdit = debtToEdit,
            onDismiss = { viewModel.closeDebtDialog() },
            onSave = { title, totalAmount, paidAmount, totalInstallments, paidInstallments, dueDateMillis, note, id ->
                viewModel.saveDebt(title, totalAmount, paidAmount, totalInstallments, paidInstallments, dueDateMillis, note, id)
            }
        )
    }

    // Diálogo para Abonar a Deuda
    debtToPay?.let { targetDebt ->
        PayDebtDialog(
            debt = targetDebt,
            availableBalance = uiState.currentAccountBalance,
            onDismiss = { viewModel.cancelPayDebt() },
            onConfirm = { paymentAmount, advanceInstallments ->
                viewModel.registerDebtPayment(targetDebt.id, paymentAmount, advanceInstallments)
            }
        )
    }

    // Diálogo de Confirmación para Eliminar Deuda
    debtToDelete?.let { targetDebt ->
        DeleteDebtConfirmationDialog(
            debt = targetDebt,
            onDismiss = { viewModel.cancelDeleteDebt() },
            onConfirm = { viewModel.confirmDeleteDebt() }
        )
    }

    // Diálogo de Confirmación para Marcar Deuda como Totalmente Pagada
    debtToMarkPaid?.let { targetDebt ->
        MarkDebtAsPaidConfirmationDialog(
            debt = targetDebt,
            availableBalance = uiState.currentAccountBalance,
            onDismiss = { viewModel.cancelToggleDebtStatus() },
            onConfirm = { viewModel.confirmMarkDebtAsPaid(targetDebt) }
        )
    }

    // Diálogos de Bolsillos
    if (isAddPocketDialogOpen) {
        AddPocketDialog(
            initialType = addPocketInitialType,
            onDismiss = { viewModel.closeAddPocketDialog() },
            onConfirm = { name, targetAmount, colorHex, iconName, type ->
                viewModel.createPocket(name, targetAmount, colorHex, iconName, type)
            }
        )
    }

    pocketToDeposit?.let { targetPocket ->
        DepositPocketDialog(
            pocket = targetPocket,
            freeAvailableBalance = if (targetPocket.isApartado) uiState.unallocatedBalance else uiState.freeAvailableBalance,
            onDismiss = { viewModel.closeDepositPocketDialog() },
            onConfirm = { amount ->
                viewModel.depositToPocket(targetPocket.id, amount)
            }
        )
    }

    pocketToWithdraw?.let { targetPocket ->
        WithdrawPocketDialog(
            pocket = targetPocket,
            onDismiss = { viewModel.closeWithdrawPocketDialog() },
            onConfirm = { amount ->
                viewModel.withdrawFromPocket(targetPocket.id, amount)
            }
        )
    }

    pocketToDelete?.let { targetPocket ->
        DeletePocketConfirmDialog(
            pocket = targetPocket,
            onDismiss = { viewModel.dismissDeletePocketDialog() },
            onConfirm = { viewModel.confirmDeletePocket() }
        )
    }
}
