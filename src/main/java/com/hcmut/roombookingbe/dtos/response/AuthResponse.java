package com.hcmut.roombookingbe.dtos.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {}
