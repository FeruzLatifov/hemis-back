-- Rollback M010: h_building_category + category ustunlarini tiklash (V010 shakli).
-- Ma'lumot tiklanmaydi (M010'дан oldin 0 bino edi) — faqat struktura + 6 seed.
-- category_code NULLABLE tiklanadi (V023 holati — V010'даги NOT NULL emas).

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

ALTER TABLE university_building ADD COLUMN IF NOT EXISTS category_code VARCHAR(20)
    REFERENCES h_building_category(code) ON DELETE RESTRICT;
CREATE INDEX IF NOT EXISTS idx_ub_category ON university_building(category_code);

ALTER TABLE building_lifecycle ADD COLUMN IF NOT EXISTS previous_category_code VARCHAR(20) REFERENCES h_building_category(code);
ALTER TABLE building_lifecycle ADD COLUMN IF NOT EXISTS new_category_code VARCHAR(20) REFERENCES h_building_category(code);
ALTER TABLE building_lifecycle ADD CONSTRAINT chk_bl_repurposed CHECK (
    event_type != 'REPURPOSED' OR
    (previous_category_code IS NOT NULL AND new_category_code IS NOT NULL
     AND previous_category_code != new_category_code)
);
