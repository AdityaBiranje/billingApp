package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.AnalyticsScope
import com.example.domain.model.BillLanguage
import com.example.ui.components.LedgerSearchBar
import com.example.ui.components.SavedLedgersSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BulkInputScreen
import com.example.ui.screens.EntriesScreen
import com.example.ui.screens.LedgerPreviewScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgCardElevated
import com.example.ui.theme.BgPrimary
import com.example.ui.theme.BgSecondary
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerBillingApp(
    viewModel: LedgerViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showSavedSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            viewModel.savePdfToUri(context, uri)
        }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            viewModel.exportJsonToUri(context, uri)
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.importJsonFromUri(context, uri)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    Scaffold(
        containerColor = BgPrimary,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgSecondary,
                    titleContentColor = TextPrimary,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    brush = Brush.linearGradient(listOf(AccentBlue, AccentViolet)),
                                    shape = RoundedCornerShape(11.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalFlorist,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(21.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (uiState.billLanguage == BillLanguage.ENGLISH) {
                                    "Ashok Flower Stall"
                                } else {
                                    "अशोक फ्लॉवर स्टॉल"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentMint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (uiState.billLanguage == BillLanguage.ENGLISH) {
                                    "Farmer Ledger & Analytics"
                                } else {
                                    "शेतकरी खातेवही व विश्लेषण"
                                },
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = viewModel::toggleBillLanguage,
                        shape = RoundedCornerShape(10.dp),
                        color = if (uiState.billLanguage == BillLanguage.ENGLISH) {
                            AccentBlue.copy(alpha = 0.2f)
                        } else {
                            AccentEmerald.copy(alpha = 0.18f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (uiState.billLanguage == BillLanguage.ENGLISH) AccentBlue else AccentEmerald,
                        ),
                        modifier = Modifier.testTag("topbar_language_toggle"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Translate,
                                contentDescription = "बिल भाषा बदला",
                                tint = if (uiState.billLanguage == BillLanguage.ENGLISH) AccentBlue else AccentMint,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.billLanguage == BillLanguage.ENGLISH) "ENG" else "मराठी",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.billLanguage == BillLanguage.ENGLISH) AccentBlue else AccentMint,
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSavedSheet = true },
                        modifier = Modifier.testTag("saved_bills_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = "जतन केलेली बिले",
                            tint = AccentMint,
                        )
                    }

                    IconButton(
                        onClick = { savePdfLauncher.launch(viewModel.suggestedPdfFileName()) },
                        modifier = Modifier.testTag("topbar_save_pdf_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PictureAsPdf,
                            contentDescription = "PDF डाउनलोड करा",
                            tint = AccentBlue,
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.testTag("more_options_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "इतर पर्याय",
                                tint = TextPrimary,
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            containerColor = BgCard,
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (uiState.billLanguage == BillLanguage.MARATHI) {
                                            "बिल English मध्ये बदला"
                                        } else {
                                            "बिल मराठीमध्ये बदला"
                                        },
                                        color = TextPrimary,
                                    )
                                },
                                leadingIcon = { Icon(Icons.Outlined.Translate, contentDescription = null, tint = AccentMint) },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.toggleBillLanguage()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("JSON फाइल आयात करा", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.FileUpload, contentDescription = null, tint = AccentBlue) },
                                onClick = {
                                    showMoreMenu = false
                                    importJsonLauncher.launch(arrayOf("application/json", "text/*"))
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("JSON बॅकअप सेव्ह करा", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = AccentEmerald) },
                                onClick = {
                                    showMoreMenu = false
                                    exportJsonLauncher.launch(viewModel.suggestedJsonFileName())
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("ऑगस्ट २०२६ सॅम्पल लोड करा", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.Restore, contentDescription = null, tint = AccentGold) },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.loadFullAugustSample()
                                },
                            )
                            HorizontalDivider(color = BorderSubtle)
                            DropdownMenuItem(
                                text = { Text("सर्व डेटा साफ करा", color = AccentRose) },
                                leadingIcon = { Icon(Icons.Outlined.DeleteSweep, contentDescription = null, tint = AccentRose) },
                                onClick = {
                                    showMoreMenu = false
                                    showClearConfirm = true
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            BoxWithConstraints {
                if (maxWidth < 840.dp) {
                    val isEng = uiState.billLanguage == BillLanguage.ENGLISH
                    NavigationBar(
                        containerColor = BgSecondary,
                        contentColor = TextPrimary,
                    ) {
                        NavigationBarItem(
                            selected = uiState.activeTab == DashboardTab.BULK_PASTE,
                            onClick = { viewModel.selectTab(DashboardTab.BULK_PASTE) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.activeTab == DashboardTab.BULK_PASTE) Icons.Filled.ContentPaste else Icons.Outlined.ContentPaste,
                                    contentDescription = "पेस्ट करा",
                                )
                            },
                            label = { Text(if (isEng) "Paste" else "पेस्ट", fontSize = 11.sp) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_paste"),
                        )
                        NavigationBarItem(
                            selected = uiState.activeTab == DashboardTab.ENTRIES,
                            onClick = { viewModel.selectTab(DashboardTab.ENTRIES) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.activeTab == DashboardTab.ENTRIES) Icons.Filled.EditNote else Icons.Outlined.EditNote,
                                    contentDescription = "व्यवहार",
                                )
                            },
                            label = { Text(if (isEng) "Entries (${uiState.transactions.size})" else "व्यवहार (${uiState.transactions.size})", fontSize = 11.sp, maxLines = 1) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_entries"),
                        )
                        NavigationBarItem(
                            selected = uiState.activeTab == DashboardTab.ANALYTICS,
                            onClick = { viewModel.selectTab(DashboardTab.ANALYTICS) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.activeTab == DashboardTab.ANALYTICS) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                                    contentDescription = "विश्लेषण",
                                )
                            },
                            label = { Text(if (isEng) "Charts" else "विश्लेषण", fontSize = 11.sp) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_analytics"),
                        )
                        NavigationBarItem(
                            selected = uiState.activeTab == DashboardTab.PREVIEW,
                            onClick = { viewModel.selectTab(DashboardTab.PREVIEW) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.activeTab == DashboardTab.PREVIEW) Icons.Filled.Description else Icons.Outlined.Description,
                                    contentDescription = "बिल प्रिव्ह्यू",
                                )
                            },
                            label = { Text(if (isEng) "Bill" else "बिल", fontSize = 11.sp) },
                            colors = navBarColors(),
                            modifier = Modifier.testTag("nav_tab_preview"),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val isWideScreen = maxWidth >= 840.dp

            if (isWideScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(0.43f)
                            .fillMaxHeight()
                            .background(BgSecondary),
                    ) {
                        LedgerSearchBar(
                            query = uiState.searchQuery,
                            language = uiState.billLanguage,
                            matchingLedgers = uiState.filteredStoredLedgers,
                            matchingTransactionCount = uiState.filteredTransactions.size,
                            onQueryChange = viewModel::updateSearchQuery,
                            onClearQuery = viewModel::clearSearchQuery,
                            onSelectFarmer = { id ->
                                viewModel.switchLedger(id)
                            },
                        )

                        val leftTab = if (uiState.activeTab == DashboardTab.PREVIEW) DashboardTab.BULK_PASTE else uiState.activeTab
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            SegmentedButton(
                                selected = leftTab == DashboardTab.BULK_PASTE,
                                onClick = { viewModel.selectTab(DashboardTab.BULK_PASTE) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                colors = segmentedTabColors(),
                            ) {
                                Text("Paste", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            SegmentedButton(
                                selected = leftTab == DashboardTab.ENTRIES,
                                onClick = { viewModel.selectTab(DashboardTab.ENTRIES) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                colors = segmentedTabColors(),
                            ) {
                                Text("व्यवहार (${uiState.transactions.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            SegmentedButton(
                                selected = leftTab == DashboardTab.ANALYTICS,
                                onClick = { viewModel.selectTab(DashboardTab.ANALYTICS) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                colors = segmentedTabColors(),
                            ) {
                                Text("Charts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        when (leftTab) {
                            DashboardTab.ENTRIES -> EntriesScreen(
                                uiState = uiState,
                                onFarmerNameChange = viewModel::updateFarmerName,
                                onMonthYearChange = viewModel::updateMonthYear,
                                onAddTransaction = viewModel::addTransaction,
                                onUpdateTransaction = viewModel::updateTransaction,
                                onDeleteTransaction = viewModel::deleteTransaction,
                                onStartNewFarmerBill = viewModel::startBlankBill,
                                onSaveFarmerBill = viewModel::saveCurrentBillToPhone,
                                onLoadSampleBill = viewModel::loadFullAugustSample,
                                onClearSearch = viewModel::clearSearchQuery,
                            )
                            DashboardTab.ANALYTICS -> AnalyticsScreen(
                                uiState = uiState,
                                onSelectScope = viewModel::setAnalyticsScope,
                                onSelectStoredFarmer = { id ->
                                    viewModel.switchLedger(id)
                                    viewModel.setAnalyticsScope(AnalyticsScope.CURRENT_FARMER)
                                },
                                onStartNewFarmerBill = {
                                    viewModel.startBlankBill()
                                    viewModel.selectTab(DashboardTab.BULK_PASTE)
                                },
                                onOpenSavedBills = { showSavedSheet = true },
                                onLoadSampleBill = viewModel::loadFullAugustSample,
                                onClearSearch = viewModel::clearSearchQuery,
                            )
                            else -> BulkInputScreen(
                                uiState = uiState,
                                onFarmerNameChange = viewModel::updateFarmerName,
                                onMonthYearChange = viewModel::updateMonthYear,
                                onRawTextChange = viewModel::updateRawBulkText,
                                onParseClick = viewModel::parseBulkText,
                                onLoadQuickSample = viewModel::loadQuickSample,
                                onClearText = viewModel::clearRawText,
                                onOpenPreview = { viewModel.selectTab(DashboardTab.PREVIEW) },
                                onSavePdf = { savePdfLauncher.launch(viewModel.suggestedPdfFileName()) },
                                onStartNewFarmerBill = viewModel::startBlankBill,
                                onSaveFarmerBill = viewModel::saveCurrentBillToPhone,
                                onOpenSavedBills = { showSavedSheet = true },
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(BorderSubtle),
                    )

                    LedgerPreviewScreen(
                        uiState = uiState,
                        onScaleChange = viewModel::setPreviewScale,
                        onLanguageChange = viewModel::setBillLanguage,
                        onSavePdf = { savePdfLauncher.launch(viewModel.suggestedPdfFileName()) },
                        onSharePdf = { viewModel.sharePdf(context) },
                        onPrintPdf = { viewModel.printPdf(context) },
                        modifier = Modifier
                            .weight(0.57f)
                            .fillMaxHeight(),
                    )
                }
            } else {
                // Phone / Compact Layout with Horizontally Scrollable Quick-Jump Section Bar + Bottom Nav
                BackHandler(enabled = uiState.activeTab != DashboardTab.BULK_PASTE) {
                    viewModel.selectTab(DashboardTab.BULK_PASTE)
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalSectionStrip(
                        activeTab = uiState.activeTab,
                        transactionCount = uiState.transactions.size,
                        language = uiState.billLanguage,
                        onSelectTab = viewModel::selectTab,
                    )

                    LedgerSearchBar(
                        query = uiState.searchQuery,
                        language = uiState.billLanguage,
                        matchingLedgers = uiState.filteredStoredLedgers,
                        matchingTransactionCount = uiState.filteredTransactions.size,
                        onQueryChange = viewModel::updateSearchQuery,
                        onClearQuery = viewModel::clearSearchQuery,
                        onSelectFarmer = { id ->
                            viewModel.switchLedger(id)
                        },
                    )

                    AnimatedContent(
                        targetState = uiState.activeTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier.weight(1f),
                        label = "tabContent",
                    ) { tab ->
                        when (tab) {
                            DashboardTab.BULK_PASTE -> BulkInputScreen(
                                uiState = uiState,
                                onFarmerNameChange = viewModel::updateFarmerName,
                                onMonthYearChange = viewModel::updateMonthYear,
                                onRawTextChange = viewModel::updateRawBulkText,
                                onParseClick = viewModel::parseBulkText,
                                onLoadQuickSample = viewModel::loadQuickSample,
                                onClearText = viewModel::clearRawText,
                                onOpenPreview = { viewModel.selectTab(DashboardTab.PREVIEW) },
                                onSavePdf = { savePdfLauncher.launch(viewModel.suggestedPdfFileName()) },
                                onStartNewFarmerBill = viewModel::startBlankBill,
                                onSaveFarmerBill = viewModel::saveCurrentBillToPhone,
                                onOpenSavedBills = { showSavedSheet = true },
                            )
                            DashboardTab.ENTRIES -> EntriesScreen(
                                uiState = uiState,
                                onFarmerNameChange = viewModel::updateFarmerName,
                                onMonthYearChange = viewModel::updateMonthYear,
                                onAddTransaction = viewModel::addTransaction,
                                onUpdateTransaction = viewModel::updateTransaction,
                                onDeleteTransaction = viewModel::deleteTransaction,
                                onStartNewFarmerBill = viewModel::startBlankBill,
                                onSaveFarmerBill = viewModel::saveCurrentBillToPhone,
                                onLoadSampleBill = viewModel::loadFullAugustSample,
                                onClearSearch = viewModel::clearSearchQuery,
                            )
                            DashboardTab.ANALYTICS -> AnalyticsScreen(
                                uiState = uiState,
                                onSelectScope = viewModel::setAnalyticsScope,
                                onSelectStoredFarmer = { id ->
                                    viewModel.switchLedger(id)
                                    viewModel.setAnalyticsScope(AnalyticsScope.CURRENT_FARMER)
                                },
                                onStartNewFarmerBill = {
                                    viewModel.startBlankBill()
                                    viewModel.selectTab(DashboardTab.BULK_PASTE)
                                },
                                onOpenSavedBills = { showSavedSheet = true },
                                onLoadSampleBill = viewModel::loadFullAugustSample,
                                onClearSearch = viewModel::clearSearchQuery,
                            )
                            DashboardTab.PREVIEW -> LedgerPreviewScreen(
                                uiState = uiState,
                                onScaleChange = viewModel::setPreviewScale,
                                onLanguageChange = viewModel::setBillLanguage,
                                onSavePdf = { savePdfLauncher.launch(viewModel.suggestedPdfFileName()) },
                                onSharePdf = { viewModel.sharePdf(context) },
                                onPrintPdf = { viewModel.printPdf(context) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSavedSheet) {
        SavedLedgersSheet(
            savedLedgers = uiState.savedLedgers,
            onSelectLedger = viewModel::switchLedger,
            onDeleteLedger = viewModel::deleteSavedLedger,
            onSaveCurrentAsNew = { viewModel.saveAsNewBill() },
            onStartBlankBill = viewModel::startBlankBill,
            onLoadSampleBill = viewModel::loadFullAugustSample,
            onDismiss = { showSavedSheet = false },
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = BgCard,
            title = { Text("सर्व डेटा साफ करायचा?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("सध्याचे शेतकऱ्याचे नाव, महिना आणि सर्व व्यवहार रिकामे केले जातील.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        viewModel.clearAllData()
                    },
                ) {
                    Text("हो, साफ करा", color = AccentRose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("रद्द करा", color = TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun HorizontalSectionStrip(
    activeTab: DashboardTab,
    transactionCount: Int,
    language: BillLanguage,
    onSelectTab: (DashboardTab) -> Unit,
) {
    val isEng = language == BillLanguage.ENGLISH
    Surface(
        color = BgSecondary,
        border = BorderStroke(1.dp, BorderSubtle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionPill(
                label = if (isEng) "WhatsApp Paste" else "WhatsApp पेस्ट",
                icon = Icons.Outlined.ContentPaste,
                selected = activeTab == DashboardTab.BULK_PASTE,
                onClick = { onSelectTab(DashboardTab.BULK_PASTE) },
            )
            SectionPill(
                label = if (isEng) "Entries ($transactionCount)" else "व्यवहार ($transactionCount)",
                icon = Icons.Outlined.EditNote,
                selected = activeTab == DashboardTab.ENTRIES,
                onClick = { onSelectTab(DashboardTab.ENTRIES) },
            )
            SectionPill(
                label = if (isEng) "Monthly Charts" else "मासिक विश्लेषण",
                icon = Icons.Outlined.Analytics,
                selected = activeTab == DashboardTab.ANALYTICS,
                onClick = { onSelectTab(DashboardTab.ANALYTICS) },
            )
            SectionPill(
                label = if (isEng) "A4 Bill & PDF" else "A4 बिल व PDF",
                icon = Icons.Outlined.Description,
                selected = activeTab == DashboardTab.PREVIEW,
                onClick = { onSelectTab(DashboardTab.PREVIEW) },
            )
        }
    }
}

@Composable
private fun SectionPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) AccentBlue.copy(alpha = 0.22f) else BgCardElevated,
        border = BorderStroke(
            1.dp,
            if (selected) AccentBlue else BorderHighlight.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) AccentBlue else TextSecondary,
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) TextPrimary else TextSecondary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun segmentedTabColors() = SegmentedButtonDefaults.colors(
    activeContainerColor = AccentBlue.copy(alpha = 0.22f),
    activeContentColor = AccentBlue,
    inactiveContainerColor = BgCard,
    inactiveContentColor = TextSecondary,
)

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AccentBlue,
    selectedTextColor = AccentBlue,
    indicatorColor = AccentBlue.copy(alpha = 0.16f),
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
)
