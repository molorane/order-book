package com.valr.order_book.repository.specification

import com.valr.order_book.entity.Trade
import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class TradeSpecification {

    companion object {

        fun filterTrades(
            currencyPair: CurrencyPair,
            startDate: LocalDateTime?,
            endDate: LocalDateTime?
        ): Specification<Trade> {
            return Specification<Trade> { root: Root<Trade>, _: CriteriaQuery<*>?, cb: CriteriaBuilder ->
                val predicates = mutableListOf<Predicate>()

                currencyPair.let {
                    val tradeOrder: Join<Trade, TradeOrder> = root.join<Trade, TradeOrder>("seller")
                    predicates.add(cb.and(cb.equal(tradeOrder.get<CurrencyPair>("currencyPair"), it)))
                    root.join<Any, Any>("seller", JoinType.INNER)
                    //predicates.add(cb.equal(root.get<CurrencyPair>("currencyPair"), it))
                }

                // Add predicate for startDate if it's not null
                startDate?.let {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("tradedAt"), it))
                }

                // Add predicate for endDate if it's not null
                endDate?.let {
                    predicates.add(cb.lessThanOrEqualTo(root.get("tradedAt"), it))
                }

                // Return combined predicates; if there are no predicates, return true
                cb.and(*predicates.toTypedArray()).takeIf { predicates.isNotEmpty() }
                    ?: cb.conjunction()
            }
        }
    }
}