package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.DevanagariFormatter
import com.example.ui.LedgerUiState
import com.example.ui.components.LedgerTextField
import com.example.ui.components.TotalsSummaryCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgInput
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import com.example.domain.model.BillLanguage

@Composable
fun BulkInputScreen(
    uiState: LedgerUiState,
    onFarmerNameChange: (String) -> Unit,
    onMonthYearChange: (String) -> Unit,
    onRawTextChange: (String) -> Unit,
    onParseClick: () -> Unit,
    onLoadQuickSample: () -> Unit,
    onClearText: () -> Unit,
    onOpenPreview: () -> Unit,
    onSavePdf: () -> Unit,
    onStartNewFarmerBill: () -> Unit,
    onSaveFarmerBill: () -> Unit,
    onOpenSavedBills: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnglish = uiState.billLanguage == BillLanguage.ENGLISH
    val storedCount = uiState.storedLedgers.count {
        it.transactionCount > 0 || it.totals.totalSaleAmount > 0.0 || it.farmerName.isNotBlank()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Farmer Info & Local Storage Card
        item {
            SectionShell(
                title = if (isEnglish) "Farmer & Phone Storage" else "शेतकरी माहिती व फोन स्टोरेज",
                icon = Icons.Outlined.PersonOutline,
                iconTint = AccentBlue,
            ) {
                Surface(
                    onClick = onOpenSavedBills,
                    shape = RoundedCornerShape(10.dp),
                    color = AccentEmerald.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bulk_storage_status_pill"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Storage,
                                contentDescription = null,
                                tint = AccentMint,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEnglish) {
                                    "$storedCount bills saved offline · Auto-updates charts"
                                } else {
                                    "फोनवर ${DevanagariFormatter.toDevanagari(storedCount.toString())} बिले जतन · आलेख आपोआप अपडेट होतात"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentMint,
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = if (isEnglish) "Open saved bills" else "जतन केलेली बिले",
                            tint = AccentMint,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LedgerTextField(
                    label = if (isEnglish) "Farmer Name" else "शेतकऱ्याचे नाव",
                    value = uiState.farmerName,
                    placeholder = "धरणेंद्र भुजप्पा मगदूम",
                    onValueChange = onFarmerNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("farmer_name_input"),
                )
                Spacer(modifier = Modifier.height(10.dp))
                LedgerTextField(
                    label = if (isEnglish) "Month / Year" else "महिना / वर्ष",
                    value = uiState.monthYear,
                    placeholder = "ऑगस्ट - २०२६",
                    onValueChange = onMonthYearChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("month_year_input"),
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
                            .testTag("bulk_new_farmer_button"),
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
                            .testTag("bulk_save_to_phone_button"),
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

        // WhatsApp Bulk Paste Card
        item {
            SectionShell(
                title = "महिन्याचा डेटा पेस्ट करा (WhatsApp)",
                icon = Icons.Outlined.ContentPaste,
                iconTint = AccentEmerald,
            ) {
                OutlinedTextField(
                    value = uiState.rawBulkText,
                    onValueChange = onRawTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 190.dp, max = 300.dp)
                        .testTag("bulk_raw_input"),
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        color = TextPrimary,
                    ),
                    placeholder = {
                        Text(
                            text = "🌹 ASHOK FLOWER STALL 🌹\n---\nAugust-26.\n---\nFarmer Name\nflower name - kk\n---\n3/8=\n50-600kg-30rt.\nComm=2700.\nHamali=400.\nTransport=5000.\nPostage=5.",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = TextSecondary.copy(alpha = 0.5f),
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = BgInput,
                        unfocusedContainerColor = BgInput,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = BorderSubtle,
                        cursorColor = AccentBlue,
                    ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onParseClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("parse_data_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentEmerald,
                        contentColor = Color(0xFF062019),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoFixHigh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "पार्स करा (Parse Data)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onLoadQuickSample,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("load_sample_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AccentBlue,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("सॅम्पल लोड करा", fontSize = 12.sp, color = TextPrimary)
                    }

                    OutlinedButton(
                        onClick = onClearText,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("clear_text_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TextSecondary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("क्लिअर", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                // Parse Status Feedback
                AnimatedVisibility(visible = uiState.parseStatus != null) {
                    uiState.parseStatus?.let { status ->
                        val bg = if (status.isSuccess) AccentEmerald.copy(alpha = 0.12f) else AccentRose.copy(alpha = 0.12f)
                        val border = if (status.isSuccess) AccentEmerald.copy(alpha = 0.35f) else AccentRose.copy(alpha = 0.35f)
                        val textColor = if (status.isSuccess) AccentMint else AccentRose
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = bg,
                            border = BorderStroke(1.dp, border),
                        ) {
                            Text(
                                text = status.message,
                                modifier = Modifier.padding(12.dp),
                                color = textColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }

                // Parsed Preview Summary
                AnimatedVisibility(visible = uiState.parsedSummary != null) {
                    uiState.parsedSummary?.let { summary ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = AccentBlue.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.25f)),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "पार्स केलेला सारांश",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    ParsedMiniStat("व्यवहार", summary.transactionCount.toString(), Modifier.weight(1f))
                                    ParsedMiniStat(
                                        "एकूण डाग",
                                        DevanagariFormatter.formatBagsDashboard(summary.totalBags),
                                        Modifier.weight(1f),
                                    )
                                    ParsedMiniStat("महिना", summary.monthYear.ifBlank { "—" }, Modifier.weight(1.2f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Totals Summary (if transactions exist)
        if (uiState.transactions.isNotEmpty()) {
            item {
                TotalsSummaryCard(
                    totals = uiState.totals,
                    transactionCount = uiState.transactions.size,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onOpenPreview,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("goto_preview_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentBlue),
                    ) {
                        Text("बिल प्रिव्ह्यू पहा", color = AccentBlue, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSavePdf,
                        enabled = !uiState.isGeneratingPdf,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("bulk_save_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isGeneratingPdf) "तयार होत आहे..." else "PDF सेव्ह करा",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Format Help Card
        item {
            SectionShell(
                title = "फॉरमॅट मदत (WhatsApp Format)",
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                iconTint = AccentGold,
            ) {
                val helpItems = listOf(
                    "3/8=" to "तारीख (Date)",
                    "50-600kg-30rt." to "डाग-वजन-दर",
                    "SRS" to "ट्रान्सपोर्टर / बॅच सब-लेबल",
                    "Comm=2700." to "कमिशन",
                    "Hamali=400." to "हमाली",
                    "Transport=5000." to "मुख्य भाडे",
                    "Local transport=0." to "जा. भाडे (ऐच्छिक)",
                    "Postage=5." to "टपाल",
                    "---" to "विभाजक (Separator)",
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    helpItems.forEach { (code, desc) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BgInput,
                            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.6f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = AccentGold.copy(alpha = 0.14f),
                                ) {
                                    Text(
                                        text = code,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGold,
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "→ $desc",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
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
fun SectionShell(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderSubtle),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconTint.copy(alpha = 0.15f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ParsedMiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = BgInput,
        border = BorderStroke(1.dp, BorderSubtle),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = label, fontSize = 10.sp, color = TextSecondary)
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentMint,
                maxLines = 1,
            )
        }
    }
}
