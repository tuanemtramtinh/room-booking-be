package com.hcmut.roombookingbe.repositories;

import com.hcmut.roombookingbe.entities.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {
    List<BookingHistory> findByBooking_User_IdOrderByChangedAtDesc(Long userId);
}
