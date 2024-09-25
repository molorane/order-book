package com.valr.order_book.repository.specification

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class TradeSpecification {

    companion object {
        fun filterTradeOrders(currencyPair: CurrencyPair, startDate: LocalDateTime?, endDate: LocalDateTime?): Specification<TradeOrder> {
            return Specification<TradeOrder> { root: Root<TradeOrder>, _: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder ->
                val predicates = mutableListOf<Predicate>()

                currencyPair.let {
                    predicates.add(criteriaBuilder.equal(root.get<CurrencyPair>("currencyPair"), it))
                }

                // Add predicate for startDate if it's not null
                startDate?.let {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("tradedAt"), it))
                }

                // Add predicate for endDate if it's not null
                endDate?.let {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("tradedAt"), it))
                }

                // Return combined predicates; if there are no predicates, return true
                criteriaBuilder.and(*predicates.toTypedArray()).takeIf { predicates.isNotEmpty() } ?: criteriaBuilder.conjunction()
            }
        }
    }
}