package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BillLanguage
import com.example.domain.model.LedgerTotals
import com.example.domain.model.LedgerTransaction
import com.example.domain.util.BillLanguageConverter
import com.example.domain.util.DevanagariFormatter
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgCardElevated
import com.example.ui.theme.BgInput
import com.example.ui.theme.BgPrimary
import com.example.ui.theme.BorderHighlight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun DailySalesExpensesBarLineChart(
    transactions: List<LedgerTransaction>,
    language: BillLanguage,
    modifier: Modifier = Modifier,
) {
    val isEnglish = language == BillLanguage.ENGLISH
    var selectedIndex by remember(transactions.size) {
        mutableIntStateOf(if (transactions.isNotEmpty()) transactions.lastIndex else -1)
    }
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(transactions) { animateIn = true }
    val animProgress by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "barChartReveal",
    )

    val maxVal = remember(transactions) {
        val peak = transactions.maxOfOrNull { max(it.totalSale, max(it.totalSpend, it.balance)) } ?: 1000.0
        max(1000.0, peak * 1.15)
    }

    val colWidthDp = 60.dp
    val colWidthPx = with(LocalDensity.current) { colWidthDp.toPx() }
    val chartHeight = 200.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_bar_line_chart"),
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.65f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEnglish) "Daily Sale, Spend & Balance" else "दैनिक विक्री, खर्च व शिल्लक आलेख",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (isEnglish) "Tap a date bar • Swipe horizontally for all days" else "तपशीलासाठी दिवसावर टॅप करा • आडवे स्क्रोल करा",
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend Pills (horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChartLegendChip(
                    label = if (isEnglish) "Gross Sale" else "एकूण विक्री",
                    color = AccentBlue,
                )
                ChartLegendChip(
                    label = if (isEnglish) "Expenses" else "एकूण खर्च",
                    color = AccentRose,
                )
                ChartLegendChip(
                    label = if (isEnglish) "Net Balance" else "निव्वळ शिल्लक",
                    color = AccentMint,
                )
            }

            // Interactive Floating Tooltip / Inspector Card for selected transaction
            val selectedTx = transactions.getOrNull(selectedIndex)
            if (selectedTx != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BgCardElevated,
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.45f)),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = BillLanguageConverter.formatDate(selectedTx.date, language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentAmber,
                            )
                            val bags = selectedTx.items.sumOf { it.bags }
                            val weight = selectedTx.items.sumOf { it.weight }
                            Text(
                                text = if (isEnglish) {
                                    "${DevanagariFormatter.formatBagsDashboard(bags)} bags · ${DevanagariFormatter.formatWeightDashboard(weight)}"
                                } else {
                                    "${BillLanguageConverter.formatNumber(bags, language)} डाग · ${BillLanguageConverter.formatNumber(weight, language)} kg"
                                },
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TooltipMetric(
                                label = if (isEnglish) "Sale" else "विक्री",
                                value = "₹${BillLanguageConverter.formatNumber(selectedTx.totalSale, language)}",
                                color = AccentBlue,
                            )
                            TooltipMetric(
                                label = if (isEnglish) "Spend" else "खर्च",
                                value = "₹${BillLanguageConverter.formatNumber(selectedTx.totalSpend, language)}",
                                color = AccentRose,
                            )
                            TooltipMetric(
                                label = if (isEnglish) "Net Bal" else "शिल्लक",
                                value = "₹${BillLanguageConverter.formatNumber(selectedTx.balance, language)}",
                                color = AccentMint,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Y-axis reference labels + Horizontally Scrollable Chart Canvas
            Row(modifier = Modifier.fillMaxWidth()) {
                // Fixed Y-Axis column
                Column(
                    modifier = Modifier
                        .width(46.dp)
                        .height(chartHeight)
                        .padding(bottom = 26.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    val steps = listOf(1.0, 0.66, 0.33, 0.0)
                    steps.forEach { frac ->
                        val v = maxVal * frac
                        Text(
                            text = formatCompactAxisRupees(v),
                            fontFamily = InterFontFamily,
                            fontSize = 10.sp,
                            color = TextMuted,
                            maxLines = 1,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Horizontally scrollable plot area
                val hScroll = rememberScrollState()
                val totalCanvasWidth = max(280f, transactions.size * colWidthDp.value).dp

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(hScroll),
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(totalCanvasWidth)
                            .height(chartHeight - 26.dp)
                            .pointerInput(transactions) {
                                detectTapGestures { offset ->
                                    if (transactions.isNotEmpty()) {
                                        val tappedIdx = (offset.x / colWidthPx).toInt()
                                            .coerceIn(0, transactions.lastIndex)
                                        selectedIndex = tappedIdx
                                    }
                                }
                            },
                    ) {
                        val plotH = size.height
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                        // Horizontal Gridlines
                        listOf(0f, 0.33f, 0.66f, 1f).forEach { frac ->
                            val y = plotH * frac
                            drawLine(
                                color = BorderSubtle.copy(alpha = 0.7f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = if (frac < 1f) dashEffect else null,
                            )
                        }

                        if (transactions.isEmpty()) return@Canvas

                        val balancePoints = mutableListOf<Offset>()
                        val barWidth = (colWidthPx * 0.28f)
                        val gapBetweenBars = 4.dp.toPx()

                        transactions.forEachIndexed { idx, tx ->
                            val centerX = idx * colWidthPx + colWidthPx / 2f

                            // Highlight band for selected column
                            if (idx == selectedIndex) {
                                drawRoundRect(
                                    color = AccentBlue.copy(alpha = 0.14f),
                                    topLeft = Offset(idx * colWidthPx + 4.dp.toPx(), 0f),
                                    size = Size(colWidthPx - 8.dp.toPx(), plotH),
                                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                )
                            }

                            // Gross Sale bar (Left)
                            val saleRatio = ((tx.totalSale / maxVal).toFloat().coerceIn(0f, 1f)) * animProgress
                            val saleH = saleRatio * plotH
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(AccentBlue, AccentBlue.copy(alpha = 0.55f)),
                                    startY = plotH - saleH,
                                    endY = plotH,
                                ),
                                topLeft = Offset(centerX - barWidth - gapBetweenBars / 2f, plotH - saleH),
                                size = Size(barWidth, max(2f, saleH)),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            )

                            // Total Spend bar (Right)
                            val spendRatio = ((tx.totalSpend / maxVal).toFloat().coerceIn(0f, 1f)) * animProgress
                            val spendH = spendRatio * plotH
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(AccentRose, AccentRose.copy(alpha = 0.55f)),
                                    startY = plotH - spendH,
                                    endY = plotH,
                                ),
                                topLeft = Offset(centerX + gapBetweenBars / 2f, plotH - spendH),
                                size = Size(barWidth, max(2f, spendH)),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            )

                            // Net Balance point
                            val balRatio = ((max(0.0, tx.balance) / maxVal).toFloat().coerceIn(0f, 1f)) * animProgress
                            val balY = plotH - balRatio * plotH
                            balancePoints.add(Offset(centerX, balY))
                        }

                        // Draw Net Balance trend polyline
                        if (balancePoints.size >= 2) {
                            val linePath = Path().apply {
                                moveTo(balancePoints.first().x, balancePoints.first().y)
                                for (i in 1 until balancePoints.size) {
                                    lineTo(balancePoints[i].x, balancePoints[i].y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = AccentMint,
                                style = Stroke(
                                    width = 2.6.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                ),
                            )
                        }

                        // Draw balance nodes
                        balancePoints.forEachIndexed { idx, pt ->
                            val isSelected = idx == selectedIndex
                            drawCircle(
                                color = if (isSelected) AccentGold else BgPrimary,
                                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = pt,
                            )
                            drawCircle(
                                color = if (isSelected) AccentGold else AccentMint,
                                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = pt,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }

                    // X-axis Date Labels row synchronized with horizontalScroll
                    Row(modifier = Modifier.width(totalCanvasWidth)) {
                        transactions.forEachIndexed { idx, tx ->
                            val isSelected = idx == selectedIndex
                            val shortDate = BillLanguageConverter.formatDate(tx.date, language)
                                .let { if (it.length >= 5) it.substring(0, 5) else it }
                            Text(
                                text = shortDate,
                                modifier = Modifier
                                    .width(colWidthDp)
                                    .clickable { selectedIndex = idx }
                                    .padding(top = 6.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) AccentAmber else TextSecondary,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RevenueExpenseDonutChart(
    totals: LedgerTotals,
    language: BillLanguage,
    modifier: Modifier = Modifier,
) {
    val isEnglish = language == BillLanguage.ENGLISH
    val slices = remember(totals, language) {
        val otherExpenses = totals.totalLocalTransport + totals.totalPostage
        listOf(
            DonutSlice(
                label = if (isEnglish) "Net Payout" else "निव्वळ शिल्लक",
                amount = max(0.0, totals.grandBalance),
                color = AccentEmerald,
            ),
            DonutSlice(
                label = if (isEnglish) "Commission" else "कमिशन",
                amount = max(0.0, totals.totalCommission),
                color = AccentBlue,
            ),
            DonutSlice(
                label = if (isEnglish) "Main Transport" else "मो. भाडे",
                amount = max(0.0, totals.totalMainTransport),
                color = AccentGold,
            ),
            DonutSlice(
                label = if (isEnglish) "Hamali" else "हमाली",
                amount = max(0.0, totals.totalHamali),
                color = AccentViolet,
            ),
            DonutSlice(
                label = if (isEnglish) "Local & Postage" else "जा. भाडे व टपाल",
                amount = max(0.0, otherExpenses),
                color = AccentRose,
            ),
        ).filter { it.amount > 0.0 }
    }

    var selectedSliceIdx by remember(slices.size) { mutableIntStateOf(0) }
    val totalSum = remember(slices) { max(1.0, slices.sumOf { it.amount }) }

    var animateSweep by remember { mutableStateOf(false) }
    LaunchedEffect(totals) { animateSweep = true }
    val sweepFactor by animateFloatAsState(
        targetValue = if (animateSweep) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "donutReveal",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("revenue_split_donut_chart"),
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.65f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isEnglish) "Gross Sale Distribution" else "एकूण विक्री व खर्च विभागणी",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (isEnglish) "Tap a slice category to inspect percentage share" else "टक्केवारी पाहण्यासाठी घटकावर टॅप करा",
                fontSize = 11.sp,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            val activeSlice = slices.getOrNull(selectedSliceIdx)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(168.dp)) {
                    val strokePx = 24.dp.toPx()
                    val diameter = size.minDimension - strokePx
                    val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Base track
                    drawArc(
                        color = BgInput,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx),
                    )

                    var startAngle = -90f
                    slices.forEachIndexed { idx, slice ->
                        val rawSweep = (slice.amount / totalSum * 360.0).toFloat()
                        val gap = if (slices.size > 1) 3f else 0f
                        val drawSweep = max(1f, (rawSweep - gap) * sweepFactor)
                        val isSelected = idx == selectedSliceIdx
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = drawSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = if (isSelected) strokePx + 5.dp.toPx() else strokePx,
                                cap = StrokeCap.Butt,
                            ),
                        )
                        startAngle += rawSweep
                    }
                }

                // Center callout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (activeSlice != null) {
                        val pct = ((activeSlice.amount / totalSum) * 100.0).roundToInt()
                        Text(
                            text = activeSlice.label,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = if (isEnglish) "$pct%" else "${DevanagariFormatter.toDevanagari(pct.toString())}%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeSlice.color,
                        )
                        Text(
                            text = "₹${BillLanguageConverter.formatNumber(activeSlice.amount, language)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                    } else {
                        Text(
                            text = if (isEnglish) "No Data" else "डेटा नाही",
                            fontSize = 12.sp,
                            color = TextMuted,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                slices.forEachIndexed { idx, slice ->
                    val isSelected = idx == selectedSliceIdx
                    val pct = ((slice.amount / totalSum) * 100.0).roundToInt()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) slice.color.copy(alpha = 0.18f) else BgInput,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) slice.color else BorderSubtle,
                        ),
                        modifier = Modifier.clickable { selectedSliceIdx = idx },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(slice.color),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${slice.label} ($pct%)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RateTrendAreaChart(
    transactions: List<LedgerTransaction>,
    language: BillLanguage,
    modifier: Modifier = Modifier,
) {
    val isEnglish = language == BillLanguage.ENGLISH
    val ratePoints = remember(transactions) {
        transactions.map { tx ->
            val totalWeight = tx.items.sumOf { it.weight }
            val avgRate = if (totalWeight > 0) {
                tx.totalSale / totalWeight
            } else {
                tx.items.map { it.rate }.filter { it > 0 }.average().takeIf { !it.isNaN() } ?: 0.0
            }
            tx.date to avgRate
        }
    }

    val rates = ratePoints.map { it.second }.filter { it > 0 }
    val minRate = rates.minOrNull() ?: 0.0
    val maxRate = rates.maxOrNull() ?: 0.0
    val avgRate = if (rates.isNotEmpty()) rates.average() else 0.0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rate_trend_area_chart"),
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.65f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isEnglish) "Flower Rate Trend (₹ / kg)" else "दैनिक दर कल (₹ / किलो)",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RateStatChip(
                    label = if (isEnglish) "Min" else "किमान",
                    value = "₹${BillLanguageConverter.formatNumber(minRate, language)}",
                    color = AccentRose,
                )
                RateStatChip(
                    label = if (isEnglish) "Average" else "सरासरी",
                    value = "₹${BillLanguageConverter.formatNumber(avgRate, language)}",
                    color = AccentCyan,
                )
                RateStatChip(
                    label = if (isEnglish) "Peak" else "कमाल",
                    value = "₹${BillLanguageConverter.formatNumber(maxRate, language)}",
                    color = AccentGold,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (ratePoints.isNotEmpty()) {
                val hScroll = rememberScrollState()
                val stepDp = 52.dp
                val stepPx = with(LocalDensity.current) { stepDp.toPx() }
                val totalWidth = max(280f, ratePoints.size * stepDp.value).dp
                val ceiling = max(10.0, maxRate * 1.2)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(hScroll),
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(totalWidth)
                            .height(130.dp),
                    ) {
                        val h = size.height
                        val pts = ratePoints.mapIndexed { idx, (_, r) ->
                            val x = idx * stepPx + stepPx / 2f
                            val y = h - ((r / ceiling).toFloat().coerceIn(0f, 1f) * (h - 14.dp.toPx()))
                            Offset(x, y)
                        }

                        // Subtle horizontal baseline
                        drawLine(
                            color = BorderSubtle,
                            start = Offset(0f, h),
                            end = Offset(size.width, h),
                            strokeWidth = 1.dp.toPx(),
                        )

                        if (pts.size >= 2) {
                            val linePath = Path().apply {
                                moveTo(pts.first().x, pts.first().y)
                                for (i in 1 until pts.size) {
                                    val prev = pts[i - 1]
                                    val cur = pts[i]
                                    val midX = (prev.x + cur.x) / 2f
                                    cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                                }
                            }
                            val fillPath = Path().apply {
                                addPath(linePath)
                                lineTo(pts.last().x, h)
                                lineTo(pts.first().x, h)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        AccentCyan.copy(alpha = 0.35f),
                                        AccentCyan.copy(alpha = 0.02f),
                                    ),
                                    startY = 0f,
                                    endY = h,
                                ),
                            )
                            drawPath(
                                path = linePath,
                                color = AccentCyan,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                            )
                        }

                        pts.forEach { pt ->
                            drawCircle(color = BgPrimary, radius = 4.dp.toPx(), center = pt)
                            drawCircle(
                                color = AccentAmber,
                                radius = 4.dp.toPx(),
                                center = pt,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }

                    Row(modifier = Modifier.width(totalWidth)) {
                        ratePoints.forEach { (date, rate) ->
                            val shortDate = BillLanguageConverter.formatDate(date, language)
                                .let { if (it.length >= 5) it.substring(0, 5) else it }
                            Column(
                                modifier = Modifier
                                    .width(stepDp)
                                    .padding(top = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "₹${BillLanguageConverter.formatNumber(rate, language)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentAmber,
                                    maxLines = 1,
                                )
                                Text(
                                    text = shortDate,
                                    fontSize = 9.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class DonutSlice(
    val label: String,
    val amount: Double,
    val color: Color,
)

@Composable
fun MonthlyAggregateComparisonChart(
    monthlyAggregates: List<com.example.domain.model.MonthlyAggregateSummary>,
    language: BillLanguage,
    modifier: Modifier = Modifier,
) {
    val isEnglish = language == BillLanguage.ENGLISH
    var selectedIndex by remember(monthlyAggregates.size) {
        mutableIntStateOf(if (monthlyAggregates.isNotEmpty()) monthlyAggregates.lastIndex else -1)
    }
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(monthlyAggregates) { animateIn = true }
    val animProgress by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "monthlyAggregateChartReveal",
    )

    val maxVal = remember(monthlyAggregates) {
        val peak = monthlyAggregates.maxOfOrNull {
            max(it.totals.totalSaleAmount, max(it.totals.totalSpend, it.totals.grandBalance))
        } ?: 1000.0
        max(1000.0, peak * 1.18)
    }

    val colWidthDp = 92.dp
    val colWidthPx = with(LocalDensity.current) { colWidthDp.toPx() }
    val chartHeight = 205.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("all_farmers_monthly_chart"),
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        border = BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.75f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isEnglish) "Monthly Totals (All Farmers)" else "मासिक एकूण व्यवहार सारांश (सर्व शेतकरी)",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (isEnglish) {
                    "Auto-updates from phone storage · Tap a month to inspect"
                } else {
                    "फोन स्टोरेजमधून आपोआप अपडेट होते · महिन्यावर टॅप करा"
                },
                fontSize = 11.sp,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChartLegendChip(
                    label = if (isEnglish) "Monthly Sale" else "मासिक विक्री",
                    color = AccentBlue,
                )
                ChartLegendChip(
                    label = if (isEnglish) "Monthly Spend" else "मासिक खर्च",
                    color = AccentRose,
                )
                ChartLegendChip(
                    label = if (isEnglish) "Net Payout" else "निव्वळ शिल्लक",
                    color = AccentMint,
                )
            }

            val selectedMonth = monthlyAggregates.getOrNull(selectedIndex)
            if (selectedMonth != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val monthLabel = com.example.domain.util.MonthlyLedgerAggregator.formatMonthKeyForLanguage(
                    selectedMonth.monthKey,
                    selectedMonth.monthYearRaw,
                    language,
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BgCardElevated,
                    border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f)),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = monthLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentAmber,
                            )
                            Text(
                                text = if (isEnglish) {
                                    "${selectedMonth.farmerCount} Farmers · ${selectedMonth.billCount} Bills"
                                } else {
                                    "${BillLanguageConverter.formatNumber(selectedMonth.farmerCount.toDouble(), language)} शेतकरी · ${BillLanguageConverter.formatNumber(selectedMonth.billCount.toDouble(), language)} बिले"
                                },
                                fontSize = 11.sp,
                                color = AccentMint,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TooltipMetric(
                                label = if (isEnglish) "Sale" else "एकूण विक्री",
                                value = "₹${BillLanguageConverter.formatNumber(selectedMonth.totals.totalSaleAmount, language)}",
                                color = AccentBlue,
                            )
                            TooltipMetric(
                                label = if (isEnglish) "Spend" else "एकूण खर्च",
                                value = "₹${BillLanguageConverter.formatNumber(selectedMonth.totals.totalSpend, language)}",
                                color = AccentRose,
                            )
                            TooltipMetric(
                                label = if (isEnglish) "Net Payout" else "निव्वळ शिल्लक",
                                value = "₹${BillLanguageConverter.formatNumber(selectedMonth.totals.grandBalance, language)}",
                                color = AccentMint,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isEnglish) {
                                "Bags: ${DevanagariFormatter.formatBagsDashboard(selectedMonth.totals.totalBags)} · Weight: ${DevanagariFormatter.formatWeightDashboard(selectedMonth.totals.totalWeight)} · Comm: ₹${BillLanguageConverter.formatNumber(selectedMonth.totals.totalCommission, language)}"
                            } else {
                                "डाग: ${BillLanguageConverter.formatNumber(selectedMonth.totals.totalBags, language)} · वजन: ${BillLanguageConverter.formatNumber(selectedMonth.totals.totalWeight, language)} kg · कमिशन: ₹${BillLanguageConverter.formatNumber(selectedMonth.totals.totalCommission, language)}"
                            },
                            fontSize = 11.sp,
                            color = TextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .width(46.dp)
                        .height(chartHeight)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    listOf(1.0, 0.66, 0.33, 0.0).forEach { frac ->
                        Text(
                            text = formatCompactAxisRupees(maxVal * frac),
                            fontFamily = InterFontFamily,
                            fontSize = 10.sp,
                            color = TextMuted,
                            maxLines = 1,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                val hScroll = rememberScrollState()
                val totalCanvasWidth = max(280f, monthlyAggregates.size * colWidthDp.value).dp

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(hScroll),
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(totalCanvasWidth)
                            .height(chartHeight - 32.dp)
                            .pointerInput(monthlyAggregates) {
                                detectTapGestures { offset ->
                                    if (monthlyAggregates.isNotEmpty()) {
                                        val tappedIdx = (offset.x / colWidthPx).toInt()
                                            .coerceIn(0, monthlyAggregates.lastIndex)
                                        selectedIndex = tappedIdx
                                    }
                                }
                            },
                    ) {
                        val plotH = size.height
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                        listOf(0f, 0.33f, 0.66f, 1f).forEach { frac ->
                            val y = plotH * frac
                            drawLine(
                                color = BorderSubtle.copy(alpha = 0.7f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = if (frac < 1f) dashEffect else null,
                            )
                        }

                        if (monthlyAggregates.isEmpty()) return@Canvas

                        val netPoints = mutableListOf<Offset>()
                        val barWidth = colWidthPx * 0.23f
                        val barGap = 4.dp.toPx()

                        monthlyAggregates.forEachIndexed { idx, month ->
                            val centerX = idx * colWidthPx + colWidthPx / 2f

                            if (idx == selectedIndex) {
                                drawRoundRect(
                                    color = AccentEmerald.copy(alpha = 0.14f),
                                    topLeft = Offset(idx * colWidthPx + 4.dp.toPx(), 0f),
                                    size = Size(colWidthPx - 8.dp.toPx(), plotH),
                                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                )
                            }

                            val saleH = ((month.totals.totalSaleAmount / maxVal).toFloat().coerceIn(0f, 1f)) * animProgress * plotH
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(AccentBlue, AccentBlue.copy(alpha = 0.55f)),
                                    startY = plotH - saleH,
                                    endY = plotH,
                                ),
                                topLeft = Offset(centerX - barWidth - barGap / 2f, plotH - saleH),
                                size = Size(barWidth, max(2f, saleH)),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            )

                            val spendH = ((month.totals.totalSpend / maxVal).toFloat().coerceIn(0f, 1f)) * animProgress * plotH
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(AccentRose, AccentRose.copy(alpha = 0.55f)),
                                    startY = plotH - spendH,
                                    endY = plotH,
                                ),
                                topLeft = Offset(centerX + barGap / 2f, plotH - spendH),
                                size = Size(barWidth, max(2f, spendH)),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            )

                            val netY = plotH - ((max(0.0, month.totals.grandBalance) / maxVal).toFloat().coerceIn(0f, 1f)) * animProgress * plotH
                            netPoints.add(Offset(centerX, netY))
                        }

                        if (netPoints.size >= 2) {
                            val linePath = Path().apply {
                                moveTo(netPoints.first().x, netPoints.first().y)
                                for (i in 1 until netPoints.size) {
                                    lineTo(netPoints[i].x, netPoints[i].y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = AccentMint,
                                style = Stroke(
                                    width = 2.8.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                ),
                            )
                        }

                        netPoints.forEachIndexed { idx, pt ->
                            val isSelected = idx == selectedIndex
                            drawCircle(
                                color = if (isSelected) AccentGold else BgPrimary,
                                radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                                center = pt,
                            )
                            drawCircle(
                                color = if (isSelected) AccentGold else AccentMint,
                                radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                                center = pt,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }

                    Row(modifier = Modifier.width(totalCanvasWidth)) {
                        monthlyAggregates.forEachIndexed { idx, month ->
                            val isSelected = idx == selectedIndex
                            val formattedMonth = com.example.domain.util.MonthlyLedgerAggregator.formatMonthKeyForLanguage(
                                month.monthKey,
                                month.monthYearRaw,
                                language,
                            )
                            Column(
                                modifier = Modifier
                                    .width(colWidthDp)
                                    .clickable { selectedIndex = idx }
                                    .padding(top = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = formattedMonth,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) AccentAmber else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = if (isEnglish) "${month.billCount} bills" else "${DevanagariFormatter.toDevanagari(month.billCount.toString())} बिले",
                                    fontSize = 9.sp,
                                    color = TextMuted,
                                    maxLines = 1,
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
private fun ChartLegendChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TooltipMetric(label: String, value: String, color: Color) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RateStatChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = BgInput,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "$label: ", fontSize = 11.sp, color = TextSecondary)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

private fun formatCompactAxisRupees(value: Double): String {
    return when {
        value >= 100000 -> "₹${(value / 100000.0 * 10).roundToInt() / 10.0}L"
        value >= 1000 -> "₹${(value / 1000.0).roundToInt()}k"
        else -> "₹${value.roundToInt()}"
    }
}
