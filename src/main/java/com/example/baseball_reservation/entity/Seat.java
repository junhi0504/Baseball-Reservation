package com.example.baseball_reservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @NoArgsConstructor
@AllArgsConstructor @Builder
public class Seat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String zone;
    private int seatNumber;
    private int price;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    // InitData용 생성자
    public Seat(String zone, int seatNumber, int price, Stadium stadium) {
        this.zone = zone;
        this.seatNumber = seatNumber;
        this.price = price;
        this.stadium = stadium;
    }
}