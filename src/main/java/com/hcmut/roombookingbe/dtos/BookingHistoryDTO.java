package com.hcmut.roombookingbe.dtos;

import com.hcmut.roombookingbe.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryDTO {
    private Long id;
    private Long bookingId;
    private BookingStatus fromStatus;
    private BookingStatus toStatus;
    private Long changedBy;
    private String note;
    private Instant changedAt;
}
