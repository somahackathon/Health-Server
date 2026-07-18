# Agent Guide

## Project Purpose

This repository is the Spring Boot backend for an AI-assisted PAPS fitness management service for adolescents. The React Native app collects student profile inputs, PAPS measurements, and exercise videos. The server validates requests, evaluates PAPS records against versioned standards, relays analysis requests to separate AI services, and returns results to the app.

## System Structure

- React Native app: Owns long-term user records and analysis history in local SQLite.
- Spring Boot server: Handles validation, PAPS evaluation, AI integration boundaries, common server data, and temporary video forwarding.
- MariaDB: Stores server-managed reference data such as PAPS items, standard versions, and AI analysis job state. It must not store long-term personal fitness records, raw videos, or local app history.
- AI servers: Provide fitness analysis and posture analysis through agreed JSON APIs.

## Server Responsibilities

- Validate incoming API requests.
- Evaluate PAPS records using versioned data, not hard-coded Java condition trees.
- Distinguish official PAPS standards from temporary self-defined standards until official criteria are secured.
- Forward agreed JSON payloads to AI services.
- Temporarily receive exercise videos only for posture analysis forwarding.
- Delete temporary videos after analysis success or failure.
- Return AI results as fitness-management reference information, not medical diagnosis.

## Things The Server Does Not Do

- No signup or login.
- No Spring Security, JWT, OAuth, or account-based authorization unless the product direction changes explicitly.
- No permanent storage of user fitness history or posture videos.
- No MSA, Kafka, Redis, API Gateway, or distributed architecture by default.
- No business rules should be finalized from unclear requirements.
- Persistent server entities are limited to PAPS reference data and temporary AI analysis jobs unless the product direction explicitly changes.

## Package Structure

Use a domain-centered modular monolith under `team.soma.teto.health`.

```text
team.soma.teto.health
├── reference
│   ├── component
│   ├── testitem
│   └── standard
├── evaluation
│   ├── presentation
│   ├── application
│   ├── domain
│   └── infrastructure
├── analysis
│   ├── fitness
│   ├── posture
│   └── job
├── ai
│   ├── client
│   └── dto
├── file
└── global
    ├── config
    ├── error
    ├── response
    └── validation
```

## Code Style

- Keep changes narrowly scoped to the requested task.
- Do not add unused abstractions, interfaces, entities, utilities, or example code.
- Do not put business logic in controllers.
- Do not return entities directly from APIs.
- Separate request DTOs and response DTOs.
- Validate request DTOs with Bean Validation.
- Do not throw broad `RuntimeException` for expected domain failures.
- Avoid server-default timezone assumptions. Prefer injectable time sources for new common code.
- Do not log sensitive user data, request bodies containing personal data, or video paths.
- Do not commit secrets, real credentials, or `.env`.
- Use Lombok only if it is already present and improves consistency.

## Testing Policy

- Keep tests focused on the changed behavior.
- Use JUnit 5 for tests.
- Add broader coverage when touching shared contracts, global error handling, request validation, or API response formats.
- Do not make production code meaningless just to satisfy tests.
- Do not leave commits in a failing test state.

## Commands

This project uses Gradle Groovy DSL with the Gradle Wrapper.

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

On Unix-like CI runners:

```sh
./gradlew test
./gradlew build
```

## Before Starting Work

- Check the current branch and working tree with `git status --short --branch`.
- Identify the default branch and remote.
- Review build files before changing dependencies or versions.
- Inspect existing docs, workflows, and project conventions.
- Confirm whether user changes or untracked files exist. Never delete or overwrite them without explicit permission.

## Before Finishing Work

- Run the relevant tests and build command.
- Review `git diff`.
- Check that no secrets, local credentials, generated build outputs, or temporary videos are staged.
- Confirm the branch, commit message, and PR body match the requested scope.

## Git Rules

Use independent branches per task:

- `chore/ci-pipeline`
- `chore/ai-harness`
- `chore/global-setup`

Use Conventional Commit titles with these types only:

- `feat`
- `fix`
- `refactor`
- `test`
- `docs`
- `chore`
- `ci`
- `build`

PR titles must use the Conventional Commit format and write the summary in Korean, for example `docs: AI 에이전트 개발 하네스 추가`.
PR bodies must be written in Korean and include:

- `## 작업 내용`
- `## 주요 변경사항`
- `## 테스트`
- `## 확인이 필요한 사항`

