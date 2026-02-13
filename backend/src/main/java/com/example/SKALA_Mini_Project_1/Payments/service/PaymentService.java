// 결제 상태를 전이표에 맞게만 변경하도록 강제하는 서비스

package com.example.SKALA_Mini_Project_1.Payments.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentCreateRequest;
import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentCreateResponse;
import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentGetResponse;
import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentSubmitResponse;
import com.example.SKALA_Mini_Project_1.Payments.domain.Payment;
import com.example.SKALA_Mini_Project_1.Payments.domain.PaymentStatus;
import com.example.SKALA_Mini_Project_1.Payments.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private static final Map<PaymentStatus, Set<PaymentStatus>> transitionMap = new EnumMap<>(PaymentStatus.class);


        /**
     * 결제 생성: PENDING + expiredAt = now + 5분
     */
    // Create
    @Transactional
    public PaymentCreateResponse createPayment(PaymentCreateRequest req) {
        if (req.getBookingId() == null) {
            throw new IllegalArgumentException("bookingId is required");
        }

        // // Booking 관련이 없어서 검증은 일단 주석처리
        // // 운영 기준: booking 존재 검증
        // if (!bookingRepository.existsById(req.getBookingId())) {
        //     throw new EntityNotFoundException("Booking not found: " + req.getBookingId());
        // }

        // ✅ booking_id UNIQUE 대응: 이미 결제가 있으면 새로 만들지 말고 그대로 반환
        return paymentRepository.findByBookingId(req.getBookingId())
            .map(existing -> new PaymentCreateResponse(
                    existing.getId(),
                    existing.getStatus(),
                    existing.getCreatedAt(),
                    existing.getExpiredAt()
            ))
            .orElseGet(() -> {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

                Payment payment = new Payment();
                payment.setBookingId(req.getBookingId());
                payment.setUserId(req.getUserId());
                payment.setSeatId(req.getSeatId());
                payment.setAmount(req.getAmount());

                payment.setStatus(PaymentStatus.PENDING);
                payment.setCreatedAt(now);
                payment.setUpdatedAt(now);
                payment.setExpiredAt(now.plusMinutes(5)); // pending 5분
                payment.setIdempotencyKey(null);

                Payment saved = paymentRepository.save(payment);

                return new PaymentCreateResponse(
                        saved.getId(),
                        saved.getStatus(),
                        saved.getCreatedAt(),
                        saved.getExpiredAt()
                );
            });
    }

    /**
     * 결제 단건 조회
     */
    // Get
    @Transactional(readOnly = true)
    public PaymentGetResponse getPayment(UUID paymentId) {
        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));

        return new PaymentGetResponse(
                p.getId(),
                p.getBookingId(),
                p.getUserId(),
                p.getSeatId(),
                p.getAmount(),
                p.getStatus().name(),
                p.getCreatedAt(),
                p.getExpiredAt(),
                p.getUpdatedAt(),
                p.getIdempotencyKey()
        );
    }

    @Transactional
    public PaymentSubmitResponse submit(UUID paymentId) {
    Payment payment = paymentRepository.findByIdForUpdate(paymentId)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));

    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    // ✅ 엔티티 전이 규칙 사용
    payment.changeStatus(PaymentStatus.PAYING);

    // expired_at +3분 연장
    OffsetDateTime base = payment.getExpiredAt();
    OffsetDateTime effective = (base == null || base.isBefore(now)) ? now : base;
    payment.setExpiredAt(effective.plusMinutes(3));

    payment.setIdempotencyKey(UUID.randomUUID().toString());
    payment.setUpdatedAt(now);

    // save는 선택(팀 스타일이면 유지)
    paymentRepository.save(payment);

    return PaymentSubmitResponse.from(payment);
    }
}