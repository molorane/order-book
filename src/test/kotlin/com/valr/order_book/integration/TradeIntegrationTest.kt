package com.valr.order_book.integration


import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.SideDto
import com.valr.order_book.model.TradeDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeIntegrationTest(@Autowired val restTemplate: TestRestTemplate) {



    @Test
    fun `When tradeHistory with XRPZAR then return 4 trades`() {
        // Arrange

        // Act
        val response = restTemplate.getForEntity("/v1/XRPZAR/tradehistory", Array<TradeDto>::class.java)

        val trades = response.body


        // Assert
        assertNotNull(trades, "Value should not be null")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertTrue(response.hasBody())
        assertTrue(trades?.size == 4)

    }

    @Test
    fun `When tradeHistory with XRPZAR should contain 2 sell trades and 2 buy trades`() {
        // Arrange

        // Act
        val response = restTemplate.getForEntity("/v1/XRPZAR/tradehistory", Array<TradeDto>::class.java)

        val trades = response.body

        // Assert
        Assertions.assertTrue(
            trades?.count { trade -> trade.takerSide == SideDto.SELL } == 2
        )

        Assertions.assertTrue(
            trades?.count { trade -> trade.takerSide == SideDto.BUY } == 2
        )
    }

    @Test
    fun `When tradeHistory with XRPZAR then return 4 XRPZAR and contain expected trade`() {
        // Arrange
        val expected = TradeDto(
            BigDecimal("150.00000000"),
            BigDecimal("10.45000000"),
            CurrencyPairDto.XRPZAR,
            LocalDateTime.parse("2024-09-28T23:30:03.643529"),
            SideDto.SELL,
            1,
            "1e055fbc-782b-11ef-90ef-13862c70d2e0",
            BigDecimal("1567.50000000")
        )

        // Act
        val response = restTemplate.getForEntity("/v1/XRPZAR/tradehistory", Array<TradeDto>::class.java)

        // Assert
        val trades = response.body

        assertNotNull(trades, "Value should not be null")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertTrue(response.hasBody())
        assertTrue(trades?.size == 4)

        if (trades != null) {
            val trade: TradeDto = trades[3]
            assertThat(trade)
                .usingRecursiveComparison()
                .ignoringFields("tradedAt")
                .isEqualTo(expected)
        }
    }

    @Test
    fun `When tradeHistory BTCZAR then return 0 trades`() {
        // Arrange
        val zeroTrades = 0

        // Act
        val response = restTemplate.getForEntity("/v1/BTCZAR/tradehistory", Array<TradeDto>::class.java)

        val trades = response.body

        assertThat(trades?.size).isEqualTo(zeroTrades)
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