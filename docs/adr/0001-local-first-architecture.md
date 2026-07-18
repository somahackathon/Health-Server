# ADR 0001: Local-First User Data Architecture

## Status

Accepted

## Context

The service does not include signup or login. The RN app can store user profile inputs, PAPS records, and analysis history locally in SQLite. The Spring server is needed for request validation, PAPS standard evaluation, AI integration, common reference data, and temporary video forwarding.

## Decision

Use RN SQLite as the source of truth for user-owned long-term data. Store only server common data in MariaDB.

## Alternatives

### Server User Account Storage

This would support cross-device sync and centralized history but requires account management, authentication, authorization, password or OAuth flows, and stronger privacy operations. It is not aligned with the current product scope.

### Anonymous Device ID Server Storage

This avoids signup but still creates long-term server-side user tracking and data retention obligations. It also needs device identity lifecycle handling and deletion policy.

### RN SQLite Storage

This keeps user-owned data on the device, reduces server privacy risk, and matches the no-auth product direction. It limits cross-device sync unless a future account model is introduced.

## Consequences

- The server must not assume it can query user history from MariaDB.
- API requests should include the data needed for each evaluation or analysis.
- Long-term analysis history display is an RN responsibility.
- Future cross-device sync requires a separate architecture decision.

