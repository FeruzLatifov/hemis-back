---
id: ADR-0010
status: implemented
date: 2026-05-08
revised: 2026-05-19
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects:
  - domain
  - service
  - api-university
liquibase:
  - V014_create_employee_sync_infrastructure.sql  # employee_sync_log drop inlined (2026-05-19)
entities:
  - Employee
  - EmployeeJobs
  - OutboxEvent
verification: |
  # Schema applied
  ./gradlew :domain:liquibaseStatus | grep V014
  # Outbox table exists
  psql -d $DB_MASTER_NAME -c "\d outbox_event"
  # Employee sync metadata columns
  psql -d $DB_MASTER_NAME -c "\d+ employee" | grep synced_at
  # 175/175 contract preserved
  cd /home/adm1n/projects/startup/hemis-tools/docs/univer_tool && node compare_endpoints.js
related:
  - ADR-0007
  - ADR-0008
  - ADR-0012
---

# ADR 0010: Employee Sync — Direct Kafka Producer pattern (api-university)

## Status

**Implemented** (2026-05-08, qayta ko'rib chiqilgan: 2026-05-18 + 2026-05-19)

> **Y-Statement:** 224 ta Univer (`hemis_337`, `hemis_401`, …) dagi xodim ma'lumotlarini markaziy HEMIS-back'ga sync qilish uchun, biz **Direct Kafka Producer + Idempotent Consumer** pattern'ini tanladik (api-university REST endpoint → KafkaTemplate.send() → 202 Accepted → background consumer DB upsert ON CONFLICT pinfl), chunki controller DB write qilmaydi (atomicity outbox kerakmas — DB write consumer'da), 224 OTM PHP kodi o'zgarmaydi (REST shape saqlanadi), va peak 10 ev/sec yuk Kafka partitioning bilan PINFL bo'yicha serial qayta ishlanadi (optimistic-lock collision yo'q).
>
> **2026-05-18 audit:** Asl ADR outbox pattern taklif qilgan edi, lekin realiteti — direct Kafka. Sabab: EmployeeSyncController **DB write qilmaydi** (consumer'da bo'ladi), shuning uchun outbox atomicity foydasi yo'q. Idempotent upsert (PINFL UNIQUE constraint) duplicate semantic'ni qoplaydi.
>
> **2026-05-19 revision — `employee_sync_log` jadvali DROP qilindi (V014 ichiga inline):**
> Tahlilda jadval 80% duplikat ekanligi aniqlandi (`activity_log` + `error_log` + Sentry birga 9 maydondan 7 tasini qoplaydi). Faqat unique qolardi: `source_uid` (allaqachon `employee_job.source_uid` da bor), `SKIP_UNCHANGED`/`CONFLICT_OVERWRITE` enum'lari (`content_hash` skip semantikasi bilan ifodalanadi). Jadval ADR-0003 ham buzgan — audit hot OLTP DB ichida emas, alohida `hemis_audit` DB'da bo'lishi shart. Yechim: `EmployeeSyncProcessor.process()` ga `@Audited(action=UPDATE, entity="Employee", entityClass=Employee.class)` annotation qo'shildi — sync event'lar avtomatik `activity_log` (hemis_audit DB) ga yoziladi. Production DB'da `employee_sync_log` bo'sh edi (Processor hech qachon yozmagan), shuning uchun ma'lumot yo'qotilmadi.
>
> **2026-05-25 revision — Job (ish joyi) tarixi + defensive code resolution:**
> Asl sync faqat xodim *shaxsiy* ma'lumotini olib kelardi (`employee_job` row'lar `department_code`/`position_code`=NULL bo'lib bo'sh yaratilardi). Endi to'liq ish joyi sync qilinadi, legacy `EmployeeMetaUpdater` (`hemishe_e_employee_jobs`) yonida **parallel**, uni o'zgartirmasdan:
> - **Manba o'zgardi:** Univer console (`SyncEmployeesToHemisController`) endi `e_employee` o'rniga `e_employee_meta` JOIN `e_employee` (+ `e_department`) bo'yicha iteratsiya qiladi. Har meta yozuvi = bitta `employee_job` → ish joyi **tarixi** saqlanadi. `source_uid = univer-{code}-e_employee_meta-{metaId}` (avval `e_employee-{id}`).
> - **Normalizatsiya:** bir shaxs → ko'p job, `employee.pinfl` (UNIQUE) orqali bittala `employee` rekordga bog'lanadi (`employee_job.employee_id` FK).
> - **Pasport tasdiqlash upstream:** shaxsiy ma'lumot allaqachon Univer tomonidan markaz `v2/services/passport-data/*` (MSPD) orqali olingan va tasdiqlangan. Sync paytida markaz **qayta MSPD chaqirmaydi** (100k+ ortiqcha chaqiruvni oldini olish) — kelgan ma'lumotga ishonadi.
> - **Defensive code resolution** (`EmployeeJobUpsertRepositoryImpl.UPSERT_SQL`): `department_code`/`position_code` endi resolving subquery orqali o'tadi — markaz klassifikatorida mavjud bo'lsa kod, aks holda NULL (FK violation YO'Q, batch DLQ'ga tushmaydi). `position_type_code` — Univer yubormaydi, `h_position.type_code` dan derive qilinadi (klassifikator kodlari bitta vazirlik manbasidan: Univer `h_teacher_position_type` = markaz `h_position`, masalan `16`=Kafedra mudiri). Schema o'zgarmadi, V014 frozen.

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

**Employee sync — Direct Kafka Producer + Idempotent Consumer pattern (api-university).**

### Asosiy printsip

> **Controller pre-validate + Kafka publish (DB write yo'q). Consumer side DB upsert (idempotent).**

```
Univer (224 OTM, kod o'zgarmaydi)
   │
   │ POST /api/v1/university/employees/sync
   │   (OAuth client_credentials, JWT university_code claim)
   ▼
api-university: EmployeeSyncController
   │
   │ 1. resolveUniversityCode(JWT) yoki X-University-Code header (dev)
   │ 2. Pre-validate PINFL har item uchun (Pinfl.isValid)
   │ 3. Invalid items → rejections list
   │ 4. Valid items → EmployeeSyncProducer.publish(batchId, ...)
   │
   │ 5. CompletableFuture.allOf(futures).get(30s)  ← acks=all kutish
   │
   ▼ 202 Accepted {batchId, accepted, rejected, rejections[]}

   ╔═══════════════════════════════════════════════════════════╗
   ║  Background (Kafka consumer)                              ║
   ╠═══════════════════════════════════════════════════════════╣
   ║  Topic: hemis.employee.sync.inbound (12 partitions)       ║
   ║  Key: PINFL → per-PINFL serial qayta ishlash              ║
   ║  Concurrency: 12 (= partition count)                      ║
   ║                                                           ║
   ║  EmployeeSyncConsumer.consume():                          ║
   ║    EmployeeSyncProcessor.process():                       ║
   ║      INSERT INTO employee ... ON CONFLICT (pinfl) DO UPDATE║
   ║      INSERT INTO employee_job ... ON CONFLICT (uc, src_uid)║
   ║      INSERT INTO employee_sync_log (audit row)            ║
   ║                                                           ║
   ║  Xato boshqaruvi (DefaultErrorHandler):                   ║
   ║    Throw → 3 retry (FixedBackOff 1s)                      ║
   ║    Hali xato → DLQ topic (hemis.employee.sync.inbound.dlq)║
   ║    Admin keyinchalik DLQ inspect/replay                   ║
   ╚═══════════════════════════════════════════════════════════╝
```

### Kalit qarorlar

| Qaror | Sabab |
|-------|-------|
| **Univer kodi o'zgarmaydi** | 175/175 contract (CLAUDE.md GOLDEN RULE #2) — REST shape saqlanadi |
| **Controller DB write yo'q (Kafka publish only)** | Atomicity outbox kerakmas — DB write consumer'da. Pre-validate Univer'ga sinxron rad qaytaradi (invalid PINFL) |
| **202 Accepted (200 emas)** | UX kontrakti — "qabul qilindi", "saqlandi" emas. Univer `batchId` orqali keyin `employee_sync_log` ni tekshirishi mumkin |
| **`acks=all` + idempotent producer** | Exactly-once semantics (Kafka 3.x default) — controller 202 OLDIN publish kafolat |
| **Topic partition key = PINFL** | Bir xil PINFL → bir partition → bir consumer thread → optimistic-lock collision yo'q |
| **Concurrency = 12 (= partition count)** | Maksimal parallel ishlash, partition bo'yicha order saqlanadi |
| **Idempotent consumer (ON CONFLICT pinfl)** | Duplicate publish (at-least-once Kafka) → DB darajada no-op |
| **DLQ topic** | Poison pill yoki business rule failure → DLQ + admin manual review |
| **Sync user — JWT subject (client_id)** | Audit tracking per-OTM |
| **api-university modul (api-legacy emas)** | Yangi endpoint, yangi B2B kontrakt — api-legacy 175/175 saqlanadi |

### Nega outbox pattern EMAS?

ADR-0010 asl yondashuvi outbox edi, lekin **realiteti** boshqacha bo'ldi va bu **to'g'ri qaror**:

| Argument | Outbox pattern | Direct Kafka (tanlanган) |
|----------|----------------|---------------------------|
| Atomicity (DB write + event) | ✅ Bir tx | ❌ Yo'q (lekin **kerakmas** — controller DB write qilmaydi) |
| UX consistency (200 = DB) | ✅ | ❌ (202 = queued, batchId polling) |
| Univer kodi o'zgarmaydi | ✅ | ✅ |
| Implementation complexity | ⚠️ Yuqori (outbox table + poller) | ✅ Past (KafkaTemplate.send) |
| Throughput | ⚠️ Poll delay (1-2s) | ✅ Darhol |
| Replay | ✅ Outbox row | ✅ Kafka topic |
| Data loss (acks=all) | ❌ Yo'q | ❌ Yo'q (idempotent producer) |
| Multi-consumer decoupling | ✅ | ✅ (bir xil topic) |

**Asosiy farq:** outbox pattern foydali bo'ladi qachonki **controller DB write qiladi va atomic event ham kerak**. EmployeeSync inbound: controller DB write qilmaydi → outbox foydasi yo'q.

**Outbox haqida:** `OutboxEventPublisher` infra mavjud (ADR-0007 Stage 1), ammo bu **api-legacy markaziy mutation** uchun (klassifikator, qoidalar) — ADR-0012 webhook fanout chain'ining manbasi. EmployeeSync (inbound) — boshqa flow.

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

- [ ] **V014 migration** — bitta atomic changeset:
  - `employee` ALTER: `synced_at TIMESTAMP NULL`
  - `employee_job` ALTER: `source_uid VARCHAR(100)`, `content_hash CHAR(64)`, `synced_at TIMESTAMP NULL`
  - `employee_job` partial UNIQUE INDEX `(university_code, source_uid) WHERE deleted_at IS NULL`
  - CREATE TABLE `outbox_event` (ADR-0007 Stage 1.2 dan kopirovka, kelajakda ko'p domen ishlatadi)
  - CREATE TABLE `employee_sync_log` (audit trail per-record sync attempt)
- [ ] **Rollback file** — V014_*_rollback.sql
- [ ] **master.yaml** — V014 changeset entry
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

> **Eslatma:** ADR `Accepted` qarorni anglatadi, **implementatsiyani EMAS**. Implementatsiya holati (2026-05-19 holatiga):
> - ✅ Stage 1.1 — V014 migration applied (`outbox_event` + employee sync columns)
> - ✅ Stage 1.2-1.4 — `OutboxEvent` entity, `EmployeeSyncController`, `EmployeeSyncConsumer`
> - ✅ Stage 1.5 — Kafka integration (`KafkaConfig`, idempotent producer, `OutboxPoller` SKIP LOCKED)
> - ⏳ Stage 1.6 — Bulk sync rollout (OTM coordination — har OTM Yii2 HemisApi.php deploy)

## Verification

```bash
# 1. Schema applied
./gradlew :domain:liquibaseStatus | grep V014

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
- [ ] V014 changeset applied (master.yaml entry)
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
- `domain/src/main/resources/db/changelog/changesets/schema/V010_create_university_buildings.sql` — sync metadata pattern
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
