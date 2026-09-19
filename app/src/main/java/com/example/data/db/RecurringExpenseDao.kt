package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.RecurringExpense
import com.example.data.model.RecurringExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 ORDER BY dueDayOfMonth ASC")
    fun getActiveRecurringExpenses(): Flow<List<RecurringExpenseWithCategory>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses ORDER BY dueDayOfMonth ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseWithCategory>>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id LIMIT 1")
    suspend fun getRecurringExpenseById(id: Long): RecurringExpense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(expense: RecurringExpense): Long

    @Update
    suspend fun updateRecurringExpense(expense: RecurringExpense)

    @Delete
    suspend fun deleteRecurringExpense(expense: RecurringExpense)

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun deleteRecurringExpenseById(id: Long)
}
