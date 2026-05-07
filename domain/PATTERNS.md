# domain/PATTERNS.md — Kod Misollar va Patternlar

> **Manual reference** (on-demand `Read`) — `domain/CLAUDE.md` qoidalarini batafsil misollar bilan to'ldiradi.
> Qoidalar canonical: `domain/CLAUDE.md`. Bu fayl misollar arxivi.

---

## TOP 10 Eng Tez-tez Xato Qilinadigan Narsa — Batafsil Misollar

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
    public void process() { saver.save(student); }
}

@Service
public class StudentSaver {
    @Transactional
    public void save(Student s) { ... }
}
```

### 4. FK ustuniga indeks qo'ymaslik — SLOW JOIN

```sql
-- Migration ichida (Partial Index — soft-delete bilan kichik+tez)
CREATE INDEX IF NOT EXISTS idx_student_faculty_id
    ON hemishe_e_student(faculty_id);

CREATE INDEX IF NOT EXISTS idx_student_active
    ON hemishe_e_student(faculty_id)
    WHERE delete_ts IS NULL;
```

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

### 6. `@ManyToOne` default `EAGER` — BUG MAGNITI

```java
// ✗ XATO — default EAGER
@ManyToOne
private Faculty faculty;

// ✓ TO'G'RI — har doim explicit LAZY
@ManyToOne(fetch = FetchType.LAZY)
private Faculty faculty;
```

### 7. `@Version` qo'ymaslik — concurrent update silent loss

```java
@Entity
public class Student extends AuditableEntity {
    @Version
    private Integer version;  // BaseEntity'da bor
}
```

Concurrent edit → `OptimisticLockException` → user'ga "data was modified, refresh".

### 8. `String` field'da uzunlik chegarasi yo'q

```java
@Column(length = 1000, nullable = false)
@Size(max = 1000)
private String description;
```

### 9. PINFL/passport'da unique constraint yo'q

```java
@Column(name = "pinfl", unique = true, nullable = false, length = 14)
@Pattern(regexp = "\\d{14}", message = "PINFL must be 14 digits")
private String pinfl;
```

### 10. `Optional<T>` ni service ga ko'tarish

```java
// ✗ XATO
public Optional<Student> findById(UUID id) { ... }

// ✓ TO'G'RI
public Student findById(UUID id) {
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
└── @ManyToMany — default LAZY (yaxshi), lekin junction table afzal
```

### Batch Fetching

```java
// application.yml
spring.jpa.properties.hibernate.default_batch_fetch_size: 50

// Yoki entity'da
@BatchSize(size = 50)
@OneToMany(mappedBy = "faculty")
private List<Student> students;
```

### Composite Key

```java
@Embeddable
public class StudentEnrollmentId {
    private UUID studentId;
    private UUID curriculumId;
}

@Entity
@IdClass(StudentEnrollmentId.class)
public class StudentEnrollment { ... }
```

---

## Indeks Strategiyasi

### Majburiy indekslar
1. **Har FK ustuniga** (PostgreSQL avtomatik yaratmaydi)
2. **Search ustunga** (email, pinfl, code)
3. **Filter ustunga** (status, is_active)
4. **Sort ustunga** (created_at, updated_at)
5. **Composite** (university_id, created_at) — tez-tez birga ishlatilsa

### Partial index (soft-delete bilan)

```sql
CREATE INDEX idx_student_pinfl
    ON hemishe_e_student(pinfl)
    WHERE delete_ts IS NULL;
```

### EXPLAIN ANALYZE

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM hemishe_e_student
WHERE faculty_id = '...' AND delete_ts IS NULL
LIMIT 20;
```

---

## Transaction Isolation Strategy

```java
// READ_COMMITTED (default) — ko'pchilik holatlar
@Transactional(isolation = Isolation.READ_COMMITTED)

// REPEATABLE_READ — financial calc
@Transactional(isolation = Isolation.REPEATABLE_READ)

// SERIALIZABLE — strict consistency (kamdan-kam)
@Transactional(isolation = Isolation.SERIALIZABLE)
```

---

## Soft Delete Pattern

### `@SQLRestriction` (Hibernate 6+)

```java
@Entity
@SQLRestriction("delete_ts IS NULL")  // legacy hemishe_*
@SQLRestriction("deleted_at IS NULL") // yangi schema
public class Student extends AuditableEntity {
    // delete_ts / deleted_at AuditableEntity'da
}
```

### Native query'da manual

```java
@Query(value = """
    SELECT * FROM hemishe_e_student
    WHERE faculty_id = :facultyId
      AND delete_ts IS NULL
    """, nativeQuery = true)
List<Student> findByFacultyId(@Param("facultyId") UUID facultyId);
```

---

## PostgreSQL Senior Tips

### JSONB

```sql
ALTER TABLE student_profile ADD COLUMN extra JSONB;
CREATE INDEX idx_extra_gin ON student_profile USING GIN (extra);
SELECT * FROM student_profile WHERE extra @> '{"hobby": "chess"}';
```

### Partitioning

```sql
CREATE TABLE report_data (
    id UUID, created_at TIMESTAMP, ...
) PARTITION BY RANGE (created_at);

CREATE TABLE report_data_2026 PARTITION OF report_data
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
```

### VACUUM/ANALYZE

```sql
VACUUM ANALYZE hemishe_e_student;
```

---

## Repository Patterns

### `@Query` JPQL

```java
@Query("""
    SELECT new uz.hemis.dto.StudentSummaryDto(s.id, s.firstName, f.name)
    FROM Student s LEFT JOIN s.faculty f
    WHERE s.deletedAt IS NULL
""")
List<StudentSummaryDto> findAllSummary();
```

### Specification (dynamic filter)

```java
public static Specification<Student> hasFaculty(UUID facultyId) {
    return (root, query, cb) ->
        cb.equal(root.get("faculty").get("id"), facultyId);
}

// Service
List<Student> result = repository.findAll(
    Specification.where(hasFaculty(id)).and(isActive())
);
```

---

## Naming Convention

- Jadval: SINGULAR (`employee`, `role`, `student`)
- Istisno: PostgreSQL reserved words PLURAL (`users`, `orders`, `groups`)
- Klassifikator: `h_*` prefiks (ADR-0006 mezoni — refdata + FK target + sync)
- Junction: `<a>_<b>` (e.g. `user_role`, `role_permission`)
- Index: `idx_<table>_<col>`, UNIQUE: `uq_<table>_<col>`
- FK: `fk_<table>_<ref>` (Liquibase auto-generated OK)

---

## See also

- `domain/CLAUDE.md` — qisqa qoidalar (canonical)
- `.claude/rules.md` — cross-cutting database rules
- `.claude/LIQUIBASE_GUIDE.md` — migration workflow
- `.claude/UNIVER_INTEGRATION.md` — Univer DB alohida (REST integration)
