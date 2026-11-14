package com.example

import com.example.domain.data.DefaultLedgerData
import com.example.domain.parser.WhatsAppLedgerParser
import com.example.domain.util.DevanagariFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerParserAndTotalsTest {

    @Test
    fun `default august data matches pre-verified totals`() {
        val totals = DevanagariFormatter.calculateTotals(DefaultLedgerData.defaultTransactions)
        assertEquals(645.0, totals.totalBags, 0.01)
        assertEquals(7740.0, totals.totalWeight, 0.01)
        assertEquals(261780.0, totals.totalSaleAmount, 0.01)
        assertEquals(39267.0, totals.totalCommission, 0.01)
        assertEquals(5160.0, totals.totalHamali, 0.01)
        assertEquals(0.0, totals.totalLocalTransport, 0.01)
        assertEquals(64500.0, totals.totalMainTransport, 0.01)
        assertEquals(100.0, totals.totalPostage, 0.01)
        assertEquals(109027.0, totals.totalSpend, 0.01)
        assertEquals(152753.0, totals.grandBalance, 0.01)
    }

    @Test
    fun `whatsapp parser handles multi-batch SRS sublabel and marathi conversion`() {
        val parsed = WhatsAppLedgerParser.parseAndConvert(WhatsAppLedgerParser.QUICK_SAMPLE_TEXT)
        assertEquals("Dharanendra Bhujappa Magdum", parsed.farmerName)
        assertEquals("ऑगस्ट - २०२६", parsed.monthYear)
        assertEquals(3, parsed.transactions.size)

        val first = parsed.transactions[0]
        assertEquals("०३-०८-२६", first.date)
        assertEquals("के. के.", first.items.first().flowerName)
        assertEquals(18000.0, first.totalSale, 0.01)
        assertEquals(8105.0, first.totalSpend, 0.01)
        assertEquals(9895.0, first.balance, 0.01)

        val third = parsed.transactions[2]
        assertEquals("०६-०८-२६", third.date)
        assertEquals(2, third.items.size)
        assertEquals("", third.items[0].subLabel)
        assertEquals("SRS", third.items[1].subLabel)
        assertEquals("के. के.", third.items[1].flowerName)
        assertEquals(16200.0, third.totalSale, 0.01)
        assertEquals(7295.0, third.totalSpend, 0.01)
        assertEquals(8905.0, third.balance, 0.01)
    }

    @Test
    fun `devanagari formatting applies indian grouping and dash for zero`() {
        assertEquals("१,५२,७५३", DevanagariFormatter.toDevanagariNumber(152753.0))
        assertEquals("-", DevanagariFormatter.toDevanagariNumber(0.0))
        assertEquals("₹1,52,753", DevanagariFormatter.formatInrDashboard(152753.0))
    }

    @Test
    fun `bill language converter converts marathi to english and vice versa`() {
        assertEquals(
            "Dharanendra Bhujappa Magdum",
            com.example.domain.util.BillLanguageConverter.formatFarmerName(
                "धरणेंद्र भुजप्पा मगदूम",
                com.example.domain.model.BillLanguage.ENGLISH,
            ),
        )
        assertEquals(
            "धरणेंद्र भुजप्पा मगदूम",
            com.example.domain.util.BillLanguageConverter.formatFarmerName(
                "Dharanendra Bhujappa Magdum",
                com.example.domain.model.BillLanguage.MARATHI,
            ),
        )
        assertEquals(
            "August - 2026",
            com.example.domain.util.BillLanguageConverter.formatMonthYear(
                "ऑगस्ट - २०२६",
                com.example.domain.model.BillLanguage.ENGLISH,
            ),
        )
        assertEquals(
            "ऑगस्ट - २०२६",
            com.example.domain.util.BillLanguageConverter.formatMonthYear(
                "August - 2026",
                com.example.domain.model.BillLanguage.MARATHI,
            ),
        )
        assertEquals(
            "03-08-26",
            com.example.domain.util.BillLanguageConverter.formatDate(
                "०३-०८-२६",
                com.example.domain.model.BillLanguage.ENGLISH,
            ),
        )
        assertEquals(
            "०३-०८-२६",
            com.example.domain.util.BillLanguageConverter.formatDate(
                "03-08-26",
                com.example.domain.model.BillLanguage.MARATHI,
            ),
        )
        assertEquals(
            "K.K.",
            com.example.domain.util.BillLanguageConverter.formatFlowerName(
                "के. के.",
                com.example.domain.model.BillLanguage.ENGLISH,
            ),
        )
        assertEquals(
            "गुलाब",
            com.example.domain.util.BillLanguageConverter.formatFlowerName(
                "Rose",
                com.example.domain.model.BillLanguage.MARATHI,
            ),
        )
        assertEquals(
            "1,52,753",
            com.example.domain.util.BillLanguageConverter.formatNumber(
                152753.0,
                com.example.domain.model.BillLanguage.ENGLISH,
            ),
        )
    }

    @Test
    fun `monthly aggregator groups marathi and english farmer bills into monthly totals`() {
        val augFarmer1 = com.example.domain.model.StoredFarmerLedger(
            id = 1L,
            farmerName = "धरणेंद्र भुजप्पा मगदूम",
            monthYear = "ऑगस्ट - २०२६",
            transactionCount = 10,
            totals = com.example.domain.model.LedgerTotals(
                totalBags = 100.0,
                totalWeight = 1200.0,
                totalSaleAmount = 50000.0,
                totalCommission = 7500.0,
                totalSpend = 15000.0,
                grandBalance = 35000.0,
            ),
            isActive = false,
            updatedAt = 1000L,
        )
        val augFarmer2 = com.example.domain.model.StoredFarmerLedger(
            id = 2L,
            farmerName = "Ramesh Patil",
            monthYear = "August-26",
            transactionCount = 5,
            totals = com.example.domain.model.LedgerTotals(
                totalBags = 50.0,
                totalWeight = 600.0,
                totalSaleAmount = 25000.0,
                totalCommission = 3750.0,
                totalSpend = 8000.0,
                grandBalance = 17000.0,
            ),
            isActive = false,
            updatedAt = 2000L,
        )
        val sepFarmer = com.example.domain.model.StoredFarmerLedger(
            id = 3L,
            farmerName = "Suresh Jadhav",
            monthYear = "September - 2026",
            transactionCount = 4,
            totals = com.example.domain.model.LedgerTotals(
                totalBags = 40.0,
                totalWeight = 480.0,
                totalSaleAmount = 20000.0,
                totalCommission = 3000.0,
                totalSpend = 6000.0,
                grandBalance = 14000.0,
            ),
            isActive = true,
            updatedAt = 3000L,
        )

        val aggregates = com.example.domain.util.MonthlyLedgerAggregator.aggregateByMonth(
            listOf(sepFarmer, augFarmer1, augFarmer2),
        )

        assertEquals(2, aggregates.size)
        // Chronological order: 2026-08 first, 2026-09 second
        val august = aggregates[0]
        assertEquals("2026-08", august.monthKey)
        assertEquals(2, august.farmerCount)
        assertEquals(2, august.billCount)
        assertEquals(75000.0, august.totals.totalSaleAmount, 0.01)
        assertEquals(23000.0, august.totals.totalSpend, 0.01)
        assertEquals(52000.0, august.totals.grandBalance, 0.01)
        assertEquals(150.0, august.totals.totalBags, 0.01)

        val september = aggregates[1]
        assertEquals("2026-09", september.monthKey)
        assertEquals(1, september.farmerCount)
        assertEquals(20000.0, september.totals.totalSaleAmount, 0.01)

        val overall = com.example.domain.util.MonthlyLedgerAggregator.combineTotals(
            listOf(augFarmer1, augFarmer2, sepFarmer),
        )
        assertEquals(95000.0, overall.totalSaleAmount, 0.01)
        assertEquals(66000.0, overall.grandBalance, 0.01)
    }

    @Test
    fun `search filter matches farmers by marathi or english name and transaction date`() {
        val farmer1 = com.example.domain.model.StoredFarmerLedger(
            id = 1L,
            farmerName = "धरणेंद्र भुजप्पा मगदूम",
            monthYear = "ऑगस्ट - २०२६",
            transactionCount = 2,
            transactionDates = listOf("०३-०८-२६", "०५-०८-२६"),
            totals = com.example.domain.model.LedgerTotals(totalSaleAmount = 50000.0, grandBalance = 35000.0),
            isActive = true,
            updatedAt = 1000L,
        )
        val farmer2 = com.example.domain.model.StoredFarmerLedger(
            id = 2L,
            farmerName = "Ramesh Patil",
            monthYear = "September - 2026",
            transactionCount = 1,
            transactionDates = listOf("14-09-26"),
            totals = com.example.domain.model.LedgerTotals(totalSaleAmount = 20000.0, grandBalance = 14000.0),
            isActive = false,
            updatedAt = 2000L,
        )
        val allFarmers = listOf(farmer1, farmer2)

        // Filter by English name matches Marathi farmer
        val byEngName = com.example.domain.util.LedgerSearchFilter.filterLedgers(allFarmers, "dharanendra")
        assertEquals(1, byEngName.size)
        assertEquals(1L, byEngName.first().id)

        // Filter by Marathi name matches Marathi farmer
        val byMarName = com.example.domain.util.LedgerSearchFilter.filterLedgers(allFarmers, "धरणेंद्र")
        assertEquals(1, byMarName.size)
        assertEquals(1L, byMarName.first().id)

        // Filter by ASCII slash date "3/8" matches Devanagari "०३-०८-२६"
        val bySlashDate = com.example.domain.util.LedgerSearchFilter.filterLedgers(allFarmers, "3/8")
        assertEquals(1, bySlashDate.size)
        assertEquals(1L, bySlashDate.first().id)

        // Filter by padded dash date "14-09" matches Ramesh Patil
        val byDashDate = com.example.domain.util.LedgerSearchFilter.filterLedgers(allFarmers, "14-09")
        assertEquals(1, byDashDate.size)
        assertEquals(2L, byDashDate.first().id)

        // Filter active transactions by date "03-08"
        val txs = DefaultLedgerData.defaultTransactions
        val filteredTxs = com.example.domain.util.LedgerSearchFilter.filterTransactions(
            transactions = txs,
            activeFarmerName = "धरणेंद्र भुजप्पा मगदूम",
            rawQuery = "03-08",
        )
        assertEquals(1, filteredTxs.size)
        assertEquals("०३-०८-२६", filteredTxs.first().date)

        // Non-matching query returns empty
        val noMatch = com.example.domain.util.LedgerSearchFilter.filterLedgers(allFarmers, "99-99")
        assertEquals(0, noMatch.size)
    }
}
