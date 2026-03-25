package com.example.baseball_reservation.repository;

import com.example.baseball_reservation.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    // 팀 이름으로 경기 검색 (Contain: 해당 글자가 포함되면 검색)
    List<Game> findByHomeTeamContainingOrAwayTeamContaining(String homeTeam, String awayTeam);
}