package com.example.SKALA_Mini_Project_1.Payments.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "seat_id")
    private Long seatId;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expired_at")
    private OffsetDateTime expiredAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;
}
