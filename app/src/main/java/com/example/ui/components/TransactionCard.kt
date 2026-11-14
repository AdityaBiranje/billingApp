package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FlowerLotItem
import com.example.domain.model.LedgerTransaction
import com.example.domain.util.DevanagariFormatter
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgCardElevated
import com.example.ui.theme.BgInput
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TransactionCard(
    transaction: LedgerTransaction,
    index: Int,
    onChange: (LedgerTransaction) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(transaction.id) { mutableStateOf(index == 0) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_card_${transaction.id}"),
        shape = RoundedCornerShape(14.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderSubtle),
    ) {
        Column {
            // Collapsible Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AccentBlue,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = transaction.date.ifBlank { "तारीख ${index + 1}" },
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "शिल्लक: ${DevanagariFormatter.formatInrDashboard(transaction.balance)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentMint,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BgInput,
                        border = BorderStroke(1.dp, BorderSubtle),
                    ) {
                        Text(
                            text = "${transaction.items.size} lot${if (transaction.items.size > 1) "s" else ""}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_txn_${transaction.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "व्यवहार हटवा",
                            tint = AccentRose,
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (expanded) "बंद करा" else "उघडा",
                        tint = TextSecondary,
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = androidx.compose.animation.expandVertically(animationSpec = spring()),
                exit = androidx.compose.animation.shrinkVertically(animationSpec = spring()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                ) {
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Date field
                    LedgerTextField(
                        label = "तारीख (Date)",
                        value = transaction.date,
                        placeholder = "उदा. ०३-०८-२६",
                        onValueChange = { onChange(transaction.copy(date = it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Flower Lots Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "फुलांचे लॉट (${transaction.items.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentMint,
                            fontWeight = FontWeight.Bold,
                        )
                        TextButton(
                            onClick = {
                                val nextLot = FlowerLotItem(
                                    id = System.nanoTime(),
                                    flowerName = transaction.items.firstOrNull()?.flowerName.orEmpty(),
                                )
                                onChange(transaction.copy(items = transaction.items + nextLot))
                            },
                            modifier = Modifier.testTag("add_lot_${transaction.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ लॉट जोडा")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    transaction.items.forEachIndexed { itemIdx, item ->
                        FlowerLotEditor(
                            item = item,
                            lotNumber = itemIdx + 1,
                            canRemove = transaction.items.size > 1,
                            onUpdate = { updatedItem ->
                                val newList = transaction.items.mapIndexed { idx, existing ->
                                    if (idx == itemIdx) updatedItem else existing
                                }
                                onChange(transaction.copy(items = newList))
                            },
                            onRemove = {
                                val newList = transaction.items.filterIndexed { idx, _ -> idx != itemIdx }
                                onChange(transaction.copy(items = newList))
                            },
                        )
                        if (itemIdx < transaction.items.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Expenses Section
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BgInput,
                        border = BorderStroke(1.dp, BorderSubtle),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "खर्च (Expenses)",
                                style = MaterialTheme.typography.labelLarge,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                NumericLedgerField(
                                    label = "कमिशन",
                                    value = itemDoubleToText(transaction.expenses.commission),
                                    onNumberChange = { v ->
                                        onChange(transaction.copy(expenses = transaction.expenses.copy(commission = v)))
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                NumericLedgerField(
                                    label = "हमाली",
                                    value = itemDoubleToText(transaction.expenses.hamali),
                                    onNumberChange = { v ->
                                        onChange(transaction.copy(expenses = transaction.expenses.copy(hamali = v)))
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                NumericLedgerField(
                                    label = "जा. भाडे",
                                    value = itemDoubleToText(transaction.expenses.localTransport),
                                    onNumberChange = { v ->
                                        onChange(transaction.copy(expenses = transaction.expenses.copy(localTransport = v)))
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                NumericLedgerField(
                                    label = "मो. भाडे",
                                    value = itemDoubleToText(transaction.expenses.mainTransport),
                                    onNumberChange = { v ->
                                        onChange(transaction.copy(expenses = transaction.expenses.copy(mainTransport = v)))
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                NumericLedgerField(
                                    label = "टपाल",
                                    value = itemDoubleToText(transaction.expenses.postage),
                                    onNumberChange = { v ->
                                        onChange(transaction.copy(expenses = transaction.expenses.copy(postage = v)))
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculated Transaction Summary Bar
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentEmerald.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.25f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            SummaryPill("विक्री", DevanagariFormatter.formatInrDashboard(transaction.totalSale), AccentBlue)
                            SummaryPill("खर्च", DevanagariFormatter.formatInrDashboard(transaction.totalSpend), AccentRose)
                            SummaryPill("शिल्लक", DevanagariFormatter.formatInrDashboard(transaction.balance), AccentMint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowerLotEditor(
    item: FlowerLotItem,
    lotNumber: Int,
    canRemove: Boolean,
    onUpdate: (FlowerLotItem) -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgCardElevated,
        border = BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.6f)),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "लॉट $lotNumber",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentMint,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            onUpdate(item.copy(saleAmount = item.weight * item.rate))
                        },
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Calculate,
                            contentDescription = "वजन × दर मोजा",
                            modifier = Modifier.size(14.dp),
                            tint = AccentBlue,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("वजन×दर", fontSize = 10.sp, color = AccentBlue)
                    }
                    if (canRemove) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "लॉट काढून टाका",
                                tint = AccentRose,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NumericLedgerField(
                    label = "डाग (Bags)",
                    value = itemDoubleToText(item.bags),
                    onNumberChange = { onUpdate(item.copy(bags = it)) },
                    modifier = Modifier.weight(0.9f),
                )
                LedgerTextField(
                    label = "फुलांचे नाव",
                    value = item.flowerName,
                    placeholder = "के. के.",
                    onValueChange = { onUpdate(item.copy(flowerName = it)) },
                    modifier = Modifier.weight(1.3f),
                )
                LedgerTextField(
                    label = "बॅच / टीप",
                    value = item.subLabel,
                    placeholder = "SRS",
                    onValueChange = { onUpdate(item.copy(subLabel = it)) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NumericLedgerField(
                    label = "एकूण (kg)",
                    value = itemDoubleToText(item.weight),
                    onNumberChange = { w ->
                        val autoSale = if (item.rate > 0) w * item.rate else item.saleAmount
                        onUpdate(item.copy(weight = w, saleAmount = autoSale))
                    },
                    modifier = Modifier.weight(1f),
                )
                NumericLedgerField(
                    label = "दर (Rate)",
                    value = itemDoubleToText(item.rate),
                    onNumberChange = { r ->
                        val autoSale = if (item.weight > 0) item.weight * r else item.saleAmount
                        onUpdate(item.copy(rate = r, saleAmount = autoSale))
                    },
                    modifier = Modifier.weight(1f),
                )
                NumericLedgerField(
                    label = "विक्री (₹)",
                    value = itemDoubleToText(item.saleAmount),
                    onNumberChange = { s -> onUpdate(item.copy(saleAmount = s)) },
                    modifier = Modifier.weight(1.1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun LedgerTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp, maxLines = 1) },
        placeholder = { Text(placeholder, fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.5f)) },
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ledgerTextFieldColors(),
    )
}

@Composable
fun NumericLedgerField(
    label: String,
    value: String,
    onNumberChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            val filtered = raw.filter { it.isDigit() || it == '.' }
            text = filtered
            onNumberChange(filtered.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(label, fontSize = 11.sp, maxLines = 1) },
        placeholder = { Text("0", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.5f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ledgerTextFieldColors(),
    )
}

@Composable
fun ledgerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = BgInput,
    unfocusedContainerColor = BgInput,
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = BorderSubtle,
    focusedLabelColor = AccentBlue,
    unfocusedLabelColor = TextSecondary,
    cursorColor = AccentBlue,
)

private fun itemDoubleToText(value: Double): String =
    DevanagariFormatter.formatPlainNumberForInput(value)
