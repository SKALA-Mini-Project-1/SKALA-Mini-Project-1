package com.example.SKALA_Mini_Project_1.domain.Seats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSeatsDto {

    @NotNull(message = "콘서트 ID는 필수입니다.")
    private Long concertId;

    @NotNull(message = "좌석 ID는 필수입니다.")
    private Long seatId;

    @NotBlank(message = "좌석 번호는 필수입니다.")
    private String seatNumber;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    public RequestSeatsDto(Long concertId, Long seatId, String seatNumber, Long userId) {
        this.concertId = concertId;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.userId = userId;
    }
}
