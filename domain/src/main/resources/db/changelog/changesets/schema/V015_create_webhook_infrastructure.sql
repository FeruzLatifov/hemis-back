-- =====================================================
-- V015: WEBHOOK INFRASTRUCTURE (Outbound markaz → Univer)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-13
-- ADR: docs/adr/0012-webhook-outbound-infrastructure.md (proposed)
-- Purpose: 224 ta Univer (hemis_337, hemis_401, ...) ga markaz tomondan
--          event push qilish infrastrukturasi. Outbox (V014 `outbox_event`)
--          → Kafka topic → Consumer → REST callback (HMAC-signed) → Univer.
--
-- Birga ishlash:
--   - V014 `outbox_event` — universal event source (employee, classifier,
--     rule_push, otm_block, notification — har biri 1 row)
--   - V015 `webhook_target` — 224 OTM ro'yxati (URL, secret, retry config)
--   - V015 `webhook_delivery_log` — har attempt audit (event × target × attempt)
--
-- Self-contained: faqat 2 yangi jadval (webhook_target, webhook_delivery_log).
-- Mavjud jadvallarga ALTER yo'q.
-- =====================================================

-- =====================================================
-- 1. webhook_target — 224 OTM Univer webhook ro'yxati
-- =====================================================
-- Har OTM uchun callback URL + HMAC secret. Admin UI orqali boshqariladi.
-- Univer offline bo'lsa active=FALSE qo'yiladi → consumer skip qiladi.
-- URL convention (2026-05-19): callback_url + active per-row saqlanmaydi.
--   - URL = ${protocol}://{hemishe_e_university.student_url}${suffix}
--     (application.yml: hemis.webhook.callback.protocol + .suffix)
--   - active = hemishe_e_university.active (university lifecycle bilan sync)
-- WebhookDispatcher buildCallbackUrl() + WebhookTargetRepository
-- findAllForActiveUniversities() JPQL JOIN orqali derive qiladi.
CREATE TABLE IF NOT EXISTS webhook_target (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code    VARCHAR(10)  NOT NULL,

    -- HMAC secret: bcrypt hash. DEPRECATED (K1, 2026-05-26) — markaz imzo qo'yadi,
    -- verify qilmaydi, shuning uchun bcrypt hash keraksiz. secret_enc source of truth.
    -- Hozircha saqlanadi (kelajakda drop). nullable — yangi target'lar to'ldirmaydi.
    secret_hash        VARCHAR(255),

    -- AES-256-GCM shifrlangan plain secret (K1). IMZO QO'YISH MANBAI: markaz har outbound
    -- webhook'ni shu bilan imzolaydi. WebhookSecretVault restart'da DB'dan lazy decrypt qiladi
    -- (oldin faqat in-memory edi → restart 224 OTM imzosini sindirardi).
    -- Kalit: HEMIS_WEBHOOK_SECRET_ENCRYPTION_KEY (.env/KMS). Yo'qolsa → secret rotation kerak.
    secret_enc         VARCHAR(512),

    description        VARCHAR(255),

    -- Per-target retry config (default qiymatlar — application.yml override)
    -- max_retries DEFAULT 3: entity (WebhookTarget.java:61) + service create (109)
    -- + application.yml hemis.webhook.retry.max-attempts bilan bir xil (ADR-0012 trim 2026-05-18, 5→3).
    timeout_ms         INT          NOT NULL DEFAULT 30000,
    max_retries        INT          NOT NULL DEFAULT 3,

    -- AuditableEntity columns (modern naming — yangi schema)
    version            INT          NOT NULL DEFAULT 1,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(50),
    updated_at         TIMESTAMP,
    updated_by         VARCHAR(50),
    deleted_at         TIMESTAMP,
    deleted_by         VARCHAR(50),

    -- Constraints
    CONSTRAINT chk_webhook_timeout
        CHECK (timeout_ms BETWEEN 1000 AND 60000),
    CONSTRAINT chk_webhook_retries
        CHECK (max_retries BETWEEN 0 AND 10)
);

COMMENT ON TABLE webhook_target IS
    '224 ta OTM Univer webhook ro''yxati. URL/active hemishe_e_university dan
     derive qilinadi (URL convention 2026-05-19). ADR-0012.';

COMMENT ON COLUMN webhook_target.university_code IS
    'OTM identifikator (hemis_NNN dagi NNN qism). Per-OTM UNIQUE (partial index).';

COMMENT ON COLUMN webhook_target.secret_hash IS
    'HMAC secret bcrypt hash. Plain secret faqat generate paytida qaytariladi
     (whsec_xxx format) — keyin OTM o''zining .env''ga yozadi. Markazda
     hash saqlanadi, signature verify qilinmaydi (Univer''da verify qilinadi).';

COMMENT ON COLUMN webhook_target.timeout_ms IS
    'HTTP request timeout (ms). Univer 30 sekunddan ortiq kutsa connection close.';

COMMENT ON COLUMN webhook_target.max_retries IS
    'Maksimal yetkazish urinishi (attempt count). DEFAULT 3 — application.yml
     hemis.webhook.retry.max-attempts bilan bir xil. attempt_n >= max_retries → DLQ
     (WebhookDispatcher.java:276). Per-OTM override CHECK(0..10). Bekor qilinsa DLQ.';

-- Per-OTM UNIQUE (soft-deleted target conflict yaratmaydi — partial index)
CREATE UNIQUE INDEX IF NOT EXISTS uq_webhook_target_university_code
    ON webhook_target (university_code)
    WHERE deleted_at IS NULL;

-- 2026-05-19 inline fix (dropped M006): idx_webhook_target_active olib tashlandi —
-- active column hemishe_e_university'dan derive qilinadi (per-row yo'q).
-- Consumer query JOIN orqali ishlatadi (university.active filter).

-- =====================================================
-- 2. webhook_delivery_log — har attempt audit
-- =====================================================
-- Bir event × N attempt = N row. Replay/troubleshooting uchun.
-- Status state machine:
--   pending → retry → success | failed | dlq
--
-- pending  — consumer yuborishni boshladi
-- success  — Univer 2xx qaytardi
-- failed   — Univer 5xx yoki 4xx (retry'siz)
-- retry    — timeout/network — exponential backoff bilan keyingi attempt
-- dlq      — max_retries tugadi → manual admin tekshiruvi
CREATE TABLE IF NOT EXISTS webhook_delivery_log (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),

    -- outbox_event.id (soft FK — outbox_event 30 kun keyin retention'da o'chishi mumkin,
    -- log esa audit uchun saqlanadi)
    event_id        UUID         NOT NULL,
    event_type      VARCHAR(64)  NOT NULL,  -- denormalized for fast filtering

    -- Target (real FK)
    target_id       UUID         NOT NULL REFERENCES webhook_target(id) ON DELETE RESTRICT,
    university_code VARCHAR(10)  NOT NULL,  -- denormalized

    attempt_n       INT          NOT NULL DEFAULT 1,

    -- Response
    http_status     INT,         -- NULL = network error (timeout, connect refused)
    response_body   TEXT,        -- truncated to 4KB max (application layer)
    error_message   TEXT,        -- exception text (qisqa sabab — to'liq stack Sentry'da)
    sentry_event_id VARCHAR(36), -- Sentry event cross-link (stack/context Sentry'da, DB faqat ID saqlaydi)
    duration_ms     INT,

    status          VARCHAR(20)  NOT NULL,

    -- Timing
    dispatched_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP,
    next_retry_at   TIMESTAMP,   -- exponential backoff hisoblanadi: 1s, 5s, 30s, 5min, 1h

    CONSTRAINT chk_webhook_delivery_status
        CHECK (status IN ('pending', 'success', 'failed', 'retry', 'dlq')),
    CONSTRAINT chk_webhook_delivery_attempt
        CHECK (attempt_n BETWEEN 1 AND 10),
    CONSTRAINT chk_webhook_delivery_http_status
        CHECK (http_status IS NULL OR http_status BETWEEN 100 AND 599)
);

COMMENT ON TABLE webhook_delivery_log IS
    'Har webhook yuborishning audit log. (event_id, target_id, attempt_n) unique.
     Replay, troubleshooting, SLA hisoboti uchun.';

COMMENT ON COLUMN webhook_delivery_log.event_id IS
    'outbox_event.id reference (soft FK — outbox retention 30 kun).';

COMMENT ON COLUMN webhook_delivery_log.event_type IS
    'Denormalized event_type (classifier.updated, rule.push, ...). Filter performance uchun.';

COMMENT ON COLUMN webhook_delivery_log.response_body IS
    'Univer response body (max 4KB application layer). Truncated indicator: ko''proq bo''lsa "...[truncated]" appended.';

COMMENT ON COLUMN webhook_delivery_log.next_retry_at IS
    'Keyingi retry vaqti. Resilience4j exponential backoff: 1s, 5s, 30s, 5min, 1h.';

-- Unique per attempt — bir event × target × attempt = bitta row
CREATE UNIQUE INDEX IF NOT EXISTS uq_webhook_delivery_event_target_attempt
    ON webhook_delivery_log (event_id, target_id, attempt_n);

-- Event uchun barcha attempt'lar (audit view)
CREATE INDEX IF NOT EXISTS idx_webhook_delivery_event
    ON webhook_delivery_log (event_id, attempt_n);

-- Target uchun status filter (admin UI: "qaysi OTM oxirgi 24 soatda fail bo'lgan?")
CREATE INDEX IF NOT EXISTS idx_webhook_delivery_target_status
    ON webhook_delivery_log (target_id, status, dispatched_at DESC);

-- Retry queue scan — har sekund poller bu indexni o'qiydi
CREATE INDEX IF NOT EXISTS idx_webhook_delivery_retry
    ON webhook_delivery_log (next_retry_at)
    WHERE status = 'retry';

-- DLQ — admin manual tekshiruvi uchun
CREATE INDEX IF NOT EXISTS idx_webhook_delivery_dlq
    ON webhook_delivery_log (dispatched_at DESC)
    WHERE status = 'dlq';

-- University filter (per-OTM hisobot)
CREATE INDEX IF NOT EXISTS idx_webhook_delivery_university
    ON webhook_delivery_log (university_code, dispatched_at DESC);
