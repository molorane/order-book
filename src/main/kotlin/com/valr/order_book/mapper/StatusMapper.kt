package com.valr.order_book.mapper

import com.valr.order_book.entity.enums.Status
import com.valr.order_book.model.StatusDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
interface StatusMapper {
    companion object {
        val INSTANCE: StatusMapper = Mappers.getMapper(StatusMapper::class.java)
    }

    fun internalToDto(status: Status): StatusDto

    fun dtoToInternal(status: StatusDto): Status
}