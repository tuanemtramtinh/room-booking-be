package com.hcmut.roombookingbe.mappers;

import com.hcmut.roombookingbe.dtos.RoomDTO;
import com.hcmut.roombookingbe.entities.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {
  RoomDTO toRoomDTO(Room room);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Room toRoom(RoomDTO roomDTO);
}
