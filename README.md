<h1 align="center">uFlex REST API</h1>

<div align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 4.0.6" />
  <img src="https://img.shields.io/badge/PostgreSQL-18-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 18" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white" alt="Maven" />
  <br />
  <img src="https://img.shields.io/badge/Architecture-DDD-blue?style=flat-square" alt="DDD Architecture" />
  <img src="https://img.shields.io/badge/Security-JWT-black?style=flat-square" alt="JWT Security" />
  <img src="https://img.shields.io/badge/Docs-Scalar-00BFFF?style=flat-square" alt="Scalar Docs" />
  <img src="https://img.shields.io/badge/Container-Docker-2496ED?style=flat-square" alt="Docker" />
</div>

---

Backend REST API for the uFlex platform — the central entry point that exposes business logic to web and mobile clients in the KinIoT ecosystem.

## Tech stack

- Java 25, Spring Boot 4.0.6, Maven
- PostgreSQL 18 + Spring Data JPA / Hibernate
- Spring Security with JWT (jjwt 0.12.6)
- springdoc-openapi with Scalar UI
- Thymeleaf (email templates), Spring Mail (Gmail SMTP)
- Lombok

## Getting started

### Prerequisites

- JDK 25 (Temurin recommended)
- Docker (for the local Postgres)

### Run locally

```bash
# 1. Start the dev database (uflex_db_dev / admin / password on :5432)
docker compose up -d

# 2. Export required environment variables
export SPRING_PROFILES_ACTIVE=dev
export JWT_SECRET=<your-secret>
export JWT_EXPIRATION_DAYS=7
# Optional — only needed for OTP / notification emails
export EMAIL_USER=...
export EMAIL_PASS=...
export EMAIL_FROM_ADDRESS=...
export EMAIL_FROM_NAME=...

# 3. Run the app
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

### Environment variables

| Variable                                                               | Required  | Notes                                                       |
|------------------------------------------------------------------------|-----------|-------------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`                                               | yes       | `dev` or `prod` — no default, app fails to start without it |
| `JWT_SECRET`                                                           | yes       | Signing secret for bearer tokens                            |
| `JWT_EXPIRATION_DAYS`                                                  | yes       | Token lifetime in days                                      |
| `UFLEX_DB_HOST`                                                        | prod      | Defaults to `localhost` in dev                              |
| `UFLEX_DB_PORT`                                                        | prod      | Defaults to `5432` in dev                                   |
| `UFLEX_DB_NAME`                                                        | prod      | Defaults to `uflex_db_dev` in dev                           |
| `UFLEX_DB_USERNAME`                                                    | prod      | Defaults to `admin` in dev                                  |
| `UFLEX_DB_PASSWORD`                                                    | prod      | Defaults to `password` in dev                               |
| `EMAIL_USER` / `EMAIL_PASS` / `EMAIL_FROM_ADDRESS` / `EMAIL_FROM_NAME` | for email | Gmail SMTP credentials                                      |

The `dev` profile uses Hibernate `ddl-auto: update`; `prod` uses `validate`. There is no schema-migration tool configured.

## Build & test

```bash
./mvnw clean package                          # build a runnable jar
./mvnw test                                   # run all tests
./mvnw test -Dtest=ClassName                  # run a single test class
./mvnw test -Dtest=ClassName#methodName       # run a single test method
```

## API documentation

Once the app is running:

- Scalar UI — http://localhost:8080/scalar
- OpenAPI JSON — http://localhost:8080/v3/api-docs
- Actuator — http://localhost:8080/actuator

These endpoints, along with `/api/v1/authentication/**`, are publicly accessible. All other endpoints require a `Bearer` JWT in the `Authorization` header — obtain one via `POST /api/v1/authentication/sign-in`.

## Project structure

The codebase follows Domain-Driven Design with bounded contexts under `com.kiniot.uflex.api`:

- `iam/` — users, roles, authentication, JWT, hashing, OTP verification
- `planning/` — treatment plans
- `organization/` — clinic/organization domain events
- `shared/` — cross-cutting infrastructure (auditing, naming strategy, OpenAPI config, email base)

Within each context the layers are: `domain/` (model, services interfaces, exceptions), `application/` (command/query services, event handlers, ACL outbound services), `infrastructure/` (JPA, Spring Security adapters, etc.), and `interfaces/` (REST controllers, ACL facades).

See [`CLAUDE.md`](./CLAUDE.md) and [`AGENTS.md`](./AGENTS.md) for architectural conventions (cross-context ACL pattern, domain-event flow, aggregate base class, UUID v7 IDs, naming strategy).

## Further reading

- [`docs/guides/setup.md`](./docs/guides/setup.md) — initial Spring Initializr setup notes