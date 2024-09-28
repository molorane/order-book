package com.valr.order_book.repository


import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDateTime


@DataJpaTest
@ActiveProfiles("test")
class TradeRepositoryTest @Autowired constructor(
    val tradeRepository: TradeRepository,
) {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `When tradeHistory with XRPZAR then return only 1 page with 4 trades`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        val totalPages = 1
        val totalTrades = 4L

        // Act
        val trades = tradeRepository.tradeHistory(
            currencyPair = CurrencyPair.XRPZAR,
            pageable = pageable,
        )

        // Assert
        assertThat(trades.totalPages).isEqualTo(totalPages)
        assertThat(trades.totalElements).isEqualTo(totalTrades)
    }

    @Test
    fun `When tradeHistory with XRPZAR orders should contain 2 sell order and 2 buy orders`() {
        // Arrange
        val pageable = PageRequest.of(0, 20)

        // Act
        val trades = tradeRepository.tradeHistory(
            currencyPair = CurrencyPair.XRPZAR,
            pageable = pageable,
        )


        // Assert
        Assertions.assertTrue(
            trades.count { trade -> trade.getTakerSide() == TakerSide.SELL } == 2
        )

        Assertions.assertTrue(
            trades.count { trade -> trade.getTakerSide() == TakerSide.BUY } == 2
        )
    }

    @Test
    fun `When tradeHistory with XRPZAR orders should have expected sell order`() {
        // Arrange
        val pageable = PageRequest.of(0, 20)
        val id = "1e055fbc-782b-11ef-90ef-13862c70d2e0";
        val sellQuantity = BigDecimal("350.00000000");
        val sellPrice = BigDecimal("10.45000000")
        val quoteVolume = BigDecimal("3657.50000000")
        val tradeAt = LocalDateTime.parse("2024-09-28T22:54:04.925846")

        // Act
        val trades = tradeRepository.tradeHistory(
            currencyPair = CurrencyPair.XRPZAR,
            pageable = pageable,
        )

        // Assert
        val sellOrder = trades.first { trade -> trade.getTakerSide() == TakerSide.SELL }

        assertThat(sellOrder.getQuantity()).isEqualTo(sellQuantity)
        assertThat(sellOrder.getPrice()).isEqualTo(sellPrice)
        assertThat(sellOrder.getQuoteVolume().setScale(8)).isEqualTo(quoteVolume)
        assertThat(sellOrder.getId()).isEqualTo(id)
    }

    @Test
    fun `When tradeHistory SOLZAR then return 0 Orders`() {
        // Arrange
        val pageable = PageRequest.of(0, 20)


        Assertions.assertTrue(
            tradeRepository.tradeHistory(
                currencyPair = CurrencyPair.BTCZAR,
                pageable = pageable,
            ).isEmpty
        )
    }
}