package com.example.baseball_reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    @NotNull(message = "경기 ID는 필수 입력 항목입니다.")
    private Long gameId;

    @NotNull(message = "좌석 ID는 필수 입력 항목입니다.")
    private Long seatId;

    // 🔍 username 필드는 이제 토큰에서 가져오므로 삭제합니다!
}