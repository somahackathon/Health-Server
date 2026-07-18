# Claude Guide

Before working, read `AGENTS.md` first and follow it. If this file conflicts with `AGENTS.md`, `AGENTS.md` is the source of truth.

This project is the Spring Boot backend for a React Native local-first PAPS fitness management service. The server validates requests, evaluates PAPS records using versioned standards, forwards fitness and posture analysis requests to AI servers, temporarily handles exercise videos, and deletes videos after analysis finishes or fails.

## Common Commands

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

## Important Constraints

- Write PR titles in Korean while keeping the Conventional Commit type prefix.
- Do not add signup, login, Spring Security, JWT, OAuth, Redis, Kafka, API Gateway, or MSA structure without an explicit product decision.
- Do not store long-term user records, analysis history, raw videos, or video paths in MariaDB.
- MariaDB stores PAPS reference entities and temporary AI analysis jobs only.
- Treat the RN app SQLite database as the source of truth for user-owned data.
- Do not hard-code PAPS standard logic in Java condition trees.
- Do not present AI results as medical diagnosis.
- Keep `CLAUDE.md` short; shared rules belong in `AGENTS.md`.

