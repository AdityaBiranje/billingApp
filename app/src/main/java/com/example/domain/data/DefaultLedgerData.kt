package com.example.domain.data

import com.example.domain.model.FlowerLotItem
import com.example.domain.model.LedgerTransaction
import com.example.domain.model.TransactionExpenses

object DefaultLedgerData {
    const val DEFAULT_FARMER_NAME = "धरणेंद्र भुजप्पा मगदूम"
    const val DEFAULT_MONTH_YEAR = "ऑगस्ट - २०२६"

    val defaultTransactions: List<LedgerTransaction> = listOf(
        tx(1, "०३-०८-२६", listOf(lot(101, 50, "के. के.", "", 600, 30, 18000)), 2700, 400, 0, 5000, 5),
        tx(2, "०५-०८-२६", listOf(lot(201, 40, "के. के.", "", 480, 30, 14400)), 2160, 320, 0, 4000, 5),
        tx(
            3, "०६-०८-२६",
            listOf(
                lot(301, 20, "के. के.", "", 240, 30, 7200),
                lot(302, 25, "के. के.", "SRS", 300, 30, 9000),
            ),
            2430, 360, 0, 4500, 5,
        ),
        tx(4, "०७-०८-२६", listOf(lot(401, 50, "के. के.", "", 600, 30, 18000)), 2700, 400, 0, 5000, 5),
        tx(5, "०८-०८-२६", listOf(lot(501, 21, "के. के.", "", 252, 40, 10080)), 1512, 168, 0, 2100, 5),
        tx(6, "०९-०८-२६", listOf(lot(601, 50, "के. के.", "", 600, 40, 24000)), 3600, 400, 0, 5000, 5),
        tx(7, "१०-०८-२६", listOf(lot(701, 51, "के. के.", "", 612, 40, 24480)), 3672, 408, 0, 5100, 5),
        tx(8, "११-०८-२६", listOf(lot(801, 50, "के. के.", "", 600, 30, 18000)), 2700, 400, 0, 5000, 5),
        tx(9, "१२-०८-२६", listOf(lot(901, 35, "के. के.", "", 420, 30, 12600)), 1890, 280, 0, 3500, 5),
        tx(10, "१३-०८-२६", listOf(lot(1001, 25, "के. के.", "", 300, 30, 9000)), 1350, 200, 0, 2500, 5),
        tx(11, "१४-०८-२६", listOf(lot(1101, 18, "के. के.", "", 216, 40, 8640)), 1296, 144, 0, 1800, 5),
        tx(12, "१५-०८-२६", listOf(lot(1201, 20, "के. के.", "", 240, 60, 14400)), 2160, 160, 0, 2000, 5),
        tx(13, "१६-०८-२६", listOf(lot(1301, 20, "के. के.", "", 240, 50, 12000)), 1800, 160, 0, 2000, 5),
        tx(14, "१७-०८-२६", listOf(lot(1401, 30, "के. के.", "", 360, 40, 14400)), 2160, 240, 0, 3000, 5),
        tx(15, "१८-०८-२६", listOf(lot(1501, 20, "के. के.", "", 240, 50, 12000)), 1800, 160, 0, 2000, 5),
        tx(16, "१९-०८-२६", listOf(lot(1601, 20, "के. के.", "", 240, 30, 7200)), 1080, 160, 0, 2000, 5),
        tx(17, "२०-०८-२६", listOf(lot(1701, 20, "के. के.", "", 240, 30, 7200)), 1080, 160, 0, 2000, 5),
        tx(18, "२२-०८-२६", listOf(lot(1801, 33, "के. के.", "", 396, 25, 9900)), 1485, 264, 0, 3300, 5),
        tx(19, "२३-०८-२६", listOf(lot(1901, 28, "के. के.", "", 336, 20, 6720)), 1008, 224, 0, 2800, 5),
        tx(20, "२५-०८-२६", listOf(lot(2001, 19, "के. के.", "", 228, 20, 4560)), 684, 152, 0, 1900, 5),
    )

    private fun lot(
        id: Long,
        bags: Int,
        flowerName: String,
        subLabel: String,
        weight: Int,
        rate: Int,
        saleAmount: Int,
    ) = FlowerLotItem(
        id = id,
        bags = bags.toDouble(),
        flowerName = flowerName,
        subLabel = subLabel,
        weight = weight.toDouble(),
        rate = rate.toDouble(),
        saleAmount = saleAmount.toDouble(),
    )

    private fun tx(
        id: Long,
        date: String,
        items: List<FlowerLotItem>,
        commission: Int,
        hamali: Int,
        localTransport: Int,
        mainTransport: Int,
        postage: Int,
    ) = LedgerTransaction(
        id = id,
        date = date,
        items = items,
        expenses = TransactionExpenses(
            commission = commission.toDouble(),
            hamali = hamali.toDouble(),
            localTransport = localTransport.toDouble(),
            mainTransport = mainTransport.toDouble(),
            postage = postage.toDouble(),
        ),
    )
}
