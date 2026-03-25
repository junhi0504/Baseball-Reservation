package com.example.baseball_reservation.controller;

import com.example.baseball_reservation.dto.GameResponse;
import com.example.baseball_reservation.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<List<GameResponse>> getGames(
            @RequestParam(name = "team", required = false) String team // 이름을 명시해줍니다.
    ) {
        // team 파라미터가 있으면 검색, 없으면 전체 목록 반환
        return ResponseEntity.ok(gameService.searchGames(team));
    }
}