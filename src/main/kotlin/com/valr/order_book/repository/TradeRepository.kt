package com.valr.order_book.repository

import com.valr.order_book.entity.Trade
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.repository.projection.TradeHistoryProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface TradeRepository : JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    @Query(
        """
        SELECT 
            t.price AS price,
            t.quantity AS quantity,
            b_order.currencyPair AS currencyPair, 
            b_order.takerSide AS takerSide, 
            b_order.sequenceId AS sequenceId,  
            b_order.id AS id,  
            t.tradedAt AS tradedAt
        FROM Trade t
        LEFT JOIN TradeOrder b_order ON t.buyer.sequenceId = b_order.sequenceId
        WHERE b_order.currencyPair = :currencyPair 
        AND (:fromDate IS NULL OR t.tradedAt >= :fromDate)
        AND (:toDate IS NULL OR t.tradedAt <= :toDate)
        
        UNION ALL
        
        SELECT 
            t.price AS price,
            t.quantity AS quantity,
            s_order.currencyPair AS currencyPair, 
            s_order.takerSide AS takerSide, 
            s_order.sequenceId AS sequenceId,  
            s_order.id AS id, 
            t.tradedAt AS tradedAt
        FROM Trade t
        LEFT JOIN TradeOrder s_order ON t.seller.sequenceId = s_order.sequenceId
        WHERE s_order.currencyPair = :currencyPair 
        AND (:fromDate IS NULL OR t.tradedAt >= :fromDate)
        AND (:toDate IS NULL OR t.tradedAt <= :toDate)
        ORDER BY tradedAt DESC
    """
    )
    fun tradeHistory(
        currencyPair: CurrencyPair,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        pageable: Pageable
    ): Page<TradeHistoryProjection>

    fun findBySellerSequenceIdAndBuyerSequenceId(seller: Long, buyer: Long): Trade
}