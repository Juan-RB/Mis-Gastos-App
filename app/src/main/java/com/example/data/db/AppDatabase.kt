package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.Pocket
import com.example.data.model.PocketTransaction
import com.example.data.model.RecurringExpense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Category::class,
        Expense::class,
        RecurringExpense::class,
        Debt::class,
        Income::class,
        Pocket::class,
        PocketTransaction::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun debtDao(): DebtDao
    abstract fun incomeDao(): IncomeDao
    abstract fun pocketDao(): PocketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val DEFAULT_CATEGORIES = listOf(
            Category(id = 1, name = "Comida", colorHex = 0xFFFB4903L, iconName = "restaurant", isDefault = true),
            Category(id = 2, name = "Transporte", colorHex = 0xFF4DA2FFL, iconName = "directions_car", isDefault = true),
            Category(id = 3, name = "Hogar", colorHex = 0xFF5C4ADEL, iconName = "home", isDefault = true),
            Category(id = 4, name = "Salud", colorHex = 0xFF55DB9CL, iconName = "medical_services", isDefault = true),
            Category(id = 5, name = "Entretenimiento", colorHex = 0xFFFFD731L, iconName = "sports_esports", isDefault = true),
            Category(id = 6, name = "Sueldo", colorHex = 0xFFE9CCFFL, iconName = "work", isDefault = true),
            Category(id = 7, name = "Otros", colorHex = 0xFF5C4ADEL, iconName = "receipt", isDefault = true),
            Category(id = 8, name = "Créditos", colorHex = 0xFF5C4ADEL, iconName = "payments", isDefault = true)
        )

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_gastos_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_gastos_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialCategories(database.categoryDao())
                    }
                }
            }
        }

        suspend fun populateInitialCategories(categoryDao: CategoryDao) {
            if (categoryDao.getCategoryCount() == 0) {
                categoryDao.insertCategories(DEFAULT_CATEGORIES)
            } else {
                // Asegurarse de que la categoría Créditos exista en bases de datos ya creadas
                val existingCreditos = categoryDao.getCategoryByName("Créditos")
                    ?: categoryDao.getCategoryByName("Creditos")
                if (existingCreditos == null) {
                    categoryDao.insertCategory(
                        Category(
                            name = "Créditos",
                            colorHex = 0xFF5C4ADEL,
                            iconName = "payments",
                            isDefault = true
                        )
                    )
                }
            }
        }
    }
}
