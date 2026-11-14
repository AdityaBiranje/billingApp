package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledgers")
data class LedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val farmerName: String,
    val monthYear: String,
    val transactionsJson: String,
    val rawBulkText: String = "",
    val isActive: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)
