package com.hcmut.roombookingbe.controllers;

import com.hcmut.roombookingbe.dtos.request.UpdateUserRequest;
import com.hcmut.roombookingbe.dtos.response.UserResponse;
import com.hcmut.roombookingbe.services.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<List<UserResponse>> getUsers(
    @RequestParam(required = false) String role,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String keyword
  ) {
    return ResponseEntity.ok(userService.getUsers(role, status, keyword));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
    @PathVariable Long id,
    @Valid @RequestBody UpdateUserRequest request
  ) {
    return ResponseEntity.ok(userService.updateUser(id, request));
  }
}
