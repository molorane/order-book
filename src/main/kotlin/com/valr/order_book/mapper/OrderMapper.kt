package com.valr.order_book.mapper

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.model.OrderDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers


@Mapper(
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = [SideMapper::class, CurrencyPairMapper::class]
)
abstract class OrderMapper {

    companion object {
        val INSTANCE: OrderMapper = Mappers.getMapper(OrderMapper::class.java)
    }

    fun internalToDto(tradeOrder: TradeOrder): OrderDto {
        return OrderDto(
            SideMapper.INSTANCE.internalToDto(tradeOrder.takerSide),
            tradeOrder.quantity,
            tradeOrder.price,
            CurrencyPairMapper.INSTANCE.internalToDto(tradeOrder.currencyPair)
        )
    }

    abstract fun internalsToDTOs(entities: List<TradeOrder>): List<OrderDto>
}
