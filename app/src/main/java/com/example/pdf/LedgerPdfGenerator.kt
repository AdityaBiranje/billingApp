package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.domain.model.BillLanguage
import com.example.domain.model.LedgerTotals
import com.example.domain.model.LedgerTransaction
import com.example.domain.util.BillLanguageConverter
import com.example.domain.util.DevanagariFormatter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

object LedgerPdfGenerator {

    // A4 Landscape in PostScript points (297mm x 210mm)
    private const val PAGE_WIDTH = 842
    private const val PAGE_HEIGHT = 595

    private const val MARGIN_X = 28f
    private const val MARGIN_Y = 20f

    private enum class ColAlign { LEFT, CENTER, RIGHT }

    private data class ColumnGeometry(
        val width: Float,
        val align: ColAlign,
    )

    // Total width = 768f (fits neatly inside 842 - 2*28 - 18 = 768f inner box padding)
    private val COLUMN_GEOMETRY = listOf(
        ColumnGeometry(62f, ColAlign.CENTER),
        ColumnGeometry(36f, ColAlign.CENTER),
        ColumnGeometry(76f, ColAlign.LEFT),
        ColumnGeometry(46f, ColAlign.CENTER),
        ColumnGeometry(36f, ColAlign.CENTER),
        ColumnGeometry(68f, ColAlign.RIGHT),
        ColumnGeometry(60f, ColAlign.RIGHT),
        ColumnGeometry(50f, ColAlign.RIGHT),
        ColumnGeometry(52f, ColAlign.CENTER),
        ColumnGeometry(64f, ColAlign.RIGHT),
        ColumnGeometry(42f, ColAlign.CENTER),
        ColumnGeometry(76f, ColAlign.RIGHT),
        ColumnGeometry(100f, ColAlign.RIGHT),
    )

    fun createPdfDocument(
        context: Context,
        farmerName: String,
        monthYear: String,
        transactions: List<LedgerTransaction>,
        totals: LedgerTotals,
        language: BillLanguage = BillLanguage.MARATHI,
    ): PdfDocument {
        val baseTypeface = ResourcesCompat.getFont(context, R.font.noto_sans_devanagari)
            ?: Typeface.SANS_SERIF
        val boldTypeface = Typeface.create(baseTypeface, Typeface.BOLD)

        val pdf = PdfDocument()
        val pagesOfTransactions = paginateTransactions(transactions, maxRowsPerPage = 23)

        pagesOfTransactions.forEachIndexed { pageIndex, pageTransactions ->
            val isLastPage = pageIndex == pagesOfTransactions.lastIndex
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = pdf.startPage(pageInfo)
            drawLedgerPage(
                canvas = page.canvas,
                baseTypeface = baseTypeface,
                boldTypeface = boldTypeface,
                farmerName = farmerName,
                monthYear = monthYear,
                transactions = pageTransactions,
                totals = totals,
                language = language,
                includeTotalRow = isLastPage,
                pageIndex = pageIndex,
                totalPages = pagesOfTransactions.size,
            )
            pdf.finishPage(page)
        }

        return pdf
    }

    fun writePdfToStream(
        context: Context,
        outputStream: OutputStream,
        farmerName: String,
        monthYear: String,
        transactions: List<LedgerTransaction>,
        totals: LedgerTotals,
        language: BillLanguage = BillLanguage.MARATHI,
    ) {
        val pdf = createPdfDocument(context, farmerName, monthYear, transactions, totals, language)
        try {
            pdf.writeTo(outputStream)
        } finally {
            pdf.close()
        }
    }

    fun createCachePdfFile(
        context: Context,
        farmerName: String,
        monthYear: String,
        transactions: List<LedgerTransaction>,
        totals: LedgerTotals,
        language: BillLanguage = BillLanguage.MARATHI,
    ): File {
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val localizedFarmer = BillLanguageConverter.formatFarmerName(farmerName, language).takeIf { farmerName.isNotBlank() }.orEmpty()
        val localizedMonth = BillLanguageConverter.formatMonthYear(monthYear, language).takeIf { monthYear.isNotBlank() }.orEmpty()
        val fileName = DevanagariFormatter.buildPdfFileName(localizedFarmer, localizedMonth)
        val outFile = File(dir, fileName)
        FileOutputStream(outFile).use { fos ->
            writePdfToStream(context, fos, farmerName, monthYear, transactions, totals, language)
        }
        return outFile
    }

    fun printLedger(
        context: Context,
        farmerName: String,
        monthYear: String,
        transactions: List<LedgerTransaction>,
        totals: LedgerTotals,
        language: BillLanguage = BillLanguage.MARATHI,
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val localizedFarmer = BillLanguageConverter.formatFarmerName(farmerName, language).takeIf { farmerName.isNotBlank() }.orEmpty()
        val localizedMonth = BillLanguageConverter.formatMonthYear(monthYear, language).takeIf { monthYear.isNotBlank() }.orEmpty()
        val jobName = DevanagariFormatter.buildPdfFileName(localizedFarmer, localizedMonth).removeSuffix(".pdf")

        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?,
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback,
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onWriteCancelled()
                    return
                }
                try {
                    FileOutputStream(destination.fileDescriptor).use { out ->
                        writePdfToStream(context, out, farmerName, monthYear, transactions, totals, language)
                    }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                }
            }
        }

        val attrs = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName, adapter, attrs)
    }

    private fun paginateTransactions(
        transactions: List<LedgerTransaction>,
        maxRowsPerPage: Int,
    ): List<List<LedgerTransaction>> {
        if (transactions.isEmpty()) return listOf(emptyList())
        val pages = mutableListOf<List<LedgerTransaction>>()
        var current = mutableListOf<LedgerTransaction>()
        var currentRowCount = 0

        for (tx in transactions) {
            val rowsNeeded = max(1, tx.items.size)
            if (current.isNotEmpty() && currentRowCount + rowsNeeded > maxRowsPerPage) {
                pages.add(current)
                current = mutableListOf()
                currentRowCount = 0
            }
            current.add(tx)
            currentRowCount += rowsNeeded
        }
        if (current.isNotEmpty()) {
            pages.add(current)
        }
        return pages
    }

    private fun drawLedgerPage(
        canvas: Canvas,
        baseTypeface: Typeface,
        boldTypeface: Typeface,
        farmerName: String,
        monthYear: String,
        transactions: List<LedgerTransaction>,
        totals: LedgerTotals,
        language: BillLanguage,
        includeTotalRow: Boolean,
        pageIndex: Int,
        totalPages: Int,
    ) {
        val labels = BillLanguageConverter.labels(language)

        // White paper background
        canvas.drawColor(Color.WHITE)

        val outerRect = RectF(MARGIN_X, MARGIN_Y, PAGE_WIDTH - MARGIN_X, PAGE_HEIGHT - MARGIN_Y)
        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2D3748")
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
        }
        canvas.drawRoundRect(outerRect, 8f, 8f, outerBorderPaint)

        val innerLeft = outerRect.left + 9f
        val innerRight = outerRect.right - 9f
        var cursorY = outerRect.top + 24f

        // Stall Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            typeface = boldTypeface
            textSize = 19f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(labels.stallTitle, PAGE_WIDTH / 2f, cursorY, titlePaint)
        cursorY += 15f

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#374151")
            typeface = baseTypeface
            textSize = 10.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(labels.stallSubtitle, PAGE_WIDTH / 2f, cursorY, subtitlePaint)
        cursorY += 9f

        // Info strip
        val stripLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.2f
        }
        canvas.drawLine(innerLeft, cursorY, innerRight, cursorY, stripLinePaint)
        cursorY += 15f

        val infoPaintLeft = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            typeface = boldTypeface
            textSize = 11.5f
            textAlign = Paint.Align.LEFT
        }
        val infoPaintRight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            typeface = boldTypeface
            textSize = 11.5f
            textAlign = Paint.Align.RIGHT
        }
        val displayName = BillLanguageConverter.formatFarmerName(farmerName, language)
        val baseMonth = BillLanguageConverter.formatMonthYear(monthYear, language)
        val displayMonth = if (totalPages > 1) {
            "$baseMonth ${BillLanguageConverter.formatPageIndicator(pageIndex + 1, totalPages, language)}"
        } else {
            baseMonth
        }
        canvas.drawText("${labels.namePrefix} $displayName", innerLeft + 6f, cursorY, infoPaintLeft)
        canvas.drawText("${labels.monthPrefix} $displayMonth", innerRight - 6f, cursorY, infoPaintRight)
        cursorY += 6f
        canvas.drawLine(innerLeft, cursorY, innerRight, cursorY, stripLinePaint)
        cursorY += 6f

        // Table geometry
        val tableTop = cursorY
        val headerHeight = 20f
        val totalItemRows = max(1, transactions.sumOf { max(1, it.items.size) })
        val totalRowCountWithFooter = totalItemRows + if (includeTotalRow) 1 else 0
        val availableBodyHeight = (outerRect.bottom - 10f) - (tableTop + headerHeight)
        val computedRowHeight = min(19.5f, max(14.5f, availableBodyHeight / max(1, totalRowCountWithFooter)))

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val altBgPaint = Paint().apply {
            color = Color.parseColor("#F9FAFB")
            style = Paint.Style.FILL
        }
        val totalBgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        val darkBorderPaint = Paint().apply {
            color = Color.parseColor("#2D3748")
            strokeWidth = 1.3f
        }
        val rowBorderPaint = Paint().apply {
            color = Color.parseColor("#718096")
            strokeWidth = 0.7f
        }
        val totalTopBorderPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            strokeWidth = 1.6f
        }

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            typeface = boldTypeface
            textSize = 8.4f
        }
        val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            typeface = baseTypeface
            textSize = 8.4f
        }
        val subLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4B5563")
            typeface = baseTypeface
            textSize = 7.4f
        }
        val totalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            typeface = boldTypeface
            textSize = 9.0f
        }

        // Compute column X offsets scaled evenly to innerLeft..innerRight
        val rawWidth = COLUMN_GEOMETRY.sumOf { it.width.toDouble() }.toFloat()
        val scale = (innerRight - innerLeft) / rawWidth
        val colLefts = FloatArray(COLUMN_GEOMETRY.size)
        val colRights = FloatArray(COLUMN_GEOMETRY.size)
        var x = innerLeft
        for (i in COLUMN_GEOMETRY.indices) {
            colLefts[i] = x
            x += COLUMN_GEOMETRY[i].width * scale
            colRights[i] = x
        }

        // Draw Table Header
        canvas.drawRect(innerLeft, tableTop, innerRight, tableTop + headerHeight, headerBgPaint)
        canvas.drawLine(innerLeft, tableTop, innerRight, tableTop, darkBorderPaint)
        for (i in COLUMN_GEOMETRY.indices) {
            drawCellText(
                canvas = canvas,
                text = labels.columns[i],
                left = colLefts[i],
                right = colRights[i],
                top = tableTop,
                bottom = tableTop + headerHeight,
                align = COLUMN_GEOMETRY[i].align,
                paint = headerTextPaint,
            )
        }
        canvas.drawLine(innerLeft, tableTop + headerHeight, innerRight, tableTop + headerHeight, darkBorderPaint)

        var rowY = tableTop + headerHeight

        if (transactions.isEmpty()) {
            val emptyRowBottom = rowY + computedRowHeight * 2f
            drawCellText(
                canvas = canvas,
                text = labels.emptyRowsLabel,
                left = innerLeft,
                right = innerRight,
                top = rowY,
                bottom = emptyRowBottom,
                align = ColAlign.CENTER,
                paint = cellTextPaint,
            )
            rowY = emptyRowBottom
        } else {
            transactions.forEachIndexed { txIndex, tx ->
                val itemCount = max(1, tx.items.size)
                val txTop = rowY
                val txBottom = rowY + itemCount * computedRowHeight

                if (txIndex % 2 == 1) {
                    canvas.drawRect(innerLeft, txTop, innerRight, txBottom, altBgPaint)
                }

                // Draw spanned date & expense columns once centered vertically across txTop..txBottom
                drawCellText(
                    canvas = canvas,
                    text = BillLanguageConverter.formatDate(tx.date, language),
                    left = colLefts[0],
                    right = colRights[0],
                    top = txTop,
                    bottom = txBottom,
                    align = COLUMN_GEOMETRY[0].align,
                    paint = cellTextPaint,
                )

                val expValues = listOf(
                    BillLanguageConverter.formatNumber(tx.expenses.commission, language),
                    BillLanguageConverter.formatNumber(tx.expenses.hamali, language),
                    BillLanguageConverter.formatNumber(tx.expenses.localTransport, language),
                    BillLanguageConverter.formatNumber(tx.expenses.mainTransport, language),
                    BillLanguageConverter.formatNumber(tx.expenses.postage, language),
                    BillLanguageConverter.formatNumber(tx.totalSpend, language),
                    BillLanguageConverter.formatNumber(tx.balance, language),
                )
                for (offset in expValues.indices) {
                    val colIdx = 6 + offset
                    drawCellText(
                        canvas = canvas,
                        text = expValues[offset],
                        left = colLefts[colIdx],
                        right = colRights[colIdx],
                        top = txTop,
                        bottom = txBottom,
                        align = COLUMN_GEOMETRY[colIdx].align,
                        paint = cellTextPaint,
                    )
                }

                // Draw each flower lot item row
                for (itemIdx in 0 until itemCount) {
                    val item = tx.items.getOrNull(itemIdx)
                    val itemTop = txTop + itemIdx * computedRowHeight
                    val itemBottom = itemTop + computedRowHeight

                    if (item != null) {
                        drawCellText(
                            canvas = canvas,
                            text = BillLanguageConverter.formatNumber(item.bags, language),
                            left = colLefts[1],
                            right = colRights[1],
                            top = itemTop,
                            bottom = itemBottom,
                            align = COLUMN_GEOMETRY[1].align,
                            paint = cellTextPaint,
                        )
                        val localizedFlower = BillLanguageConverter.formatFlowerName(item.flowerName, language)
                        val localizedSub = BillLanguageConverter.formatSubLabel(item.subLabel, language)
                        val flowerLabel = buildString {
                            append(localizedFlower)
                            if (localizedSub.isNotBlank()) {
                                append(" ($localizedSub)")
                            }
                        }
                        drawCellText(
                            canvas = canvas,
                            text = flowerLabel,
                            left = colLefts[2],
                            right = colRights[2],
                            top = itemTop,
                            bottom = itemBottom,
                            align = COLUMN_GEOMETRY[2].align,
                            paint = if (localizedSub.isNotBlank()) subLabelPaint else cellTextPaint,
                        )
                        drawCellText(
                            canvas = canvas,
                            text = BillLanguageConverter.formatNumber(item.weight, language),
                            left = colLefts[3],
                            right = colRights[3],
                            top = itemTop,
                            bottom = itemBottom,
                            align = COLUMN_GEOMETRY[3].align,
                            paint = cellTextPaint,
                        )
                        drawCellText(
                            canvas = canvas,
                            text = BillLanguageConverter.formatNumber(item.rate, language),
                            left = colLefts[4],
                            right = colRights[4],
                            top = itemTop,
                            bottom = itemBottom,
                            align = COLUMN_GEOMETRY[4].align,
                            paint = cellTextPaint,
                        )
                        drawCellText(
                            canvas = canvas,
                            text = BillLanguageConverter.formatNumber(item.saleAmount, language),
                            left = colLefts[5],
                            right = colRights[5],
                            top = itemTop,
                            bottom = itemBottom,
                            align = COLUMN_GEOMETRY[5].align,
                            paint = cellTextPaint,
                        )
                    }

                    if (itemIdx < itemCount - 1) {
                        canvas.drawLine(colLefts[1], itemBottom, colRights[5], itemBottom, rowBorderPaint)
                    }
                }

                canvas.drawLine(innerLeft, txBottom, innerRight, txBottom, rowBorderPaint)
                rowY = txBottom
            }
        }

        // Draw Totals Row on last page
        if (includeTotalRow) {
            val totalRowBottom = rowY + computedRowHeight + 2f
            canvas.drawRect(innerLeft, rowY, innerRight, totalRowBottom, totalBgPaint)
            canvas.drawLine(innerLeft, rowY, innerRight, rowY, totalTopBorderPaint)

            val totalCells = listOf(
                labels.totalRowLabel,
                BillLanguageConverter.formatNumber(totals.totalBags, language),
                "-",
                BillLanguageConverter.formatNumber(totals.totalWeight, language),
                "-",
                BillLanguageConverter.formatNumber(totals.totalSaleAmount, language),
                BillLanguageConverter.formatNumber(totals.totalCommission, language),
                BillLanguageConverter.formatNumber(totals.totalHamali, language),
                BillLanguageConverter.formatNumber(totals.totalLocalTransport, language),
                BillLanguageConverter.formatNumber(totals.totalMainTransport, language),
                BillLanguageConverter.formatNumber(totals.totalPostage, language),
                BillLanguageConverter.formatNumber(totals.totalSpend, language),
                BillLanguageConverter.formatNumber(totals.grandBalance, language),
            )
            for (i in COLUMN_GEOMETRY.indices) {
                drawCellText(
                    canvas = canvas,
                    text = totalCells[i],
                    left = colLefts[i],
                    right = colRights[i],
                    top = rowY,
                    bottom = totalRowBottom,
                    align = COLUMN_GEOMETRY[i].align,
                    paint = totalTextPaint,
                )
            }
            canvas.drawLine(innerLeft, totalRowBottom, innerRight, totalRowBottom, totalTopBorderPaint)
        }
    }

    private fun drawCellText(
        canvas: Canvas,
        text: String,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        align: ColAlign,
        paint: Paint,
    ) {
        val pad = 4f
        val fm = paint.fontMetrics
        val baseline = (top + bottom) / 2f - (fm.ascent + fm.descent) / 2f
        val savedAlign = paint.textAlign
        val xPos = when (align) {
            ColAlign.LEFT -> {
                paint.textAlign = Paint.Align.LEFT
                left + pad
            }
            ColAlign.CENTER -> {
                paint.textAlign = Paint.Align.CENTER
                (left + right) / 2f
            }
            ColAlign.RIGHT -> {
                paint.textAlign = Paint.Align.RIGHT
                right - pad
            }
        }
        canvas.drawText(text, xPos, baseline, paint)
        paint.textAlign = savedAlign
    }
}
