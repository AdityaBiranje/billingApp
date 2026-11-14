package com.example.domain.util

import com.example.domain.model.LedgerTotals
import com.example.domain.model.LedgerTransaction
import kotlin.math.abs

object DevanagariFormatter {

    private val devanagariDigits = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')

    fun toDevanagari(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(devanagariDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toAsciiDigits(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '०'..'९') {
                sb.append(('0'.code + (ch - '०')).toChar())
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatIndianNumber(num: Double?): String {
        if (num == null || num.isNaN() || abs(num) < 0.0001) return "-"
        val isNegative = num < 0
        val absVal = abs(num)
        val rounded = kotlin.math.round(absVal * 100.0) / 100.0
        val intPart = rounded.toLong()
        val fracPart = kotlin.math.round((rounded - intPart) * 100.0).toInt()

        val digits = intPart.toString()
        val groupedInt = if (digits.length <= 3) {
            digits
        } else {
            val lastThree = digits.takeLast(3)
            val rest = digits.dropLast(3)
            val restGrouped = rest.reversed().chunked(2).joinToString(",").reversed()
            "$restGrouped,$lastThree"
        }

        val fracSuffix = when {
            fracPart == 0 -> ""
            fracPart % 10 == 0 -> ".${fracPart / 10}"
            else -> ".${fracPart.toString().padStart(2, '0')}"
        }
        return (if (isNegative) "-" else "") + groupedInt + fracSuffix
    }

    fun formatPlainNumberForInput(num: Double): String {
        if (abs(num) < 0.0001) return ""
        return if (num % 1.0 == 0.0) {
            num.toLong().toString()
        } else {
            num.toString()
        }
    }

    fun toDevanagariNumber(num: Double?): String {
        if (num == null || num.isNaN() || abs(num) < 0.0001) return "-"
        val formatted = formatIndianNumber(num)
        return if (formatted == "-") "-" else toDevanagari(formatted)
    }

    fun formatInrDashboard(num: Double): String {
        val formatted = formatIndianNumber(num)
        return if (formatted == "-") "₹0" else "₹$formatted"
    }

    fun formatWeightDashboard(weight: Double): String {
        if (abs(weight) < 0.0001) return "0 kg"
        val formatted = formatIndianNumber(weight)
        return "$formatted kg"
    }

    fun formatBagsDashboard(bags: Double): String {
        if (abs(bags) < 0.0001) return "0"
        return formatIndianNumber(bags)
    }

    fun calculateTotals(transactions: List<LedgerTransaction>): LedgerTotals {
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

        for (tx in transactions) {
            for (item in tx.items) {
                totalBags += item.bags
                totalWeight += item.weight
                totalSaleAmount += item.saleAmount
            }
            totalCommission += tx.expenses.commission
            totalHamali += tx.expenses.hamali
            totalLocalTransport += tx.expenses.localTransport
            totalMainTransport += tx.expenses.mainTransport
            totalPostage += tx.expenses.postage

            val spend = tx.totalSpend
            val sale = tx.totalSale
            totalSpend += spend
            grandBalance += (sale - spend)
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

    fun buildPdfFileName(farmerName: String, monthYear: String): String {
        val illegalChars = Regex("""[/\\?%*:|"<>]""")
        val cleanFarmer = farmerName.replace(illegalChars, "-").trim()
        val cleanMonth = monthYear.replace(illegalChars, "-").trim()
        return when {
            cleanFarmer.isNotEmpty() && cleanMonth.isNotEmpty() -> "$cleanFarmer - $cleanMonth.pdf"
            cleanFarmer.isNotEmpty() -> "$cleanFarmer.pdf"
            cleanMonth.isNotEmpty() -> "$cleanMonth.pdf"
            else -> "ledger_report.pdf"
        }
    }
}
