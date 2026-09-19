package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.Expense
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.Income
import com.example.data.model.Pocket
import com.example.data.model.PocketTransaction
import com.example.data.model.PocketType
import com.example.data.model.RecurringExpense
import com.example.data.model.RecurringExpenseStatus
import com.example.data.model.RecurringExpenseWithCategory
import com.example.data.repository.ExpenseRepository
import com.example.util.CsvExporter
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.worker.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategorySummary(
    val category: Category,
    val totalAmount: Long,
    val percentage: Float,
    val count: Int
) {
    val monthlyLimit: Long? get() = category.monthlyLimit
    val hasLimit: Boolean get() = monthlyLimit != null && monthlyLimit!! > 0L
    val budgetUsageFraction: Float get() = if (hasLimit) (totalAmount.toFloat() / monthlyLimit!!.toFloat()) else 0f
    val budgetUsagePercent: Int get() = (budgetUsageFraction * 100).toInt()
    val isOverBudget: Boolean get() = hasLimit && totalAmount > monthlyLimit!!
    val overBudgetAmount: Long get() = if (isOverBudget) totalAmount - monthlyLimit!! else 0L
}

data class ExpensesUiState(
    val categories: List<Category> = emptyList(),
    val allExpenses: List<ExpenseWithCategory> = emptyList(),
    val filteredExpenses: List<ExpenseWithCategory> = emptyList(),
    val selectedYear: Int = DateUtils.getCurrentYear(),
    val selectedMonth: Int = DateUtils.getCurrentMonth(),
    val isMonthFilterActive: Boolean = true,
    val selectedCategoryId: Long? = null,
    val currentMonthTotal: Long = 0L,
    val filteredTotal: Long = 0L,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val baseSalary: Long = 0L,
    val monthIncomes: List<Income> = emptyList(),
    val remainingBalance: Long = 0L,
    val currentAccountBalance: Long = 0L,
    val debts: List<Debt> = emptyList(),
    val totalDebtAmount: Long = 0L,
    val totalDebtPaid: Long = 0L,
    val pendingDebtAmount: Long = 0L,
    val pockets: List<Pocket> = emptyList(),
    val totalPocketsAmount: Long = 0L,
    val totalSavingsAmount: Long = 0L,
    val totalAllocatedAmount: Long = 0L,
    val unallocatedBalance: Long = 0L,
    val freeAvailableBalance: Long = 0L,
    val realTotalBalance: Long = 0L
)

data class FilterParams(
    val year: Int,
    val month: Int,
    val isMonthActive: Boolean,
    val categoryId: Long?
)

data class DebtsAndPockets(
    val debts: List<Debt> = emptyList(),
    val pockets: List<Pocket> = emptyList()
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    private val _selectedYear = MutableStateFlow(DateUtils.getCurrentYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _isMonthFilterActive = MutableStateFlow(true)
    val isMonthFilterActive: StateFlow<Boolean> = _isMonthFilterActive.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    // Dialog & UI interaction states
    private val _expenseToEdit = MutableStateFlow<ExpenseWithCategory?>(null)
    val expenseToEdit: StateFlow<ExpenseWithCategory?> = _expenseToEdit.asStateFlow()

    private val _isAddExpenseDialogOpen = MutableStateFlow(false)
    val isAddExpenseDialogOpen: StateFlow<Boolean> = _isAddExpenseDialogOpen.asStateFlow()

    private val _expenseToDelete = MutableStateFlow<ExpenseWithCategory?>(null)
    val expenseToDelete: StateFlow<ExpenseWithCategory?> = _expenseToDelete.asStateFlow()

    private val _categoryToDelete = MutableStateFlow<Category?>(null)
    val categoryToDelete: StateFlow<Category?> = _categoryToDelete.asStateFlow()

    private val _isAddCategoryDialogOpen = MutableStateFlow(false)
    val isAddCategoryDialogOpen: StateFlow<Boolean> = _isAddCategoryDialogOpen.asStateFlow()

    private val _categoryToEditLimit = MutableStateFlow<Category?>(null)
    val categoryToEditLimit: StateFlow<Category?> = _categoryToEditLimit.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // Recurring expenses dialog & selection states
    private val _isAddRecurringDialogOpen = MutableStateFlow(false)
    val isAddRecurringDialogOpen: StateFlow<Boolean> = _isAddRecurringDialogOpen.asStateFlow()

    private val _recurringToEdit = MutableStateFlow<RecurringExpenseWithCategory?>(null)
    val recurringToEdit: StateFlow<RecurringExpenseWithCategory?> = _recurringToEdit.asStateFlow()

    private val _recurringToDelete = MutableStateFlow<RecurringExpenseWithCategory?>(null)
    val recurringToDelete: StateFlow<RecurringExpenseWithCategory?> = _recurringToDelete.asStateFlow()

    private val _recurringToConfirmRegister = MutableStateFlow<RecurringExpenseWithCategory?>(null)
    val recurringToConfirmRegister: StateFlow<RecurringExpenseWithCategory?> = _recurringToConfirmRegister.asStateFlow()

    // Income dialog & state
    private val _isAddIncomeDialogOpen = MutableStateFlow(false)
    val isAddIncomeDialogOpen: StateFlow<Boolean> = _isAddIncomeDialogOpen.asStateFlow()

    // Debt dialog & state
    private val _isAddDebtDialogOpen = MutableStateFlow(false)
    val isAddDebtDialogOpen: StateFlow<Boolean> = _isAddDebtDialogOpen.asStateFlow()

    private val _debtToEdit = MutableStateFlow<Debt?>(null)
    val debtToEdit: StateFlow<Debt?> = _debtToEdit.asStateFlow()

    private val _debtToDelete = MutableStateFlow<Debt?>(null)
    val debtToDelete: StateFlow<Debt?> = _debtToDelete.asStateFlow()

    private val _debtToPay = MutableStateFlow<Debt?>(null)
    val debtToPay: StateFlow<Debt?> = _debtToPay.asStateFlow()

    private val _debtToMarkPaid = MutableStateFlow<Debt?>(null)
    val debtToMarkPaid: StateFlow<Debt?> = _debtToMarkPaid.asStateFlow()

    // Pocket dialog & state
    private val _isAddPocketDialogOpen = MutableStateFlow(false)
    val isAddPocketDialogOpen: StateFlow<Boolean> = _isAddPocketDialogOpen.asStateFlow()

    private val _addPocketInitialType = MutableStateFlow(PocketType.SAVINGS)
    val addPocketInitialType: StateFlow<PocketType> = _addPocketInitialType.asStateFlow()

    private val _pocketToDeposit = MutableStateFlow<Pocket?>(null)
    val pocketToDeposit: StateFlow<Pocket?> = _pocketToDeposit.asStateFlow()

    private val _pocketToWithdraw = MutableStateFlow<Pocket?>(null)
    val pocketToWithdraw: StateFlow<Pocket?> = _pocketToWithdraw.asStateFlow()

    private val _pocketToDelete = MutableStateFlow<Pocket?>(null)
    val pocketToDelete: StateFlow<Pocket?> = _pocketToDelete.asStateFlow()

    // Reminder preferences state
    private val reminderPrefs = application.getSharedPreferences("mis_gastos_reminders", Context.MODE_PRIVATE)

    private val _remindersEnabled = MutableStateFlow(reminderPrefs.getBoolean("reminders_enabled", true))
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _reminderDaysInAdvance = MutableStateFlow(reminderPrefs.getInt("days_in_advance", 3))
    val reminderDaysInAdvance: StateFlow<Int> = _reminderDaysInAdvance.asStateFlow()

    // Accessibility DataStore repository
    private val accessibilityRepo = com.example.util.AccessibilityPreferencesRepository(application)
    val accessibilitySettings: StateFlow<com.example.util.AccessibilitySettings> = accessibilityRepo.accessibilitySettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.example.util.AccessibilitySettings()
        )

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = ExpenseRepository(
            db.categoryDao(),
            db.expenseDao(),
            db.recurringExpenseDao(),
            db.debtDao(),
            db.incomeDao(),
            db.pocketDao()
        )
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.populateInitialCategories(db.categoryDao())
        }
        if (_remindersEnabled.value) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    ReminderScheduler.scheduleDailyReminder(application)
                } catch (e: Throwable) {
                    // Safe fallback
                }
            }
        }
    }

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allRecurringExpenses: StateFlow<List<RecurringExpenseWithCategory>> = repository.allRecurringExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Compute recurring expenses status against the selected month transactions
    val recurringStatuses: StateFlow<List<RecurringExpenseStatus>> = combine(
        repository.allRecurringExpenses,
        repository.allExpensesWithCategory,
        _selectedYear,
        _selectedMonth
    ) { recurringList, expensesList, year, month ->
        val monthStart = DateUtils.getStartOfMonth(year, month)
        val monthEnd = DateUtils.getEndOfOfMonth(year, month)
        val currentDay = if (year == DateUtils.getCurrentYear() && month == DateUtils.getCurrentMonth()) {
            DateUtils.getCurrentDayOfMonth()
        } else {
            1
        }

        val monthExpenses = expensesList.filter { it.expense.dateMillis in monthStart..monthEnd }

        recurringList.map { rec ->
            // Check if this recurring expense is registered in the month (by note format "[Fijo: Nombre]" or matching category and amount)
            val isRegistered = monthExpenses.any { expItem ->
                val exp = expItem.expense
                exp.note.contains("[Fijo: ${rec.recurringExpense.name}]", ignoreCase = true) ||
                    (exp.categoryId == rec.recurringExpense.categoryId && exp.amount == rec.recurringExpense.amount)
            }

            val dueDay = rec.recurringExpense.dueDayOfMonth
            val daysUntil = dueDay - currentDay
            val isDueToday = dueDay == currentDay
            val isOverdue = dueDay < currentDay && !isRegistered

            RecurringExpenseStatus(
                item = rec,
                isRegisteredThisMonth = isRegistered,
                daysUntilDue = daysUntil,
                isDueToday = isDueToday,
                isOverdue = isOverdue
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val filterParams = combine(
        _selectedYear,
        _selectedMonth,
        _isMonthFilterActive,
        _selectedCategoryId
    ) { year, month, active, catId ->
        FilterParams(year, month, active, catId)
    }

    val allDebts: StateFlow<List<Debt>> = repository.allDebts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPockets: StateFlow<List<Pocket>> = repository.allPockets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val debtsAndPockets = combine(
        repository.allDebts,
        repository.allPockets
    ) { debts, pockets ->
        DebtsAndPockets(debts, pockets)
    }

    val uiState: StateFlow<ExpensesUiState> = combine(
        repository.allCategories,
        repository.allExpensesWithCategory,
        repository.allIncomes,
        debtsAndPockets,
        filterParams
    ) { categoriesList, expensesList, incomesList, debtsAndPocketsData, filters ->
        val debtsList = debtsAndPocketsData.debts
        val pocketsList = debtsAndPocketsData.pockets
        val currentMonthStart = DateUtils.getStartOfMonth(DateUtils.getCurrentYear(), DateUtils.getCurrentMonth())
        val currentMonthEnd = DateUtils.getEndOfOfMonth(DateUtils.getCurrentYear(), DateUtils.getCurrentMonth())
        
        val currentMonthTotal = expensesList
            .filter { it.expense.dateMillis in currentMonthStart..currentMonthEnd }
            .sumOf { it.expense.amount }

        val selectedMonthStart = DateUtils.getStartOfMonth(filters.year, filters.month)
        val selectedMonthEnd = DateUtils.getEndOfOfMonth(filters.year, filters.month)

        // Filtered transactions for the main list
        val filtered = expensesList.filter { item ->
            val dateMatches = !filters.isMonthActive || (item.expense.dateMillis in selectedMonthStart..selectedMonthEnd)
            val catMatches = filters.categoryId == null || item.expense.categoryId == filters.categoryId
            dateMatches && catMatches
        }

        val filteredTotal = filtered.sumOf { it.expense.amount }

        // Monthly category summary for the selected month (independent of category chip filter)
        val selectedMonthExpenses = expensesList.filter {
            it.expense.dateMillis in selectedMonthStart..selectedMonthEnd
        }
        val monthTotal = selectedMonthExpenses.sumOf { it.expense.amount }

        val grouped = selectedMonthExpenses.groupBy { it.expense.categoryId }
        val summariesWithExpenses = grouped.mapNotNull { (categoryId, items) ->
            val category = categoriesList.firstOrNull { it.id == categoryId }
                ?: items.firstOrNull()?.category
                ?: Category(id = categoryId, name = "Otros", colorHex = 0xFF78909CL, iconName = "receipt")
            val catTotal = items.sumOf { it.expense.amount }
            val percentage = if (monthTotal > 0L) (catTotal.toFloat() / monthTotal.toFloat()) else 0f
            CategorySummary(
                category = category,
                totalAmount = catTotal,
                percentage = percentage,
                count = items.size
            )
        }

        val categoriesWithLimitNoExpenses = categoriesList.filter { cat ->
            cat.monthlyLimit != null && cat.monthlyLimit > 0L && grouped[cat.id] == null
        }.map { cat ->
            CategorySummary(
                category = cat,
                totalAmount = 0L,
                percentage = 0f,
                count = 0
            )
        }

        val summaries = (summariesWithExpenses + categoriesWithLimitNoExpenses).sortedWith(
            compareByDescending<CategorySummary> { it.totalAmount }
                .thenByDescending { it.hasLimit }
        )

        // Income & Sueldo Base calculation
        val selectedMonthIncomes = incomesList.filter { it.dateMillis in selectedMonthStart..selectedMonthEnd }
        val explicitBase = selectedMonthIncomes.filter { it.isBaseSalary }.sumOf { it.amount }
        val baseSalary = if (explicitBase > 0L) {
            explicitBase
        } else if (selectedMonthIncomes.isNotEmpty()) {
            selectedMonthIncomes.sumOf { it.amount }
        } else {
            incomesList.filter { it.isBaseSalary }.maxByOrNull { it.dateMillis }?.amount
                ?: incomesList.maxByOrNull { it.dateMillis }?.amount ?: 0L
        }

        val totalSavingsPockets = pocketsList.filter { it.isSavings }.sumOf { it.currentAmount }
        val totalAllocatedPockets = pocketsList.filter { it.isApartado }.sumOf { it.currentAmount }
        val effectiveExpenses = if (filters.isMonthActive) monthTotal else filteredTotal
        val realTotalBalance = baseSalary - effectiveExpenses
        val freeAvailableBalance = realTotalBalance - totalSavingsPockets
        val unallocatedBalance = (freeAvailableBalance - totalAllocatedPockets).coerceAtLeast(0L)

        // Current real-world month balance (dinero disponible en cuenta hoy)
        val currentMonthIncomes = incomesList.filter { it.dateMillis in currentMonthStart..currentMonthEnd }
        val currentMonthExplicitBase = currentMonthIncomes.filter { it.isBaseSalary }.sumOf { it.amount }
        val currentMonthBaseSalary = if (currentMonthExplicitBase > 0L) {
            currentMonthExplicitBase
        } else if (currentMonthIncomes.isNotEmpty()) {
            currentMonthIncomes.sumOf { it.amount }
        } else {
            incomesList.filter { it.isBaseSalary }.maxByOrNull { it.dateMillis }?.amount
                ?: incomesList.maxByOrNull { it.dateMillis }?.amount ?: 0L
        }
        val currentAccountBalance = currentMonthBaseSalary - currentMonthTotal

        val totalDebt = debtsList.sumOf { it.totalAmount }
        val totalPaid = debtsList.sumOf { it.paidAmount }
        val pendingDebt = (totalDebt - totalPaid).coerceAtLeast(0L)

        ExpensesUiState(
            categories = categoriesList,
            allExpenses = expensesList,
            filteredExpenses = filtered,
            selectedYear = filters.year,
            selectedMonth = filters.month,
            isMonthFilterActive = filters.isMonthActive,
            selectedCategoryId = filters.categoryId,
            currentMonthTotal = currentMonthTotal,
            filteredTotal = filteredTotal,
            categorySummaries = summaries,
            baseSalary = baseSalary,
            monthIncomes = selectedMonthIncomes,
            remainingBalance = freeAvailableBalance,
            currentAccountBalance = currentAccountBalance,
            debts = debtsList,
            totalDebtAmount = totalDebt,
            totalDebtPaid = totalPaid,
            pendingDebtAmount = pendingDebt,
            pockets = pocketsList,
            totalPocketsAmount = totalSavingsPockets + totalAllocatedPockets,
            totalSavingsAmount = totalSavingsPockets,
            totalAllocatedAmount = totalAllocatedPockets,
            unallocatedBalance = unallocatedBalance,
            freeAvailableBalance = freeAvailableBalance,
            realTotalBalance = realTotalBalance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpensesUiState()
    )

    // Pocket management methods
    fun openAddPocketDialog(initialType: PocketType = PocketType.SAVINGS) {
        _addPocketInitialType.value = initialType
        _isAddPocketDialogOpen.value = true
    }

    fun closeAddPocketDialog() {
        _isAddPocketDialogOpen.value = false
    }

    fun openDepositPocketDialog(pocket: Pocket) {
        _pocketToDeposit.value = pocket
    }

    fun closeDepositPocketDialog() {
        _pocketToDeposit.value = null
    }

    fun openWithdrawPocketDialog(pocket: Pocket) {
        _pocketToWithdraw.value = pocket
    }

    fun closeWithdrawPocketDialog() {
        _pocketToWithdraw.value = null
    }

    fun promptDeletePocket(pocket: Pocket) {
        _pocketToDelete.value = pocket
    }

    fun dismissDeletePocketDialog() {
        _pocketToDelete.value = null
    }

    fun createPocket(
        name: String,
        targetAmount: Long?,
        colorHex: Long,
        iconName: String,
        type: String = PocketType.SAVINGS.name
    ) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPocket(
                Pocket(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    currentAmount = 0L,
                    colorHex = colorHex,
                    iconName = iconName,
                    type = type
                )
            )
            val typeLabel = if (type == PocketType.APARTADO.name) "Apartado" else "Ahorro"
            _userFeedbackMessage.value = "$typeLabel \"${name.trim()}\" creado"
        }
        closeAddPocketDialog()
    }

    fun depositToPocket(pocketId: Long, amount: Long) {
        if (amount <= 0L) {
            _userFeedbackMessage.value = "El monto debe ser mayor a 0"
            return
        }
        val targetPocket = _pocketToDeposit.value ?: allPockets.value.firstOrNull { it.id == pocketId }
        val isApartado = targetPocket?.isApartado == true
        val availableLimit = if (isApartado) uiState.value.unallocatedBalance else uiState.value.freeAvailableBalance
        if (amount > availableLimit) {
            val errorMsg = if (isApartado) {
                "Saldo libre sin asignar insuficiente (${CurrencyUtils.formatClp(availableLimit)})"
            } else {
                "Saldo libre insuficiente (${CurrencyUtils.formatClp(availableLimit)})"
            }
            _userFeedbackMessage.value = errorMsg
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.depositToPocket(pocketId, amount)
            if (success) {
                val actionLabel = if (isApartado) "Asignaste" else "Cargaste"
                _userFeedbackMessage.value = "$actionLabel ${CurrencyUtils.formatClp(amount)} a ${targetPocket?.name ?: "bolsillo"}"
            }
        }
        closeDepositPocketDialog()
    }

    fun withdrawFromPocket(pocketId: Long, amount: Long) {
        val pocket = _pocketToWithdraw.value ?: allPockets.value.firstOrNull { it.id == pocketId } ?: return
        if (amount <= 0L) {
            _userFeedbackMessage.value = "El monto debe ser mayor a 0"
            return
        }
        if (amount > pocket.currentAmount) {
            _userFeedbackMessage.value = "No puedes retirar más de lo asignado en el bolsillo"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.withdrawFromPocket(pocketId, amount)
            if (success) {
                val destLabel = if (pocket.isApartado) "saldo libre sin asignar" else "saldo libre"
                _userFeedbackMessage.value = "Liberaste ${CurrencyUtils.formatClp(amount)} a tu $destLabel"
            }
        }
        closeWithdrawPocketDialog()
    }

    fun quickAssignToPocket(pocket: Pocket, amount: Long) {
        depositToPocket(pocket.id, amount)
    }

    fun quickWithdrawFromPocket(pocket: Pocket, amount: Long) {
        if (amount <= 0L) return
        if (amount > pocket.currentAmount) {
            _userFeedbackMessage.value = "No puedes retirar más de lo asignado"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.withdrawFromPocket(pocket.id, amount)
            if (success) {
                val destLabel = if (pocket.isApartado) "saldo libre sin asignar" else "saldo libre"
                _userFeedbackMessage.value = "Liberaste ${CurrencyUtils.formatClp(amount)} a tu $destLabel"
            }
        }
    }

    fun confirmDeletePocket() {
        val pocket = _pocketToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePocket(pocket)
            _userFeedbackMessage.value = "Bolsillo \"${pocket.name}\" eliminado"
        }
        _pocketToDelete.value = null
    }

    fun setPreviousMonth() {
        if (_selectedMonth.value == 0) {
            _selectedMonth.value = 11
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun setNextMonth() {
        if (_selectedMonth.value == 11) {
            _selectedMonth.value = 0
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun resetToCurrentMonth() {
        _selectedYear.value = DateUtils.getCurrentYear()
        _selectedMonth.value = DateUtils.getCurrentMonth()
        _isMonthFilterActive.value = true
    }

    fun toggleMonthFilter(active: Boolean) {
        _isMonthFilterActive.value = active
    }

    fun selectCategoryFilter(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun openAddExpenseDialog() {
        _expenseToEdit.value = null
        _isAddExpenseDialogOpen.value = true
    }

    fun openEditExpenseDialog(expenseWithCategory: ExpenseWithCategory) {
        _expenseToEdit.value = expenseWithCategory
        _isAddExpenseDialogOpen.value = true
    }

    fun closeExpenseDialog() {
        _expenseToEdit.value = null
        _isAddExpenseDialogOpen.value = false
    }

    fun promptDeleteExpense(expense: ExpenseWithCategory) {
        _expenseToDelete.value = expense
    }

    fun cancelDeleteExpense() {
        _expenseToDelete.value = null
    }

    fun confirmDeleteExpense() {
        val target = _expenseToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(target.expense)
            _expenseToDelete.value = null
            _userFeedbackMessage.value = "Gasto eliminado correctamente"
        }
    }

    fun saveExpense(
        amount: Long,
        categoryId: Long,
        dateMillis: Long,
        note: String,
        id: Long = 0L
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (id > 0L) {
                repository.updateExpense(
                    Expense(
                        id = id,
                        amount = amount,
                        categoryId = categoryId,
                        dateMillis = dateMillis,
                        note = note.trim()
                    )
                )
                _userFeedbackMessage.value = "Gasto actualizado"
            } else {
                repository.insertExpense(
                    Expense(
                        amount = amount,
                        categoryId = categoryId,
                        dateMillis = dateMillis,
                        note = note.trim()
                    )
                )
                _userFeedbackMessage.value = "Gasto registrado"
            }
            closeExpenseDialog()
        }
    }

    fun openAddCategoryDialog() {
        _isAddCategoryDialogOpen.value = true
    }

    fun closeAddCategoryDialog() {
        _isAddCategoryDialogOpen.value = false
    }

    fun promptDeleteCategory(category: Category) {
        _categoryToDelete.value = category
    }

    fun cancelDeleteCategory() {
        _categoryToDelete.value = null
    }

    fun confirmDeleteCategory() {
        val target = _categoryToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            // Reasignar al primer default disponible si no es este
            val fallbackCat = categories.value.firstOrNull { it.id != target.id }
            repository.deleteCategory(target, fallbackCat?.id)
            _categoryToDelete.value = null
            _userFeedbackMessage.value = "Categoría eliminada"
        }
    }

    fun openEditCategoryLimitDialog(category: Category) {
        _categoryToEditLimit.value = category
    }

    fun closeEditCategoryLimitDialog() {
        _categoryToEditLimit.value = null
    }

    fun updateCategoryLimit(category: Category, newLimit: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = category.copy(monthlyLimit = if (newLimit != null && newLimit > 0L) newLimit else null)
            repository.updateCategory(updated)
            _categoryToEditLimit.value = null
            _userFeedbackMessage.value = if (updated.monthlyLimit != null) {
                "Límite fijado en ${CurrencyUtils.formatClp(updated.monthlyLimit!!)}"
            } else {
                "Límite de categoría eliminado"
            }
        }
    }

    fun saveCategory(name: String, colorHex: Long, iconName: String, monthlyLimit: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategory(
                Category(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName,
                    isDefault = false,
                    monthlyLimit = if (monthlyLimit != null && monthlyLimit > 0L) monthlyLimit else null
                )
            )
            closeAddCategoryDialog()
            _userFeedbackMessage.value = "Categoría agregada"
        }
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    fun exportExpensesCsv(context: Context): Intent? {
        val all = uiState.value.allExpenses
        return CsvExporter.exportAndShare(context, all)
    }

    // Recurring expenses management
    fun openAddRecurringDialog() {
        _recurringToEdit.value = null
        _isAddRecurringDialogOpen.value = true
    }

    fun openEditRecurringDialog(item: RecurringExpenseWithCategory) {
        _recurringToEdit.value = item
        _isAddRecurringDialogOpen.value = true
    }

    fun closeRecurringDialog() {
        _recurringToEdit.value = null
        _isAddRecurringDialogOpen.value = false
    }

    fun promptDeleteRecurring(item: RecurringExpenseWithCategory) {
        _recurringToDelete.value = item
    }

    fun cancelDeleteRecurring() {
        _recurringToDelete.value = null
    }

    fun confirmDeleteRecurring() {
        val target = _recurringToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecurringExpense(target.recurringExpense)
            _recurringToDelete.value = null
            _userFeedbackMessage.value = "Gasto fijo eliminado"
        }
    }

    fun saveRecurringExpense(
        name: String,
        amount: Long,
        categoryId: Long,
        dueDayOfMonth: Int,
        note: String,
        id: Long = 0L
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (id > 0L) {
                repository.updateRecurringExpense(
                    RecurringExpense(
                        id = id,
                        name = name.trim(),
                        amount = amount,
                        categoryId = categoryId,
                        dueDayOfMonth = dueDayOfMonth.coerceIn(1, 31),
                        note = note.trim(),
                        isActive = true
                    )
                )
                _userFeedbackMessage.value = "Gasto fijo actualizado"
            } else {
                repository.insertRecurringExpense(
                    RecurringExpense(
                        name = name.trim(),
                        amount = amount,
                        categoryId = categoryId,
                        dueDayOfMonth = dueDayOfMonth.coerceIn(1, 31),
                        note = note.trim(),
                        isActive = true
                    )
                )
                _userFeedbackMessage.value = "Gasto fijo creado"
            }
            closeRecurringDialog()
        }
    }

    fun promptConfirmRegisterRecurring(item: RecurringExpenseWithCategory) {
        _recurringToConfirmRegister.value = item
    }

    fun cancelRegisterRecurring() {
        _recurringToConfirmRegister.value = null
    }

    fun confirmRegisterRecurringExpense() {
        val target = _recurringToConfirmRegister.value ?: return
        val currentYear = _selectedYear.value
        val currentMonth = _selectedMonth.value
        val dateMillis = DateUtils.getDateForMonthDay(
            currentYear,
            currentMonth,
            target.recurringExpense.dueDayOfMonth
        )
        val noteContent = if (target.recurringExpense.note.isNotBlank()) {
            "[Fijo: ${target.recurringExpense.name}] ${target.recurringExpense.note}"
        } else {
            "[Fijo: ${target.recurringExpense.name}]"
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExpense(
                Expense(
                    amount = target.recurringExpense.amount,
                    categoryId = target.recurringExpense.categoryId,
                    dateMillis = dateMillis,
                    note = noteContent
                )
            )
            _recurringToConfirmRegister.value = null
            _userFeedbackMessage.value = "${target.recurringExpense.name} registrado en este mes"
        }
    }

    // --- Incomes Management ---
    fun openAddIncomeDialog() {
        _isAddIncomeDialogOpen.value = true
    }

    fun closeAddIncomeDialog() {
        _isAddIncomeDialogOpen.value = false
    }

    fun saveIncome(name: String, amount: Long, dateMillis: Long, isBaseSalary: Boolean = true, note: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertIncome(
                Income(
                    name = name.trim().ifBlank { "Sueldo base" },
                    amount = amount,
                    dateMillis = dateMillis,
                    isBaseSalary = isBaseSalary,
                    note = note.trim()
                )
            )
            _isAddIncomeDialogOpen.value = false
            _userFeedbackMessage.value = "Ingreso registrado con éxito"
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteIncome(income)
            _userFeedbackMessage.value = "Ingreso eliminado"
        }
    }

    // --- Debts Management ---
    fun openAddDebtDialog() {
        _debtToEdit.value = null
        _isAddDebtDialogOpen.value = true
    }

    fun openEditDebtDialog(debt: Debt) {
        _debtToEdit.value = debt
        _isAddDebtDialogOpen.value = true
    }

    fun closeDebtDialog() {
        _debtToEdit.value = null
        _isAddDebtDialogOpen.value = false
    }

    fun promptDeleteDebt(debt: Debt) {
        _debtToDelete.value = debt
    }

    fun cancelDeleteDebt() {
        _debtToDelete.value = null
    }

    fun confirmDeleteDebt() {
        val target = _debtToDelete.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDebt(target)
            _debtToDelete.value = null
            _userFeedbackMessage.value = "Deuda eliminada"
        }
    }

    fun promptPayDebt(debt: Debt) {
        _debtToPay.value = debt
    }

    fun cancelPayDebt() {
        _debtToPay.value = null
    }

    fun registerDebtPayment(debtId: Long, paymentAmount: Long, advanceInstallments: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            val available = uiState.value.currentAccountBalance
            if (paymentAmount > available) {
                _userFeedbackMessage.value = "Dinero insuficiente en cuenta para realizar el abono"
                return@launch
            }

            val currentDebts = allDebts.value
            val debt = currentDebts.firstOrNull { it.id == debtId }
                ?: repository.getDebtById(debtId)
                ?: return@launch
            val newPaidAmount = debt.paidAmount + paymentAmount
            val newPaidInstallments = (debt.paidInstallments + advanceInstallments).coerceAtMost(debt.totalInstallments)
            val isNowPaid = newPaidAmount >= debt.totalAmount

            repository.updateDebt(
                debt.copy(
                    paidAmount = newPaidAmount,
                    paidInstallments = newPaidInstallments,
                    isPaid = isNowPaid
                )
            )

            // Registrar el abono como egreso con la categoría "Créditos"
            val allCats = categories.value
            var creditosCat = allCats.firstOrNull {
                it.name.equals("Créditos", ignoreCase = true) ||
                it.name.equals("Creditos", ignoreCase = true)
            } ?: repository.getCategoryByName("Créditos")
              ?: repository.getCategoryByName("Creditos")

            val categoryId = if (creditosCat != null) {
                creditosCat.id
            } else {
                // Si aún no existe, crear la categoría "Créditos"
                repository.insertCategory(
                    Category(
                        name = "Créditos",
                        colorHex = 0xFF5C4ADEL,
                        iconName = "payments",
                        isDefault = true
                    )
                )
            }

            val installmentText = if (debt.totalInstallments > 1) {
                " (Cuota $newPaidInstallments/${debt.totalInstallments})"
            } else ""

            repository.insertExpense(
                Expense(
                    amount = paymentAmount,
                    categoryId = categoryId,
                    dateMillis = System.currentTimeMillis(),
                    note = "[Crédito: ${debt.title}]$installmentText"
                )
            )

            _debtToPay.value = null
            _userFeedbackMessage.value = if (isNowPaid) {
                "¡Deuda saldada! Abono de ${CurrencyUtils.formatClp(paymentAmount)} descontado del sueldo"
            } else {
                "Abono de ${CurrencyUtils.formatClp(paymentAmount)} descontado del sueldo base"
            }
        }
    }

    fun promptToggleDebtStatus(debt: Debt) {
        if (!debt.isPaid) {
            // Si está pendiente y se va a marcar como pagada, requiere confirmación por el descuento
            _debtToMarkPaid.value = debt
        } else {
            // Si ya está pagada y se vuelve a marcar como pendiente, cambiar directamente
            toggleDebtToPending(debt)
        }
    }

    fun cancelToggleDebtStatus() {
        _debtToMarkPaid.value = null
    }

    private fun toggleDebtToPending(debt: Debt) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = debt.copy(isPaid = false)
            repository.updateDebt(updated)
            _userFeedbackMessage.value = "Deuda marcada como pendiente"
        }
    }

    fun confirmMarkDebtAsPaid(debt: Debt) {
        viewModelScope.launch(Dispatchers.IO) {
            val remainingAmount = debt.remainingAmount
            val available = uiState.value.currentAccountBalance

            if (remainingAmount > 0L && remainingAmount > available) {
                _debtToMarkPaid.value = null
                _userFeedbackMessage.value = "Dinero insuficiente en cuenta (${CurrencyUtils.formatClp(available)}) para saldar esta deuda de ${CurrencyUtils.formatClp(remainingAmount)}"
                return@launch
            }

            // Actualizar la deuda a pagada en su totalidad
            val updated = debt.copy(
                isPaid = true,
                paidAmount = debt.totalAmount,
                paidInstallments = debt.totalInstallments
            )
            repository.updateDebt(updated)

            // Si quedaba un saldo pendiente por pagar, registrarlo como gasto en categoría "Créditos"
            if (remainingAmount > 0L) {
                val allCats = categories.value
                val creditosCat = allCats.firstOrNull {
                    it.name.equals("Créditos", ignoreCase = true) ||
                    it.name.equals("Creditos", ignoreCase = true)
                } ?: repository.getCategoryByName("Créditos")
                  ?: repository.getCategoryByName("Creditos")

                val categoryId = if (creditosCat != null) {
                    creditosCat.id
                } else {
                    repository.insertCategory(
                        Category(
                            name = "Créditos",
                            colorHex = 0xFF5C4ADEL,
                            iconName = "payments",
                            isDefault = true
                        )
                    )
                }

                repository.insertExpense(
                    Expense(
                        amount = remainingAmount,
                        categoryId = categoryId,
                        dateMillis = System.currentTimeMillis(),
                        note = "[Crédito saldo total: ${debt.title}]"
                    )
                )

                _userFeedbackMessage.value = "¡Deuda saldada! Se descontaron ${CurrencyUtils.formatClp(remainingAmount)} como gasto en Créditos"
            } else {
                _userFeedbackMessage.value = "Deuda marcada como pagada"
            }

            _debtToMarkPaid.value = null
        }
    }

    fun toggleDebtPaidStatus(debt: Debt) {
        promptToggleDebtStatus(debt)
    }

    fun saveDebt(
        title: String,
        totalAmount: Long,
        paidAmount: Long,
        totalInstallments: Int,
        paidInstallments: Int,
        dueDateMillis: Long?,
        note: String,
        id: Long = 0L
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val isPaid = paidAmount >= totalAmount
            if (id > 0L) {
                repository.updateDebt(
                    Debt(
                        id = id,
                        title = title.trim(),
                        totalAmount = totalAmount,
                        paidAmount = paidAmount,
                        totalInstallments = totalInstallments.coerceAtLeast(1),
                        paidInstallments = paidInstallments.coerceIn(0, totalInstallments.coerceAtLeast(1)),
                        dueDateMillis = dueDateMillis,
                        isPaid = isPaid,
                        note = note.trim()
                    )
                )
                _userFeedbackMessage.value = "Deuda actualizada"
            } else {
                repository.insertDebt(
                    Debt(
                        title = title.trim(),
                        totalAmount = totalAmount,
                        paidAmount = paidAmount,
                        totalInstallments = totalInstallments.coerceAtLeast(1),
                        paidInstallments = paidInstallments.coerceIn(0, totalInstallments.coerceAtLeast(1)),
                        dueDateMillis = dueDateMillis,
                        isPaid = isPaid,
                        note = note.trim()
                    )
                )
                _userFeedbackMessage.value = "Deuda registrada"
            }
            closeDebtDialog()
        }
    }

    // ==========================================
    // NOTIFICACIONES Y RECORDATORIOS
    // ==========================================

    fun setRemindersEnabled(enabled: Boolean) {
        _remindersEnabled.value = enabled
        reminderPrefs.edit().putBoolean("reminders_enabled", enabled).apply()
        if (enabled) {
            ReminderScheduler.scheduleDailyReminder(getApplication())
            _userFeedbackMessage.value = "Recordatorios automáticos activados"
        } else {
            ReminderScheduler.cancelReminders(getApplication())
            _userFeedbackMessage.value = "Recordatorios automáticos desactivados"
        }
    }

    fun setReminderDaysInAdvance(days: Int) {
        _reminderDaysInAdvance.value = days
        reminderPrefs.edit().putInt("days_in_advance", days).apply()
        _userFeedbackMessage.value = "Aviso configurado a $days días antes del vencimiento"
    }

    fun triggerReminderCheckNow() {
        ReminderScheduler.triggerImmediateCheck(getApplication())
        _userFeedbackMessage.value = "Verificación de vencimientos ejecutada"
    }

    fun sendTestReminderNotification() {
        ReminderScheduler.sendTestNotification(getApplication())
        _userFeedbackMessage.value = "Notificación de prueba enviada"
    }

    // ==========================================
    // ACCESIBILIDAD (DATASTORE PREFERENCES)
    // ==========================================

    fun setFontSizeScale(scale: com.example.util.FontSizeScale) {
        viewModelScope.launch(Dispatchers.IO) {
            accessibilityRepo.setFontSizeScale(scale)
            _userFeedbackMessage.value = "Tamaño de fuente: ${scale.label}"
        }
    }

    fun setHighContrast(isHigh: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            accessibilityRepo.setHighContrast(isHigh)
            _userFeedbackMessage.value = if (isHigh) "Modo de alto contraste activado" else "Contraste normal activado"
        }
    }

    // ==========================================
    // PERSONALIZACIÓN (TEMA Y ACENTO)
    // ==========================================

    fun setAppTheme(theme: com.example.util.AppTheme) {
        viewModelScope.launch(Dispatchers.IO) {
            accessibilityRepo.setAppTheme(theme)
            _userFeedbackMessage.value = "Tema actualizado: ${theme.label}"
        }
    }

    fun setAccentColor(colorLong: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            accessibilityRepo.setAccentColor(colorLong)
            val hexString = String.format("#%06X", 0xFFFFFF and colorLong.toInt())
            _userFeedbackMessage.value = "Color de acento actualizado ($hexString)"
        }
    }
}
