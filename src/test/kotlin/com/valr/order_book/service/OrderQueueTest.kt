package com.valr.order_book.service

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.User
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.Status
import com.valr.order_book.entity.enums.TakerSide
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal

@SpringBootTest
class OrderQueueTest(
    @Autowired val orderQueue: OrderQueue
) {

    @Test
    fun `given matchOrder then sell order queue must contain 1 order and buy order queue empty`() {
        // Arrange
        val mothusi = User(
            id = 2024L,
            firstName = "Mothusi"
        )

        val michael = User(
            id = 2022L,
            firstName = "Mothusi"
        )

        val sellOrder = TradeOrder(
            sequenceId = 1L,
            id = "id1",
            user = mothusi,
            takerSide = TakerSide.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "123",
        )

        val buyOrder = TradeOrder(
            sequenceId = 1L,
            id = "id2",
            user = michael,
            takerSide = TakerSide.BUY,
            quantity = BigDecimal("80.00000000"),
            price = BigDecimal("11.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "1234",
        )

        val expectedSellOrder = TradeOrder(
            sequenceId = 1L,
            id = "id1",
            user = mothusi,
            takerSide = TakerSide.SELL,
            status = Status.PARTIALLY_FILLED,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            matchedQuantity = BigDecimal("80.00000000"),
            customerOrderId = "123",
        )

        // Act
        orderQueue.addOrder(sellOrder)
        orderQueue.addOrder(buyOrder)

        // Delay to allow a trade to be executed
        Thread.sleep(500)

        // Assert
        assertThat(orderQueue.getSellOrderQueue().size).isEqualTo(1)
        assertThat(orderQueue.getBuyOrderQueue().size).isEqualTo(0)

        val updatedSellOrder = orderQueue.getSellOrderQueue()[CurrencyPair.XRPZAR]?.first()

        assertThat(updatedSellOrder)
            .usingRecursiveComparison()
            .ignoringFields("sequenceId")
            .ignoringFields("orderDate")
            .isEqualTo(expectedSellOrder)
    }

    @Test
    fun `given no matchOrder then sell order queue must contain 2 order and buy order queue 1`() {
        // Arrange
        val mothusi = User(
            id = 2024L,
            firstName = "Mothusi"
        )

        val michael = User(
            id = 2022L,
            firstName = "Mothusi"
        )

        val sellOrder = TradeOrder(
            sequenceId = 1L,
            id = "id1",
            user = mothusi,
            takerSide = TakerSide.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "123",
        )

        val buyOrder = TradeOrder(
            sequenceId = 1L,
            id = "id2",
            user = michael,
            takerSide = TakerSide.BUY,
            quantity = BigDecimal("80.00000000"),
            price = BigDecimal("9.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "1234",
        )

        // Act
        orderQueue.addOrder(sellOrder)
        orderQueue.addOrder(buyOrder)

        // Delay to allow a trade to be executed
        Thread.sleep(500)

        // Assert
        assertThat(orderQueue.getSellOrderQueue().size).isEqualTo(1)
        assertThat(orderQueue.getBuyOrderQueue().size).isEqualTo(1)
    }
}