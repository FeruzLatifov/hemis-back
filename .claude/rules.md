# HEMIS Backend – Coding Standards

---

## Golden Rules

1. **Stability first:** Do not break existing behaviour or APIs. Backward compatibility with legacy systems is mandatory.
2. **No direct schema changes:** All database alterations via Liquibase migrations. Never `ALTER`, `DROP` or `RENAME` on legacy tables.
3. **Service layer required:** Controllers delegate to services. Repositories never called directly from controllers.
4. **Security by default:** All endpoints require authentication and authorisation. Validate input; no raw SQL; no exposed internal exceptions.
5. **Documentation & tests mandatory:** Every endpoint → Swagger annotations + integration test. Every service method → unit test. Minimum coverage 70%.
6. **Idempotent migrations:** Safe to run multiple times; always include rollback. Test forward + rollback on staging first.
7. **No hardcoded secrets:** Use environment variables; never commit secrets.

---

## Module Guidelines

### `common` — DTOs, exceptions, utilities
- Use Lombok (`@Value`, `@Builder`, `@Data`) and Jackson (`@JsonProperty`) for legacy field names
- NO Spring dependencies, NO business logic, NO entity classes

### `domain` — Entities, repositories, migrations
**Entities:**
- Map exactly to legacy tables: `@Table(name="...")`, `@Column(name="...")`
- **Eski CUBA jadvallar** → extend `BaseEntity` (audit: `create_ts/update_ts/delete_ts`)
- **Yangi jadvallar** → extend `AuditableEntity` / `ReferenceEntity` / `ImmutableEntity` (audit: `created_at/updated_at/deleted_at`)
- Never rename/drop existing CUBA columns — add new columns instead
- Do NOT use `@Data` on entities (lazy loading / equality issues)

**Audit ustunlari standarti (yangi jadvallar):**

| Jadval turi | Ustunlar |
|---|---|
| **Entity** (employee, users, university_*) | `version`, `created_at/by`, `updated_at/by`, `deleted_at/by` (7) |
| **Classifier** (genders, soato, positions) | `version`, `created_at/by`, `updated_at/by` (5) + `is_active` |
| **Immutable** (lifecycle, password_history) | `created_at`, `created_by` (2) |
| **Junction** (user_roles, role_permissions) | `created_at` (1) |

**Jadval/ustun nomlash (yangi jadvallar):**
- Java entity: **camelCase** (`academicDegree`, `createdAt`) — Spring Boot avtomatik `academic_degree`, `created_at` ga moslab oladi
- Yangi entity'larda `@Column(name="...")` **yozilmaydi** — Spring Boot `CamelCaseToUnderscoresNamingStrategy` ishlaydi
- Eski CUBA entity'larda `@Column(name="...")` **qoladi** (eski ustun nomlari pattern'ga mos kelmaydi)
- Jadval nomi: ingliz tili, **singular** (`employee`, `gender`, `academic_degree`), prefix YO'Q
- PK: `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- Mavjud plural jadvallar (`users`, `roles`, `positions`) RENAME qilinmaydi — exception
- Eski CUBA jadvallar (`hemishe_*`) TEGILMAYDI

**Repositories:**
- Extend `JpaRepository<Entity, ID>`. Use method naming conventions (`findByEmail`, `existsById`)
- `@Query` for complex queries. Prefer pagination/projections for large collections
- Soft-delete instead of hard-delete

**Migrations:**
- Path: `domain/src/main/resources/db/changelog/changesets/`
- Naming: `20251115-01-add-photo-column`. Always provide rollback
- Idempotent: `IF NOT EXISTS`, `ON CONFLICT DO NOTHING`
- Never modify existing changesets — create new ones. See `@LIQUIBASE_GUIDE.md`

### `security` — Authentication & authorisation
- `@PreAuthorize` on service methods for permissions
- Cache user authorities in Redis. BCrypt for passwords; never store plain text
- Security config stays in this module — don't duplicate in controllers

### `service` — Business logic
- `@Service` + `@Transactional`. Use `readOnly=true` for queries
- `@Valid` for input validation. MapStruct for entity ↔ DTO mapping
- Throw custom exceptions (`ResourceNotFoundException`, `ValidationException`) — don't return nulls
- NO business logic in controllers or repositories

### `api-web`, `api-legacy`, `api-external` — Presentation layer
- `@RestController` + `@RequestMapping`. Return `ResponseWrapper<T>` with proper HTTP status codes
- Swagger annotations on EVERY endpoint: `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`
- `@Valid` on request bodies. Integration test for each endpoint (success + error scenarios)
- `api-legacy`: maintain backwards-compatible URLs, JSON structures and HTTP statuses

---

## Technical Standards

### Exception Handling
- Custom exception hierarchy (`ResourceNotFoundException`, `ValidationException`, etc.)
- `@RestControllerAdvice` + `@ExceptionHandler` for structured error responses
- Include error code, message, timestamp in payloads

### Validation
- Jakarta Bean Validation: `@NotBlank`, `@Email`, `@Positive`, `@Past` on DTO fields
- Custom constraints (e.g. `@UniqueEmail`) for business rules
- Validate at service boundary, not controllers

### Transactions
- `@Transactional` on write methods. `readOnly=true` routes to read replica
- Avoid `REQUIRES_NEW` unless absolutely necessary
- Let Spring handle rollbacks via exception throwing

### Logging
- SLF4J + Logback with `@Slf4j`. Levels: DEBUG (dev), INFO (business events), WARN (recoverable), ERROR (errors)
- Audit create/update/delete at INFO level. Structured key-value pairs when possible
- NEVER log passwords, tokens, personal data. No `System.out.println`

### MapStruct Mapping
- `@Mapper(componentModel = "spring")`. Methods for single + collection conversion
- Ignore ID and audit fields on create/update: `@Mapping(target="id", ignore=true)`
- `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` for partial updates

### Code Style
- **PascalCase** classes, **camelCase** methods/variables, **lowercase** packages
- 4 spaces indentation; max 120 chars per line
- Import order: JDK → third-party → Spring → project
- `@RequiredArgsConstructor` for constructor injection
