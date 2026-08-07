---
name: classifier-add
description: Yangi h_* klassifikator (reference table) qo'shish - markaziy aggregation. Trigger - "klassifikator qo'sh", "h_ jadval", "reference table", "lookup", "klassifikator distribution".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Add Classifier (h_* prefix)

> ADR-0006: Markaziy klassifikator → 230 OTM bir xil qiymatdan foydalanadi. CLAUDE.md "Klassifikatorlarni UMUMIY saqlash" maqsadi.
>
> **KANONIK SHAKL (ADR-0006, majburiy):** modern klassifikatorlar **`code`-PK** (natural key, `VARCHAR`) + **`ReferenceEntity`** base — ustunlar `name` / `name_ru` / `name_en`, `is_active`, `version` + 4 audit ustun.
> ❌ Surrogat `id` / `BIGSERIAL` PK YO'Q · ❌ ustun `name_uz` EMAS (to'g'risi `name`) · ❌ soft-delete YO'Q (`is_active=false` ishlatiladi).
> Real manba: `domain/.../entity/base/ReferenceEntity.java` + `V003_create_positions.sql` + `HPosition extends ReferenceEntity`.

## Workflow (5 layer)

### 1. Schema — V### migration

`liquibase-changeset` skill ishlating. Kanonik shape (`V003_create_positions.sql` bilan bir xil):

```sql
CREATE TABLE h_<name> (
    code       VARCHAR(20)  PRIMARY KEY,          -- natural key (ReferenceEntity.code); surrogat id YO'Q
    name       VARCHAR(255) NOT NULL,             -- ustun nomi `name` (NOT name_uz)
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    -- FK bo'lsa (h_position → h_position_type kabi):
    -- type_code   VARCHAR(20) NOT NULL REFERENCES h_position_type(code),
    -- Hierarxik bo'lsa: parent_code VARCHAR(20) REFERENCES h_<name>(code),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,    -- @Version optimistic lock
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
-- FK ustuniga indeks (PostgreSQL avtomatik qo'ymaydi):
-- CREATE INDEX idx_h_<name>_type ON h_<name>(type_code);
```

> ⚠️ Soft-delete ustuni (`deleted_at`/`delete_ts`) QO'SHMANG — klassifikator o'chirilmaydi, `is_active=false` qilinadi (ReferenceEntity qoidasi).

### 2. Seed — S### (boshlang'ich qiymatlar)

```sql
INSERT INTO h_<name>(code, name, name_ru) VALUES
  ('CODE1', 'Nomi UZ', 'Имя RU')
ON CONFLICT (code) DO NOTHING;
```

### 3. JPA Entity (`domain` modul)

`H<Name>` **`ReferenceEntity` dan meros oladi** — `code` (PK), `name` / `nameRu` / `nameEn`, `isActive`, `sortOrder`, `version` + audit ustunlar tayyor. Faqat qo'shimcha ustun (FK va h.k.) yoziladi.

Joylashuv: klassifikatorning domen paketiga (mavjud misolga qarab) — masalan `entity/employee/H<Name>.java` (`HPosition`), `entity/infrastructure/H<Name>.java` (`HRoofType`). Alohida `classifier` paketi shart emas.

```java
@Entity
@Table(name = "h_<name>")
@Getter @Setter @NoArgsConstructor
public class H<Name> extends ReferenceEntity {

    // ReferenceEntity beradi: code (PK, String), name, nameRu, nameEn,
    // isActive, sortOrder, version, createdAt/By, updatedAt/By, equals/hashCode

    // Faqat qo'shimcha ustun (FK bo'lsa):
    @Column(name = "type_code", nullable = false, length = 20)
    private String typeCode;   // FK → h_position_type(code)
}
```

> ❗ `@Id` / `id` maydon YOZMANG — PK `code` `ReferenceEntity` dan keladi.
> ❗ `@Data` / `@ToString` ishlatmang (equals/hashCode allaqachon ReferenceEntity'da; lazy init buziladi).

### 4. Repository + Service + Cache

PK turi **`String`** (code) — `Long` EMAS:

```java
public interface H<Name>Repository extends JpaRepository<H<Name>, String> {
    List<H<Name>> findAllByIsActiveTrueOrderBySortOrderAsc();
    // findById(code) — code PK bo'lgani uchun tayyor
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

**MAJBURIY:** `service/.../config/DashboardCacheConfig.java` `cacheManager()` ichidagi `redisCacheConfigurations` map'iga `classifier<Name>` qo'shish (`redisCacheConfigurations.put("classifier<Name>", defaultConfig.entryTtl(Duration.ofHours(24)));`), TTL **24h** (klassifikator kam o'zgaradi).

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

## Univer distribution (ADR-0007 / ADR-0012)

Klassifikator markazdan OTM'ga **PUSH** qilinadi: outbox → Kafka (`hemis.classifier.events.v1`) → `WebhookFanoutConsumer` → 224 OTM HMAC callback (`kafka-outbox-topic` + `webhook-target-add` skill'lari). Univer (per-OTM) yangi qiymatni sync'da oladi va **faqat o'qiydi** — OTM tomonida alohida implement qilinmaydi. (REST fallback klient: `service/integration/HemisApiService`.)

## Verification

```bash
./gradlew :domain:liquibaseStatus
grep "classifier<Name>" service/src/main/java/uz/hemis/service/config/DashboardCacheConfig.java
grep -n "extends ReferenceEntity" domain/src/main/java/uz/hemis/domain/entity/**/H<Name>.java
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/v1/classifiers/<name> | jq length
./scripts/check_table_mappings.sh
```

## Constraints

- ❌ Surrogat `id` / `BIGSERIAL` PK → ADR-0006 `code`-PK buziladi (`code VARCHAR` PK ishlating)
- ❌ Ustun nomi `name_uz` → to'g'risi `name`
- ❌ `ReferenceEntity` extend qilmaslik → audit/version/equals qo'lda yozilib xato bo'ladi
- ❌ Soft-delete ustuni (`deleted_at`) → klassifikatorda `is_active` ishlatiladi
- ❌ `@Cacheable` bor-u `DashboardCacheConfig` TTL yo'q → memory leak
- ❌ `@CacheEvict` pair yo'q → 24h stale data
- ❌ Entity'da `@Data` / `@ToString`
- ❌ `h_*` o'rniga boshqa prefiks (ADR-0006 buziladi)
- ✅ Univer tomonida alohida implement qilmaslik

## See also

- ADR-0006 · ADR-0007 · ADR-0012
- Kanonik base: `domain/src/main/java/uz/hemis/domain/entity/base/ReferenceEntity.java`
- Real DDL: `domain/src/main/resources/db/changelog/changesets/schema/V003_create_positions.sql`
- `.claude/skills/liquibase-changeset/SKILL.md`
- `service/.../config/DashboardCacheConfig.java`
- Mavjud `h_*` entity misollari: `HPosition` (`entity/employee/`), `HRoofType` (`entity/infrastructure/`)
