package com.valr.order_book.mapper

import com.valr.order_book.entity.enums.OrderType
import com.valr.order_book.model.OrderTypeDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
interface OrderTypeMapper {
    companion object {
        val INSTANCE: OrderTypeMapper = Mappers.getMapper(OrderTypeMapper::class.java)
    }

    fun internalToDto(orderType: OrderType): OrderTypeDto

    fun dtoToInternal(orderType: OrderTypeDto): OrderType
}