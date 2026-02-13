package com.example.SKALA_Mini_Project_1.modules.Seats.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SKALA_Mini_Project_1.modules.Seats.dto.RequestSeatsDto;
import com.example.SKALA_Mini_Project_1.modules.Seats.service.SeatReservationService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatReservationService seatReservationService;

    @PostMapping("/{seatId}/hold")
    public ResponseEntity<?> reserveSeat(@RequestBody @Valid RequestSeatsDto requestDto) {
        try {
            seatReservationService.reserveSeatTemporary(
                    requestDto.getConcertId(),
                    requestDto.getSeatId(),
                    requestDto.getSection(),
                    requestDto.getRowNumber(),
                    requestDto.getSeatNumber(),
                    requestDto.getUserId()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "좌석이 성공적으로 선점되었습니다.",
                    "status", "success"
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", e.getMessage(),
                    "status", "not_found"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "status", "bad_request"
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", e.getMessage(),
                    "status", "conflict"
            ));
        }
    }
}
