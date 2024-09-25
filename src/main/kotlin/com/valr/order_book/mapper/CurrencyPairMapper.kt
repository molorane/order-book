package com.valr.order_book.mapper

import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.model.CurrencyPairDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
interface CurrencyPairMapper {
    companion object {
        val INSTANCE: CurrencyPairMapper = Mappers.getMapper(CurrencyPairMapper::class.java)
    }

    fun internalToDto(currencyPair: CurrencyPair): CurrencyPairDto

    fun dtoToInternal(currencyPair: CurrencyPairDto): CurrencyPair
}