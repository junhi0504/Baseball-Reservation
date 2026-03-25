package com.example.baseball_reservation.repository;

import com.example.baseball_reservation.entity.Game;
import com.example.baseball_reservation.entity.Reservation;
import com.example.baseball_reservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByGameAndSeat(Game game, Seat seat);

    // 이 부분 추가: 사용자 이름으로 예매 내역 찾기
    List<Reservation> findByUsername(String username);
    List<Reservation> findByGame(Game game);
}