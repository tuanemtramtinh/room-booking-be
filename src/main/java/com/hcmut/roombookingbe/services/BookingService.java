package com.hcmut.roombookingbe.services;

import com.hcmut.roombookingbe.dtos.BookingDTO;
import com.hcmut.roombookingbe.dtos.CreateBookingRequestDTO;
import com.hcmut.roombookingbe.entities.Booking;
import com.hcmut.roombookingbe.entities.Room;
import com.hcmut.roombookingbe.entities.User;
import com.hcmut.roombookingbe.enums.BookingStatus;
import com.hcmut.roombookingbe.repositories.BookingRepository;
import com.hcmut.roombookingbe.repositories.RoomRepository;
import com.hcmut.roombookingbe.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public BookingDTO createBooking(CreateBookingRequestDTO request) {
        validateBookingSchedule(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + request.getUserId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + request.getRoomId()));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setTitle(request.getTitle().trim());
        booking.setDescription(request.getDescription());
        booking.setAttendeeCount(request.getAttendeeCount());
        booking.setStartDate(request.getStartDate());
        booking.setStartHour(request.getStartHour());
        booking.setEndHour(request.getEndHour());
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        return toDTO(savedBooking);
    }

    private void validateBookingSchedule(CreateBookingRequestDTO request) {
        if (!request.getStartHour().isBefore(request.getEndHour())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "startHour must be before endHour");
        }
    }

    private BookingDTO toDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .roomId(booking.getRoom().getId())
                .title(booking.getTitle())
                .description(booking.getDescription())
                .attendeeCount(booking.getAttendeeCount())
                .startDate(booking.getStartDate())
                .startHour(booking.getStartHour())
                .endHour(booking.getEndHour())
                .status(booking.getStatus())
                .rejectReason(booking.getRejectReason())
                .reviewedAt(booking.getReviewedAt())
                .reviewedBy(booking.getReviewedBy() != null ? booking.getReviewedBy().getId() : null)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
