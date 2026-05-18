---
name: classifier-add
description: Yangi h_* klassifikator (reference table) qo'shish - markaziy aggregation. Trigger - "klassifikator qo'sh", "h_ jadval", "reference table", "lookup", "klassifikator distribution".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Add Classifier (h_* prefix)

> ADR-0006: Markaziy klassifikator → 230 OTM bir xil qiymatdan foydalanadi. CLAUDE.md "Klassifikatorlarni UMUMIY saqlash" maqsadi.

## Workflow (5 layer)

### 1. Schema — V### migration

`liquibase-changeset` skill ishlating. Standart shape:

```sql
CREATE TABLE IF NOT EXISTS h_<name> (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(64)  NOT NULL UNIQUE,
    name_uz      VARCHAR(255) NOT NULL,
    name_ru      VARCHAR(255),
    name_en      VARCHAR(255),
    parent_id    BIGINT REFERENCES h_<name>(id),   -- ierarxik bo'lsa
    sort_order   INTEGER DEFAULT 0,
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMPTZ DEFAULT now(),
    updated_at   TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_h_<name>_code ON h_<name>(code);
CREATE INDEX IF NOT EXISTS idx_h_<name>_active ON h_<name>(is_active) WHERE is_active;
```

### 2. Seed — S### (boshlang'ich qiymatlar)

```sql
INSERT INTO h_<name>(code, name_uz, name_ru) VALUES
  ('CODE1', 'Nomi UZ', 'Имя RU')
ON CONFLICT (code) DO NOTHING;
```

### 3. JPA Entity (`domain` modul)

`domain/src/main/java/uz/hemis/domain/entity/classifier/H<Name>.java`:

```java
@Entity
@Table(name = "h_<name>")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class H<Name> {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "name_uz", nullable = false) private String nameUz;
    @Column(name = "name_ru") private String nameRu;
    @Column(name = "name_en") private String nameEn;
    @Column(name = "is_active") private Boolean isActive = true;
    @Column(name = "sort_order") private Integer sortOrder = 0;
    @Column(name = "created_at", updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
```

> ❗ `@Data` / `@ToString` ishlatmang (equals/hashCode + lazy init buziladi).

### 4. Repository + Service + Cache

```java
public interface H<Name>Repository extends JpaRepository<H<Name>, Long> {
    Optional<H<Name>> findByCode(String code);
    List<H<Name>> findAllByIsActiveTrueOrderBySortOrderAsc();
}

@Service
@RequiredArgsConstructor
public class H<Name>Service {
    private final H<Name>Repository repo;

    @Cacheable("classifier<Name>")
    @Transactional(readOnly = true)
    public List<H<Name>> findAllActive() {
        return List.copyOf(repo.findAllByIsActiveTrueOrderBySortOrderAsc());
    }

    @CacheEvict(value = "classifier<Name>", allEntries = true)
    @Transactional
    public H<Name> save(H<Name> entity) { return repo.save(entity); }
}
```

**MAJBURIY:** `service/.../config/DashboardCacheConfig.java` ichidagi `TwoLevelCacheManager` ro'yxatiga `classifier<Name>` qo'shish, TTL **24h** (klassifikator kam o'zgaradi).

### 5. REST endpoint

```java
@RestController
@RequestMapping("/api/v1/classifiers/<name>")
@RequiredArgsConstructor
public class H<Name>Controller {
    private final H<Name>Service service;

    @GetMapping
    @PreAuthorize("hasAuthority('classifier.view')")
    public List<H<Name>Dto> list() { return service.findAllActive().stream().map(H<Name>Dto::from).toList(); }
}
```

## Univer distribution (ADR-0007)

Univer 224 OTM yangi klassifikatorni REST polling (`UniverApiService`) yoki Kafka topic (Phase 2) orqali oladi. Yangi klassifikator → Univer tomonida **alohida ishlamaydi**, faqat o'qiydi.

## Verification

```bash
./gradlew :domain:liquibaseStatus
grep "classifier<Name>" service/src/main/java/uz/hemis/service/config/DashboardCacheConfig.java
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/classifiers/<name> | jq length
./scripts/check_table_mappings.sh
```

## Constraints

- ❌ `@Cacheable` bor-u `DashboardCacheConfig` TTL yo'q → memory leak
- ❌ `@CacheEvict` pair yo'q → 24h stale data
- ❌ Entity'da `@Data` / `@ToString`
- ❌ `h_*` o'rniga boshqa prefiks (ADR-0006 buziladi)
- ✅ Univer tomonida alohida implement qilmaslik

## See also

- ADR-0006 · ADR-0007
- `.claude/skills/liquibase-changeset/SKILL.md`
- `service/.../config/DashboardCacheConfig.java`
- `domain/.../entity/classifier/` — mavjud misollar
