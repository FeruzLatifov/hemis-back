-- =====================================================
-- V009: LIFECYCLE MODULE — university_lifecycle
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-25
-- Purpose: University lifecycle events (closure, merger, split, etc.)
-- Pattern: IPEDS institution_history + HESA provider_events
-- Immutable event log — no version, no soft delete
-- =====================================================

CREATE TABLE IF NOT EXISTS university_lifecycle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- ON DELETE RESTRICT: tarixiy lifecycle log'ni accidental delete'dan himoya qilish
    -- (Universitet o'chirilishi uchun avval lifecycle row'larni qo'lda o'chirish kerak — qaror qabul etilgan)
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code) ON DELETE RESTRICT,

    -- Event classification — frontend triggers via STATUS_EVENT_MAP only.
    -- SPLIT/RENAMED removed: no UI path creates them; reintroduce via migration
    -- if a feature requires them in future.
    event_type VARCHAR(30) NOT NULL CHECK (event_type IN (
        'CLOSED',
        'MERGED',
        'LICENSE_REVOKED',
        'SUSPENDED',
        'REACTIVATED',
        'REORGANIZED'
    )),

    -- When
    event_date DATE NOT NULL,

    -- Successor university (MERGED/REORGANIZED)
    -- ON DELETE SET NULL: successor o'chirilsa lifecycle row qoladi, successor_code NULL
    successor_code VARCHAR(255) REFERENCES hemishe_e_university(code) ON DELETE SET NULL,

    -- Government decree
    decree_number VARCHAR(100),
    decree_date DATE,

    -- Notes (free-form description; covers ad-hoc rename/snapshot context)
    note TEXT,

    -- Audit (immutable — only created_at/by, no update/delete)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),

    -- Data integrity: successor_code required for MERGED/REORGANIZED
    CONSTRAINT chk_lifecycle_successor CHECK (
        event_type NOT IN ('MERGED', 'REORGANIZED')
        OR successor_code IS NOT NULL
    ),
    -- Event date cannot be in future
    CONSTRAINT chk_lifecycle_event_date CHECK (event_date <= CURRENT_DATE),
    -- Decree date must precede or equal event date
    CONSTRAINT chk_lifecycle_decree_date CHECK (decree_date IS NULL OR decree_date <= event_date)
);

COMMENT ON TABLE university_lifecycle IS 'University lifecycle events — admin-created via status-change auto-trigger. Immutable log (no update/soft-delete).';
COMMENT ON COLUMN university_lifecycle.successor_code IS 'Successor university (MERGED/REORGANIZED). NULL for non-successor events.';

CREATE INDEX IF NOT EXISTS idx_lifecycle_university ON university_lifecycle(university_code);
CREATE INDEX IF NOT EXISTS idx_lifecycle_successor  ON university_lifecycle(successor_code) WHERE successor_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_lifecycle_event_type ON university_lifecycle(event_type);
CREATE INDEX IF NOT EXISTS idx_lifecycle_date       ON university_lifecycle(event_date DESC);
