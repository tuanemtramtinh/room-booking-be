package com.hcmut.roombookingbe.services;

import com.hcmut.roombookingbe.dtos.request.UpdateUserRequest;
import com.hcmut.roombookingbe.dtos.response.UserResponse;
import com.hcmut.roombookingbe.entities.User;
import com.hcmut.roombookingbe.enums.Role;
import com.hcmut.roombookingbe.enums.UserStatus;
import com.hcmut.roombookingbe.mappers.UserMapper;
import com.hcmut.roombookingbe.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> getUsers(String role, String status, String keyword) {
        Role roleEnum = null;
        if (role != null && !role.isBlank()) {
            try {
                roleEnum = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + role);
            }
        }

        UserStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
            }
        }

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findUsersWithFilters(
                        currentUser.getId(),
                        roleEnum != null ? roleEnum.name() : null,
                        statusEnum != null ? statusEnum.name() : null,
                        kw)
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        user.setFullName(request.fullName());
        return userMapper.toUserResponse(userRepository.save(user));
    }
}
