package com.example.SKALA_Mini_Project_1.Payments.controller.dto;

import com.example.SKALA_Mini_Project_1.Payments.domain.Payment;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter
@AllArgsConstructor
public class PaymentSubmitResponse {

    private UUID id;
    private String status;
    private OffsetDateTime expiredAt;
    private String idempotencyKey;
    private OffsetDateTime updatedAt;

    public static PaymentSubmitResponse from(Payment p) {
        return new PaymentSubmitResponse(
                p.getId(),
                p.getStatus().name(),
                p.getExpiredAt(),
                p.getIdempotencyKey(),
                p.getUpdatedAt()
        );
    }
}
