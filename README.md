# Baseball Reservation System

대규모 트래픽 상황에서 좌석 중복 예약을 방지하는 야구 경기 티켓 예약 백엔드 시스템 프로젝트입니다.
실제 티켓 예매 서비스를 가정하여 동시성 처리, 트랜잭션 관리, 도메인 설계 경험을 목표로 개발했습니다.

---

## Project Goal

* 예약 시스템 백엔드 아키텍처 설계 경험
* 동시성 문제 분석 및 해결 경험
* RESTful API 설계 역량 강화
* JPA 기반 트랜잭션 처리 이해
* 유지보수 가능한 도메인 중심 구조 설계

---

## System Architecture

### Basic Structure

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

### Scalable Structure (Production Oriented)

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
* 중복 예약 방지 로직 적용
* 트랜잭션 기반 예약 처리

### Reservation Cancel

* 예약 내역 조회
* 예약 취소 처리

---

## Concurrency Handling Strategy (Revised)

### 1. Problem: Race Condition (경쟁 상태)

현상
동일 좌석에 대해 여러 사용자가 동시에 예약 요청을 보낼 때
두 트랜잭션이 동시에 `isReserved == false` 상태를 읽어 모두 예약을 성공시키는 정합성 오류가 발생할 수 있음.

원인
단순 SELECT 후 UPDATE 방식은 데이터 조회와 수정 사이에 다른 트랜잭션이 개입할 수 있는 간격이 존재함.

---

### 2. Solution: Pessimistic Lock 적용

데이터 정합성을 최우선으로 고려하여 DB 레벨에서 락을 제어하는 비관적 락 전략을 적용함.

메커니즘

* `SELECT ... FOR UPDATE` 기반의 배타적 락 획득
* 특정 트랜잭션이 좌석 데이터를 조회하는 시점부터 락을 보유
* 다른 트랜잭션은 해당 좌석 접근 시 대기 상태(Blocking)

선택 이유
티켓팅 서비스 특성상 짧은 시간 내 동일 자원 충돌이 빈번하게 발생하므로
재시도 비용이 발생하는 낙관적 락보다 비관적 락이 더 안정적이고 효율적이라 판단함.

---

### 3. Core Logic

#### SeatRepository

```java
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);
}
```

#### ReservationService

```java
@Transactional
public void reserveSeat(Long userId, Long seatId) {

    Seat seat = seatRepository.findByIdWithLock(seatId)
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

## Trouble Shooting (Updated)

### 1. Duplicate Reservation 및 DB Deadlock 방지

Cause
다수의 트랜잭션이 락 획득을 위해 대기하면서 DB 커넥션 풀이 고갈되거나
상호 락 대기로 인해 Deadlock 발생 가능성이 존재함.

Resolution

* Lock Timeout 설정을 통해 무한 대기 방지
* 트랜잭션 범위를 최소화하여 락 유지 시간 단축
* 좌석 조회 → 상태 변경 → 예약 저장 로직을 빠르게 수행하도록 구조 개선

---

### 2. N+1 문제 해결을 통한 조회 성능 최적화

Cause
경기(Game) 목록 조회 시 연관된 좌석(Seat)을 Lazy Loading으로 조회하면서
`1 + N` 쿼리가 발생하여 응답 속도가 저하됨.

Resolution

* Fetch Join
  JPQL 조인 조회를 통해 연관 엔티티를 한 번의 쿼리로 조회하도록 개선

* Batch Size
  `default_batch_fetch_size` 설정을 통해 IN 절 기반 묶음 조회 수행

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

## Result Screen

### Seat Status Before Reservation

* GET /api/games/1/seats
* 모든 좌석 상태가 AVAILABLE 인 화면 스크린샷 첨부

### Reservation Success

* POST /api/reservations
* SUCCESS 응답 및 reservationId 생성 화면 스크린샷 첨부

### Seat Status After Reservation

* GET /api/games/1/seats
* 특정 좌석 상태가 RESERVED 로 변경된 화면 스크린샷 첨부

### Duplicate Reservation Fail

* 동일 좌석 재예약 요청
* FAIL 응답 화면 스크린샷 첨부

### Concurrency Test Result

* Thread 기반 동시 요청 테스트 실행 콘솔 로그 스크린샷 첨부
* 1건 성공 / 나머지 실패 결과 확인 가능

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

* Redis Distributed Lock 적용
* Optimistic Lock (@Version) 전략 비교 실험
* 동시성 테스트 자동화
* JWT Authentication
* Test Code (JUnit / Mockito)
* Docker Containerization
* AWS Deployment
* 좌석 예매 프론트 UI 구현

---

## Developer

김준희
Backend Developer (Java / Spring)

GitHub
https://github.com/junhi0504
