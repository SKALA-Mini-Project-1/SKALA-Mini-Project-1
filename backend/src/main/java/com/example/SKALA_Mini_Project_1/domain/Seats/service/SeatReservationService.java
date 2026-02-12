package com.example.SKALA_Mini_Project_1.domain.Seats.service;

import com.example.SKALA_Mini_Project_1.domain.Seats.entity.Seat;
import com.example.SKALA_Mini_Project_1.domain.Seats.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatReservationService {

    private final RedissonClient redissonClient;
    private final SeatRepository seatRepository;

    private static final String LOCK_KEY_PREFIX = "LOCK:SEAT:";
    private static final int HOLD_DURATION_MINUTES = 5;

    /**
     * 좌석 선점 (임시 예약)
     * 분산 락을 사용하여 동일 좌석에 대한 중복 요청을 제어합니다.
     */
    @Transactional
    public boolean reserveSeatTemporary(Long seatId, Long userId) {
        final String lockKey = LOCK_KEY_PREFIX + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 1. 락 획득 시도 (최대 3초 대기, 획득 시 10초간 점유)
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                log.info("좌석 {}에 대한 락 획득 실패 - 사용자가 많음", seatId);
                return false;
            }

            // 2. 비즈니스 로직 (DB 트랜잭션 내에서 수행)
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new EntityNotFoundException("좌석이 존재하지 않습니다. ID: " + seatId));

            try {
                // 엔티티 내부 로직을 통해 상태 변경 가능 여부 검증 및 변경
                seat.hold(userId, HOLD_DURATION_MINUTES);
                seatRepository.save(seat);
                log.info("좌석 {} 선점 성공 (사용자: {})", seatId, userId);
                return true;
            } catch (IllegalStateException e) {
                log.warn("좌석 {} 선점 실패: {}", seatId, e.getMessage());
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("락 획득 중 인터럽트 발생", e);
            return false;
        } finally {
            // 3. 락 해제 (현재 스레드가 락을 가지고 있는 경우만)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
