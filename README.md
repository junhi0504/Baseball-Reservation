Markdown
# ⚾ Baseball Reservation System

대규모 트래픽 상황에서도 데이터 무결성을 보장하는 **야구 경기 티켓 예매 백엔드 시스템**입니다.  
실제 서비스 수준의 동시성 처리, 트랜잭션 관리, 그리고 도메인 주도 설계(DDD) 경험을 목표로 개발했습니다.

---

## 📌 Project Goal

- **대규모 트래픽 대응**: 예매 오픈 시 발생하는 동시성 문제를 이해하고 해결하는 경험.
- **데이터 무결성 보장**: 중복 예약 방지 및 올바른 트랜잭션 처리 능력 향상.
- **유지보수 가능한 도메인 설계**: 비즈니스 로직을 도메인 엔티티 내부에 캡슐화하는 설계 적용.
- **RESTful API 설계 및 문서화**: Swagger를 통한 직관적인 API 명세 및 테스트 환경 구축.

---

## 🧠 System Architecture

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository (JPA)
  ↓
MySQL
🔥 확장 구조 (실서비스 고려 설계)
실제 서비스 환경을 가정하여, 단일 서버의 한계를 극복하기 위한 아키텍처를 구상했습니다.

Plaintext
Client
  ↓
Nginx (Load Balancer)
  ↓
Spring Boot Server (Multi Instance)
  ↓
Redis (Distributed Lock / Cache)
  ↓
MySQL
📊 ERD & Domain Analysis
코드 스니펫
erDiagram
    User ||--o{ Reservation : manages
    Game ||--o{ Seat : has
    Seat ||--o{ Reservation : booked_by

    User {
        long id PK
        string name
        string email
    }
    Game {
        long id PK
        string stadium
        date gameDate
    }
    Seat {
        long id PK
        string seatNumber
        string status
        long gameId FK
    }
    Reservation {
        long id PK
        long userId FK
        long seatId FK
        timestamp reservedAt
    }
핵심 도메인 규칙
User 1 : N Reservation: 한 사용자는 여러 개의 예매 내역을 가질 수 있습니다.

Seat 1 : 1 Reservation: 하나의 좌석은 오직 한 명의 사용자에게만 예매될 수 있습니다. (중복 예약 방지)

Game 1 : N Seat: 하나의 경기는 여러 개의 좌석을 가집니다.

🔥 Main Features & API Showcase
1. 전체 API 명세 (Swagger UI)
프로젝트 실행 후 모든 API 엔드포인트를 시각적으로 확인하고 직접 테스트할 수 있도록 구축했습니다.

2. 핵심 로직: 예매 취소 API 테스트 성공
@PathVariable 이름 명시적 선언을 통해 400 에러를 해결한 후, 정상적으로 예매 취소가 작동하는 모습입니다.

🚨 Concurrency Handling Strategy (동시성 처리 전략)
해결 전략: 비관적 락(Pessimistic Lock) & 트랜잭션 범위 확장
문제: Seat 조회 후 상태 변경 전에 다른 트랜잭션이 접근하여 동일 좌석을 "예약 가능"으로 인식하는 문제 발생.

해결: 좌석 조회(findById) 단계에서 비관적 락을 적용하여 다른 트랜잭션의 접근을 원천 차단.

효과: 좌석 상태 변경과 예약 저장을 동일 트랜잭션에서 원자적(Atomic)으로 처리하여 데이터 일관성 확보.

핵심 코드
Java
@Transactional
public void reserveSeat(Long userId, Long seatId) {
    // 1. 좌석 조회 (비관적 락 적용 추천)
    Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new RuntimeException("Seat not found"));

    // 2. 좌석 상태 검증
    if (seat.isReserved()) {
        throw new RuntimeException("이미 예약된 좌석입니다.");
    }

    // 3. 도메인 로직 실행 (상태 변경)
    seat.reserve(); 

    // 4. 예약 정보 저장
    Reservation reservation = new Reservation(userId, seat);
    reservationRepository.save(reservation);
}
🧪 Trouble Shooting
✅ API 파라미터 인식 오류 해결 (400 Bad Request)
문제: 특정 컨트롤러에서 경로 변수(@PathVariable)가 인식되지 않아 API 호출 실패.

원인: 컴파일 시 파라미터 이름 보존 문제로 이름 불일치 현상 발생.

해결: @PathVariable(name = "id")와 같이 이름을 명시적으로 선언하여 해결.

✅ N+1 문제 해결
상황: 연관 객체 조회 시 쿼리가 폭발적으로 발생하는 현상 발생.

해결: JPA의 Fetch Join을 적용하여 한 번의 쿼리로 연관 데이터를 가져오도록 개선.

🛠 Tech Stack
Language: Java 17

Framework: Spring Boot 3.x

Data: Spring Data JPA, MySQL

Build Tool: Gradle

API Documentation: Swagger (Springdoc OpenAPI)

👨‍💻 Developer
김준희 Backend Developer

GitHub: https://github.com/junhi0504

Email: junhi0504@naver.com
