package com.example.domain.parser

import com.example.domain.model.FlowerLotItem
import com.example.domain.model.LedgerTransaction
import com.example.domain.model.ParsedLedgerResult
import com.example.domain.model.TransactionExpenses
import com.example.domain.util.DevanagariFormatter

object WhatsAppLedgerParser {

    const val QUICK_SAMPLE_TEXT = """🌹 ASHOK FLOWER STALL 🌹
---
August-26.
---
Dharanendra Bhujappa Magdum
flower name - kk 
---
3/8=
50-600kg-30rt.
Total sale=18000.
Comm=2700.
Hamali=400.
Transport=5000.
Postage=5.
Total spend=8105.
Bal=9895.
---
5/8=
40-480kg-30rt.
Total sale=14400.
Comm=2160.
Hamali=320.
Transport=4000.
Postage=5.
Total spend=6485.
Bal=7915.
---
6/8=
20-240kg-30rt.
SRS
25-300kg-30rt.
Total sale=16200.
Comm=2430.
Hamali=360.
Transport=4500.
Postage=5.
Total spend=7295.
Bal=8905."""

    private val KNOWN_TRANSPORTER_CODES = setOf(
        "SRS", "VRL", "KRL", "MST", "TPT", "ABT", "ST", "KPN", "SRS TRAVELS", "NOTE"
    )

    private val KNOWN_FLOWERS = listOf(
        "kk", "k.k.", "k.k",
        "gulab", "rose", "गुलाब",
        "shevanti", "शेवंती",
        "marigold", "झेंडू", "zendu",
        "aster", "अ‍ॅस्टर", "अस्टर",
        "bijli", "बिजली",
        "chafa", "चाफा",
        "mogra", "मोगरा",
        "gerbera", "जरबेरा",
        "carnation", "कार्नेशियन",
        "orchid", "ऑर्चिड",
        "lily", "लिली",
    )

    private val MONTH_MAP = mapOf(
        "january" to "जानेवारी",
        "february" to "फेब्रुवारी",
        "march" to "मार्च",
        "april" to "एप्रिल",
        "may" to "मे",
        "june" to "जून",
        "july" to "जुलै",
        "august" to "ऑगस्ट",
        "september" to "सप्टेंबर",
        "october" to "ऑक्टोबर",
        "november" to "नोव्हेंबर",
        "december" to "डिसेंबर",
    )

    private val FLOWER_MAP = mapOf(
        "kk" to "के. के.",
        "k.k." to "के. के.",
        "k.k" to "के. के.",
        "gulab" to "गुलाब",
        "rose" to "गुलाब",
        "shevanti" to "शेवंती",
        "marigold" to "झेंडू",
        "zendu" to "झेंडू",
        "aster" to "अ‍ॅस्टर",
        "bijli" to "बिजली",
        "chafa" to "चाफा",
        "mogra" to "मोगरा",
    )

    private val DATE_LINE_REGEX = Regex("""^(\d{1,2})\s*/\s*(\d{1,2})\s*=""")
    private val ITEM_LINE_REGEX = Regex("""^(\d+)\s*-\s*(\d+(?:\.\d+)?)\s*kg\s*-\s*(\d+(?:\.\d+)?)\s*rt""", RegexOption.IGNORE_CASE)
    private val FLOWER_NAME_HEADER_REGEX = Regex("""flower\s*name\s*[-:]\s*(.+)""", RegexOption.IGNORE_CASE)
    private val FLOWER_OVERRIDE_REGEX = Regex("""^flower\s*[-:]\s*(.+)""", RegexOption.IGNORE_CASE)

    fun parseAndConvert(rawText: String): ParsedLedgerResult {
        val rawParsed = parseRawLedger(rawText)
        val withDevanagariDates = convertDatesToDevanagari(rawParsed.transactions, rawParsed.monthYear)
        val withMarathiFlowers = withDevanagariDates.map { tx ->
            tx.copy(
                items = tx.items.map { item ->
                    item.copy(flowerName = convertFlowerToMarathi(item.flowerName))
                }
            )
        }
        val marathiMonth = convertMonthYearToMarathi(rawParsed.monthYear)
        return ParsedLedgerResult(
            farmerName = rawParsed.farmerName,
            monthYear = marathiMonth,
            defaultFlower = convertFlowerToMarathi(rawParsed.defaultFlower),
            transactions = withMarathiFlowers,
        )
    }

    fun parseRawLedger(rawText: String): ParsedLedgerResult {
        if (rawText.isBlank()) {
            return ParsedLedgerResult("", "", "", emptyList())
        }

        // Split by lines consisting of "---" (or more dashes)
        val sections = mutableListOf<List<String>>()
        var currentLines = mutableListOf<String>()
        for (rawLine in rawText.lines()) {
            val trimmed = rawLine.trim()
            if (trimmed == "---" || Regex("""^-{3,}$""").matches(trimmed)) {
                if (currentLines.isNotEmpty()) {
                    sections.add(currentLines)
                    currentLines = mutableListOf()
                }
            } else if (trimmed.isNotEmpty()) {
                currentLines.add(trimmed)
            }
        }
        if (currentLines.isNotEmpty()) {
            sections.add(currentLines)
        }

        var farmerName = ""
        var monthYear = ""
        var defaultFlower = ""
        val transactions = mutableListOf<LedgerTransaction>()

        for ((index, lines) in sections.withIndex()) {
            val hasDate = lines.any { isDateLine(it) }
            if (hasDate) {
                val tx = parseTransactionSection(lines, defaultFlower, transactions.size + 1L)
                if (tx != null) {
                    transactions.add(tx)
                }
            } else if (index < 3) {
                when (index) {
                    0 -> {
                        // Stall header (e.g. ASHOK FLOWER STALL) — skip
                    }
                    1 -> {
                        monthYear = lines.first().removeSuffix(".").trim()
                    }
                    2 -> {
                        for (line in lines) {
                            val flowerMatch = FLOWER_NAME_HEADER_REGEX.find(line)
                            if (flowerMatch != null) {
                                defaultFlower = flowerMatch.groupValues[1].trim().removeSuffix(".").trim()
                            } else if (farmerName.isEmpty()) {
                                farmerName = line.removeSuffix(".").trim()
                            }
                        }
                    }
                }
            }
        }

        return ParsedLedgerResult(
            farmerName = farmerName,
            monthYear = monthYear,
            defaultFlower = defaultFlower,
            transactions = transactions,
        )
    }

    private fun parseTransactionSection(
        lines: List<String>,
        defaultFlower: String,
        txId: Long,
    ): LedgerTransaction? {
        var dateStr = ""
        val items = mutableListOf<FlowerLotItem>()
        var commission = 0.0
        var hamali = 0.0
        var localTransport = 0.0
        var mainTransport = 0.0
        var postage = 0.0

        var currentFlower = defaultFlower
        var currentSubLabel = ""

        for (i in lines.indices) {
            val line = lines[i]
            val lineLower = line.lowercase().replace(Regex("""\s+"""), " ").trim()

            when {
                isDateLine(line) -> {
                    dateStr = parseDateLine(line)
                }
                isItemLine(line) -> {
                    val parsed = parseItemLine(line)
                    if (parsed != null) {
                        val itemId = txId * 1000L + items.size + 1L
                        items.add(
                            FlowerLotItem(
                                id = itemId,
                                bags = parsed.bags,
                                flowerName = currentFlower,
                                subLabel = currentSubLabel,
                                weight = parsed.weight,
                                rate = parsed.rate,
                                saleAmount = parsed.saleAmount,
                            )
                        )
                        currentSubLabel = ""
                        currentFlower = defaultFlower
                    }
                }
                Regex("""^total\s*sale\s*=""").containsMatchIn(lineLower) -> Unit
                Regex("""^comm(?:ission)?\s*=""").containsMatchIn(lineLower) -> {
                    commission = extractValue(line)
                }
                Regex("""^hamali\s*=""").containsMatchIn(lineLower) -> {
                    hamali = extractValue(line)
                }
                Regex("""^local\s*transport\s*=""").containsMatchIn(lineLower) -> {
                    localTransport = extractValue(line)
                }
                Regex("""^transport\s*=""").containsMatchIn(lineLower) -> {
                    mainTransport = extractValue(line)
                }
                Regex("""^postage\s*=""").containsMatchIn(lineLower) -> {
                    postage = extractValue(line)
                }
                Regex("""^total\s*spend\s*=""").containsMatchIn(lineLower) -> Unit
                Regex("""^bal(?:ance)?\s*=""").containsMatchIn(lineLower) -> Unit
                else -> {
                    val nextLine = if (i + 1 < lines.size) lines[i + 1] else ""
                    if (nextLine.isNotEmpty() && isItemLine(nextLine) && !isDateLine(line) && !line.contains('=')) {
                        val cleaned = line.removeSuffix(".").trim()
                        val overrideMatch = FLOWER_OVERRIDE_REGEX.find(cleaned)
                        when {
                            isTransporterOrNote(cleaned) -> {
                                currentSubLabel = cleaned
                                currentFlower = defaultFlower
                            }
                            overrideMatch != null -> {
                                currentFlower = overrideMatch.groupValues[1].trim()
                            }
                            isKnownFlower(cleaned) -> {
                                currentFlower = cleaned
                            }
                            else -> {
                                currentSubLabel = cleaned
                                currentFlower = defaultFlower
                            }
                        }
                    }
                }
            }
        }

        if (dateStr.isEmpty() && items.isEmpty()) return null

        val finalItems = if (items.isNotEmpty()) {
            items
        } else {
            listOf(FlowerLotItem(id = txId * 1000L + 1L, flowerName = defaultFlower))
        }

        return LedgerTransaction(
            id = txId,
            date = dateStr,
            items = finalItems,
            expenses = TransactionExpenses(
                commission = commission,
                hamali = hamali,
                localTransport = localTransport,
                mainTransport = mainTransport,
                postage = postage,
            ),
        )
    }

    private fun parseNum(str: String): Double {
        if (str.isBlank()) return 0.0
        val cleaned = str.replace(Regex("""[,.\s]+$"""), "").replace(",", "").trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun extractValue(line: String): Double {
        val idx = line.indexOf('=')
        if (idx == -1) return 0.0
        return parseNum(line.substring(idx + 1))
    }

    private fun isItemLine(line: String): Boolean = ITEM_LINE_REGEX.containsMatchIn(line.trim())

    private data class ParsedItemValues(
        val bags: Double,
        val weight: Double,
        val rate: Double,
        val saleAmount: Double,
    )

    private fun parseItemLine(line: String): ParsedItemValues? {
        val cleaned = line.trim().removeSuffix(".")
        val match = ITEM_LINE_REGEX.find(cleaned) ?: return null
        val bags = match.groupValues[1].toDoubleOrNull() ?: 0.0
        val weight = match.groupValues[2].toDoubleOrNull() ?: 0.0
        val rate = match.groupValues[3].toDoubleOrNull() ?: 0.0
        return ParsedItemValues(
            bags = bags,
            weight = weight,
            rate = rate,
            saleAmount = weight * rate,
        )
    }

    private fun isDateLine(line: String): Boolean = DATE_LINE_REGEX.containsMatchIn(line.trim())

    private fun parseDateLine(line: String): String {
        val match = DATE_LINE_REGEX.find(line.trim()) ?: return ""
        val day = match.groupValues[1].padStart(2, '0')
        val month = match.groupValues[2].padStart(2, '0')
        return "$day-$month"
    }

    private fun isTransporterOrNote(str: String): Boolean {
        if (str.isBlank()) return false
        val upper = str.uppercase().trim()
        val noDots = upper.replace(".", "")
        if (upper in KNOWN_TRANSPORTER_CODES || noDots in KNOWN_TRANSPORTER_CODES) return true
        return Regex("""^[A-Z]{2,6}$""").matches(noDots)
    }

    private fun isKnownFlower(str: String): Boolean {
        if (str.isBlank()) return false
        val normalized = str.lowercase().replace(Regex("""[\s.\-]"""), "")
        return KNOWN_FLOWERS.any { it.replace(Regex("""[\s.\-]"""), "").lowercase() == normalized }
    }

    fun convertDatesToDevanagari(
        transactions: List<LedgerTransaction>,
        monthYearStr: String,
    ): List<LedgerTransaction> {
        val yearMatch = Regex("""(\d{2,4})""").find(monthYearStr)
        val yearSuffix = yearMatch?.groupValues?.get(1)?.let { y ->
            if (y.length == 4) y.substring(2) else y
        }.orEmpty()

        return transactions.map { tx ->
            if (tx.date.isNotEmpty()) {
                val full = if (yearSuffix.isNotEmpty()) "${tx.date}-$yearSuffix" else tx.date
                tx.copy(date = DevanagariFormatter.toDevanagari(full))
            } else {
                tx
            }
        }
    }

    fun convertMonthYearToMarathi(monthYearStr: String): String {
        if (monthYearStr.isBlank()) return ""
        val parts = monthYearStr.split(Regex("""[-\s]+"""))
        var monthEng = ""
        var year = ""
        for (p in parts) {
            if (Regex("""^\d+$""").matches(p)) {
                year = p
            } else if (p.length > 1) {
                monthEng = p.lowercase()
            }
        }
        val marathiMonth = MONTH_MAP[monthEng] ?: monthYearStr
        val fullYear = if (year.length == 2) "20$year" else year
        val devYear = DevanagariFormatter.toDevanagari(fullYear)
        return if (devYear.isNotEmpty()) "$marathiMonth - $devYear" else marathiMonth
    }

    fun convertFlowerToMarathi(flowerName: String): String {
        if (flowerName.isBlank()) return ""
        val key = flowerName.lowercase().trim()
        return FLOWER_MAP[key] ?: flowerName
    }
}
