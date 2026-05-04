package com.hcmut.roombookingbe.repositories;

import com.hcmut.roombookingbe.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
