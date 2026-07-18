# Health Server

AI-assisted PAPS fitness management service backend for a React Native app.

## Architecture

- React Native stores user profile, PAPS history, AI results, and exercise solutions in local SQLite.
- Spring Boot validates requests, evaluates PAPS records from versioned standards, forwards AI requests, and manages temporary analysis jobs.
- MariaDB stores PAPS reference data, PAPS standard metadata/ranges, and temporary AI job state.
- The server does not provide signup, login, Spring Security, JWT, Redis, Kafka, API Gateway, or MSA.
- Raw posture videos are temporary files and are deleted after analysis success or failure.

## Runtime

- Java 21
- Spring Boot 4.1.0
- Gradle Groovy DSL
- MariaDB or MySQL
- Flyway

## Local Commands

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Run locally with explicit environment variables:

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:DB_URL='jdbc:mariadb://localhost:3306/health'
$env:DB_USERNAME='health'
$env:DB_PASSWORD='change-me'
$env:AI_MODE='mock'
.\gradlew.bat bootRun
```

## PAPS Reference API

- `GET /api/v1/paps/components`
- `GET /api/v1/paps/test-items`
- `GET /api/v1/paps/test-items?component={componentCode}`
- `GET /api/v1/paps/standards/current`

Only active components, active test items, and the single active standard version are returned. Entity IDs are not exposed.

## PAPS Evaluation API

- `POST /api/v1/paps/evaluations`

The server validates profile and measurement inputs, calculates age from `assessmentDate`, calculates BMI from height and weight, and evaluates item-level grades from `PapsStandard` ranges. The request includes `schoolLevel` and `schoolGrade` because official PAPS criteria are organized by school grade, not exact age. Requests and results are not stored in MariaDB.

Current blocker: the repository contains `HACKATHON_V1` metadata and PAPS items, but no verified `paps_standard` range rows. The schema now supports elementary, middle, and high school-grade-based criteria, but real evaluation requests cannot return grades from Seed Data until official criteria or team-approved internal criteria are added.

## AI Analysis APIs

All analysis APIs require `X-Installation-Id`, a client installation UUID. The server hashes this value with SHA-256 and stores only the hash.

- `POST /api/fitness-analyses`
- `POST /api/posture-analyses`
- `GET /api/analysis-jobs/{publicId}`

AI mode is configured through `AI_MODE`:

- `mock`: local/test mock clients.
- `real`: HTTP clients using `AI_BASE_URL`, `AI_API_KEY`, timeout, path, and retry settings.

`AI_FITNESS_MODE` and `AI_POSTURE_MODE` can override each feature independently. For Cloudtype deployment with the included FastAPI posture service, use `AI_FITNESS_MODE=mock`, `AI_POSTURE_MODE=real`, and `AI_POSTURE_PATH=/pose/extract`.

Fitness AI requests are JSON. Posture AI requests are forwarded to `ai-python` as `multipart/form-data` with `exerciseType` and `video` parts. Original video filenames and temp paths are not sent to the client.

## Cloudtype Deployment

Deploy two services from this repository:

- Spring Boot service from the repository root.
- FastAPI posture service from `ai-python` using `ai-python/Dockerfile`.

Spring service environment example:

```env
AI_MODE=mock
AI_FITNESS_MODE=mock
AI_POSTURE_MODE=real
AI_BASE_URL=https://<ai-python-service-url>
AI_POSTURE_PATH=/pose/extract
```

The FastAPI service exposes `GET /health` and `POST /pose/extract`.

## Environment Variables

See `.env.example`. Do not commit real DB passwords, AI API keys, production URLs, or local `.env` files.

## OpenAPI

OpenAPI is disabled by default. Enable it locally with:

```powershell
$env:OPENAPI_ENABLED='true'
```
