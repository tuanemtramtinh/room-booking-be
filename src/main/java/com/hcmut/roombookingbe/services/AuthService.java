package com.hcmut.roombookingbe.services;

import com.hcmut.roombookingbe.dtos.internal.GoogleTokenPayload;
import com.hcmut.roombookingbe.dtos.response.AuthResponse;
import com.hcmut.roombookingbe.entities.User;
import com.hcmut.roombookingbe.enums.Role;
import com.hcmut.roombookingbe.enums.UserStatus;
import com.hcmut.roombookingbe.mappers.UserMapper;
import com.hcmut.roombookingbe.repositories.UserRepository;
import com.hcmut.roombookingbe.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

  @Value("${google.client-id}")
  private String googleClientId;

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final RestClient restClient = RestClient.create();

  public AuthResponse loginWithGoogle(String idToken) {
    GoogleTokenPayload payload = verifyGoogleToken(idToken);

    User user = userRepository
      .findByGoogleId(payload.sub())
      .orElseGet(() ->
        userRepository
          .findByEmail(payload.email())
          .map(existing -> {
            existing.setGoogleId(payload.sub());
            existing.setAvatarUrl(payload.picture());
            return userRepository.save(existing);
          })
          .orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(payload.email());
            newUser.setFullName(payload.name());
            newUser.setAvatarUrl(payload.picture());
            newUser.setGoogleId(payload.sub());
            return userRepository.save(newUser);
          })
      );

    if (user.getStatus() == UserStatus.INACTIVE) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Account is inactive"
      );
    }

    String token = jwtService.generateToken(user);
    return new AuthResponse(token, "Bearer", userMapper.toUserResponse(user));
  }

  public AuthResponse loginAsAdmin(String email, String password) {
    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.UNAUTHORIZED,
          "Invalid credentials"
        )
      );

    if (user.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    if (
      user.getPassword() == null ||
      !passwordEncoder.matches(password, user.getPassword())
    ) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Invalid credentials"
      );
    }

    if (user.getStatus() == UserStatus.INACTIVE) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Account is inactive"
      );
    }

    String token = jwtService.generateToken(user);
    return new AuthResponse(token, "Bearer", userMapper.toUserResponse(user));
  }

  private GoogleTokenPayload verifyGoogleToken(String idToken) {
    try {
      GoogleTokenPayload payload = restClient
        .get()
        .uri("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken)
        .retrieve()
        .body(GoogleTokenPayload.class);

      if (payload == null || !googleClientId.equals(payload.aud())) {
        throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED,
          "Invalid Google token"
        );
      }
      return payload;
    } catch (RestClientException e) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Invalid Google token"
      );
    }
  }
}
