package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AnalyticsScope
import com.example.domain.model.BillLanguage
import com.example.domain.model.LedgerTotals
import com.example.domain.model.MonthlyAggregateSummary
import com.example.domain.util.BillLanguageConverter
import com.example.domain.util.DevanagariFormatter
import com.example.domain.util.MonthlyLedgerAggregator
import com.example.ui.LedgerUiState
import com.example.ui.components.DailySalesExpensesBarLineChart
import com.example.ui.components.MonthlyAggregateComparisonChart
import com.example.ui.components.RateTrendAreaChart
import com.example.ui.components.RevenueExpenseDonutChart
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgCardElevated
import com.example.ui.theme.BgInput
import com.example.ui.theme.BgPrimary
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    uiState: LedgerUiState,
    onSelectScope: (AnalyticsScope) -> Unit,
    onSelectStoredFarmer: (Long) -> Unit,
    onStartNewFarmerBill: () -> Unit,
    onOpenSavedBills: () -> Unit,
    onLoadSampleBill: () -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnglish = uiState.billLanguage == BillLanguage.ENGLISH
    val isAllFarmers = uiState.analyticsScope == AnalyticsScope.ALL_FARMERS_MONTHLY
    val isFiltering = uiState.searchQuery.isNotBlank()
    val displayStoredLedgers = uiState.filteredStoredLedgers
    val displayAggregates = uiState.filteredMonthlyAggregates
    val storedCount = displayStoredLedgers.size
    val totalStoredCount = uiState.storedLedgers.count {
        it.transactionCount > 0 || it.totals.totalSaleAmount > 0.0 || it.farmerName.isNotBlank()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Local Phone Storage Status + Quick Actions
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = BgCardElevated,
                border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.45f)),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentEmerald.copy(alpha = 0.16f),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Storage,
                                    contentDescription = if (isEnglish) "Local Phone Storage" else "फोन लोकल स्टोरेज",
                                    tint = AccentMint,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEnglish) {
                                        "$totalStoredCount Farmer Bills Saved Offline"
                                    } else {
                                        "फोनवर ${DevanagariFormatter.toDevanagari(totalStoredCount.toString())} शेतकरी बिले जतन"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                )
                                Text(
                                    text = if (isEnglish) {
                                        "Every new farmer bill automatically updates monthly charts"
                                    } else {
                                        "प्रत्येक नवीन शेतकरी बिल जोडल्यावर मासिक आलेख आपोआप अपडेट होतात"
                                    },
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            onClick = onStartNewFarmerBill,
                            shape = RoundedCornerShape(10.dp),
                            color = AccentBlue.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, AccentBlue),
                            modifier = Modifier.testTag("analytics_new_farmer_button"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PersonAddAlt1,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) "+ New Farmer Bill" else "+ नवीन शेतकरी बिल",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue,
                                )
                            }
                        }

                        Surface(
                            onClick = onOpenSavedBills,
                            shape = RoundedCornerShape(10.dp),
                            color = BgInput,
                            border = BorderStroke(1.dp, BorderHighlight),
                            modifier = Modifier.testTag("analytics_open_saved_button"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FolderOpen,
                                    contentDescription = null,
                                    tint = AccentMint,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) "Switch / Manage Bills" else "जतन केलेली बिले पहा",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentMint,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scope Toggle: All Farmers Monthly vs Current Farmer Daily
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_scope_toggle"),
            ) {
                SegmentedButton(
                    selected = isAllFarmers,
                    onClick = { onSelectScope(AnalyticsScope.ALL_FARMERS_MONTHLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = AccentBlue.copy(alpha = 0.24f),
                        activeContentColor = AccentBlue,
                        inactiveContainerColor = BgCard,
                        inactiveContentColor = TextSecondary,
                    ),
                ) {
                    Text(
                        text = if (isEnglish) "All Farmers (Monthly)" else "सर्व शेतकरी (मासिक)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                SegmentedButton(
                    selected = !isAllFarmers,
                    onClick = { onSelectScope(AnalyticsScope.CURRENT_FARMER) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = AccentEmerald.copy(alpha = 0.24f),
                        activeContentColor = AccentMint,
                        inactiveContainerColor = BgCard,
                        inactiveContentColor = TextSecondary,
                    ),
                ) {
                    Text(
                        text = if (isEnglish) "This Bill (Daily)" else "सध्याचे बिल (दैनिक)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }

        if (isAllFarmers) {
            val combinedTotals = if (isFiltering) {
                MonthlyLedgerAggregator.combineTotals(displayStoredLedgers)
            } else {
                uiState.allFarmersTotals
            }
            item {
                HeroLedgerBanner(
                    title = if (isFiltering) {
                        if (isEnglish) "Filtered Farmers (${displayStoredLedgers.size})" else "फिल्टर केलेले शेतकरी (${DevanagariFormatter.toDevanagari(displayStoredLedgers.size.toString())})"
                    } else if (isEnglish) {
                        "All Stored Farmers"
                    } else {
                        "सर्व जतन केलेले शेतकरी"
                    },
                    subtitle = if (isEnglish) {
                        "${displayAggregates.size} Months · $storedCount Saved Bills"
                    } else {
                        "${DevanagariFormatter.toDevanagari(displayAggregates.size.toString())} महिने · ${DevanagariFormatter.toDevanagari(storedCount.toString())} बिले"
                    },
                    totals = combinedTotals,
                    avgLabel = if (isEnglish) "Commission" else "एकूण कमिशन",
                    avgValue = "₹${BillLanguageConverter.formatNumber(combinedTotals.totalCommission, uiState.billLanguage)}",
                    language = uiState.billLanguage,
                )
            }

            if (displayAggregates.isEmpty()) {
                item {
                    if (isFiltering) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = BgCard,
                            border = BorderStroke(1.dp, BorderSubtle),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = if (isEnglish) {
                                        "No farmers or dates matching \"${uiState.searchQuery}\""
                                    } else {
                                        "\"${uiState.searchQuery}\" साठी कोणताही शेतकरी किंवा तारीख सापडली नाही"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = onClearSearch,
                                    border = BorderStroke(1.dp, AccentBlue),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text(
                                        text = if (isEnglish) "Clear Search" else "शोध साफ करा",
                                        color = AccentBlue,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    } else {
                        EmptyAnalyticsCard(
                            isEnglish = isEnglish,
                            onLoadSampleBill = onLoadSampleBill,
                        )
                    }
                }
            } else {
                item {
                    MonthlyAggregateComparisonChart(
                        monthlyAggregates = displayAggregates,
                        language = uiState.billLanguage,
                    )
                }

                item {
                    RevenueExpenseDonutChart(
                        totals = combinedTotals,
                        language = uiState.billLanguage,
                    )
                }

                item {
                    Text(
                        text = if (isEnglish) {
                            "Monthly Breakdown & Stored Farmer Bills"
                        } else {
                            "महिनानिहाय एकूण व शेतकरी यादी"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }

                items(
                    items = displayAggregates,
                    key = { it.monthKey },
                ) { monthSummary ->
                    MonthlyBreakdownCard(
                        monthSummary = monthSummary,
                        language = uiState.billLanguage,
                        onSelectFarmer = onSelectStoredFarmer,
                    )
                }
            }
        } else {
            val activeTransactions = uiState.filteredTransactions
            val totals = if (isFiltering) {
                DevanagariFormatter.calculateTotals(activeTransactions)
            } else {
                uiState.totals
            }
            val avgDailyNet = if (activeTransactions.isNotEmpty()) {
                totals.grandBalance / activeTransactions.size
            } else {
                0.0
            }

            item {
                HeroLedgerBanner(
                    title = BillLanguageConverter.formatFarmerName(uiState.farmerName, uiState.billLanguage),
                    subtitle = BillLanguageConverter.formatMonthYear(uiState.monthYear, uiState.billLanguage),
                    totals = totals,
                    avgLabel = if (isEnglish) "Avg / Day" else "दैनिक सरासरी",
                    avgValue = "₹${BillLanguageConverter.formatNumber(avgDailyNet, uiState.billLanguage)}",
                    language = uiState.billLanguage,
                )
            }

            if (activeTransactions.isEmpty()) {
                item {
                    EmptyAnalyticsCard(
                        isEnglish = isEnglish,
                        onLoadSampleBill = onLoadSampleBill,
                    )
                }
            } else {
                item {
                    DailySalesExpensesBarLineChart(
                        transactions = activeTransactions,
                        language = uiState.billLanguage,
                    )
                }

                item {
                    RevenueExpenseDonutChart(
                        totals = totals,
                        language = uiState.billLanguage,
                    )
                }

                item {
                    RateTrendAreaChart(
                        transactions = activeTransactions,
                        language = uiState.billLanguage,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroLedgerBanner(
    title: String,
    subtitle: String,
    totals: LedgerTotals,
    avgLabel: String,
    avgValue: String,
    language: BillLanguage,
) {
    val isEnglish = language == BillLanguage.ENGLISH
    val retentionPct = if (totals.totalSaleAmount > 0) {
        ((totals.grandBalance / totals.totalSaleAmount) * 100.0).roundToInt()
    } else {
        0
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = BgCard,
        border = BorderStroke(1.2.dp, BorderHighlight),
        shadowElevation = 6.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_mandi_banner),
                contentDescription = if (isEnglish) "Monthly Ledger Hero Illustration" else "मासिक खातेवही सारांश चित्र",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp)),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BgPrimary.copy(alpha = 0.72f),
                                BgPrimary.copy(alpha = 0.92f),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = AccentAmber,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AccentEmerald.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AccentEmerald),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = AccentMint,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEnglish) "$retentionPct% Net" else "$retentionPct% निव्वळ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentMint,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isEnglish) "NET FARMER PAYOUT" else "शेतकरी एकूण निव्वळ शिल्लक",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary,
                )
                Text(
                    text = "₹${BillLanguageConverter.formatNumber(totals.grandBalance, language)}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentMint,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeroKpiPill(
                        label = if (isEnglish) "Gross Sale" else "एकूण विक्री",
                        value = "₹${BillLanguageConverter.formatNumber(totals.totalSaleAmount, language)}",
                        accent = AccentBlue,
                    )
                    HeroKpiPill(
                        label = if (isEnglish) "Total Spend" else "एकूण खर्च",
                        value = "₹${BillLanguageConverter.formatNumber(totals.totalSpend, language)}",
                        accent = AccentRose,
                    )
                    HeroKpiPill(
                        label = if (isEnglish) "Total Bags" else "एकूण डाग",
                        value = BillLanguageConverter.formatNumber(totals.totalBags, language),
                        accent = AccentGold,
                    )
                    HeroKpiPill(
                        label = if (isEnglish) "Weight" else "एकूण वजन",
                        value = "${BillLanguageConverter.formatNumber(totals.totalWeight, language)} kg",
                        accent = AccentCyan,
                    )
                    HeroKpiPill(
                        label = avgLabel,
                        value = avgValue,
                        accent = AccentEmerald,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyBreakdownCard(
    monthSummary: MonthlyAggregateSummary,
    language: BillLanguage,
    onSelectFarmer: (Long) -> Unit,
) {
    val isEnglish = language == BillLanguage.ENGLISH
    val monthTitle = MonthlyLedgerAggregator.formatMonthKeyForLanguage(
        monthSummary.monthKey,
        monthSummary.monthYearRaw,
        language,
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderSubtle),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = monthTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentAmber,
                    )
                    Text(
                        text = if (isEnglish) {
                            "${monthSummary.farmerCount} Farmers · ${monthSummary.billCount} Bills · ${DevanagariFormatter.formatBagsDashboard(monthSummary.totals.totalBags)} bags"
                        } else {
                            "${DevanagariFormatter.toDevanagari(monthSummary.farmerCount.toString())} शेतकरी · ${DevanagariFormatter.toDevanagari(monthSummary.billCount.toString())} बिले · ${BillLanguageConverter.formatNumber(monthSummary.totals.totalBags, language)} डाग"
                        },
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${BillLanguageConverter.formatNumber(monthSummary.totals.grandBalance, language)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentMint,
                    )
                    Text(
                        text = if (isEnglish) {
                            "Sale ₹${BillLanguageConverter.formatNumber(monthSummary.totals.totalSaleAmount, language)}"
                        } else {
                            "विक्री ₹${BillLanguageConverter.formatNumber(monthSummary.totals.totalSaleAmount, language)}"
                        },
                        fontSize = 10.sp,
                        color = AccentBlue,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                monthSummary.ledgers.forEach { ledger ->
                    val displayFarmer = BillLanguageConverter.formatFarmerName(
                        ledger.farmerName.ifBlank { if (isEnglish) "Unnamed Farmer" else "निनावी शेतकरी" },
                        language,
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFarmer(ledger.id) },
                        shape = RoundedCornerShape(10.dp),
                        color = BgInput,
                        border = BorderStroke(
                            1.dp,
                            if (ledger.isActive) AccentEmerald else BorderSubtle,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                if (ledger.isActive) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = if (isEnglish) "Active bill" else "सक्रिय बिल",
                                        tint = AccentEmerald,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .size(16.dp),
                                    )
                                }
                                Column {
                                    Text(
                                        text = displayFarmer,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = if (isEnglish) {
                                            "${ledger.transactionCount} entries · Sale ₹${BillLanguageConverter.formatNumber(ledger.totals.totalSaleAmount, language)}"
                                        } else {
                                            "${DevanagariFormatter.toDevanagari(ledger.transactionCount.toString())} व्यवहार · विक्री ₹${BillLanguageConverter.formatNumber(ledger.totals.totalSaleAmount, language)}"
                                        },
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                    )
                                }
                            }

                            Text(
                                text = "₹${BillLanguageConverter.formatNumber(ledger.totals.grandBalance, language)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentMint,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAnalyticsCard(
    isEnglish: Boolean,
    onLoadSampleBill: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderSubtle),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Analytics,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(44.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isEnglish) "No transactions to visualize" else "आलेखासाठी व्यवहार उपलब्ध नाहीत",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isEnglish) {
                    "Paste WhatsApp ledger data or load the August 2026 sample to inspect charts."
                } else {
                    "आलेख पाहण्यासाठी WhatsApp डेटा पेस्ट करा किंवा ऑगस्ट २०२६ सॅम्पल लोड करा."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onLoadSampleBill,
                border = BorderStroke(1.dp, AccentBlue),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = if (isEnglish) "Load August 2026 Sample" else "ऑगस्ट २०२६ सॅम्पल लोड करा",
                    color = AccentBlue,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun HeroKpiPill(
    label: String,
    value: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgCard.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                maxLines = 1,
            )
        }
    }
}
