package com.valr.order_book.integration


import com.valr.order_book.model.OrderBookDto
import com.valr.order_book.model.SideDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderIntegrationTest(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun `When orderBook with XRPZAR orders should contain 5 orders, 1 sell order and 4 buy orders`() {
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
    fun `When orderBook BTCZAR then return 0 Orders`() {
        // Act
        val response = restTemplate.getForEntity("/v1/BTCZAR/orderbook", OrderBookDto::class.java)

        // Assert
        val orderBook = response.body
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertTrue(response.hasBody())
        assertThat(orderBook?.asks?.count()?.plus(orderBook.bids?.count()!!)).isEqualTo(0)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup(): Unit {
            println(">> Setup")
        }

        @JvmStatic
        @AfterAll
        fun teardown(): Unit {
            println(">> Tear down")
        }
    }
}