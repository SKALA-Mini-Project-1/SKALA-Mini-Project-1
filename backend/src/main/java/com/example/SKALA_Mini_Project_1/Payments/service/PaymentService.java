// 결제 상태를 전이표에 맞게만 변경하도록 강제하는 서비스

package com.example.SKALA_Mini_Project_1.Payments.service;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 허용된 상태 전이만 정의하는 전이 맵
    private static final Map<PaymentStatus, Set<PaymentStatus>> transitionMap = new EnumMap<>(PaymentStatus.class);

    static {
        transitionMap.put(PaymentStatus.PENDING,
                Set.of(PaymentStatus.PAYING, PaymentStatus.CANCELED, PaymentStatus.EXPIRED));

        transitionMap.put(PaymentStatus.PAYING,
                Set.of(PaymentStatus.PAID, PaymentStatus.FAILED, PaymentStatus.CANCELED, PaymentStatus.EXPIRED));

        transitionMap.put(PaymentStatus.PAID,
                Set.of(PaymentStatus.CONFIRMED));

        transitionMap.put(PaymentStatus.EXPIRED,
                Set.of(PaymentStatus.REFUND_REQUIRED));
    }

    // 현재 상태에서 다음 상태로 전이가 가능한지 확인한다
    private boolean canTransition(PaymentStatus from, PaymentStatus to) {
        return transitionMap.containsKey(from)
                && transitionMap.get(from).contains(to);
    }

    // 결제 상태를 비관락과 트랜잭션으로 안전하게 변경한다
    @Transactional
    public void changeStatus(UUID paymentId, PaymentStatus newStatus) {

        // 비관락으로 조회하여 동시에 수정되지 않도록 한다
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));


        PaymentStatus currentStatus = payment.getStatus();

        // 허용되지 않은 상태 전이면 예외를 발생시킨다
        if (!canTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + currentStatus + " → " + newStatus);
        }

        // 상태를 변경한다
        payment.setStatus(newStatus);

        // 상태 변경 시간을 기록한다
        payment.setUpdatedAt(OffsetDateTime.now());

        paymentRepository.save(payment);
    }

    @Transactional
    public PaymentSubmitResponse submit(UUID paymentId) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));

        // 1) 상태 검증 + 전이 (changeStatus 메서드 대신 직접 검증)
        changeStatus(paymentId, PaymentStatus.PAYING); // 기존 메서드 사용

        // 2) expired_at +3분 연장
        OffsetDateTime now = OffsetDateTime.now(); // LocalDateTime -> OffsetDateTime
        OffsetDateTime base = payment.getExpiredAt();
        
        OffsetDateTime effective = (base == null || base.isBefore(now)) ? now : base;
        payment.setExpiredAt(effective.plusMinutes(3));

        // 3) idempotencyKey 생성
        payment.setIdempotencyKey(UUID.randomUUID().toString());

        // 4) updatedAt 갱신
        payment.setUpdatedAt(now); // now도 OffsetDateTime으로

        // 5) 저장
        paymentRepository.save(payment);

        return PaymentSubmitResponse.from(payment);
    }
}
