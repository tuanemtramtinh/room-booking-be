package com.hcmut.roombookingbe.mappers;

import com.hcmut.roombookingbe.dtos.BookingDTO;
import com.hcmut.roombookingbe.entities.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface BookingMapper {
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "roomId", source = "room.id")
  @Mapping(target = "reviewedBy", source = "reviewedBy.id")
  @Mapping(target = "requester", source = "user")
  BookingDTO toBookingDTO(Booking booking);
}
