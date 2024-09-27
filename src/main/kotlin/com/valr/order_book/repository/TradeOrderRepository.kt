package com.valr.order_book.repository

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.repository.projection.OrderBookProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface TradeOrderRepository : JpaRepository<TradeOrder, Long>, JpaSpecificationExecutor<TradeOrder> {

    @Query(
        """
        SELECT o.id,
           o.takerSide as takerSide,
           o.orderType as orderType,
           o.quantity as quantity,
           o.price as price,
           o.currencyPair as currencyPair,
           COUNT(t.id) AS orderCount
        FROM TradeOrder o
        LEFT JOIN Trade t
        ON o.sequenceId = t.buyer.sequenceId OR o.sequenceId = t.seller.sequenceId
        GROUP BY o.id, o.takerSide, o.orderType, o.quantity, o.price
        ORDER BY o.id
    """
    )
    fun orderBook(currencyPair: CurrencyPair): List<OrderBookProjection>

    fun findAllByCurrencyPair(currencyPair: CurrencyPair): List<TradeOrder>

    fun findTop40ByCurrencyPair(currencyPair: CurrencyPair, pageable: Pageable): Page<TradeOrder>
}