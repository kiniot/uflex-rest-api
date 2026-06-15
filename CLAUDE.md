# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Java 25 + Spring Boot 4.0.6, Maven (wrapper checked in), PostgreSQL, JPA/Hibernate, Spring Security with JWT (jjwt), Lombok, springdoc-openapi (Scalar UI), Thymeleaf for email templates.

## Common commands

```bash
# Bring up the dev Postgres (uflex_db_dev / admin / password on :5432)
docker compose up -d

# Run the app — REQUIRES SPRING_PROFILES_ACTIVE (no default in application.yaml)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# Build / package
./mvnw clean package

# Run all tests
./mvnw test

# Run a single test class or method
./mvnw test -Dtest=UflexRestApiApplicationTests
./mvnw test -Dtest=UflexRestApiApplicationTests#contextLoads
```

Required env vars (see `src/main/resources/application*.yaml`):
- `SPRING_PROFILES_ACTIVE` (`dev` or `prod`) — mandatory, no default
- `JWT_SECRET`, `JWT_EXPIRATION_DAYS` — mandatory in all profiles
- `UFLEX_DB_HOST/PORT/NAME/USERNAME/PASSWORD` — defaulted in `dev` to match docker-compose, must be set in `prod`
- `EMAIL_USER`, `EMAIL_PASS`, `EMAIL_FROM_ADDRESS`, `EMAIL_FROM_NAME` — needed for OTP/notification emails (Gmail SMTP)

`dev` profile uses `ddl-auto: update`; `prod` uses `validate` (no auto-migration tooling like Flyway/Liquibase is configured).

API docs: Scalar UI at `/scalar`, OpenAPI JSON at `/v3/api-docs`. Both are public.

## Architecture

The codebase follows a **DDD bounded-context layout** under `com.kiniot.uflex.api`. Top-level packages are bounded contexts; each context internally repeats the same four-layer structure:

```
<context>/
  domain/         model/{aggregates,entities,commands,queries,events,valueobjects}, services (interfaces), exceptions
  application/    internal/{commandservices,queryservices,eventhandlers,outboundservices/<facet>}, acl/  (facade impls)
  infrastructure/ persistence/jpa/repositories, plus context-specific tech adapters (identity, hashing, tokens, verification, email, authorization)
  interfaces/     rest/{controllers,resources,transform}, acl/  (facade interfaces exposed to other contexts)
```

Current contexts: `iam` (users, roles, auth, JWT, hashing, OTP), `planning` (treatment plans), `organization` (events only — emits `ClinicAdminRegisteredEvent`), and `shared` (cross-context infrastructure and base abstractions).

**Cross-context communication uses the ACL pattern, never direct cross-context imports of domain objects.** A consumer context defines an `outboundservices/acl/External<Other>Service` that calls a facade interface in the producing context's `interfaces/acl` package (e.g. `IamContextFacade`, implemented in `iam/application/acl/IamContextFacadeImpl`). When adding a new cross-context call, extend the existing facade rather than reaching across packages.

**Domain events also cross context boundaries.** Aggregates extend `shared/.../AuditableAbstractAggregateRoot` (which extends Spring Data's `AbstractAggregateRoot`), call `addDomainEvent(...)`, and Spring publishes them on save. Listeners live in `<context>/application/internal/eventhandlers/` annotated with `@EventListener`. Example: `organization` publishes `ClinicAdminRegisteredEvent`; `iam`'s `ClinicAdminRegisteredEventHandler` consumes it to assign a `TenantId` to the user.

### Aggregates and persistence conventions

- All aggregates extend `AuditableAbstractAggregateRoot<T, ID>`. It provides `createdAt`/`updatedAt` (via `@EnableJpaAuditing` on the application class), domain-event registration, and a `Persistable.isNew()` override that flips on `@PostLoad`/`@PostPersist` — this avoids a SELECT-before-INSERT when saving entities with pre-assigned IDs. Concrete aggregates must implement `getId()`.
- IDs are **UUID v7** (`com.fasterxml.uuid.Generators.timeBasedEpochGenerator()`) wrapped in `@Embeddable` record value objects (e.g. `UserId`, `TenantId`, `TreatmentPlanId`, `ClinicId`) and used as `@EmbeddedId`.
- Table/column naming is driven by `SnakeCaseWithPluralizedTablePhysicalNamingStrategy` (in `shared/infrastructure/persistence/jpa/configuration/strategy`). Entity class names become snake_case **pluralized** table names; columns become snake_case. Don't override with `@Table(name=...)` unless there's a real need — let the strategy do it.

### Security

`iam/infrastructure/authorization/sfs/configuration/WebSecurityConfiguration` defines a stateless JWT filter chain. Public matchers: `/api/v1/authentication/**`, `/v3/api-docs/**`, `/swagger-ui*`, `/scalar/**`, `/actuator/**`. Everything else requires a Bearer token validated by `BearerAuthorizationRequestFilter` against `BearerTokenService`. Method-level security is enabled (`@EnableMethodSecurity`).

Roles are seeded on `ApplicationReadyEvent` by `ApplicationReadyEventHandler` → `RoleCommandService.handle(SeedRolesCommand)`. Don't add app-startup logic elsewhere; reuse this pattern.

### REST layer conventions

Controllers live in `<context>/interfaces/rest/controllers`, request/response DTOs in `resources/`, and DTO↔domain mapping in `transform/` as static `*Assembler` classes (e.g. `SignUpCommandFromResourceAssembler.toCommandFromResource(...)`, `UserResourceFromEntityAssembler.toResourceFromEntity(...)`). Controllers should call command/query services and assemblers only — keep domain logic out.

Service interfaces live in `domain/services` (e.g. `UserCommandService`, `UserQueryService`); implementations live in `application/internal/{commandservices,queryservices}` with the `Impl` suffix.

### Exceptions and API errors

- Prefer **named domain exceptions** (or shared context exceptions under `shared/domain/exceptions`) instead of generic `IllegalStateException` / `RuntimeException` for business-relevant failures.
- Keep invariant guards in value objects/commands with `IllegalArgumentException` when appropriate, but when an error represents a meaningful domain/application scenario that the API should expose clearly, create a dedicated exception class.
- Any new exception that can bubble to REST must be handled by `shared/interfaces/rest/GlobalExceptionHandler`; do not add new domain exceptions without wiring their HTTP status mapping.
- Stable API error codes are documented in [`docs/reference/error-codes.md`](./docs/reference/error-codes.md).
