package com.hcmut.roombookingbe.mappers;

import com.hcmut.roombookingbe.dtos.BookingHistoryDTO;
import com.hcmut.roombookingbe.entities.BookingHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingHistoryMapper {

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "changedBy", source = "changedBy.id")
    BookingHistoryDTO toDTO(BookingHistory bookingHistory);
}
