package com.valr.order_book.service

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.User
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.Status
import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.repository.OrderRepository
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.repository.UserWalletRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@ActiveProfiles("test")
@SpringBootTest
class TradeWorkerServiceTest(
    @Autowired private val tradeWorkerService: TradeWorkerService,
    @Autowired private val orderQueue: OrderQueue
) {

    @MockBean
    lateinit var tradeRepository: TradeRepository

    @MockBean
    lateinit var tradeExecutor: TradeExecutor

    @MockBean
    lateinit var orderRepository: OrderRepository

    @MockBean
    lateinit var userWalletRepository: UserWalletRepository


    val mothusi = User(
        id = 2024L,
        firstName = "Mothusi"
    )

    val michael = User(
        id = 2022L,
        firstName = "Mothusi"
    )

    @Test
    @DirtiesContext
    fun `given executeTrade and orders partially match then sell order queue should be contain one partially filled order`() {
        // Arrange
        val sellOrder = TradeOrder(
            sequenceId = 11L,
            id = "id1",
            user = mothusi,
            takerSide = TakerSide.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "123",
        )

        val buyOrder = TradeOrder(
            sequenceId = 12L,
            id = "id2",
            user = michael,
            takerSide = TakerSide.BUY,
            quantity = BigDecimal("80.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "1234",
        )

        val expectedSellOrder = TradeOrder(
            sequenceId = 11L,
            id = "id1",
            user = mothusi,
            status = Status.PARTIALLY_FILLED,
            takerSide = TakerSide.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "123",
            matchedQuantity = BigDecimal("80.00000000")
        )

        doNothing().`when`(tradeExecutor).run()

        // Act
        tradeWorkerService.executeTrade(sellOrder, buyOrder)

        // Assert

        // Assert: Verify that save method was called exactly once
        verify(tradeRepository).save(any())

        // Assert: Verify that save method was called exactly once
        verify(orderRepository, times(2)).save(any())

        // Assert: Verify that save method was called exactly once
        verify(userWalletRepository, times(4)).save(any())

        val updatedSellOrder = orderQueue.getSellOrderQueue()[CurrencyPair.XRPZAR]?.first()

        assertThat(expectedSellOrder)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .ignoringFields("sequenceId")
            .ignoringFields("user")
            .ignoringFields("orderDate")
            .isEqualTo(updatedSellOrder)
    }

    @Test
    @DirtiesContext
    fun `given executeTrade and orders partially match then buy order queue should contain one partially filled order`() {
        // Arrange
        val sellOrder = TradeOrder(
            sequenceId = 11L,
            id = "id1",
            user = mothusi,
            takerSide = TakerSide.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "123",
        )

        val buyOrder = TradeOrder(
            sequenceId = 12L,
            id = "id2",
            user = michael,
            takerSide = TakerSide.BUY,
            quantity = BigDecimal("200.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "1234",
        )

        val expectedBuyOrder = TradeOrder(
            sequenceId = 12L,
            id = "id2",
            user = mothusi,
            status = Status.PARTIALLY_FILLED,
            takerSide = TakerSide.BUY,
            quantity = BigDecimal("200.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "1234",
            matchedQuantity = BigDecimal("105.00000000")
        )

        doNothing().`when`(tradeExecutor).run()

        // Act
        tradeWorkerService.executeTrade(sellOrder, buyOrder)

        // Assert

        // Assert: Verify that save method was called exactly once
        verify(tradeRepository).save(any())

        // Assert: Verify that save method was called exactly once
        verify(orderRepository, times(2)).save(any())

        // Assert: Verify that save method was called exactly once
        verify(userWalletRepository, times(4)).save(any())

        val updatedBuyOrder = orderQueue.getBuyOrderQueue()[CurrencyPair.XRPZAR]?.first()

        assertThat(expectedBuyOrder)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .ignoringFields("sequenceId")
            .ignoringFields("user")
            .ignoringFields("orderDate")
            .isEqualTo(updatedBuyOrder)
    }

    @Test
    @DirtiesContext
    fun `given executeTrade and orders match fully then sell and buy order queue should be empty`() {
        // Arrange

        val sellOrder = TradeOrder(
            sequenceId = 11L,
            id = "id1",
            user = mothusi,
            takerSide = TakerSide.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "123",
        )

        val buyOrder = TradeOrder(
            sequenceId = 12L,
            id = "id2",
            user = michael,
            takerSide = TakerSide.BUY,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.45000000"),
            currencyPair = CurrencyPair.XRPZAR,
            customerOrderId = "1234",
        )

        doNothing().`when`(tradeExecutor).run()

        // Act
        tradeWorkerService.executeTrade(sellOrder, buyOrder)

        // Assert

        // Assert: Verify that save method was called exactly once
        verify(tradeRepository).save(any())

        // Assert: Verify that save method was called exactly once
        verify(orderRepository, times(2)).save(any())

        // Assert: Verify that save method was called exactly once
        verify(userWalletRepository, times(4)).save(any())

        assertEquals(0, orderQueue.getSellOrderQueue().size)
        assertEquals(0, orderQueue.getBuyOrderQueue().size)
    }

}