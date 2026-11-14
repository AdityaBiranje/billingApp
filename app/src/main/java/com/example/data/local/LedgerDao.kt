package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledgers ORDER BY isActive DESC, updatedAt DESC")
    fun observeAllLedgers(): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledgers WHERE isActive = 1 LIMIT 1")
    fun observeActiveLedger(): Flow<LedgerEntity?>

    @Query("SELECT * FROM ledgers WHERE id = :id LIMIT 1")
    suspend fun getLedgerById(id: Long): LedgerEntity?

    @Query("SELECT * FROM ledgers ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getMostRecentLedger(): LedgerEntity?

    @Query("SELECT COUNT(*) FROM ledgers")
    suspend fun getLedgerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedger(ledger: LedgerEntity): Long

    @Query("UPDATE ledgers SET isActive = 0")
    suspend fun clearActiveFlags()

    @Query("UPDATE ledgers SET isActive = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markActive(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Transaction
    suspend fun insertAndActivateLedger(ledger: LedgerEntity): Long {
        clearActiveFlags()
        return insertLedger(ledger.copy(isActive = true))
    }

    @Transaction
    suspend fun switchActiveLedger(id: Long) {
        clearActiveFlags()
        markActive(id)
    }

    @Query("DELETE FROM ledgers WHERE id = :id")
    suspend fun deleteLedgerById(id: Long)
}
