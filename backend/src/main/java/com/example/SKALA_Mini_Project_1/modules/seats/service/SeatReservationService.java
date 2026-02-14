package com.example.SKALA_Mini_Project_1.modules.seats.service;

import com.example.SKALA_Mini_Project_1.global.redis.RedisLockRepository;
import com.example.SKALA_Mini_Project_1.modules.seats.domain.Seat;
import com.example.SKALA_Mini_Project_1.modules.seats.domain.SeatStatus;
import com.example.SKALA_Mini_Project_1.modules.seats.repository.SeatRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatReservationService {
    private static final int MAX_HOLD_SEAT_COUNT = 4; // 1인 최대 4매 제한

    public enum SeatHoldResult {
        HELD,
        RELEASED
    }

    public record BatchHoldResult(boolean success, List<Long> heldSeatIds, List<Long> failedSeatIds) {}

    private final SeatRepository seatRepository;
    private final RedisLockRepository redisLockRepository;

    /*
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
        Seat seat = validateSeat(concertId, seatId, section, rowNumber, seatNumber);
        if (seat.getStatus() == SeatStatus.RESERVED) {
            throw new IllegalStateException("이미 판매된 좌석입니다.");
        }

        String userIdString = String.valueOf(userId);
        String currentOwner = redisLockRepository.getSeatOwner(concertId, section, rowNumber, seatNumber);

        if (Objects.equals(currentOwner, userIdString)) {
            redisLockRepository.unlockSeat(concertId, section, rowNumber, seatNumber);
            log.info("좌석 {} 선점 해제 성공 (사용자: {})", seatId, userId);
            return SeatHoldResult.RELEASED;
        }

        if (currentOwner != null) {
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

        int currentHoldCount = redisLockRepository.countUserHeldSeats(concertId, userIdString);
        if (currentHoldCount >= MAX_HOLD_SEAT_COUNT) {
            throw new IllegalArgumentException("좌석은 최대 4매까지만 선택할 수 있습니다.");
        }

        // Redis를 통한 선점 시도 (HOLD는 Redis TTL로만 관리)
        boolean isLocked = redisLockRepository.lockSeat(concertId, section, rowNumber, seatNumber, userIdString);
        
        if (!isLocked) {
            String lockOwner = redisLockRepository.getSeatOwner(concertId, section, rowNumber, seatNumber);
            if (Objects.equals(lockOwner, userIdString)) {
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
                    lockOwner,
                    userId
            );
            throw new IllegalStateException("이미 다른 사용자가 선택한 좌석입니다.");
        }

        log.info("좌석 {} 선점 성공 (사용자: {})", seatId, userId);
        return SeatHoldResult.HELD;
    }

    public BatchHoldResult holdSeatsBatch(Long concertId, List<Long> seatIds, Long userId) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("좌석 ID 목록은 비어 있을 수 없습니다.");
        }

        Set<Long> uniqueSeatIds = new LinkedHashSet<>(seatIds);
        if (uniqueSeatIds.size() > MAX_HOLD_SEAT_COUNT) {
            throw new IllegalArgumentException("좌석은 최대 4매까지만 선택할 수 있습니다.");
        }

        String userIdString = String.valueOf(userId);
        int currentHoldCount = redisLockRepository.countUserHeldSeats(concertId, userIdString);

        List<Long> failedSeatIds = new ArrayList<>();
        List<Seat> seatsToLock = new ArrayList<>();
        List<Long> alreadyHeldByMe = new ArrayList<>();

        for (Long seatId : uniqueSeatIds) {
            Seat seat = seatRepository.findByIdAndConcertId(seatId, concertId)
                    .orElse(null);

            if (seat == null || seat.getStatus() == SeatStatus.RESERVED) {
                failedSeatIds.add(seatId);
                continue;
            }

            String owner = redisLockRepository.getSeatOwner(concertId, seat.getSection(), seat.getRowNumber(), seat.getSeatNumber());
            if (owner == null) {
                seatsToLock.add(seat);
                continue;
            }

            if (Objects.equals(owner, userIdString)) {
                alreadyHeldByMe.add(seatId);
            } else {
                failedSeatIds.add(seatId);
            }
        }

        if (!failedSeatIds.isEmpty()) {
            return new BatchHoldResult(false, List.of(), failedSeatIds);
        }

        if (currentHoldCount + seatsToLock.size() > MAX_HOLD_SEAT_COUNT) {
            throw new IllegalArgumentException("좌석은 최대 4매까지만 선택할 수 있습니다.");
        }

        List<Long> newlyLockedSeatIds = new ArrayList<>();
        for (Seat seat : seatsToLock) {
            boolean locked = redisLockRepository.lockSeat(
                    concertId,
                    seat.getSection(),
                    seat.getRowNumber(),
                    seat.getSeatNumber(),
                    userIdString
            );

            if (!locked) {
                failedSeatIds.add(seat.getId());
                for (Long lockedSeatId : newlyLockedSeatIds) {
                    Seat lockedSeat = seatRepository.findById(lockedSeatId)
                            .orElse(null);
                    if (lockedSeat != null) {
                        redisLockRepository.unlockSeat(
                                concertId,
                                lockedSeat.getSection(),
                                lockedSeat.getRowNumber(),
                                lockedSeat.getSeatNumber()
                        );
                    }
                }
                return new BatchHoldResult(false, List.of(), failedSeatIds);
            }
            newlyLockedSeatIds.add(seat.getId());
        }

        List<Long> heldSeatIds = new ArrayList<>(alreadyHeldByMe);
        heldSeatIds.addAll(newlyLockedSeatIds);
        return new BatchHoldResult(true, heldSeatIds, List.of());
    }

    private Seat validateSeat(
            Long concertId,
            Long seatId,
            String section,
            Integer rowNumber,
            Integer seatNumber
    ) {
        Seat seat = seatRepository.findByIdAndConcertId(seatId, concertId)
                .orElseThrow(() -> new EntityNotFoundException("좌석이 존재하지 않습니다. ID: " + seatId));

        if (!Objects.equals(seat.getSection(), section)
                || !Objects.equals(seat.getRowNumber(), rowNumber)
                || !Objects.equals(seat.getSeatNumber(), seatNumber)) {
            throw new IllegalArgumentException("요청한 좌석 정보가 seatId와 일치하지 않습니다.");
        }

        return seat;
    }
}
