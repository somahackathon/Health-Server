# Architecture

## Overview

```mermaid
flowchart LR
    RN[React Native App<br/>SQLite source of truth]
    API[Spring Boot Server]
    DB[(MariaDB)]
    FitnessAI[Fitness AI Server]
    PostureAI[Posture AI Server]

    RN --> API
    API --> DB
    API --> FitnessAI
    API --> PostureAI
```

The app keeps user-owned records locally. The server owns shared reference data, validates requests, evaluates PAPS records, and forwards agreed analysis payloads to AI servers.

## Why Local-First

The product does not require signup or login. Keeping long-term user data in RN SQLite reduces server-side privacy risk and avoids introducing identity, authorization, and account recovery flows before they are needed.

## Data Boundary

- Device data: user profile inputs, PAPS records, analysis history.
- Server data: PAPS items, standard versions, server common codes, AI analysis job state.
- Temporary server files: exercise videos only while forwarding posture analysis requests.

## PAPS Evaluation Flow

```mermaid
sequenceDiagram
    participant App as RN App
    participant Server as Spring Server
    participant DB as MariaDB

    App->>Server: Submit PAPS record
    Server->>Server: Validate request
    Server->>DB: Load standard version
    Server->>Server: Evaluate record
    Server-->>App: Return evaluation result
```

Official PAPS standards and temporary self-defined standards must be clearly distinguished in data.

## Fitness AI Analysis Flow

```mermaid
sequenceDiagram
    participant App as RN App
    participant Server as Spring Server
    participant AI as Fitness AI

    App->>Server: Request fitness analysis
    Server->>Server: Validate request
    Server->>AI: Send agreed JSON payload
    AI-->>Server: Return analysis result
    Server-->>App: Return result
```

AI results are fitness-management reference information and must not be described as medical diagnosis.

## Posture Video Analysis Flow

```mermaid
sequenceDiagram
    participant App as RN App
    participant Server as Spring Server
    participant AI as Posture AI

    App->>Server: Upload exercise video
    Server->>Server: Store temporary file
    Server->>AI: Forward video or agreed request
    AI-->>Server: Return posture result
    Server->>Server: Delete temporary video
    Server-->>App: Return result
```

Temporary videos must be deleted after success or failure. Raw videos and video paths must not be stored as long-term server data.

## Future Operations

- Decide whether production OpenAPI docs are private, disabled, or network-restricted.
- Define timeout, retry, and idempotency behavior with the AI team.
- Add observability without logging personal data or video paths.
- Revisit server-side user storage only if the product explicitly introduces accounts or cross-device sync.

