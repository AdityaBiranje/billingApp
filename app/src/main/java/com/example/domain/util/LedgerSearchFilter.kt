package com.example.domain.util

import com.example.domain.model.BillLanguage
import com.example.domain.model.LedgerTransaction
import com.example.domain.model.StoredFarmerLedger

object LedgerSearchFilter {

    fun matchesDate(date: String, rawQuery: String): Boolean {
        val queryNorm = normalizeSearchToken(rawQuery)
        if (queryNorm.isBlank()) return true
        if (date.isBlank()) return false

        val dateVariants = buildDateVariants(date)
        val querySlash = queryNorm.replace('-', '/')
        val queryDash = queryNorm.replace('/', '-')
        val queryUnpaddedDash = stripLeadingZerosInDate(queryDash)
        val queryUnpaddedSlash = stripLeadingZerosInDate(querySlash)
        val candidateQueries = listOf(
            queryNorm,
            queryDash,
            querySlash,
            queryUnpaddedDash,
            queryUnpaddedSlash,
        ).filter { it.isNotBlank() }.distinct()

        return dateVariants.any { variant ->
            candidateQueries.any { q ->
                variant == q ||
                    variant.startsWith(q) ||
                    variant.contains("-$q") ||
                    variant.contains("/$q")
            }
        }
    }

    fun matchesFarmerName(farmerName: String, rawQuery: String): Boolean {
        val queryNorm = normalizeSearchToken(rawQuery)
        if (queryNorm.isBlank()) return true
        if (farmerName.isBlank()) return false

        val rawLower = farmerName.trim().lowercase()
        val engLower = BillLanguageConverter.formatFarmerName(farmerName, BillLanguage.ENGLISH)
            .trim()
            .lowercase()
        val marLower = BillLanguageConverter.formatFarmerName(farmerName, BillLanguage.MARATHI)
            .trim()
            .lowercase()
        val queryAscii = DevanagariFormatter.toAsciiDigits(rawQuery).trim().lowercase()
        val queryRaw = rawQuery.trim().lowercase()

        return rawLower.contains(queryRaw) ||
            rawLower.contains(queryAscii) ||
            engLower.contains(queryAscii) ||
            engLower.contains(queryRaw) ||
            marLower.contains(queryRaw)
    }

    fun matchesLedger(ledger: StoredFarmerLedger, rawQuery: String): Boolean {
        if (rawQuery.isBlank()) return true
        val monthMatches = matchesMonth(ledger.monthYear, rawQuery)
        val nameMatches = matchesFarmerName(ledger.farmerName, rawQuery)
        val dateMatches = ledger.transactionDates.any { matchesDate(it, rawQuery) }
        return nameMatches || monthMatches || dateMatches
    }

    fun matchingDatesForLedger(ledger: StoredFarmerLedger, rawQuery: String): List<String> {
        if (rawQuery.isBlank()) return emptyList()
        return ledger.transactionDates.filter { matchesDate(it, rawQuery) }
    }

    fun filterLedgers(
        ledgers: List<StoredFarmerLedger>,
        rawQuery: String,
    ): List<StoredFarmerLedger> {
        val nonBlank = ledgers.filter {
            it.transactionCount > 0 || it.totals.totalSaleAmount > 0.0 || it.farmerName.isNotBlank()
        }
        if (rawQuery.isBlank()) return nonBlank
        return nonBlank.filter { matchesLedger(it, rawQuery) }
    }

    fun filterTransactions(
        transactions: List<LedgerTransaction>,
        activeFarmerName: String,
        rawQuery: String,
    ): List<LedgerTransaction> {
        if (rawQuery.isBlank()) return transactions
        val queryLower = rawQuery.trim().lowercase()
        val directMatches = transactions.filter { tx ->
            matchesDate(tx.date, rawQuery) ||
                tx.items.any { item ->
                    item.flowerName.lowercase().contains(queryLower) ||
                        BillLanguageConverter.formatFlowerName(item.flowerName, BillLanguage.ENGLISH)
                            .lowercase()
                            .contains(queryLower) ||
                        item.subLabel.lowercase().contains(queryLower)
                }
        }
        if (directMatches.isNotEmpty()) return directMatches
        return if (matchesFarmerName(activeFarmerName, rawQuery)) transactions else emptyList()
    }

    private fun matchesMonth(monthYear: String, rawQuery: String): Boolean {
        if (monthYear.isBlank()) return false
        val qAscii = DevanagariFormatter.toAsciiDigits(rawQuery).trim().lowercase()
        val qRaw = rawQuery.trim().lowercase()
        val rawLower = DevanagariFormatter.toAsciiDigits(monthYear).trim().lowercase()
        val engLower = BillLanguageConverter.formatMonthYear(monthYear, BillLanguage.ENGLISH)
            .trim()
            .lowercase()
        val marLower = BillLanguageConverter.formatMonthYear(monthYear, BillLanguage.MARATHI)
            .trim()
            .lowercase()
        return rawLower.contains(qAscii) ||
            engLower.contains(qAscii) ||
            marLower.contains(qRaw)
    }

    private fun normalizeSearchToken(input: String): String {
        return DevanagariFormatter.toAsciiDigits(input)
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }

    private fun buildDateVariants(date: String): List<String> {
        val trimmed = date.trim().lowercase()
        val asciiDash = DevanagariFormatter.toAsciiDigits(trimmed).replace('/', '-')
        val asciiSlash = asciiDash.replace('-', '/')
        val unpaddedDash = stripLeadingZerosInDate(asciiDash)
        val unpaddedSlash = stripLeadingZerosInDate(asciiSlash)
        return listOf(trimmed, asciiDash, asciiSlash, unpaddedDash, unpaddedSlash).distinct()
    }

    private fun stripLeadingZerosInDate(dateStr: String): String {
        return dateStr.split('-', '/')
            .joinToString(if (dateStr.contains('/')) "/" else "-") { part ->
                part.trimStart('0').ifEmpty { "0" }
            }
    }
}
