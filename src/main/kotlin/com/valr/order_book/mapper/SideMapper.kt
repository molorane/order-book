package com.valr.order_book.mapper

import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.model.SideDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
interface SideMapper {
    companion object {
        val INSTANCE: SideMapper = Mappers.getMapper(SideMapper::class.java)
    }

    fun internalToDto(takerSide: TakerSide): SideDto

    fun dtoToInternal(side: SideDto): TakerSide
}