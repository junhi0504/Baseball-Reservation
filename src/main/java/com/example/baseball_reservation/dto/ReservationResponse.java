package com.example.baseball_reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor // 이 어노테이션이 아래 필드 순서대로 생성자를 만듭니다.
public class ReservationResponse {
    private Long id;
    private String stadiumName;
    private String homeTeam;
    private String awayTeam;
    private String zone;
    private int seatNumber;   // 6번째
    private int paymentAmount; // 7번째 (서비스의 r.getPrice()가 여기 담겨야 함)
    private LocalDateTime reservedAt;
}