---
id: ADR-0012
status: accepted
date: 2026-05-13
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects:
  - domain
  - service
  - api-web
  - common
liquibase:
  - V015_create_webhook_infrastructure.sql
  - M004_webhook_permissions.sql
entities:
  - WebhookTarget
  - WebhookDeliveryLog
  - WebhookDeliveryStatus
verification: |
  # 1. Schema
  ./gradlew :domain:liquibaseStatus | grep V015
  # 2. Compile
  ./gradlew :app:build -x test
  # 3. Permission grants
  psql -d $DB_MASTER_NAME -c "SELECT code FROM sec_permission WHERE code LIKE 'webhook.%'"
  # 4. Smoke test (after bootRun + docker-compose Kafka up)
  curl -X POST http://localhost:8081/api/v1/web/admin/webhooks \
       -H "Authorization: Bearer $TOKEN" \
       -d '{"universityCode":"337","callbackUrl":"http://localhost:9999/test"}'
  curl -X POST http://localhost:8081/api/v1/web/admin/webhooks/{id}/test \
       -H "Authorization: Bearer $TOKEN"
related:
  - ADR-0007
  - ADR-0010
  - ADR-0011
---

# ADR 0012: Webhook Outbound Infrastructure (markaz → Univer)

## Status

Accepted (2026-05-13)

> **Y-Statement:** Markaz tomondagi event'larni (klassifikator update, qoidalar push, OTM block) 224 ta Univer Yii2 PHP backend'iga real-time yetkazish uchun, biz **Transactional Outbox + Kafka fanout + REST webhook callback** strategiyasini tanladik, chunki bu yo'l Univer kodbase'ida minimal o'zgarish talab qiladi (yangi Yii2 controller + jadval + queue handler), markaz tomondan high-throughput va replay imkoniyatini beradi, va davlat tarmoq policy'lari bilan mos (faqat HTTPS — TCP 9092 kerak emas). Oqibatda real-time push (~2-5 sekund latency), per-OTM retry boshqaruvi, DLQ orqali silent failure himoyasi.

## Context

### Hozirgi vaziyat (2026-05-13)

| Yo'nalish | Holat |
|-----------|-------|
| **Inbound (Univer → markaz)** | ✅ Sync REST `/app/rest/v2/...` (175/175 contract) |
| **Outbound (markaz → Univer)** | ❌ **YO'Q** — back-channel yo'q |
| **Klassifikator distribution** | ⚠️ Univer cron pull (24h kechiktirish) |
| **Qoidalar push** | ❌ Real-time push imkonsiz |

### Univer ekosistemasi

- **PHP 7.4 + Yii2** — 224 ta OTM per-OTM deploy (`hemis_337`, `hemis_401`, …)
- **`yii2-queue` (Redis)** — durable queue, allaqachon ishlatiladi
- **`HemisApi.php`** (1764 satr) — outbound REST sender (Univer → markaz)
- ❌ `php-rdkafka` — composer.json'da yo'q (Kafka direct integration imkonsiz)

### Talab

1. **Real-time push** — vazirlik klassifikator yangilashida 5 sekund ichida 224 OTM yetkazish
2. **Idempotency** — bir event 2 marta kelishi mumkin (Kafka at-least-once)
3. **Retry + DLQ** — Univer offline bo'lsa qayta urinish, max attempts'dan keyin manual review
4. **HMAC auth** — Univer markazdan kelganligini tasdiqlash (signature verify)
5. **Per-OTM observability** — qaysi OTM oxirgi 24 soatda nechta fail bergan
6. **Davlat compliance** — data sovereignty (on-premise), HTTPS only

## Decision

**3-bosqichli arxitektura:**

```
1. Producer (markaz domain)        ↓ (Transactional Outbox)
2. Kafka fanout                    ↓ (per-OTM message)
3. REST webhook dispatcher         ↓ (HMAC + retry + DLQ)
   → Univer HemisCallbackController
```

### Komponentlar

| Komponent | Tafsilot |
|-----------|----------|
| **`outbox_event`** | V014 jadval (ADR-0010 dan meros) — atomic DB write + event publish |
| **`OutboxEventPublisher`** | Service'lar dan chaqiriladi (programmatic API) |
| **`OutboxPoller`** | `@Scheduled(1s)` — outbox → Kafka topic (FOR UPDATE SKIP LOCKED) |
| **`WebhookFanoutConsumer`** | Domain topiclardan o'qiydi, 224 OTM per-message yaratadi → `hemis.webhook.events` |
| **`WebhookDispatcher`** | `hemis.webhook.events` consumer → HMAC + REST POST + retry routing |
| **`WebhookRetryScheduler`** | DB-based retry queue (5s interval) — RETRY status'dagi log'larni qayta yuboradi |
| **`HmacSigner`** | SHA-256 signature + constant-time verify |
| **`WebhookSecretVault`** | In-memory plain secret cache (bcrypt hash DB'da) |
| **`WebhookTargetService`** | CRUD + regenerate-secret + delivery log views |
| **`WebhookTargetController`** | `/api/v1/web/admin/webhooks/**` — admin REST API |
| **`WebhookMetrics`** | Prometheus counter + latency histogram (per-OTM) |

### Kafka topology

| Topic | Partitions | Retention | Maqsad |
|-------|------------|-----------|--------|
| `hemis.classifier.events.v1` | 3 | 30 kun | h_* update'lar (domain) |
| `hemis.rule.events.v1` | 3 | 30 kun | Talaba/baho lock qoidalar |
| `hemis.university.events.v1` | 3 | 30 kun | OTM o'zgarishlari |
| `hemis.webhook.events` | 6 | 30 kun | Fanout (key=university_code) |
| `hemis.webhook.dlq` | 3 | 90 kun | Failed delivery |

### HTTP contract (Univer side)

```
POST {callbackUrl}
Content-Type: application/json
X-Hemis-Signature: {hex(HMAC_SHA256(secret, timestamp + "." + body))}
X-Hemis-Timestamp: {unix_epoch_seconds}
X-Hemis-University-Code: {NNN}

{
  "event_id": "550e8400-...",
  "event_type": "classifier.updated",
  "aggregate_type": "classifier",
  "aggregate_id": "123",
  "occurred_at": "2026-05-13T10:30:00",
  "schema_version": 1,
  "data": { ... }
}
```

### Retry policy (2026-05-18 trim: 5 → 3 attempts)

| HTTP javob | Status | Keyingi attempt |
|-----------|--------|-----------------|
| 2xx | SUCCESS | Terminal |
| 4xx | FAILED | Terminal (payload xato) |
| 5xx | RETRY | Exponential backoff |
| Timeout | RETRY | 1s, 30s, 5min |
| `attempt >= 3` | DLQ | Kafka DLQ topic + admin alert |

> **Asl plan 5 attempts edi (1s, 5s, 30s, 5min, 1h). Trim sababi:** Univer offline 1 soatdan oshsa baribir manual review kerak — 5-attempt 1 soat kutish foydasiz. 3-attempt = 5 minut total, keyin DLQ + admin alert. Past complexity, aniq SLA.

## Alternatives Considered

### Alternative 1: Sync REST (Univer poll) — eski yondashuv
- Univer har 5 min `GET /updates?since=T` chaqiradi
- **Rad etish sababi:** 5-15 min latency, markazga 224×N pull/min yuk, real-time emas

### Alternative 2: WebSocket / SSE persistent connection
- 224 persistent connection markaz tomonda
- **Rad etish sababi:** Yii2'da WebSocket server zaif (Ratchet/Swoole — Yii2 standard emas), 224 connection load balancer murakkabligi

### Alternative 3: Kafka direct (Univer producer/consumer)
- Univer `php-rdkafka` ekstension orqali bevosita Kafka
- **Rad etish sababi:** PHP `rdkafka` C extension shared hosting'da install qiyin, davlat tarmoq policy TCP 9092 yopiq

### Alternative 4: gRPC streaming
- Bidirectional streaming
- **Rad etish sababi:** PHP gRPC ekstension noaniq, JSON over HTTP yetadi (99% use case)

## Consequences

### Positive
- **Real-time:** 2-5 sekund latency markaz event → Univer
- **Univer kod minimal o'zgarish:** Bitta controller (Yii2 monorepo'da bir marta yoziladi, 224 OTM update oladi)
- **Decoupling:** 1 sekin OTM qolganlarga ta'sir qilmaydi (Kafka per-OTM partition)
- **Replay:** 30 kun event saqlash, bug fix → consumer group reset → qayta jo'natish
- **Per-OTM observability:** Prometheus per-university metric (Grafana dashboard)
- **DR ready:** Kafka Mirror Maker 2 + multi-region cluster (Stage 4)

### Negative
- **Operatsion murakkablik:** Kafka cluster boshqarish (Stage 3 K8s + Strimzi)
- **Eventual consistency:** 1-5s latency (sub-second emas)
- **In-memory vault cheklov:** Application restart'da plain secret yo'qoladi → admin manual regenerate (TODO: HashiCorp Vault integration)
- **WebhookRetryScheduler placeholder:** Hozirgi implementatsiya envelope payload retry uchun saqlamaydi — full retry Sprint 5+ da (admin endpoint orqali manual retry)

### Neutral
- Univer kodbase'iga yangi: `HemisCallbackController`, `hemis_callback_log` jadval, `yii2-queue` handler
- 224 OTM IT deploy: 30 daqiqa (`composer update` + `yii migrate` + `.env` HEMIS_WEBHOOK_SECRET)

## Implementation

| Sprint | Komponent | Status |
|--------|-----------|--------|
| 1.1 | V015 migration (webhook_target + webhook_delivery_log) | ✅ |
| 1.2 | OutboxEvent JPA entity | ✅ |
| 1.3 | WebhookTarget + WebhookDeliveryLog entity | ✅ |
| 1.4 | spring-kafka dependency + config | ✅ |
| 2   | OutboxEventPublisher + OutboxPoller + Kafka topics | ✅ |
| 3   | WebhookFanoutConsumer + Dispatcher + HMAC + Retry | ✅ |
| 4   | Admin API (CRUD + secret rotation + delivery log) | ✅ |
| 5   | Prometheus metrics + M004 permissions + Sandbox + ADR + Docs | ✅ |

## Verification

```bash
# 1. Build
./gradlew :app:build -x test

# 2. Liquibase status (V015 + M004 pending)
./gradlew :domain:liquibaseStatus

# 3. Local stack
docker-compose up -d  # Kafka + PostgreSQL + Redis
./gradlew :domain:liquibaseUpdate
SPRING_PROFILES_ACTIVE=dev,redis ./gradlew :app:bootRun

# 4. Smoke test
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/web/auth/login -d '{...}' | jq -r .data.token)

# Create webhook target (returns plain secret)
curl -X POST http://localhost:8081/api/v1/web/admin/webhooks \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"universityCode":"337","callbackUrl":"http://localhost:9999/test"}'

# Send test event
curl -X POST http://localhost:8081/api/v1/web/admin/webhooks/{id}/test \
     -H "Authorization: Bearer $TOKEN"

# View delivery log
curl http://localhost:8081/api/v1/web/admin/webhooks/{id}/deliveries \
     -H "Authorization: Bearer $TOKEN"

# 5. Prometheus metrics
curl http://localhost:8081/actuator/prometheus | grep hemis_webhook
```

## References

- ADR-0007 — Sync architecture evolution (Kafka-first)
- ADR-0010 — Employee sync outbox implementation (Outbox pattern)
- ADR-0011 — Swagger multi-group strategy
- [Chris Richardson — Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html)
- `docs/architecture/hemis-univer-integration-patterns.html`
