package com.example.SKALA_Mini_Project_1.global.redis;

public class RedisKeyGenerator {
    public static String queueKey(Long concertId) {
        return "queue:concert:" + concertId;
    }

    public static String seatLockKey(Long concertId, String seatNumber) {
        return "seat:concert:" + concertId + ":seat:" + seatNumber;
    }
}
