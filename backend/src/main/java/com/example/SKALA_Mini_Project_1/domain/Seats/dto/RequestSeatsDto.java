package com.example.SKALA_Mini_Project_1.domain.Seats.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSeatsDto {

    @NotNull(message = "좌석 ID는 필수입니다.")
    private Long seatId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    public RequestSeatsDto(Long seatId, Long userId) {
        this.seatId = seatId;
        this.userId = userId;
    }
}
