package com.valr.order_book.service

import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.SideDto
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.repository.projection.TradeHistoryProjection
import com.valr.order_book.service.impl.TradeServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TradeServiceTest {

    @Mock
    lateinit var tradeRepository: TradeRepository

    @InjectMocks
    lateinit var tradeService: TradeServiceImpl

    @Test
    fun `should return list with one trade when tradeHistory called with XRPZAR`() {
        // Arrange
        val trade = mock(TradeHistoryProjection::class.java)
        `when`(trade.getPrice()).thenReturn(BigDecimal("350.00000000"))
        `when`(trade.getQuantity()).thenReturn(BigDecimal("10.45000000"))
        `when`(trade.getCurrencyPair()).thenReturn(CurrencyPair.XRPZAR)
        `when`(trade.getTakerSide()).thenReturn(TakerSide.SELL)
        `when`(trade.getSequenceId()).thenReturn(1289673060357513217L)
        `when`(trade.getId()).thenReturn("7de4d453-7dd1-11ef-b601-d1eca0747ffd")
        `when`(trade.getTradedAt()).thenReturn(LocalDateTime.parse("2024-09-28T22:54:04.925846"))
        `when`(trade.getQuoteVolume()).thenReturn(BigDecimal("3657.50000000"))

        val trades = listOf(trade)
        val pageable: Pageable = PageRequest.of(0, 20)
        val tradePage: Page<TradeHistoryProjection> = PageImpl(trades, pageable, 1)

        `when`(
            tradeRepository.tradeHistory(
                currencyPair = CurrencyPair.BTCZAR,
                pageable = pageable
            )
        ).thenReturn(tradePage)

        // Act
        val result = tradeService.tradeHistory(
            currencyPair = CurrencyPairDto.BTCZAR,
            skip = 0,
            limit = 20
        )

        // Assert
        assertEquals(1, result.size)
        assertEquals(BigDecimal("350.00000000"), result[0].price)
        assertEquals(BigDecimal("10.45000000"), result[0].quantity)
        assertEquals(CurrencyPairDto.XRPZAR, result[0].currencyPair)
        assertEquals(SideDto.SELL, result[0].takerSide)
        assertEquals(1289673060357513217L, result[0].sequenceId)
        assertEquals("7de4d453-7dd1-11ef-b601-d1eca0747ffd", result[0].id)
        assertEquals(LocalDateTime.parse("2024-09-28T22:54:04.925846"), result[0].tradedAt)
        assertEquals(BigDecimal("3657.50000000"), result[0].quoteVolume)
    }


    @Test
    fun `should return empty list when tradeHistory called with BTCZAR`() {
        // Arrange
        val pageable = PageRequest.of(0, 20)
        `when`(
            tradeRepository.tradeHistory(
                currencyPair = CurrencyPair.BTCZAR,
                pageable = pageable
            )
        ).thenReturn(PageImpl(emptyList(), pageable, 0))

        // Act
        val result = tradeService.tradeHistory(
            currencyPair = CurrencyPairDto.BTCZAR,
            skip = 0,
            limit = 20
        )

        // Assert
        assertNotNull(result)

        assertTrue(result.isEmpty())
    }
}