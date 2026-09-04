---
name: webhook-target-add
description: Yangi OTM webhook target (markaz → Univer outbound callback) qo'shish — secret generatsiya/rotatsiya, HMAC, max_retries, apply-status. Trigger - "webhook target qo'sh", "OTM ga webhook", "webhook secret rotate", "yangi OTM callback", "webhook endpoint ulash".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Add Webhook Target (markaz → OTM outbound)

> ADR-0012 (webhook outbound) + K1 secret persistence + K2 apply-status feedback. Bu skill **yangi OTM uchun webhook target ro'yxatga olish** pattern'i — kod yozish EMAS (subsystem tayyor), balki admin API / seed orqali target qo'shish va invariantlarni saqlash.

## Subsystem (tayyor — qaytadan yozmang)

- Jadval: `webhook_target` (V015), `webhook_delivery_log`, `webhook_apply_result` (V015 — K2).
- Service: `service/webhook/` — `WebhookTargetService`, `WebhookSecretService`/`WebhookSecretCipher`/`WebhookSecretVault`, `WebhookDispatcher`, `WebhookRetryScheduler`, `WebhookFanoutConsumer`, `WebhookAckService`, `HmacSigner`.
- Admin API: `WebhookTargetController` (api-web, `/api/v1/web/admin/webhooks/**`).
- Ack: `WebhookAckController` (api-university, `/api/v1/university/hemis-events/ack`).
- Univer (PHP) tomoni: `docs/integration/webhook-implementation-guide.md` (receiver + ApplyHemisEventJob + ack).

## Workflow

### 1. Target yaratish (admin API — afzal yo'l)

`POST /api/v1/web/admin/webhooks` (permission `webhook.create` → S038'dan keyin faqat SUPER_ADMIN; ADMIN webhook'ni ko'radi, sirini o'zgartira olmaydi):

```jsonc
{
  "universityCode": "337",          // existsByCode bilan tekshiriladi (O3 — orphan target TAQIQ)
  "url": "https://otm337.edu.uz/hemis/webhook",
  "eventTypes": ["classifier.updated", "rule.pushed"],  // canonical lug'at (O4)
  "maxRetries": 3,                  // DEFAULT 3, CHECK 0-10 (O1)
  "active": true
}
```

Javob (`WebhookSecretResponse`) — **plaintext secret FAQAT shu yerda bir marta qaytadi**:
```jsonc
{ "id": "...", "universityCode": "337", "secret": "whk_<base64>", "secretVersion": 1 }
```

> Secret DB'da `webhook_target.secret_enc` (AES-256-GCM, `WebhookSecretCipher`) sifatida saqlanadi — **plaintext hech qachon DB/log'da emas** (K1). Univer bu secret'ni `ApplyHemisEventJob` HMAC verify uchun saqlaydi.

### 2. Secret rotatsiya

`POST /api/v1/web/admin/webhooks/{id}/regenerate-secret` → yangi `secret_enc` + `secretVersion++`; eski secret darhol bekor. Univer tomon yangi secret'ni olishi shart (downtime oldini olish uchun grace-window kerak bo'lsa — runbook).

### 3. Sandbox test (deploy oldidan)

`POST /api/v1/web/admin/webhooks/{id}/test` — sandbox event yuboradi, real fanout'siz. 2xx kutiladi.

### 4. Invariantlar (BUZILMASDAN)

- **HMAC:** har outbound POST `X-Hemis-Signature` (SHA-256, `HmacSigner`); Univer constant-time verify qiladi.
- **Idempotency:** Univer tomon `event_id` bo'yicha duplicate'ni rad etadi (at-least-once delivery).
- **Retry:** 5xx/timeout → `WebhookRetryScheduler` DB queue, exponential backoff, `maxRetries` dan keyin DLQ; 4xx → terminal FAILED.
- **K2 apply-status:** Univer apply tugagach `POST /hemis-events/ack` (HMAC, JWT EMAS) → `webhook_apply_result`. "delivered ≠ applied".
- **Frozen FK YO'Q:** `webhook_target` markaz frozen `hemishe_e_university`'ga hard FK qo'ymaydi — `existsByCode` app-layer validatsiya.

### 5. Verification

```bash
# Target ro'yxatda
psql -d $DB_MASTER_NAME -c "SELECT university_code, url, max_retries, secret_version, active FROM webhook_target WHERE university_code='337'"
# secret_enc plaintext EMAS (NOT NULL, shifrlangan)
psql -d $DB_MASTER_NAME -c "SELECT length(secret_enc), secret_hash IS NULL FROM webhook_target WHERE university_code='337'"
# Delivery + apply natija
psql -d $DB_MASTER_NAME -c "SELECT status, count(*) FROM webhook_delivery_log WHERE university_code='337' GROUP BY status"
```

### 6. Audit

- `webhook-outbox-reviewer` agent — HMAC/secret/DLQ/retention/apply-status invariantlari.
- Runbook: `docs/runbooks/webhook-delivery-failure.md` (5 failure mode: 5xx retry loop, 403 HMAC fail, DLQ grow, Univer worker offline, rule.push DEFERRED).

## See also

- ADR-0012 `docs/adr/0012-webhook-outbound-infrastructure.md`
- `docs/integration/webhook-implementation-guide.md` — Univer PHP Yii2 receiver
- Serena memory `mem:kafka_outbox` (pipeline) + `mem:recent_audit_p4` (K1/K2/Y1/O hardening)
- `kafka-outbox-topic` skill — yangi outbox topic (bu skilldan farqli: target ro'yxati emas, topic+consumer)
