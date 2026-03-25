# ⚾ Baseball Reservation System

대규모 트래픽 상황에서 좌석 중복 예약을 방지하는  
**야구 경기 티켓 예약 백엔드 시스템 프로젝트**입니다.

실제 티켓 예매 서비스를 가정하여  
동시성 처리 / 트랜잭션 관리 / 도메인 설계 경험을 목표로 개발했습니다.

---

## 📌 Project Goal

- 예약 시스템 백엔드 구조 설계 경험
- 동시성 문제 해결 경험
- RESTful API 설계 능력 향상
- JPA 기반 트랜잭션 처리 이해
- 유지보수 가능한 도메인 구조 설계

---

## 🧠 System Architecture

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

### 🔥 확장 구조 (실서비스 고려)

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

## 📊 ERD

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

### 관계

```
User 1 --- N Reservation
Seat 1 --- 1 Reservation
Game 1 --- N Seat
```

---

## 🔥 Main Features

### ✅ 경기 조회
- 경기 목록 조회
- 경기 상세 조회

### ✅ 좌석 조회
- 경기별 좌석 상태 조회
- 예약 가능 좌석 확인

### ✅ 좌석 예약
- 좌석 예약 요청
- 중복 예약 방지
- 트랜잭션 기반 처리

### ✅ 예약 취소
- 예약 내역 조회
- 예약 취소 처리

---

## 🚨 Concurrency Handling Strategy

### 문제 상황
예매 오픈 시 여러 사용자가 동시에 동일 좌석 예약 시도

### 해결 전략
- `@Transactional` 적용
- 좌석 예약 상태 검증
- 예약 완료 시 상태 즉시 변경

### 핵심 코드

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

## 🧪 Trouble Shooting

### ❗ 중복 예약 발생

**원인**
Seat 조회 후 상태 변경 전에 다른 트랜잭션 접근 가능

**해결**
- 트랜잭션 범위 확장
- 좌석 상태 변경과 예약 저장을 동일 트랜잭션에서 처리

---

### ❗ N+1 문제

**해결**
- Fetch Join 적용
- DTO 조회 방식으로 개선

---

## 📮 API Example

### 좌석 예약

**Request**

```
POST /api/reservations
```

```json
{
  "userId": 1,
  "gameId": 2,
  "seatNumber": "A-10"
}
```

**Success Response**

```json
{
  "status": "SUCCESS",
  "reservationId": 3
}
```

**Fail Response**

```json
{
  "status": "FAIL",
  "message": "이미 예약된 좌석입니다."
}
```

---

## 📷 Result Screen (Example)

### 🔵 Swagger Test

```
POST /api/reservations

Response 200 OK
reservationId : 5
```

### 🟢 Seat Status

```
A-1 예약가능
A-2 예약완료
A-3 예약가능
A-4 예약완료
```

---

## 🛠 Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- MySQL
- Gradle

---

## 🚀 Run

```bash
git clone https://github.com/junhi0504/Baseball-Reservation.git
cd Baseball-Reservation
./gradlew bootRun
```

---

## 📈 Future Improvement

- Redis Distributed Lock
- 동시성 테스트 코드 작성 (Thread 기반)
- JWT Authentication
- Test Code (JUnit / Mockito)
- Docker
- AWS Deploy
- 좌석 예매 프론트 UI 구현

---

## 👨‍💻 Developer

김준희  
Backend Developer (Java / Spring)

GitHub  
https://github.com/junhi0504
