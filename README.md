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

The server validates profile and measurement inputs, calculates age from `assessmentDate`, calculates BMI from height and weight, and evaluates item-level grades from `PapsStandard` ranges. Requests and results are not stored in MariaDB.

Current blocker: the repository contains `HACKATHON_V1` metadata and PAPS items, but no verified `paps_standard` range rows. Real evaluation requests cannot return grades from Seed Data until official criteria or team-approved internal criteria are added.

## AI Analysis APIs

All analysis APIs require `X-Installation-Id`, a client installation UUID. The server hashes this value with SHA-256 and stores only the hash.

- `POST /api/fitness-analyses`
- `POST /api/posture-analyses`
- `GET /api/analysis-jobs/{publicId}`

AI mode is configured through `AI_MODE`:

- `mock`: local/test mock clients.
- `real`: HTTP clients using `AI_BASE_URL`, `AI_API_KEY`, timeout, path, and retry settings.

Fitness AI requests are JSON. Posture AI requests are forwarded as `multipart/form-data` with `metadata` JSON and `video` binary parts. Original video filenames and temp paths are not sent to the client.

## Environment Variables

See `.env.example`. Do not commit real DB passwords, AI API keys, production URLs, or local `.env` files.

## OpenAPI

OpenAPI is disabled by default. Enable it locally with:

```powershell
$env:OPENAPI_ENABLED='true'
```
