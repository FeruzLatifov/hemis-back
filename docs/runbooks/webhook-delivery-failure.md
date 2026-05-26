# Webhook Delivery Failure — Runbook

> **Maqsad:** Markaz → Univer (224 OTM) webhook yetkazib berishida bug topib tuzatish.
>
> **Komponentlar:** `OutboxPoller` → Kafka → `WebhookDispatcher` → REST POST (HMAC SHA-256) → Univer `HemisCallbackController` → `ApplyHemisEventJob` (Yii2 queue worker).
>
> **Ma'lumotnoma:** [ADR-0012](../adr/0012-webhook-outbound-infrastructure.md), [Implementation guide](../integration/webhook-implementation-guide.md).

---

## 0. Tez tashxis (60 sek)

Eng kerakli 3 ta so'rov:

```sql
-- 1. Markaz: yetkazib berilmagan event'lar
SELECT status, COUNT(*) FROM webhook_delivery_log
WHERE created_at > now() - interval '1 hour'
GROUP BY status;

-- 2. Markaz: DLQ ga ko'chgan
SELECT id, university_code, event_id, attempt_count, last_error, last_attempt_at
FROM webhook_delivery_log
WHERE status='dlq' OR attempt_count >= 3
ORDER BY id DESC LIMIT 20;

-- 3. Outbox queue depth (publish kutayotgan)
SELECT COUNT(*) FROM outbox_event WHERE published_at IS NULL;
```

Sentry'da `hemis_webhook_dispatch_total{status="failed"}` counter — burst signal.

---

## 1. Symptom: 5xx retry loop (`status='queued'` 3 attempt'dan keyin)

**Belgi:** `webhook_delivery_log.attempt_count >= 3`, `last_error` 5xx yoki timeout.

**Sabab variantlari:**
- Univer DB connection tushgan (`DB_DSN` env, lokal Postgres)
- Classifier jadval Univer DB'da yo'q (`h_position`, `h_gender`, …) — schema drift
- `ApplyHemisEventJob` prefiks logikasi xato

**Diagnoz:**

```bash
# Univer-tomon:
ssh univer-host
cd /var/www/univer
psql -d hemis_NNN -c "SELECT * FROM hemis_callback_log
                     WHERE status='failed'
                     ORDER BY received_at DESC LIMIT 10;"

# Schema mosligini tekshir
psql -d hemis_NNN -c "SELECT table_name FROM information_schema.tables
                      WHERE table_name LIKE 'h_%'
                      ORDER BY table_name;"
```

**Fix:**

| Topilma | Yechim |
|---------|--------|
| `hemis_callback_log` ichida `error_message` "relation does not exist" | Univer migration apply: `php yii migrate` (markazning `classifier_type` apiKey `h_*` yo'q jadvalga ishora qiladi → Univer schema yangilanmagan) |
| `ApplyHemisEventJob.php:103` `$table = 'hemishe_h_' . $type` (eski xatolik) | Yangi versiyaga upgrade — actual kod: `strpos($type, 'h_') === 0 ? $type : 'h_' . $type` |
| Univer DB connection lost | `systemctl status postgresql` + `journalctl -u postgresql -n 50` |

---

## 2. Symptom: 403 Forbidden (HMAC fail)

**Belgi:** Markaz `webhook_delivery_log.last_error: HTTP 403 Forbidden`. Univer log: `Invalid signature` yoki `Timestamp out of window`.

**Sabab variantlari:**
- `HEMIS_WEBHOOK_SECRET` Univer `.env` da yo'q yoki noto'g'ri (markaz `whsec_xxx` ↔ Univer kopiyasi farq qiladi)
- Markaz va Univer o'rtasida vaqt drift (NTP buzilgan, replay window 300s'dan ortiq)
- HMAC payload formatda farq (markaz `timestamp + "." + body` ⟷ Univer ham shu)

**Diagnoz:**

```bash
# Markaz tomondan delivery log'ga qara
psql -d hemis -c "SELECT request_payload, response_body
                  FROM webhook_delivery_log
                  WHERE id = <ID>;"

# Univer tomondan offline HMAC verify (timestamp + body bilan)
php -r "echo hash_hmac('sha256', '<TIMESTAMP>.<BODY>', '<SECRET>');"
# Markaz tomondan kelgan signature bilan compare

# NTP tekshir (markaz + Univer)
timedatectl status
chronyc tracking  # yoki: ntpq -p
```

**Fix:**

| Topilma | Yechim |
|---------|--------|
| Secret farqi | Markazda `POST /api/v1/web/admin/webhooks/{id}/regenerate-secret` → yangi `whsec_xxx` ni Univer `.env`'ga ko'chir (`HEMIS_WEBHOOK_SECRET=...`) + queue worker restart |
| Timestamp drift > 300s | NTP sync (`timedatectl set-ntp true`). Replay window xavf bilan kengaytirilmaydi — vaqtni tuzating |
| Payload format farq | Markaz `WebhookDispatcher.java:281-299` ⟷ Univer `HemisCallbackController.php verifyHemisSignature()` — `timestamp + "." + body` bo'lishi shart |

---

## 3. Symptom: DLQ to'ldi (`hemis.webhook.dlq` Kafka topic)

**Belgi:** Sentry alert yoki Grafana `kafka_consumer_lag{topic="hemis.webhook.dlq"}` > 100.

**Sabab variantlari:**
- Bitta OTM (masalan 337) doimiy fail bermoqda (host offline, TLS srt expired, …)
- Markaz event flood (`outbox_event` to'satdan o'sdi — classifier mass-update)

**Diagnoz:**

```bash
# DLQ ichini ko'r
~/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic hemis.webhook.dlq \
    --from-beginning --max-messages 20

# Per-OTM failure breakdown
psql -d hemis -c "SELECT university_code, COUNT(*), MAX(last_attempt_at)
                  FROM webhook_delivery_log
                  WHERE status IN ('failed','dlq')
                    AND last_attempt_at > now() - interval '1 hour'
                  GROUP BY university_code
                  ORDER BY 2 DESC LIMIT 10;"
```

**Fix:**

| Topilma | Yechim |
|---------|--------|
| Bitta OTM dominant (>80% fail) | Shu OTM uchun `WebhookTarget` ni vaqtinchalik disable: `PATCH /api/v1/web/admin/webhooks/{id}` (`active: false`). OTM admin bilan aloqa: HTTPS endpoint sog'liqmi |
| Hammasi (broad fail) | Markaz tomonidagi `WebhookDispatcher` config tekshir (timeout, TLS truststore) |
| TLS cert expired | OTM tomondan yangilash. `openssl s_client -connect <otm>:443 -servername <host>` |
| DLQ manual replay | `POST /api/v1/web/admin/webhooks/dlq/{eventId}/replay` (yoki SQL: `UPDATE webhook_delivery_log SET status='pending', attempt_count=0, next_retry_at=now() WHERE id=<ID>`) |

---

## 4. Symptom: Univer queue worker offline (queue grow)

**Belgi:** Univer'da `hemis_callback_log.status='queued'` event'lar to'planmoqda (worker'ga yetib bormaydi).

**Diagnoz:**

```bash
# Univer tomondan worker status
systemctl status univer-queue-worker

# Yii2 queue info
cd /var/www/univer
php yii queue/info

# Redis queue length (agar Redis queue ishlatilsa)
redis-cli LLEN queue-<channel-md5>
```

**Fix:**

```bash
systemctl restart univer-queue-worker
# Yoki manual:
php yii queue/listen --verbose
```

Kelajak uchun: systemd unit + restart=always + journal monitoring.

---

## 5. Symptom: `rule.push` received but no-op

**Bu xato emas.** `ApplyHemisEventJob:149-157` da `rule.push` event handler **DEFERRED** — Univer'da `system_rule` schema dizayni hozircha yo'q ([ADR-0012 alternative tanlovlar](../adr/0012-webhook-outbound-infrastructure.md)).

**Yechim variantlari:**
- Markazdan `rule.push` event yuborishni to'xtatish (rules engine implement bo'lmaguncha)
- Yoki Univer'da `system_rule` jadval qo'shib, handler ishchi qilish

---

## Lokal test stack

Manual e2e tekshirish:

```bash
# 1. Markaz dev
cd /home/adm1n/projects/startup/hemis-back
./gradlew :app:bootRun  # port 8081

# 2. Lokal Univer queue worker
cd /home/adm1n/projects/startup/hemis-univer
php yii queue/listen --verbose

# 3. Sandbox event yuborish (markaz tomondan)
curl -X POST http://localhost:8081/api/v1/web/admin/webhooks/{id}/test \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json"

# 4. Univer-side tekshiruv
psql -d hemis_337 -c "SELECT * FROM hemis_callback_log ORDER BY received_at DESC LIMIT 5;"
tail -f /home/adm1n/projects/startup/hemis-univer/console/runtime/logs/app.log
```

---

## Eskirgan dokumentatsiya (sinxron qilingan)

- `docs/integration/webhook-implementation-guide.md` sec 3.3 (`hemishe_h_*` → `h_*`) — 2026-05-19 fix
- Actual prefiks logikasi: `univer/common/components/hemis/jobs/ApplyHemisEventJob.php:98-103`
- HMAC algoritm: hemis-back `HmacSigner.java:54` ⟷ Univer `HemisCallbackController.php verifyHemisSignature()`

---

## Ma'lumotnoma fayllar

- **Markaz:** `service/src/main/java/uz/hemis/service/webhook/` (10 fayl)
- **Univer:** `api/controllers/v1/HemisCallbackController.php`, `common/components/hemis/jobs/ApplyHemisEventJob.php`
- **Migration:** `domain/.../V015_create_webhook_infrastructure.sql` (markaz), `console/migrations/m260513_120000_create_hemis_callback_log.php` (Univer)
- **Metrics:** `WebhookMetrics` (Micrometer counter+histogram)
