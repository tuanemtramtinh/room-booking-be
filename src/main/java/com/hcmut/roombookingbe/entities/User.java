package com.hcmut.roombookingbe.entities;

import com.hcmut.roombookingbe.enums.Role;
import com.hcmut.roombookingbe.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "google_id", unique = true)
  private String googleId;

  @Column
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role = Role.STAFF;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status = UserStatus.ACTIVE;
}
