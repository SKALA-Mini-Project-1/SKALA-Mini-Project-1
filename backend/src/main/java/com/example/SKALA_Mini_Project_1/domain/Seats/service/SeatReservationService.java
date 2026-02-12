package com.example.SKALA_Mini_Project_1.domain.Seats.service;

import com.example.SKALA_Mini_Project_1.domain.Seats.entity.Seat;
import com.example.SKALA_Mini_Project_1.domain.Seats.repository.SeatRepository;
import com.example.SKALA_Mini_Project_1.global.redis.RedisLockRepository;

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

    private final RedissonClient redissonClient; // 좌석 선택 시 중복 예약을 막기 위한 분산 락
    private final SeatRepository seatRepository;
    private final RedisLockRepository redisLockRepository;

    private static final String LOCK_KEY_PREFIX = "LOCK:SEAT:"; // seat_id로 락 획득
    private static final int HOLD_DURATION_MINUTES = 5;

    /**
     * 좌석 선점 (임시 예약)
     * Redis를 통해 선점 상태를 관리하고, DB에도 반영합니다.
     */
    // @Transactional
    public boolean reserveSeatTemporary(Long concertId, Long seatId, String seatNumber, Long userId) {
        // 1. Redis를 통한 선점 시도 (분산 락 역할 겸 상태 저장)
        boolean isLocked = redisLockRepository.lockSeat(concertId, seatNumber, userId.toString());
        
        if (!isLocked) {
            String currentOwner = redisLockRepository.getSeatOwner(concertId, seatNumber);
            log.info("좌석 {} - {} 선점 실패: 현재 소유자(userId: {}), 시도자(userId: {})", concertId, seatNumber, currentOwner, userId);
            return false;
        }

        final String redissonLockKey = LOCK_KEY_PREFIX + seatId;
        RLock lock = redissonClient.getLock(redissonLockKey);

        try {
            // 2. DB 업데이트를 위한 짧은 시간의 락 획득 (선택 사항이지만 안전을 위해 유지)
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                log.info("좌석 {}에 대한 DB 업데이트 락 획득 실패", seatId);
                redisLockRepository.unlockSeat(concertId, seatNumber); // Redis 선점 해제
                return false;
            }

            // 3. DB 상태 변경

            log.info("현재 DB의 전체 좌석 수: {}", seatRepository.count()); // 이게 0이 나오면 DB 연결 설정 문제
            
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new EntityNotFoundException("좌석이 존재하지 않습니다. ID: " + seatId));

            try {
                seat.hold(userId, HOLD_DURATION_MINUTES);
                seatRepository.save(seat);
                log.info("좌석 {} 선점 성공 (사용자: {})", seatId, userId);
                return true;
            } catch (IllegalStateException e) {
                log.warn("좌석 {} DB 선점 실패: {}", seatId, e.getMessage());
                redisLockRepository.unlockSeat(concertId, seatNumber); // Redis 선점 해제
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("락 획득 중 인터럽트 발생", e);
            redisLockRepository.unlockSeat(concertId, seatNumber); // Redis 선점 해제
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
