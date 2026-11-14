package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LedgerRepository
import com.example.domain.data.DefaultLedgerData
import com.example.domain.model.AnalyticsScope
import com.example.domain.model.BillLanguage
import com.example.domain.model.FlowerLotItem
import com.example.domain.model.LedgerTotals
import com.example.domain.model.LedgerTransaction
import com.example.domain.model.MonthlyAggregateSummary
import com.example.domain.model.SavedLedgerSummary
import com.example.domain.model.StoredFarmerLedger
import com.example.domain.model.TransactionExpenses
import com.example.domain.parser.WhatsAppLedgerParser
import com.example.domain.util.BillLanguageConverter
import com.example.domain.util.DevanagariFormatter
import com.example.domain.util.LedgerSearchFilter
import com.example.domain.util.MonthlyLedgerAggregator
import com.example.pdf.LedgerPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DashboardTab {
    BULK_PASTE,
    ENTRIES,
    ANALYTICS,
    PREVIEW,
}

data class ParseStatus(
    val isSuccess: Boolean,
    val message: String,
)

data class ParsedSummary(
    val farmerName: String,
    val monthYear: String,
    val transactionCount: Int,
    val totalBags: Double,
)

data class LedgerUiState(
    val activeLedgerId: Long? = null,
    val farmerName: String = DefaultLedgerData.DEFAULT_FARMER_NAME,
    val monthYear: String = DefaultLedgerData.DEFAULT_MONTH_YEAR,
    val rawBulkText: String = "",
    val transactions: List<LedgerTransaction> = DefaultLedgerData.defaultTransactions,
    val totals: LedgerTotals = DevanagariFormatter.calculateTotals(DefaultLedgerData.defaultTransactions),
    val savedLedgers: List<SavedLedgerSummary> = emptyList(),
    val storedLedgers: List<StoredFarmerLedger> = emptyList(),
    val monthlyAggregates: List<MonthlyAggregateSummary> = emptyList(),
    val allFarmersTotals: LedgerTotals = LedgerTotals(),
    val analyticsScope: AnalyticsScope = AnalyticsScope.ALL_FARMERS_MONTHLY,
    val searchQuery: String = "",
    val activeTab: DashboardTab = DashboardTab.BULK_PASTE,
    val billLanguage: BillLanguage = BillLanguage.MARATHI,
    val previewScale: Float = 0.85f,
    val parseStatus: ParseStatus? = null,
    val parsedSummary: ParsedSummary? = null,
    val isGeneratingPdf: Boolean = false,
    val snackbarMessage: String? = null,
) {
    val filteredStoredLedgers: List<StoredFarmerLedger>
        get() = LedgerSearchFilter.filterLedgers(storedLedgers, searchQuery)

    val filteredTransactions: List<LedgerTransaction>
        get() = LedgerSearchFilter.filterTransactions(transactions, farmerName, searchQuery)

    val filteredMonthlyAggregates: List<MonthlyAggregateSummary>
        get() = if (searchQuery.isBlank()) {
            monthlyAggregates
        } else {
            MonthlyLedgerAggregator.aggregateByMonth(filteredStoredLedgers)
        }
}

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LedgerRepository(
        AppDatabase.getInstance(application).ledgerDao()
    )

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    private var persistJob: Job? = null
    private var hasLoadedInitialRecord = false

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.ensureSeeded()
            }
        }

        viewModelScope.launch {
            repository.activeLedger.collect { record ->
                if (record != null) {
                    val isDifferentLedger = _uiState.value.activeLedgerId != record.id
                    if (!hasLoadedInitialRecord || isDifferentLedger) {
                        hasLoadedInitialRecord = true
                        _uiState.update { state ->
                            state.copy(
                                activeLedgerId = record.id,
                                farmerName = record.farmerName,
                                monthYear = record.monthYear,
                                rawBulkText = record.rawBulkText,
                                transactions = record.transactions,
                                totals = DevanagariFormatter.calculateTotals(record.transactions),
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.allStoredLedgers.collect { stored ->
                val aggregates = MonthlyLedgerAggregator.aggregateByMonth(stored)
                val combined = MonthlyLedgerAggregator.combineTotals(stored)
                _uiState.update {
                    it.copy(
                        storedLedgers = stored,
                        monthlyAggregates = aggregates,
                        allFarmersTotals = combined,
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.allLedgerSummaries.collect { summaries ->
                _uiState.update { it.copy(savedLedgers = summaries) }
            }
        }
    }

    fun selectTab(tab: DashboardTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearchQuery() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun updateFarmerName(name: String) {
        _uiState.update { it.copy(farmerName = name) }
        schedulePersist()
    }

    fun updateMonthYear(monthYear: String) {
        _uiState.update { it.copy(monthYear = monthYear) }
        schedulePersist()
    }

    fun updateRawBulkText(text: String) {
        _uiState.update {
            it.copy(
                rawBulkText = text,
                parseStatus = null,
                parsedSummary = null,
            )
        }
        schedulePersist()
    }

    fun loadQuickSample() {
        _uiState.update {
            it.copy(
                rawBulkText = WhatsAppLedgerParser.QUICK_SAMPLE_TEXT,
                parseStatus = null,
                parsedSummary = null,
            )
        }
        schedulePersist()
    }

    fun clearRawText() {
        _uiState.update {
            it.copy(
                rawBulkText = "",
                parseStatus = null,
                parsedSummary = null,
            )
        }
        schedulePersist()
    }

    fun parseBulkText() {
        val currentState = _uiState.value
        val raw = currentState.rawBulkText
        if (raw.isBlank()) {
            _uiState.update {
                it.copy(parseStatus = ParseStatus(false, "कृपया डेटा पेस्ट करा!"))
            }
            return
        }

        val parsed = WhatsAppLedgerParser.parseAndConvert(raw)
        if (parsed.transactions.isEmpty()) {
            _uiState.update {
                it.copy(
                    parseStatus = ParseStatus(false, "कोणताही व्यवहार सापडला नाही. कृपया फॉरमॅट तपासा.")
                )
            }
            return
        }

        val resolvedFarmer = parsed.farmerName.ifBlank { currentState.farmerName }
        val resolvedMonth = parsed.monthYear.ifBlank { currentState.monthYear }
        val totalBags = parsed.transactions.sumOf { tx -> tx.items.sumOf { it.bags } }
        val newTotals = DevanagariFormatter.calculateTotals(parsed.transactions)

        val currentFarmerNorm = MonthlyLedgerAggregator.normalizeFarmerKey(currentState.farmerName)
        val currentMonthNorm = MonthlyLedgerAggregator.normalizeMonthKey(currentState.monthYear)
        val parsedFarmerNorm = MonthlyLedgerAggregator.normalizeFarmerKey(resolvedFarmer)
        val parsedMonthNorm = MonthlyLedgerAggregator.normalizeMonthKey(resolvedMonth)

        val isDifferentFarmerOrMonth = currentState.transactions.isNotEmpty() &&
            parsed.farmerName.isNotBlank() &&
            (parsedFarmerNorm != currentFarmerNorm ||
                (parsed.monthYear.isNotBlank() && parsedMonthNorm != currentMonthNorm))

        if (isDifferentFarmerOrMonth) {
            persistJob?.cancel()
            viewModelScope.launch {
                val newId = withContext(Dispatchers.IO) {
                    repository.createAndActivateLedger(
                        farmerName = resolvedFarmer,
                        monthYear = resolvedMonth,
                        transactions = parsed.transactions,
                        rawBulkText = raw,
                    )
                }
                val isEng = _uiState.value.billLanguage == BillLanguage.ENGLISH
                _uiState.update { state ->
                    state.copy(
                        activeLedgerId = newId,
                        farmerName = resolvedFarmer,
                        monthYear = resolvedMonth,
                        transactions = parsed.transactions,
                        totals = newTotals,
                        parseStatus = ParseStatus(
                            true,
                            if (isEng) {
                                "Saved as new farmer bill (${parsed.transactions.size} entries) · Monthly chart updated!"
                            } else {
                                "नवीन शेतकरी बिल फोनवर सेव्ह झाले (${parsed.transactions.size} व्यवहार) · मासिक विश्लेषण अपडेट झाले!"
                            },
                        ),
                        parsedSummary = ParsedSummary(
                            farmerName = resolvedFarmer,
                            monthYear = resolvedMonth,
                            transactionCount = parsed.transactions.size,
                            totalBags = totalBags,
                        ),
                        snackbarMessage = if (isEng) {
                            "New bill saved to phone: $resolvedFarmer"
                        } else {
                            "नवीन शेतकरी बिल जतन झाले: $resolvedFarmer"
                        },
                    )
                }
            }
        } else {
            _uiState.update { state ->
                state.copy(
                    farmerName = resolvedFarmer,
                    monthYear = resolvedMonth,
                    transactions = parsed.transactions,
                    totals = newTotals,
                    parseStatus = ParseStatus(
                        true,
                        "${parsed.transactions.size} व्यवहार यशस्वीपणे पार्स झाले व फोनवर जतन केले!",
                    ),
                    parsedSummary = ParsedSummary(
                        farmerName = resolvedFarmer,
                        monthYear = resolvedMonth,
                        transactionCount = parsed.transactions.size,
                        totalBags = totalBags,
                    ),
                )
            }
            schedulePersist()
        }
    }

    fun loadFullAugustSample() {
        val sampleTransactions = DefaultLedgerData.defaultTransactions
        _uiState.update { state ->
            state.copy(
                farmerName = DefaultLedgerData.DEFAULT_FARMER_NAME,
                monthYear = DefaultLedgerData.DEFAULT_MONTH_YEAR,
                transactions = sampleTransactions,
                totals = DevanagariFormatter.calculateTotals(sampleTransactions),
                parseStatus = null,
                parsedSummary = null,
                snackbarMessage = "ऑगस्ट २०२६ सॅम्पल बिल लोड केले",
            )
        }
        schedulePersist()
    }

    fun clearAllData() {
        _uiState.update { state ->
            state.copy(
                farmerName = "",
                monthYear = "",
                rawBulkText = "",
                transactions = emptyList(),
                totals = DevanagariFormatter.calculateTotals(emptyList()),
                parseStatus = null,
                parsedSummary = null,
                snackbarMessage = "सर्व डेटा साफ केला",
            )
        }
        schedulePersist()
    }

    fun addTransaction() {
        val newTx = LedgerTransaction(
            id = System.nanoTime(),
            date = "",
            items = listOf(FlowerLotItem(id = System.nanoTime() + 1)),
            expenses = TransactionExpenses(),
        )
        val updated = _uiState.value.transactions + newTx
        updateTransactionsList(updated)
    }

    fun updateTransaction(updatedTx: LedgerTransaction) {
        val updated = _uiState.value.transactions.map { tx ->
            if (tx.id == updatedTx.id) updatedTx else tx
        }
        updateTransactionsList(updated)
    }

    fun deleteTransaction(txId: Long) {
        val updated = _uiState.value.transactions.filterNot { it.id == txId }
        updateTransactionsList(updated)
    }

    private fun updateTransactionsList(updated: List<LedgerTransaction>) {
        _uiState.update { state ->
            state.copy(
                transactions = updated,
                totals = DevanagariFormatter.calculateTotals(updated),
            )
        }
        schedulePersist()
    }

    fun setPreviewScale(scale: Float) {
        _uiState.update { it.copy(previewScale = scale.coerceIn(0.45f, 1.5f)) }
    }

    fun setBillLanguage(language: BillLanguage) {
        if (_uiState.value.billLanguage == language) return
        _uiState.update {
            it.copy(
                billLanguage = language,
                snackbarMessage = if (language == BillLanguage.ENGLISH) {
                    "Bill converted to English"
                } else {
                    "बिल मराठीमध्ये रूपांतरित झाले"
                },
            )
        }
    }

    fun toggleBillLanguage() {
        val next = if (_uiState.value.billLanguage == BillLanguage.MARATHI) {
            BillLanguage.ENGLISH
        } else {
            BillLanguage.MARATHI
        }
        setBillLanguage(next)
    }

    fun setAnalyticsScope(scope: AnalyticsScope) {
        _uiState.update { it.copy(analyticsScope = scope) }
    }

    fun saveCurrentBillToPhone() {
        persistJob?.cancel()
        viewModelScope.launch {
            val state = _uiState.value
            val farmer = state.farmerName.ifBlank { "नवीन शेतकरी" }
            val month = state.monthYear.ifBlank { "नवीन महिना" }
            val savedId = withContext(Dispatchers.IO) {
                repository.saveActiveLedger(
                    id = state.activeLedgerId,
                    farmerName = farmer,
                    monthYear = month,
                    transactions = state.transactions,
                    rawBulkText = state.rawBulkText,
                )
            }
            val isEng = state.billLanguage == BillLanguage.ENGLISH
            _uiState.update {
                it.copy(
                    activeLedgerId = savedId,
                    farmerName = farmer,
                    monthYear = month,
                    snackbarMessage = if (isEng) {
                        "Saved to phone storage & charts updated: $farmer"
                    } else {
                        "फोनवर जतन झाले व मासिक विश्लेषण अपडेट झाले: $farmer"
                    },
                )
            }
        }
    }

    fun saveAsNewBill(customName: String? = null) {
        persistJob?.cancel()
        viewModelScope.launch {
            val state = _uiState.value
            val farmer = customName?.takeIf { it.isNotBlank() } ?: state.farmerName.ifBlank { "नवीन शेतकरी" }
            val month = state.monthYear.ifBlank { "नवीन महिना" }
            val newId = withContext(Dispatchers.IO) {
                repository.createAndActivateLedger(
                    farmerName = farmer,
                    monthYear = month,
                    transactions = state.transactions,
                    rawBulkText = state.rawBulkText,
                )
            }
            val isEng = state.billLanguage == BillLanguage.ENGLISH
            _uiState.update {
                it.copy(
                    activeLedgerId = newId,
                    farmerName = farmer,
                    monthYear = month,
                    snackbarMessage = if (isEng) {
                        "Saved as separate farmer bill: $farmer"
                    } else {
                        "नवीन शेतकरी बिल जतन केले: $farmer"
                    },
                )
            }
        }
    }

    fun startBlankBill() {
        persistJob?.cancel()
        viewModelScope.launch {
            val newId = withContext(Dispatchers.IO) {
                repository.createAndActivateLedger(
                    farmerName = "",
                    monthYear = "",
                    transactions = emptyList(),
                    rawBulkText = "",
                )
            }
            val isEng = _uiState.value.billLanguage == BillLanguage.ENGLISH
            _uiState.update {
                it.copy(
                    activeLedgerId = newId,
                    farmerName = "",
                    monthYear = "",
                    rawBulkText = "",
                    transactions = emptyList(),
                    totals = DevanagariFormatter.calculateTotals(emptyList()),
                    parseStatus = null,
                    parsedSummary = null,
                    snackbarMessage = if (isEng) {
                        "Started new blank farmer bill (previous bills preserved)"
                    } else {
                        "नवीन शेतकऱ्याचे रिकामे बिल तयार केले (जुनी बिले सुरक्षित आहेत)"
                    },
                )
            }
        }
    }

    fun switchLedger(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.activateLedger(id)
            }
            _uiState.update {
                it.copy(
                    parseStatus = null,
                    parsedSummary = null,
                    snackbarMessage = "जतन केलेले बिल लोड झाले",
                )
            }
        }
    }

    fun deleteSavedLedger(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteLedger(id)
            }
            _uiState.update { it.copy(snackbarMessage = "बिल हटवले") }
        }
    }

    fun importJson(jsonText: String) {
        val payload = repository.importFromJson(jsonText)
        if (payload == null) {
            _uiState.update { it.copy(snackbarMessage = "JSON फॉरमॅट अवैध आहे") }
            return
        }
        _uiState.update { state ->
            state.copy(
                farmerName = payload.farmerName,
                monthYear = payload.monthYear,
                transactions = payload.transactions,
                totals = DevanagariFormatter.calculateTotals(payload.transactions),
                parseStatus = ParseStatus(true, "JSON मधून ${payload.transactions.size} व्यवहार लोड झाले!"),
                snackbarMessage = "JSON डेटा आयात केला",
            )
        }
        schedulePersist()
    }

    fun exportJson(): String {
        val state = _uiState.value
        return repository.exportToJson(state.farmerName, state.monthYear, state.transactions)
    }

    fun suggestedPdfFileName(): String {
        val state = _uiState.value
        val localizedFarmer = if (state.farmerName.isBlank()) "" else {
            BillLanguageConverter.formatFarmerName(state.farmerName, state.billLanguage)
        }
        val localizedMonth = if (state.monthYear.isBlank()) "" else {
            BillLanguageConverter.formatMonthYear(state.monthYear, state.billLanguage)
        }
        return DevanagariFormatter.buildPdfFileName(localizedFarmer, localizedMonth)
    }

    fun suggestedJsonFileName(): String {
        return suggestedPdfFileName().removeSuffix(".pdf") + ".json"
    }

    fun savePdfToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val state = _uiState.value
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        LedgerPdfGenerator.writePdfToStream(
                            context = context,
                            outputStream = out,
                            farmerName = state.farmerName,
                            monthYear = state.monthYear,
                            transactions = state.transactions,
                            totals = state.totals,
                            language = state.billLanguage,
                        )
                    } ?: error("Output stream unavailable")
                }
            }
            _uiState.update {
                it.copy(
                    isGeneratingPdf = false,
                    snackbarMessage = if (result.isSuccess) {
                        "PDF यशस्वीरित्या सेव्ह झाली: ${suggestedPdfFileName()}"
                    } else {
                        "PDF सेव्ह करताना त्रुटी आली"
                    },
                )
            }
        }
    }

    fun sharePdf(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }
            val fileResult = withContext(Dispatchers.IO) {
                runCatching {
                    val state = _uiState.value
                    LedgerPdfGenerator.createCachePdfFile(
                        context = context,
                        farmerName = state.farmerName,
                        monthYear = state.monthYear,
                        transactions = state.transactions,
                        totals = state.totals,
                        language = state.billLanguage,
                    )
                }
            }
            _uiState.update { it.copy(isGeneratingPdf = false) }
            fileResult.onSuccess { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, "PDF शेअर करा")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure {
                _uiState.update {
                    it.copy(snackbarMessage = "PDF शेअर करण्यात अडचण आली")
                }
            }
        }
    }

    fun printPdf(context: Context) {
        val state = _uiState.value
        LedgerPdfGenerator.printLedger(
            context = context,
            farmerName = state.farmerName,
            monthYear = state.monthYear,
            transactions = state.transactions,
            totals = state.totals,
            language = state.billLanguage,
        )
    }

    fun exportJsonToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(exportJson().toByteArray(Charsets.UTF_8))
                    } ?: error("Cannot open stream")
                }.isSuccess
            }
            _uiState.update {
                it.copy(
                    snackbarMessage = if (ok) "JSON बॅकअप सेव्ह झाला" else "JSON सेव्ह करण्यात त्रुटी आली"
                )
            }
        }
    }

    fun importJsonFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
            }
            if (json != null) {
                importJson(json)
            } else {
                _uiState.update { it.copy(snackbarMessage = "फाइल वाचण्यात अडचण आली") }
            }
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun schedulePersist() {
        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(350)
            val state = _uiState.value
            val savedId = withContext(Dispatchers.IO) {
                repository.saveActiveLedger(
                    id = state.activeLedgerId,
                    farmerName = state.farmerName,
                    monthYear = state.monthYear,
                    transactions = state.transactions,
                    rawBulkText = state.rawBulkText,
                )
            }
            if (state.activeLedgerId == null) {
                _uiState.update { it.copy(activeLedgerId = savedId) }
            }
        }
    }
}
