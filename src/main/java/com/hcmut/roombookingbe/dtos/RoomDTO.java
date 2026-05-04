package com.hcmut.roombookingbe.dtos;

import com.hcmut.roombookingbe.enums.RoomStatus;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class RoomDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private String description;
    private RoomStatus status;
}
