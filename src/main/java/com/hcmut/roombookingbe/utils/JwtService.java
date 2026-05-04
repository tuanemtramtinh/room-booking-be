package com.hcmut.roombookingbe.utils;

import com.hcmut.roombookingbe.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  @Value("${app.jwt.secret}")
  private String secret;

  @Value("${app.jwt.expiration}")
  private long expiration;

  public String generateToken(User user) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
      .subject(user.getId().toString())
      .claim("email", user.getEmail())
      .claim("role", user.getRole().name())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expiration))
      .signWith(key)
      .compact();
  }

  public Claims extractClaims(String token) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parser()
      .verifyWith(key)
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  public boolean isTokenValid(String token) {
    try {
      extractClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
