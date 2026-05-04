package com.hcmut.roombookingbe.controllers;

import com.hcmut.roombookingbe.dtos.RoomDTO;
import com.hcmut.roombookingbe.dtos.RoomDetailDTO;
import com.hcmut.roombookingbe.services.RoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;

  @GetMapping
  public ResponseEntity<List<RoomDTO>> getAllRooms() {
    return ResponseEntity.ok(roomService.getAllRooms());
  }

  @GetMapping("/{id}")
  public ResponseEntity<RoomDetailDTO> getRoomDetail(@PathVariable Long id) {
    return ResponseEntity.ok(roomService.getRoomDetail(id));
  }

  @PostMapping
  public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO) {
    return ResponseEntity.ok(roomService.createRoom(roomDTO));
  }
}
