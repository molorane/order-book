package com.valr.order_book.service.impl

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.mapper.CurrencyPairMapper
import com.valr.order_book.mapper.OrderMapper
import com.valr.order_book.mapper.TradeMapper
import com.valr.order_book.model.*
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.repository.specification.TradeSpecification
import com.valr.order_book.service.TradeService
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
class TradeServiceImpl(private val tradeRepository: TradeRepository) : TradeService {

    override fun tradeHistory(
        currencyPair: CurrencyPairDto,
        skip: Int?,
        limit: Int?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): List<TradeOrderDto> {
        val pageable = PageRequest.of(skip ?: 0, limit ?: 20)

        val specification: Specification<TradeOrder> = TradeSpecification.filterTradeOrders(
            CurrencyPairMapper.INSTANCE.dtoToInternal(currencyPair), startTime, endTime
        )

        val page = tradeRepository.findAll(specification, pageable)

        return TradeMapper.INSTANCE.internalsToDTOs(
            page.content
        )
    }

    override fun orderBook(currencyPair: CurrencyPairDto): OrderBookDto {
        val orderBook = tradeRepository.findAllByCurrencyPair(
            CurrencyPairMapper.INSTANCE.dtoToInternal(currencyPair)
        )
            .stream()
            .map { trade -> OrderMapper.INSTANCE.internalToDto(trade) }
            .collect(Collectors.groupingBy(OrderDto::side))

        val asks = orderBook[SideDto.SELL]?.toMutableList()
        val bids = orderBook[SideDto.BUY]?.toMutableList()

        asks?.sortWith(compareBy<OrderDto> { it.price })
        bids?.sortByDescending { it.price }

        return OrderBookDto(
            asks = asks,
            bids = bids,
            lastChange = LocalDateTime.now(),
        )
    }
}