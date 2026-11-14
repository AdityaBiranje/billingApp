package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SavedLedgerSummary
import com.example.domain.util.DevanagariFormatter
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgSecondary
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedLedgersSheet(
    savedLedgers: List<SavedLedgerSummary>,
    onSelectLedger: (Long) -> Unit,
    onDeleteLedger: (Long) -> Unit,
    onSaveCurrentAsNew: () -> Unit,
    onStartBlankBill: () -> Unit,
    onLoadSampleBill: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgSecondary,
        contentColor = TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "जतन केलेली बिले (Saved Ledgers)",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "सर्व बिले तुमच्या फोनवर सुरक्षितपणे ऑफलाइन जतन राहतात.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        onSaveCurrentAsNew()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_as_new_bill_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("नवीन प्रत जतन करा", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        onStartBlankBill()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_blank_bill_button"),
                    border = BorderStroke(1.dp, BorderHighlight),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.NoteAdd, contentDescription = null, tint = AccentMint)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("नवीन रिकामे बिल", fontSize = 12.sp, color = AccentMint)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    onLoadSampleBill()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
            ) {
                Icon(Icons.Outlined.Restore, contentDescription = null, tint = AccentGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ऑगस्ट २०२६ सॅम्पल बिल लोड करा", color = AccentGold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(savedLedgers, key = { it.id }) { summary ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLedger(summary.id)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = BgCard,
                        border = BorderStroke(
                            1.dp,
                            if (summary.isActive) AccentEmerald else BorderSubtle,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (summary.isActive) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = "सक्रिय बिल",
                                            tint = AccentEmerald,
                                            modifier = Modifier.padding(end = 6.dp),
                                        )
                                    }
                                    Text(
                                        text = summary.farmerName.ifBlank { "निनावी शेतकरी" },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${summary.monthYear.ifBlank { "महिना नाही" }} · ${summary.transactionCount} व्यवहार · शिल्लक ${DevanagariFormatter.formatInrDashboard(summary.grandBalance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                            }

                            if (savedLedgers.size > 1) {
                                IconButton(onClick = { onDeleteLedger(summary.id) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "हटवा",
                                        tint = AccentRose,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
