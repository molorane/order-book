package com.valr.order_book.repository.impl

import com.valr.order_book.entity.Trade
import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.mapper.CurrencyPairMapper
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.OrderDto
import com.valr.order_book.repository.OrderRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Repository
class OrderRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : OrderRepository {

    override fun tradeHistory(
        currencyPair: CurrencyPairDto,
        skip: Int,
        limit: Int,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): List<OrderDto> {
        val pageable = PageRequest.of(skip, limit)

        val pair: CurrencyPair = CurrencyPairMapper.INSTANCE.dtoToInternal(currencyPair)

        val cb = entityManager.criteriaBuilder
        val query = cb.createQuery(OrderDto::class.java)
        val order = query.from(TradeOrder::class.java)

        // Join with the Trade table
        val trade = order.join<TradeOrder, Trade>("trades", JoinType.LEFT)

        // Aggregate functions: Count trades and sum quantities
        val tradeCount: Expression<Long> = cb.count(trade) // Count the trades for each order


        // Select clause: Project aggregated data into OrderDto
        query.select(
            cb.construct(
                OrderDto::class.java,
                order.get<Any>("sequenceId"),
                tradeCount
            )
        )

        // WHERE clause: Optional filters (only applied if not null)
        val predicates = mutableListOf<Predicate>()

        predicates.add(cb.equal(order.get<CurrencyPair>("currencyPair"), pair))

        // Add predicate for startDate if it's not null
        startTime?.let {
            predicates.add(cb.greaterThanOrEqualTo(order.get("tradedAt"), it))
        }

        // Add predicate for endDate if it's not null
        endTime?.let {
            predicates.add(cb.lessThanOrEqualTo(order.get("tradedAt"), it))
        }

        // Apply filters (predicates)
        if (predicates.isNotEmpty()) {
            query.where(cb.and(*predicates.toTypedArray()))
        }

        // Group by symbol and order ID to perform aggregation
        query.groupBy(order.get<Long>("id"));

        // Apply pagination (limit/offset)
        val typedQuery = entityManager.createQuery(query)
        typedQuery.setFirstResult(pageable.offset.toInt())
        typedQuery.setMaxResults(pageable.pageSize)

        // Fetch total count for pagination
        val countQuery = cb.createQuery(Long::class.java)
        val countRoot = countQuery.from(TradeOrder::class.java)
        countQuery.select(cb.count(countRoot))
        countQuery.where(cb.and(*predicates.toTypedArray()))

        return typedQuery.resultList
    }
}