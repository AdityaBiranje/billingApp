package com.example.domain.util

import com.example.domain.model.BillLanguage
import com.example.domain.model.LedgerTotals
import com.example.domain.model.MonthlyAggregateSummary
import com.example.domain.model.StoredFarmerLedger

object MonthlyLedgerAggregator {

    private val monthNameToNumber = mapOf(
        "january" to 1, "jan" to 1, "जानेवारी" to 1,
        "february" to 2, "feb" to 2, "फेब्रुवारी" to 2,
        "march" to 3, "mar" to 3, "मार्च" to 3,
        "april" to 4, "apr" to 4, "एप्रिल" to 4,
        "may" to 5, "मे" to 5,
        "june" to 6, "jun" to 6, "जून" to 6,
        "july" to 7, "jul" to 7, "जुलै" to 7,
        "august" to 8, "aug" to 8, "ऑगस्ट" to 8,
        "september" to 9, "sep" to 9, "sept" to 9, "सप्टेंबर" to 9,
        "october" to 10, "oct" to 10, "ऑक्टोबर" to 10,
        "november" to 11, "nov" to 11, "नोव्हेंबर" to 11,
        "december" to 12, "dec" to 12, "डिसेंबर" to 12,
    )

    private val numberToEnglishMonth = mapOf(
        1 to "January", 2 to "February", 3 to "March", 4 to "April",
        5 to "May", 6 to "June", 7 to "July", 8 to "August",
        9 to "September", 10 to "October", 11 to "November", 12 to "December",
    )

    fun normalizeFarmerKey(farmerName: String): String {
        val trimmed = farmerName.trim()
        if (trimmed.isBlank()) return ""
        return BillLanguageConverter.formatFarmerName(trimmed, BillLanguage.ENGLISH)
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun normalizeMonthKey(monthYear: String): String {
        val trimmed = monthYear.trim()
        if (trimmed.isBlank()) return "UNASSIGNED"
        val ascii = DevanagariFormatter.toAsciiDigits(trimmed).lowercase()
        val engConverted = BillLanguageConverter.formatMonthYear(trimmed, BillLanguage.ENGLISH)
            .lowercase()

        var foundMonth: Int? = null
        for ((token, num) in monthNameToNumber) {
            if (ascii.contains(token) || engConverted.contains(token)) {
                foundMonth = num
                break
            }
        }

        val yearMatch = Regex("""(20\d{2}|\d{2})""").findAll(ascii).lastOrNull()?.value
        val resolvedYear = when {
            yearMatch == null -> null
            yearMatch.length == 2 -> 2000 + (yearMatch.toIntOrNull() ?: 0)
            else -> yearMatch.toIntOrNull()
        }

        return if (foundMonth != null && resolvedYear != null) {
            val monthStr = foundMonth.toString().padStart(2, '0')
            "$resolvedYear-$monthStr"
        } else {
            engConverted.replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "UNASSIGNED" }
        }
    }

    fun computeSortOrder(monthKey: String): Int {
        val match = Regex("""^(\d{4})-(\d{2})$""").matchEntire(monthKey) ?: return Int.MAX_VALUE
        val year = match.groupValues[1].toIntOrNull() ?: return Int.MAX_VALUE
        val month = match.groupValues[2].toIntOrNull() ?: return Int.MAX_VALUE
        return year * 100 + month
    }

    fun formatMonthKeyForLanguage(
        monthKey: String,
        fallbackRaw: String,
        language: BillLanguage,
    ): String {
        val match = Regex("""^(\d{4})-(\d{2})$""").matchEntire(monthKey)
        if (match != null) {
            val year = match.groupValues[1]
            val monthNum = match.groupValues[2].toIntOrNull()
            val engMonth = numberToEnglishMonth[monthNum]
            if (engMonth != null) {
                val engString = "$engMonth - $year"
                return BillLanguageConverter.formatMonthYear(engString, language)
            }
        }
        if (monthKey == "UNASSIGNED" || fallbackRaw.isBlank()) {
            return if (language == BillLanguage.ENGLISH) "Unassigned" else "महिना नाही"
        }
        return BillLanguageConverter.formatMonthYear(fallbackRaw, language)
    }

    fun combineTotals(ledgers: List<StoredFarmerLedger>): LedgerTotals {
        var totalBags = 0.0
        var totalWeight = 0.0
        var totalSaleAmount = 0.0
        var totalCommission = 0.0
        var totalHamali = 0.0
        var totalLocalTransport = 0.0
        var totalMainTransport = 0.0
        var totalPostage = 0.0
        var totalSpend = 0.0
        var grandBalance = 0.0

        ledgers.forEach { ledger ->
            val t = ledger.totals
            totalBags += t.totalBags
            totalWeight += t.totalWeight
            totalSaleAmount += t.totalSaleAmount
            totalCommission += t.totalCommission
            totalHamali += t.totalHamali
            totalLocalTransport += t.totalLocalTransport
            totalMainTransport += t.totalMainTransport
            totalPostage += t.totalPostage
            totalSpend += t.totalSpend
            grandBalance += t.grandBalance
        }

        return LedgerTotals(
            totalBags = totalBags,
            totalWeight = totalWeight,
            totalSaleAmount = totalSaleAmount,
            totalCommission = totalCommission,
            totalHamali = totalHamali,
            totalLocalTransport = totalLocalTransport,
            totalMainTransport = totalMainTransport,
            totalPostage = totalPostage,
            totalSpend = totalSpend,
            grandBalance = grandBalance,
        )
    }

    fun aggregateByMonth(storedLedgers: List<StoredFarmerLedger>): List<MonthlyAggregateSummary> {
        val nonEmptyBills = storedLedgers.filter {
            it.transactionCount > 0 || it.totals.totalSaleAmount > 0.0 || it.farmerName.isNotBlank()
        }
        if (nonEmptyBills.isEmpty()) return emptyList()

        return nonEmptyBills
            .groupBy { normalizeMonthKey(it.monthYear) }
            .map { (monthKey, group) ->
                val distinctFarmers = group
                    .map { normalizeFarmerKey(it.farmerName) }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .size
                    .coerceAtLeast(1)

                val representativeRaw = group.firstOrNull { it.monthYear.isNotBlank() }?.monthYear ?: ""
                MonthlyAggregateSummary(
                    monthKey = monthKey,
                    monthYearRaw = representativeRaw,
                    sortOrder = computeSortOrder(monthKey),
                    farmerCount = distinctFarmers,
                    billCount = group.size,
                    totals = combineTotals(group),
                    ledgers = group.sortedByDescending { it.totals.totalSaleAmount },
                )
            }
            .sortedWith(
                compareBy<MonthlyAggregateSummary> { it.sortOrder }
                    .thenByDescending { it.totals.totalSaleAmount }
            )
    }
}
