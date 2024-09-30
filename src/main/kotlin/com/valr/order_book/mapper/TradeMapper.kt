package com.valr.order_book.mapper

import com.valr.order_book.model.TradeDto
import com.valr.order_book.repository.projection.TradeHistoryProjection
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers


@Mapper(
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = [SideMapper::class, CurrencyPairMapper::class, StatusMapper::class, OrderTypeMapper::class]
)
abstract class TradeMapper {

    companion object {
        val INSTANCE: TradeMapper = Mappers.getMapper(TradeMapper::class.java)
    }

    fun tradeHistory(trade: TradeHistoryProjection): TradeDto {
        return TradeDto(
            trade.getPrice(),
            trade.getQuantity(),
            CurrencyPairMapper.INSTANCE.internalToDto(trade.getCurrencyPair()),
            trade.getTradedAt(),
            SideMapper.INSTANCE.internalToDto(trade.getTakerSide()),
            trade.getSequenceId(),
            trade.getId(),
            trade.getQuoteVolume().setScale(8)
        )
    }

    abstract fun internalsToDTOs(entities: List<TradeHistoryProjection>): List<TradeDto>
}
