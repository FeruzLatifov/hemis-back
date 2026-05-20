-- =====================================================
-- V010: UNIVERSITY BUILDINGS MODULE
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-21
-- Source: /home/adm1n/projects/startup/docs/Бино ва иншоотлар жадвали.xlsx
-- Purpose: Physical building data for university infrastructure
-- Architecture: Senior Enterprise (A'' strategy)
--   1. Normalized schema: 3 classifiers + main + immutable lifecycle log
--   2. Cadastre data: cad_number (string) + cadastre JSONB snapshot (caller-provided)
--   3. Data irreversibility: building_lifecycle preserves renovation history
--   4. Source tracking: univer_sync / manual / excel_import / kadastr_sync
--   5. Idempotent sync: (university_code, source_uid) UNIQUE + content_hash
--   6. Graceful degradation: coordinates NULLABLE for legacy data
-- =====================================================

-- =====================================================
-- 1. h_building_category — bino kategoriyasi classifier (kengayadigan)
-- h_* prefiks: 224 OTM ekosistemi konvensiyasi (ADR-0006)
-- =====================================================
CREATE TABLE IF NOT EXISTS h_building_category (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO h_building_category (code, name, name_ru, name_en, sort_order) VALUES
    ('ACADEMIC',   'O''quv binolari',        'Учебные здания',        'Academic buildings',      1),
    ('DORMITORY',  'Talabalar turar joyi',   'Студенческие общежития','Student dormitories',     2),
    ('ACTIVITY',   'Faollar zali',           'Актовый зал',           'Activity hall',           3),
    ('SPORTS',     'Sport inshootlari',      'Спортивные сооружения', 'Sports facilities',       4),
    ('UTILITY',    'Xo''jalik binolari',     'Хозяйственные здания',  'Utility buildings',       5),
    ('RECREATION', 'Dam olish maskanlari',   'Места отдыха',          'Recreation facilities',   6)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- 2. h_construction_material — qurilish materiali classifier
-- =====================================================
CREATE TABLE IF NOT EXISTS h_construction_material (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO h_construction_material (code, name, name_ru, name_en, sort_order) VALUES
    ('BRICK',    'G''isht',    'Кирпич',        'Brick',    1),
    ('CONCRETE', 'Beton',      'Бетон',         'Concrete', 2),
    ('PANEL',    'Panel',      'Панель',        'Panel',    3),
    ('WOOD',     'Yog''och',   'Дерево',        'Wood',     4),
    ('STONE',    'Tosh',       'Камень',        'Stone',    5),
    ('METAL',    'Metall',     'Металл',        'Metal',    6),
    ('MIXED',    'Aralash',    'Смешанный',     'Mixed',    7)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- 3. h_roof_type — tom qoplamasi turi classifier
-- =====================================================
CREATE TABLE IF NOT EXISTS h_roof_type (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO h_roof_type (code, name, name_ru, name_en, sort_order) VALUES
    ('METAL_SHEET', 'Metall qoplama',    'Металлическое',        'Metal sheet',      1),
    ('TILE',        'Cherepitsa',        'Черепица',             'Tile',             2),
    ('SHIFER',      'Shifer',            'Шифер',                'Slate',            3),
    ('RUBEROID',    'Ruberoyd (tekis)',  'Рубероидная плоская',  'Rubber flat',      4),
    ('PROFNAIL',    'Profnastil',        'Профнастил',           'Corrugated metal', 5),
    ('CONCRETE',    'Betonli',           'Бетонная',             'Concrete roof',    6)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- 4. university_building — asosiy jadval
-- =====================================================
-- Pattern: AuditableEntity (7 audit ustun: version, created_at/by, updated_at/by, deleted_at/by)
-- Linkage: cadastre snapshot (JSONB) — caller API tomonidan to'ldiriladi (integratsiya yo'q).
-- Excel mapping: docs/Бино ва иншоотлар жадвали.xlsx dagi 14 ustunga to'liq mos
-- =====================================================
CREATE TABLE IF NOT EXISTS university_building (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Universitet — ON DELETE RESTRICT (defense-in-depth, ADR-0001 — building alohida domain).
    -- Loyiha qoidasi: faqat soft-delete (deleted_at SET, FK trigger emas). Bevosita SQL DELETE
    -- bajarilsa RESTRICT bilan blokirovka qilinadi va manual cleanup talab qilinadi.
    university_code VARCHAR(255) NOT NULL
        REFERENCES hemishe_e_university(code) ON DELETE RESTRICT,

    -- Excel col 2: Binoning nomi
    name VARCHAR(500) NOT NULL,

    -- Excel: Kategoriya (6 ta, kengayuvchi)
    category_code VARCHAR(20) NOT NULL
        REFERENCES h_building_category(code) ON DELETE RESTRICT,

    -- Excel col 3: Yuridik manzil (cadastre'dan auto-fill)
    address TEXT,

    -- Excel col 4-6: Qurilish parametrlari
    year_built INTEGER,
    capacity INTEGER,                          -- o'quv/yotoq o'rin
    floor_count INTEGER,

    -- Excel col 7-8: Maydon (cadastre'dan auto-fill)
    total_area NUMERIC(10,2),
    usable_area NUMERIC(10,2),

    -- Excel col 9-10: Material va tom (classifier FK)
    construction_material_code VARCHAR(20)
        REFERENCES h_construction_material(code) ON DELETE SET NULL,
    roof_type_code VARCHAR(20)
        REFERENCES h_roof_type(code) ON DELETE SET NULL,

    -- Excel col 11: Oxirgi ta'mir (tarixi — building_lifecycle'da)
    last_renovation_date DATE,

    -- Excel col 12: Joylashuv (WGS84 — NULLABLE, sync flexibility)
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    map_url TEXT,                              -- Google/Yandex Maps link

    -- Excel col 13: Kadastr raqami (string, snapshot JSONB cadastre ustunda)
    cad_number VARCHAR(50),

    -- Cadastre snapshot (caller-provided JSON, kadastr ma'lumotlari)
    cadastre JSONB,

    -- Excel col 14: Izoh
    note TEXT,

    -- Sync tracking (univer → centralized)
    source VARCHAR(20) NOT NULL DEFAULT 'univer_sync',
    source_uid VARCHAR(255),                   -- Univer'ning ichki ID
    synced_at TIMESTAMP,
    content_hash VARCHAR(64),                  -- SHA-256 change detection

    -- CHECK constraints (data quality)
    CONSTRAINT chk_ub_source CHECK (source IN
        ('univer_sync', 'manual', 'excel_import', 'kadastr_sync')),
    CONSTRAINT chk_ub_year CHECK
        (year_built IS NULL OR year_built BETWEEN 1800 AND 2100),
    CONSTRAINT chk_ub_floors CHECK
        (floor_count IS NULL OR floor_count BETWEEN 1 AND 100),
    CONSTRAINT chk_ub_area_positive CHECK
        ((total_area IS NULL OR total_area >= 0)
         AND (usable_area IS NULL OR usable_area >= 0)),
    CONSTRAINT chk_ub_usable_le_total CHECK
        (usable_area IS NULL OR total_area IS NULL OR usable_area <= total_area),
    CONSTRAINT chk_ub_capacity CHECK (capacity IS NULL OR capacity >= 0),
    CONSTRAINT chk_ub_lat CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_ub_lng CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_ub_coords_pair CHECK
        ((latitude IS NULL) = (longitude IS NULL)),  -- ikkalasi bor yoki ikkalasi yo'q

    -- Audit (AuditableEntity — 7 ustun)
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

COMMENT ON TABLE university_building IS
    'Physical buildings at universities (academic/operational view).
     Cadastre data stored as JSONB snapshot (caller-provided).
     Source: Excel template "Бино ва иншоотлар жадвали"';
COMMENT ON COLUMN university_building.content_hash IS
    'SHA-256 of sync-relevant fields for idempotent change detection';
COMMENT ON COLUMN university_building.source IS
    'Data origin: univer_sync (OTM push) | manual (ministry) | excel_import (bulk) | kadastr_sync';
COMMENT ON COLUMN university_building.cad_number IS
    'Kadastr raqami (string). Cadastre tafsiloti `cadastre` JSONB ustunida saqlanadi.';
COMMENT ON COLUMN university_building.cadastre IS
    'Cadastre snapshot (JSONB). Caller API tomonidan to''ldiriladi.';

-- Indexes (query pattern bo'yicha)
CREATE INDEX IF NOT EXISTS idx_ub_university ON university_building(university_code);
CREATE INDEX IF NOT EXISTS idx_ub_category   ON university_building(category_code);
CREATE INDEX IF NOT EXISTS idx_ub_material   ON university_building(construction_material_code) WHERE construction_material_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ub_roof       ON university_building(roof_type_code) WHERE roof_type_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ub_cad        ON university_building(cad_number) WHERE cad_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ub_geo        ON university_building(latitude, longitude) WHERE latitude IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ub_synced     ON university_building(synced_at) WHERE synced_at IS NOT NULL;
-- NOTE: `idx_ub_deleted ON (deleted_at) WHERE deleted_at IS NULL` removed (always empty).

-- Univer sync idempotency: (OTM, source_uid) juftligi bir marta keladi
CREATE UNIQUE INDEX IF NOT EXISTS uq_ub_univer_source
    ON university_building (university_code, source_uid)
    WHERE source_uid IS NOT NULL AND deleted_at IS NULL;

-- Cadastre raqami unique per LIVING building (soft-delete uyg'unligi)
-- Bino soft-delete bo'lsa, kadastr boshqa binoga (qayta foydalaniladigan kadastr) biriktirilishi mumkin
CREATE UNIQUE INDEX IF NOT EXISTS uq_ub_cad_number
    ON university_building (cad_number)
    WHERE cad_number IS NOT NULL AND deleted_at IS NULL;

-- =====================================================
-- 5. building_lifecycle — immutable event log
-- =====================================================
-- Pattern: ImmutableEntity (faqat created_at/by)
-- Purpose: Tarixni yo'qotmaslik — ta'mir, yiqilish, qaytadan maqsadiga o'zgartirish
-- Trigger: building.last_renovation_date yangilansa, avtomatik RENOVATED event yoziladi (service layer)
-- =====================================================
CREATE TABLE IF NOT EXISTS building_lifecycle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- ON DELETE RESTRICT (defense-in-depth) — bino tarixi yo'qolmasligi kerak (immutable log).
    -- Loyiha qoidasi: bino soft-delete'd; lifecycle row physical delete'siz arxiv sifatida saqlanadi.
    building_id UUID NOT NULL
        REFERENCES university_building(id) ON DELETE RESTRICT,

    -- Event turi (CHECK enum — kengayuvchi bo'lsa yangi migration kerak)
    event_type VARCHAR(30) NOT NULL CHECK (event_type IN (
        'CONSTRUCTED',     -- Qurildi
        'RENOVATED',       -- Ta'mirlandi
        'EXPANDED',        -- Kengaytirildi
        'REPURPOSED',      -- Kategoriya o'zgartirildi (dorm → academic)
        'CLOSED',          -- Yopildi (vaqtincha)
        'REOPENED',        -- Qayta ochildi
        'DEMOLISHED'       -- Yiqib tashlandi
    )),
    event_date DATE NOT NULL,

    -- REPURPOSED holat uchun
    previous_category_code VARCHAR(20) REFERENCES h_building_category(code),
    new_category_code VARCHAR(20) REFERENCES h_building_category(code),

    -- Moliyaviy ma'lumot (ta'mir xarajati)
    cost NUMERIC(15,2),
    decree_number VARCHAR(100),
    decree_date DATE,

    note TEXT,

    -- Immutable audit (update/delete ruxsat etilmaydi — rollback/rewrite yo'q)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),

    -- Data integrity
    CONSTRAINT chk_bl_event_date CHECK (event_date <= CURRENT_DATE),
    CONSTRAINT chk_bl_decree_date CHECK
        (decree_date IS NULL OR decree_date <= event_date),
    CONSTRAINT chk_bl_cost CHECK (cost IS NULL OR cost >= 0),
    CONSTRAINT chk_bl_repurposed CHECK (
        event_type != 'REPURPOSED' OR
        (previous_category_code IS NOT NULL
         AND new_category_code IS NOT NULL
         AND previous_category_code != new_category_code)
    )
);

COMMENT ON TABLE building_lifecycle IS
    'Immutable event log for building history: construction, renovation, closure, demolition.
     Fills automatically from building.last_renovation_date updates (via @EventListener).
     No UPDATE/DELETE allowed at application layer — audit integrity.';

CREATE INDEX IF NOT EXISTS idx_bl_building      ON building_lifecycle(building_id);
CREATE INDEX IF NOT EXISTS idx_bl_event_type    ON building_lifecycle(event_type);
CREATE INDEX IF NOT EXISTS idx_bl_date          ON building_lifecycle(event_date DESC);
CREATE INDEX IF NOT EXISTS idx_bl_building_date ON building_lifecycle(building_id, event_date DESC);
