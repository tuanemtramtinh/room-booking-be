package com.hcmut.roombookingbe.dtos;

import com.hcmut.roombookingbe.dtos.response.UserResponse;
import com.hcmut.roombookingbe.enums.BookingStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

  private Long id;
  private Long userId;
  private UserResponse requester;
  private Long roomId;
  private String title;
  private String description;
  private Integer attendeeCount;
  private LocalDate startDate;
  private LocalTime startHour;
  private LocalTime endHour;
  private BookingStatus status;
  private String rejectReason;
  private Instant reviewedAt;
  private Long reviewedBy;
  private Instant createdAt;
  private Instant updatedAt;
}
