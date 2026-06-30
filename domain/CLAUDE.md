# domain module — Entity, Repository, Liquibase

> **Eng kritik modul.** Bu **markaziy DB** (vazirlik) — 230 OTM dan ~1.15M talaba metadata aggregation. Bu yerdagi xato butun tizimga (224 Univer client + davlat integratsiya + vazirlik admin) ta'sir qiladi.
>
> JPA entity, Spring Data repository, Liquibase migration — hammasi shu yerda.
>
> **Avval bu hujjatni o'qing, keyin kod yozing.**

---

## TOP 10 Eng Tez-tez Xato Qilinadigan Narsa (impact bo'yicha)

> **Batafsil misollar va kod bloklari:** [`domain/PATTERNS.md`](PATTERNS.md)

| # | Xato | To'g'ri | Sabab |
|---|------|---------|-------|
| 1 | Lombok `@Data` `@Entity`'da | `@Getter @Setter` + `equals`/`hashCode` BaseEntity'da | `@Data` lazy relation'ni triggerlaydi → silent N+1 |
| 2 | `findAll()` + relation access | `@Query JOIN FETCH` yoki `@EntityGraph` | 1+N query → ~1.15M talaba scale'da halokat |
| 3 | `@Transactional` private method | `public` + alohida `@Service` bean | Spring AOP proxy private intercept qilmaydi |
| 4 | FK ustun indekssiz | `CREATE INDEX ... WHERE delete_ts IS NULL` | PostgreSQL FK auto-index qo'ymaydi → sequential scan |
| 5 | `cascade = CascadeType.ALL` + `orphanRemoval` | `mappedBy` + manual orchestration + soft-delete | Faculty o'chirilsa talabalar yo'qoladi |
| 6 | `@ManyToOne` default EAGER | `@ManyToOne(fetch = FetchType.LAZY)` har doim | Default EAGER avtomatik fetch (silent perf bug) |
| 7 | `@Version` yo'q | `@Version private Integer version` (BaseEntity) | Concurrent update — silent lost update |
| 8 | `@Column` length yo'q | `@Column(length=1000) + @Size(max=1000)` | DB TEXT default + Bean Validation mos kelishi shart |
| 9 | PINFL unique constraint yo'q | `unique=true, length=14, @Pattern("\\d{14}")` | Duplikat talaba real bug |
| 10 | `Optional<T>` service'da | `orElseThrow(ResourceNotFoundException::new)` | Service interface murakkablik chiqaradi |

**Eng kritik (1, 2, 3, 6):** ~1.15M talaba metadata aggregation scale'da production'da halokat.

---

## JPA/Hibernate Senior Patterns

### Fetch Strategy

```
Default qoidalar:
├── @ManyToOne(fetch = FetchType.LAZY)  — har doim
├── @OneToMany — default LAZY (yaxshi), lekin explicit yozish
├── @OneToOne — TRAP: default EAGER, manual LAZY qilish
└── @ManyToMany — default LAZY (yaxshi), lekin foydalanmaslik (junction table afzal)
```

**Eager fetch faqat:** entity hayoti davomida har doim relation kerak bo'lsa (kamdan-kam).

### Batch Fetching (N+1 hal qilish strategiyasi)

```yaml
# application.yml — bizda mavjud
spring.jpa.properties.hibernate:
  default_batch_fetch_size: 20   # lazy SELECT IN-clause batch
  jdbc.batch_size: 20             # INSERT/UPDATE batch
order_inserts: true
order_updates: true
```

JOIN FETCH yozish unutilsa — Hibernate avtomatik 20 ta IN clause bilan birlashtiradi
(N+1 SELECT loop'larni ceil(N/20) IN-clause query'ga aylantiradi).

### Composite Key

```java
// ✓ Embedded composite key
@Embeddable
public record StudentCourseId(Long studentId, Long courseId) implements Serializable {}

@Entity
public class StudentCourse {
    @EmbeddedId
    private StudentCourseId id;
}
```

---

## Liquibase Migration

**Canonical workflow:** [`.claude/LIQUIBASE_GUIDE.md`](../.claude/LIQUIBASE_GUIDE.md)

Eng muhim qoidalar (qisqacha):
- **Naming:** `V###/M###/S###` (schema/migration/seed) + `_rollback.sql` MAJBURIY
- **Idempotent:** `IF NOT EXISTS`, `ON CONFLICT DO NOTHING`
- **Long-running:** multi-step (NULLable add → backfill batched → NOT NULL constraint)
- **1M+ row legacy jadval (hemishe_e_*) index — CONCURRENTLY pattern MAJBURIY:** `runInTransaction: false` + `CREATE INDEX CONCURRENTLY IF NOT EXISTS` + `splitStatements: true` + Liquibase `preConditions` (DO $$ block ichida ishlamaydi). Misol: [`M002a-e`](src/main/resources/db/changelog/changesets/migration/). Tafsilot: [`LIQUIBASE_GUIDE.md`](../.claude/LIQUIBASE_GUIDE.md) "CONCURRENTLY pattern" bo'limi
- **`hemishe_*` schema FROZEN:** ALTER/DROP/RENAME TAQIQ. Yangi ustun → `ref_ext` extension table
- **Pre-commit hook** rollback fayl yo'qligini bloklaydi

---

## Indeks Strategiyasi

### Majburiy indekslar

1. **Har FK ustuniga** (PostgreSQL avtomatik yaratmaydi)
2. **Soft-delete partial:** `WHERE delete_ts IS NULL` — kichik index, tez
3. **Unique constraint:** implicit, lekin tekshirish
4. **WHERE + ORDER BY birga:** composite index

### Misol — talaba qidirish

```sql
-- Query: WHERE faculty_id = ? AND delete_ts IS NULL ORDER BY last_name
CREATE INDEX idx_student_search
    ON hemishe_e_student (faculty_id, last_name)
    WHERE delete_ts IS NULL;
-- Composite + partial = ikki kuch
```

### Indeks ishlatilganini tekshirish

```sql
-- Query rejasi
EXPLAIN (ANALYZE, BUFFERS) SELECT ... WHERE ...;
-- ✓ "Index Scan using idx_student_search"
-- ✗ "Seq Scan on hemishe_e_student" → indeks yo'q yoki ishlatilmayapti

-- Ishlatilmayotgan indekslar (resurs sarflaydi)
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND indexrelname NOT LIKE '%_pkey';

-- Eng katta indekslar
SELECT pg_size_pretty(pg_relation_size(indexrelid)) AS size, ...
FROM pg_stat_user_indexes ORDER BY pg_relation_size(indexrelid) DESC;
```

### Partial index — keng ishlatilsin

```sql
-- Faqat aktiv yozuvlar uchun (90% query soft-delete bo'lmaganlarni qidiradi)
CREATE INDEX idx_active_student ON hemishe_e_student (faculty_id)
    WHERE delete_ts IS NULL;
-- Disk 10-20% joy oladi, query 5-10x tez
```

---

## Transaction Isolation Strategy

| Holat | Isolation | Misol |
|-------|-----------|-------|
| Default read | `READ_COMMITTED` (default) | Talaba ro'yxati |
| Hisobot consistency | `REPEATABLE_READ` | Statistik hisobot generatsiya |
| Money/critical | `SERIALIZABLE` | Stipendiya hisoblash |
| Lock-free counter | Optimistic (`@Version`) | Concurrent edit |
| Critical row lock | Pessimistic (`SELECT FOR UPDATE`) | Pul o'tkazma |

```java
// Optimistic — default qoida
@Version
private Integer version;

// Pessimistic — kerak bo'lganda
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Student s WHERE s.id = :id")
Student findByIdForUpdate(@Param("id") Long id);
```

---

## Soft Delete — `@SQLRestriction`

```java
@Entity
@Table(name = "hemishe_e_student")
@SQLRestriction("delete_ts IS NULL")  // Hibernate har query'ga qo'shadi
public class Student extends BaseEntity { ... }
```

**Diqqat:** `@SQLRestriction` faqat JPA query'larida ishlaydi. Native query yozilsa, manual `WHERE delete_ts IS NULL` qo'shilmasa, soft-deleted ham ko'rinadi.

```java
// ✗ XATO — soft-deleted ham qaytadi
@Query(nativeQuery = true, value = "SELECT * FROM hemishe_e_student WHERE faculty_id = :id")

// ✓ TO'G'RI
@Query(nativeQuery = true,
       value = "SELECT * FROM hemishe_e_student WHERE faculty_id = :id AND delete_ts IS NULL")
```

### Audit Column Naming Convention — `delete_ts` vs `deleted_at`

Loyihada **ikki audit naming convention** parallel ishlatiladi. Yangi entity yaratayotganda
to'g'ri base class ishlatish — har bir convention'ning aniq qoidasi:

| Convention | Base class | Soft-delete | Audit columns | Qachon |
|------------|-----------|-------------|---------------|--------|
| **CUBA legacy** | `BaseEntity`, `LegacyClassifierEntity` | `delete_ts`, `deleted_by` | `create_ts`, `created_by`, `update_ts`, `updated_by` | Eski `hemishe_*` jadvallari (FROZEN schema) |
| **Modern** | `AuditableEntity` | `deleted_at`, `deleted_by` | `created_at`, `created_by`, `updated_at`, `updated_by` | Yangi jadvallar (prefiks-siz, `h_*` yangi classifierlar, modular monolith) |

> **Modern base variantlar** (`entity/base/`): `AuditableEntity` — to'liq soft-delete (`deleted_at` + `updated_at`); `AuditableEntityNoSoftDelete` — soft-delete YO'Q, faqat `version` + `updated_at` (API sync snapshot uchun); `ImmutableEntity` — append-only, faqat `created_at`/`created_by` (update/delete yo'q); `ReferenceEntity` — modern classifier (`code` PK, soft-delete o'rniga `is_active` flag).

**Qoida:**
- Eski `hemishe_*` jadvalga **map qilinayotgan** entity → `BaseEntity` extend (column'lar CUBA tomonidan boshqariladi, FROZEN)
- Yangi yaratayotgan jadval (Liquibase V### ichida) → `AuditableEntity` extend, `deleted_at`/`created_at` columnlar qo'l bilan ko'rsatilsin
- **Aralashtirish TAQIQ** — bitta jadvalda `delete_ts` va `deleted_at` birga bo'lmasin

**Misol:**
```java
// ✓ Eski CUBA jadval — BaseEntity (delete_ts/create_ts)
@Entity
@Table(name = "hemishe_e_student")
@SQLRestriction("delete_ts IS NULL")
public class Student extends BaseEntity { ... }

// ✓ Yangi modular jadval — AuditableEntity (deleted_at/created_at)
@Entity
@Table(name = "university_building")
@SQLRestriction("deleted_at IS NULL")
public class UniversityBuilding extends AuditableEntity { ... }
```

**Migration tomonida:**
```sql
-- ✓ Yangi jadval — modern naming
CREATE TABLE university_building (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- ...
    version    INTEGER   DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

-- ✗ TAQIQ — yangi jadvalda CUBA naming (delete_ts) — convention buzilgan
```

**Sabab — nega ikki convention?**
- `hemishe_*` jadvallari — markaziy HEMIS-back DB'da, **eski CUBA Platform 7.3 (Java + Groovy, Haulmont)** strukturasini saqlaydi. FROZEN, chunki 224 ta Univer Yii2 PHP client `hemishe_e_*` shape kutadi (api-legacy 175/175 contract).
- Yangi jadvallar — modern Spring Data JPA + `@CreatedDate`/`@LastModifiedDate` annotation default `created_at`/`updated_at` bilan ishlaydi.
- Hibrid yondashuv: backward compat (Univer client kontrakt) + modern semantic.

---

## PostgreSQL + Repository Patterns

> **Batafsil misollar:** [`domain/PATTERNS.md`](PATTERNS.md) (JSONB, Partitioning, Specification, Repository @Query)

| Pattern | Qisqa qoida |
|---------|-------------|
| **JSONB** | `ALTER TABLE student_profile ADD COLUMN extra JSONB` + `GIN` index. Query: `extra @> '{"key":"val"}'` |
| **Partitioning (>100M)** | `PARTITION BY RANGE (created_at)` + yillik partition (`report_data_2026`) |
| **VACUUM/ANALYZE** | Autovacuum default. Yirik DELETE'dan keyin manual: `VACUUM ANALYZE <table>` |
| **Pool sizing** | `(cores × 2) + spindles`. Cluster: 30 master + 60 replica DB capacity. Tafsilot: `../.claude/architecture.md` |
| **`@Query` vs naming** | Oddiy → method naming (`findByFacultyIdAndDeletedAtIsNull`). Murakkab → `@Query` JPQL `LEFT JOIN FETCH`. Native faqat window/recursive CTE |
| **Specification** | Dynamic filter: `where(hasFaculty(...)).and(notDeleted())`. Conjunction `null` filterda |

---

## Naming Convention (Single Schema)

**Qaror:** Schema separation BEKOR — barcha jadvallar `public` schema ichida.
Sabab: cross-schema FK overhead, search_path murakkabliklari, hemis_337 (224 OTM ekosistemi) bilan moslashish.

| Prefiks | Maqsad | Misol |
|---------|--------|-------|
| `hemishe_e_*` | Eski CUBA entity (FROZEN) | `hemishe_e_student`, `hemishe_e_university` |
| `hemishe_h_*` | Eski CUBA klassifikator (FROZEN) | `hemishe_h_gender`, `hemishe_h_soato` |
| `h_*` (yangi) | Yangi klassifikatorlar (FK target) | `h_building_category`, `h_position` |
| (prefiksiz) | Yangi entity (biznes ob'ekt) | `users`, `employee`, `organization`, `university_building`, `webhook_target` (+ `webhook_delivery_log`, `webhook_apply_result` — K2, ADR-0012), `outbox_event` (ADR-0007) |
| `sec_user` | Old CUBA auth (FROZEN, parallel) | sec_user_role, sec_role_permission |

**Klassifikator (`h_*`) mezoni:** boshqa jadvallar tomonidan FK reference target sifatida ishlatilsa
VA universitet ekosistemi (224 OTM) bilan sync mantiqiy bo'lsa.

**FK qoidalari:**
- Eski klassifikatorga: `gender_code REFERENCES hemishe_h_gender(code)`
- Yangi klassifikatorga: `category_code REFERENCES h_building_category(code)`
- Entity'lar orasida: `employee_id REFERENCES employee(id)`

---

## ⚠️ CRITICAL — Bizning baza vs Univer baza

**To'liq tushuntirish:** [`.claude/UNIVER_INTEGRATION.md`](../.claude/UNIVER_INTEGRATION.md)

Eng muhim:
- **Markaziy DB** (`.env` `DB_MASTER_NAME` — lokal `test1_hemis`): bizning HEMIS-back jadvallari, masalan `hemishe_e_student`, `hemishe_e_teacher`, `hemishe_e_university`
- **Univer DB** (`hemis_337`, `hemis_401`, ..., 224 ta): per-OTM Yii2 PHP — bizda EMAS. Masalan `hemishe_e_grade`, `hemishe_e_attendance`, `hemishe_e_curriculum`, `hemishe_e_schedule`, `hemishe_e_exam`, `hemishe_e_enrollment`, `hemishe_e_course`, `hemishe_e_contract`, `hemishe_e_employment`

**Yangi `@Table(name="hemishe_e_*")` qo'shganda:**
1. **`./scripts/check_table_mappings.sh`** majburiy (pre-commit hook avtomatik chaqiradi)
2. Mismatch → entity Univer'da, bizda EMAS → JPA entity yaratmang
3. Univer'dagi ma'lumot kerak bo'lsa → `service/integration/UniverApiService` (REST + `@Cacheable` per-OTM key)

**Audit checklist (yangi entity PR):**
- [ ] Jadval `.env`'dagi DB'da mavjudmi? (`check_table_mappings.sh` ✅)
- [ ] Mapping nomi to'g'ri (typo yo'q)?
- [ ] Univer'da bo'lsa — REST integratsiya yarating, JPA emas
- [ ] `@SQLRestriction("delete_ts IS NULL")` qo'shilgan (CUBA legacy)
- [ ] Repository test real DB'da o'tadi

---

## PR Checklist (entity yoki migration)

- [ ] `@Data` ishlatilmagan, `@Getter @Setter` mavjud
- [ ] Har `@ManyToOne` da `fetch = FetchType.LAZY`
- [ ] `@Version` AuditableEntity orqali
- [ ] Cascade `ALL` yo'q (yoki sabab PR'da)
- [ ] FK ustuniga indeks qo'shilgan
- [ ] `String` field'da `length` aniq
- [ ] Soft delete: `@SQLRestriction("delete_ts IS NULL")`
- [ ] Native query'larda `delete_ts IS NULL` manual
- [ ] Migration: `IF NOT EXISTS`, rollback fayl, idempotent
- [ ] Migration: `hemishe_*` ALTER yo'q (faqat DML)
- [ ] Repository: JOIN FETCH yoki @EntityGraph relation access uchun
- [ ] Test: `@DataJpaTest` + real shared dev DB (`DB_MASTER_*` env, `ddl-auto: none`) — bizda 5 yillik real CUBA data, Testcontainers'da har gal restore qilish maqbul emas

---

## Testing Strategy (loyiha kontekstiga moslashtirilgan)

> **MUHIM:** Bu loyiha Strangler Fig migration. Real CUBA dev DB (5 yillik data) ustida ishlaydi. Testcontainers asosiy strategy EMAS.

### Real DB testing pattern (asosiy)

| Modul | Test DB | Sabab |
|-------|---------|-------|
| `domain` | Real shared PostgreSQL (`DB_MASTER_*`) | 5 yillik CUBA data, schema = production-like |
| `service` | Real shared PostgreSQL | Cross-table integratsiya, real classifier'lar |
| `security` | Real shared PostgreSQL | RBAC + sec_user real data |
| `app` (integration) | Testcontainers PostgreSQL | Faqat full-context smoke test'lar uchun |

### Test isolation pattern

`@Transactional` test method'da → tx auto-rollback (har test'dan keyin clean):

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // Spring TestContext rollback qiladi har test oxirida
class StudentServiceTest {

    @Autowired StudentService service;

    @Test
    void create_shouldPersist() {
        StudentDto created = service.create(validDto());
        // Verification — committed dev data ko'rinmaydi (rollback)
        assertThat(created.id()).isNotNull();
    }
}
```

### `@Sql` seed agar kerak bo'lsa

```java
@Test
@Sql("/test-data/extra-students.sql")  // qo'shimcha test data
void shouldFindByCustomFilter() { ... }
```

### Test data tozaligi

- ❌ `DELETE FROM hemishe_e_student WHERE ...` test'da — boshqa testlarni buzadi
- ✅ `@Transactional` rollback isolation
- ✅ Yangi yozuvni unique pinfl bilan yaratish (`"99000000000001"` test prefix)
- ⚠ Cleanup hook `@AfterEach`, faqat manual create qilingan ID'larni o'chirish

---

## See Also
- `../.claude/architecture.md` — DB master/replica routing
- `../.claude/LIQUIBASE_GUIDE.md` — Migration workflow
- `../.claude/rules.md` — Entity qoidalari (general)
