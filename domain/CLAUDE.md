# domain module — Entity, Repository, Liquibase

> **Eng kritik modul.** Bu **markaziy DB** (vazirlik) — 230 OTM dan ~1.15M talaba metadata aggregation. Bu yerdagi xato butun tizimga (224 Univer client + davlat integratsiya + vazirlik admin) ta'sir qiladi.
>
> JPA entity, Spring Data repository, Liquibase migration — hammasi shu yerda.
>
> **Avval bu hujjatni o'qing, keyin kod yozing.**

---

## TOP 10 Eng Tez-tez Xato Qilinadigan Narsa (impact bo'yicha)

### 1. Lombok `@Data` JPA entity'da — TAQIQLANGAN

```java
// ✗ XATO — equals/hashCode lazy-load triggerlaydi → silent N+1
@Data
@Entity
public class Student extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Faculty faculty;
}

// ✓ TO'G'RI
@Getter
@Setter
@Entity
public class Student extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Faculty faculty;
    // equals/hashCode AuditableEntity'da id orqali
}
```

**Sabab:** `@Data` barcha field'lar bo'yicha `equals`/`hashCode` generatsiya qiladi → lazy relation'ni triggerlaydi → 1000 entity = 1001 query.

**Default:** `@Getter @Setter` + `@ToString(onlyExplicitlyIncluded = true)`.

---

### 2. `findAll()` keyin relation access — KLASSIK N+1

```java
// ✗ XATO — 1 + N query
List<Student> students = studentRepository.findAll();
for (Student s : students) {
    System.out.println(s.getFaculty().getName());  // har biri yangi query
}

// ✓ TO'G'RI — JOIN FETCH
@Query("""
    SELECT s FROM Student s
      LEFT JOIN FETCH s.faculty
      LEFT JOIN FETCH s.speciality
    WHERE s.deletedAt IS NULL
""")
List<Student> findAllWithRelations();

// ✓ TO'G'RI — @EntityGraph (re-usable)
@EntityGraph(attributePaths = {"faculty", "speciality", "course"})
List<Student> findByActiveTrue();
```

**1.15M talaba × N+1 = production halokati.** Har list method'da JOIN FETCH yoki EntityGraph majburiy.

---

### 3. `@Transactional` private method'da — ISHLAMAYDI

Spring AOP proxy private va same-class call'larni intercept qilmaydi.

```java
// ✗ XATO
@Service
public class StudentService {
    public void process() {
        save(student);  // proxy'dan o'tmaydi → tx yo'q
    }

    @Transactional
    private void save(Student s) { ... }
}

// ✓ TO'G'RI — public + alohida bean
@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentSaver saver;

    public void process() {
        saver.save(student);  // proxy'dan o'tadi
    }
}

@Service
public class StudentSaver {
    @Transactional
    public void save(Student s) { ... }
}
```

---

### 4. FK ustuniga indeks qo'ymaslik — SLOW JOIN

PostgreSQL FK uchun avtomatik indeks yaratmaydi. **Har FK ustuniga manual majburiy.**

```sql
-- Migration ichida
CREATE INDEX IF NOT EXISTS idx_student_faculty_id
    ON hemishe_e_student(faculty_id);

-- Soft-delete partial index (kichik + tez)
CREATE INDEX IF NOT EXISTS idx_student_active
    ON hemishe_e_student(faculty_id)
    WHERE delete_ts IS NULL;
```

**1.15M talaba × FK indeksiz JOIN = sequential scan = 5+ sekund.**

---

### 5. Cascade `ALL` + `orphanRemoval = true` — MA'LUMOT YO'QOTISH

```java
// ✗ XAVFLI — Faculty o'chirilsa, butun talabalar yo'qoladi
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
private List<Student> students;

// ✓ TO'G'RI — cascade yo'q, manual orchestration + soft delete
@OneToMany(mappedBy = "faculty")
private List<Student> students;
```

Cascade DELETE `hemishe_*` jadvallarida har doim taqiqlangan — soft delete ishlatish.

---

### 6. `@ManyToOne` default `EAGER` — BUG MAGNITI

```java
// ✗ XATO — har Student fetch da Faculty avtomatik keladi (EAGER default)
@ManyToOne
private Faculty faculty;

// ✓ TO'G'RI — har doim explicit LAZY
@ManyToOne(fetch = FetchType.LAZY)
private Faculty faculty;
```

**Qoida:** Hech qachon EAGER ishlatmang. Kerak bo'lganda JOIN FETCH yoki EntityGraph.

---

### 7. `@Version` qo'ymaslik — concurrent update silent loss

```java
// ✗ XATO — concurrent updates'da oxirgisi g'olib (lost update)
@Entity
public class Student { ... }

// ✓ TO'G'RI — optimistic locking
@Entity
public class Student extends AuditableEntity {
    @Version
    private Integer version;  // BaseEntity'da bor
}
```

Concurrent edit → `OptimisticLockException` → user'ga "data was modified, refresh" message.

---

### 8. `String` field'da uzunlik chegarasi yo'q

```java
// ✗ XATO — DB'da TEXT yoki VARCHAR(255) default, control yo'q
@Column
private String description;

// ✓ TO'G'RI
@Column(length = 1000, nullable = false)
@Size(max = 1000)
private String description;
```

**Implication:** validation Bean Validation tomonida + DB constraint. Ikkalasi mos kelishi shart.

---

### 9. PINFL/passport'da unique constraint yo'q

```java
// ✓ MAJBURIY — duplikat talaba bo'lmasligi uchun
@Column(name = "pinfl", unique = true, nullable = false, length = 14)
@Pattern(regexp = "\\d{14}", message = "PINFL must be 14 digits")
private String pinfl;
```

---

### 10. `Optional<T>` ni service ga ko'tarish

```java
// ✗ XATO — service murakkablik chiqaradi
public Optional<Student> findById(Long id) { ... }

// ✓ TO'G'RI — repository → Optional, service → ResourceNotFoundException
public Student findById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
}
```

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
- **`CREATE INDEX CONCURRENTLY`** + `runInTransaction: false`
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
| **Modern** | `AuditableEntity`, `ImmutableEntity` | `deleted_at`, `deleted_by` | `created_at`, `created_by`, `updated_at`, `updated_by` | Yangi jadvallar (prefiks-siz, `h_*` yangi classifierlar, modular monolith) |

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
@Table(name = "university_legal")
@SQLRestriction("deleted_at IS NULL")
public class UniversityLegal extends AuditableEntity { ... }
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

## PostgreSQL-Specific (Senior tips)

### JSONB — variable shape data

```sql
-- Misol: talaba qo'shimcha attribut'lar (markaziy DB, public schema)
ALTER TABLE student_profile ADD COLUMN extra JSONB;

-- GIN index for JSONB
CREATE INDEX idx_extra_gin ON student_profile USING GIN (extra);

-- Query
SELECT * FROM student_profile WHERE extra @> '{"hobby": "chess"}';
```

### Partitioning (>100M qator)

```sql
-- Hisobot jadvali — yil bo'yicha partition (markaziy DB, public schema)
CREATE TABLE report_data (
    id UUID,
    created_at TIMESTAMP,
    ...
) PARTITION BY RANGE (created_at);

CREATE TABLE report_data_2026 PARTITION OF report_data
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
```

### VACUUM/ANALYZE

PostgreSQL avtomatik (autovacuum), lekin yirik DELETE keyin manual:
```sql
VACUUM ANALYZE hemishe_e_student;
```

### Connection pool sizing

> Pool sizing aniq tafsiloti: `../.claude/architecture.md` (Master 10 + Replica 20 per-instance).

**Formula:** `connections = (cores × 2) + effective_spindles`
**Diqqat:** Production cluster (3 instance) → 30 master + 60 replica DB-side capacity zarur.

---

## Repository Patterns

### `@Query` vs Method Naming

```java
// ✓ Oddiy queries — method naming
List<Student> findByFacultyIdAndDeletedAtIsNull(Long facultyId);

// ✓ Murakkab — @Query + JPQL
@Query("""
    SELECT s FROM Student s
      LEFT JOIN FETCH s.faculty f
    WHERE f.id = :facultyId
      AND s.deletedAt IS NULL
    ORDER BY s.lastName, s.firstName
""")
Page<Student> findByFacultyWithDetails(@Param("facultyId") Long facultyId, Pageable pageable);

// ⚠ Native query — faqat JPQL imkonsiz bo'lsa (window function, recursive CTE)
@Query(nativeQuery = true, value = """
    SELECT * FROM hemishe_e_student
    WHERE delete_ts IS NULL
      AND faculty_id = :facultyId
""")
List<Student> findByFacultyNative(@Param("facultyId") Long facultyId);
```

### Specification (dynamic filter)

```java
public interface StudentSpecs {
    static Specification<Student> hasFaculty(Long facultyId) {
        return (root, query, cb) -> facultyId == null
            ? cb.conjunction()
            : cb.equal(root.get("faculty").get("id"), facultyId);
    }

    static Specification<Student> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }
}

// Foydalanish
Page<Student> result = repository.findAll(
    where(hasFaculty(facultyId)).and(notDeleted()),
    pageable
);
```

---

## Naming Convention (Single Schema)

**Qaror:** Schema separation BEKOR — barcha jadvallar `public` schema ichida.
Sabab: cross-schema FK overhead, search_path murakkabliklari, hemis_337 (224 OTM ekosistemi) bilan moslashish.

| Prefiks | Maqsad | Misol |
|---------|--------|-------|
| `hemishe_e_*` | Eski CUBA entity (FROZEN) | `hemishe_e_student`, `hemishe_e_university` |
| `hemishe_h_*` | Eski CUBA klassifikator (FROZEN) | `hemishe_h_gender`, `hemishe_h_soato` |
| `h_*` (yangi) | Yangi klassifikatorlar (FK target) | `h_building_category`, `h_position` |
| (prefiksiz) | Yangi entity (biznes ob'ekt) | `users`, `employee`, `organization`, `university_building` |
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
