package com.example.domain.util

import com.example.domain.model.BillLanguage

data class BillLabels(
    val stallTitle: String,
    val stallSubtitle: String,
    val namePrefix: String,
    val monthPrefix: String,
    val totalRowLabel: String,
    val emptyRowsLabel: String,
    val columns: List<String>,
)

object BillLanguageConverter {

    private val marathiLabels = BillLabels(
        stallTitle = "अशोक फ्लॉवर स्टॉल",
        stallSubtitle = "फुलांचे विक्रेते व कमिशन एजंट",
        namePrefix = "नाव :",
        monthPrefix = "महिना :",
        totalRowLabel = "एकूण",
        emptyRowsLabel = "व्यवहार उपलब्ध नाहीत",
        columns = listOf(
            "तारीख",
            "डाग",
            "फुलांचे नाव",
            "एकूण",
            "दर",
            "ए. विक्री",
            "कमिशन",
            "हमाली",
            "जा. भाडे",
            "मो. भाडे",
            "टपाल",
            "ए. खर्च",
            "ए. शिल्लक",
        ),
    )

    private val englishLabels = BillLabels(
        stallTitle = "ASHOK FLOWER STALL",
        stallSubtitle = "Flower Merchants & Commission Agents",
        namePrefix = "Name :",
        monthPrefix = "Month :",
        totalRowLabel = "Total",
        emptyRowsLabel = "No transactions available",
        columns = listOf(
            "Date",
            "Bags",
            "Flower",
            "Wt(kg)",
            "Rate",
            "Sale",
            "Comm.",
            "Hamali",
            "Local Tpt",
            "Transport",
            "Postage",
            "Spend",
            "Balance",
        ),
    )

    private val engToMarathiMonths = mapOf(
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

    private val marathiToEngMonths = mapOf(
        "जानेवारी" to "January",
        "फेब्रुवारी" to "February",
        "मार्च" to "March",
        "एप्रिल" to "April",
        "मे" to "May",
        "जून" to "June",
        "जुलै" to "July",
        "ऑगस्ट" to "August",
        "सप्टेंबर" to "September",
        "ऑक्टोबर" to "October",
        "नोव्हेंबर" to "November",
        "डिसेंबर" to "December",
    )

    private val flowerToMarathi = mapOf(
        "kk" to "के. के.",
        "k.k." to "के. के.",
        "k.k" to "के. के.",
        "k k" to "के. के.",
        "rose" to "गुलाब",
        "gulab" to "गुलाब",
        "shevanti" to "शेवंती",
        "marigold" to "झेंडू",
        "zendu" to "झेंडू",
        "aster" to "अ‍ॅस्टर",
        "bijli" to "बिजली",
        "chafa" to "चाफा",
        "mogra" to "मोगरा",
        "gerbera" to "जरबेरा",
        "carnation" to "कार्नेशियन",
        "orchid" to "ऑर्चिड",
        "lily" to "लिली",
    )

    private val flowerToEnglish = mapOf(
        "के. के." to "K.K.",
        "के.के." to "K.K.",
        "के. के" to "K.K.",
        "kk" to "K.K.",
        "k.k" to "K.K.",
        "k.k." to "K.K.",
        "गुलाब" to "Rose",
        "gulab" to "Rose",
        "शेवंती" to "Shevanti",
        "झेंडू" to "Marigold",
        "zendu" to "Marigold",
        "अ‍ॅस्टर" to "Aster",
        "अस्टर" to "Aster",
        "बिजली" to "Bijli",
        "चाफा" to "Chafa",
        "मोगरा" to "Mogra",
        "जरबेरा" to "Gerbera",
        "कार्नेशियन" to "Carnation",
        "ऑर्चिड" to "Orchid",
        "लिली" to "Lily",
    )

    private val knownNamesMarathiToEnglish = mapOf(
        "धरणेंद्र भुजप्पा मगदूम" to "Dharanendra Bhujappa Magdum",
        "अशोक" to "Ashok",
    )

    private val knownNamesEnglishToMarathi = mapOf(
        "dharanendra bhujappa magdum" to "धरणेंद्र भुजप्पा मगदूम",
        "ashok" to "अशोक",
    )

    fun labels(language: BillLanguage): BillLabels =
        if (language == BillLanguage.MARATHI) marathiLabels else englishLabels

    fun formatPageIndicator(page: Int, totalPages: Int, language: BillLanguage): String {
        return if (language == BillLanguage.MARATHI) {
            "(पान ${DevanagariFormatter.toDevanagari(page.toString())}/${DevanagariFormatter.toDevanagari(totalPages.toString())})"
        } else {
            "(Page $page/$totalPages)"
        }
    }

    fun formatNumber(value: Double?, language: BillLanguage): String {
        return if (language == BillLanguage.MARATHI) {
            DevanagariFormatter.toDevanagariNumber(value)
        } else {
            DevanagariFormatter.formatIndianNumber(value)
        }
    }

    fun formatDate(date: String, language: BillLanguage): String {
        if (date.isBlank()) return "-"
        return if (language == BillLanguage.MARATHI) {
            DevanagariFormatter.toDevanagari(date)
        } else {
            DevanagariFormatter.toAsciiDigits(date)
        }
    }

    fun formatMonthYear(monthYear: String, language: BillLanguage): String {
        if (monthYear.isBlank()) return "—"
        val trimmed = monthYear.trim()
        return if (language == BillLanguage.MARATHI) {
            var converted = trimmed
            for ((eng, mar) in engToMarathiMonths) {
                val regex = Regex("\\b$eng\\b", RegexOption.IGNORE_CASE)
                if (regex.containsMatchIn(converted)) {
                    converted = regex.replace(converted, mar)
                }
            }
            // Convert 2-digit year like "-26" or "- 26" to 4-digit "2026" if needed
            val shortYear = Regex("""(\s*-\s*)(\d{2})$""").find(converted)
            if (shortYear != null) {
                val yy = shortYear.groupValues[2]
                converted = converted.substring(0, shortYear.range.first) + " - 20$yy"
            }
            DevanagariFormatter.toDevanagari(converted)
        } else {
            var converted = DevanagariFormatter.toAsciiDigits(trimmed)
            for ((mar, eng) in marathiToEngMonths) {
                if (converted.contains(mar)) {
                    converted = converted.replace(mar, eng)
                }
            }
            converted
        }
    }

    fun formatFlowerName(flowerName: String, language: BillLanguage): String {
        if (flowerName.isBlank()) return "-"
        val trimmed = flowerName.trim()
        val lower = trimmed.lowercase()
        return if (language == BillLanguage.MARATHI) {
            flowerToMarathi[lower]
                ?: if (containsDevanagari(trimmed)) trimmed else transliterateLatinToDevanagari(trimmed)
        } else {
            flowerToEnglish[trimmed]
                ?: flowerToEnglish[lower]
                ?: if (containsDevanagari(trimmed)) transliterateDevanagariToLatin(trimmed) else trimmed
        }
    }

    fun formatFarmerName(farmerName: String, language: BillLanguage): String {
        if (farmerName.isBlank()) return "—"
        val trimmed = farmerName.trim()
        return if (language == BillLanguage.MARATHI) {
            knownNamesEnglishToMarathi[trimmed.lowercase()]
                ?: if (containsDevanagari(trimmed)) {
                    DevanagariFormatter.toDevanagari(trimmed)
                } else {
                    transliterateLatinToDevanagari(trimmed)
                }
        } else {
            knownNamesMarathiToEnglish[trimmed]
                ?: if (containsDevanagari(trimmed)) {
                    transliterateDevanagariToLatin(trimmed)
                } else {
                    DevanagariFormatter.toAsciiDigits(trimmed)
                }
        }
    }

    fun formatSubLabel(subLabel: String, language: BillLanguage): String {
        if (subLabel.isBlank()) return ""
        return if (language == BillLanguage.MARATHI) {
            DevanagariFormatter.toDevanagari(subLabel)
        } else {
            if (containsDevanagari(subLabel)) transliterateDevanagariToLatin(subLabel)
            else DevanagariFormatter.toAsciiDigits(subLabel)
        }
    }

    fun containsDevanagari(text: String): Boolean {
        return text.any { it in '\u0900'..'\u097F' }
    }

    // ── Devanagari -> Latin phonetic transliteration ─────────────────────────

    private val devIndependentVowels = mapOf(
        'अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee",
        'उ' to "u", 'ऊ' to "oo", 'ऋ' to "ru", 'ए' to "e",
        'ऐ' to "ai", 'ओ' to "o", 'औ' to "au", 'ऑ' to "o", 'ॲ' to "a",
    )

    private val devMatras = mapOf(
        'ा' to "a", 'ि' to "i", 'ी' to "ee", 'ु' to "u", 'ू' to "oo",
        'ृ' to "ru", 'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au",
        'ॉ' to "o", 'ॅ' to "a",
    )

    private val devConsonants = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ng",
        'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ञ' to "ny",
        'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n",
        'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m",
        'य' to "y", 'र' to "r", 'ल' to "l", 'ळ' to "l", 'व' to "v",
        'श' to "sh", 'ष' to "sh", 'स' to "s", 'ह' to "h",
    )

    fun transliterateDevanagariToLatin(input: String): String {
        val words = input.trim().split(Regex("\\s+"))
        return words.joinToString(" ") { word ->
            val out = StringBuilder()
            var i = 0
            while (i < word.length) {
                val ch = word[i]
                when {
                    ch in '०'..'९' -> {
                        out.append(('0'.code + (ch - '०')).toChar())
                    }
                    devIndependentVowels.containsKey(ch) -> {
                        out.append(devIndependentVowels[ch])
                    }
                    devConsonants.containsKey(ch) -> {
                        val base = devConsonants[ch]!!
                        out.append(base)
                        val next = if (i + 1 < word.length) word[i + 1] else null
                        when {
                            next == '्' -> {
                                // Halant suppresses inherent vowel
                                i++
                            }
                            next != null && devMatras.containsKey(next) -> {
                                out.append(devMatras[next])
                                i++
                            }
                            else -> {
                                // Add inherent 'a' unless at end of word or followed by anusvara at end
                                val isWordEnd = i == word.lastIndex
                                if (!isWordEnd) {
                                    out.append('a')
                                }
                            }
                        }
                    }
                    ch == 'ं' -> out.append('n')
                    ch == 'ँ' -> out.append('n')
                    ch == 'ः' -> out.append('h')
                    ch == '‍' || ch == '‌' || ch == '़' -> Unit
                    else -> out.append(ch)
                }
                i++
            }
            out.toString().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    // ── Latin -> Devanagari phonetic transliteration ─────────────────────────

    private val latinDigraphConsonants = mapOf(
        "chh" to "छ", "ksh" to "क्ष", "dny" to "ज्ञ", "gny" to "ज्ञ",
        "kh" to "ख", "gh" to "घ", "ch" to "च", "jh" to "झ",
        "th" to "थ", "dh" to "ध", "ph" to "फ", "bh" to "भ",
        "sh" to "श", "ng" to "ंग",
    )

    private val latinSingleConsonants = mapOf(
        'k' to "क", 'g' to "ग", 'c' to "क", 'j' to "ज",
        't' to "त", 'd' to "द", 'n' to "न", 'p' to "प",
        'f' to "फ", 'b' to "ब", 'm' to "म", 'y' to "य",
        'r' to "र", 'l' to "ल", 'v' to "व", 'w' to "व",
        's' to "स", 'h' to "ह", 'z' to "झ", 'q' to "क", 'x' to "क्स",
    )

    private val latinVowelStandalone = listOf(
        "aa" to "आ", "ee" to "ई", "oo" to "ऊ", "ai" to "ऐ", "au" to "औ",
        "ou" to "औ", "a" to "अ", "i" to "इ", "u" to "उ", "e" to "ए", "o" to "ओ",
    )

    private val latinVowelMatra = listOf(
        "aa" to "ा", "ee" to "ी", "oo" to "ू", "ai" to "ै", "au" to "ौ",
        "ou" to "ौ", "a" to "", "i" to "ि", "u" to "ु", "e" to "े", "o" to "ो",
    )

    fun transliterateLatinToDevanagari(input: String): String {
        val words = input.trim().split(Regex("\\s+"))
        return words.joinToString(" ") { rawWord ->
            val word = rawWord.lowercase()
            val out = StringBuilder()
            var i = 0
            while (i < word.length) {
                val ch = word[i]
                if (ch in '0'..'9') {
                    out.append(DevanagariFormatter.toDevanagari(ch.toString()))
                    i++
                    continue
                }
                if (ch !in 'a'..'z') {
                    out.append(rawWord[i])
                    i++
                    continue
                }

                // Match vowel at start or after vowel
                val standaloneVowel = latinVowelStandalone.firstOrNull { (k, _) ->
                    word.startsWith(k, i)
                }
                if (standaloneVowel != null) {
                    out.append(standaloneVowel.second)
                    i += standaloneVowel.first.length
                    continue
                }

                // Match consonant (digraph first)
                val consMatch = latinDigraphConsonants.entries.firstOrNull { (k, _) ->
                    word.startsWith(k, i)
                }
                val consDev: String
                val consLen: Int
                if (consMatch != null) {
                    consDev = consMatch.value
                    consLen = consMatch.key.length
                } else {
                    consDev = latinSingleConsonants[ch] ?: ch.toString()
                    consLen = 1
                }
                out.append(consDev)
                i += consLen

                // Check following vowel for matra
                if (i < word.length) {
                    val matraMatch = latinVowelMatra.firstOrNull { (k, _) ->
                        word.startsWith(k, i)
                    }
                    if (matraMatch != null) {
                        out.append(matraMatch.second)
                        i += matraMatch.first.length
                    } else if (word[i] in 'a'..'z' && consDev != "ंग") {
                        // Conjunct consonant if another consonant immediately follows
                        if (i < word.lastIndex || word[i] != 'n') {
                            out.append("्")
                        }
                    }
                }
            }
            out.toString()
        }
    }
}
