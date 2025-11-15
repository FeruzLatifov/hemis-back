# HEMIS Backend – Core Coding Standards

> **Mandatory coding standards for all contributions**  
> **Last Updated:** 2025‑11‑15

---

## 🎯 Golden Rules

These rules apply across all modules and MUST be followed at all times:

1. **Stability first:** Do not break existing behaviour or APIs. Backward compatibility with legacy systems is mandatory.
2. **No direct schema changes:** All database alterations must be performed through Liquibase migrations. Never run `ALTER`, `DROP` or `RENAME` on legacy tables.
3. **Service layer required:** Controllers must delegate to services; business logic lives in services. Repositories should never be called directly from controllers.
4. **Security by default:** All endpoints require authentication and authorisation. Input must be validated; do not use raw SQL or expose internal exceptions.
5. **Documentation & tests mandatory:** Every endpoint requires Swagger annotations and an integration test. Every service method requires a unit test. Minimum test coverage is 70 %.
6. **Idempotent migrations:** Migration scripts must be safe to run multiple times and include rollback instructions. Always test forward and rollback on staging before production.
7. **No hardcoded secrets:** Use environment variables or configuration; never commit secrets to the repository.

---

## 📁 Module Guidelines

### `common` – Shared DTOs and utilities

**Purpose:** Houses plain data objects and helper classes used across modules.

- ✅ **Do:**
  - Create DTOs using Lombok annotations such as `@Value`, `@Builder` or `@Data`.
  - Use Jackson annotations (`@JsonProperty`) to maintain legacy field names.
  - Document public APIs with JavaDoc.
- ❌ **Don’t:**
  - Include Spring dependencies or business logic.
  - Use entity classes from the domain module.
  - Put services or repositories here.

### `domain` – Entities, repositories, migrations

**Purpose:** Defines the persistence model and manages database interactions.

**Entity Rules:**

- Map entities exactly to legacy tables using `@Table(name="...")` and `@Column(name="...")`.
- Extend `BaseEntity` for audit fields (`createdAt`, `updatedAt`).
- Define indexes with `@Index`.
- Avoid renaming or dropping existing columns; add new columns instead.

**Repository Rules:**

- Extend `JpaRepository<Entity, ID>` or `CrudRepository`.
- Use method naming conventions (`findByEmail`, `existsById`).
- Add `@Query` annotations for complex queries and use projections to limit result size.
- Avoid eager fetching and prefer pagination or projections for large collections.
- Implement soft‑delete behaviour instead of overriding delete methods.

**Migration Rules:**

- Store changesets in `domain/src/main/resources/db/changelog/changesets/`.
- Use sequential IDs such as `20251115-01-add-photo-column`.
- Provide a rollback for every change.
- Use idempotent statements (`IF NOT EXISTS`, `ON CONFLICT DO NOTHING`).
- Do not modify existing changesets; create new ones for each change.
- See `LIQUIBASE_GUIDE.md` for detailed instructions.

### `security` – Authentication & authorisation

**Purpose:** Manages JWT, permission checks and security configuration.

- Use `@PreAuthorize` annotations on service methods to enforce permissions.
- Validate JWT tokens and cache user authorities.
- Encode passwords with BCrypt; never store plain text.
- Keep security configuration in a dedicated module; do not duplicate in controllers.

### `service` – Business logic

**Purpose:** Encapsulates domain operations, validation and transactions.

- Annotate classes with `@Service` and methods with `@Transactional` as needed.
- Use `readOnly=true` for queries and default transactions for writes.
- Validate input using `@Valid` and custom validators.
- Convert entities to DTOs using MapStruct mappers before returning responses.
- Throw and handle custom exceptions (`ResourceNotFoundException`, `ValidationException`) rather than returning nulls.
- Do not perform business logic in controllers or repositories.

### `api-web`, `api-legacy`, `api-external` – Presentation layer

**Purpose:** Exposes RESTful endpoints for web clients, legacy consumers and external integrations.

- Annotate controllers with `@RestController` and set a base `@RequestMapping`.
- Return responses wrapped in `ResponseWrapper<T>` and appropriate HTTP status codes.
- Apply Swagger annotations (`@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`) to every endpoint.
- Validate input parameters and request bodies using `@Valid`.
- Do not place business logic in controllers; call service methods instead.
- Write integration tests for each endpoint covering success, validation errors, unauthorised and forbidden scenarios.
- For legacy controllers (`api-legacy`), maintain backwards‑compatible URLs, JSON structures and HTTP statuses.

---

## 🧑‍💻 Technical Standards

### Exception Handling

- Create a hierarchy of custom exceptions for business errors (e.g. `ResourceNotFoundException`, `ValidationException`).
- Use `@RestControllerAdvice` with `@ExceptionHandler` methods to centralise error handling and return structured error responses.
- Provide error codes, messages and timestamps in error payloads.

### Validation

- Leverage Jakarta Bean Validation (JSR 380) annotations (`@NotBlank`, `@Email`, `@Positive`, `@Past`) on DTO fields.
- Create custom constraints (e.g. `@UniqueEmail`) when business rules require additional validation.
- Validate input at the service boundary rather than in controllers.

### Transactions

- Mark methods that modify data with `@Transactional` to ensure atomicity.
- Use `readOnly=true` for read operations to route to read replicas.
- Avoid manual transaction management or `REQUIRES_NEW` propagation unless absolutely necessary.
- Let Spring’s transaction manager handle rollbacks by throwing exceptions.

### Logging

- Use SLF4J with Logback; annotate classes with `@Slf4j`.
- Log at appropriate levels: `DEBUG` for development details, `INFO` for business events, `WARN` for recoverable issues and `ERROR` for errors.
- Never log sensitive information such as passwords, tokens or personal data.
- Do not use `System.out.println` for logging.

### Mapping between Entities and DTOs

- Use MapStruct (`@Mapper(componentModel = "spring")`) to generate boilerplate mappers.
- Provide methods for converting single objects and collections.
- When mapping create/update DTOs to entities, ignore ID and audit fields (`@Mapping(target="id", ignore=true)`).
- Write custom mapping methods where needed (e.g. ignoring relationships to avoid lazy loading issues).

### Code Style & Conventions

- Use **PascalCase** for classes and **camelCase** for methods and variables.
- Use 4 spaces for indentation; no tabs.
- Keep line length under 120 characters and break long expressions at logical points.
- Order imports: JDK, third‑party, Spring, then project packages.
- Name packages in lowercase (e.g. `uz.hemis.service.impl`).
- Use `@RequiredArgsConstructor` for constructor injection and Lombok on DTOs (`@Data`, `@Builder`, `@Value`).
- Avoid using Lombok’s `@Data` on JPA entities to prevent lazy loading and equality issues.

### Logging & Monitoring

- Audit important user actions (e.g. create, update, delete) at `INFO` level.
- Use structured logging with key–value pairs when possible.
- Integrate with application performance monitoring (APM) tools to trace requests and capture errors.

---

## 📦 Further Standards

- **Documentation:** See `SWAGGER_GUIDE.md` for required Swagger annotations and examples.
- **Testing:** See `TESTING_GUIDE.md` for mandatory unit and integration testing practices.
- **Migrations:** See `LIQUIBASE_GUIDE.md` for detailed database migration instructions.
- **Architecture & Context:** See `architecture.md` and `context.md` for system design and project background.

---

**Remember:** clean, consistent and well‑tested code saves time in the long run. Following these standards ensures maintainability and reliability of the HEMIS backend.