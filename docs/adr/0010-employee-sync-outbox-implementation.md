---
id: ADR-0010
status: proposed
date: 2026-05-08
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects:
  - domain
  - service
  - api-legacy
  - api-web
liquibase:
  - V015_create_employee_sync_infrastructure.sql
entities:
  - Employee
  - EmployeeJobs
  - OutboxEvent
  - EmployeeSyncLog
verification: |
  # Schema applied
  ./gradlew :domain:liquibaseStatus | grep V015
  # Outbox table exists
  psql -d $DB_MASTER_NAME -c "\d outbox_event"
  # Employee sync metadata columns
  psql -d $DB_MASTER_NAME -c "\d+ employee" | grep synced_at
  # 175/175 contract preserved
  cd /home/adm1n/projects/startup/hemis-tools/docs/univer_tool && node compare_endpoints.js
related:
  - ADR-0007
  - ADR-0008
  - ADR-0001
---

# ADR 0010: Employee Sync — Transactional Outbox birinchi implementatsiyasi

## Status

Proposed (2026-05-08)

> **Y-Statement:** 224 ta Univer (`hemis_337`, `hemis_401`, …) dagi xodim ma'lumotlarini markaziy HEMIS-back'ga sync qilish uchun, biz **Transactional Outbox Pattern** ni tanladik (sync REST → DB write + outbox row bir transactionda → background OutboxPublisher Kafka'ga jo'natadi → consumer'lar side-effect bajaradi), chunki bu yo'l UI consistency'ni saqlaydi (REST 200 = DB'da bor) va atomicity kafolatlaydi (DB write + event publish hech qachon ajralib chiqmaydi); oqibatda Univer kodi 1 satr o'zgarmaydi, eventual consistency UX bug yo'q, va keyinchalik multi-consumer ekosistemasi (cache invalidation, audit log, search index) Kafka topic orqali decouple qilinadi.

## Context

### Hozirgi vaziyat

- `employee` jadvalida **0 row** (yangi schema, V004 da yaratilgan)
- `employee_job` jadvalida **0 row**
- 224 ta Univer DB (`hemis_337`, …) — har birida real xodim/lavozim ma'lumotlari mavjud
- `UniversityBuildingSyncService` — bu pattern allaqachon production'da ishlaydi (REST + idempotent upsert + content hash + tenant guard)
- ADR-0007 (Sync Architecture) — **Proposed**, lekin **0 satr kod** (audit: outbox table yo'q, Spring Kafka dep yo'q, OutboxPublisher yo'q)

### Foydalanuvchi savoli (2026-05-08)

Foydalanuvchi 3 ta variantni so'radi:

1. **V1**: Har Univer'ga Kafka user ochish (PHP rdkafka producer)
2. **V2**: HEMIS-back o'zi Kafka'ga ulanib, REST endpoint orqali qabul qilib, Kafka'ga tashlab, qolganini background task qilish
3. **V3**: Boshqacha struktura

Va alohida flow'lar:
- Initial bulk sync (224 OTM dan barcha mavjud xodimlar)
- Sinxron create (admin UI dan, 6 OTM Univer ishlatmaydigan + ministry staff)
- Async update via Kafka

### Univer ekosistemasi mavjud holati

`/home/adm1n/projects/startup/univer/` audit (2026-05-08):

- ✅ **`yii2-queue`** (`yiisoft/yii2-queue: ^2.0`) — Redis-backed durable queue, retry, exponential backoff
- ✅ Per-row sync metadata: `_sync` (bool), `_sync_status` (5 ta enum), `_qid` (queue task id)
- ✅ `e_system_sync_log` table — failure tracking, max 10 retry, 24h cooldown
- ✅ `HemisApi.php` (1764 satr) — REST sender, OAuth bearer token
- ✅ Cron jobs: `oneInADay`, `oneInAHour`, `everyFiveMinute` — batch retry
- ❌ `php-rdkafka` extension — **`composer.json` da YO'Q** (V1 blocker)

Demak Univer-side outbox **allaqachon mavjud** — faqat boshqa nomda (`yii2-queue` Redis backend = de-facto outbox).

### Volume tahlili

| Metrika | Qiymat | Manba |
|---------|--------|-------|
| 224 OTM × ~100 xodim avg | ≈ 22,400 row initial | bulk sync |
| Daily change rate (~1%) | ≈ 224 update/day | steady state |
| Daily peak rate | ~10 update/sec (semestr boshi) | concurrent OTM |
| **Hozirgi Kafka justification** | **YO'Q** (volume past) | per ADR-0007 |
| **Outbox justification** | **HA** (atomicity, multi-consumer, replay) | per Chris Richardson |

### Pain points (Outbox yechadigan muammolar)

1. **Asymmetric write path** — agar create sinxron, update Kafka bo'lsa, foydalanuvchi UI darhol read qilsa eski qiymat ko'radi (eventual consistency UX bug)
2. **DB ↔ Kafka 2-phase commit muammo** — `kafkaTemplate.send()` keyin `repo.save()` ketma-ket: 1-si muvaffaqiyatli, 2-si fail bo'lsa silent loss
3. **Cache invalidation, audit, notification** — har biri DB'ga ulanishi kerak (4× connection pool yuk)
4. **Replay** — issue paydo bo'lsa hech qanday tarix yo'q
5. **Multi-consumer** — 1 ta event'ga 4 ta service kerak, lekin barchasi DB'ga directly ulansa coupling
6. **ADR-0007 noaniqlik** — strategy bor, kod yo'q. Birinchi domen kerak.

## Decision

**Employee/EmployeeJobs sync — Transactional Outbox Pattern (Chris Richardson) — ADR-0007 Stage 1 ning birinchi konkret implementatsiyasi.**

### Asosiy printsip

> **Write path SYNC. Side-effects ASYNC via Kafka.**

```
Univer (224 OTM, kod o'zgarmaydi)
   │
   │ POST /v2/services/employee/sync (Univer Yii queue)
   ▼
api-legacy: EmployeeSyncController
   │ tenantGuard.verifyOwnership(universityCode)
   ▼
service: EmployeeSyncService.upsertBulk()
   │
   │ @Transactional {
   │   ├── employee  upsert (PINFL idempotent)
   │   ├── employee_job upsert (cascade by source_uid)
   │   ├── employee_sync_log INSERT (audit)
   │   └── outbox_event INSERT (atomic)
   │ }
   │
   ▼ 200 OK { processed, conflicts, eventIds: [...] }

   ╔═══════════════════════════════════════════════════════════╗
   ║  Background (decoupled from request)                      ║
   ╠═══════════════════════════════════════════════════════════╣
   ║  OutboxPublisher (@Scheduled fixedDelay=1s)               ║
   ║    SELECT ... WHERE published_at IS NULL LIMIT 100        ║
   ║    kafkaTemplate.send("hemis.employee.events.v1", ...)    ║
   ║    UPDATE outbox_event SET published_at = now()           ║
   ║                                                           ║
   ║  Kafka topic: hemis.employee.events.v1                    ║
   ║    ├── EmployeeCacheInvalidator (Redis L2 evict)          ║
   ║    ├── EmployeeAuditLogger (hemis_audit DB consumer)      ║
   ║    └── (future) EmployeeSearchIndexer, NotificationSvc    ║
   ╚═══════════════════════════════════════════════════════════╝
```

### Kalit qarorlar

| Qaror | Sabab |
|-------|-------|
| **Univer kodi o'zgarmaydi** | 175/175 contract (CLAUDE.md GOLDEN RULE #2) |
| **REST endpoint sync** | UI consistency, Univer queue retry ishlatadi |
| **DB write + outbox bir transactionda** | Atomicity (Chris Richardson Outbox Pattern canonical) |
| **OutboxPublisher Spring `@Scheduled` poller** | Volume past (~10 ev/s) — Debezium CDC overkill (ADR-0007 stage 3) |
| **Kafka publish failed → outbox kutadi** | Durable, no event loss |
| **Initial bulk = special endpoint** | `/v2/services/employee/sync-bulk` chunked (500/batch), resumable |
| **Admin UI create — sinxron, lekin same outbox** | UX consistency: REST 200 = DB'da, side-effects async |
| **Kafka topic naming**: `hemis.{aggregate}.events.v1` | ADR-0007 ga muvofiq |
| **Topic partition key**: `university_code` | Per-OTM ordering kafolat |
| **Schema versioning**: JSONB payload + `schema_version` | Apicurio kelgunga qadar oddiy |

### Nima Kafka bilan, nima Kafka'siz

| Flow | Sync DB write | Outbox row | Kafka publish | UX javob |
|------|--------------|------------|--------------|---------|
| Univer event sync (`/v2/services/employee/sync`) | ✅ | ✅ | ✅ async | 200 OK darhol |
| Markaziy admin create (UI, 6 OTM) | ✅ | ✅ | ✅ async | 200 OK darhol |
| Markaziy admin update (UI) | ✅ | ✅ | ✅ async | 200 OK darhol |
| Initial bulk sync (chunked) | ✅ | ❌ (skip — log only) | ❌ | 200 OK (chunk) |
| Soft-delete (markaz) | ✅ | ✅ | ✅ async | 200 OK darhol |

**Initial bulk skip outbox** — chunki initial sync = "hech qachon hech kim ko'rmagan past data". Cache invalidation, notification keraksiz. Faqat audit log yetadi (compliance).

## Alternatives Considered

### Alternative 1: Per-OTM Kafka Producer (foydalanuvchi V1)

PHP `rdkafka` extension 224 OTM'ga deploy → Univer'dan to'g'ridan-to'g'ri Kafka'ga producer.

- ✅ Eng tez (REST overhead yo'q)
- ❌ `php-rdkafka` `composer.json` da yo'q — **224 OTM heterogeneous PHP environment**ga extension install (DevOps katta loyiha, 6+ oy)
- ❌ Per-OTM SASL/SCRAM credential — 224 ta key rotation, audit, compliance overhead
- ❌ Kafka brokers 224 OTM'dan tashqarida ochiq (security audit, certificate mgmt, firewall rules)
- ❌ **Univer kod o'zgaradi** → 175/175 contract risk
- ❌ Yii queue durable retry mexanizmi yo'qoladi
- ❌ Volume justification yo'q (0.3 ev/s — Kafka 1M ev/s uchun)
- **Rad etish sababi:** Massive ops overhead, contract buzilish, ROI past. ADR-0007 Stage 2 keyinchalik (kerak bo'lganda) qayta ko'rib chiqiladi.

### Alternative 2: REST → Kafka direct → background DB consumer (foydalanuvchi V2 — Async update)

REST endpoint qabul qiladi → kafkaTemplate.send() → 200 OK. Background consumer DB'ga yozadi.

- ✅ Univer kodi o'zgarmaydi
- ✅ Kafka decoupling
- ❌ **Eventual consistency UX bug**: REST 200 ≠ DB'da bor. UI darhol read qilsa "saqlanmadi" deb ko'rinadi
- ❌ Kafka publish fail → REST 5xx? Murakkab error semantics
- ❌ DB ↔ Kafka 2-phase commit muammo (atomicity yo'q)
- **Rad etish sababi:** UX consistency talab qiladi sync write. Outbox shu masalani atomic bilan hal qiladi.

### Alternative 3: Sync REST + idempotent DB upsert (Outbox'siz, foydalanuvchi V3 - "boshqacha")

REST → upsert → 200 OK. Kafka'siz.

- ✅ Eng oddiy
- ✅ BuildingSyncService allaqachon shu pattern
- ❌ Multi-consumer ekosistemasi (cache, audit, search) DB'ga directly ulanishi kerak
- ❌ Replay yo'q
- ❌ Audit DB (`hemis_audit`) bilan integratsiya manual (ADR-0003)
- ❌ Future-proofing yo'q (Kafka qo'shilgach refactor kerak)
- **Qisman qabul qilingan:** Initial bulk sync uchun bu yo'l (outbox'siz) — past hajm, audit log etarli. Steady state uchun Outbox afzal.

### Alternative 4: Async via Yii2 webhook → Kafka

Univer'da yangi webhook publisher → Kafka.

- ❌ Univer kod o'zgaradi (175/175 risk)
- ❌ Webhook delivery guarantee Kafka'ga tenglashmaydi
- **Rad etish sababi:** ADR-0007'da rad etilgan (Alternative 6).

### Alternative 5: Kafka topic'ini Univer push qiladi (REST'ni butunlay drop)

ADR-0007 Stage 2 — keyinchalik mumkin, hozir emas.

- **Hozir rad etish sababi:** Stage 1 hali ishga tushmagan. REST 12+ oy saqlanish kontrakt majburiyati.

## Consequences

### Positive

- ✅ **UI consistency** — REST 200 = DB'da yangi qiymat (read-after-write works)
- ✅ **Atomicity** — DB write + outbox INSERT bitta transaction, hech qachon ajralmaydi
- ✅ **Univer kod o'zgarmaydi** — 175/175 contract saqlanadi
- ✅ **Kafka deploy uzilsa — outbox kutadi** — no event loss
- ✅ **Multi-consumer decoupling** — cache, audit, notification = mustaqil services
- ✅ **Replay imkoni** — Kafka topic 7 day retention
- ✅ **ADR-0007 Stage 1 birinchi konkret implementatsiya** — Kafka skeleton template kelajakdagi domen (Student, Teacher, Classifier) uchun
- ✅ **BuildingSync pattern bilan moslashish** — `source_uid`, `content_hash`, `synced_at` aynan o'sha conventionlar
- ✅ **Initial bulk + steady state alohida** — har biri o'z optimum

### Negative

- ⚠️ **OutboxPublisher poll-based latency** — fixedDelay=1s, p99 ≈ 1-2s (cache invalidation darhol emas)
- ⚠️ **Yangi konsept jamoa uchun** — Outbox + Kafka schema evolution
- ⚠️ **Lokal dev Kafka overhead** — docker-compose.yml allaqachon bor lekin ~512MB RAM
- ⚠️ **Schema versioning manual** (Apicurio integratsiya keyinroq)
- ⚠️ **`outbox_event` table o'sib boradi** — partitioning keyinchalik (volume > 1M event/day bo'lganda)

### Risks

- **Risk:** OutboxPublisher Kafka downtime'da queue ortib ketadi (PostgreSQL disk yetmaydi)
  **Mitigation:** Retry policy + DLQ topic. `outbox_event.retry_count > 5` → `hemis.dlq.v1` topic + alert. Queue `last_error` audit qilinadi.

- **Risk:** Same-class self-invocation trap (`@Scheduled` + `@Transactional`)
  **Mitigation:** OutboxPublisher alohida `@Service` bean. Spring AOP proxy correctly intercept qiladi.

- **Risk:** Multiple instance OutboxPublisher race condition
  **Mitigation:** `SELECT ... FOR UPDATE SKIP LOCKED` — har instance o'z chunkini oladi, hech qanday duplikat publish.

- **Risk:** Bulk sync chunk'i o'rtasida fail bo'ladi
  **Mitigation:** `synced_at` cursor — Univer queue retry oxirgi muvaffaqiyatli `source_uid` dan davom etadi.

- **Risk:** PINFL conflict — 2 ta OTM bir xil PINFL push qilsa
  **Mitigation:** Last-write-wins by `synced_at`. `employee_sync_log` event_type=`CONFLICT_OVERWRITE` audit. Ministry alert (manual review).

- **Risk:** OutboxEvent payload juda katta (JSONB > 1MB)
  **Mitigation:** Outbox payload faqat aggregate ID + delta (full entity emas). Consumer kerak bo'lsa DB'dan o'qiydi.

## Implementation

### Stage 1.1 — Schema foundation (1 hafta)

- [ ] **V015 migration** — bitta atomic changeset:
  - `employee` ALTER: `synced_at TIMESTAMP NULL`
  - `employee_job` ALTER: `source_uid VARCHAR(100)`, `content_hash CHAR(64)`, `synced_at TIMESTAMP NULL`
  - `employee_job` partial UNIQUE INDEX `(university_code, source_uid) WHERE deleted_at IS NULL`
  - CREATE TABLE `outbox_event` (ADR-0007 Stage 1.2 dan kopirovka, kelajakda ko'p domen ishlatadi)
  - CREATE TABLE `employee_sync_log` (audit trail per-record sync attempt)
- [ ] **Rollback file** — V015_*_rollback.sql
- [ ] **master.yaml** — V015 changeset entry
- [ ] **Pre-commit hook** — yangi `outbox_event`, `employee_sync_log` mapping verification (`check_table_mappings.sh`)

### Stage 1.2 — Domain layer (1 hafta)

- [ ] **`OutboxEvent` JPA entity** (`domain/entity/outbox/`)
- [ ] **`EmployeeSyncLog` JPA entity** (`domain/entity/employee/`)
- [ ] **`Employee` ALTER**: `@Column synced_at`
- [ ] **`EmployeeJobs` ALTER**: `@Column source_uid, content_hash, synced_at`
- [ ] **`OutboxEventRepository`** — `findUnpublishedForUpdate(int limit)` SKIP LOCKED
- [ ] **`EmployeeSyncLogRepository`**

### Stage 1.3 — Service layer (1.5 hafta)

- [ ] **`OutboxAppender`** — `appendEvent(aggregateType, aggregateId, eventType, payload)` — same-tx insert helper
- [ ] **`EmployeeSyncService`** — BuildingSyncService pattern + outbox append
  - `syncFromUniver(universityCode, List<EmployeeSyncDto>)` — Univer push
  - `createByAdmin(EmployeeCreateDto)` — markaziy admin (6 OTM + ministry)
  - `updateByAdmin(UUID id, EmployeeUpdateDto)` — markaziy admin
- [ ] **`EmployeeJobsSyncService`** — cascade upsert by `(university_code, source_uid)`
- [ ] **Content hash computation** — SHA-256, sync-relevant fields only
- [ ] **`TenantGuard.verifyOwnership(universityCode)`** — JWT claim check (existing)
- [ ] **`OutboxPublisher`** — Spring `@Scheduled(fixedDelay=1000)` poller (Kafka mock initially)
- [ ] **Metrics** — `employee.sync.duration`, `outbox.publish.lag`

### Stage 1.4 — Controller layer (0.5 hafta)

- [ ] **`/v2/services/employee/sync` POST** — bulk single payload (Univer queue)
- [ ] **`/v2/services/employee/sync-bulk` POST** — chunked (500/batch, resumable cursor)
- [ ] **`/v2/services/employee/sync-status` GET** — Univer cursor tracking
- [ ] **`/api/v1/employees` POST** (api-web) — markaziy admin UI
- [ ] **DTO** — `EmployeeSyncDto` (Univer kontraktiga mos), `EmployeeCreateDto` (admin UI)
- [ ] **Validation** — `Pinfl.of()`, `Tin.of()`, `@Valid`
- [ ] **`@PreAuthorize`** — `hasAuthority('employee.sync')` (Univer), `hasAuthority('employees.create')` (admin)

### Stage 1.5 — Kafka integration (1 hafta — keyingi sprint)

- [ ] **`docker-compose.yml`** — Kafka allaqachon bor (ADR-0007), tekshirish
- [ ] **`application.yml`** — kafka config (bootstrap-servers, producer, consumer)
- [ ] **Spring Kafka dependency** — `spring-kafka:3.2.0` service module
- [ ] **`OutboxPublisher` → real Kafka producer** — mock dan real
- [ ] **`EmployeeCacheInvalidator`** — first consumer (Redis L2 evict)
- [ ] **`EmployeeAuditLogger`** — second consumer (`hemis_audit` DB)
- [ ] **DLT (dead letter topic)** — `hemis.dlq.v1`
- [ ] **Schema validation** — JSONB Bean Validation (Apicurio keyinroq)

### Stage 1.6 — Initial Bulk Sync orchestration (1 hafta)

- [ ] **Univer-side Yii console command** — `EmployeeBulkSyncCommand` (har OTM bir martalik)
- [ ] **Resume support** — `synced_at` cursor + `source_uid` page key
- [ ] **Pilot OTM 337** — first wave (1 OTM)
- [ ] **Wave deploy** — 5 → 50 → 224 OTM
- [ ] **Monitoring dashboards** — sync rate, error rate, conflict rate

### Stage 1.7 — Tests (parallel sprintlarda)

- [ ] Unit tests — EmployeeSyncService, OutboxAppender, content hash determinism
- [ ] Integration tests — `@SpringBootTest` real DB
- [ ] Side-by-side tests — `compare_endpoints.js` 175/175 saqlash
- [ ] Concurrency tests — 50 parallel POST `/sync` (DB lock contention)
- [ ] Outbox poller tests — race condition (multi-instance), Kafka failure simulation

> **Eslatma:** ADR `Accepted` qarorni anglatadi, **implementatsiyani EMAS**. Implementatsiya holati majburiy:
> - ⏳ Stage 1.1 — V015 migration (pending)
> - ⏳ Stage 1.2-1.4 — domain + service + controller (pending)
> - ⏳ Stage 1.5 — Kafka integration (pending, keyingi sprint)
> - ⏳ Stage 1.6 — Bulk sync rollout (pending, OTM coordination)

## Verification

```bash
# 1. Schema applied
./gradlew :domain:liquibaseStatus | grep V015

# 2. Outbox table mavjud
psql -d $DB_MASTER_NAME -c "\d outbox_event"
psql -d $DB_MASTER_NAME -c "\d employee_sync_log"

# 3. Employee sync metadata columns
psql -d $DB_MASTER_NAME -c "\d+ employee" | grep -E "synced_at"
psql -d $DB_MASTER_NAME -c "\d+ employee_job" | grep -E "source_uid|content_hash|synced_at"

# 4. 175/175 contract preserved (Univer side)
cd /home/adm1n/projects/startup/hemis-tools/docs/univer_tool && node compare_endpoints.js
# Expected: MATCH 175/175

# 5. Outbox publisher running
curl http://localhost:8081/actuator/scheduledtasks | jq '.cron[] | select(.runnable.target | contains("OutboxPublisher"))'

# 6. Employee sync metrics
curl http://localhost:8081/actuator/metrics/employee.sync.duration

# 7. Pre-commit hook validates new tables
./scripts/check_table_mappings.sh
```

**Acceptance criteria:**
- [ ] V015 changeset applied (master.yaml entry)
- [ ] `outbox_event`, `employee_sync_log` jadvallar yaratilgan
- [ ] `employee.synced_at`, `employee_job.source_uid/content_hash/synced_at` columnlar qo'shilgan
- [ ] `EmployeeSyncService.syncFromUniver()` integration test o'tadi (real DB)
- [ ] `OutboxPublisher` `@Scheduled` har 1s ishlaydi (mock'da log ko'rinadi)
- [ ] Univer mock POST `/v2/services/employee/sync` → 200 OK + `outbox_event` row mavjud
- [ ] 175/175 endpoint contract saqlangan
- [ ] Pilot OTM (hemis_337) bilan side-by-side test o'tadi
- [ ] Bulk sync chunk endpoint resume cursor bilan ishlaydi
- [ ] Concurrency test: 50 parallel sync — duplicate yo'q, lost update yo'q

## References

### Code (current state)
- `service/src/main/java/uz/hemis/service/infrastructure/UniversityBuildingSyncService.java` — canonical pattern
- `domain/src/main/java/uz/hemis/domain/entity/employee/Employee.java` — target entity (V004)
- `domain/src/main/java/uz/hemis/domain/entity/employee/EmployeeJobs.java` — target entity (V004)
- `domain/src/main/resources/db/changelog/changesets/schema/V004_create_employee.sql` — base schema
- `domain/src/main/resources/db/changelog/changesets/schema/V011_create_university_buildings.sql` — sync metadata pattern
- `/home/adm1n/projects/startup/univer/common/components/hemis/HemisApi.php` — Univer-side sender (1764 lines)
- `/home/adm1n/projects/startup/univer/common/config/hemis.php` — Univer sync model registry (`EEmployee`, `EEmployeeMeta`)

### Books / Patterns
- Chris Richardson, *Microservices Patterns* (2018) — Outbox, Saga, CQRS chapters 4 & 5
- Martin Fowler, *Patterns of Enterprise Application Architecture* (2002) — Domain Events
- Ben Stopford, *Designing Event-Driven Systems* (2018) — Kafka topology

### Standards
- Transactional Outbox Pattern — https://microservices.io/patterns/data/transactional-outbox.html
- Kafka KRaft mode — https://kafka.apache.org/documentation/#kraft

### Related ADRs
- ADR-0001 (Building table design) — sync metadata pattern source (`source_uid`, `content_hash`)
- ADR-0003 (Audit DB isolation) — `hemis_audit` consumer entry point
- ADR-0005 (OAuth client_credentials) — per-OTM JWT `university_code` claim
- ADR-0007 (Sync Architecture) — **bu ADR uning Stage 1 birinchi konkret implementatsiyasi**
- ADR-0008 (api-legacy entity rebinding) — modern `employee` table sync target qilingan
