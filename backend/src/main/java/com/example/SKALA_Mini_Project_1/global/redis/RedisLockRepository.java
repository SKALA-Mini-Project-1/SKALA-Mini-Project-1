package com.example.SKALA_Mini_Project_1.global.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisLockRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean lockSeat(Long concertId, String seatNumber, String userId) {

        String key = RedisKeyGenerator.seatLockKey(concertId, seatNumber);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, userId, Duration.ofMinutes(5));

        return Boolean.TRUE.equals(success);
    }

    public void unlockSeat(Long concertId, String seatNumber) {

        String key = RedisKeyGenerator.seatLockKey(concertId, seatNumber);

        redisTemplate.delete(key);
    }

    public String getSeatOwner(Long concertId, String seatNumber) {

        String key = RedisKeyGenerator.seatLockKey(concertId, seatNumber);

        return redisTemplate.opsForValue().get(key);
    }
}
