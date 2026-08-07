-- =====================================================
-- V019: SPECIALITY → OTM ATTACHMENT (h_speciality_attachment)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-07-17
-- Purpose: Attach a unified-classifier speciality (h_speciality) to a specific
--          OTM (university). Ministry-kurirovka / OTM assignment surface:
--          "which speciality is this university allowed to run". Permission +
--          role driven, tenant-scope fail-closed (enforced in the service layer).
-- Pattern: AuditableEntity (modern audit WITH soft delete via deleted_at) —
--          an attachment is a revocable business record (unlike the classifier
--          rows themselves), so a soft-delete trail is wanted here.
-- Keying: university_code is a by-value reference to hemishe_e_university.code
--          (VARCHAR, the 224-OTM identifier), NOT a UUID FK. speciality_id is a
--          UUID FK into the new h_speciality table.
-- =====================================================

CREATE TABLE IF NOT EXISTS h_speciality_attachment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code VARCHAR(255) NOT NULL,          -- by-value ref to hemishe_e_university.code (OTM id)
    speciality_id   UUID NOT NULL,
    education_form  VARCHAR(32),
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',

    -- Audit (AuditableEntity — modern naming WITH soft delete)
    version         INTEGER NOT NULL DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50),
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(50),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(50),

    CONSTRAINT fk_h_spec_attach_spec FOREIGN KEY (speciality_id)
        REFERENCES h_speciality(id) ON DELETE RESTRICT
);

COMMENT ON TABLE h_speciality_attachment IS
    'Attach a unified-classifier speciality (h_speciality) to an OTM (university_code). Permission/role driven, tenant-scope fail-closed in the service layer.';

-- One live attachment per (OTM, speciality, education_form); soft-deleted rows excluded
CREATE UNIQUE INDEX IF NOT EXISTS uq_h_spec_attach
    ON h_speciality_attachment(university_code, speciality_id, education_form) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_h_spec_attach_univ ON h_speciality_attachment(university_code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_h_spec_attach_spec ON h_speciality_attachment(speciality_id)   WHERE deleted_at IS NULL;
