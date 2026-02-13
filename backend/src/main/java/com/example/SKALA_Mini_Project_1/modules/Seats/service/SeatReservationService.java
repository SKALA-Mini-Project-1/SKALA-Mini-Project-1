package com.example.SKALA_Mini_Project_1.modules.Seats.service;

import com.example.SKALA_Mini_Project_1.global.redis.RedisLockRepository;
import com.example.SKALA_Mini_Project_1.modules.Seats.domain.Seat;
import com.example.SKALA_Mini_Project_1.modules.Seats.repository.SeatRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatReservationService {

    private final RedissonClient redissonClient; // 좌석 선택 시 중복 예약을 막기 위한 분산 락
    private final SeatRepository seatRepository;
    private final RedisLockRepository redisLockRepository;

    private static final String LOCK_KEY_PREFIX = "LOCK:SEAT:"; // seat_id로 락 획득
    private static final int HOLD_DURATION_MINUTES = 1;

    /**
     * 좌석 선점 (임시 예약)
     * Redis를 통해 선점 상태를 관리하고, DB에도 반영합니다.
     */
    public void reserveSeatTemporary(
            Long concertId,
            Long seatId,
            String section,
            Integer rowNumber,
            Integer seatNumber,
            Long userId
    ) {
        // 1. Redis를 통한 선점 시도 (분산 락 역할 겸 상태 저장)
        boolean isLocked = redisLockRepository.lockSeat(concertId, section, rowNumber, seatNumber, userId.toString());
        
        if (!isLocked) {
            String currentOwner = redisLockRepository.getSeatOwner(concertId, section, rowNumber, seatNumber);
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

        final String redissonLockKey = LOCK_KEY_PREFIX + seatId;
        RLock lock = redissonClient.getLock(redissonLockKey);
        boolean releaseRedisLockOnFailure = false;

        try {
            // 2. DB 업데이트를 위한 짧은 시간의 락 획득 (선택 사항이지만 안전을 위해 유지)
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                log.info("좌석 {}에 대한 DB 업데이트 락 획득 실패", seatId);
                redisLockRepository.unlockSeat(concertId, section, rowNumber, seatNumber); // Redis 선점 해제
                throw new IllegalStateException("좌석 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }
            releaseRedisLockOnFailure = true;

            // 3. DB 상태 변경

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new EntityNotFoundException("좌석이 존재하지 않습니다. ID: " + seatId));

            if (!Objects.equals(seat.getSection(), section)
                    || !Objects.equals(seat.getRowNumber(), rowNumber)
                    || !Objects.equals(seat.getSeatNumber(), seatNumber)) {
                redisLockRepository.unlockSeat(concertId, section, rowNumber, seatNumber); // Redis 선점 해제
                throw new IllegalArgumentException("요청한 좌석 정보가 seatId와 일치하지 않습니다.");
            }

            seat.hold(userId, HOLD_DURATION_MINUTES);
            seatRepository.save(seat);
            log.info("좌석 {} 선점 성공 (사용자: {})", seatId, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("락 획득 중 인터럽트 발생", e);
            redisLockRepository.unlockSeat(concertId, section, rowNumber, seatNumber); // Redis 선점 해제
            throw new IllegalStateException("좌석 선점 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (RuntimeException e) {
            if (releaseRedisLockOnFailure) {
                redisLockRepository.unlockSeat(concertId, section, rowNumber, seatNumber);
            }
            throw e;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
