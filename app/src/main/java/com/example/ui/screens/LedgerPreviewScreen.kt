package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BillLanguage
import com.example.domain.model.FlowerLotItem
import com.example.domain.model.LedgerTotals
import com.example.domain.model.LedgerTransaction
import com.example.domain.util.BillLanguageConverter
import com.example.ui.LedgerUiState
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentMint
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgInput
import com.example.ui.theme.BgPrimary
import com.example.ui.theme.BgSecondary
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PaperAltRowBg
import com.example.ui.theme.PaperBorderDark
import com.example.ui.theme.PaperBorderLight
import com.example.ui.theme.PaperBorderMid
import com.example.ui.theme.PaperHeaderBg
import com.example.ui.theme.PaperInk
import com.example.ui.theme.PaperSubInk
import com.example.ui.theme.PaperTotalBg
import com.example.ui.theme.PaperTotalBorder
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun LedgerPreviewScreen(
    uiState: LedgerUiState,
    onScaleChange: (Float) -> Unit,
    onLanguageChange: (BillLanguage) -> Unit,
    onSavePdf: () -> Unit,
    onSharePdf: () -> Unit,
    onPrintPdf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary),
    ) {
        // Preview Language Switcher, Zoom & Export Toolbar
        Surface(
            color = BgSecondary,
            border = BorderStroke(1.dp, BorderSubtle),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "A4 Landscape",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentMint,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(uiState.previewScale * 100).roundToInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onScaleChange(uiState.previewScale - 0.1f) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Remove,
                                contentDescription = "झूम कमी करा",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Slider(
                            value = uiState.previewScale,
                            onValueChange = onScaleChange,
                            valueRange = 0.45f..1.5f,
                            modifier = Modifier
                                .width(120.dp)
                                .height(28.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = AccentBlue,
                                activeTrackColor = AccentBlue,
                            ),
                        )
                        IconButton(
                            onClick = { onScaleChange(uiState.previewScale + 0.1f) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "झूम वाढवा",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Horizontally scrollable zoom presets + Bill Language switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(0.55f to "Fit", 0.85f to "85%", 1.0f to "100%", 1.25f to "125%").forEach { (preset, label) ->
                        val active = kotlin.math.abs(uiState.previewScale - preset) < 0.04f
                        Surface(
                            onClick = { onScaleChange(preset) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) AccentBlue.copy(alpha = 0.2f) else BgInput,
                            border = BorderStroke(1.dp, if (active) AccentBlue else BorderSubtle),
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) AccentBlue else TextSecondary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = null,
                        tint = AccentMint,
                        modifier = Modifier.size(16.dp),
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.testTag("bill_language_toggle"),
                    ) {
                        SegmentedButton(
                            selected = uiState.billLanguage == BillLanguage.MARATHI,
                            onClick = { onLanguageChange(BillLanguage.MARATHI) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.testTag("lang_marathi_btn"),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = AccentEmerald.copy(alpha = 0.2f),
                                activeContentColor = AccentMint,
                                inactiveContainerColor = BgCard,
                                inactiveContentColor = TextSecondary,
                            ),
                        ) {
                            Text("मराठी", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        SegmentedButton(
                            selected = uiState.billLanguage == BillLanguage.ENGLISH,
                            onClick = { onLanguageChange(BillLanguage.ENGLISH) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.testTag("lang_english_btn"),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = AccentBlue.copy(alpha = 0.22f),
                                activeContentColor = AccentBlue,
                                inactiveContainerColor = BgCard,
                                inactiveContentColor = TextSecondary,
                            ),
                        ) {
                            Text("English", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onSavePdf,
                        enabled = !uiState.isGeneratingPdf,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("preview_save_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (uiState.billLanguage == BillLanguage.ENGLISH) "Save PDF" else "PDF सेव्ह",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }

                    Button(
                        onClick = onSharePdf,
                        enabled = !uiState.isGeneratingPdf,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("preview_share_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentEmerald,
                            contentColor = Color(0xFF062019),
                        ),
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (uiState.billLanguage == BillLanguage.ENGLISH) "Share" else "शेअर",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }

                    OutlinedButton(
                        onClick = onPrintPdf,
                        enabled = !uiState.isGeneratingPdf,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(44.dp)
                            .testTag("preview_print_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderHighlight),
                    ) {
                        Icon(Icons.Outlined.Print, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (uiState.billLanguage == BillLanguage.ENGLISH) "Print" else "प्रिंट",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // Smooth 2D Scrollable A4 Landscape Sheet Canvas (horizontal + vertical without gesture interception)
        val hScroll = rememberScrollState()
        val vScroll = rememberScrollState()
        val sheetWidth = (840f * uiState.previewScale).dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(hScroll)
                .verticalScroll(vScroll)
                .padding(14.dp),
        ) {
            LedgerPaperSheet(
                farmerName = uiState.farmerName,
                monthYear = uiState.monthYear,
                transactions = uiState.transactions,
                totals = uiState.totals,
                language = uiState.billLanguage,
                scale = uiState.previewScale,
                sheetWidth = sheetWidth,
            )
        }
    }
}

@Composable
private fun LedgerPaperSheet(
    farmerName: String,
    monthYear: String,
    transactions: List<LedgerTransaction>,
    totals: LedgerTotals,
    language: BillLanguage,
    scale: Float,
    sheetWidth: Dp,
) {
    val labels = BillLanguageConverter.labels(language)

    Surface(
        modifier = Modifier
            .width(sheetWidth)
            .testTag("ledger_paper_sheet"),
        shape = RoundedCornerShape((8f * scale).dp),
        color = PaperWhite,
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier
                .padding((12f * scale).dp)
                .border(
                    width = (1.5f * scale).dp,
                    color = PaperBorderDark,
                    shape = RoundedCornerShape((8f * scale).dp),
                )
                .padding((10f * scale).dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = labels.stallTitle,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = PaperInk,
                    fontSize = (20f * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = labels.stallSubtitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = (2f * scale).dp),
                    textAlign = TextAlign.Center,
                    color = PaperSubInk,
                    fontSize = (11f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height((6f * scale).dp))

                HorizontalDivider(thickness = (1.2f * scale).dp, color = PaperBorderLight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (6f * scale).dp, vertical = (5f * scale).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${labels.namePrefix} ${BillLanguageConverter.formatFarmerName(farmerName, language)}",
                        color = PaperInk,
                        fontSize = (12f * scale).sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${labels.monthPrefix} ${BillLanguageConverter.formatMonthYear(monthYear, language)}",
                        color = PaperInk,
                        fontSize = (12f * scale).sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                HorizontalDivider(thickness = (1.2f * scale).dp, color = PaperBorderLight)

                Spacer(modifier = Modifier.height((6f * scale).dp))

                HorizontalDivider(thickness = (1.4f * scale).dp, color = PaperBorderDark)
                LedgerTableHeader(columns = labels.columns, scale = scale)
                HorizontalDivider(thickness = (1.4f * scale).dp, color = PaperBorderDark)

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = (24f * scale).dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = labels.emptyRowsLabel,
                            color = PaperSubInk,
                            fontSize = (11f * scale).sp,
                        )
                    }
                } else {
                    transactions.forEachIndexed { txIndex, tx ->
                        val rowBg = if (txIndex % 2 == 1) PaperAltRowBg else PaperWhite
                        LedgerTransactionRowGroup(
                            tx = tx,
                            rowBg = rowBg,
                            language = language,
                            scale = scale,
                        )
                        HorizontalDivider(thickness = (0.8f * scale).dp, color = PaperBorderMid)
                    }
                }

                HorizontalDivider(thickness = (1.6f * scale).dp, color = PaperTotalBorder)
                LedgerTotalRow(
                    totalLabel = labels.totalRowLabel,
                    totals = totals,
                    language = language,
                    scale = scale,
                )
                HorizontalDivider(thickness = (1.4f * scale).dp, color = PaperTotalBorder)
            }
        }
    }
}

private data class TableWeights(
    val date: Float = 1.15f,
    val bags: Float = 0.65f,
    val flower: Float = 1.35f,
    val weight: Float = 0.85f,
    val rate: Float = 0.65f,
    val sale: Float = 1.15f,
    val comm: Float = 1.05f,
    val hamali: Float = 0.85f,
    val localTpt: Float = 0.90f,
    val mainTpt: Float = 1.05f,
    val postage: Float = 0.72f,
    val spend: Float = 1.25f,
    val balance: Float = 1.55f,
)

private val Weights = TableWeights()

@Composable
private fun LedgerTableHeader(columns: List<String>, scale: Float) {
    val fontSize = (8.8f * scale).sp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperHeaderBg)
            .padding(vertical = (4f * scale).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell(columns[0], Weights.date, TextAlign.Center, fontSize)
        HeaderCell(columns[1], Weights.bags, TextAlign.Center, fontSize)
        HeaderCell(columns[2], Weights.flower, TextAlign.Start, fontSize)
        HeaderCell(columns[3], Weights.weight, TextAlign.Center, fontSize)
        HeaderCell(columns[4], Weights.rate, TextAlign.Center, fontSize)
        HeaderCell(columns[5], Weights.sale, TextAlign.End, fontSize)
        HeaderCell(columns[6], Weights.comm, TextAlign.End, fontSize)
        HeaderCell(columns[7], Weights.hamali, TextAlign.End, fontSize)
        HeaderCell(columns[8], Weights.localTpt, TextAlign.Center, fontSize)
        HeaderCell(columns[9], Weights.mainTpt, TextAlign.End, fontSize)
        HeaderCell(columns[10], Weights.postage, TextAlign.Center, fontSize)
        HeaderCell(columns[11], Weights.spend, TextAlign.End, fontSize)
        HeaderCell(columns[12], Weights.balance, TextAlign.End, fontSize)
    }
}

@Composable
private fun LedgerTransactionRowGroup(
    tx: LedgerTransaction,
    rowBg: Color,
    language: BillLanguage,
    scale: Float,
) {
    val fontSize = (8.6f * scale).sp
    val items = tx.items.ifEmpty { listOf(FlowerLotItem()) }

    val middleWeightSum = Weights.bags + Weights.flower + Weights.weight + Weights.rate + Weights.sale

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(rowBg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(Weights.date)
                .fillMaxHeight()
                .padding(horizontal = (2f * scale).dp, vertical = (3f * scale).dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = BillLanguageConverter.formatDate(tx.date, language),
                color = PaperInk,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }

        Column(modifier = Modifier.weight(middleWeightSum)) {
            items.forEachIndexed { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = (3f * scale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BodyCell(BillLanguageConverter.formatNumber(item.bags, language), Weights.bags, TextAlign.Center, fontSize)
                    val localizedFlower = BillLanguageConverter.formatFlowerName(item.flowerName, language)
                    val localizedSub = BillLanguageConverter.formatSubLabel(item.subLabel, language)
                    val flowerLabel = buildString {
                        append(localizedFlower)
                        if (localizedSub.isNotBlank()) append(" ($localizedSub)")
                    }
                    BodyCell(flowerLabel, Weights.flower, TextAlign.Start, fontSize)
                    BodyCell(BillLanguageConverter.formatNumber(item.weight, language), Weights.weight, TextAlign.Center, fontSize)
                    BodyCell(BillLanguageConverter.formatNumber(item.rate, language), Weights.rate, TextAlign.Center, fontSize)
                    BodyCell(BillLanguageConverter.formatNumber(item.saleAmount, language), Weights.sale, TextAlign.End, fontSize)
                }
                if (idx < items.lastIndex) {
                    HorizontalDivider(thickness = (0.7f * scale).dp, color = PaperBorderMid)
                }
            }
        }

        SpannedValueCell(BillLanguageConverter.formatNumber(tx.expenses.commission, language), Weights.comm, TextAlign.End, fontSize, scale)
        SpannedValueCell(BillLanguageConverter.formatNumber(tx.expenses.hamali, language), Weights.hamali, TextAlign.End, fontSize, scale)
        SpannedValueCell(BillLanguageConverter.formatNumber(tx.expenses.localTransport, language), Weights.localTpt, TextAlign.Center, fontSize, scale)
        SpannedValueCell(BillLanguageConverter.formatNumber(tx.expenses.mainTransport, language), Weights.mainTpt, TextAlign.End, fontSize, scale)
        SpannedValueCell(BillLanguageConverter.formatNumber(tx.expenses.postage, language), Weights.postage, TextAlign.Center, fontSize, scale)
        SpannedValueCell(BillLanguageConverter.formatNumber(tx.totalSpend, language), Weights.spend, TextAlign.End, fontSize, scale)
        SpannedValueCell(BillLanguageConverter.formatNumber(tx.balance, language), Weights.balance, TextAlign.End, fontSize, scale)
    }
}

@Composable
private fun LedgerTotalRow(
    totalLabel: String,
    totals: LedgerTotals,
    language: BillLanguage,
    scale: Float,
) {
    val fontSize = (9.2f * scale).sp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PaperTotalBg)
            .padding(vertical = (4f * scale).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TotalCell(totalLabel, Weights.date, TextAlign.Center, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalBags, language), Weights.bags, TextAlign.Center, fontSize)
        TotalCell("-", Weights.flower, TextAlign.Start, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalWeight, language), Weights.weight, TextAlign.Center, fontSize)
        TotalCell("-", Weights.rate, TextAlign.Center, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalSaleAmount, language), Weights.sale, TextAlign.End, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalCommission, language), Weights.comm, TextAlign.End, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalHamali, language), Weights.hamali, TextAlign.End, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalLocalTransport, language), Weights.localTpt, TextAlign.Center, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalMainTransport, language), Weights.mainTpt, TextAlign.End, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalPostage, language), Weights.postage, TextAlign.Center, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.totalSpend, language), Weights.spend, TextAlign.End, fontSize)
        TotalCell(BillLanguageConverter.formatNumber(totals.grandBalance, language), Weights.balance, TextAlign.End, fontSize)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(
    text: String,
    weight: Float,
    align: TextAlign,
    fontSize: androidx.compose.ui.unit.TextUnit,
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 3.dp),
        color = PaperInk,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        textAlign = align,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BodyCell(
    text: String,
    weight: Float,
    align: TextAlign,
    fontSize: androidx.compose.ui.unit.TextUnit,
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 3.dp),
        color = PaperInk,
        fontSize = fontSize,
        textAlign = align,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SpannedValueCell(
    text: String,
    weight: Float,
    align: TextAlign,
    fontSize: androidx.compose.ui.unit.TextUnit,
    scale: Float,
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .padding(horizontal = (3f * scale).dp, vertical = (3f * scale).dp),
        contentAlignment = when (align) {
            TextAlign.Start -> Alignment.CenterStart
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.Center
        },
    ) {
        Text(
            text = text,
            color = PaperInk,
            fontSize = fontSize,
            textAlign = align,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TotalCell(
    text: String,
    weight: Float,
    align: TextAlign,
    fontSize: androidx.compose.ui.unit.TextUnit,
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 3.dp),
        color = PaperInk,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        textAlign = align,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
