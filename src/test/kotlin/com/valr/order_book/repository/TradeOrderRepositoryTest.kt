package com.valr.order_book.repository

import com.valr.order_book.entity.enums.CurrencyPair
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class TradeOrderRepositoryTest @Autowired constructor(
    val tradeOrderRepository: TradeOrderRepository,
) {

    @Test
    fun `When findAllByCurrencyPair with BTCZAR then return 99 Trades`() {
        Assertions.assertTrue(
            tradeOrderRepository.findAllByCurrencyPair(CurrencyPair.BTCZAR).size == 99
        )
    }

    @Test
    fun `When findAllByCurrencyPair SOLZAR then return 0 Trades`() {
        Assertions.assertTrue(
            tradeOrderRepository.findAllByCurrencyPair(CurrencyPair.SOLZAR).isEmpty()
        )
    }
}