# HEMIS Backend – Coding Standards (v2.0)

**Oxirgi yangilanish:** 2026-04-23
**Status:** Active

---

## Golden Rules

1. **Stability first:** Do not break existing behaviour or APIs. Backward compatibility with legacy systems is mandatory.
2. **No destructive schema changes:** All database alterations via Liquibase migrations. Never `ALTER`, `DROP`, `RENAME` structure on legacy `hemishe_*` tables (DML `INSERT`/`UPDATE` ruxsat etiladi).
3. **Single Source of Truth:** Har business concept uchun bitta jadval. Dublikat jadval yaratilmaydi.
4. **Service layer required:** Controllers delegate to services. Repositories never called directly from controllers.
5. **Security by default:** All endpoints require authentication and authorisation. Validate input; no raw SQL; no exposed internal exceptions.
6. **Documentation & tests mandatory:** Every endpoint → Swagger annotations + integration test. Every service method → unit test. Minimum coverage 70%.
7. **Idempotent migrations:** Safe to run multiple times; always include rollback. Test forward + rollback on staging first.
8. **No hardcoded secrets:** Use environment variables; never commit secrets.

---

## Database Schema Architecture (v2.0)

HEMIS **modular monolith + bounded context schema separation** asosida quriladi.

### Schema struktura

```
PostgreSQL: hemis_db
│
├── public schema (eski CUBA — TEGILMAYDI):
│   ├── hemishe_e_*   — eski CUBA operational jadvallar
│   ├── hemishe_h_*   — eski CUBA classifier (102 ta — yagona manba)
│   └── sec_user      — eski auth (M001 migratsiya keyin users ga)
│
├── auth schema (yangi):
│   ├── users, role, permission
│   ├── user_role, role_permission
│   └── password_history, password_reset_token
│
├── hr schema (yangi):
│   ├── employee, employee_job
│   └── position, position_type
│
├── univ schema (yangi):
│   ├── organization, university_legal, university_profile
│   ├── university_founder, university_lifecycle, university_cadastre
│   └── university_building, building_lifecycle, building_category,
│       construction_material, roof_type
│
├── ui schema (yangi):
│   └── menu, user_favorite
│
├── i18n schema (yangi):
│   ├── language, configuration
│   └── system_message, system_message_translation
│
├── ref_ext schema (kelgusi):
│   └── classifier extension jadvallar (eski jadvalga qo'shimcha ustun uchun)
│
└── analytics schema (kelgusi):
    └── report/denormalized jadvallar
```

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

- **Schema:** mos domain schema'da (`auth`, `hr`, `univ`, `ui`, `i18n`, `edu`, `analytics`)
- **Jadval nomi:** singular, lowercase, underscore separator (`employee_job`, `building_lifecycle`)
- **Prefix YO'Q:** yangi jadvalda prefix ishlatilmaydi (`e_`, `h_`, `r_` kabi)
- **PK:** `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
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

### `common` — DTOs, exceptions, utilities
- Use Lombok (`@Value`, `@Builder`, `@Data`) and Jackson (`@JsonProperty`) for legacy field names
- NO Spring dependencies, NO business logic, NO entity classes

### `domain` — Entities, repositories, migrations
- Entity'lar schema bo'yicha tashkil qilinadi: `entity/auth/`, `entity/hr/`, `entity/univ/`, `entity/legacy/`
- Repositories `extends JpaRepository<Entity, ID>`
- Soft-delete instead of hard-delete

### `security` — Authentication & authorisation
- `@PreAuthorize` on service methods for permissions
- Cache user authorities in Redis. BCrypt for passwords; never store plain text
- Security config stays in this module — don't duplicate in controllers

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
