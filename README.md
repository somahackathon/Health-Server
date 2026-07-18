# Health Server

## PAPS Reference Query API

The RN app can query active PAPS reference metadata from the server:

- `GET /api/v1/paps/components`: active fitness components ordered by display order.
- `GET /api/v1/paps/test-items`: active test items for input UI configuration.
- `GET /api/v1/paps/test-items?component={componentCode}`: active test items for one fitness component.
- `GET /api/v1/paps/standards/current`: the currently active standard version.

`official=false` means the active standard version is an internal temporary standard, not an official PAPS source.

## PAPS Evaluation API

- `POST /api/v1/paps/evaluations`: validates profile and measurement inputs, calculates age from `assessmentDate`, calculates BMI on the server, and returns item-level grades using the active standard version.
- Evaluation requests and results are not persisted in MariaDB. The RN app stores returned results in local SQLite.
- The current MVP uses `HIGH` as the school level policy until a product-level school-level contract is confirmed.
- The client must not send `BMI`; the server calculates BMI from height and weight and evaluates it through the `BMI` test item.
- Only one measurement item is allowed per fitness component in a single request.
- Overall grade is not calculated because no official or team-approved aggregation policy is confirmed.

AI 기반 PAPS 체력 관리 서비스의 Spring Boot 백엔드 서버입니다.

React Native 앱은 학생의 생년월일, 성별, 키, 체중, PAPS 측정 기록과 분석 이력을 로컬 SQLite에 저장합니다. 서버는 요청 검증, PAPS 판정, AI 서버 연동, 임시 영상 전달을 담당하며 사용자의 장기 데이터를 영구 저장하지 않습니다.

## 주요 기능

- PAPS 측정 요청 검증
- 버전이 있는 PAPS 기준 데이터 기반 판정
- 체력 분석 AI 서버 연동
- 자세 영상 분석 AI 서버 연동
- 분석 작업 상태 관리
- 임시 영상 삭제 정책 적용

현재 저장소는 초기 Spring Boot 프로젝트 상태이며, 위 기능은 단계적으로 구현 예정입니다.

## 전체 아키텍처

```text
React Native App
  - user profile
  - PAPS records
  - analysis history
  - SQLite source of truth

Spring Boot Server
  - validation
  - PAPS evaluation
  - AI API integration
  - temporary video forwarding

MariaDB
  - PAPS reference data
  - standard versions
  - AI job state

AI Servers
  - fitness analysis
  - posture analysis
```

## 기술 스택

- Java 21
- Spring Boot 4.1.0
- Gradle Groovy DSL
- Spring Web MVC
- Spring Data JPA
- MariaDB JDBC Driver
- MySQL JDBC Driver
- JUnit 5
- Lombok

## 로컬 실행

```powershell
.\gradlew.bat bootRun
```

현재 기본 설정에는 실제 DB 접속 정보가 포함되어 있지 않습니다. 로컬 DB 설정은 환경변수와 profile 설정으로 분리해 추가해야 합니다.

## 환경변수

아직 운영용 환경변수 계약은 확정되지 않았습니다. 향후 DB와 AI 서버 연동이 추가되면 다음 성격의 값은 저장소에 커밋하지 말고 환경변수 또는 배포 환경의 secret으로 주입합니다.

- MariaDB URL
- MariaDB 사용자명
- MariaDB 비밀번호
- AI 서버 base URL
- AI 서버 인증 토큰이 도입되는 경우 해당 토큰

## 테스트

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

## 프로젝트 구조

기본 패키지는 `team.soma.teto.health`입니다.

```text
src/main/java/team/soma/teto/health
├── reference
├── evaluation
├── analysis
├── ai
├── file
└── global
```

## 팀별 연동 관계

- RN 앱 팀: 사용자 입력, 로컬 SQLite 저장, 결과 표시를 담당합니다.
- Spring 서버 팀: 요청 검증, 기준 판정, AI 연동, 임시 영상 처리 정책을 담당합니다.
- AI 팀: 체력 분석과 자세 분석 API를 제공합니다.

AI API 계약은 `docs/ai-contract.md`에서 관리합니다.

## 데이터 저장 정책

- RN SQLite: 사용자 프로필, PAPS 기록, 분석 이력의 원본입니다.
- MariaDB: PAPS 종목, 기준치, 기준 버전, 서버 공통 데이터, AI 분석 작업 상태만 저장합니다.
- 서버 파일시스템: 자세 분석 중 필요한 영상만 임시 저장할 수 있으며 성공 또는 실패 후 삭제해야 합니다.
- 저장 금지: 장기 사용자 기록, raw 영상, 영상 경로, 민감정보, 의료 진단성 데이터.

## 서버 영속성 모델

MariaDB에는 다음 Entity만 서버 공통 데이터로 저장합니다.

- `FitnessComponent`
- `FitnessTestItem`
- `PapsStandardVersion`
- `PapsStandard`
- `AiAnalysisJob`

사용자별 PAPS 평가 이력, 운동 솔루션, 자세 피드백 장기 이력은 서버에 저장하지 않고 RN SQLite에 남깁니다. AI 분석 작업의 요청/결과 payload는 영구 기록이 아니라 만료 전 임시 조회를 위한 데이터이며, 만료 후 삭제 대상입니다.

PAPS 기준은 `PapsStandardVersion`으로 버전을 구분합니다. `HACKATHON_V1`은 자체 기준 버전이며, 실제 PAPS 등급 구간은 공식 자료 또는 팀 합의 후 `PapsStandard` 데이터로 추가해야 합니다.

## 현재 개발 상태

초기 영속성 기반 구축 단계입니다. API, AI 실제 연동, 파일 업로드 기능과 실제 PAPS 등급 기준 데이터는 아직 구현되지 않았습니다.

