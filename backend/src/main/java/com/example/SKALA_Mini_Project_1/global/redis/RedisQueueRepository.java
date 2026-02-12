package com.example.SKALA_Mini_Project_1.global.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisQueueRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void addToQueue(Long concertId, String userId) {

        String key = RedisKeyGenerator.queueKey(concertId);
        long timestamp = System.currentTimeMillis();

        redisTemplate.opsForZSet()
                .add(key, userId, timestamp);
    }

    public Long getRank(Long concertId, String userId) {

        String key = RedisKeyGenerator.queueKey(concertId);

        return redisTemplate.opsForZSet()
                .rank(key, userId);
    }

    public void removeFromQueue(Long concertId, String userId) {

        String key = RedisKeyGenerator.queueKey(concertId);

        redisTemplate.opsForZSet()
                .remove(key, userId);
    }
}
