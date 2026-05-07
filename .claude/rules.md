# HEMIS Backend – Coding Standards (v3.0)

**Oxirgi yangilanish:** 2026-05-02
**Status:** Active
**Scale:** 230 universitet, ~1.15M talaba (Vazirlik miqyosi)

---

## Golden Rules

1. **Stability first:** Do not break existing behaviour or APIs. Backward compatibility with legacy systems is mandatory.
2. **No destructive schema changes:** All database alterations via Liquibase migrations. Never `ALTER`, `DROP`, `RENAME` structure on legacy `hemishe_*` tables (DML `INSERT`/`UPDATE` ruxsat etiladi).
3. **Single Source of Truth:** Har business concept uchun bitta jadval. Dublikat jadval yaratilmaydi.
4. **Service layer required:** Controllers delegate to services. Repositories never called directly from controllers.
5. **Security by default:** All endpoints require authentication and authorisation. Validate input; no raw SQL; no exposed internal exceptions. OWASP Top 10:2025 checklist mandatory.
6. **Documentation & tests mandatory:** Every endpoint → Swagger annotations + integration test. Every service method → unit test. Minimum coverage 70% (service layer 90%) — enforced via Jacoco gate.
7. **Idempotent migrations:** Safe to run multiple times; always include rollback. Test forward + rollback on staging first.
8. **No hardcoded secrets:** Use environment variables; never commit secrets. Rotation policy: JWT 90 days, DB password 180 days.
9. **AOP self-invocation awareness:** `@Cacheable`, `@Transactional`, `@Async`, `@PreAuthorize` on private methods or same-class calls **do NOT work** — Spring proxy bypasses them. Extract to separate `@Service` bean.
10. **Java 25 modern features:** Prefer `record` for DTOs, pattern matching for `switch`, sealed classes for closed hierarchies. **Forbidden:** Lombok `@Data` on JPA entities (triggers N+1 via `equals`/`hashCode`).
11. **External integration timeout:** Hozir RestTemplate'da global timeout aniq belgilanmagan — har yangi integration'da `RestClient.Builder().requestFactory(...)` orqali connect+read timeout (10s/30s) sozlanishi kerak.
12. **Cache invariant:** Every `@Cacheable` MUST have a corresponding `@CacheEvict` on mutation methods. Cache name MUST be configured in `DashboardCacheConfig.TwoLevelCacheManager` with explicit TTL.

---

## Database Schema Architecture (v3.0)

HEMIS **modular monolith + bounded context** asosida quriladi.

### Real holat (V001-V014, 2026-05-07)

Hozir **barcha yangi jadvallar `public` schema'da** yaratilgan (PostgreSQL default). Quyidagi domain bo'linish faqat **logical** — JPA package strukturasida (`uz.hemis.domain.entity.security`, `.employee`, `.infrastructure`, …), lekin DB darajasida fizik schema separation YO'Q.

```
PostgreSQL: ${DB_MASTER_NAME}    (lokal: test1_hemis, prod: turli)
│
└── public schema (HOZIRGI HAQIQIY HOLAT):
    │
    ├── Eski CUBA (TEGILMAYDI — Univer 224 OTM bilan moslik):
    │   ├── hemishe_e_*   — eski CUBA operational jadvallar
    │   ├── hemishe_h_*   — eski CUBA classifier (102 ta — yagona manba)
    │   ├── hemishe_r_*   — eski CUBA junction
    │   └── sec_user, sec_role, sec_permission — eski auth
    │
    ├── Yangi auth (V001, V002, V006, V007):
    │   ├── role, permission, user_role, role_permission
    │   ├── users (PLURAL — PostgreSQL reserved word)
    │   ├── password_history, password_reset_token
    │   └── oauth_client, oauth_client_role
    │
    ├── Yangi HR (V003, V004):
    │   ├── employee, employee_job
    │   ├── employee_academic_credential
    │   └── h_position_type, h_position (h_* — ADR-0006 klassifikator)
    │
    ├── Yangi university (V005, V008, V009, V011):
    │   ├── organization, university_profile
    │   ├── university_founder, university_lifecycle
    │   ├── university_building, building_lifecycle
    │   └── h_building_category, h_construction_material, h_roof_type (ADR-0006)
    │
    ├── Yangi UI (V014):
    │   └── menu, user_favorite
    │
    └── Yangi i18n (V012, V013):
        ├── language, configuration
        └── system_message, system_message_translation
```

### Maqsadli holat (kelajakdagi migration — Plan only)

Kelajakda alohida fizik schema separation tavsiya etiladi: `auth.*`, `hr.*`, `univ.*`, `ui.*`, `i18n.*`. Buning uchun alohida ADR yoziladi. Hozircha bunga tayyorgarlik yo'q — barcha entity `@Table(name = "...")` schema-siz yoziladi (default `public`).

---

## Jadval yaratish qoidalari

### ❌ HECH QACHON YANGI JADVAL YARATILMAYDI

1. **Agar `hemishe_h_X` mavjud bo'lsa** — bu classifier uchun yangi jadval yaratish taqiqlanadi.
   - Entity eski jadvalga to'g'ridan-to'g'ri map qilinadi (`@Table(name="hemishe_h_gender")`)
   - Modern field nomlari Java tomonda `@Column` annotation orqali (`@Column(name="active")` Boolean `isActive`)

2. **Faqat naming convention uchun** — `active → is_active`, `create_ts → created_at` — dublikat yaratilmaydi. Hibernate `@Column` bilan hal qilinadi.

3. **Faqat audit pattern uchun** — eski CUBA audit (`create_ts`, `delete_ts`) yetarli. `extends BaseEntity` va `@SQLRestriction("delete_ts IS NULL")` bilan.

### ✅ YANGI JADVAL QAYSI HOLATLARDA YARATILADI

Faqat **yangi business concept** uchun, ya'ni `hemishe_*` da mavjud bo'lmagan narsa:

- Yangi autentifikatsiya (BCrypt, JWT sessions)
- Yangi RBAC (role, permission, user_role)
- Yangi i18n (system_message, language)
- Yangi UI (menu, user_favorite)
- Yangi domen (university_legal, building, employee ning yangi dizayn)

### ✅ YANGI JADVAL YARATILSA — schema va naming qoidalari

- **Schema (hozir):** `public` (default — V001-V014 hammasi shu yerda). Domen schema separation kelajakdagi reja, ADR talab qiladi.
- **Jadval nomi:** singular, lowercase, underscore separator (`employee_job`, `building_lifecycle`)
- **Naming istisno (PostgreSQL reserved words):** `users`, `orders`, `groups` — PLURAL ishlatiladi (`user` keyword bilan to'qnashishni oldini olish). Boshqa hech qaysi jadval PLURAL emas.
- **Prefix:** `h_` faqat ADR-0006 mezoni bo'yicha (klassifikator + FK target + sync). Boshqa jadvallar prefiks-siz.
- **PK:** `id UUID PRIMARY KEY DEFAULT gen_random_uuid()` (yangi jadvallar). Klassifikator (`h_*`) — `code VARCHAR(20) PRIMARY KEY`.
- **Audit (modern pattern):**

| Jadval turi | Ustunlar | Base class |
|---|---|---|
| Operational entity | `version, created_at/by, updated_at/by, deleted_at/by` | `AuditableEntity` |
| Reference (classifier) | `version, created_at/by, updated_at/by, is_active` | `ReferenceEntity` |
| Immutable (log) | `created_at, created_by` | `ImmutableEntity` |
| Junction (N:N) | `created_at` | — |

### FK qoidalari — yangi jadvallardan

Yangi jadval boshqa jadvalga FK qo'yganda:

1. **Classifier FK** — har doim eski `hemishe_h_*` ga:
   ```sql
   gender_code VARCHAR(20) REFERENCES public.hemishe_h_gender(code)
   ```

2. **Eski entity FK** — har doim eski `hemishe_e_*` ga:
   ```sql
   student_id UUID REFERENCES public.hemishe_e_student(id)
   ```

3. **Yangi entity FK** — schema bilan:
   ```sql
   employee_id UUID REFERENCES hr.employee(id)
   ```

### Extra ustun kerak bo'lsa — Extension Table Pattern

Eski `hemishe_h_*` jadvaliga qo'shimcha ustun kerak bo'lsa, **ALTER yo'q**. O'rniga extension table:

```sql
CREATE TABLE ref_ext.certificate_language_ext (
    code VARCHAR(20) PRIMARY KEY
        REFERENCES public.hemishe_h_certificate_language(code),
    certificate_type_code VARCHAR(20) NOT NULL,
    extra_attribute VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Java tomonida composition pattern orqali birlashtirilishi mumkin.

---

## Entity qoidalari

### Entity qoidalari — Senior Patterns (v3.0)

**MAJBURIY:**
- `@Getter @Setter` (NOT `@Data` — `equals`/`hashCode` lazy-load triggerlaydi → N+1)
- Har `@ManyToOne` da explicit `(fetch = FetchType.LAZY)` (default EAGER trap)
- `@Version` optimistic locking (lost-update himoyasi)
- `String` field'da explicit `length` + `@Size` (DB constraint + Bean Validation mos)
- Soft delete: `@SQLRestriction("delete_ts IS NULL")`
- FK ustuniga indeks (PostgreSQL avtomatik yaratmaydi)

**TAQIQLANGAN:**
- `@Data` JPA entity'da
- `cascade = CascadeType.ALL` operational entity'larda
- `@OneToMany(fetch = FetchType.EAGER)` — har doim LAZY
- `Optional<T>` field type sifatida (faqat return type)
- `final` field with `@Column` (Hibernate proxy break)

### Classifier entity (hemishe_h_* ga map)

```java
@Entity
@Table(name = "hemishe_h_gender")  // legacy jadval — MANBA
@SQLRestriction("delete_ts IS NULL")  // CUBA soft-delete
@Getter
@Setter
public class Gender extends BaseEntity {
    // BaseEntity: id, version, createTs, createdBy, updateTs, updatedBy, deleteTs, deletedBy

    @Id
    @Column(name = "code", length = 20)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "active")  // DB tomonda "active", Java tomonda "isActive"
    private Boolean isActive;
}
```

### Operational entity (yangi schema'da)

```java
@Entity
@Table(name = "employee", schema = "hr")  // YANGI schema
@Getter
@Setter
public class Employee extends AuditableEntity {
    // AuditableEntity: id, version, createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy

    @Column(nullable = false, unique = true, length = 14)
    private String pinfl;

    @Column(nullable = false)
    private String firstName;

    // FK eski classifier'ga (cross-schema):
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_code")
    private Gender gender;  // Gender entity → public.hemishe_h_gender
}
```

### Legacy entity (hemishe_e_* ga map — faqat api-legacy uchun)

```java
@Entity
@Table(name = "hemishe_e_student")
@SQLRestriction("delete_ts IS NULL")
public class Student extends BaseEntity {
    @Column(name = "_gender")  // CUBA underscore prefix
    private String genderCode;
}
```

---

## Migration qoidalari

### Path va naming

```
domain/src/main/resources/db/changelog/changesets/
├── schema/      V001..V014+   DDL (CREATE TABLE, INDEX, CONSTRAINT)
├── seed/        S001..S009+   DML (INSERT reference data)
└── migration/   M001..M005+   Data migratsiya (legacy sistemadan)
```

**Naming:**
- `V021_create_schemas.sql` — schema'lar
- `V022_move_tables_to_auth.sql` — jadvallar ko'chirish
- `S010_seed_X.sql` — seed
- `M006_migrate_X.sql` — data

**Har migration uchun rollback fayl majburiy:** `VXXX_...rollback.sql`

### Idempotent patterns

```sql
CREATE TABLE IF NOT EXISTS ...
INSERT ... ON CONFLICT (code) DO NOTHING
ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...
```

### Changeset qoidalari

1. Har migration alohida changeset
2. `preConditions: not tableExists` — idempotent
3. `runOnChange: true` faqat seed uchun
4. Mavjud changeset **o'zgartirilmaydi** — yangi V### yaratiladi
5. **Istisno:** Test DB'da production'ga chiqmagan migration'lar in-place edit qilinishi mumkin (checksum re-calc)

---

## Module Guidelines

### Spring Modulith — modul chegaralarini saqlash

Loyihada Spring Modulith 1.4.0 ishlatiladi (`app/build.gradle.kts`). Modul chegaralarini compile-time tekshiradi.

```java
// package-info.java — har modulning ildizida
@ApplicationModule(
    displayName = "Service Layer",
    allowedDependencies = {"common", "domain", "security"}
)
package uz.hemis.service;

import org.springframework.modulith.ApplicationModule;
```

**Verification:**
```bash
./gradlew :app:test --tests "ModularityTests"
```

**TAQIQ:**
- Modul ichidan `allowedDependencies` da yo'q boshqa modulga import — compile error
- Cyclic dependency — compile error
- Direct field access cross-module — only events yoki public APIs

**Test:** `app/src/test/java/uz/hemis/app/modulith/ModularityTests.java` — har CI run'da bajariladi.

### `common` — DTOs, exceptions, utilities
- Use Lombok (`@Value`, `@Builder`, `@Data`) and Jackson (`@JsonProperty`) for legacy field names
- NO Spring dependencies, NO business logic, NO entity classes

### `domain` — Entities, repositories, migrations
- Entity'lar schema bo'yicha tashkil qilinadi: `entity/auth/`, `entity/hr/`, `entity/univ/`, `entity/legacy/`
- Repositories `extends JpaRepository<Entity, ID>`
- Soft-delete instead of hard-delete

### `security` — Authentication & authorisation
- `@PreAuthorize` on service methods for permissions
- Cache user authorities in Redis. **BCrypt strength factor 12 minimum** (OWASP 2025); consider Argon2id for new services
- Never store plain text passwords; never log JWT tokens, PINFL, passwords, or other PII
- Security config stays in this module — don't duplicate in controllers
- Rate limiting per role: VIEWER 60 req/min, UNIVERSITY_ADMIN 300, MINISTRY_ADMIN 600, SUPER_ADMIN 1000
- Audit log for every CRUD action (7 yil retention — Vazirlik talabi)

### `service` — Business logic
- `@Service` + `@Transactional`. Use `readOnly=true` for queries
- `@Valid` for input validation. MapStruct for entity ↔ DTO mapping
- Throw custom exceptions (`ResourceNotFoundException`, `ValidationException`) — don't return nulls
- NO business logic in controllers or repositories
- **api-legacy va api-web bir xil service'lardan foydalanadi** — hech qanday business logic dublikat yo'q

### `api-web`, `api-legacy`, `api-external` — Presentation layer
- `@RestController` + `@RequestMapping`. Return `ResponseWrapper<T>` with proper HTTP status codes
- Swagger annotations on EVERY endpoint: `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`
- `@Valid` on request bodies. Integration test for each endpoint (success + error scenarios)
- **api-legacy** faqat response formatini CUBA ga (`_entityName`, `_instanceName`) o'tkazadi — business logic emas

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
- DTO: prefer Java 25 `record` over class (immutability default)
- `switch`: prefer pattern matching (Java 25 feature)
- **Configuration:** prefer `@ConfigurationProperties` over `@Value` (type-safe + validation)

### `@ConfigurationProperties` over `@Value`

```java
// ✗ Eski — type-unsafe, validation yo'q, scattered
@Value("${hemis.jwt.secret}") private String jwtSecret;
@Value("${hemis.jwt.access-token-validity:43200}") private long accessTokenValidity;
@Value("${hemis.jwt.refresh-token-validity:604800}") private long refreshTokenValidity;

// ✓ Modern — type-safe, validated, grouped
@ConfigurationProperties(prefix = "hemis.jwt")
@Validated
public record JwtProperties(
    @NotBlank @Size(min = 32) String secret,
    @Positive long accessTokenValiditySeconds,
    @Positive long refreshTokenValiditySeconds,
    String jwkSetUri  // optional — production'da JWK Set URI uchun
) {}

// Foydalanish
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProperties jwt;
    // jwt.secret(), jwt.accessTokenValiditySeconds() — type-safe
}
```

**Migration plan:** har sprintda 5-10 ta `@Value` ni `@ConfigurationProperties` ga ko'chirish. Yangi config — har doim `@ConfigurationProperties`.


---

## Cache Strategy (v3.0)

### 2-Level Cache Architecture

```
Application
   ├─ L1: Caffeine (per-instance JVM, fast, ~100ns)
   └─ L2: Redis (shared across instances, ~1ms)
         └─ Database (~10ms)
```

Manager: `DashboardCacheConfig.TwoLevelCacheManager`

### Cache Invariant — MAJBURIY

**Har `@Cacheable` uchun:**
1. Cache name `DashboardCacheConfig.TwoLevelCacheManager` da explicit TTL bilan ro'yxatda bo'lishi
2. Mutation method'da `@CacheEvict` corresponding pair
3. Method `public` (private = AOP fail)
4. Method **boshqa bean** dan chaqirilishi (same-class call = AOP fail)
5. `unless = "#result == null"` agar nullable return

```java
// ✓ TO'G'RI
@Service
public class FacultyService {
    @Cacheable(value = "faculties", key = "#id", unless = "#result == null")
    public FacultyDto findById(Long id) { ... }

    @CacheEvict(value = "faculties", key = "#dto.id")
    public FacultyDto update(FacultyUpdateDto dto) { ... }

    @Caching(evict = {
        @CacheEvict(value = "faculties", key = "#id"),
        @CacheEvict(value = "faculties:list", allEntries = true)
    })
    public void delete(Long id) { ... }
}
```

### TTL Tavsiyasi

| Data turi | TTL | Misol |
|-----------|-----|-------|
| Classifier (kam o'zgaradi) | 24h | `hokimiyatClassifiers`, `classifierEducationType` |
| Hot entity (tez o'qiladi, kam o'zgaradi) | 5-15m | `students`, `faculties` |
| User permissions | 1h | `permissions` |
| Sessions | 12h | JWT validity |
| List/search results | 1-5m | `students:search` |
| Mutation-heavy | Don't cache | — |

### AOP Self-Invocation Trap

```java
// ✗ XATO — same-class call, proxy bypass, cache silently fails
@Service
public class StudentService {
    public Student get(Long id) { return load(id); }

    @Cacheable("students")
    private Student load(Long id) { ... }  // private + same-class = double fail
}

// ✓ TO'G'RI — alohida bean
@Service @RequiredArgsConstructor
public class StudentService {
    private final StudentLoader loader;
    public Student get(Long id) { return loader.loadById(id); }
}

@Service
public class StudentLoader {
    @Cacheable(value = "students", key = "#id")
    public Student loadById(Long id) { ... }
}
```

**Real misol:** `ClassifierReferenceLoader` — `StudentLegacyMapper` ichidan extract qilingan, AOP self-invocation muammosini hal qildi.

---

## Reliability

**Hozir:** Resilience4j yo'q. `AbstractGovernmentApiService` RestTemplate'ni ishlatadi.

**Majburiy qoidalar (yangi external integratsiya):**
- **Timeout:** `SimpleClientHttpRequestFactory` bilan `setConnectTimeout(10_000)` + `setReadTimeout(30_000)`. Misol: [`api-external/CLAUDE.md`](../api-external/CLAUDE.md) "Outbound RestClient".
- **Graceful Shutdown:** `server.shutdown: graceful` + `spring.lifecycle.timeout-per-shutdown-phase: 30s`. SIGTERM in-flight requestlarni 30s da yakunlaydi.
- **Resilience4j:** yangi external bo'lsa qo'shish kerak — qoida bugun ishlatilmaydigan kutubxona uchun yozilmaydi.

---

## Modernization (Spring 4 / Spring 6 / Java 25)

Yangi kod yozilganda **modern API afzal**:

- **`@MockBean` → `@MockitoBean`** (Spring Boot 4.x)
- **`RestTemplate` → `RestClient`** (Spring 6.1+, fluent + type-safe)
- **`JdbcTemplate` → `JdbcClient`** (Spring 6.1+)
- **`@Value` → `@ConfigurationProperties`** (63 ta `@Value` migration kandidat)
- **HikariCP `leak-detection-threshold: 60000`** master pool'da MAJBURIY
- **PostgreSQL `statement_timeout`** har profilda (`30s` dev, `60s` prod)

**Java 25 majburiy:** Records (DTO), Pattern Matching for switch, Sealed Classes (closed hierarchies). Virtual threads — audit keyin (synchronized + ThreadLocal tekshir).

> **Batafsil misollar:** [`.claude/MODERNIZATION.md`](MODERNIZATION.md)

---

## Architecture Decision Records (ADR)

Har "muhim arxitektura qaror" uchun ADR majburiy. **Triggerlar:** yangi jadval, external integratsiya, cache pattern, schema separation, library tanlash, async/sync.

**Canonical workflow:** [`.claude/skills/adr-create/SKILL.md`](skills/adr-create/SKILL.md)
**Template:** [`docs/adr/template.md`](../docs/adr/template.md) (AgDR 2026 YAML frontmatter)
**Index:** [`docs/adr/README.md`](../docs/adr/README.md)

**Tavsiya:** Qaror jarayonida (keyinroq emas) yozish — PR description'da ADR link.

---

## PR Checklist (yangi jadval yaratishda)

- [ ] `hemishe_h_X` yoki `hemishe_e_X` mavjud emas (grep tekshirildi)
- [ ] Bu haqiqatan yangi business concept (sabab PR'da yozilgan)
- [ ] Mos schema tanlandi (`auth`, `hr`, `univ`, `ui`, `i18n`, `edu`, `analytics`)
- [ ] Jadval nomi singular, lowercase, prefixsiz
- [ ] Primary Key UUID default `gen_random_uuid()`
- [ ] Audit ustunlar mos pattern bilan (`AuditableEntity`/`ReferenceEntity`/`ImmutableEntity`)
- [ ] Classifier FK'lari eski `public.hemishe_h_*` ga yo'naltirilgan
- [ ] Rollback migration fayli yozilgan
- [ ] Entity package mos schema'ga (`entity/hr/`, `entity/univ/`, ...)
- [ ] Repository va MapStruct mapper yozilgan
- [ ] Service + Swagger + integration test yozilgan

---

## Kelajak reja (V3.0+, old-hemis voz kechilgach)

Bu qoidalar **vaqtinchalik** — 2027 yoki undan keyin:

1. Univer Yii2 PHP yo'qoladi
2. Eski `hemishe_*` jadvallar yangi schema'ga ko'chadi
3. `api-legacy` moduli olib tashlanadi
4. Classifier'lar `ref` schema'ga ko'chadi
5. Modern naming (`is_active`, `created_at`) butun sistemada

Bu — **Strangler Fig Pattern'ning to'liq yakuni**. Hozir faqat eski tizim bilan parallel ishlash.

---

## Cross-Cutting Database Rules (2026-05-07 — kengaytirma)

Quyidagi qoidalar `V001-V014` audit (2026-05-07) natijasida aniqlangan. Avval implicit edi, hozir explicit:

### 1. Naming Exceptions — PostgreSQL Reserved Words

| Jadval | Forma | Sabab |
|--------|-------|-------|
| `users` | PLURAL | `user` PostgreSQL reserved word — `SELECT * FROM user` xato |
| `orders` (kelajakda) | PLURAL | `order` reserved (ORDER BY) |
| `groups` (kelajakda) | PLURAL | `group` reserved (GROUP BY) |
| Qolgan barcha jadvallar | SINGULAR | `employee`, `role`, `permission`, `system_message` |

**Qoida:** Yangi jadval nomi PostgreSQL reserved word'ga to'g'ri kelsa — PLURAL. Aks holda — SINGULAR. Tasdiqlash: `psql -c "SELECT word FROM pg_get_keywords() WHERE word = 'X' AND catcode = 'R'"` reserved bo'lsa NULLda emas qaytaradi.

### 2. Soft-Delete UNIQUE — har doim PARTIAL

Soft-delete (`deleted_at`) bilan jadvalda har UNIQUE constraint **majburiy `WHERE deleted_at IS NULL`** partial index'i bilan:

```sql
-- ❌ NOTO'G'RI (oddiy UNIQUE):
email VARCHAR(255) NOT NULL UNIQUE,

-- ✅ TO'G'RI (Partial UNIQUE):
email VARCHAR(255) NOT NULL,
...
CREATE UNIQUE INDEX uq_users_email ON users(email) WHERE deleted_at IS NULL;
```

**Sabab:** Soft-deleted yozuvni qayta yaratish kerak bo'lsa — oddiy UNIQUE bloklaydi. Partial UNIQUE soft-deleted'ni e'tiborsiz qoldiradi.

**Hozirgi xato:** `oauth_client.client_id` (V006:154) — oddiy UNIQUE. Kelajak migration (M005) Partial UNIQUE'ga ko'chiradi.

### 3. FK Index Mandate — har FK ga partial index

PostgreSQL **avtomatik FK index qo'ymaydi**. Har FK uchun majburiy:

```sql
-- FK declaration:
gender_code VARCHAR(20) REFERENCES public.hemishe_h_gender(code),
...

-- Index (majburiy):
CREATE INDEX idx_employee_gender_code ON employee(gender_code) WHERE deleted_at IS NULL;
```

Index'siz: `DELETE FROM hemishe_h_gender` o'nlab daqiqa, JOIN'da sequential scan. Audit: `M004_classifier_fk_indexes` changeset rejada (V004 classifier FK'lari uchun).

### 4. Module ↔ Entity Ownership — Pre-commit Reject

`api-legacy/**/*.java` ichida YANGI schema entity import topilsa — pre-commit hook reject qiladi:

```
api-legacy/**/import.*entity\.(security\.User|employee\.Employee|employee\.EmployeeJobs)\b → REJECT
```

Manba: ADR-0008. Implementation: `.claude/hooks/post-edit.sh` (pending — Stage 5).

### 5. ADR Status Drift Detection — har sprint

ADR `Accepted` qarorni anglatadi, **implementatsiyani EMAS**. Har ADR'da `## Implementation` bo'limi bo'lishi shart:

```markdown
## Implementation
- ✅ Stage 1 — Audit
- ⏳ Stage 2 — Code refactor
- ❌ Stage 3 — Blocked (sabab: …)
```

Sprint check: `grep -L "## Implementation" docs/adr/*.md` → bo'sh bo'lishi shart.

### 6. Bootstrap Source of Truth

Har jadval uchun **manba** dokumentlangan:
- **Bizning Liquibase changeset** (V001-V014, M001-M003, S001-S010) — bizning ownership
- **Legacy dump (old-hemis)** — `hemishe_*`, `sec_*` — manbai dump-NNNN.sql + commit-hash (`README.md` "DB Bootstrap" bo'limida)

Yangi jadval qo'shilganda: `domain/CLAUDE.md` "Real holat" jadvaliga qator qo'shish (ADR-0006/0008 patterni).

### 7. ADR-0006 Mezoni — `h_*` faqat refdata

`h_` prefiks majburiy mezonlari (UCHCHALA bajarilishi shart):
1. FK target (boshqa entity'lar tomonidan ko'rsatiladi)
2. Univer ekosistemi sync mantiqiy (`hemishe_h_*` ga teng)
3. Refdata semantikasi (code-based PK, stable enumeration)

Birortasi bajarilmasa — prefiks YO'Q. RBAC (`role`, `permission`), Auth (`users`, `oauth_client`), Operational log (`*_lifecycle`) — prefiks-siz.

Tafsilot: `docs/adr/0006-classifier-h-prefix.md`.
