-- =====================================================
-- V025: university_cadastre — kadastr huquqiy snapshot (172.18.9.171/kadastr/by-cadnum)
-- =====================================================
-- Author: hemis-team
-- Purpose: Bino (operatsion) qatlamiga qo'shimcha HUQUQIY qatlam. Kadastr obyekti (yer/bino)
--          egalar/maydon/qiymat/huquqiy holat — cad_number bo'yicha BIR MARTA (umumiy fakt).
--          university_building.cad_number YUMSHOQ bog'lanadi (qattiq FK YO'Q — bino kadastrsiz
--          ham yashaydi; API o'lik bo'lsa saqlash bloklanmaydi).
-- Oqim: /kadastr/by-inn {tin} -> cadastr_list (cad_number'lar) -> har biriga /by-cadnum -> shu jadval.
-- Chidamlilik: fetch_status (COMPLETE/PENDING/FAILED) + xom `raw` JSONB (hech narsa yo'qolmaydi) + retry.
-- Maydonlar: 172.18.9.171/kadastr/by-cadnum JONLI javobidan (2026-08-19 tasdiqlangan).
-- Idempotent: CREATE IF NOT EXISTS.
-- =====================================================

CREATE TABLE IF NOT EXISTS university_cadastre (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Identifikatsiya (cad_number = umumiy kalit, BIR MARTA)
    cad_number      VARCHAR(50) NOT NULL,
    cad_number_old  VARCHAR(50),
    name            VARCHAR(500),
    data_source     VARCHAR(30),                 -- masalan "1C"
    response_id     BIGINT,

    -- Joylashuv (kadastr API'ning o'z raqamlash tizimi — SOATO EMAS)
    region_id       INTEGER,
    region          VARCHAR(255),
    district_id     INTEGER,
    district        VARCHAR(255),
    address         TEXT,
    short_address   VARCHAR(500),
    street          VARCHAR(500),
    street_code     VARCHAR(50),
    dom_num         VARCHAR(50),
    kvartira_num    VARCHAR(50),
    neighborhood    VARCHAR(255),
    neighborhood_id VARCHAR(50),

    -- Obyekt turi (tip/vid — kadastr API kod + matn)
    tip             VARCHAR(10),
    tip_text        VARCHAR(500),                -- masalan "Yakka tartibdagi uy-joy"
    vid             VARCHAR(10),
    vid_text        VARCHAR(500),
    object_rooms    INTEGER,

    -- Yer maydoni (m²) — 7 kategoriya (API: land_area + _i/_b/_f/_z/_d/_u)
    land_area       NUMERIC(14,2),
    land_area_i     NUMERIC(14,2),               -- sug'oriladigan
    land_area_b     NUMERIC(14,2),               -- bino ostidagi (footprint)
    land_area_f     NUMERIC(14,2),               -- ozod
    land_area_z     NUMERIC(14,2),               -- zaxira
    land_area_d     NUMERIC(14,2),               -- yo'l/kirish
    land_area_u     NUMERIC(14,2),               -- foydalaniladigan

    -- Obyekt (bino) maydoni (m²)
    object_area     NUMERIC(14,2),
    object_area_l   NUMERIC(14,2),               -- yashash (yotoqxona uchun muhim)
    object_area_u   NUMERIC(14,2),               -- foydalaniladigan

    -- Qiymat (API string qaytaradi -> BIGINT)
    cost            BIGINT,

    -- Huquqiy holat
    ban_is          BOOLEAN DEFAULT false,       -- cheklov mavjudmi (API "0"/"1")
    eco_zone        VARCHAR(50),
    land_fund_type      VARCHAR(100),
    land_use_type       VARCHAR(100),
    land_fund_category  VARCHAR(100),

    -- Nested (egalar, hujjatlar, cheklovlar) — hisobot uchun JSONB
    subjects        JSONB,                       -- [{type,name,inn,percent,pinfl}] — EGALAR
    documents       JSONB,                       -- [{type,num,owner,date}]
    documents_l     JSONB,
    bans            JSONB,

    -- To'liq xom javob (hech narsa yo'qolmaydi; keyin parser kengaysa shundan olamiz)
    raw             JSONB,

    -- Fetch chidamlilik
    fetch_status        VARCHAR(20) NOT NULL DEFAULT 'COMPLETE',  -- COMPLETE / PENDING / FAILED
    fetch_error         VARCHAR(500),
    last_fetch_attempt  TIMESTAMP,
    synced_at           TIMESTAMP,

    -- Audit (AuditableEntityNoSoftDelete — snapshot, soft-delete YO'Q)
    version         INTEGER DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50) DEFAULT 'system',
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(50),

    CONSTRAINT chk_ucad_status CHECK (fetch_status IN ('COMPLETE','PENDING','FAILED'))
);

COMMENT ON TABLE university_cadastre IS
    'Kadastr huquqiy snapshot (172.18.9.171/kadastr/by-cadnum). cad_number bo''yicha BIR MARTA (umumiy fakt). university_building.cad_number yumshoq bog''lanadi. `raw` = to''liq xom javob.';

-- cad_number = umumiy noyob (bir jismoniy mulk = bir yozuv). PER-OTM emas: bir binoni bir nechta OTM
-- ijaraga olsa ham kadastr fakti bitta; OTM bog'lanish university_building'da (per-OTM).
CREATE UNIQUE INDEX IF NOT EXISTS uq_ucad_cad_number ON university_cadastre(cad_number);
CREATE INDEX IF NOT EXISTS idx_ucad_region  ON university_cadastre(region_id) WHERE region_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ucad_status  ON university_cadastre(fetch_status);
CREATE INDEX IF NOT EXISTS idx_ucad_subjects ON university_cadastre USING GIN(subjects);
