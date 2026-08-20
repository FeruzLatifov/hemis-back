-- =====================================================
-- Rollback M011: restore the soft-delete columns + the V019 partial indexes
-- =====================================================
-- ⚠️ DATA: the purged ROWS do not come back. M011 step 2 permanently deleted every
--    deleted_at IS NOT NULL attachment — a one-way cleanup, by design (an attachment
--    carries no dependants and is re-created on demand). This script restores the
--    STRUCTURE only: deleted_at/deleted_by come back empty, so every surviving row
--    reads as live — which is exactly what it is.
-- Order: columns first, indexes after — the partial predicate needs deleted_at to exist.
-- Idempotent: ADD COLUMN IF NOT EXISTS + DROP INDEX IF EXISTS / CREATE ... IF NOT EXISTS.
-- =====================================================

ALTER TABLE university_speciality_attachment ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE university_speciality_attachment ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(50);

-- Back to the V019 shape: soft-deleted rows excluded from all four indexes (names unchanged).
DROP INDEX IF EXISTS uq_univ_spec_attach;
CREATE UNIQUE INDEX IF NOT EXISTS uq_univ_spec_attach
    ON university_speciality_attachment(university_code, speciality_id, education_form, edu_year) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS idx_univ_spec_attach_univ;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_univ ON university_speciality_attachment(university_code) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS idx_univ_spec_attach_spec;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_spec ON university_speciality_attachment(speciality_id)   WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS idx_univ_spec_attach_year;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_year ON university_speciality_attachment(edu_year)         WHERE deleted_at IS NULL;

-- V019 wording (soft delete back in the contract).
COMMENT ON TABLE university_speciality_attachment IS
    'Attach a unified-classifier speciality (h_speciality) to an OTM (university_code). Permission/role driven, tenant-scope fail-closed in the service layer.';
