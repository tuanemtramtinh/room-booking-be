package com.hcmut.roombookingbe.controllers;

import com.hcmut.roombookingbe.dtos.request.GoogleLoginRequest;
import com.hcmut.roombookingbe.dtos.response.AuthResponse;
import com.hcmut.roombookingbe.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request.idToken()));
    }
}
