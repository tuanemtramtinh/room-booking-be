package com.hcmut.roombookingbe.repositories;

import com.hcmut.roombookingbe.entities.Booking;
import com.hcmut.roombookingbe.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByRoom_IdOrderByStartDateAscStartHourAsc(Long roomId);

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.room.id = :roomId
          AND b.startDate = :startDate
          AND b.status = 'APPROVED'
          AND b.startHour < :endHour
          AND b.endHour > :startHour
    """)
    boolean existsConflict(
        @Param("roomId") Long roomId,
        @Param("startDate") LocalDate startDate,
        @Param("startHour") LocalTime startHour,
        @Param("endHour") LocalTime endHour
    );
}
