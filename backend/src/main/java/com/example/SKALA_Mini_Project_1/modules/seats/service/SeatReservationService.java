package com.example.SKALA_Mini_Project_1.modules.seats.service;

import com.example.SKALA_Mini_Project_1.global.redis.RedisLockRepository;
import com.example.SKALA_Mini_Project_1.modules.seats.domain.Seat;
import com.example.SKALA_Mini_Project_1.modules.seats.domain.SeatStatus;
import com.example.SKALA_Mini_Project_1.modules.seats.repository.SeatRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatReservationService {
    public enum SeatHoldResult {
        HELD,
        RELEASED
    }

    private final SeatRepository seatRepository;
    private final RedisLockRepository redisLockRepository;

    /**
     * 좌석 선점 (임시 예약)
     * 좌석 HOLD 상태는 Redis에만 저장하고, DB는 AVAILABLE/RESERVED만 유지합니다.
     */
    public SeatHoldResult reserveSeatTemporary(
            Long concertId,
            Long seatId,
            String section,
            Integer rowNumber,
            Integer seatNumber,
            Long userId
    ) {
        Seat seat = validateSeat(seatId, section, rowNumber, seatNumber);
        if (seat.getStatus() == SeatStatus.RESERVED) {
            throw new IllegalStateException("이미 판매된 좌석입니다.");
        }

        // Redis를 통한 선점 시도 (HOLD는 Redis TTL로만 관리)
        boolean isLocked = redisLockRepository.lockSeat(concertId, section, rowNumber, seatNumber, userId.toString());
        
        if (!isLocked) {
            String currentOwner = redisLockRepository.getSeatOwner(concertId, section, rowNumber, seatNumber);
            if (Objects.equals(currentOwner, userId.toString())) {
                redisLockRepository.unlockSeat(concertId, section, rowNumber, seatNumber);
                log.info("좌석 {} 선점 해제 성공 (사용자: {})", seatId, userId);
                return SeatHoldResult.RELEASED;
            }

            log.info(
                    "좌석 {} - {}-{}-{} 선점 실패: 현재 소유자(userId: {}), 시도자(userId: {})",
                    concertId,
                    section,
                    rowNumber,
                    seatNumber,
                    currentOwner,
                    userId
            );
            throw new IllegalStateException("이미 다른 사용자가 선택한 좌석입니다.");
        }

        log.info("좌석 {} 선점 성공 (사용자: {})", seatId, userId);
        return SeatHoldResult.HELD;
    }

    private Seat validateSeat(
            Long seatId,
            String section,
            Integer rowNumber,
            Integer seatNumber
    ) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("좌석이 존재하지 않습니다. ID: " + seatId));

        if (!Objects.equals(seat.getSection(), section)
                || !Objects.equals(seat.getRowNumber(), rowNumber)
                || !Objects.equals(seat.getSeatNumber(), seatNumber)) {
            throw new IllegalArgumentException("요청한 좌석 정보가 seatId와 일치하지 않습니다.");
        }

        return seat;
    }
}
