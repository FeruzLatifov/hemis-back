-- =====================================================
-- V024: building egalik klassifikatori + cad_number per-OTM scope
-- =====================================================
-- Author: hemis-team
-- Purpose: (1) Bino egalik shakli — xususiy OTM ijaraда o'tiradi, davlat OTM operativ
--          boshqaruvда. `h_building_ownership` klassifikatori (OWN/OPERATIVE/RENT) +
--          university_building.ownership_code FK (default OWN). Kelajakда kadastr subjects'дан
--          auto-derive (OTM INN egami).
--          (2) cad_number uniqueness GLOBAL -> per-OTM: bir ijara/umumiy binoni (bir cad_number)
--          bir nechta OTM ulasha oladi (masalan biznes-markazда 2 xususiy OTM qavat ijaraga oladi).
-- Idempotent: CREATE IF NOT EXISTS, seed ON CONFLICT, ADD COLUMN IF NOT EXISTS, DROP/CREATE INDEX.
-- =====================================================

-- 1. Egalik shakli klassifikatori (ReferenceEntity shape)
CREATE TABLE IF NOT EXISTS h_building_ownership (
    code        VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
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

COMMENT ON TABLE h_building_ownership IS
    'Bino egalik shakli: OWN (o''z mulki) / OPERATIVE (operativ boshqaruv — davlat mulki, OTM ixtiyorida) / RENT (ijara). Kadastr subjects''дан auto-derive mumkin.';

INSERT INTO h_building_ownership (code, name, name_ru, name_en, sort_order, created_by) VALUES
    ('OWN',       'O''z mulki',          'Собственность',          'Owned',                  1, 'system'),
    ('OPERATIVE', 'Operativ boshqaruv',  'Оперативное управление', 'Operational management', 2, 'system'),
    ('RENT',      'Ijara',               'Аренда',                 'Rented',                 3, 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, name_ru = EXCLUDED.name_ru, name_en = EXCLUDED.name_en,
    sort_order = EXCLUDED.sort_order, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- 2. university_building.ownership_code FK (default OWN — aksariyat bino OTM'niki; faqat ijara belgilanadi).
ALTER TABLE university_building ADD COLUMN IF NOT EXISTS ownership_code VARCHAR(20) DEFAULT 'OWN';

ALTER TABLE university_building DROP CONSTRAINT IF EXISTS fk_ub_ownership;
ALTER TABLE university_building
    ADD CONSTRAINT fk_ub_ownership FOREIGN KEY (ownership_code)
    REFERENCES h_building_ownership(code) ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_ub_ownership
    ON university_building(ownership_code) WHERE ownership_code IS NOT NULL;

-- 3. cad_number uniqueness: GLOBAL -> per-OTM. Ijara/umumiy binoni bir nechta OTM ulasha oladi.
DROP INDEX IF EXISTS uq_ub_cad_number;
CREATE UNIQUE INDEX IF NOT EXISTS uq_ub_univ_cad
    ON university_building (university_code, cad_number)
    WHERE cad_number IS NOT NULL AND deleted_at IS NULL;
