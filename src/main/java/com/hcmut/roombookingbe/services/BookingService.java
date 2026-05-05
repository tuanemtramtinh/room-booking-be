package com.hcmut.roombookingbe.services;

import com.hcmut.roombookingbe.dtos.BookingDTO;
import com.hcmut.roombookingbe.dtos.CreateBookingRequestDTO;
import com.hcmut.roombookingbe.entities.Booking;
import com.hcmut.roombookingbe.entities.BookingHistory;
import com.hcmut.roombookingbe.entities.Room;
import com.hcmut.roombookingbe.entities.User;
import com.hcmut.roombookingbe.enums.BookingStatus;
import com.hcmut.roombookingbe.mappers.BookingMapper;
import com.hcmut.roombookingbe.repositories.BookingHistoryRepository;
import com.hcmut.roombookingbe.repositories.BookingRepository;
import com.hcmut.roombookingbe.repositories.RoomRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;
  private final BookingHistoryRepository bookingHistoryRepository;
  private final RoomRepository roomRepository;
  private final BookingMapper bookingMapper;

  @Transactional
  public BookingDTO createBooking(CreateBookingRequestDTO request, User user) {
    validateBookingSchedule(request);

    Room room = roomRepository
      .findById(request.getRoomId())
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Room not found with id: " + request.getRoomId()
        )
      );

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

    if (
      bookingRepository.existsConflict(
        room.getId(),
        request.getStartDate(),
        request.getStartHour().minusMinutes(15),
        request.getEndHour().plusMinutes(15)
      )
    ) {
      throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        "Room is not available in this time slot"
      );
    }

    Booking savedBooking = bookingRepository.save(booking);
    saveHistory(savedBooking, null, BookingStatus.PENDING, user, null);

    return bookingMapper.toBookingDTO(savedBooking);
  }

  public List<BookingDTO> getBookings(BookingStatus status) {
    List<Booking> bookings =
      status != null
        ? bookingRepository.findByStatusOrderByCreatedAtDesc(status)
        : bookingRepository.findAllByOrderByCreatedAtDesc();
    return bookings.stream().map(bookingMapper::toBookingDTO).toList();
  }

  @Transactional
  public BookingDTO approveBooking(Long bookingId, User admin) {
    Booking booking = getBookingOrThrow(bookingId);

    if (booking.getStatus() != BookingStatus.PENDING) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Only PENDING bookings can be approved"
      );
    }

    BookingStatus previous = booking.getStatus();
    booking.setStatus(BookingStatus.APPROVED);
    booking.setReviewedBy(admin);
    booking.setReviewedAt(Instant.now());

    Booking saved = bookingRepository.save(booking);
    saveHistory(saved, previous, BookingStatus.APPROVED, admin, null);

    return bookingMapper.toBookingDTO(saved);
  }

  @Transactional
  public BookingDTO rejectBooking(
    Long bookingId,
    User admin,
    String rejectReason
  ) {
    Booking booking = getBookingOrThrow(bookingId);

    if (booking.getStatus() != BookingStatus.PENDING) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Only PENDING bookings can be rejected"
      );
    }

    BookingStatus previous = booking.getStatus();
    booking.setStatus(BookingStatus.REJECTED);
    booking.setReviewedBy(admin);
    booking.setReviewedAt(Instant.now());
    booking.setRejectReason(rejectReason);

    Booking saved = bookingRepository.save(booking);
    saveHistory(saved, previous, BookingStatus.REJECTED, admin, rejectReason);

    return bookingMapper.toBookingDTO(saved);
  }

  public List<BookingDTO> getBookingsByUser(Long userId) {
    return bookingRepository
      .findByUser_IdOrderByCreatedAtDesc(userId)
      .stream()
      .map(bookingMapper::toBookingDTO)
      .toList();
  }

  private Booking getBookingOrThrow(Long bookingId) {
    return bookingRepository
      .findById(bookingId)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Booking not found with id: " + bookingId
        )
      );
  }

  private void saveHistory(
    Booking booking,
    BookingStatus from,
    BookingStatus to,
    User changedBy,
    String note
  ) {
    BookingHistory history = new BookingHistory();
    history.setBooking(booking);
    history.setFromStatus(from);
    history.setToStatus(to);
    history.setChangedBy(changedBy);
    history.setNote(note);
    bookingHistoryRepository.save(history);
  }

  private void validateBookingSchedule(CreateBookingRequestDTO request) {
    if (!request.getStartHour().isBefore(request.getEndHour())) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "startHour must be before endHour"
      );
    }
  }
}
