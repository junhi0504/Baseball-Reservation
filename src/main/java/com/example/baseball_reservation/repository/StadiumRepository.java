package com.example.baseball_reservation.repository;

import com.example.baseball_reservation.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    // 구장 이름으로 찾기 기능 추가
    Stadium findByName(String name);
}