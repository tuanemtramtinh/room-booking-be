package com.hcmut.roombookingbe.dtos;

import com.hcmut.roombookingbe.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailDTO {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private String description;
    private RoomStatus status;
    private List<BookingDTO> bookings;
}
