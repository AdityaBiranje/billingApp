package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BillLanguage
import com.example.domain.model.LedgerTransaction
import com.example.ui.LedgerUiState
import com.example.ui.components.LedgerTextField
import com.example.ui.components.TotalsSummaryCard
import com.example.ui.components.TransactionCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentMint
import com.example.ui.theme.BgCard
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EntriesScreen(
    uiState: LedgerUiState,
    onFarmerNameChange: (String) -> Unit,
    onMonthYearChange: (String) -> Unit,
    onAddTransaction: () -> Unit,
    onUpdateTransaction: (LedgerTransaction) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onStartNewFarmerBill: () -> Unit,
    onSaveFarmerBill: () -> Unit,
    onLoadSampleBill: () -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnglish = uiState.billLanguage == BillLanguage.ENGLISH
    val isFiltering = uiState.searchQuery.isNotBlank()
    val visibleTransactions = uiState.filteredTransactions
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionShell(
                title = if (isEnglish) "Farmer & Month Details" else "शेतकरी व महिना तपशील",
                icon = Icons.Outlined.PersonOutline,
                iconTint = AccentBlue,
            ) {
                LedgerTextField(
                    label = if (isEnglish) "Farmer Name" else "शेतकऱ्याचे नाव",
                    value = uiState.farmerName,
                    placeholder = "धरणेंद्र भुजप्पा मगदूम",
                    onValueChange = onFarmerNameChange,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                LedgerTextField(
                    label = if (isEnglish) "Month / Year" else "महिना / वर्ष",
                    value = uiState.monthYear,
                    placeholder = "ऑगस्ट - २०२६",
                    onValueChange = onMonthYearChange,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onStartNewFarmerBill,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("entries_new_farmer_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentBlue),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAddAlt1,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "+ New Farmer" else "+ नवीन शेतकरी",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                        )
                    }
                    Button(
                        onClick = onSaveFarmerBill,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("entries_save_to_phone_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue.copy(alpha = 0.22f),
                            contentColor = AccentMint,
                        ),
                        border = BorderStroke(1.dp, AccentEmerald),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Save to Phone" else "फोनवर सेव्ह करा",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentMint,
                        )
                    }
                }
            }
        }

        item {
            TotalsSummaryCard(
                totals = uiState.totals,
                transactionCount = uiState.transactions.size,
            )
        }

        if (uiState.transactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isEnglish) "No transactions yet" else "अद्याप कोणतेही व्यवहार नाहीत",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isEnglish) {
                                "Paste WhatsApp data in the Paste tab or add a new entry below."
                            } else {
                                "'पेस्ट करा' टॅबमधून WhatsApp डेटा पार्स करा किंवा खाली नवीन व्यवहार जोडा."
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
        } else if (isFiltering && visibleTransactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("entries_no_search_match"),
                    shape = RoundedCornerShape(14.dp),
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
                            imageVector = Icons.Outlined.SearchOff,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isEnglish) {
                                "No entries matching \"${uiState.searchQuery}\""
                            } else {
                                "\"${uiState.searchQuery}\" साठी कोणतेही व्यवहार सापडले नाहीत"
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
                                text = if (isEnglish) "Clear Search Filter" else "शोध साफ करा",
                                color = AccentBlue,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        } else {
            itemsIndexed(
                items = visibleTransactions,
                key = { _, tx -> tx.id },
            ) { index, tx ->
                TransactionCard(
                    transaction = tx,
                    index = index,
                    onChange = onUpdateTransaction,
                    onDelete = { onDeleteTransaction(tx.id) },
                )
            }
        }

        item {
            Button(
                onClick = onAddTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_transaction_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("+ नवीन व्यवहार जोडा", fontWeight = FontWeight.Bold)
            }
        }
    }
}
