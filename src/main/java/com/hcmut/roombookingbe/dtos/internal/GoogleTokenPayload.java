package com.hcmut.roombookingbe.dtos.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenPayload(
        String sub,
        String aud,
        String email,
        String name,
        String picture,
        @JsonProperty("email_verified") String emailVerified
) {}
