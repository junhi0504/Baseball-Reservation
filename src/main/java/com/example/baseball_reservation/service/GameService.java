package com.example.baseball_reservation.service;

import com.example.baseball_reservation.dto.GameResponse;
import com.example.baseball_reservation.entity.Game;
import com.example.baseball_reservation.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;

    public List<GameResponse> searchGames(String teamName) {
        List<Game> games;

        // 검색어가 비어있거나 null이면 전체 조회, 있으면 팀 이름 포함 검색
        if (teamName == null || teamName.isBlank()) {
            games = gameRepository.findAll();
        } else {
            games = gameRepository.findByHomeTeamContainingOrAwayTeamContaining(teamName, teamName);
        }

        return games.stream()
                .map(game -> new GameResponse(
                        game.getId(),
                        game.getStadium().getName(),
                        game.getHomeTeam(),
                        game.getAwayTeam(),
                        game.getGameTime()
                ))
                .collect(Collectors.toList());
    }
}