package com.example.SKALA_Mini_Project_1.modules.waiting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.SKALA_Mini_Project_1.modules.waiting.service.QueueService;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/queue")
public class QueueController {
    private final QueueService queueService;

    @PostMapping("/enter")
    public ResponseEntity<?> enter(
            @RequestParam Long concertId,
            @RequestParam String userId,
            @RequestParam long fandomWeightMillis) {

        long rank = queueService.enterQueue(concertId, userId, fandomWeightMillis);

        return ResponseEntity.ok(Map.of(
                "rank", rank
        ));
    }

    @GetMapping("/rank")
    public ResponseEntity<?> rank(
            @RequestParam Long concertId,
            @RequestParam String userId) {

        long rank = queueService.getRank(concertId, userId);

        return ResponseEntity.ok(Map.of(
                "rank", rank
        ));
    }

    @GetMapping("/can-enter")
        public ResponseEntity<?> canEnter(
                @RequestParam Long concertId,
                @RequestParam String userId) {

        boolean allowed = queueService.canEnter(concertId, userId);

        if (allowed) {
                return ResponseEntity.ok(Map.of(
                        "allowed", true,
                        "redirectUrl", "http://localhost:8081/seats?concertId=" + concertId
                ));
        }

        return ResponseEntity.ok(Map.of(
                "allowed", false
        ));
        }
}
