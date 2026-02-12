package com.example.SKALA_Mini_Project_1.domain.Seats.controller;

import com.example.SKALA_Mini_Project_1.domain.Seats.dto.RequestSeatsDto;
import com.example.SKALA_Mini_Project_1.domain.Seats.service.SeatReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatReservationService seatReservationService;

    /**
     * 좌석 임시 선점 API
     */
    @PostMapping("/reserve")
    public ResponseEntity<String> reserveSeat(@RequestBody @Valid RequestSeatsDto requestDto) {
        boolean success = seatReservationService.reserveSeatTemporary(
                requestDto.getSeatId(), 
                requestDto.getUserId()
        );

        if (success) {
            return ResponseEntity.ok("좌석이 성공적으로 선점되었습니다. 5분 내에 결제를 완료해주세요.");
        } else {
            return ResponseEntity.status(409).body("좌석 선점에 실패했습니다. 이미 선택된 좌석이거나 잠시 후 다시 시도해주세요.");
        }
    }
}
