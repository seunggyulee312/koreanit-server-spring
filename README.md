# Koreanit Server (Spring Boot)

세션 기반 인증/인가를 적용한 **게시판 백엔드 API 서버**입니다.  
사용자, 게시글, 댓글 도메인을 중심으로 CRUD + 권한 제어를 구현했고, 프론트엔드와 분리된 REST API 구조로 설계했습니다.

---

## 1) 프로젝트 개요

- **프로젝트명**: koreanit-server-spring
- **목적**: 실무형 게시판 API 설계/구현 역량 증명
- **핵심 포인트**
  - Spring Security + HttpSession( Redis 저장소 ) 기반 인증
  - 역할/소유권 기반 인가 (`ADMIN` 또는 본인/작성자)
  - 일관된 API 응답 포맷(`ApiResponse<T>`)과 예외 처리
  - 페이징/유효성 검증/에러 코드 표준화

---

## 2) 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.10
- **Build Tool**: Gradle
- **Database**: MySQL (JDBC)
- **Session Store**: Redis + Spring Session
- **Security**: Spring Security (세션 인증, Method Security)
- **Validation**: Jakarta Validation
- **Logging**: Logback

---

## 3) 프로젝트 구조

```text
src/main/java/com/koreanit/spring
├── common
│   ├── error                  # ApiException, ErrorCode, GlobalExceptionHandler
│   ├── logging                # AccessLogFilter
│   └── response               # ApiResponse 공통 응답 래퍼
├── security
│   ├── cors                   # dev/prod CORS 설정 분리
│   ├── config                 # Method Security 설정
│   ├── SessionAuthenticationFilter
│   ├── SecurityConfig
│   └── AuthController         # /api/login, /api/logout, /api/me
├── user                       # 사용자 도메인 (CRUD, 비밀번호/닉네임/이메일 변경)
├── post                       # 게시글 도메인 (CRUD, 조회수/페이징)
└── comment                    # 댓글 도메인 (등록/조회/삭제)
```

---

## 4) 주요 기능

### 인증/인가
- `POST /api/login` 로그인 시 세션 생성 (`LOGIN_USER_ID` 저장)
- `POST /api/logout` 세션 무효화
- `GET /api/me` 현재 로그인 사용자 조회
- `/api/**` 기본 인증 필요(일부 공개 API 제외)
- 서비스 레이어에서 `@PreAuthorize`로 세밀한 권한 제어
  - 예: 게시글/댓글 수정·삭제는 **ADMIN 또는 작성자만 가능**

### 사용자(User)
- 회원가입, 단건 조회, 목록 조회(ADMIN), 정보 수정(닉네임/비밀번호/이메일), 삭제
- username/email 중복 예외 처리(409)
- 비밀번호 BCrypt 해시 저장

### 게시글(Post)
- 작성/목록/단건/수정/삭제
- 목록 조회 페이징(`page`, `limit`)
- 단건 조회 시 조회수 증가

### 댓글(Comment)
- 댓글 작성/목록/삭제
- 삭제 시 게시글 댓글 수 동기화
- 소유권 기반 삭제 권한 제어

### 공통 응답/예외
- 성공/실패 모두 `ApiResponse` 포맷으로 통일
- 에러 코드 표준화: `INVALID_REQUEST`, `NOT_FOUND_RESOURCE`, `DUPLICATE_RESOURCE`, `UNAUTHORIZED`, `FORBIDDEN`, `INTERNAL_ERROR`

---

## 5) API 요약

### Auth
- `POST /api/login`
- `POST /api/logout`
- `GET /api/me`

### Users
- `POST /api/users`
- `GET /api/users/{id}`
- `GET /api/users?limit=10`
- `PUT /api/users/{id}/nickname`
- `PUT /api/users/{id}/password`
- `PUT /api/users/{id}/email`
- `DELETE /api/users/{id}`

### Posts
- `POST /api/posts`
- `GET /api/posts?page=1&limit=20`
- `GET /api/posts/{id}`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`

### Comments
- `POST /api/posts/{postId}/comments`
- `GET /api/posts/{postId}/comments?before=&limit=20`
- `DELETE /api/comments/{id}`

> 테스트 요청 샘플은 `.apitest/*.http` 파일 참고

---

## 6) 실행 방법

### 1) 사전 준비
- JDK 17
- MySQL
- Redis

### 2) 로컬(dev) 설정
기본 프로필은 `dev`이며 `src/main/resources/application-dev.yml`을 사용합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/koreanit_service?serverTimezone=Asia/Seoul&characterEncoding=utf8
    username: koreanit_app
    password: password
  data:
    redis:
      host: 127.0.0.1
      port: 6379
  session:
    store-type: redis
```

### 3) 실행

```bash
./gradlew bootRun
```

기본 포트: `8000` (환경에 따라 다를 수 있음)

### 4) 빌드

```bash
./gradlew clean build
```

---

## 7) 배포(production) 환경 변수

`application-prod.yml` 기준:

- `PORT`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`

예시:

```bash
PORT=8000 \
DB_URL=jdbc:mysql://<host>:3306/<db>?serverTimezone=Asia/Seoul&characterEncoding=utf8 \
DB_USER=<user> \
DB_PASSWORD=<password> \
REDIS_HOST=<redis-host> \
REDIS_PORT=6379 \
java -jar build/libs/spring-0.0.1-SNAPSHOT.jar
```

---

## 8) 트러블슈팅 / 개선 포인트

### 구현 완료
- 세션 기반 인증 필터 커스텀 주입
- Method Security 기반 소유권/역할 인가
- 공통 예외 응답 포맷 적용
- 게시글 페이징 및 조회수/댓글수 관리

### 향후 개선
- Swagger/OpenAPI 문서화
- 테스트 코드(단위/통합) 보강
- Refresh/Remember-me 정책 고도화
- CORS 허용 도메인 운영환경 기준 세분화
- DB 마이그레이션 도구(Flyway/Liquibase) 도입

---

## 9) 기능 포인트

- 단순 CRUD를 넘어서 **인증/인가 경계**를 명확히 설계했다는 점
- 컨트롤러가 아닌 서비스 계층에서 `@PreAuthorize`로 **도메인 권한 정책**을 구현한 점
- 프론트엔드 연동을 고려한 **일관된 응답 스키마** 및 HTTP 상태/에러코드 관리
- Redis 세션 저장소를 통해 **확장 가능한 인증 구조**를 고려한 점

---

## 10) 저장소

- GitHub: https://github.com/seunggyulee312/koreanit-server-spring
