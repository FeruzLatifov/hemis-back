-- =====================================================
-- V023: modern h_building_type classifier + university_building.building_type_code FK
-- =====================================================
-- Author: hemis-team
-- Purpose: "Bino turi" endi har Univer'ning lokal vaqtinchalik jadvalidan emas, markaziy
--          klassifikatordan kelsin. Modern `h_building_type` (ReferenceEntity: code PK,
--          name/name_ru/name_en, is_active, sort_order, version, modern audit) — h_education_type
--          (V022) / h_education_form (V021) BIR XIL PATTERN.
--          35 tur, kod 11..45 (vazirlik konvensiyasi: 11'dan boshlab ketma-ket). Nomlar Univer
--          `h_building_type` seed'idan (m260814_100100); RU/EN'ni biz to'ldiramiz.
-- university_building: `building_type_code` FK (-> h_building_type) qo'shiladi; `category_code`
--          NOT NULL yumshatiladi (kadastr/Univer push kategoriya bermaydi — building_type_code beradi).
--          Eski h_building_category (6 kod) saqlanadi (ixtiyoriy coarse rollup).
-- Idempotent: CREATE IF NOT EXISTS, seed ON CONFLICT, ADD COLUMN IF NOT EXISTS, DROP/ADD CONSTRAINT.
-- =====================================================

-- 1. Modern classifier table (ReferenceEntity shape)
CREATE TABLE IF NOT EXISTS h_building_type (
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

COMMENT ON TABLE h_building_type IS
    'Modern "Bino turi" classifier (ministry-authoritative). 35 turlar, kod 11-45. 224 Univer''ga tarqatiladi (distribution keyingi bosqichda); har Univer''ning lokal vaqtinchalik h_building_type (1-35) o''rniga.';

-- 2. Seed 35 building types (uz/ru/en, self-contained). Kod 11..45.
INSERT INTO h_building_type (code, name, name_ru, name_en, sort_order, created_by) VALUES
    ('11', 'O''quv binosi',                                'Учебный корпус',                          'Academic building',                  11, 'system'),
    ('12', 'Ma''muriy bino',                               'Административное здание',                  'Administrative building',            12, 'system'),
    ('13', 'O''quv-ma''muriy bino',                        'Учебно-административное здание',           'Academic-administrative building',   13, 'system'),
    ('14', 'Laboratoriya binosi',                          'Лабораторное здание',                     'Laboratory building',                14, 'system'),
    ('15', 'Ilmiy-tadqiqot binosi',                        'Научно-исследовательское здание',         'Research building',                  15, 'system'),
    ('16', 'O''quv-ishlab chiqarish binosi',               'Учебно-производственное здание',          'Educational-production building',    16, 'system'),
    ('17', 'Ustaxona',                                     'Мастерская',                              'Workshop',                           17, 'system'),
    ('18', 'Axborot-resurs markazi / kutubxona binosi',    'Информационно-ресурсный центр / библиотека','Information-resource center / library',18, 'system'),
    ('19', 'Talabalar turar joyi',                         'Студенческое общежитие',                  'Student residence',                  19, 'system'),
    ('20', 'O''quvchilar turar joyi / yotoqxona',          'Общежитие учащихся',                      'Dormitory',                          20, 'system'),
    ('21', 'Sport zali',                                   'Спортивный зал',                          'Sports hall',                        21, 'system'),
    ('22', 'Sport majmuasi',                               'Спортивный комплекс',                     'Sports complex',                     22, 'system'),
    ('23', 'Yopiq sport inshooti',                         'Крытое спортивное сооружение',            'Indoor sports facility',             23, 'system'),
    ('24', 'Madaniyat saroyi / faollar zali',              'Дворец культуры / актовый зал',           'Culture palace / assembly hall',     24, 'system'),
    ('25', 'Oshxona',                                      'Столовая',                                'Canteen',                            25, 'system'),
    ('26', 'Bufet / ovqatlanish binosi',                   'Буфет / здание питания',                  'Buffet / catering building',         26, 'system'),
    ('27', 'Tibbiyot punkti / tibbiyot binosi',            'Медпункт / медицинское здание',           'Medical point / medical building',   27, 'system'),
    ('28', 'Sog''lomlashtirish majmuasi',                  'Оздоровительный комплекс',                'Wellness complex',                   28, 'system'),
    ('29', 'Dam olish maskani / oromgoh',                  'Место отдыха',                            'Recreation facility',                29, 'system'),
    ('30', 'Mehmonxona / mehmon uyi',                      'Гостиница / гостевой дом',                'Hotel / guest house',                30, 'system'),
    ('31', 'Arxiv binosi',                                 'Архивное здание',                         'Archive building',                   31, 'system'),
    ('32', 'Muzey',                                        'Музей',                                   'Museum',                             32, 'system'),
    ('33', 'Nashriyot / bosmaxona binosi',                 'Издательство / типография',               'Publishing / printing building',     33, 'system'),
    ('34', 'Ishlab chiqarish binosi',                      'Производственное здание',                 'Production building',                34, 'system'),
    ('35', 'Omborxona',                                    'Склад',                                   'Warehouse',                          35, 'system'),
    ('36', 'Garaj / avtotransport binosi',                 'Гараж / автотранспортное здание',         'Garage / vehicle building',          36, 'system'),
    ('37', 'Qozonxona',                                    'Котельная',                               'Boiler house',                       37, 'system'),
    ('38', 'Transformator / elektr ta''minoti binosi',     'Трансформаторная / здание электроснабжения','Transformer / power supply building',38, 'system'),
    ('39', 'Nasos stansiyasi / suv ta''minoti binosi',     'Насосная станция / здание водоснабжения',  'Pump station / water supply building',39, 'system'),
    ('40', 'Issiqlik ta''minoti inshooti',                 'Сооружение теплоснабжения',               'Heat supply facility',               40, 'system'),
    ('41', 'Xo''jalik binosi',                             'Хозяйственное здание',                    'Utility building',                   41, 'system'),
    ('42', 'Qo''riqlash posti / nazorat-o''tkazish punkti','Пост охраны / КПП',                       'Security post / checkpoint',         42, 'system'),
    ('43', 'Sanitar-maishiy bino',                         'Санитарно-бытовое здание',                'Sanitary-domestic building',         43, 'system'),
    ('44', 'Yordamchi texnik bino',                        'Вспомогательное техническое здание',      'Auxiliary technical building',       44, 'system'),
    ('45', 'Boshqa bino',                                  'Другое здание',                           'Other building',                     45, 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, name_ru = EXCLUDED.name_ru, name_en = EXCLUDED.name_en,
    sort_order = EXCLUDED.sort_order, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- 3. university_building: add building_type_code FK + index; relax category_code NOT NULL.
--    (V010 prod'da jonli — additive/idempotent. hemishe_* ga tegilmaydi.)
ALTER TABLE university_building ADD COLUMN IF NOT EXISTS building_type_code VARCHAR(20);

ALTER TABLE university_building DROP CONSTRAINT IF EXISTS fk_ub_building_type;
ALTER TABLE university_building
    ADD CONSTRAINT fk_ub_building_type FOREIGN KEY (building_type_code)
    REFERENCES h_building_type(code) ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_ub_building_type
    ON university_building(building_type_code) WHERE building_type_code IS NOT NULL;

-- Kadastr/Univer push building_type_code beradi, category_code (eski 6-kod) EMAS.
ALTER TABLE university_building ALTER COLUMN category_code DROP NOT NULL;

-- 4. Ko'p-tur (multi-purpose bino): bir bino bir nechta turga tegishli bo'lishi mumkin
--    (masalan ham o'quv, ham ma'muriy). `building_type_code` = ASOSIY tur (ro'yxat/filtr);
--    junction = BARCHA turlar. Hisobot ("necha o'quv binosi") junction bo'yicha aniq.
CREATE TABLE IF NOT EXISTS university_building_type (
    building_id        UUID        NOT NULL REFERENCES university_building(id) ON DELETE CASCADE,
    building_type_code VARCHAR(20) NOT NULL REFERENCES h_building_type(code)  ON DELETE RESTRICT,
    PRIMARY KEY (building_id, building_type_code)
);
CREATE INDEX IF NOT EXISTS idx_ubt_type ON university_building_type(building_type_code);
