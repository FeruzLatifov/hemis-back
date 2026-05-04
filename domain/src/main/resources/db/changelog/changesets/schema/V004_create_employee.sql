-- =====================================================
-- V004: HR CORE — employee + employee_job
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-23
-- Purpose: Central person registry + employment records (jobs).
--   Pattern: Banner SPRIDEN, PeopleSoft PS_JOB, Oracle PER_ALL_ASSIGNMENTS_F.
--
-- Self-contained: only own tables, no cross-file ALTER. Auth (V006), university
-- domain (V005, V008) reference {employee, employee_job} via INLINE FK.
--
-- Depends on: V003 positions (position_type, position)
--             + legacy hemishe_h_* classifiers
--             + legacy hemishe_e_university.
-- =====================================================

-- =====================================================
-- TABLE 1: employee (one person = one record, PINFL unique)
-- =====================================================
CREATE TABLE employee (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pinfl                VARCHAR(14) NOT NULL,  -- Partial UNIQUE pastda (soft-delete uyg'unligi)
    first_name           VARCHAR(255) NOT NULL,
    last_name            VARCHAR(255) NOT NULL,
    middle_name          VARCHAR(255),
    birth_date           DATE,

    -- Person type discriminator — universal registry (UNIVERSITY + vazirlik + markaz + tashkilot)
    -- PINFL-based identity (MyGov/E-Imzo/OneID SSO uchun — context.md integrations)
    person_type          VARCHAR(30) NOT NULL DEFAULT 'UNIVERSITY_STAFF',

    -- Classifier FK'lari: eski hemishe_h_* jadvallar (single source of truth — rules.md v2.0)
    gender_code          VARCHAR(20) REFERENCES hemishe_h_gender(code),
    citizenship_code     VARCHAR(20) REFERENCES hemishe_h_citizenship(code),
    nationality_code     VARCHAR(20) REFERENCES hemishe_h_nationality(code),
    -- Passport: single column for consistency with legacy hemishe_e_employee
    -- and per-OTM university databases (hemis_337 style). Format: "AA1234567"
    -- (2 letters + 7 digits). SSO callbacks that deliver series/number separately
    -- are concatenated at the service layer before persistence.
    passport             VARCHAR(20),
    passport_date        DATE,
    phone                VARCHAR(50),
    email                VARCHAR(255),
    address              TEXT,

    -- Single hierarchical SOATO code: first 4 digits = region, 7 = district, 11 = neighborhood.
    soato_code           VARCHAR(20) REFERENCES hemishe_h_soato(code),
    academic_degree_code VARCHAR(20) REFERENCES hemishe_h_academic_degree(code),
    academic_rank_code   VARCHAR(20) REFERENCES hemishe_h_academic_rank(code),
    tin                  VARCHAR(20),

    -- Email validation
    CONSTRAINT chk_employee_email CHECK (
        email IS NULL OR email ~* '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$'
    ),

    -- Person type discriminator (UNIVERSITY_STAFF default, others for ministry/center/external)
    CONSTRAINT chk_employee_person_type CHECK (
        person_type IN ('UNIVERSITY_STAFF', 'MINISTRY_STAFF', 'CENTER_STAFF', 'OTHER_ORG_STAFF')
    ),

    -- Audit (AuditableEntity: 7 columns)
    version    INTEGER   DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

COMMENT ON TABLE  employee IS 'Universal person registry — OTM staff + ministry + centers + external orgs. PINFL unique per person. MyGov/E-Imzo SSO lookup ga asos.';
COMMENT ON COLUMN employee.pinfl IS 'Personal identification number (JSHSHIR) — unique identifier. SSO integratsiya asosi (context.md).';
COMMENT ON COLUMN employee.person_type IS 'Discriminator: UNIVERSITY_STAFF (46K OTM) | MINISTRY_STAFF | CENTER_STAFF (DTM/UzACI) | OTHER_ORG_STAFF (GUVD/Hokimiyat)';
COMMENT ON COLUMN employee.soato_code IS 'Address SOATO (hierarchical): 4=region, 7=district, 11=neighborhood';
COMMENT ON COLUMN employee.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN employee.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_employee_pinfl        ON employee(pinfl);         -- MyGov/E-Imzo lookup
CREATE INDEX idx_employee_person_type  ON employee(person_type);
CREATE INDEX idx_employee_name         ON employee(last_name, first_name);
CREATE INDEX idx_employee_tin ON employee(tin) WHERE tin IS NOT NULL;
CREATE INDEX idx_employee_deleted ON employee(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_employee_soato ON employee(soato_code) WHERE soato_code IS NOT NULL;

-- Passport is globally unique for a living person.
-- Partial UNIQUE index — multiple NULLs allowed (employees with unknown passport),
-- but any non-null value must be unique. Restored uniqueness on soft-delete
-- undo so a former employee's passport can be re-bound if restored.
CREATE UNIQUE INDEX uq_employee_passport
    ON employee(passport)
    WHERE passport IS NOT NULL AND deleted_at IS NULL;

-- PINFL globally unique per LIVING person.
-- Partial UNIQUE: soft-deleted xodim PINFL'ini qayta ishlatish mumkin (xodim qaytsa).
CREATE UNIQUE INDEX uq_employee_pinfl
    ON employee(pinfl)
    WHERE deleted_at IS NULL;

-- =====================================================
-- TABLE 2: employee_job (one person = many positions at many universities)
-- Pattern: PeopleSoft PS_JOB, Oracle PER_ALL_ASSIGNMENTS_F, Banner NBRJOBS
-- Direct successor of hemishe_e_employee_jobs (old CUBA table, stripped of prefix)
-- =====================================================
CREATE TABLE employee_job (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id           UUID NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    -- ON DELETE RESTRICT: universitetni o'chirish uchun avval xodim assignment'larini qo'lda hal qilish
    university_code       VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code) ON DELETE RESTRICT,
    -- Department FK — legacy CUBA jadvalga yo'naltirilgan
    department_code       VARCHAR(255) REFERENCES hemishe_e_university_department(code) ON DELETE SET NULL,
    position_code         VARCHAR(10) REFERENCES position(code) ON DELETE RESTRICT,
    position_type_code    VARCHAR(10) REFERENCES position_type(code) ON DELETE RESTRICT,
    -- Legacy classifier FK'lar — hemishe_h_*
    employment_form_code  VARCHAR(20) REFERENCES hemishe_h_university_employee_form(code) ON DELETE RESTRICT,
    employee_rate_code    VARCHAR(20) REFERENCES hemishe_h_university_employee_rate(code) ON DELETE RESTRICT,
    specialty             VARCHAR(500),

    is_current            BOOLEAN NOT NULL DEFAULT true,
    start_date            DATE,
    end_date              DATE,

    contract_number       VARCHAR(100),
    contract_date         DATE,
    decree_number         VARCHAR(100),
    decree_date           DATE,

    -- Audit
    version    INTEGER   DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

COMMENT ON TABLE employee_job IS 'Employment records — one person can hold many jobs at many universities. PeopleSoft: PS_JOB. Banner: NBRJOBS.';
COMMENT ON COLUMN employee_job.university_code IS 'Which university (like PeopleSoft SetID)';
COMMENT ON COLUMN employee_job.is_current IS 'Active assignment flag — false when person leaves or position changes';
COMMENT ON COLUMN employee_job.specialty IS 'Specialty for this specific job (assignment-scoped, not person-scoped)';
COMMENT ON COLUMN employee_job.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN employee_job.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_ejob_employee ON employee_job(employee_id);
CREATE INDEX idx_ejob_university ON employee_job(university_code);
CREATE INDEX idx_ejob_department ON employee_job(department_code) WHERE department_code IS NOT NULL;
CREATE INDEX idx_ejob_current ON employee_job(employee_id, is_current) WHERE is_current = true AND deleted_at IS NULL;
CREATE INDEX idx_ejob_position ON employee_job(position_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_ejob_deleted ON employee_job(deleted_at) WHERE deleted_at IS NULL;

-- =====================================================
-- TABLE 3: employee_academic_credential (STI — Single Table Inheritance)
-- One row = one award: either a "DEGREE" (ilmiy daraja — PhD, DSc, Candidate)
-- or a "TITLE"  (ilmiy unvon — Doцent, Professor). One employee can hold many.
-- Pattern: Martin Fowler PEAA — Single Table Inheritance with discriminator.
-- Mirrors legacy hemis_337 credential table shape and SAC API response structure
-- (each response row already contains either 'ilmiy_unvon' OR 'ilmiy_daraja').
-- =====================================================
CREATE TABLE employee_academic_credential (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id            UUID         NOT NULL REFERENCES employee(id) ON DELETE CASCADE,

    -- STI discriminator
    credential_type        VARCHAR(10)  NOT NULL,

    -- Classifier FK'lar (legacy hemishe_h_* — Single Source of Truth, rules.md #3).
    -- XOR: degree_code populated for DEGREE rows, rank_code for TITLE rows.
    -- Populated opportunistically by SAC sync service via name-based lookup:
    --   DEGREE: hemishe_h_academic_degree.name = api.degree_name ("Фан номзоди")
    --   TITLE : hemishe_h_academic_rank.name   = api.title       ("Доцент")
    -- When lookup fails the FK stays NULL and display falls back to external_*_name.
    degree_code            VARCHAR(20)  REFERENCES hemishe_h_academic_degree(code),
    rank_code              VARCHAR(20)  REFERENCES hemishe_h_academic_rank(code),

    -- External API values — preserved VERBATIM as SAC delivered them.
    -- SAC is the authoritative source. 'science_sector' has NO matching classifier
    -- in our database (legacy hemishe_h_science_branch doesn't cover SAC sectors),
    -- so both code and name are stored raw. 'title_code' and 'degree_code' integers
    -- from SAC are kept for audit and re-sync reverse lookup.
    external_classifier_code INTEGER,       -- API 'title_code' or 'degree_code' (int)
    external_classifier_name VARCHAR(255),  -- API 'title' or 'degree_name' (text)
    external_sector_code     INTEGER,       -- API 'science_sector_code' (int)
    external_sector_name     VARCHAR(255),  -- API 'science_sector' (text)

    -- Shared fields (both DEGREE and TITLE use these)
    -- Speciality: SAC API delivers the authoritative, currently-valid string
    -- ("02.00.13 - Технология..."). We store it verbatim rather than FK to
    -- hemishe_h_speciality_doctoral because the legacy classifier is not kept
    -- up to date. A future migration can backfill the FK when the classifier
    -- is refreshed.
    speciality             VARCHAR(500),

    -- Diploma number: stored verbatim for display ("03 № 007620"), but uniqueness
    -- is enforced against diploma_number_key (generated, DB-level normalization).
    -- This prevents duplicates when SAC occasionally varies whitespace or № char
    -- ("03 № 007620" vs "03№007620") — neither service drift nor human input
    -- variations can bypass the UNIQUE constraint.
    diploma_number         VARCHAR(100) NOT NULL,
    diploma_number_key     VARCHAR(100) GENERATED ALWAYS AS (
        UPPER(REGEXP_REPLACE(COALESCE(diploma_number, ''), '\s+', '', 'g'))
    ) STORED,

    confirmed_date         DATE,

    -- DEGREE-only: dissertation theme
    theme                  TEXT,

    -- Source tracking (SAC — Science Academic Center)
    source                 VARCHAR(50)  NOT NULL DEFAULT 'sac-api',
    source_raw             JSONB,                        -- original API record (audit trail)
    source_updated_at      TIMESTAMP,

    -- Audit (AuditableEntity pattern)
    version    INTEGER   DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    -- Discriminator domain
    CONSTRAINT chk_eac_type CHECK (credential_type IN ('DEGREE', 'TITLE')),

    -- STI integrity: DEGREE has degree_code (no rank_code); TITLE has rank_code (no degree_code, no theme).
    CONSTRAINT chk_eac_xor CHECK (
        (credential_type = 'DEGREE' AND rank_code   IS NULL)
        OR
        (credential_type = 'TITLE'  AND degree_code IS NULL AND theme IS NULL)
    ),

    -- Idempotent upsert key — re-sync from SAC won't create duplicates.
    -- Uses the normalized generated column (whitespace-stripped, uppercased),
    -- so "03 № 007620" and "03№007620" collide and the second INSERT fails,
    -- regardless of whether the service layer normalizes.
    CONSTRAINT uq_eac_diploma UNIQUE (employee_id, diploma_number_key)
);

COMMENT ON TABLE  employee_academic_credential IS 'Academic credentials (degrees + titles) — one row per awarded credential. STI with credential_type discriminator. Populated from SAC API and legacy hemis_337.';
COMMENT ON COLUMN employee_academic_credential.credential_type IS 'STI discriminator: DEGREE (ilmiy daraja) | TITLE (ilmiy unvon)';
COMMENT ON COLUMN employee_academic_credential.degree_code IS 'FK to hemishe_h_academic_degree. NULL for TITLE rows.';
COMMENT ON COLUMN employee_academic_credential.rank_code IS 'FK to hemishe_h_academic_rank. NULL for DEGREE rows.';
COMMENT ON COLUMN employee_academic_credential.speciality IS 'Raw speciality string as delivered by SAC API ("02.00.13 - Технология..."). Authoritative — legacy hemishe_h_speciality_doctoral classifier is outdated, FK deferred.';
COMMENT ON COLUMN employee_academic_credential.external_classifier_code IS 'Integer code from SAC API (title_code/degree_code) — raw, never transformed.';
COMMENT ON COLUMN employee_academic_credential.external_classifier_name IS 'Text name from SAC API (title/degree_name) — display fallback when classifier FK is NULL.';
COMMENT ON COLUMN employee_academic_credential.external_sector_name IS 'Text name from SAC API (science_sector) — display fallback.';
COMMENT ON COLUMN employee_academic_credential.theme IS 'Dissertation theme. DEGREE-only — NULL for TITLE rows (enforced by CHECK).';
COMMENT ON COLUMN employee_academic_credential.source_raw IS 'Original API response JSON — debug/audit trail.';

CREATE INDEX idx_eac_employee      ON employee_academic_credential(employee_id);
CREATE INDEX idx_eac_type          ON employee_academic_credential(credential_type);
CREATE INDEX idx_eac_degree        ON employee_academic_credential(degree_code)   WHERE degree_code IS NOT NULL;
CREATE INDEX idx_eac_rank          ON employee_academic_credential(rank_code)     WHERE rank_code IS NOT NULL;
CREATE INDEX idx_eac_confirmed     ON employee_academic_credential(confirmed_date) WHERE confirmed_date IS NOT NULL;
CREATE INDEX idx_eac_deleted       ON employee_academic_credential(deleted_at)    WHERE deleted_at IS NULL;

