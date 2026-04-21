package com.hcmut.roombookingbe.repositories;

import com.hcmut.roombookingbe.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
