# AI Contract

이 문서는 Spring 서버와 AI 처리 계층 사이의 JSON 경계를 확정한 문서입니다.
`docs/architecture.md`의 "외부 AI 서버로 전달" 구성은 이 프로젝트에서 다음과 같이 구체화되었습니다.

## 1. 아키텍처 개요

AI는 별도의 외부 서버가 아니라 **Spring 서버 내부 모듈**로 존재하며, 실제 LLM 추론은
**Google Gemini API**를 호출해 수행합니다. 자세 분석은 컴퓨터 비전(관절 추출)과
LLM 판단을 분리한 2단계 파이프라인입니다.

```
RN 앱 ──JSON──▶ Spring /api/v1/analysis/fitness ──────────────▶ Gemini API (structured JSON)
RN 앱 ──video─▶ Spring /api/v1/analysis/posture ──video──▶ ai-python /pose/extract (MediaPipe)
                                                ◀─관절 지표(JSON)─┘
                                                ──지표+프롬프트──▶ Gemini API (structured JSON)
```

- **fitness**: Spring이 신체 정보 + PAPS 기록으로 프롬프트를 구성해 Gemini API에 직접 요청합니다.
- **posture**: Spring이 영상을 내부 Python 서브서버(`ai-python/`, FastAPI + MediaPipe)로 전달해
  관절 각도·정렬 지표를 추출한 뒤, 그 지표를 근거로 Gemini API에 자세 판단을 요청합니다.
  Gemini API 키는 Spring에만 존재하며, `ai-python`은 순수 컴퓨터 비전 계산만 수행합니다.
- **처리 방식은 동기(synchronous)로 확정**되었습니다. 앱은 요청 후 같은 HTTP 응답으로 결과를 받습니다.
  Spring은 감사·상태 조회를 위해 `AiAnalysisJob` 행을 기록하지만, 이는 폴링 대상이 아닙니다.

## 2. 공통 헤더

- `X-Correlation-Id`: 요청 추적용. 클라이언트가 보내지 않으면 서버가 생성합니다(`CorrelationIdFilter`).
- `Idempotency-Key`: **사용하지 않는 것으로 확정.** 동기 처리이며 영상 업로드에 대한 무분별한
  재시도를 막기 위해 클라이언트가 실패 시에만 명시적으로 재요청하도록 합니다(§7 참고).

## 3. 체력 분석 (Fitness Analysis)

### 요청

`POST /api/v1/analysis/fitness` (`application/json`)

```json
{
  "installationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "profile": {
    "birthDate": "2011-03-05",
    "gender": "MALE",
    "heightCm": 158.5,
    "weightKg": 47.2,
    "weeklyExerciseFrequency": 3
  },
  "records": [
    { "itemCode": "PUSH_UP", "value": 21, "unit": "COUNT", "measuredAt": "2026-07-10" },
    { "itemCode": "SIT_AND_REACH", "value": 4.5, "unit": "CENTIMETER", "measuredAt": "2026-07-10" }
  ]
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| `installationId` | string | Y | 공백 아님, 최대 100자 |
| `profile.birthDate` | date | Y | 과거 날짜 |
| `profile.gender` | enum | Y | `MALE`, `FEMALE` |
| `profile.heightCm` | number | Y | 50~250 |
| `profile.weightKg` | number | Y | 10~300 |
| `profile.weeklyExerciseFrequency` | int | N | 0~14 |
| `records` | array | Y | 1~50개 |
| `records[].itemCode` | enum | Y | `FitnessTestItemCode` (12종, §5) |
| `records[].value` | number | Y | 0 이상 |
| `records[].unit` | enum | N | `MeasurementUnit` (§5) |
| `records[].measuredAt` | date | Y | 오늘 이하 |

### 응답 (200, `ApiResponse` 봉투)

```json
{
  "success": true,
  "data": {
    "analysisId": "b1e9c4a0-...-uuid",
    "summary": "근력·근지구력은 또래 대비 우수하지만 유연성이 부족한 편입니다.",
    "overallLevel": "AVERAGE",
    "weakAreas": [
      { "itemCode": "SIT_AND_REACH", "componentCode": "FLEXIBILITY", "reason": "또래 평균 대비 낮은 기록입니다." }
    ],
    "solutions": [
      { "title": "햄스트링 스트레칭", "description": "무릎을 편 상태에서 15초씩 3세트 반복합니다.", "frequency": "주 3회" }
    ],
    "disclaimer": "본 분석 결과는 참고용 정보이며 의학적 진단이 아닙니다.",
    "modelVersion": "gemini-3.5-flash-lite"
  },
  "error": null,
  "timestamp": "2026-07-18T05:00:00Z"
}
```

`overallLevel` ∈ `EXCELLENT`, `GOOD`, `AVERAGE`, `NEEDS_IMPROVEMENT`.
`weakAreas[].itemCode`는 항목 단위 약점이 없으면 `null`일 수 있습니다(요인 단위 약점만 존재).
`disclaimer`는 Gemini가 생성하지 않고 **서버가 고정 문자열로 항상 추가**합니다.

## 4. 자세 분석 (Posture Analysis)

### 요청

`POST /api/v1/analysis/posture` (`multipart/form-data`)

| 파트 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `video` | file | Y | `video/mp4` 또는 `video/quicktime`, 최대 20MB, 30초 이내 권장 |
| `installationId` | text | Y | 공백 아님 |
| `exerciseType` | text | Y | `PUSH_UP` 또는 `CURL_UP` |

### 응답 (200, `ApiResponse` 봉투)

```json
{
  "success": true,
  "data": {
    "analysisId": "c7d2...-uuid",
    "exerciseType": "PUSH_UP",
    "postureScore": 78,
    "problemSegments": [
      { "startSec": 3.5, "endSec": 6.0, "bodyPart": "WAIST", "issue": "허리가 아래로 처집니다.", "severity": "MEDIUM" }
    ],
    "improvements": [
      { "bodyPart": "WAIST", "suggestion": "코어에 힘을 주어 어깨-엉덩이-발목이 일직선이 되도록 유지하세요." }
    ],
    "disclaimer": "본 분석 결과는 참고용 정보이며 의학적 진단이 아닙니다.",
    "modelVersion": "gemini-3.5-flash-lite"
  },
  "error": null,
  "timestamp": "2026-07-18T05:00:00Z"
}
```

`postureScore`는 0~100 정수. `severity` ∈ `LOW`, `MEDIUM`, `HIGH`.
`bodyPart` ∈ `HEAD`, `NECK`, `SHOULDER`, `ELBOW`, `WRIST`, `BACK`, `WAIST`, `CORE`, `HIP`, `KNEE`, `ANKLE`, `HAMSTRING`.

## 5. Spring ↔ ai-python 내부 계약

이 구간은 두 서버 모두 이 저장소 내부에 있으므로 앱에는 노출되지 않는 내부 API입니다.

`POST /pose/extract` (`multipart/form-data`) — 파트: `video`(file), `exerciseType`(text)

성공 200:
```json
{
  "exerciseType": "PUSH_UP",
  "durationSec": 18.4,
  "sampledFps": 10,
  "personDetected": true,
  "metrics": {
    "repCount": 8,
    "reps": [
      { "startSec": 1.2, "endSec": 3.4, "minElbowAngleDeg": 95.0, "maxHipSagDeg": 12.5, "neckAlignedRatio": 0.8 }
    ],
    "bodyLineDeviationSeries": [{ "sec": 1.0, "deg": 4.2 }]
  }
}
```

`CURL_UP`의 `metrics`: `{repCount, reps: [{startSec, endSec, maxTrunkLiftDeg, minKneeAngleDeg, maxNeckPullDeg}], kneeAngleStableRatio}`.

오류:
- `400 {"code": "UNSUPPORTED_EXERCISE_TYPE"}`
- `422 {"code": "NO_PERSON_DETECTED"}` — 사람 미검출
- `422 {"code": "VIDEO_DECODE_FAILED"}` — 영상 디코딩 실패

Spring은 이 `metrics` JSON을 그대로 Gemini 프롬프트에 삽입해 자세 판단을 요청합니다.
전체 landmark 원본은 전달하지 않습니다(토큰 낭비 방지, 개인 식별 정보 최소화).

## 6. enum 카탈로그

| enum | 값 |
|---|---|
| `Gender` | `MALE`, `FEMALE` |
| `FitnessTestItemCode` | `SHUTTLE_RUN`, `LONG_RUN_WALK`, `STEP_TEST`, `SIT_AND_REACH`, `TOTAL_FLEXIBILITY`, `PUSH_UP`, `CURL_UP`, `GRIP_STRENGTH`, `SPRINT_50M`, `STANDING_LONG_JUMP`, `BMI`, `BODY_FAT_PERCENTAGE` |
| `MeasurementUnit` | `COUNT`, `SECOND`, `CENTIMETER`, `KILOGRAM`, `PERCENT`, `BMI`, `SCORE` |
| `FitnessComponentCode` | `CARDIO_ENDURANCE`, `FLEXIBILITY`, `MUSCULAR_STRENGTH_ENDURANCE`, `POWER`, `BODY_COMPOSITION` |
| `ExerciseType` (posture 지원 종목) | `PUSH_UP`, `CURL_UP` |
| `overallLevel` | `EXCELLENT`, `GOOD`, `AVERAGE`, `NEEDS_IMPROVEMENT` |
| `severity` | `LOW`, `MEDIUM`, `HIGH` |
| `bodyPart` | `HEAD`, `NECK`, `SHOULDER`, `ELBOW`, `WRIST`, `BACK`, `WAIST`, `CORE`, `HIP`, `KNEE`, `ANKLE`, `HAMSTRING` |

## 7. 오류 코드 카탈로그

| 코드 | HTTP | 의미 | 재시도 가능 |
|---|---|---|---|
| `AI_TIMEOUT` | 504 | Gemini 또는 ai-python 응답 지연 | O |
| `AI_SERVER_ERROR` | 502 | Gemini 4xx/5xx, ai-python 5xx/연결 불가 | O |
| `AI_INVALID_RESPONSE` | 502 | Gemini candidates 누락 또는 결과 JSON 파싱 실패 | O |
| `AI_VIDEO_PROCESSING_FAILED` | 422 | 빈/손상 영상, 사람 미검출 등 | X |
| `AI_VIDEO_TOO_LARGE` | 413 | 영상이 20MB 초과 | X |
| `AI_UNSUPPORTED_VIDEO_TYPE` | 415 | mp4/mov 외 콘텐츠 타입 | X |
| `AI_UNSUPPORTED_EXERCISE_TYPE` | 400 | PUSH_UP/CURL_UP 외 종목 | X |
| `AI_UNKNOWN_ERROR` | 500 | 위에 해당하지 않는 예외 | X |
| `COMMON_PAYLOAD_TOO_LARGE` | 413 | 전체 요청 크기가 25MB 초과 (multipart 전송 한도) | X |
| `COMMON_INVALID_INPUT` | 400 | Bean Validation 실패 | X |

모든 오류는 기존 공통 오류 응답 형식을 그대로 사용합니다:

```json
{
  "success": false,
  "data": null,
  "error": { "code": "AI_TIMEOUT", "message": "AI 분석 응답이 지연되었습니다. 잠시 후 다시 시도해 주세요.", "details": null },
  "timestamp": "2026-07-18T05:00:00Z"
}
```

## 8. 분석 작업(Job) 라이프사이클

동기 응답이지만, Spring은 감사/상태 조회 목적으로 `AiAnalysisJob` 행을 기록합니다.

- 상태: `PENDING → PROCESSING → COMPLETED | FAILED | EXPIRED`
- `requestPayload`는 **메타데이터만** 저장합니다.
  - fitness: 측정 항목 코드 목록과 개수만 (키·몸무게·생년월일 등 개인정보 미저장)
  - posture: `exerciseType`, `sizeBytes`, `contentType`만 (영상 바이트·파일명 미저장)
- `resultPayload`는 Gemini가 생성한 최종 결과 JSON입니다.
- `expiresAt`은 생성 시점 + **24시간**이며, 만료된 행은 별도 정리 대상입니다(본 스프린트 범위 밖).

## 9. 영상 저장 정책

- **Spring**: 업로드된 영상은 메모리에서만 처리합니다(`MultipartFile` → byte[] → Base64/멀티파트 전달 → 즉시 폐기). 디스크에 쓰지 않습니다.
- **ai-python**: 처리 중에만 임시 파일로 존재하며, 성공/실패와 무관하게 `finally` 블록에서 즉시 삭제합니다.
- 두 서버 모두 영상을 영구 저장하지 않으며, 로그에 파일명·영상 경로·영상 바이트를 남기지 않습니다.

## 10. 타임아웃 및 재시도

| 대상 | connect timeout | read timeout |
|---|---|---|
| Spring → Gemini API | 5s | 90s |
| Spring → ai-python | 5s | 60s |

- 재시도는 클라이언트(앱) 판단이며, `error.code`가 재시도 가능(§7 "재시도 가능" 열이 O)한 경우에만 재요청합니다.
- **영상 업로드는 무분별하게 자동 재시도하지 않습니다.** 실패 시 사용자가 명시적으로 다시 시도해야 합니다.
- Idempotency-Key는 사용하지 않습니다(§2).

## 11. 향후 과제 (Future Work)

- 20MB를 초과하는 영상 지원이 필요해지면 Gemini Files API(재개 가능한 업로드) 도입 검토.
- 현재는 요청당 최대 ~150초(포즈 추출 60s + Gemini 90s)까지 Tomcat 워커를 점유합니다. 트래픽이 늘면 비동기 처리(폴링 또는 웹훅)로 전환 검토.
- `AiAnalysisJob` 만료 행에 대한 배치 정리 작업(스케줄러) 추가.
- PAPS 등급 판정(§ evaluation)과 체력 분석 결과를 연계할지 여부는 별도 결정 필요.
