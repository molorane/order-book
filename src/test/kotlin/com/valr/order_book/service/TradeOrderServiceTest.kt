package com.valr.order_book.service

import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.repository.TradeRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class TradeOrderServiceTest(
    @Autowired val tradeService: TradeService,
) {

    @MockBean
    lateinit var repository: TradeRepository

    @Test
    fun `given BTCZAR currency pair should return empty list`() {
        // Arrange
        Mockito.`when`(repository.findAllByCurrencyPair(CurrencyPair.ETHZAR)).thenReturn(
            emptyList()
        )

        // Act
        val result = tradeService.tradeHistory( currencyPair = CurrencyPairDto.ETHZAR)

        // Assert
        Assertions.assertTrue(
            result.isEmpty()
        )
    }
}