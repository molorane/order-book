package com.valr.order_book.mapper

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.Status
import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto
import com.valr.order_book.model.TradeOrderDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import java.util.*


@Mapper(
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = [SideMapper::class, CurrencyPairMapper::class, StatusMapper::class, OrderTypeMapper::class]
)
abstract class TradeMapper {

    companion object {
        val INSTANCE: TradeMapper = Mappers.getMapper(TradeMapper::class.java)
    }

    fun internalToDto(tradeOrder: TradeOrder): TradeOrderDto {
        return TradeOrderDto(
            tradeOrder.sequenceId,
            tradeOrder.id,
            SideMapper.INSTANCE.internalToDto(tradeOrder.takerSide),
            tradeOrder.quantity,
            tradeOrder.price,
            tradeOrder.quoteVolume,
            CurrencyPairMapper.INSTANCE.internalToDto(tradeOrder.currencyPair),
            tradeOrder.tradedAt
        )
    }

    fun internalToOrderResponse(tradeOrder: TradeOrder): OrderResponseDto {
        return OrderResponseDto(
            tradeOrder.sequenceId,
            tradeOrder.id,
            SideMapper.INSTANCE.internalToDto(tradeOrder.takerSide),
            tradeOrder.quantity,
            tradeOrder.price,
            tradeOrder.quoteVolume,
            CurrencyPairMapper.INSTANCE.internalToDto(tradeOrder.currencyPair),
            StatusMapper.INSTANCE.internalToDto(tradeOrder.status),
            OrderTypeMapper.INSTANCE.internalToDto(tradeOrder.orderType),
            tradeOrder.tradedAt
        )
    }

    fun requestToInternal(request: OrderRequestDto): TradeOrder {

        if (request.side == null) {
            throw RuntimeException("side not set")
        }

        if (request.pair == null) {
            throw RuntimeException("pair not set")
        }

        val tradeOrder = TradeOrder(
            id = UUID.randomUUID().toString(),
            takerSide = SideMapper.INSTANCE.dtoToInternal(request.side),
            quantity = request.quantity ?: 0.toBigDecimal(),
            price = request.price ?: 0.toBigDecimal(),
            quoteVolume = request.price?.times(request.quantity!!) ?: 0.toBigDecimal(),
            currencyPair = CurrencyPairMapper.INSTANCE.dtoToInternal(request.pair),
            status = Status.PLACED
        )

        return tradeOrder
    }

    abstract fun internalsToDTOs(entities: List<TradeOrder>): List<TradeOrderDto>
}
