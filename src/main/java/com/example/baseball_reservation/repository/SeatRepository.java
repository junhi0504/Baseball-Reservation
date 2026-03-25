package com.example.baseball_reservation.repository;

import com.example.baseball_reservation.entity.Seat;
import com.example.baseball_reservation.entity.Stadium;
import jakarta.persistence.LockModeType; // 락 타입 설정을 위해 필요
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock; // @Lock 어노테이션
import org.springframework.data.jpa.repository.Query; // @Query 어노테이션
import org.springframework.data.repository.query.Param; // @Param 어노테이션
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByStadium(Stadium stadium);

    // 낙관적 락을 적용하여 좌석 하나를 조회합니다.
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);
}