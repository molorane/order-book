package com.valr.order_book.integration


import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.SideDto
import com.valr.order_book.model.TradeOrderDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDateTime


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeOrderIntegrationTest(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun `Assert trade-history returns 100 XRPZAR trades and contain expected trade`() {
        // Arrange
        val expected = TradeOrderDto(
            1287070023713554432,
            "84236217-782c-11ef-90ef-13862c70d2e0",
            SideDto.SELL,
            BigDecimal("105.00000000"),
            BigDecimal("10.42000000"),
            BigDecimal("1094.10000000"),
            CurrencyPairDto.XRPZAR,
            LocalDateTime.parse("2024-09-21T17:16:46.258")
        )

        // Act
        val response = restTemplate.getForEntity("/v1/XRPZAR/tradehistory", Array<TradeOrderDto>::class.java)

        // Assert
        val trades = response.body
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertTrue(response.hasBody())
        if (trades != null) {
            val trade: TradeOrderDto = trades[99]
            assertThat(trade).isEqualTo(expected)
            assertTrue(trades.contains(expected))
        }
    }

    @Test
    fun `Assert order-book, endpoint exist`() {
        val entity = restTemplate.getForEntity<String>("/v1/BTCZAR/orderbook")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        // assertThat(entity.body).contains("<h1>Blog</h1>", "Lorem")
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