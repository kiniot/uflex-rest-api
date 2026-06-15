# AGENTS.md

Spring Boot 4.0.6 / Java 25 REST API. DDD with bounded contexts. See `CLAUDE.md` for a longer version of these notes.

## Build & run

```bash
docker compose up -d                                     # dev Postgres on :5432
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run        # profile is mandatory, no default
./mvnw test                                              # all tests
./mvnw test -Dtest=ClassName#methodName                  # single test
```

Required env: `SPRING_PROFILES_ACTIVE`, `JWT_SECRET`, `JWT_EXPIRATION_DAYS`. Dev defaults DB creds to docker-compose values; prod requires `UFLEX_DB_*`. Email features need `EMAIL_USER/PASS/FROM_ADDRESS/FROM_NAME`. Dev uses `ddl-auto: update` — there is **no migration tool** (no Flyway/Liquibase). Scalar UI: `/scalar`, OpenAPI: `/v3/api-docs`.

## Layout — bounded contexts under `com.kiniot.uflex.api`

Contexts: `iam`, `planning`, `organization`, `shared`. Each context repeats:

```
domain/         model/{aggregates,entities,commands,queries,events,valueobjects}, services/ (interfaces), exceptions/
application/    internal/{commandservices,queryservices,eventhandlers,outboundservices/<facet>}, acl/ (facade impls)
infrastructure/ persistence/jpa/repositories, plus tech adapters (identity, hashing, tokens, verification, email, authorization)
interfaces/     rest/{controllers,resources,transform}, acl/ (facade interfaces other contexts call)
```

## Cross-context rules (don't violate)

- **Never import another context's domain types directly.** Producing context exposes a facade in `interfaces/acl/` (e.g. `iam/interfaces/acl/IamContextFacade`); consumer wraps it in `application/internal/outboundservices/acl/External<X>Service` (see `planning/.../ExternalIamService`). Extend the existing facade rather than adding a parallel one.
- **Domain events are the other allowed channel.** Aggregates call `addDomainEvent(...)` (inherited from `AuditableAbstractAggregateRoot`), Spring publishes on save, listeners sit in `<context>/application/internal/eventhandlers/` with `@EventListener`. Example flow: `organization.ClinicAdminRegisteredEvent` → `iam.ClinicAdminRegisteredEventHandler` → assigns `TenantId` to user.

## Persistence conventions

- All aggregates extend `shared/.../AuditableAbstractAggregateRoot<T, ID>` — gives auditing (`@EnableJpaAuditing` is on the main app class), domain events, and a `Persistable.isNew()` override that flips on `@PostLoad`/`@PostPersist` to avoid SELECT-before-INSERT for pre-assigned IDs. Implement `getId()` in the concrete class.
- IDs are **UUID v7** via `com.fasterxml.uuid.Generators.timeBasedEpochGenerator()`, wrapped in `@Embeddable` record value objects (see `iam/.../UserId`, `TenantId`), used as `@EmbeddedId`.
- Tables/columns are auto-named by `SnakeCaseWithPluralizedTablePhysicalNamingStrategy` (entity → snake_case **plural** table; field → snake_case column). Don't set `@Table(name=...)` unless overriding intentionally.

## REST + security

- Controllers in `interfaces/rest/controllers/` only call services + static assemblers (`*FromResourceAssembler`, `*FromEntityAssembler` in `interfaces/rest/transform/`). DTOs are in `resources/`. Keep domain logic out of controllers (see `AuthenticationController`, `UsersController`).
- Service interface in `domain/services/`, impl in `application/internal/{commandservices,queryservices}/` with `Impl` suffix.
- Security is stateless JWT (`iam/.../WebSecurityConfiguration`). Public paths: `/api/v1/authentication/**`, `/v3/api-docs/**`, `/swagger-ui*`, `/scalar/**`, `/actuator/**`. Everything else needs a Bearer token. `@EnableMethodSecurity` is on.
- Startup seeding goes through the `ApplicationReadyEvent` handler (`iam/.../ApplicationReadyEventHandler` → `SeedRolesCommand`). Reuse this pattern; don't add ad-hoc `CommandLineRunner`s.
- Prefer **domain exceptions or shared context exceptions** over generic `IllegalStateException`/`RuntimeException` for business and application failures. If a case is expected enough to matter to API consumers, give it a named exception.
- Any new domain/shared exception that can reach the REST layer must be mapped in `shared/interfaces/rest/GlobalExceptionHandler` so clients receive the correct HTTP status and stable error `code`. See [`docs/reference/error-codes.md`](./docs/reference/error-codes.md).
