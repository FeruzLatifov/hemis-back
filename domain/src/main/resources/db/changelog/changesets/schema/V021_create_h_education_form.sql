-- =====================================================
-- V021: modern h_education_form classifier + FK swap on university_speciality_attachment
-- =====================================================
-- Author: hemis-team
-- Purpose: Ta'lim shakli endi FE'da hard-code (faqat 11/12/16) EMAS — o'zimizning modern
--          klassifikator `h_education_form` (ReferenceEntity: code PK, name/name_ru/name_en,
--          is_active, version, modern audit). h_education_year (V018) BILAN BIR XIL PATTERN:
--          eski frozen CUBA `hemishe_h_education_form` jadvalidan 1:1 SELECT bilan olinadi,
--          o'z schemamizga moslanadi. CUBA manbada RU/EN yo'q (faqat uz `name`) — shuning uchun
--          RU/EN'ni O'ZIMIZ to'ldiramiz (multilingual — CUBA'da bo'lmagan qo'shimcha qiymat).
-- FK: university_speciality_attachment.education_form — hard-coded CHECK('11','12','16') OLIB
--     TASHLANADI -> REAL FK h_education_form(code) + index (drift yo'q, 13 shakl ham mavjud).
-- Idempotent: CREATE IF NOT EXISTS, seed ON CONFLICT, ALTER IF EXISTS.
-- =====================================================

-- 1. Modern classifier table (ReferenceEntity shape)
CREATE TABLE IF NOT EXISTS h_education_form (
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

COMMENT ON TABLE h_education_form IS
    'Modern "Ta''lim shakli" classifier. Seeded 1:1 from frozen hemishe_h_education_form (uz name + active); RU/EN owned by us (CUBA source lacks them). FK target for university_speciality_attachment.education_form.';

-- 2a. 1:1 seed from the frozen legacy classifier (canonical set + authoritative uz name + active).
--     Mirrors h_education_year (V018): numeric code, live rows only (CUBA soft-delete delete_ts).
INSERT INTO h_education_form (code, name, is_active, sort_order, created_by)
SELECT code,
       COALESCE(NULLIF(name, ''), code),
       COALESCE(active, true),
       code::int,                       -- natural display order = numeric form code (11..23)
       'system'
FROM hemishe_h_education_form
WHERE code ~ '^[0-9]+$'
  AND delete_ts IS NULL
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, is_active = EXCLUDED.is_active, sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- 2b. RU/EN enrichment — the CUBA source is uz-only; we own the translations. ON CONFLICT touches
--     ONLY name_ru/name_en (keeps the CUBA-authoritative uz name). Doubles as a completeness
--     fallback: seeds the canonical 13 if step 2a missed a code.
INSERT INTO h_education_form (code, name, name_ru, name_en, sort_order, created_by) VALUES
    ('11', 'Kunduzgi',                  'Дневная',                      'Full-time',                  11, 'system'),
    ('12', 'Kechki',                    'Вечерняя',                     'Evening',                    12, 'system'),
    ('13', 'Sirtqi',                    'Заочная',                      'Part-time',                  13, 'system'),
    ('14', 'Maxsus sirtqi',             'Специальная заочная',          'Special part-time',          14, 'system'),
    ('15', 'Sirtqi (Ikkinchi oliy)',    'Заочная (второе высшее)',      'Part-time (second degree)',  15, 'system'),
    ('16', 'Masofaviy',                 'Дистанционная',                'Distance',                   16, 'system'),
    ('17', 'Sirtqi (Qo''shma)',         'Заочная (совместная)',         'Part-time (joint)',          17, 'system'),
    ('18', 'Kunduzgi (Ikkinchi oliy)',  'Дневная (второе высшее)',      'Full-time (second degree)',  18, 'system'),
    ('19', 'Kechki (Ikkinchi oliy)',    'Вечерняя (второе высшее)',     'Evening (second degree)',    19, 'system'),
    ('20', 'Kunduzgi (Qo''shma)',       'Дневная (совместная)',         'Full-time (joint)',          20, 'system'),
    ('21', 'Kechki (Qo''shma)',         'Вечерняя (совместная)',        'Evening (joint)',            21, 'system'),
    ('22', 'Masofaviy (Ikkinchi oliy)', 'Дистанционная (второе высшее)','Distance (second degree)',   22, 'system'),
    ('23', 'Masofaviy (Qo''shma)',      'Дистанционная (совместная)',   'Distance (joint)',           23, 'system')
ON CONFLICT (code) DO UPDATE SET
    name_ru = EXCLUDED.name_ru, name_en = EXCLUDED.name_en,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- 3. Swap the hard-coded CHECK for a REAL FK (h_education_form.code). Existing attachment rows
--    hold only 11/12/16 — all seeded above — so the FK validates cleanly. Runs after V019 (table)
--    and before S018 (attachment seed), so on a fresh build the FK exists when S018 inserts.
ALTER TABLE university_speciality_attachment DROP CONSTRAINT IF EXISTS chk_univ_spec_attach_form;
ALTER TABLE university_speciality_attachment
    ADD CONSTRAINT fk_univ_spec_attach_form FOREIGN KEY (education_form)
    REFERENCES h_education_form(code) ON DELETE RESTRICT;
-- FK column index (PostgreSQL does not auto-create it)
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_form ON university_speciality_attachment(education_form);
