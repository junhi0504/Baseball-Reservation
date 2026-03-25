package com.example.baseball_reservation.controller;

import com.example.baseball_reservation.dto.AvailableSeatResponse;
import com.example.baseball_reservation.dto.ReservationRequest;
import com.example.baseball_reservation.dto.ReservationResponse;
import com.example.baseball_reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 1. 예매하기
    @PostMapping
    public ResponseEntity<String> reserve(
            @Valid @RequestBody ReservationRequest request,
            Principal principal) {

        Long reservationId = reservationService.createReservation(
                request.getGameId(),
                request.getSeatId(),
                principal.getName()
        );
        return ResponseEntity.ok("예매가 완료되었습니다! 예약 번호: " + reservationId);
    }

    // 2. 내 예매 내역 조회
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Principal principal) {
        List<ReservationResponse> responses = reservationService.getMyReservations(principal.getName());
        return ResponseEntity.ok(responses);
    }

    // 🔍 수정 포인트: @PathVariable에 "gameId" 이름을 명시했습니다.
    @GetMapping("/available/{gameId}")
    public ResponseEntity<List<AvailableSeatResponse>> getAvailableSeats(
            @PathVariable(name = "gameId") Long gameId) {
        List<AvailableSeatResponse> responses = reservationService.getAvailableSeats(gameId);
        return ResponseEntity.ok(responses);
    }

    // 3. 예매 취소
    // 🔍 수정 포인트: @PathVariable에 "reservationId" 이름을 명시했습니다.
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<String> cancelReservation(
            @PathVariable(name = "reservationId") Long reservationId,
            Principal principal) {
        reservationService.cancelReservation(reservationId, principal.getName());
        return ResponseEntity.ok("예매가 성공적으로 취소되었습니다.");
    }
}