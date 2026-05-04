package com.hcmut.roombookingbe.entities;

import com.hcmut.roombookingbe.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String location;

  private Integer capacity;

  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoomStatus status = RoomStatus.AVAILABLE;
}
