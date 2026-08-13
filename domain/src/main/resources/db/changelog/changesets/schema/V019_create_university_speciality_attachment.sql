-- =====================================================
-- V019: SPECIALITY → OTM ATTACHMENT (university_speciality_attachment)
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
-- Keying: university_code REFERENCES hemishe_e_university.code (the 224-OTM VARCHAR
--          identifier) — a real FK (ON DELETE RESTRICT), matching the users/employee/
--          building pattern, so a code can never be orphaned. speciality_id is a UUID FK
--          into the new h_speciality table; edu_year FKs into h_education_year.
-- =====================================================

CREATE TABLE IF NOT EXISTS university_speciality_attachment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code VARCHAR(255) NOT NULL,          -- FK -> hemishe_e_university.code (OTM id)
    speciality_id   UUID NOT NULL,
    -- Education form is mandatory: every ministry assignment is for a specific form (Kunduzgi=11,
    -- Kechki=12, Masofaviy=16). NOT NULL closes a null-form duplicate race (two blank-form inserts
    -- would otherwise both slip the partial unique index, since NULL <> NULL). CHECK (not FK) keeps
    -- this self-contained — it does not depend on the frozen hemishe_h_education_form dump carrying
    -- code 16 — while still rejecting a typo'd form code. Mirrors h_speciality.education_type's CHECK.
    education_form  VARCHAR(32) NOT NULL
        CONSTRAINT chk_univ_spec_attach_form CHECK (education_form IN ('11', '12', '16')),
    edu_year        INTEGER NOT NULL,               -- academic year of THIS assignment (2026 = 2026-2027);
                                                    -- FK -> h_education_year(year), the SAME modern year classifier
                                                    -- h_speciality_year uses (NOT a separate year). Distinct in meaning
                                                    -- from the speciality's own validity years (h_speciality_year),
                                                    -- but keyed to the one shared h_education_year value set.
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'
        CONSTRAINT chk_univ_spec_attach_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'REVOKED')),

    -- Audit (AuditableEntity — modern naming WITH soft delete)
    version         INTEGER NOT NULL DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50),
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(50),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(50),

    CONSTRAINT fk_univ_spec_attach_spec FOREIGN KEY (speciality_id)
        REFERENCES h_speciality(id) ON DELETE RESTRICT,
    -- ON DELETE RESTRICT (no ON UPDATE CASCADE) — matches the 6 sibling FKs to hemishe_e_university(code)
    -- (V004/V005/V006/V008/V009/V010/V016). A university_code rename is an out-of-band, manually
    -- coordinated operation, not a per-table cascade (a lone cascade here would be a dead guarantee
    -- since the other referencing tables default to NO ACTION and would reject the rename first).
    CONSTRAINT fk_univ_spec_attach_univ FOREIGN KEY (university_code)
        REFERENCES hemishe_e_university(code) ON DELETE RESTRICT,
    CONSTRAINT fk_univ_spec_attach_year FOREIGN KEY (edu_year)
        REFERENCES h_education_year(year) ON DELETE RESTRICT
);

COMMENT ON TABLE university_speciality_attachment IS
    'Attach a unified-classifier speciality (h_speciality) to an OTM (university_code). Permission/role driven, tenant-scope fail-closed in the service layer.';

-- One live attachment per (OTM, speciality, education_form, edu_year); soft-deleted rows excluded
CREATE UNIQUE INDEX IF NOT EXISTS uq_univ_spec_attach
    ON university_speciality_attachment(university_code, speciality_id, education_form, edu_year) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_univ ON university_speciality_attachment(university_code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_spec ON university_speciality_attachment(speciality_id)   WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_year ON university_speciality_attachment(edu_year)         WHERE deleted_at IS NULL;
