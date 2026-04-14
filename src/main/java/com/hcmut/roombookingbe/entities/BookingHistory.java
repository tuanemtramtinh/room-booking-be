package com.hcmut.roombookingbe.entities;

import com.hcmut.roombookingbe.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "booking_history")
@Getter
@Setter
@NoArgsConstructor
public class BookingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private BookingStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status")
    private BookingStatus toStatus;

    private String note;

    @Column(name = "changed_at", updatable = false)
    private Instant changedAt = Instant.now();
}