package com.example.SKALA_Mini_Project_1.modules.seats.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSeatsDto {

    @NotNull(message = "콘서트 ID는 필수입니다.")
    @Schema(description = "콘서트 ID", example = "1")
    private Long concertId;

    @NotNull(message = "좌석 ID는 필수입니다.")
    @Schema(description = "좌석 ID", example = "2")
    private Long seatId;

    @NotBlank(message = "구역은 필수입니다.")
    @Schema(description = "구역", example = "A")
    private String section;

    @NotNull(message = "열 정보는 필수입니다.")
    @Positive(message = "열은 1 이상이어야 합니다.")
    @Schema(description = "열 번호", example = "1")
    private Integer rowNumber;

    @NotNull(message = "좌석 번호는 필수입니다.")
    @Positive(message = "좌석 번호는 1 이상이어야 합니다.")
    @Schema(description = "좌석 번호", example = "2")
    private Integer seatNumber;

    @NotNull(message = "사용자 ID는 필수입니다.")
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    public RequestSeatsDto(Long concertId, Long seatId, String section, Integer rowNumber, Integer seatNumber, Long userId) {
        this.concertId = concertId;
        this.seatId = seatId;
        this.section = section;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.userId = userId;
    }
}
