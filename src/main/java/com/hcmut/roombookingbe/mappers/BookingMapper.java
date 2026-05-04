package com.hcmut.roombookingbe.mappers;

import com.hcmut.roombookingbe.dtos.BookingDTO;
import com.hcmut.roombookingbe.entities.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "reviewedBy", source = "reviewedBy.id")
    BookingDTO toBookingDTO(Booking booking);
}
