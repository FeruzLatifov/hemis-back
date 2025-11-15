# HEMIS Backend – Project Memory

<!--
This memory file is loaded by Claude Code at startup.  Keep it concise and update it regularly.  See the official
Claude docs for guidance on memory files【296851436055028†L208-L215】.
-->

## Project Overview

HEMIS Backend is a modular monolith built with Java 21 and Spring Boot.  It implements a clean architecture with separate
layers for API, service, domain and security and preserves backwards compatibility with the legacy CUBA platform.

### Golden Rules

- **No manual schema changes** – all database changes must be made through Liquibase changesets; never alter the legacy
  `ministry.sql` schema directly.
- **Service layer** – controllers must delegate to services; business logic belongs in the service layer, not controllers.
- **Security by default** – all endpoints require authentication & authorization (`@PreAuthorize`) and input validation.
- **Swagger & tests** – every endpoint must be documented using Swagger annotations and covered by integration tests;
  unit tests are required for service methods; maintain ≥ 70 % coverage.
- **Idempotent migrations** – migrations must use `IF NOT EXISTS` and include rollbacks; test migrations on staging before
  production.
- **Backward compatibility** – do not break existing APIs or rename legacy fields; migrations must not remove or rename
  columns.

### Daily Commands

- Build the project: `./gradlew clean build`
- Run the application: `./gradlew :app:bootRun`
- Run all tests: `./gradlew test`
- Apply migrations: `./gradlew :domain:liquibaseUpdate`
- Check migration status: `./gradlew :domain:liquibaseStatus`

### Environment & Runtime

The application reads its configuration from environment variables (optionally loaded via a `.env` file).  Key settings:

- **Port** – configured via `SERVER_PORT`.  The internal default is `8081`; override it (e.g. to `8080`) for legacy compatibility.  Do not hard‑code port numbers.
- **Databases** – master and replica PostgreSQL connections are defined by `DB_MASTER_*` and `DB_REPLICA_*`.  These should point to an existing `ministry.sql` schema; never alter legacy tables or columns.
- **Redis** – configured via `REDIS_HOST`, `REDIS_PORT` and `REDIS_PASSWORD`.  Used for caching and token storage.
- **Tests** – tests only run when `TESTS_ENABLED=true` is set.  Use local or staging databases for testing; never run tests against production.

### JWT Notes

Tokens are signed using **HS256** (HMAC‑SHA256).  They contain only the subject (`sub`), username and scope; permissions are cached in Redis.  By default:

- **Access tokens** expire after **12 hours**.
- **Refresh tokens** expire after **7 days**.

You can override `HEMIS_SECURITY_JWT_EXPIRATION` and `HEMIS_SECURITY_JWT_REFRESH_EXPIRATION` in the environment to customise these values.  Legacy OAuth2 clients receive a longer `expires_in` (~30 days) to maintain compatibility.

### Adding a New Endpoint

Follow this checklist when adding a new REST endpoint:

- Create a controller annotated with `@RestController` and define request/response DTOs.
- Define a service interface and implementation; apply `@PreAuthorize` on service methods.
- Add a repository when data access is needed; avoid direct repository calls in controllers.
- Document the controller with `@Tag`, `@Operation` and `@ApiResponses` for each endpoint.
- Write integration tests covering success and error cases (200, 201, 400, 401, 403, 404).
- Write unit tests for the service.
- Test the endpoint in Swagger UI before committing.

### Writing a Migration

- Create files `XX-description.sql` and `XX-description-rollback.sql` in
  `domain/src/main/resources/db/changelog/changesets`.
- Use idempotent DDL and DML (e.g., `CREATE TABLE IF NOT EXISTS`, `INSERT … ON CONFLICT DO NOTHING`).
- Always write a corresponding rollback script.
- Add the changesets to `db.changelog-master.yaml` in order.
- Use `liquibaseRollbackSQL` to preview rollbacks; test forward/rollback locally and on staging.

### Do Not

- Do **not** modify or drop legacy tables or columns.
- Do **not** bypass the service layer.
- Do **not** remove existing fields or change API URLs.
- Do **not** commit code without Swagger documentation and tests.
- Do **not** hardcode secrets or credentials.

### Environment & Runtime

This application reads its configuration from environment variables and an optional `.env` file at the project root.
Before running or testing, load these variables using a tool such as `direnv` or your IDE.  Key variables include:

- **`SERVER_PORT`** – overrides the default HTTP port (`8081`).  Set this to `8080` for backward
  compatibility with legacy clients or choose any free port for development.
- **`DB_MASTER_*` and `DB_REPLICA_*`** – connection details (host, port, name, username, password) for the
  master (write) and replica (read) PostgreSQL databases.  These must point at an existing `ministry.sql`
  schema; never rename or drop tables or columns when migrating.
- **`REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`** – location of the Redis instance used for caching and
  legacy token storage.
- **`TESTS_ENABLED`** – set to `true` to enable unit and integration tests.  If this flag is absent or set
  to `false`, the Gradle `test` task will abort.  Tests automatically load variables from `.env` and use
  either an in‑memory H2 database (app module) or the master database (service and security modules); never
  point tests at a production database.

Create a `.env` file for local development and add it to `.gitignore`.  Do **not** commit `.env` to version
control.

### JWT Notes

The security module issues stateless JSON Web Tokens (JWTs) signed with **HS256** (HMAC‑SHA256).  Important points:

- **Signing secret** – provided via `hemis.security.jwt.secret` in your environment.  Keep this value
  secret; never commit it to the repository.
- **Expiration** – access tokens expire after **12 hours** by default (`hemis.security.jwt.expiration`).  Refresh
  tokens expire after **7 days** (`hemis.security.jwt.refresh-expiration`).  You can override these values via
  environment variables.
- **Claims** – tokens include the subject (`sub`, user ID), `username` and `scope`.  User roles and
  permissions are not stored in the token; they are cached in Redis by user ID.
- **External validation** – the application can optionally validate tokens issued by an external identity
  provider by specifying `JWT_JWK_SET_URI` or `JWT_ISSUER_URI`.  When these are set, the resource server uses
  RS256 key material from the JWK set.  Leave them blank to use the local HS256 secret.

### Monitoring & Observability

Spring Boot Actuator is enabled for health checks and metrics.  Endpoints:

- `GET /actuator/health` – application health (public)
- `GET /actuator/info` – build information (public)
- `GET /actuator/metrics` – JVM and application metrics (protected)
- `GET /actuator/liquibase` – migration status (protected)
- `GET /actuator/env` – environment variables (admin only)

Only the health and info endpoints are publicly accessible.  All other actuator endpoints require a valid
JWT with administrative privileges.  Do not expose sensitive information in logs or metrics.

### Legacy & Migration Notes

This project modernises the old HEMIS backend while keeping existing clients running.  The `api-legacy`
module exposes the old CUBA REST endpoints **unchanged**.  **Do not** rename URLs, change HTTP methods,
modify parameter names or adjust JSON structures in legacy controllers.  Use the service layer to adapt
legacy behaviour without breaking compatibility.

Database changes must be additive.  When migrating data from `ministry.sql`, use Liquibase to add new
tables or columns without renaming or deleting existing structures.  Migrations must be idempotent and
reversible.  Refer to the original system (`old-hemis.zip`) and the exported API definitions
(`hemis_backend.json`) when uncertain about legacy behaviour.

## Further Reading

This file provides a high‑level summary.  For more details consult:

- Detailed architecture and business context: `@context.md`
- Complete coding standards: `@rules.md`
- Swagger documentation guide: `@SWAGGER_GUIDE.md`
- Testing guide: `@TESTING_GUIDE.md`
- Database migration guide: `@LIQUIBASE_GUIDE.md`
- System architecture diagrams: `@architecture.md`

Referencing these files using the `@` syntax allows Claude Code to import them on demand【296851436055028†L92-L111】.