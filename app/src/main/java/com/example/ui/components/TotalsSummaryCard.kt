package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LedgerTotals
import com.example.domain.util.DevanagariFormatter
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgCardElevated
import com.example.ui.theme.BgInput
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TotalsSummaryCard(
    totals: LedgerTotals,
    transactionCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.7f)),
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(BgCardElevated.copy(alpha = 0.55f), BgCard),
                    ),
                )
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "एकूण सारांश",
                    style = MaterialTheme.typography.titleSmall,
                    color = AccentMint,
                    fontWeight = FontWeight.ExtraBold,
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentEmerald.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.45f)),
                ) {
                    Text(
                        text = "$transactionCount व्यवहार",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentMint,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary 3 Financial KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatTile(
                    label = "एकूण विक्री",
                    value = DevanagariFormatter.formatInrDashboard(totals.totalSaleAmount),
                    valueColor = AccentBlue,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "एकूण खर्च",
                    value = DevanagariFormatter.formatInrDashboard(totals.totalSpend),
                    valueColor = AccentRose,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "एकूण शिल्लक",
                    value = DevanagariFormatter.formatInrDashboard(totals.grandBalance),
                    valueColor = AccentGold,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontally scrollable secondary volume & expense chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                VolumeChip(
                    label = "एकूण डाग",
                    value = DevanagariFormatter.formatBagsDashboard(totals.totalBags),
                    accent = AccentMint,
                )
                VolumeChip(
                    label = "एकूण वजन",
                    value = DevanagariFormatter.formatWeightDashboard(totals.totalWeight),
                    accent = AccentCyan,
                )
                VolumeChip(
                    label = "कमिशन",
                    value = DevanagariFormatter.formatInrDashboard(totals.totalCommission),
                    accent = AccentBlue,
                )
                VolumeChip(
                    label = "हमाली",
                    value = DevanagariFormatter.formatInrDashboard(totals.totalHamali),
                    accent = TextPrimary,
                )
                VolumeChip(
                    label = "मो. भाडे",
                    value = DevanagariFormatter.formatInrDashboard(totals.totalMainTransport),
                    accent = AccentGold,
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = BgInput,
        border = BorderStroke(1.dp, valueColor.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.5.dp)
                    .background(valueColor, RoundedCornerShape(2.dp)),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun VolumeChip(
    label: String,
    value: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = BgInput,
        border = BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "$label: ", fontSize = 11.sp, color = TextSecondary)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}
