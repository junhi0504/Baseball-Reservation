package com.example.baseball_reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GameResponse {
    private Long id;
    private String stadiumName;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime gameTime;
}