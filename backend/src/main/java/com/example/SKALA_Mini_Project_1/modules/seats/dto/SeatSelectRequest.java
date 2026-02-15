package com.example.SKALA_Mini_Project_1.modules.seats.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(
        description = "좌석 선점/해제 요청 DTO. 동일 사용자가 이미 선점한 좌석을 다시 요청하면 선점 해제됩니다."
)
public class SeatSelectRequest {

    @NotNull(message = "콘서트 ID는 필수입니다.")
    @Schema(description = "콘서트 ID", example = "1")
    private Long concertId;

    @NotNull(message = "좌석 ID는 필수입니다.")
    @Schema(description = "좌석 PK ID (section/rowNumber/seatNumber와 동일 좌석이어야 함)", example = "401")
    private Long seatId;

    @NotBlank(message = "구역은 필수입니다.")
    @Schema(description = "좌석 구역 코드", example = "B")
    private String section;

    @NotNull(message = "열 정보는 필수입니다.")
    @Positive(message = "열은 1 이상이어야 합니다.")
    @Schema(description = "열 번호(1 이상)", example = "6")
    private Integer rowNumber;

    @NotNull(message = "좌석 번호는 필수입니다.")
    @Positive(message = "좌석 번호는 1 이상이어야 합니다.")
    @Schema(description = "좌석 번호(1 이상)", example = "1")
    private Integer seatNumber;

    public SeatSelectRequest(Long concertId, Long seatId, String section, Integer rowNumber, Integer seatNumber) {
        this.concertId = concertId;
        this.seatId = seatId;
        this.section = section;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
    }
}
