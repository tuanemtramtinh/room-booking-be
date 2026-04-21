package com.hcmut.roombookingbe.services;

import com.hcmut.roombookingbe.dtos.RoomDTO;
import com.hcmut.roombookingbe.entities.Room;
import com.hcmut.roombookingbe.mappers.RoomMapper;
import com.hcmut.roombookingbe.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toRoomDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO createRoom(RoomDTO roomDTO) {
        Room room = roomMapper.toRoom(roomDTO);
        Room savedRoom = roomRepository.save(room);
        return roomMapper.toRoomDTO(savedRoom);
    }
}
