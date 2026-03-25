# Baseball Reservation System

대규모 트래픽 상황에서 좌석 중복 예약을 방지하는 야구 경기 티켓 예약 백엔드 시스템 프로젝트입니다.
실제 티켓 예매 서비스를 가정하여 동시성 처리, 트랜잭션 관리, 도메인 설계 경험을 목표로 개발했습니다.

---

## Project Goal

* 예약 시스템 백엔드 구조 설계 경험
* 동시성 문제 해결 경험
* RESTful API 설계 능력 향상
* JPA 기반 트랜잭션 처리 이해
* 유지보수 가능한 도메인 구조 설계

---

## System Architecture

```
Client
 ↓
Controller
 ↓
Service
 ↓
Repository (JPA)
 ↓
MySQL
```

### 확장 구조 (실서비스 고려)

```
Client
 ↓
Nginx
 ↓
Spring Server (Multi Instance)
 ↓
Redis (Distributed Lock / Cache)
 ↓
MySQL
```

---

## ERD

```
User
 - id
 - name
 - email

Game
 - id
 - stadium
 - gameDate

Seat
 - id
 - seatNumber
 - status
 - gameId

Reservation
 - id
 - userId
 - seatId
 - reservedAt
```

### Relationship

```
User 1 --- N Reservation
Seat 1 --- 1 Reservation
Game 1 --- N Seat
```

---

## Main Features

### Game Inquiry

* 경기 목록 조회
* 경기 상세 조회

### Seat Inquiry

* 경기별 좌석 상태 조회
* 예약 가능 좌석 확인

### Seat Reservation

* 좌석 예약 요청
* 중복 예약 방지
* 트랜잭션 기반 처리

### Reservation Cancel

* 예약 내역 조회
* 예약 취소 처리

---

## Concurrency Handling Strategy

### Problem

예매 오픈 시 여러 사용자가 동시에 동일 좌석 예약 시도

### Solution

* `@Transactional` 적용
* 좌석 예약 상태 검증
* 예약 완료 시 상태 즉시 변경

### Core Code

```java
@Transactional
public void reserveSeat(Long userId, Long seatId) {

    Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new RuntimeException("Seat not found"));

    if (seat.isReserved()) {
        throw new RuntimeException("이미 예약된 좌석입니다.");
    }

    seat.reserve();

    Reservation reservation = new Reservation(userId, seat);
    reservationRepository.save(reservation);
}
```

---

## Trouble Shooting

### Duplicate Reservation

Cause
Seat 조회 후 상태 변경 전에 다른 트랜잭션 접근 가능

Solution

* 트랜잭션 범위 확장
* 좌석 상태 변경과 예약 저장을 동일 트랜잭션에서 처리

### N+1 Problem

Solution

* Fetch Join 적용
* DTO 조회 방식으로 개선

---

## API Example

### Seat Reservation

Request

```
POST /api/reservations
```

```json
{
  "userId": 1,
  "seatId": 3
}
```

Success Response

```json
{
  "status": "SUCCESS",
  "reservationId": 3
}
```

Fail Response

```json
{
  "status": "FAIL",
  "message": "이미 예약된 좌석입니다."
}
```

---

## Result Screen Guide

### Seat Status Before Reservation

```
GET /api/games/1/seats
```

* 모든 좌석 상태가 AVAILABLE 인 화면 캡처

### Reservation Success

```
POST /api/reservations
```

* SUCCESS 응답 및 reservationId 생성 화면 캡처

### Seat Status After Reservation

```
GET /api/games/1/seats
```

* 특정 좌석 상태가 RESERVED 로 변경된 화면 캡처

### Duplicate Reservation Fail

```
POST /api/reservations
```

* 동일 좌석 재요청 시 FAIL 응답 화면 캡처

### Concurrency Test Result

* 여러 Thread 동시 요청 시 콘솔 로그에서 1건 성공 / 나머지 실패 화면 캡처

---

## Concurrency Test Code

```java
@SpringBootTest
class ConcurrencyTest {

    @Autowired
    ReservationService reservationService;

    @Test
    void 동시에_좌석_예약() throws InterruptedException {

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long userId = (long) i + 1;

            executorService.submit(() -> {
                try {
                    reservationService.reserveSeat(userId, 1L);
                    System.out.println("예약 성공 userId = " + userId);
                } catch (Exception e) {
                    System.out.println("예약 실패 userId = " + userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
    }
}
```

---

## Tech Stack

* Java 21
* Spring Boot
* Spring Data JPA
* MySQL
* Gradle

---

## Run

```bash
git clone https://github.com/junhi0504/Baseball-Reservation.git
cd Baseball-Reservation
./gradlew bootRun
```

---

## Future Improvement

* Redis Distributed Lock
* Optimistic Lock 적용
* 동시성 테스트 고도화
* JWT Authentication
* Test Code (JUnit / Mockito)
* Docker
* AWS Deploy
* 좌석 예매 프론트 UI 구현

---

## Developer

김준희
Backend Developer (Java / Spring)

GitHub
https://github.com/junhi0504
