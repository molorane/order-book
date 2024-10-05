package com.valr.order_book.repository


import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal


@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest @Autowired constructor(
    val orderRepository: OrderRepository,
) {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `given orderBook with XRPZAR orders should contain 5 orders, 1 sell order and 4 buy orders`() {
        // Arrange
        val orderCount = 5

        // Act
        val orders = orderRepository.orderBook(CurrencyPair.XRPZAR)

        // Assert
        assertThat(orderRepository.orderBook(CurrencyPair.XRPZAR).size).isEqualTo(orderCount)

        assertTrue(
            orders.count { o -> o.getTakerSide() == TakerSide.SELL } == 1
        )

        assertTrue(
            orders.count { o -> o.getTakerSide() == TakerSide.BUY } == 4
        )
    }

    @Test
    fun `given orderBook with XRPZAR orders should have expected sell order`() {
        // Arrange
        val sellOrderCount = 2;
        val sellQuantity = BigDecimal("500.00000000");
        val sellPrice = BigDecimal("10.45000000")

        // Act
        val orders = orderRepository.orderBook(CurrencyPair.XRPZAR)

        // Assert
        val sellOrder = orders.first { order -> order.getTakerSide() == TakerSide.SELL }

        assertThat(sellOrder.getOrderCount()).isEqualTo(sellOrderCount)
        assertThat(sellOrder.getQuantity()).isEqualTo(sellQuantity)
        assertThat(sellOrder.getPrice()).isEqualTo(sellPrice)
    }

    @Test
    fun `given orderBook BTCZAR then return 0 Orders`() {
        assertTrue(
            orderRepository.orderBook(CurrencyPair.BTCZAR).isEmpty()
        )
    }
}