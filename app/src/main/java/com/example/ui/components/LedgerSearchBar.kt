package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BillLanguage
import com.example.domain.model.StoredFarmerLedger
import com.example.domain.util.BillLanguageConverter
import com.example.domain.util.DevanagariFormatter
import com.example.domain.util.LedgerSearchFilter
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentMint
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgCardElevated
import com.example.ui.theme.BgInput
import com.example.ui.theme.BgSecondary
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LedgerSearchBar(
    query: String,
    language: BillLanguage,
    matchingLedgers: List<StoredFarmerLedger>,
    matchingTransactionCount: Int,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSelectFarmer: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnglish = language == BillLanguage.ENGLISH
    val focusManager = LocalFocusManager.current

    Surface(
        color = BgSecondary,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                placeholder = {
                    Text(
                        text = if (isEnglish) {
                            "Filter farmers by name or date (e.g. 03-08)..."
                        } else {
                            "शेतकरी नाव किंवा तारीख शोधा (उदा. 03-08)..."
                        },
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = if (isEnglish) "Search ledger" else "खातेवही शोधा",
                        tint = if (query.isNotBlank()) AccentBlue else TextSecondary,
                        modifier = Modifier.size(19.dp),
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onClearQuery()
                                focusManager.clearFocus()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("ledger_search_clear_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = if (isEnglish) "Clear search" else "शोध साफ करा",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = BgInput,
                    unfocusedContainerColor = BgInput,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = BorderHighlight.copy(alpha = 0.65f),
                    cursorColor = AccentBlue,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("ledger_search_bar"),
            )

            AnimatedVisibility(visible = query.isNotBlank()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (isEnglish) {
                                "${matchingLedgers.size} farmer(s) · $matchingTransactionCount active entries matched"
                            } else {
                                "${DevanagariFormatter.toDevanagari(matchingLedgers.size.toString())} शेतकरी · ${DevanagariFormatter.toDevanagari(matchingTransactionCount.toString())} व्यवहार जुळले"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentMint,
                            modifier = Modifier.testTag("ledger_search_summary"),
                        )
                    }

                    if (matchingLedgers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            matchingLedgers.forEach { ledger ->
                                SearchMatchedFarmerChip(
                                    ledger = ledger,
                                    query = query,
                                    language = language,
                                    onClick = {
                                        focusManager.clearFocus()
                                        onSelectFarmer(ledger.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchMatchedFarmerChip(
    ledger: StoredFarmerLedger,
    query: String,
    language: BillLanguage,
    onClick: () -> Unit,
) {
    val isEnglish = language == BillLanguage.ENGLISH
    val displayFarmer = BillLanguageConverter.formatFarmerName(
        ledger.farmerName.ifBlank { if (isEnglish) "Unnamed Farmer" else "निनावी शेतकरी" },
        language,
    )
    val displayMonth = BillLanguageConverter.formatMonthYear(ledger.monthYear, language)
    val matchedDates = LedgerSearchFilter.matchingDatesForLedger(ledger, query)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (ledger.isActive) BgCardElevated else BgCard,
        border = BorderStroke(
            1.dp,
            if (ledger.isActive) AccentEmerald else BorderHighlight,
        ),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .testTag("search_farmer_result_${ledger.id}"),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (ledger.isActive) Icons.Outlined.CheckCircle else Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    tint = if (ledger.isActive) AccentEmerald else AccentBlue,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = displayFarmer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$displayMonth · ₹${BillLanguageConverter.formatNumber(ledger.totals.grandBalance, language)}",
                    fontSize = 10.sp,
                    color = AccentMint,
                    maxLines = 1,
                )
                if (matchedDates.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = matchedDates.take(2).joinToString(", ") {
                            BillLanguageConverter.formatDate(it, language)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentAmber,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
