package com.example.domain.model

import com.squareup.moshi.JsonClass

enum class BillLanguage {
    MARATHI,
    ENGLISH,
}

@JsonClass(generateAdapter = true)
data class FlowerLotItem(
    val id: Long = System.nanoTime(),
    val bags: Double = 0.0,
    val flowerName: String = "",
    val subLabel: String = "",
    val weight: Double = 0.0,
    val rate: Double = 0.0,
    val saleAmount: Double = 0.0,
)

@JsonClass(generateAdapter = true)
data class TransactionExpenses(
    val commission: Double = 0.0,
    val hamali: Double = 0.0,
    val localTransport: Double = 0.0,
    val mainTransport: Double = 0.0,
    val postage: Double = 0.0,
) {
    val total: Double
        get() = commission + hamali + localTransport + mainTransport + postage
}

@JsonClass(generateAdapter = true)
data class LedgerTransaction(
    val id: Long = System.nanoTime(),
    val date: String = "",
    val items: List<FlowerLotItem> = listOf(FlowerLotItem()),
    val expenses: TransactionExpenses = TransactionExpenses(),
) {
    val totalSale: Double
        get() = items.sumOf { it.saleAmount }

    val totalSpend: Double
        get() = expenses.total

    val balance: Double
        get() = totalSale - totalSpend
}

data class LedgerTotals(
    val totalBags: Double = 0.0,
    val totalWeight: Double = 0.0,
    val totalSaleAmount: Double = 0.0,
    val totalCommission: Double = 0.0,
    val totalHamali: Double = 0.0,
    val totalLocalTransport: Double = 0.0,
    val totalMainTransport: Double = 0.0,
    val totalPostage: Double = 0.0,
    val totalSpend: Double = 0.0,
    val grandBalance: Double = 0.0,
)

@JsonClass(generateAdapter = true)
data class LedgerJsonPayload(
    val farmerName: String = "",
    val monthYear: String = "",
    val transactions: List<LedgerTransaction> = emptyList(),
)

data class SavedLedgerSummary(
    val id: Long,
    val farmerName: String,
    val monthYear: String,
    val transactionCount: Int,
    val grandBalance: Double,
    val isActive: Boolean,
    val updatedAt: Long,
)

data class ParsedLedgerResult(
    val farmerName: String,
    val monthYear: String,
    val defaultFlower: String,
    val transactions: List<LedgerTransaction>,
)

data class StoredFarmerLedger(
    val id: Long,
    val farmerName: String,
    val monthYear: String,
    val transactionCount: Int,
    val transactionDates: List<String> = emptyList(),
    val totals: LedgerTotals,
    val isActive: Boolean,
    val updatedAt: Long,
)

data class MonthlyAggregateSummary(
    val monthKey: String,
    val monthYearRaw: String,
    val sortOrder: Int,
    val farmerCount: Int,
    val billCount: Int,
    val totals: LedgerTotals,
    val ledgers: List<StoredFarmerLedger>,
)

enum class AnalyticsScope {
    ALL_FARMERS_MONTHLY,
    CURRENT_FARMER,
}

