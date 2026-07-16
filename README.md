# OpenBake

강의 레포 `DongwooSeo/lecture-examples` (`lectures/msa/step1`) 구조를 기준으로 함.

## 기술 스택

- Java 21, Spring Boot 4.1.0
- PostgreSQL 17, Redis 7
- Gradle (Groovy DSL)

## 실행 방법

```bash
# 1. 인프라 실행
docker compose up -d

# 2. 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 3. 헬스체크
curl localhost:8080/actuator/health
```

## 패키지 구조

```
com.openbake
├── common/exception/    공통 예외 처리
├── common/response/     공통 응답 포맷
├── member/              회원
├── seller/              판매자
├── drop/                드롭(상품·재고)
├── order/               주문
├── payment/             결제
├── settlement/          정산
└── cart/                장바구니
```

각 도메인은 4계층 구조:
- `domain/` — 엔티티, 리포지토리 인터페이스, 도메인 규칙
- `application/` — 서비스, 커맨드/쿼리 DTO
- `infrastructure/` — JPA 리포지토리, 어댑터, 외부 연동
- `presentation/` — 컨트롤러, 요청/응답 DTO
