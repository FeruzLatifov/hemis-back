-- =====================================================
-- V015: EMPLOYEE SYNC INFRASTRUCTURE
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-08
-- ADR: docs/adr/0010-employee-sync-outbox-implementation.md
-- Purpose: 224 ta Univer (hemis_337, hemis_401, ...) → markaziy HEMIS-back
--          xodim sync infrastructure. Transactional Outbox Pattern
--          (Chris Richardson) — sync REST + DB write + outbox row
--          bir transactionda → background OutboxPublisher Kafka'ga.
--
-- Self-contained: only own ALTERs (employee, employee_job) + 2 yangi jadval
-- (outbox_event, employee_sync_log). Cross-domen ADR-0007 Stage 1 ning
-- birinchi konkret implementatsiyasi — outbox_event keyinchalik student,
-- teacher, classifier domenlari uchun ham ishlatiladi.
--
-- Depends on: V004 (employee, employee_job)
-- =====================================================

-- =====================================================
-- 1. ALTER employee — sync metadata (last sync timestamp)
-- =====================================================
-- Employee global natural key = PINFL. 1 ta xodim ko'p OTM da ish qilishi
-- mumkin (multi-employer). Demak source_uid PER-EMPLOYEE mantiqiy emas —
-- bir xil PINFL ko'p Univer'dan kelishi mumkin (har Univer'ning o'z ichki ID).
-- Faqat synced_at — oxirgi sync timestamp. PINFL conflict last-write-wins.
--
-- source_uid emas (multi-source bo'lishi mumkin) — sync log per-row track qiladi.
ALTER TABLE employee
    ADD COLUMN IF NOT EXISTS synced_at TIMESTAMP;

COMMENT ON COLUMN employee.synced_at IS
    'Oxirgi muvaffaqiyatli Univer sync timestamp. NULL = markazda yaratilgan
     (admin UI), NOT NULL = Univer push qilgan. Audit trail employee_sync_log da.';

CREATE INDEX IF NOT EXISTS idx_employee_synced
    ON employee (synced_at)
    WHERE synced_at IS NOT NULL;

-- =====================================================
-- 2. ALTER employee_job — sync metadata (per-OTM source ID + change detection)
-- =====================================================
-- EmployeeJob PER-OTM — har OTM faqat o'zining position'larini push qiladi
-- (university_code bo'yicha tenant guard). Demak (university_code, source_uid)
-- juftligi UNIQUE — bu OTM-side e_employee_meta.id ga mos.
ALTER TABLE employee_job
    ADD COLUMN IF NOT EXISTS source_uid VARCHAR(100);

ALTER TABLE employee_job
    ADD COLUMN IF NOT EXISTS content_hash CHAR(64);

ALTER TABLE employee_job
    ADD COLUMN IF NOT EXISTS synced_at TIMESTAMP;

COMMENT ON COLUMN employee_job.source_uid IS
    'Univer-side e_employee_meta.id (per-OTM). NULL = markazda yaratilgan
     (admin UI), NOT NULL = OTM push qilgan.';
COMMENT ON COLUMN employee_job.content_hash IS
    'SHA-256 of sync-relevant fields. Idempotent change detection —
     incoming hash == stored hash → skip (no-op).';
COMMENT ON COLUMN employee_job.synced_at IS
    'Oxirgi muvaffaqiyatli sync timestamp.';

-- Per-OTM idempotent upsert key: (university_code, source_uid) UNIQUE
-- partial — soft-deleted yoki markazda yaratilgan (source_uid IS NULL) row'lar
-- conflict yaratmaydi.
CREATE UNIQUE INDEX IF NOT EXISTS uq_ejob_univer_source
    ON employee_job (university_code, source_uid)
    WHERE source_uid IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_ejob_synced
    ON employee_job (synced_at)
    WHERE synced_at IS NOT NULL;

-- =====================================================
-- 3. CREATE TABLE outbox_event — Transactional Outbox (Chris Richardson)
-- =====================================================
-- ADR-0007 Stage 1.2 birinchi implementatsiya. Atomic write with domain
-- entity (same @Transactional), async publish to Kafka via OutboxPublisher
-- (@Scheduled). DLQ/retry built-in.
--
-- Multi-domen jadval: aggregate_type discriminator orqali employee, student,
-- teacher, classifier domenlari ishlatadi. Topic naming:
--   hemis.{aggregate_type}.events.v{schema_version}
-- Misol: hemis.employee.events.v1, hemis.classifier.events.v1
CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Domain discriminator — Kafka topic routing
    aggregate_type VARCHAR(50) NOT NULL,        -- 'employee', 'employee_job', 'student', 'classifier', ...
    aggregate_id   VARCHAR(100) NOT NULL,       -- Entity primary key (UUID/string)
    event_type     VARCHAR(50) NOT NULL,        -- 'created', 'updated', 'deleted', 'synced'

    -- Payload — full event body (JSONB for flexibility)
    payload        JSONB NOT NULL,
    schema_version INT NOT NULL DEFAULT 1,      -- Apicurio schema versioning (Stage 2)

    -- Lifecycle
    occurred_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at   TIMESTAMP,                   -- NULL = not yet sent to Kafka

    -- Retry / DLQ
    retry_count    INT NOT NULL DEFAULT 0,
    last_error     TEXT,

    -- Tracing (correlation across services)
    correlation_id VARCHAR(100),                -- request-level (HTTP header X-Correlation-ID)
    causation_id   VARCHAR(100),                -- previous event id (event chain)

    -- Audit (created_by — who triggered the write)
    created_by     VARCHAR(50),

    CONSTRAINT chk_outbox_aggregate_type CHECK (aggregate_type IN (
        'employee', 'employee_job', 'student', 'teacher',
        'classifier', 'university', 'building', 'audit'
    )),
    CONSTRAINT chk_outbox_event_type CHECK (event_type IN (
        'created', 'updated', 'deleted', 'synced',
        'soft_deleted', 'restored', 'conflict_resolved'
    )),
    CONSTRAINT chk_outbox_retry CHECK (retry_count >= 0 AND retry_count <= 100)
);

COMMENT ON TABLE outbox_event IS
    'Transactional Outbox Pattern (Chris Richardson). Atomic write with
     domain entity, async publish to Kafka. ADR-0007 Stage 1 + ADR-0010.
     Multi-domain — aggregate_type discriminator routes to topic.';
COMMENT ON COLUMN outbox_event.aggregate_type IS
    'Domain discriminator. Kafka topic: hemis.{aggregate_type}.events.v{schema_version}';
COMMENT ON COLUMN outbox_event.payload IS
    'Full event payload (JSONB). Consumer-friendly — event body itself,
     emas DB join. Schema versioning via schema_version field.';
COMMENT ON COLUMN outbox_event.published_at IS
    'NULL = pending publish. NOT NULL = sent to Kafka (or DLQ on terminal fail).';
COMMENT ON COLUMN outbox_event.correlation_id IS
    'Request-level tracing ID (X-Correlation-ID header). Cross-service trace.';

-- Outbox publisher poll query: SELECT ... WHERE published_at IS NULL
-- ORDER BY occurred_at LIMIT 100. FOR UPDATE SKIP LOCKED — multi-instance safe.
CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON outbox_event (occurred_at)
    WHERE published_at IS NULL;

-- Per-aggregate audit/replay support
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate
    ON outbox_event (aggregate_type, aggregate_id, occurred_at DESC);

-- DLQ investigation
CREATE INDEX IF NOT EXISTS idx_outbox_failed
    ON outbox_event (retry_count, occurred_at)
    WHERE retry_count > 0 AND published_at IS NULL;

-- Tracing
CREATE INDEX IF NOT EXISTS idx_outbox_correlation
    ON outbox_event (correlation_id)
    WHERE correlation_id IS NOT NULL;

-- M007 (2026-05-19): employee_sync_log jadvali olib tashlandi.
-- Sabab: activity_log (hemis_audit DB) + Sentry bilan 80% duplikat edi.
-- EmployeeSyncProcessor @Audited annotation orqali activity_log'ga yoziladi.
-- ADR-0010 revision (2026-05-19).
