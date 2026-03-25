# Baseball Reservation System  

대규모 트래픽 상황에서도 좌석 중복 예약이 발생하지 않도록 설계한 야구 경기 티켓 예매 백엔드 시스템입니다.  
실제 티켓팅 서비스를 가정하여 동시성 처리, 트랜잭션 관리, 도메인 중심 설계 경험을 목표로 개발했습니다.

---

## 프로젝트 목표  

- 실서비스 수준의 예약 시스템 백엔드 구조 설계 경험  
- 동시 요청 상황에서 발생하는 동시성 문제 해결 경험  
- REST API 설계 및 트랜잭션 처리 이해  
- 유지보수 가능한 도메인 중심 구조 설계  

---

## 프로젝트를 만든 이유  

티켓 예매 서비스는 특정 시간에 많은 사용자가 동시에 접근하는 대표적인 고트래픽 시스템입니다.  

이 프로젝트는 단순 CRUD 구현이 아니라  
동일 좌석에 대한 중복 예약 문제를 실제로 어떻게 해결하는지 경험하기 위해 개발했습니다.

---

## 시스템 구조  

### 기본 구조  

Client  
↓  
Controller  
↓  
Service  
↓  
Repository (JPA)  
↓  
MySQL  

### 확장 가능한 구조 (실서비스 환경 가정)  

Client  
↓  
Nginx  
↓  
Spring Server (Multi Instance)  
↓  
Redis (Distributed Lock / Cache)  
↓  
MySQL  

---

## 데이터 구조 (ERD)

### Entity  

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

### 관계  

User 1 --- N Reservation  
Seat 1 --- 1 Reservation  
Game 1 --- N Seat  

---

## 주요 기능  

### 경기 조회  
- 경기 목록 조회  
- 경기 상세 조회  

### 좌석 조회  
- 경기별 좌석 상태 조회  
- 예약 가능 좌석 확인  

### 좌석 예약  
- 좌석 예약 요청 처리  
- 중복 예약 방지 로직 적용  
- 트랜잭션 기반 데이터 정합성 보장  

### 예약 취소  
- 예약 내역 조회  
- 예약 취소 처리  

---

## 동시성 문제 해결 전략  

### 문제 상황  

여러 사용자가 동시에 동일 좌석을 예약하면  
두 트랜잭션이 동시에 예약 가능 상태를 조회하여  
중복 예약이 발생하는 Race Condition 문제가 발생할 수 있습니다.

### 해결 방법  

데이터 정합성을 최우선으로 고려하여 DB 비관적 락(Pessimistic Lock)을 적용했습니다.

- SELECT ... FOR UPDATE 기반 배타적 락 획득  
- 좌석 조회 시점부터 트랜잭션 종료까지 락 유지  
- 다른 트랜잭션은 해당 좌석 접근 시 대기 상태  

### 선택 이유  

티켓팅 서비스는 짧은 시간 동안 동일 자원 충돌이 매우 빈번하게 발생합니다.  

따라서 충돌 후 재시도가 필요한 낙관적 락보다  
즉시 충돌을 차단하는 비관적 락이 더 안정적이라 판단했습니다.

---

## 핵심 예약 처리 로직  

### SeatRepository  

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select s from Seat s where s.id = :id")
Optional<Seat> findByIdWithLock(Long id);
```

### ReservationService  

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

## 문제 해결 과정  

### 중복 예약 및 Deadlock 방지  

- Lock Timeout 설정으로 무한 대기 방지  
- 트랜잭션 범위를 최소화하여 락 유지 시간 단축  
- 예약 처리 로직을 단순화하여 빠르게 수행  

### 조회 성능 개선 (N+1 문제 해결)  

- Fetch Join 적용  
- default_batch_fetch_size 설정  

---

## 동시성 테스트 결과  

Thread 기반 동시 요청 테스트 수행  

- 동일 좌석에 대해 여러 요청 발생  
- 1건만 예약 성공  
- 나머지는 예약 실패 응답  

데이터 정합성 유지 확인  

---

## API 예시  

### 좌석 예약  

POST /api/reservations  

```json
{
  "userId": 1,
  "seatId": 3
}
```

---

## 사용 기술  

- Java 21  
- Spring Boot  
- Spring Data JPA  
- MySQL  
- Gradle  

---

## 실행 방법  

```bash
git clone https://github.com/junhi0504/Baseball-Reservation.git
cd Baseball-Reservation
./gradlew bootRun
```

---

## 향후 개선 계획  

- Redis 분산 락 적용  
- 낙관적 락(@Version) 성능 비교 실험  
- 동시성 테스트 자동화  
- JWT 인증 적용  
- 테스트 코드 작성 (JUnit / Mockito)  
- Docker 컨테이너 환경 구성  
- AWS 배포  
- 좌석 예매 프론트 UI 개발  

---

## 개발자  

김준희  
Backend Developer (Java / Spring)  

GitHub  
https://github.com/junhi0504  
