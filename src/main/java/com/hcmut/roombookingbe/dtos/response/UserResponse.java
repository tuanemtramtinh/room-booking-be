package com.hcmut.roombookingbe.dtos.response;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String avatarUrl,
        String role,
        String status
) {}
