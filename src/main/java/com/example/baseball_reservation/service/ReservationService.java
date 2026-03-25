package com.example.baseball_reservation.service;

import com.example.baseball_reservation.dto.AvailableSeatResponse;
import com.example.baseball_reservation.dto.ReservationResponse;
import com.example.baseball_reservation.entity.*;
import com.example.baseball_reservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GameRepository gameRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository; // 🔍 유저 조회를 위해 추가

    @Transactional
    public Long createReservation(Long gameId, Long seatId, String loginId) {
        // 1. 경기 및 좌석 조회 (낙관적 락 적용)
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));

        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 2. 로그인한 유저 정보 가져오기
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. 중복 예매 체크
        if (reservationRepository.existsByGameAndSeat(game, seat)) {
            throw new IllegalStateException("이미 예매된 좌석입니다.");
        }

        // 4. 가격 계산 (주말 할증 등)
        int finalPrice = seat.getPrice();
        int dayOfWeek = game.getGameTime().getDayOfWeek().getValue();
        if (dayOfWeek >= 5) { // 금, 토, 일
            finalPrice += 2000;
        }

        // 5. 예매 저장
        Reservation reservation = Reservation.builder()
                .game(game)
                .seat(seat)
                .username(user.getName()) // 기존 필드 유지 시 유저 이름 저장
                .price(finalPrice)
                .reservedAt(LocalDateTime.now())
                .build();

        return reservationRepository.save(reservation).getId();
    }

    public List<ReservationResponse> getMyReservations(String loginId) {
        // 🔍 loginId로 유저를 찾은 뒤 해당 유저의 이름으로 조회하거나,
        // 나중에 Reservation 엔티티를 User와 연관관계 매핑하면 더 깔끔합니다.
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return reservationRepository.findByUsername(user.getName()).stream()
                .map(r -> new ReservationResponse(
                        r.getId(),
                        r.getGame().getStadium().getName(),
                        r.getGame().getHomeTeam(),
                        r.getGame().getAwayTeam(),
                        r.getSeat().getZone(),
                        r.getSeat().getSeatNumber(),
                        r.getPrice(),
                        r.getReservedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<AvailableSeatResponse> getAvailableSeats(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));

        List<Seat> allSeats = seatRepository.findByStadium(game.getStadium());
        List<Long> reservedSeatIds = reservationRepository.findByGame(game).stream()
                .map(r -> r.getSeat().getId())
                .collect(Collectors.toList());

        return allSeats.stream()
                .filter(seat -> !reservedSeatIds.contains(seat.getId()))
                .map(seat -> new AvailableSeatResponse(
                        seat.getId(),
                        seat.getZone(),
                        seat.getSeatNumber()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(Long reservationId, String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매 내역입니다."));

        // 본인 확인: 토큰의 주인과 예매자가 일치하는지 확인
        if (!reservation.getUsername().equals(user.getName())) {
            throw new IllegalStateException("본인의 예매 내역만 취소할 수 있습니다.");
        }

        reservationRepository.delete(reservation);
    }
}