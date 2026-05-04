package com.hcmut.roombookingbe.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String fullName
) {}
