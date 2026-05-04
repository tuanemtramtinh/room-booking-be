package com.hcmut.roombookingbe.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateBookingRequestDTO {

    @NotNull
    private Long userId;

    @NotNull
    private Long roomId;

    @NotBlank
    private String title;

    private String description;

    @Min(1)
    private Integer attendeeCount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalTime startHour;

    @NotNull
    private LocalTime endHour;
}
