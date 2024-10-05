package com.valr.order_book.integration


import com.valr.order_book.exception.ApiError
import com.valr.order_book.model.*
import com.valr.order_book.service.OrderQueue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class OrderIntegrationTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val orderQueue: OrderQueue
) {
    private val USER_ID_KEY = "X-VALR-USER-ID";

    private val sellRequest = OrderRequestDto(
        side = SideDto.SELL,
        quantity = BigDecimal("3000.00000000"),
        price = BigDecimal("10.45000000"),
        pair = CurrencyPairDto.XRPZAR,
        customerOrderId = "123",
    )

    private val invalidSellRequest = OrderRequestDto(
        side = SideDto.SELL,
        quantity = BigDecimal("0.00000000"),
        price = BigDecimal("0.000000"),
        pair = CurrencyPairDto.XRPZAR,
        customerOrderId = "123",
    )

    private val buyRequest = OrderRequestDto(
        side = SideDto.BUY,
        quantity = BigDecimal("10.00000000"),
        price = BigDecimal("10.45000000"),
        pair = CurrencyPairDto.XRPZAR,
        customerOrderId = "123",
    )

    @Test
    @Order(1)
    fun `given orderBook with XRPZAR orders should contain 5 orders, 1 sell order and 4 buy orders`() {
        // Arrange
        val orderCount = 5

        // Act
        val response = restTemplate.getForEntity("/v1/XRPZAR/orderbook", OrderBookDto::class.java)

        // Assert
        val orderBook = response.body

        assertNotNull(orderBook, "Value should not be null")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertTrue(response.hasBody())

        assertTrue(
            orderBook?.asks?.count { o -> o.side == SideDto.SELL } == 1
        )

        assertTrue(
            orderBook?.bids?.count { o -> o.side == SideDto.BUY } == 4
        )

        assertThat(orderBook?.asks?.count()?.plus(orderBook.bids?.count()!!)).isEqualTo(orderCount)
    }

    @Test
    @Order(2)
    fun `given orderBook BTCZAR then return 0 Orders`() {
        // Act
        val response = restTemplate.getForEntity("/v1/BTCZAR/orderbook", OrderBookDto::class.java)

        // Assert
        val orderBook = response.body
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertTrue(response.hasBody())
        assertThat(orderBook?.asks?.count()?.plus(orderBook.bids?.count()!!)).isEqualTo(0)
    }

    @Test
    @Order(3)
    fun `given insufficient funds when placeOrder then should return ApiError`() {
        // Arrange
        val headers = HttpHeaders().apply {
            add(USER_ID_KEY, "2024")
            add("Content-Type", "application/json")
        }
        val reqBodyWithHeaders: HttpEntity<OrderRequestDto> = HttpEntity(sellRequest, headers)

        // Act
        val response = restTemplate.postForEntity("/v1/orders/limit", reqBodyWithHeaders, ApiError::class.java)

        // Assert
        val body = response.body
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertTrue(response.hasBody())
        assertEquals("Insufficient balance.", body?.message)
    }

    @Test
    @Order(4)
    fun `given invalid request when placeOrder then should return ApiError`() {
        // Arrange
        val headers = HttpHeaders().apply {
            add(USER_ID_KEY, "2024")
            add("Content-Type", "application/json")
        }
        val reqBodyWithHeaders: HttpEntity<OrderRequestDto> = HttpEntity(invalidSellRequest, headers)

        // Act
        val response = restTemplate.postForEntity("/v1/orders/limit", reqBodyWithHeaders, ApiError::class.java)

        // Assert
        val body = response.body
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertTrue(response.hasBody())
        assertEquals("Invalid order request.", body?.message)
    }

    @Test
    @Order(5)
    fun `given unsupported currency pair when placeOrder then should return ApiError`() {
        // Arrange
        val invalidSellRequest = OrderRequestDto(
            side = SideDto.SELL,
            quantity = BigDecimal("10.00000000"),
            price = BigDecimal("10.000000"),
            pair = CurrencyPairDto.SOLZAR,
            customerOrderId = "123",
        )
        val headers = HttpHeaders().apply {
            add(USER_ID_KEY, "2024")
            add("Content-Type", "application/json")
        }

        val reqBodyWithHeaders: HttpEntity<OrderRequestDto> = HttpEntity(invalidSellRequest, headers)

        // Act
        val response = restTemplate.postForEntity("/v1/orders/limit", reqBodyWithHeaders, ApiError::class.java)

        // Assert
        val body = response.body
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertTrue(response.hasBody())
        assertEquals("Invalid currency pair.", body?.message)
    }

    @Test
    @Order(6)
    fun `given valid request when placeOrder then should place an order`() {
        // Arrange
        val request = OrderRequestDto(
            side = SideDto.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.450000"),
            pair = CurrencyPairDto.XRPZAR,
            customerOrderId = "123",
        )

        val headers = HttpHeaders().apply {
            add(USER_ID_KEY, "2024")
            add("Content-Type", "application/json")
        }

        val reqBodyWithHeaders: HttpEntity<OrderRequestDto> = HttpEntity(request, headers)

        val orderResponse = OrderResponseDto(
            sequenceId = 1,
            id = "id",
            side = SideDto.SELL,
            quantity = BigDecimal("105.00000000"),
            price = BigDecimal("10.450000"),
            quoteVolume = BigDecimal("1097.25000000000000"),
            matchedQuantity = BigDecimal("0"),
            pair = CurrencyPairDto.XRPZAR,
            status = StatusDto.PLACED,
            orderType = OrderTypeDto.LIMIT_ORDER,
            postOnly = false,
            customerOrderId = null,
            timeInForce = TimeInForceDto.GTC,
            allowMargin = false,
            reduceOnly = false
        )

        // Act
        val response = restTemplate.postForEntity("/v1/orders/limit", reqBodyWithHeaders, OrderResponseDto::class.java)

        // Assert
        val body = response.body
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertTrue(response.hasBody())

        assertThat(body)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .ignoringFields("sequenceId")
            .ignoringFields("tradedAt")
            .isEqualTo(orderResponse)

        assertThat(orderQueue.getSellOrderQueue().size).isEqualTo(1)
    }
}