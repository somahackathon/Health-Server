# Conventions

## Package Structure

Use `team.soma.teto.health` as the base package.

```text
reference/component
reference/testitem
reference/standard
evaluation/presentation
evaluation/application
evaluation/domain
evaluation/infrastructure
analysis/fitness
analysis/posture
analysis/job
ai/client
ai/dto
file
global/config
global/error
global/response
global/validation
```

## Layer Responsibilities

- `presentation`: controllers and API DTO mapping.
- `application`: use cases and transaction boundaries.
- `domain`: domain models, policies, and domain errors.
- `infrastructure`: persistence and external adapters.
- `global`: shared configuration, response, error, validation, and cross-cutting concerns.

## DTO, Entity, Service, Repository

- Request and response DTOs are separate.
- Request DTOs use Bean Validation.
- Entities are not returned directly from controllers.
- Services contain application behavior; controllers coordinate HTTP only.
- Repositories stay behind application or infrastructure boundaries.
- Persistent entities are limited to PAPS reference data and temporary AI job data. Do not add user account, body profile, PAPS history, exercise solution, or long-term posture feedback entities without a new product decision.
- All `ManyToOne` associations use `FetchType.LAZY` unless a measured use case requires otherwise.
- Flyway migrations are the schema source of truth; production must not rely on `ddl-auto=create` or `ddl-auto=update`.

## Exception Handling

- Use stable error codes.
- Do not expose stack traces or unexpected exception messages to clients.
- Validation errors should include field-level details where useful.
- Expected business failures should use a common exception base.

## API Naming

- URI paths use lowercase kebab-case.
- JSON fields use lower camelCase.
- Correlation IDs use `X-Correlation-Id`.
- Do not encode business version assumptions only in URI paths; use explicit version fields where standards or AI models require them.

## ENUM Rules

- Use uppercase enum constants.
- Persist stable enum names only after confirming migration impact.
- Avoid encoding user-facing Korean labels directly in enum constants.

## Persistence

- `FitnessComponent` and `FitnessTestItem` store supported PAPS reference items.
- `PapsStandardVersion` distinguishes internal and official standard sources.
- `PapsStandard` stores versioned grade ranges only after the standard values are confirmed.
- `AiAnalysisJob` stores asynchronous AI job state and temporary request/result payloads until expiration.

## Tests

- Test names should describe behavior.
- Unit tests cover policies, mappers, and error behavior.
- Web tests cover validation, response shape, and HTTP status.
- Integration tests that need external systems must be profile-gated or use test infrastructure.

## Git and PR

- Branch one task per PR.
- Use Conventional Commit titles.
- Keep commits focused on one logical change.
- PR bodies are written in Korean with 작업 내용, 주요 변경사항, 테스트, 확인이 필요한 사항.
- Do not create stacked PRs unless explicitly requested.

