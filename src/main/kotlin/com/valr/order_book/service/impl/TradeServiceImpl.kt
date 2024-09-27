package com.valr.order_book.service.impl

import com.valr.order_book.mapper.CurrencyPairMapper
import com.valr.order_book.mapper.TradeMapper
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.TradeDto
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.service.TradeService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TradeServiceImpl(private val tradeRepository: TradeRepository) : TradeService {

    override fun tradeHistory(
        currencyPair: CurrencyPairDto,
        skip: Int?,
        limit: Int?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): List<TradeDto> {
        val pageable = PageRequest.of(skip ?: 0, limit ?: 20)

        val page = tradeRepository.tradeHistory(
            CurrencyPairMapper.INSTANCE.dtoToInternal(currencyPair),
            startTime,
            endTime,
            pageable
        )

        return TradeMapper.INSTANCE.internalsToDTOs(
            page.content
        )
    }
}