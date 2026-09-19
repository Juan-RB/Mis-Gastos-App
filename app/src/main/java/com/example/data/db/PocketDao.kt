package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Pocket
import com.example.data.model.PocketTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PocketDao {

    @Query("SELECT * FROM pockets ORDER BY id ASC")
    fun getAllPockets(): Flow<List<Pocket>>

    @Query("SELECT * FROM pockets WHERE id = :id")
    suspend fun getPocketById(id: Long): Pocket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPocket(pocket: Pocket): Long

    @Update
    suspend fun updatePocket(pocket: Pocket)

    @Delete
    suspend fun deletePocket(pocket: Pocket)

    @Query("DELETE FROM pockets WHERE id = :id")
    suspend fun deletePocketById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PocketTransaction): Long

    @Query("SELECT * FROM pocket_transactions WHERE pocketId = :pocketId ORDER BY dateMillis DESC")
    fun getTransactionsForPocket(pocketId: Long): Flow<List<PocketTransaction>>

    @Query("SELECT * FROM pocket_transactions ORDER BY dateMillis DESC")
    fun getAllPocketTransactions(): Flow<List<PocketTransaction>>

    @Transaction
    suspend fun depositToPocket(pocketId: Long, amount: Long, dateMillis: Long = System.currentTimeMillis()): Boolean {
        if (amount <= 0L) return false
        val pocket = getPocketById(pocketId) ?: return false
        val updatedPocket = pocket.copy(currentAmount = pocket.currentAmount + amount)
        updatePocket(updatedPocket)
        insertTransaction(
            PocketTransaction(
                pocketId = pocketId,
                amount = amount,
                dateMillis = dateMillis,
                type = "DEPOSITAR"
            )
        )
        return true
    }

    @Transaction
    suspend fun withdrawFromPocket(pocketId: Long, amount: Long, dateMillis: Long = System.currentTimeMillis()): Boolean {
        if (amount <= 0L) return false
        val pocket = getPocketById(pocketId) ?: return false
        if (pocket.currentAmount < amount) return false
        val updatedPocket = pocket.copy(currentAmount = pocket.currentAmount - amount)
        updatePocket(updatedPocket)
        insertTransaction(
            PocketTransaction(
                pocketId = pocketId,
                amount = amount,
                dateMillis = dateMillis,
                type = "RETIRAR"
            )
        )
        return true
    }
}
