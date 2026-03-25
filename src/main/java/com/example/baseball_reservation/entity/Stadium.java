package com.example.baseball_reservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @NoArgsConstructor
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 생성
@Builder            // 빌더 패턴 사용 가능 (선택 사항)
public class Stadium {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private int totalSeats;

    // 만약 빌더를 안 쓰신다면 아래와 같은 수동 생성자가 필요합니다 (이미 코드에서 사용 중인 형태)
    public Stadium(String name, String location, int totalSeats) {
        this.name = name;
        this.location = location;
        this.totalSeats = totalSeats;
    }
}