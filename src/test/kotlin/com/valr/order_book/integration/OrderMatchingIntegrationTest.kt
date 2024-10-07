package com.valr.order_book.integration

import com.valr.order_book.entity.enums.Status
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto
import com.valr.order_book.model.SideDto
import com.valr.order_book.repository.OrderRepository
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.service.OrderQueue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class OrderMatchingIntegrationTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val orderQueue: OrderQueue,
    @Autowired val tradeRepository: TradeRepository,
    @Autowired val orderRepository: OrderRepository
) {
    private val USER_ID_KEY = "X-VALR-USER-ID";

    @Test
    fun `given matching sell and buy order then execute a trade`() {
        // Arrange
        val latch = CountDownLatch(1)
        val sellRequest = OrderRequestDto(
            side = SideDto.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.450000"),
            pair = CurrencyPairDto.XRPZAR,
            customerOrderId = "123",
        )

        val buyRequest = OrderRequestDto(
            side = SideDto.BUY,
            quantity = BigDecimal("80.00000000"),
            price = BigDecimal("11.450000"),
            pair = CurrencyPairDto.XRPZAR,
            customerOrderId = "2022",
        )

        val sellHeaders = HttpHeaders().apply {
            add(USER_ID_KEY, "2024")
            add("Content-Type", "application/json")
        }

        val buyHeaders = HttpHeaders().apply {
            add(USER_ID_KEY, "2022")
            add("Content-Type", "application/json")
        }

        val sellRequestBody: HttpEntity<OrderRequestDto> = HttpEntity(sellRequest, sellHeaders)
        val buyRequestBody: HttpEntity<OrderRequestDto> = HttpEntity(buyRequest, buyHeaders)


        // Act
        val sellResponse = restTemplate.postForEntity("/v1/orders/limit", sellRequestBody, OrderResponseDto::class.java)
        val buyResponse = restTemplate.postForEntity("/v1/orders/limit", buyRequestBody, OrderResponseDto::class.java)

        // Assert
        assertThat(sellResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(buyResponse.statusCode).isEqualTo(HttpStatus.OK)

        assertTrue(sellResponse.hasBody())
        assertTrue(buyResponse.hasBody())

        // Delay to ensure, we give threads enough time to execute a trade for matched orders
        val orderProcessed = latch.await(3, TimeUnit.SECONDS)

        assertThat(orderQueue.getSellOrderQueue().size).isEqualTo(1)
        assertThat(orderQueue.getBuyOrderQueue().size).isEqualTo(0) // By this time, a trade must have been executed, therefore no buy order

        // Pull a trade to confirm that orders matched
        val trade = tradeRepository.findBySellerSequenceIdAndBuyerSequenceId(
            sellResponse.body?.sequenceId!!,
            buyResponse.body?.sequenceId!!
        )

        // Confirm a trade was created
        assertNotNull(trade)
        assertThat(trade.price).isEqualTo(BigDecimal("10.45000000"))
        assertThat(trade.quantity).isEqualTo(BigDecimal("80.00000000"))

        // Pull a sell order to confirm that an order was updated accordingly
        val sellOrder = orderRepository.findById(sellResponse.body?.sequenceId!!)

        // Pull a buy order to confirm that an order was FILLED
        val buyOrder = orderRepository.findById(buyResponse.body?.sequenceId!!)

        assertThat(sellOrder.get().status).isEqualTo(Status.PARTIALLY_FILLED)
        assertThat(sellOrder.get().matchedQuantity).isEqualTo(BigDecimal("80.00000000"))
        assertThat(sellOrder.get().price).isEqualTo(BigDecimal("10.45000000"))

        assertThat(buyOrder.get().status).isEqualTo(Status.FILLED)
        assertThat(sellOrder.get().matchedQuantity).isEqualTo(BigDecimal("80.00000000"))
        assertThat(sellOrder.get().price).isEqualTo(BigDecimal("10.45000000"))
    }
}