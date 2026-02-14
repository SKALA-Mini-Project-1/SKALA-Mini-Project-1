package com.example.SKALA_Mini_Project_1.global.redis;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisLockRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean lockSeat(Long concertId, String section, Integer rowNumber, Integer seatNumber, String userId) {

        String key = RedisKeyGenerator.seatLockKey(concertId, section, rowNumber, seatNumber);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, userId, Duration.ofMinutes(5));

        return Boolean.TRUE.equals(success);
    }

    public void unlockSeat(Long concertId, String section, Integer rowNumber, Integer seatNumber) {

        String key = RedisKeyGenerator.seatLockKey(concertId, section, rowNumber, seatNumber);

        redisTemplate.delete(key);
    }

    public String getSeatOwner(Long concertId, String section, Integer rowNumber, Integer seatNumber) {

        String key = RedisKeyGenerator.seatLockKey(concertId, section, rowNumber, seatNumber);

        return redisTemplate.opsForValue().get(key);
    }

    public Long getSeatLockTtlSeconds(Long concertId, String section, Integer rowNumber, Integer seatNumber) {
        String key = RedisKeyGenerator.seatLockKey(concertId, section, rowNumber, seatNumber);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0) {
            return null;
        }
        return ttl;
    }

    public int countUserHeldSeats(Long concertId, String userId) {
        Set<String> keys = redisTemplate.keys("seat:concert:" + concertId + ":*");
        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String key : keys) {
            String owner = redisTemplate.opsForValue().get(key);
            if (userId.equals(owner)) {
                count++;
            }
        }
        return count;
    }
}
