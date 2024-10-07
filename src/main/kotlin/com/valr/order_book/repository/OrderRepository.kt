package com.valr.order_book.repository

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.repository.projection.OrderBookProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface OrderRepository : JpaRepository<TradeOrder, Long>, JpaSpecificationExecutor<TradeOrder> {

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
        WHERE o.currencyPair = :currencyPair
        GROUP BY o.id, o.takerSide, o.orderType, o.quantity, o.price
        ORDER BY o.id
        LIMIT 40
    """
    )
    fun orderBook(currencyPair: CurrencyPair): List<OrderBookProjection>
}