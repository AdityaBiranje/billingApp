package com.example.data.local

import com.example.domain.data.DefaultLedgerData
import com.example.domain.model.LedgerJsonPayload
import com.example.domain.model.LedgerTransaction
import com.example.domain.model.SavedLedgerSummary
import com.example.domain.model.StoredFarmerLedger
import com.example.domain.util.DevanagariFormatter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ActiveLedgerRecord(
    val id: Long,
    val farmerName: String,
    val monthYear: String,
    val transactions: List<LedgerTransaction>,
    val rawBulkText: String,
    val updatedAt: Long,
)

class LedgerRepository(private val ledgerDao: LedgerDao) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, LedgerTransaction::class.java)
    private val txListAdapter = moshi.adapter<List<LedgerTransaction>>(listType)
    private val payloadAdapter = moshi.adapter(LedgerJsonPayload::class.java).indent("  ")

    val activeLedger: Flow<ActiveLedgerRecord?> = ledgerDao.observeActiveLedger().map { entity ->
        entity?.toActiveRecord()
    }

    val allStoredLedgers: Flow<List<StoredFarmerLedger>> = ledgerDao.observeAllLedgers().map { entities ->
        entities.map { entity ->
            val txs = decodeTransactions(entity.transactionsJson)
            val totals = DevanagariFormatter.calculateTotals(txs)
            StoredFarmerLedger(
                id = entity.id,
                farmerName = entity.farmerName,
                monthYear = entity.monthYear,
                transactionCount = txs.size,
                transactionDates = txs.map { it.date.trim() }.filter { it.isNotBlank() }.distinct(),
                totals = totals,
                isActive = entity.isActive,
                updatedAt = entity.updatedAt,
            )
        }
    }

    val allLedgerSummaries: Flow<List<SavedLedgerSummary>> = allStoredLedgers.map { ledgers ->
        ledgers.map { ledger ->
            SavedLedgerSummary(
                id = ledger.id,
                farmerName = ledger.farmerName,
                monthYear = ledger.monthYear,
                transactionCount = ledger.transactionCount,
                grandBalance = ledger.totals.grandBalance,
                isActive = ledger.isActive,
                updatedAt = ledger.updatedAt,
            )
        }
    }

    suspend fun ensureSeeded() {
        if (ledgerDao.getLedgerCount() == 0) {
            val defaultEntity = LedgerEntity(
                farmerName = DefaultLedgerData.DEFAULT_FARMER_NAME,
                monthYear = DefaultLedgerData.DEFAULT_MONTH_YEAR,
                transactionsJson = encodeTransactions(DefaultLedgerData.defaultTransactions),
                rawBulkText = "",
                isActive = true,
                updatedAt = System.currentTimeMillis(),
            )
            ledgerDao.insertLedger(defaultEntity)
        }
    }

    suspend fun saveActiveLedger(
        id: Long?,
        farmerName: String,
        monthYear: String,
        transactions: List<LedgerTransaction>,
        rawBulkText: String,
    ): Long {
        val json = encodeTransactions(transactions)
        val entity = LedgerEntity(
            id = id ?: 0L,
            farmerName = farmerName,
            monthYear = monthYear,
            transactionsJson = json,
            rawBulkText = rawBulkText,
            isActive = true,
            updatedAt = System.currentTimeMillis(),
        )
        return if (id == null || id == 0L) {
            ledgerDao.insertAndActivateLedger(entity)
        } else {
            ledgerDao.insertLedger(entity)
        }
    }

    suspend fun createAndActivateLedger(
        farmerName: String,
        monthYear: String,
        transactions: List<LedgerTransaction>,
        rawBulkText: String = "",
    ): Long {
        return ledgerDao.insertAndActivateLedger(
            LedgerEntity(
                farmerName = farmerName,
                monthYear = monthYear,
                transactionsJson = encodeTransactions(transactions),
                rawBulkText = rawBulkText,
                isActive = true,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun activateLedger(id: Long) {
        ledgerDao.switchActiveLedger(id)
    }

    suspend fun deleteLedger(id: Long) {
        val target = ledgerDao.getLedgerById(id)
        ledgerDao.deleteLedgerById(id)
        if (target?.isActive == true) {
            val remaining = ledgerDao.getMostRecentLedger()
            if (remaining != null) {
                ledgerDao.switchActiveLedger(remaining.id)
            } else {
                ensureSeeded()
            }
        }
    }

    fun exportToJson(
        farmerName: String,
        monthYear: String,
        transactions: List<LedgerTransaction>,
    ): String {
        val payload = LedgerJsonPayload(
            farmerName = farmerName,
            monthYear = monthYear,
            transactions = transactions,
        )
        return payloadAdapter.toJson(payload)
    }

    fun importFromJson(jsonText: String): LedgerJsonPayload? {
        return runCatching { payloadAdapter.fromJson(jsonText) }.getOrNull()
    }

    private fun encodeTransactions(transactions: List<LedgerTransaction>): String {
        return txListAdapter.toJson(transactions)
    }

    private fun decodeTransactions(json: String): List<LedgerTransaction> {
        return runCatching { txListAdapter.fromJson(json).orEmpty() }.getOrDefault(emptyList())
    }

    private fun LedgerEntity.toActiveRecord(): ActiveLedgerRecord {
        return ActiveLedgerRecord(
            id = id,
            farmerName = farmerName,
            monthYear = monthYear,
            transactions = decodeTransactions(transactionsJson),
            rawBulkText = rawBulkText,
            updatedAt = updatedAt,
        )
    }
}
