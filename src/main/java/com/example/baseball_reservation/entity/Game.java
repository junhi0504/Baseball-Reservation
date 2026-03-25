package com.example.baseball_reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Getter @NoArgsConstructor
@AllArgsConstructor @Builder
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime gameTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    // InitData에서 사용 중인 생성자
    public Game(String homeTeam, String awayTeam, LocalDateTime gameTime, Stadium stadium) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.gameTime = gameTime;
        this.stadium = stadium;
    }
}