-- =====================================================
-- V012: LIFECYCLE MODULE — university_lifecycle
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-25
-- Purpose: University lifecycle events (closure, merger, split, etc.)
-- Pattern: IPEDS institution_history + HESA provider_events
-- Immutable event log — no version, no soft delete
-- =====================================================

CREATE TABLE university_lifecycle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code),

    -- Event classification
    event_type VARCHAR(30) NOT NULL CHECK (event_type IN (
        'CLOSED',
        'MERGED',
        'SPLIT',
        'LICENSE_REVOKED',
        'SUSPENDED',
        'REACTIVATED',
        'RENAMED',
        'REORGANIZED'
    )),

    -- When
    event_date DATE NOT NULL,

    -- Successor university (MERGED/SPLIT/REORGANIZED)
    successor_code VARCHAR(255) REFERENCES hemishe_e_university(code),

    -- Government decree
    decree_number VARCHAR(100),
    decree_date DATE,

    -- Snapshot at time of event
    students_count INTEGER,
    employees_count INTEGER,

    -- Old/new name (RENAMED)
    old_name VARCHAR(1024),
    new_name VARCHAR(1024),

    -- Notes
    note TEXT,

    -- Audit (immutable — only created_at/by, no update/delete)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),

    -- Data integrity: successor_code required for MERGED/SPLIT/REORGANIZED
    CONSTRAINT chk_lifecycle_successor CHECK (
        event_type NOT IN ('MERGED', 'SPLIT', 'REORGANIZED')
        OR successor_code IS NOT NULL
    ),
    -- RENAMED requires old_name and new_name
    CONSTRAINT chk_lifecycle_rename CHECK (
        event_type != 'RENAMED'
        OR (old_name IS NOT NULL AND new_name IS NOT NULL)
    ),
    -- Event date cannot be in future
    CONSTRAINT chk_lifecycle_event_date CHECK (event_date <= CURRENT_DATE),
    -- Decree date must precede or equal event date
    CONSTRAINT chk_lifecycle_decree_date CHECK (decree_date IS NULL OR decree_date <= event_date),
    -- Counts non-negative
    CONSTRAINT chk_lifecycle_students_count CHECK (students_count IS NULL OR students_count >= 0),
    CONSTRAINT chk_lifecycle_employees_count CHECK (employees_count IS NULL OR employees_count >= 0)
);

COMMENT ON TABLE university_lifecycle IS 'University lifecycle events — full chronological history';
COMMENT ON COLUMN university_lifecycle.successor_code IS 'Successor university. MERGED: target. SPLIT: each new university gets a row.';
COMMENT ON COLUMN university_lifecycle.students_count IS 'Student count at time of event (historical snapshot)';

CREATE INDEX idx_lifecycle_university ON university_lifecycle(university_code);
CREATE INDEX idx_lifecycle_successor ON university_lifecycle(successor_code) WHERE successor_code IS NOT NULL;
CREATE INDEX idx_lifecycle_event_type ON university_lifecycle(event_type);
CREATE INDEX idx_lifecycle_date ON university_lifecycle(event_date DESC);
