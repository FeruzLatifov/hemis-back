-- =====================================================
-- V022: modern h_education_type classifier + repoint h_speciality.education_type FK
-- =====================================================
-- Author: hemis-team
-- Purpose: Ta'lim turi endi frozen CUBA `hemishe_h_education_type`ga bog'lanmasin — o'zimizning
--          modern klassifikator `h_education_type` (ReferenceEntity: code PK, name/name_ru/name_en,
--          is_active, version, modern audit). h_education_form (V021) / h_education_year (V018) bilan
--          BIR XIL PATTERN: eski jadvaldan 1:1 SELECT, o'z schemamizga moslash; RU/EN'ni biz to'ldiramiz.
--          5 tur: 11=Bakalavr, 12=Magistr, 13=Ordinatura, 14=Doktorantura PhD, 15=Doktorantura DSc.
-- FK: h_speciality.education_type FK'ini hemishe_h_education_type(code) -> h_education_type(code) ga
--     KO'CHIRAMIZ. CHECK (education_type IN '11','12') SAQLANADI — bu klassifikator hozir faqat
--     Bakalavr+Magistr; kelajakda Ordinatura/Doktorantura mutaxassisliklari qo'shilsa: CHECK relax +
--     ma'lumot yuklash + FE tab. Student._education_type FK'ига TEGILMAYDI (alohida).
-- Idempotent: CREATE IF NOT EXISTS, seed ON CONFLICT, ALTER IF EXISTS.
-- =====================================================

-- 1. Modern classifier table (ReferenceEntity shape)
CREATE TABLE IF NOT EXISTS h_education_type (
    code        VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,        -- uz
    name_ru     VARCHAR(128),
    name_en     VARCHAR(128),
    is_active   BOOLEAN NOT NULL DEFAULT true,
    sort_order  INTEGER,
    version     INTEGER NOT NULL DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50) DEFAULT 'system',
    updated_at  TIMESTAMP,
    updated_by  VARCHAR(50)
);

COMMENT ON TABLE h_education_type IS
    'Modern "Ta''lim turi" classifier. Seeded 1:1 from frozen hemishe_h_education_type (uz name + active); RU/EN owned by us. FK target for h_speciality.education_type (currently Bakalavr+Magistr only).';

-- 2a. 1:1 seed from the frozen legacy classifier (canonical set + authoritative uz name + active).
INSERT INTO h_education_type (code, name, is_active, sort_order, created_by)
SELECT code,
       COALESCE(NULLIF(name, ''), code),
       COALESCE(active, true),
       code::int,
       'system'
FROM hemishe_h_education_type
WHERE code ~ '^[0-9]+$'
  AND delete_ts IS NULL
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, is_active = EXCLUDED.is_active, sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- 2b. RU/EN enrichment — CUBA source is uz-only; we own the translations. ON CONFLICT touches ONLY
--     name_ru/name_en (keeps the CUBA-authoritative uz name). Doubles as a completeness fallback.
INSERT INTO h_education_type (code, name, name_ru, name_en, sort_order, created_by) VALUES
    ('11', 'Bakalavr',            'Бакалавр',            'Bachelor',                  11, 'system'),
    ('12', 'Magistr',             'Магистр',             'Master',                    12, 'system'),
    ('13', 'Ordinatura',          'Ординатура',          'Residency',                 13, 'system'),
    ('14', 'Doktorantura PhD',    'Докторантура PhD',    'Doctoral studies (PhD)',    14, 'system'),
    ('15', 'Doktorantura DSc',    'Докторантура DSc',    'Doctoral studies (DSc)',    15, 'system')
ON CONFLICT (code) DO UPDATE SET
    name_ru = EXCLUDED.name_ru, name_en = EXCLUDED.name_en,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- 3. Repoint h_speciality.education_type FK to OUR classifier. Existing rows are 11/12 — both seeded
--    above — so the new FK validates cleanly. The CHECK (IN '11','12') stays as the business rule.
ALTER TABLE h_speciality DROP CONSTRAINT IF EXISTS fk_h_speciality_edu_type;
ALTER TABLE h_speciality
    ADD CONSTRAINT fk_h_speciality_edu_type FOREIGN KEY (education_type)
    REFERENCES h_education_type(code);
-- (idx_h_speciality_edu_type already indexes the column — V018.)
