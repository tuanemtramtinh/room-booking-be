package com.hcmut.roombookingbe.services;

import com.hcmut.roombookingbe.dtos.BookingDTO;
import com.hcmut.roombookingbe.dtos.RoomDTO;
import com.hcmut.roombookingbe.dtos.RoomDetailDTO;
import com.hcmut.roombookingbe.entities.Room;
import com.hcmut.roombookingbe.mappers.BookingMapper;
import com.hcmut.roombookingbe.mappers.RoomMapper;
import com.hcmut.roombookingbe.repositories.BookingRepository;
import com.hcmut.roombookingbe.repositories.RoomRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;
  private final BookingRepository bookingRepository;
  private final RoomMapper roomMapper;
  private final BookingMapper bookingMapper;

  public List<RoomDTO> getAllRooms() {
    return roomRepository
      .findAll()
      .stream()
      .map(roomMapper::toRoomDTO)
      .collect(Collectors.toList());
  }

  public RoomDetailDTO getRoomDetail(Long id) {
    Room room = roomRepository
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found with id: " + id)
      );

    List<BookingDTO> bookings = bookingRepository
      .findByRoom_IdOrderByStartDateAscStartHourAsc(id)
      .stream()
      .map(bookingMapper::toBookingDTO)
      .collect(Collectors.toList());

    return RoomDetailDTO.builder()
      .id(room.getId())
      .name(room.getName())
      .location(room.getLocation())
      .capacity(room.getCapacity())
      .description(room.getDescription())
      .status(room.getStatus())
      .bookings(bookings)
      .build();
  }

  public RoomDTO createRoom(RoomDTO roomDTO) {
    Room room = roomMapper.toRoom(roomDTO);
    Room savedRoom = roomRepository.save(room);
    return roomMapper.toRoomDTO(savedRoom);
  }

  public RoomDTO updateRoom(Long id, RoomDTO roomDTO) {
    Room room = roomRepository
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found with id: " + id)
      );

    if (roomDTO.getName() != null) room.setName(roomDTO.getName());
    if (roomDTO.getLocation() != null) room.setLocation(roomDTO.getLocation());
    if (roomDTO.getCapacity() != null) room.setCapacity(roomDTO.getCapacity());
    if (roomDTO.getDescription() != null) room.setDescription(roomDTO.getDescription());
    if (roomDTO.getStatus() != null) room.setStatus(roomDTO.getStatus());

    return roomMapper.toRoomDTO(roomRepository.save(room));
  }
}
