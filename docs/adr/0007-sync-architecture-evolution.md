---
id: ADR-0007
status: partially-implemented
date: 2026-05-06
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects: [service, api-external, infrastructure]
liquibase:
  - V014_create_employee_sync_infrastructure.sql
  - V015_create_webhook_infrastructure.sql
entities: [OutboxEvent]
verification: |
  # Outbox jadval mavjud
  psql -d $DB_MASTER_NAME -c "\d outbox_event"
  # spring-kafka dependency
  grep "spring-kafka" service/build.gradle.kts
related: [ADR-0008, ADR-0010, ADR-0012]
---

# ADR 0007: Sync Architecture — Selective Kafka Adoption

## Status

**Partially Implemented** (2026-05-06, qayta ko'rib chiqilgan: 2026-05-18 — Stage 2 va Apicurio SUPERSEDED/DEFERRED)

> **Implementation status (2026-05-18 audit):**
>
> **Markaz internal:**
> - ✅ Stage 0 — Docker Compose Kafka (`apache/kafka:3.7.0`)
> - ✅ Stage 1.1 — V014/V015 migration (`outbox_event` + `webhook_*` jadvallari)
> - ✅ Stage 1.2 — `spring-kafka` dependency (`service/build.gradle.kts:47`)
> - ✅ Stage 1.3 — `OutboxEvent` JPA entity + `OutboxEventRepository`
> - ✅ Stage 1.4 — `OutboxEventPublisher` + `OutboxPoller` (`@Scheduled` 1s, SKIP LOCKED)
> - ✅ Stage 1.5 — Kafka topic config (`KafkaTopicConfig.java`, `application.yml`)
> - ✅ Stage 1.6 — `KafkaConfig` (acks=all, idempotent producer, manual ack)
> - ✅ Stage 1.7 — Domain event publisher integration: `ClassifierWebService` ham `outboxEventPublisher.publish(...)` chaqirilgan (3 joy: line 46, 252, 329, 398). Webhook fanout topiclar boshqaruv yo'lida ishlamoqda. Boshqa domain'lar (Student, Contract, Grade) hali ulanmagan — Stage 1.8 qaytariladi.
>
> **Markazga inbound (api-university → Kafka):**
> - ✅ `EmployeeSyncController` → direct Kafka publish (outbox EMAS — chunki controller DB write qilmaydi, atomicity kerakmas)
> - ✅ `EmployeeSyncConsumer` — idempotent upsert (ON CONFLICT pinfl), DLQ
> - ⚠️ Boshqa domain sync (Student, Contract, Schedule, Grade) — ❌ Implementation yo'q
>
> **Markazdan outbound (markaz → 224 Univer):**
> - ✅ ADR-0012 webhook outbound (Outbox + Kafka fanout + REST callback)
> - ✅ Source publisher (Stage 1.7) — ClassifierWebService bog'langan; Student/Contract/Grade keyingi sprint'da
>
> **Stage 2 (224 OTM Kafka producer migration) — SUPERSEDED (2026-05-18).**
> Sabab: api-legacy 175/175 frozen kontrakt saqlanadi. 224 Univer PHP backend `HemisApi.php` REST orqali sync qiladi — Kafka producer migration kerakmas. Univer kodbase'iga PHP `rdkafka` ekstension deploy 224 OTM bo'yicha massive ops overhead bo'lardi (heterogeneous PHP environment, davlat tarmoq policy TCP 9092 yopiq). Real-time push uchun **webhook outbound** (ADR-0012) yetadi.
>
> **Schema Registry (Apicurio) — DEFERRED (2026-05-18).**
> Sabab: hozirgi volume (~0.3 event/sec) JSONB + Bean Validation bilan to'liq qoplanadi. Apicurio cluster, CI gate, schema PR workflow — 100K+ event/day yoki multi-team schema evolution real talab bo'lganda re-evaluate.

## Context

### Hozirgi sync arxitekturasi

HEMIS-back **markaziy aggregation point** sifatida 3 ta yo'nalishli traffic'ga bardosh berishi kerak:

```
INBOUND (Univer → markaz):
  [224 ta per-OTM Univer (Yii2 PHP)]   ──HTTPS REST──▶  [Markaziy HEMIS-back]
   hemis_337, hemis_401, ..., hemis_NNN                  ↳ /app/rest/v2/entities/hemishe_*
                                                         ↳ /app/rest/v2/services/*
                                                         ↳ /api/v1/university/*
OUTBOUND #1 (markaz → Univer) — HOZIRDA YO'Q:
  [Markaziy HEMIS-back]   ❌──no back-channel──▶   [224 Univer]
   ↳ klassifikator update (h_*) hozir cron-based pull
   ↳ qoidalar push (talaba kiritish lock, baho lock) yo'q

OUTBOUND #2 (markaz ↔ davlat sistemalari):
  [Markaziy HEMIS-back]  ◀──S2S──▶  [MyGov, MSPD, BIMM, Tax/Soliq, GUVD, OneID]
   ↳ aggregated reports (vazirlik darajasidagi statistika)
   ↳ PINFL verifikatsiya (passport, Tax sub'ekt)
   ↳ OneID auth federation
```

- **67 ta unique endpoint** (33 entity + 32 service + 2 OAuth — `@docs/UNIVER_CONTRACT.md`)
- **35 ta caller class** (Univer tomon)
- **Hozir 1-yo'nalishli sync** (Univer → Hemis-back) — back-channel yo'qligi bois klassifikator/qoida push imkonsiz
- **REST + JWT** (ADR-0005 client_credentials migration plan'da)
- **Schema** 100% FROZEN `hemishe_*` CUBA legacy

### Greenfield strategic position

**Hemis-back hali production'ga ulanmagan**, 224 OTM hozircha old-hemis CUBA bilan ishlaydi. Bu vaziyat — arxitekturani boshidanoq event-driven qilib qurishga imkon beradi:
- Production'da real user yo'q → canary pilot ortiqcha (kim bilan canary qilamiz?)
- "REST sync hardening → keyin Kafka migrate" eski path keraksiz qatlam — birdaniga to'g'ri yo'lda boshlash ratsionalroq
- Webhook back-channel hech qachon implementatsiya qilinmadi → Kafka topic'i o'rnida shu rolni o'taydi

### Sync hajmi (taxminiy production'ga ulanganda)

**INBOUND (Univer → markaz):**

| Metrika | Qiymat |
|---------|--------|
| 224 OTM × ~100 student/day update | ≈ 22,000 POST/day |
| 224 OTM × ~10 teacher update/day | ≈ 2,200 POST/day |
| 224 OTM × ~5 publication/project upsert | ≈ 1,100 POST/day |
| **Jami inbound** | **≈ 25,000+ POST/day** |
| Peak time concurrent OTM (semestr boshi) | ≈ 50 OTM simultaneously |

**OUTBOUND #1 (markaz → Univer):**

| Metrika | Qiymat |
|---------|--------|
| 230 OTM × ~50 klassifikator update sync | ≈ 11,500 events/day |
| Qoidalar push (talaba lock, baho lock) | ≈ 100 events/day (event-driven) |
| Notification (admin xabarnomasi) | ≈ 500 events/day |

**OUTBOUND #2 (markaz ↔ davlat sistemalari):**

| Metrika | Qiymat |
|---------|--------|
| MyGov auth federation | ≈ 5,000 calls/day |
| MSPD social welfare verify | ≈ 1,000 calls/day |
| Tax sub'ekt PINFL check | ≈ 2,000 calls/day |
| Vazirlik aggregated reports | ≈ 10 daily, ≈ 50 monthly |
| **Jami outbound government** | **≈ 8,000+ calls/day** |

### Pain points (event-driven yechadigan muammolar)

8 ta muammo aniqlandi — Kafka + Outbox pattern hammasini hal qiladi:

#### 1. Data loss — fire-and-forget POST
```php
// Univer EmployeeUpdater.php
$this->_client->post('/v2/entities/hemishe_ETeacher', $data)->send();
// Network 503? Hemis-back deploy in progress? → silently lost
```
- Outbox pattern yo'q, retry queue yo'q, DLQ yo'q

#### 2. Idempotency — duplicate insert
- `Idempotency-Key` header yo'q, business_key (PINFL) bilan upsert har controller'da o'zicha

#### 3. Concurrency — lost update
- `ETag` / `If-Match` yo'q, JPA `@Version` ishlatilmaydi

#### 4. Bulk inefficiency
- Per-row POST: 1000 student = 1000 ta TCP handshake (~50s parallel, ~500s sequential)

#### 5. Observability
- Markaziy event log yo'q, debug uchun 224 OTM SSH access talab qiladi

#### 6. Back-channel YO'Q (KRITIK — markaziy maqsadning #2)
- HEMIS-back markaziy klassifikator distribution maqsadi (ADR-0006) — yagona manba sifatida saqlash va Univer'larga yetkazish
- **Hozir:** Univer cron orqali har 1—24 soatda HEMIS-back'dan klassifikatorni pull qiladi (passive lag)
- **Maqsad (ADR-0006 push):** markaz event chiqaradi → Univer'lar darhol oladi (Kafka outbound topic)
- **Hozirgi pull → kelajak push** o'tish ushbu ADR-0007 ning asosiy konstruktsiyasi

#### 7. Schema drift
- Univer JSON yangi field silently dropped, contract test yo'q

#### 8. Schema coupling
- 67 endpoint `hemishe_*` jadvallariga 1:1 mapped

### Strategic constraints

- **Backward compat**: 224 OTM PHP kodi keyingi 12 oyda 100% ishlashi shart (kontrakt!)
- **No migration coercion**: 224 OTM o'z tezligida Kafka'ga o'tadi — markaz tomon majburlamaydi
- **Cost-conscious**: Kafka cluster lokal docker'da boshlanadi, production scale'ga ehtiyojda yoyiladi

## Decision

**Kafka-first internal architecture, immediate setup.**

| Bosqich | Maqsad | Vaqt | Qamrov |
|---------|--------|------|--------|
| **0** | Schema YAGNI cleanup | DONE (2026-05-04) | 7 jadval |
| **1** | Kafka cluster + Outbox + internal events | 4—6 hafta | Hemis-back internal |
| **2** | 224 OTM Kafka producer migration | OTM ready'da (no fixed deadline) | 100% (kutiluvchi) |

### Kalit qarorlar

| Qaror | Sabab |
|-------|-------|
| REST endpointlar 12+ oy saqlanadi | 224 OTM PHP klient compat (FROZEN kontrakt) |
| Backend internal write path event-driven boshidanoq | Greenfield — to'g'ri yo'lda boshlash arzonroq |
| Pilot bosqich olib tashlandi | Production'da real user yo'q (kim bilan canary?) |
| REST hardening (Idempotency-Key, ETag, bulk) hozir kerakmas | Kafka exactly-once + idempotent consumer qoplaydi |
| Webhook back-channel keraksiz | Kafka topic'i shu rolni o'taydi |
| Debezium CDC dastlab keraksiz | Scheduled outbox poll past hajmda yetarli, volume isbotlanganda Debezium |

**Strangler Fig saqlanadi**: REST + Kafka parallel ishlaydi. 224 OTM o'z tezligida Kafka producer'ga o'tadi.

## Alternatives Considered

### Alternative 1: Status quo saqlash (REST only)
- ✅ Hech narsa o'zgartirish kerak emas
- ❌ Data loss, observability past, back-channel yo'q
- **Rad etish sababi:** scale uchun yetarli emas, debt eksponensial o'sadi

### Alternative 2: Big-bang Kafka migration (REST drop boshidanoq)
- ✅ Toza arxitektura, bitta protocol
- ❌ 224 OTM PHP team bir vaqtda yangilanishi kerak — 100% imkonsiz
- ❌ Backward compat kafolati buziladi
- **Rad etish sababi:** REST 12+ oy saqlanishi mantiqiy zarurat

### Alternative 3: Database logical replication (Univer DB → Hemis-back DB)
- ✅ Application kod o'zgarmaydi
- ❌ 224 ta DB ↔ 1 markaz schema mismatch, FK noaniqlik
- ❌ pg_logical replication slot 224 ta — PostgreSQL master CPU/memory load
- **Rad etish sababi:** schema heterogeneity DB-level repl'ni qo'llab-quvvatlamaydi

### Alternative 4: GraphQL Federation
- ✅ Schema-as-contract
- ❌ Sync write-heavy — GraphQL afzalligi yo'qoladi, N+1 risk
- **Rad etish sababi:** wrong tool for write-heavy sync

### Alternative 5: gRPC streaming
- ✅ Binary efficient, HTTP/2 multiplexing
- ❌ PHP gRPC ekosistemi zaif, schema evolution Kafka'dan zaif
- **Qisman qabul qilingan:** Kafka producer'larda binary serialization (Avro/Protobuf)

### Alternative 6: Webhook-only (push-pull)
- ✅ Sodda
- ❌ Webhook delivery guarantee zaif, replay yo'q, order yo'q
- **Rad etish sababi:** Kafka topic to'liq qoplaydi

### Alternative 7: 3-bosqichli rollout REST hardening + pilot bilan (asl plan, 2026-05-05)
- ✅ Risk minimization (canary deploy)
- ✅ REST hardening (Idempotency-Key, ETag) avval o'rnatadi
- ❌ **Greenfield holatga zid** — production'da user yo'q, canary pilot uchun audience yo'q
- ❌ REST hardening keyinchalik keraksiz qatlam (Kafka exactly-once kelganda)
- ❌ 12 oy timeline juda uzun, evolutionary path overhead
- **Rad etish sababi:** Greenfield uchun over-engineered. Birdaniga to'g'ri yo'lda boshlash mantiqiyroq.

## Consequences

### Positive

- ✅ **Data durability** — outbox + Kafka = at-least-once kafolat, idempotent consumer = effectively exactly-once
- ✅ **Observability** — Kafka topic = central event log (7+ day retention, replay imkoni)
- ✅ **Decoupling** — Univer va Hemis-back deploy mustaqil bo'ladi
- ✅ **Audit trail** — har event sourceable (compliance/forensic uchun)
- ✅ **Schema governance** — Apicurio Schema Registry + CI gate
- ✅ **Replay** — issue paydo bo'lsa, last 24h—7d replay
- ✅ **Greenfield clean start** — REST hardening qatlamsiz to'g'ri arxitektura
- ✅ **Strangler-friendly** — REST endpointlar saqlanadi, 224 OTM o'z tezligida o'tadi

### Negative

- ⚠️ **4—6 hafta investment** Kafka foundation uchun (Docker compose, Spring Kafka, outbox)
- ⚠️ **Yangi konsept** team uchun (outbox pattern, schema evolution)
- ⚠️ **Lokal docker overhead** — Kafka + Apicurio dev environment'da resource (~512MB RAM)
- ⚠️ **Schema evolution governance** — backward/forward compat qoidalari kerak

### Risks

- **Risk:** Outbox poll-based publisher Kafka downtime'da queue ortib ketadi (PostgreSQL disk yetmaydi)
  **Mitigation:** Retry policy + DLQ topic. `outbox_event` jadvalida `retry_count` field, > 5 marta fail bo'lsa DLQ'ga yo'naltiriladi va alert.

- **Risk:** Schema Registry breaking change pipeline'ni buzadi
  **Mitigation:** Compatibility mode = `BACKWARD` (yangi schema eski klient'larni o'qiy oladi). CI gate: schema PR avtomatik check.

- **Risk:** 224 OTM PHP team Stage 2'da Kafka producer integratsiyasiga vaqt topmaydi
  **Mitigation:** REST sync 100% parallel ishlaydi. Per-OTM feature flag (`Config::CONFIG_USE_KAFKA`). Hech qaysi OTM majburlanmaydi.

- **Risk:** Audit DB (`hemis_audit`) Kafka topic bilan mos emas
  **Mitigation:** ADR-0003 saqlanadi. Yangi consumer service `audit-event-consumer` Kafka topic'ni o'qib `activity_log`ga yozadi.

- **Risk:** Outbox pattern Univer team uchun yangi konsept (PHP idioma emas)
  **Mitigation:** Hemis-back tomon implementatsiya namuna sifatida — kod + dokumentatsiya ko'rsatiladi. Univer template'sini Hemis tomon yozib beradi (3 ta sample updater class).

## Implementation

### Bosqich 1: Kafka foundation (4—6 hafta)

#### 1.1 Docker Compose — Kafka KRaft + Apicurio

KRaft mode (Zookeeper-siz, Kafka 3.5+ standard):

```yaml
# docker-compose.yml — qo'shimcha service'lar
services:
  kafka:
    image: apache/kafka:3.7.0
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://kafka:9092,CONTROLLER://kafka:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"  # explicit topic management
      CLUSTER_ID: 4L6g3nShT-eMCtK--X86sw
    ports: ["9092:9092"]
    volumes: [kafka-data:/var/lib/kafka/data]

  apicurio:
    image: apicurio/apicurio-registry-mem:2.5.0.Final
    environment:
      QUARKUS_PROFILE: prod
    ports: ["8888:8080"]
    depends_on: [kafka]

  kafka-ui:  # admin UI (optional)
    image: provectuslabs/kafka-ui:latest
    environment:
      KAFKA_CLUSTERS_0_NAME: hemis-local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
      KAFKA_CLUSTERS_0_SCHEMAREGISTRY: http://apicurio:8080/apis/registry/v2
    ports: ["8889:8080"]
    depends_on: [kafka, apicurio]

volumes:
  kafka-data:
```

**Production**: 3-broker cluster, separate disks, ACL + SASL/SCRAM auth, TLS. Lokalda single broker yetarli.

#### 1.2 Outbox jadval — Liquibase migration

```sql
-- domain/.../changesets/schema/V012_create_outbox_event.sql
CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(50) NOT NULL,        -- 'student', 'teacher', 'classifier'
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,             -- 'created', 'updated', 'deleted'
    payload JSONB NOT NULL,
    schema_version INT NOT NULL DEFAULT 1,
    occurred_at TIMESTAMP NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP,                       -- NULL = not yet sent to Kafka
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT,
    correlation_id VARCHAR(100),
    causation_id VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON outbox_event (occurred_at)
    WHERE published_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate
    ON outbox_event (aggregate_type, aggregate_id, occurred_at);

COMMENT ON TABLE outbox_event IS 'Transactional outbox pattern (Chris Richardson). Atomic write with domain entity, async publish to Kafka.';
```

#### 1.3 Spring Kafka dependencies

```kotlin
// service/build.gradle.kts
dependencies {
    implementation("org.springframework.kafka:spring-kafka:3.2.0")
    implementation("io.apicurio:apicurio-registry-serdes-jsonschema-serde:2.5.0.Final")
}
```

#### 1.4 OutboxEvent JPA entity + repository

```java
// domain/.../entity/outbox/OutboxEvent.java
@Entity
@Table(name = "outbox_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "schema_version", nullable = false)
    private Integer schemaVersion = 1;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
}
```

```java
// domain/.../repository/OutboxEventRepository.java
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.publishedAt IS NULL ORDER BY e.occurredAt LIMIT :limit")
    List<OutboxEvent> findUnpublished(@Param("limit") int limit);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.publishedAt = :now WHERE e.id = :id")
    void markPublished(@Param("id") UUID id, @Param("now") LocalDateTime now);
}
```

#### 1.5 Outbox publisher service (poll-based)

```java
// service/.../outbox/OutboxPublisher.java
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepo.findUnpublished(100);
        for (OutboxEvent e : pending) {
            try {
                String topic = "hemis." + e.getAggregateType() + ".events.v1";
                kafkaTemplate.send(topic, e.getAggregateId(), e.getPayload()).get(5, TimeUnit.SECONDS);
                e.setPublishedAt(LocalDateTime.now());
            } catch (Exception ex) {
                e.setRetryCount(e.getRetryCount() + 1);
                e.setLastError(ex.getMessage());
                if (e.getRetryCount() > 5) {
                    log.error("Outbox event {} failed > 5 retries, sending to DLQ", e.getId());
                    kafkaTemplate.send("hemis.dlq.v1", e.getAggregateId(), e.getPayload());
                    e.setPublishedAt(LocalDateTime.now());  // mark as terminal
                }
            }
        }
    }
}
```

**Note:** Debezium CDC keyinchalik (volume > 10K event/min isbotlanganda) qo'shiladi. Hozirgi 25K POST/day = ~0.3 event/sec — scheduled poll yetarli.

#### 1.6 Atomic outbox write (transactional)

```java
// service/.../student/StudentService.java
@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {
    private final StudentRepository repo;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper jsonMapper;

    public Student create(StudentDto dto) {
        Student s = repo.save(toEntity(dto));
        outboxRepo.save(OutboxEvent.builder()
            .aggregateType("student")
            .aggregateId(s.getId().toString())
            .eventType("created")
            .payload(jsonMapper.writeValueAsString(s))
            .schemaVersion(1)
            .build());
        return s;
        // Bitta DB transaction'da: student INSERT + outbox INSERT = atomic
    }
}
```

**Eslatma**: Outbox publisher ALOHIDA transaction (separate `@Scheduled` thread). Domain transaction commit bo'lgandan keyin Kafka'ga yuboriladi → exactly-once semantics.

#### 1.7 REST controller delegation (CUBA compat saqlanadi)

```java
// api-legacy/.../StudentEntityController.java
@PostMapping("/v2/entities/hemishe_EStudent")
public ResponseEntity<?> create(@RequestBody StudentDto dto) {
    return ResponseEntity.ok(studentService.create(dto));
    // Service event publish qiladi (outbox) — REST shape o'zgarmaydi
}
```

REST response shape o'zgarmaydi — 224 OTM PHP klient buzilmaydi.

#### 1.8 Birinchi domain event'lar (rollout tartibi)

| Hafta | Topic | Source domain | Consumer (kelajakda) |
|-------|-------|---------------|----------------------|
| 3 | `hemis.student.events.v1` | StudentService CRUD | api-university dashboard, audit log |
| 4 | `hemis.classifier.events.v1` | ClassifierService CRUD | Univer subscribers (Stage 2) |
| 5 | `hemis.teacher.events.v1` | TeacherService CRUD | api-university dashboard |
| 5 | `hemis.audit.events.v1` | All write paths | hemis_audit DB consumer |
| 6 | `hemis.publication.events.v1` | PublicationService CRUD | research analytics |

#### 1.9 Schema Registry (Apicurio) — DEFERRED (2026-05-18)

> Hozirgi volume (~0.3 event/sec) JSONB + Bean Validation bilan to'liq qoplanadi.
> Apicurio cluster, CI gate, schema PR workflow — 100K+ event/day yoki multi-team
> schema evolution real talab paydo bo'lganda re-evaluate.
>
> Hozircha JSONB payload `OutboxEvent.schema_version` field bilan version'lanadi
> (manual increment), backward compat dasturchi tomonidan code review'da tekshiriladi.

### Bosqich 2: 224 OTM Kafka producer migration — SUPERSEDED (2026-05-18)

> **Bekor qilindi.** api-legacy 175/175 frozen kontrakt saqlanadi. 224 Univer PHP backend REST orqali sync qiladi (`HemisApi.php`). Real-time push uchun ADR-0012 webhook outbound infrastructure ishlatiladi (markaz → 224 Univer HTTPS callback + HMAC, TCP 9092 emas).
>
> **Sabab batafsil:** PHP `rdkafka` ekstension 224 OTM heterogeneous environment'ga deploy massive ops overhead. Per-OTM SASL/SCRAM credential rotation, davlat tarmoq policy (TCP 9092 yopiq), Univer kodbase 175/175 contract risk — barchasi ROI past qiladi.

## Configuration

```yaml
# application.yml
hemis:
  outbox:
    poll-interval-ms: 5000
    batch-size: 100
    max-retry-count: 5
    publishing-enabled: true
  kafka:
    bootstrap-servers: ${KAFKA_BROKERS:kafka:9092}
    schema-registry-url: ${APICURIO_URL:http://apicurio:8080/apis/registry/v2}
    producer:
      acks: all
      compression-type: snappy
      enable-idempotence: true
      max-in-flight-requests: 5
    consumer:
      group-id: hemis-back
      auto-offset-reset: earliest
      isolation-level: read_committed
```

```yaml
# .env (lokal dev)
KAFKA_BROKERS=kafka:9092
APICURIO_URL=http://apicurio:8080/apis/registry/v2
```

## Success Metrics

| Metrika | Stage 0 (hozirgi) | Stage 1 target (4—6 hafta) | Stage 2 target |
|---------|-------------------|------------------------------|------------------|
| Internal write path event coverage | 0% | 100% (outbox) | 100% |
| Outbox publisher latency p99 | N/A | < 10s (poll-based) | < 1s (Debezium CDC) |
| Audit event durability | best-effort | exactly-once (outbox) | exactly-once |
| Backend deploy independence | partial | full (Kafka buffer) | full |
| Schema evolution governance | none | Apicurio + CI gate | + Univer side |
| 224 OTM Kafka coverage | 0% | 0% (kutilayapti) | 100% (target) |
| Outbox DLQ event count/day | N/A | < 10 | 0 |

## Migration Path

### Stage 1 (4—6 hafta — Hemis-back internal)

| Hafta | Faza | Faoliyat |
|-------|------|----------|
| 1 | Setup | Docker compose Kafka + Apicurio, Spring Kafka deps, V012 outbox migration |
| 2 | Foundation | OutboxEvent entity + repo, OutboxPublisher (poll-based), birinchi schema (student-event-v1) |
| 3 | First domain | StudentService → outbox → Kafka topic, basic test consumer |
| 4 | Domain rollout | TeacherService, ClassifierService — outbox |
| 5 | Domain rollout | PublicationService, ProjectService — outbox |
| 6 | Audit consumer | hemis_audit → Kafka consumer service, observability dashboards |

### Stage 2 (224 OTM migration — when ready)

- 224 OTM PHP team tayyorgarlik (har OTM IT team o'z tezligida)
- Wave deploy: 5 OTM → 50 OTM → 224 OTM
- Per-OTM feature flag toggle
- REST sync 12+ oy parallel ishlaydi (kontrakt majburiyati)
- 12—18 oy keyin REST endpointlar `Sunset: 2027-12-31` header bilan deprecate

## References

### Code (current state)
- `/home/adm1n/projects/startup/hemis-back/api-legacy/` — current sync receivers (123 controller)
- `/home/adm1n/projects/startup/univer/common/components/hemis/sync/` — current sync senders (35 caller)
- `docs/UNIVER_CONTRACT.md` — frozen API contract (67 endpoint)
- `docs/UNIVER_ENDPOINT_AUDIT.md` — per-endpoint audit
- `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/` — 175 ta integration test

### Books
- Chris Richardson, *Microservices Patterns* (2018) — Outbox, Saga, CQRS patterns
- Ben Stopford, *Designing Event-Driven Systems* (2018) — Kafka topology, Schema Registry
- Martin Kleppmann, *Designing Data-Intensive Applications* (2017) — replication, consensus

### Standards
- Kafka KRaft mode — https://kafka.apache.org/documentation/#kraft
- Confluent Schema Compatibility — https://docs.confluent.io/platform/current/schema-registry/avro.html

### Tools
- Apache Kafka 3.x KRaft — https://kafka.apache.org/documentation/
- Apicurio Schema Registry — https://www.apicur.io/registry/
- Spring Kafka — https://docs.spring.io/spring-kafka/reference/
- Debezium PostgreSQL connector (Stage 2'da) — https://debezium.io/documentation/

### Related ADRs
- ADR-0003 (Audit DB isolation) — Kafka consumer entry point
- ADR-0004 (api-university module) — per-OTM scope boundary (rows-level), topic partition by university_code
- ADR-0005 (OAuth client_credentials) — OTM auth (independent track)
- ADR-0006 (h_* classifier prefix) — classifier event topic source
