-- =====================================================
-- V018: UNIFIED SPECIALITY CLASSIFIER (h_education_year + h_speciality + h_speciality_year)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-07-17
-- Purpose: New unified bachelor+master speciality classifier, keyed by UUID
--          (the xlsx/legacy ID), with an education_type FK discriminator, a
--          self-referencing parent tree, normalized years (1:N child) that FK
--          into a modern INTEGER-keyed education-year classifier, and a
--          review_status workflow. Source of truth = curated xlsx
--          (2_Bakalavr.xlsx + 3_Magistr.xlsx) + 53 live-DB-new rows.
-- Pattern: AuditableEntityNoSoftDelete (modern audit: version, created_at/by,
--          updated_at/by — NO deleted_at; classifier rows are deactivated via
--          active=false, never soft-deleted). Self-FK tree copies menu (V013).
-- Distribution: modern PUSH (outbox aggregate_type="classifier" -> webhook fanout)
--          from HSpecialityService.update(), plus an api-university bootstrap PULL
--          (GET /api/v1/university/classifiers/speciality). The frozen legacy pull
--          and ClassifierLegacyService.OLD_CLASSIFIER_MAP are INTENTIONALLY NOT
--          extended (see hemis-back-modular-architecture). The GENERATED "name"
--          column below is kept for display parity across channels.
-- Frozen: does NOT touch hemishe_h_speciality_bachelor/_master or the 175/175
--          contract — this is a brand-new additive table.
-- =====================================================

-- =====================================================
-- Education-year classifier (h_education_year) — modern, INTEGER-keyed.
-- Our own version of the frozen legacy hemishe_h_education_year: the legacy
-- classifier stores the year as code VARCHAR ('2024'); here it is the integer
-- primary key. Univer (per-OTM Yii2) keeps a same-named classifier table
-- `h_education_year` with a string `code` — our integer maps 1:1 to that code
-- (same value set, our internal representation). Seeded 1:1 from the legacy
-- table below; pushing these values to Univer is a future additive layer
-- (the frozen 175/175 legacy education_year serving is NOT touched).
-- Created + seeded BEFORE h_speciality_year so its FK (year -> h_education_year)
-- resolves when S015 loads the year rows.
-- =====================================================
CREATE TABLE IF NOT EXISTS h_education_year (
    year        INTEGER PRIMARY KEY,              -- internal key = Univer classifier code (e.g. 2024)
    name        VARCHAR(32)  NOT NULL,            -- academic-year span, e.g. "2024-2025"
    name_ru     VARCHAR(128),
    name_en     VARCHAR(128),
    active      BOOLEAN NOT NULL DEFAULT true,
    -- Audit (AuditableEntityNoSoftDelete — mirrors h_speciality; version = distribution cache-bust)
    version     INTEGER NOT NULL DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50),
    updated_at  TIMESTAMP,
    updated_by  VARCHAR(50)
);

COMMENT ON TABLE h_education_year IS
    'Modern INTEGER-keyed academic-year classifier. Seeded 1:1 from frozen hemishe_h_education_year (code::int). FK target for h_speciality_year.year; future push-distribution source for Univer''s h_education_year classifier.';

-- 1:1 seed from the frozen legacy classifier (the canonical value set: 1991-2040).
-- code(varchar numeric) -> year(int); blank locale names -> NULL; the span name is
-- kept (fallback-generated only if a legacy name is blank). Idempotent on re-run.
INSERT INTO h_education_year (year, name, name_ru, name_en, active)
SELECT code::int,
       COALESCE(NULLIF(name, ''), code || '-' || (code::int + 1)),
       NULLIF(name_ru, ''),
       NULLIF(name_en, ''),
       active
FROM hemishe_h_education_year
WHERE code ~ '^[0-9]+$'
  AND delete_ts IS NULL          -- CUBA soft-delete: import live rows only
ON CONFLICT (year) DO NOTHING;

-- Fallback range so the speciality-year FK always resolves even on a legacy-less fresh DB
-- (locale names from the legacy insert above win; this only fills any missing years).
INSERT INTO h_education_year (year, name)
SELECT y, y || '-' || (y + 1) FROM generate_series(1991, 2040) AS y
ON CONFLICT (year) DO NOTHING;

-- Identity fold: apostrophe-variant -> space, lower, collapse whitespace. IMMUTABLE so it can back a
-- GENERATED name_search column (no trigger, cannot be bypassed by any write path). MUST stay
-- byte-identical to the ETL fold() and the Java foldSearch() — all three feed the same
-- (education_type, code, name_search) identity key, so a drift would let a duplicate slip the constraint.
CREATE OR REPLACE FUNCTION h_speciality_fold(txt text) RETURNS text
    LANGUAGE sql IMMUTABLE PARALLEL SAFE AS
$$ SELECT btrim(regexp_replace(lower(translate(coalesce(txt, ''), '''’ʻʼ‘`', '      ')), '\s+', ' ', 'g')) $$;

CREATE TABLE IF NOT EXISTS h_speciality (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),   -- xlsx ID on import; gen for FE-created rows
    code             VARCHAR(64),                                  -- NULLABLE: 15 NEEDS_REVIEW rows are code-less until curated
    name_uz          VARCHAR(512) NOT NULL,                        -- uz-UZ (Latin) — primary + identity anchor
    name_oz          VARCHAR(512),                                 -- oz-UZ (Uzbek Cyrillic); NULL where source had none
    name_ru          VARCHAR(512),
    name_en          VARCHAR(512),
    -- Display-parity column (mirrors name_uz). Kept generated so any name-keyed
    -- consumer/report sees a populated "name" without a separate write path.
    name             VARCHAR(512) GENERATED ALWAYS AS (name_uz) STORED,
    -- Identity/search key: DB-authoritative, GENERATED from name_uz via the immutable fold above.
    -- No write path (seed, ETL, JPA) can set a divergent value, so the identity UNIQUE below is trustworthy.
    name_search      VARCHAR(512) GENERATED ALWAYS AS (h_speciality_fold(name_uz)) STORED,
    -- Education type: REAL FK to the frozen CUBA "Ta'lim turlari" classifier
    -- (hemishe_h_education_type.code — '11'=Bakalavr, '12'=Magistr, 13=Ordinatura, 14=PhD, 15=DSc).
    -- Same classifier Student._education_type references — single source of truth (goal #2), no
    -- standalone enum. CHECK narrows to bachelor+master (this classifier is 2-tab); add a code here
    -- + an FE tab to admit a new level later. FK target pre-exists (CUBA restore) — same pattern as
    -- V004 gender_code/nationality_code/academic_degree_code (7+ live FKs to hemishe_h_*(code)).
    education_type   VARCHAR(32) NOT NULL
        CONSTRAINT chk_h_speciality_edu_type CHECK (education_type IN ('11','12'))
        CONSTRAINT fk_h_speciality_edu_type REFERENCES hemishe_h_education_type(code),
    review_status    VARCHAR(20) NOT NULL DEFAULT 'APPROVED'
        CONSTRAINT chk_h_speciality_review CHECK (review_status IN ('APPROVED','NEEDS_REVIEW')),
    parent_id        UUID,                                         -- self-ref rows land as NULL (ETL null-fixes)
    hierarchy_level  INTEGER,
    active           BOOLEAN NOT NULL DEFAULT true,
    is_checked       BOOLEAN NOT NULL DEFAULT false,

    -- Audit (AuditableEntityNoSoftDelete — modern naming, no soft delete)
    version          INTEGER NOT NULL DEFAULT 1,                   -- @Version; Univer cache-bust = SUM(version)
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(50),
    updated_at       TIMESTAMP,
    updated_by       VARCHAR(50),

    -- Self-FK deferrable so in-transaction seed order (parent before child) is not fatal
    CONSTRAINT fk_h_speciality_parent FOREIGN KEY (parent_id)
        REFERENCES h_speciality(id) ON DELETE RESTRICT
        DEFERRABLE INITIALLY DEFERRED,

    -- Identity: enforces the ministry rule "within one classifier, a (education_type, code, name)
    -- appears once". name_search is the folded name (generated above). NULLS NOT DISTINCT (PG15+) makes
    -- the ~15 code-less NEEDS_REVIEW rows obey (education_type, name) uniqueness too. Combined with
    -- uq_h_speciality_year(speciality_id, year) this also guarantees (edu, code, name, year) is unique.
    -- Same-code/different-name AND same-name/different-code both stay legal (both are key members).
    CONSTRAINT uq_h_speciality_identity
        UNIQUE NULLS NOT DISTINCT (education_type, code, name_search)
);

COMMENT ON TABLE h_speciality IS
    'Unified bachelor+master speciality classifier (UUID-keyed tree, normalized years, review workflow). Source: curated xlsx + live-DB-new. Additive; frozen hemishe_h_speciality_* untouched.';

CREATE INDEX IF NOT EXISTS idx_h_speciality_parent    ON h_speciality(parent_id) WHERE parent_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_h_speciality_edu_type  ON h_speciality(education_type);
CREATE INDEX IF NOT EXISTS idx_h_speciality_review    ON h_speciality(review_status);
CREATE INDEX IF NOT EXISTS idx_h_speciality_code      ON h_speciality(code) WHERE code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_h_speciality_search    ON h_speciality(name_search);

-- 1:N normalized years (dot-range expanded, comma-list split; APPROVED rows only).
-- year FK -> h_education_year(year): DB-level referential integrity, no app-side load.
CREATE TABLE IF NOT EXISTS h_speciality_year (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    speciality_id   UUID NOT NULL,
    year            INTEGER NOT NULL,
    CONSTRAINT fk_h_speciality_year_spec FOREIGN KEY (speciality_id)
        REFERENCES h_speciality(id) ON DELETE CASCADE,
    CONSTRAINT fk_h_speciality_year_year FOREIGN KEY (year)
        REFERENCES h_education_year(year),
    CONSTRAINT uq_h_speciality_year UNIQUE (speciality_id, year)
);

CREATE INDEX IF NOT EXISTS idx_h_speciality_year_spec ON h_speciality_year(speciality_id);
CREATE INDEX IF NOT EXISTS idx_h_speciality_year_year ON h_speciality_year(year);
