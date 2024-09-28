package com.valr.order_book.integration


import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.SideDto
import com.valr.order_book.model.TradeDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
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
    fun `Assert trade-history returns 4 XRPZAR orders and contain expected order`() {
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
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertTrue(response.hasBody())
        if (trades != null) {
            val trade: TradeDto = trades[3]
            assertThat(trade)
                .usingRecursiveComparison()
                .ignoringFields("tradedAt")
                .isEqualTo(expected)
        }
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