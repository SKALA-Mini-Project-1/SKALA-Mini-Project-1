package com.example.SKALA_Mini_Project_1.modules.Waiting.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Duration;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {
    private final RedisTemplate<String, String> redisTemplate;

    // 팬덤 가중치 최대치 (밀리초)
    private static final int MAX_WEIGHT_MILLIS = 5000;

    public long enterQueue(Long concertId, String userId, long fandomWeightMillis) {

        String key = getQueueKey(concertId);

        // 현재 시간 기반 점수 계산
        long now = System.currentTimeMillis();
        long weight = Math.min(fandomWeightMillis, MAX_WEIGHT_MILLIS);
        long jitter = ThreadLocalRandom.current().nextLong(0, 50);

        // 팬덤 가중치와 무작위 지터를 반영한 점수 계산
        long score = now - weight + jitter;

        redisTemplate.opsForZSet()
                .add(key, userId, score);

        Long rank = redisTemplate.opsForZSet()
                .rank(key, userId);

        return rank != null ? rank : -1;
    }

    // 현재 대기열에서의 순위 조회
    public long getRank(Long concertId, String userId) {
        String key = getQueueKey(concertId);
        Long rank = redisTemplate.opsForZSet().rank(key, userId);
        return rank != null ? rank : -1;
    }

    public boolean canEnter(Long concertId, String userId) {

        String queueKey = getQueueKey(concertId);
        String enterKey = getEnterKey(concertId);

        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
        String allowedStr = redisTemplate.opsForValue().get(enterKey);

        if (rank == null || allowedStr == null) return false;

        long allowed = Long.parseLong(allowedStr);

        if (rank < allowed) {

            // 입장 토큰 발급 (30초)
            String tokenKey = getTokenKey(concertId, userId);

            redisTemplate.opsForValue()
                    .set(tokenKey, "1", Duration.ofSeconds(30));

            // 선택: 대기열에서 제거
            redisTemplate.opsForZSet().remove(queueKey, userId);

            return true;
        }

        return false;
    }

    private String getTokenKey(Long concertId, String userId) {
        return "queue:enter:token:" + concertId + ":" + userId;
    }

    private String getQueueKey(Long concertId) {
        return "queue:concert:" + concertId;
    }

    private String getEnterKey(Long concertId) {
        return "queue:concert:" + concertId + ":enter";
    }
}
