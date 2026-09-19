package com.example.data.repository

import com.example.data.db.CategoryDao
import com.example.data.db.DebtDao
import com.example.data.db.ExpenseDao
import com.example.data.db.IncomeDao
import com.example.data.db.PocketDao
import com.example.data.db.RecurringExpenseDao
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.Expense
import com.example.data.model.ExpenseWithCategory
import com.example.data.model.Income
import com.example.data.model.Pocket
import com.example.data.model.PocketTransaction
import com.example.data.model.RecurringExpense
import com.example.data.model.RecurringExpenseWithCategory
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val recurringExpenseDao: RecurringExpenseDao,
    private val debtDao: DebtDao,
    private val incomeDao: IncomeDao,
    private val pocketDao: PocketDao
) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allExpensesWithCategory: Flow<List<ExpenseWithCategory>> = expenseDao.getAllExpensesWithCategory()
    val allRecurringExpenses: Flow<List<RecurringExpenseWithCategory>> = recurringExpenseDao.getAllRecurringExpenses()
    val activeRecurringExpenses: Flow<List<RecurringExpenseWithCategory>> = recurringExpenseDao.getActiveRecurringExpenses()
    val allDebts: Flow<List<Debt>> = debtDao.getAllDebts()
    val allIncomes: Flow<List<Income>> = incomeDao.getAllIncomes()
    val allPockets: Flow<List<Pocket>> = pocketDao.getAllPockets()

    fun getExpensesByDateRange(startMillis: Long, endMillis: Long): Flow<List<ExpenseWithCategory>> {
        return expenseDao.getExpensesByDateRange(startMillis, endMillis)
    }

    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)
    }

    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category, reassignToCategoryId: Long? = null) {
        if (reassignToCategoryId != null) {
            expenseDao.reassignCategory(category.id, reassignToCategoryId)
        }
        categoryDao.deleteCategory(category)
    }

    suspend fun getExpenseCountForCategory(categoryId: Long): Int {
        return expenseDao.getExpenseCountForCategory(categoryId)
    }

    suspend fun getAllExpensesSnapshot(): List<ExpenseWithCategory> {
        return expenseDao.getAllExpensesSnapshot()
    }

    // Recurring expenses
    suspend fun insertRecurringExpense(expense: RecurringExpense): Long {
        return recurringExpenseDao.insertRecurringExpense(expense)
    }

    suspend fun updateRecurringExpense(expense: RecurringExpense) {
        recurringExpenseDao.updateRecurringExpense(expense)
    }

    suspend fun deleteRecurringExpense(expense: RecurringExpense) {
        recurringExpenseDao.deleteRecurringExpense(expense)
    }

    suspend fun deleteRecurringExpenseById(id: Long) {
        recurringExpenseDao.deleteRecurringExpenseById(id)
    }

    // Debts
    suspend fun getDebtById(id: Long): Debt? {
        return debtDao.getDebtById(id)
    }

    suspend fun insertDebt(debt: Debt): Long {
        return debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: Debt) {
        debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: Debt) {
        debtDao.deleteDebt(debt)
    }

    suspend fun deleteDebtById(id: Long) {
        debtDao.deleteDebtById(id)
    }

    // Incomes
    fun getIncomesByDateRange(startMillis: Long, endMillis: Long): Flow<List<Income>> {
        return incomeDao.getIncomesByDateRange(startMillis, endMillis)
    }

    suspend fun insertIncome(income: Income): Long {
        return incomeDao.insertIncome(income)
    }

    suspend fun updateIncome(income: Income) {
        incomeDao.updateIncome(income)
    }

    suspend fun deleteIncome(income: Income) {
        incomeDao.deleteIncome(income)
    }

    suspend fun deleteIncomeById(id: Long) {
        incomeDao.deleteIncomeById(id)
    }

    // Pockets
    suspend fun insertPocket(pocket: Pocket): Long {
        return pocketDao.insertPocket(pocket)
    }

    suspend fun updatePocket(pocket: Pocket) {
        pocketDao.updatePocket(pocket)
    }

    suspend fun deletePocket(pocket: Pocket) {
        pocketDao.deletePocket(pocket)
    }

    suspend fun deletePocketById(id: Long) {
        pocketDao.deletePocketById(id)
    }

    suspend fun getPocketById(id: Long): Pocket? {
        return pocketDao.getPocketById(id)
    }

    suspend fun depositToPocket(pocketId: Long, amount: Long): Boolean {
        return pocketDao.depositToPocket(pocketId, amount)
    }

    suspend fun withdrawFromPocket(pocketId: Long, amount: Long): Boolean {
        return pocketDao.withdrawFromPocket(pocketId, amount)
    }

    fun getTransactionsForPocket(pocketId: Long): Flow<List<PocketTransaction>> {
        return pocketDao.getTransactionsForPocket(pocketId)
    }
}
