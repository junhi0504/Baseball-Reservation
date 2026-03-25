# Baseball Reservation System  

![main](images/main.png)

대규모 트래픽 상황에서도 좌석 중복 예약이 발생하지 않도록 설계한  
야구 경기 티켓 예매 백엔드 시스템입니다.  

실제 티켓팅 서비스를 가정하여  
동시성 처리, 트랜잭션 관리, 도메인 중심 설계 경험을 목표로 개발했습니다.

---

## 프로젝트 목표  

- 실서비스 수준의 예약 시스템 백엔드 구조 설계 경험  
- 동시 요청 상황에서 발생하는 동시성 문제 해결 경험  
- REST API 설계 및 트랜잭션 처리 이해  
- 유지보수 가능한 도메인 중심 구조 설계  

---

## 프로젝트를 만든 이유  

티켓 예매 서비스는 특정 시간에 많은 사용자가 동시에 접근하는  
대표적인 고트래픽 시스템입니다.  

이 프로젝트는 단순 CRUD 구현이 아니라  
동일 좌석에 대한 중복 예약 문제를 실제로 해결하는 경험을 목표로 개발했습니다.

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

---

## 주요 기능  

### 좌석 조회  

경기별 좌석 상태를 조회하여 예약 가능한 좌석을 확인할 수 있습니다.

![좌석 조회](images/seat_before.png)

---

### 좌석 예약  

좌석 예약 요청 시 트랜잭션 기반으로 데이터 정합성을 보장하며  
예약 성공 시 reservationId가 생성되고 좌석 상태가 RESERVED로 변경됩니다.

![예약 성공](images/reservation_success.png)

---

### 좌석 상태 변경  

예약 이후 좌석 상태가 AVAILABLE → RESERVED로 변경되는 것을 확인할 수 있습니다.

![좌석 상태 변경](images/seat_after.png)

---

### 중복 예약 실패  

이미 예약된 좌석에 대해 다시 요청할 경우 실패 응답을 반환합니다.

![중복 예약 실패](images/duplicate_fail.png)

---

## 동시성 문제 해결 전략  

여러 사용자가 동시에 동일 좌석을 예약하면  
두 트랜잭션이 동시에 예약 가능 상태를 조회하여  
중복 예약이 발생하는 Race Condition 문제가 발생할 수 있습니다.  

이를 해결하기 위해 DB 비관적 락(Pessimistic Lock)을 적용했습니다.

- SELECT ... FOR UPDATE 기반 배타적 락 획득  
- 좌석 조회 시점부터 트랜잭션 종료까지 락 유지  
- 다른 트랜잭션은 해당 좌석 접근 시 대기 상태  

---

## 동시성 테스트 결과  

동일 좌석에 대해 여러 스레드가 동시에 요청을 보냈으며  
1건만 예약 성공하고 나머지는 실패하는 것을 확인할 수 있습니다.  

이를 통해 데이터 정합성이 유지됨을 검증했습니다.

![동시성 테스트](images/concurrency_test.png)

---

## 핵심 예약 처리 로직  

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select s from Seat s where s.id = :id")
Optional<Seat> findByIdWithLock(Long id);
```

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
- 테스트 코드 작성  
- Docker 환경 구성  
- AWS 배포  
- 프론트 UI 개발  

---

## 개발자  

김준희  
Backend Developer (Java / Spring)  

GitHub  
https://github.com/junhi0504  
