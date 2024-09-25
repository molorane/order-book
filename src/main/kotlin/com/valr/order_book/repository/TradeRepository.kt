package com.valr.order_book.repository

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TradeRepository : JpaRepository<TradeOrder, Long>, JpaSpecificationExecutor<TradeOrder> {
    fun findAllByCurrencyPair(currencyPair: CurrencyPair): List<TradeOrder>
    fun findTop40ByCurrencyPair(currencyPair: CurrencyPair, pageable: Pageable): Page<TradeOrder>
}