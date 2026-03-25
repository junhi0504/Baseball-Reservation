package com.example.baseball_reservation;

import com.example.baseball_reservation.entity.*;
import com.example.baseball_reservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitData implements CommandLineRunner {

    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. 데이터 중복 삽입 방지
        if (stadiumRepository.count() > 0) return;

        // 2. 구장 생성
        Stadium jamsil = new Stadium("잠실 야구장", "서울", 25000);
        stadiumRepository.save(jamsil);

        // 3. 경기 일정 생성 (평일 1개, 주말 1개)
        // 평일 경기: 2026년 4월 1일 (수요일)
        Game weekdayGame = new Game("LG 트윈스", "두산 베어스",
                LocalDateTime.of(2026, 4, 1, 18, 30), jamsil);
        gameRepository.save(weekdayGame);

        // 주말 경기: 2026년 4월 4일 (토요일)
        Game weekendGame = new Game("키움 히어로즈", "SSG 랜더스",
                LocalDateTime.of(2026, 4, 4, 17, 00), jamsil);
        gameRepository.save(weekendGame);

        // 4. 좌석 생성 (주중/주말 공통 좌석 마스터 데이터)
        List<Seat> seats = new ArrayList<>();

        // 프리미엄석 (60,000원)
        for (int i = 1; i <= 10; i++) seats.add(new Seat("프리미엄석", i, 60000, jamsil));
        // 테이블석 (45,000원)
        for (int i = 1; i <= 20; i++) seats.add(new Seat("테이블석", i, 45000, jamsil));
        // 블루석 (20,000원)
        for (int i = 101; i <= 130; i++) seats.add(new Seat("블루석", i, 20000, jamsil));
        // 오렌지석 (18,000원)
        for (int i = 201; i <= 230; i++) seats.add(new Seat("오렌지석", i, 18000, jamsil));
        // 레드석 (15,000원)
        for (int i = 111; i <= 140; i++) seats.add(new Seat("레드석", i, 15000, jamsil));
        // 네이비석 (13,000원)
        for (int i = 301; i <= 350; i++) seats.add(new Seat("네이비석", i, 13000, jamsil));
        // 외야석 (9,000원)
        for (int i = 401; i <= 450; i++) seats.add(new Seat("외야석", i, 9000, jamsil));

        seatRepository.saveAll(seats);

        System.out.println("✅ 잠실 구장(평일/주말 경기) 데이터 삽입 완료!");
    }
}