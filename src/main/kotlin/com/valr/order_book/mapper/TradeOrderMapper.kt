package com.valr.order_book.mapper

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.User
import com.valr.order_book.entity.enums.Status
import com.valr.order_book.model.OrderDto
import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto
import com.valr.order_book.repository.projection.OrderBookProjection
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import java.util.*


@Mapper(
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = [SideMapper::class, CurrencyPairMapper::class]
)
abstract class TradeOrderMapper {

    companion object {
        val INSTANCE: TradeOrderMapper = Mappers.getMapper(TradeOrderMapper::class.java)
    }

    fun internalToOrderDTO(tradeOrder: TradeOrder): OrderDto {
        return OrderDto(
            SideMapper.INSTANCE.internalToDto(tradeOrder.takerSide),
            tradeOrder.quantity,
            tradeOrder.price,
            CurrencyPairMapper.INSTANCE.internalToDto(tradeOrder.currencyPair)
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
            tradeOrder.orderDate
        )
    }

    fun requestToInternal(user: User, request: OrderRequestDto): TradeOrder {

        if (request.side == null) {
            throw RuntimeException("side not set")
        }

        if (request.pair == null) {
            throw RuntimeException("pair not set")
        }

        val tradeOrder = TradeOrder(
            sequenceId = null,
            id = UUID.randomUUID().toString(),
            takerSide = SideMapper.INSTANCE.dtoToInternal(request.side),
            quantity = request.quantity ?: 0.toBigDecimal(),
            price = request.price ?: 0.toBigDecimal(),
            quoteVolume = request.price?.times(request.quantity!!) ?: 0.toBigDecimal(),
            currencyPair = CurrencyPairMapper.INSTANCE.dtoToInternal(request.pair),
            status = Status.PLACED,
            user = user,
        )

        return tradeOrder
    }

    fun orderBookProjection(order: OrderBookProjection): OrderDto {
        return OrderDto(
            SideMapper.INSTANCE.internalToDto(order.getTakerSide()),
            order.getQuantity(),
            order.getPrice(),
            CurrencyPairMapper.INSTANCE.internalToDto(order.getCurrencyPair()),
            order.getOrderCount()
        )
    }

    abstract fun internalsToOrderDTOs(entities: List<TradeOrder>): List<OrderDto>
}
