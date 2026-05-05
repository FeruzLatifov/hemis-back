-- =====================================================
-- V005: UNIVERSITY INFO — organization + university_profile
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-23
-- Purpose: University info extension tables
--   TABLE 1: organization — yuridik shaxslar registri (TIN UNIQUE)
--   TABLE 2: university_profile — aloqa, ijtimoiy tarmoq, hujjatlar (admin/univer kiritadi)
--
-- Self-contained: only own tables, no cross-file ALTER. Auth (V006) references
-- organization(id) via INLINE FK from oauth_client.
--
-- Depends on: legacy hemishe_e_university.
-- =====================================================

-- =====================================================
-- TABLE 1: organization — yuridik shaxslar registri
-- =====================================================
-- TIN UNIQUE = bitta tashkilot = bitta yozuv
-- university_founder (legal), shartnomalar, hamkorlar — hammasi shu jadvalga FK
-- Pattern: employee (PINFL UNIQUE) bilan analogik
-- World equivalent: SAP Business Partner (type=ORG), PeopleSoft VENDOR
-- =====================================================
CREATE TABLE IF NOT EXISTS organization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tin VARCHAR(20) NOT NULL,  -- Partial UNIQUE pastda (soft-delete uyg'unligi)
    name VARCHAR(500) NOT NULL,
    short_name VARCHAR(255),
    opf INTEGER,                      -- tashkiliy-huquqiy shakli
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(255),

    -- Sync
    source VARCHAR(50) DEFAULT 'api_legal',
    api_raw_response JSONB,
    synced_at TIMESTAMP,

    -- Audit (AuditableEntity: 7)
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    -- Data integrity
    CONSTRAINT chk_organization_source CHECK (source IN ('api_legal', 'manual')),
    CONSTRAINT chk_organization_email CHECK (email IS NULL OR email ~* '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$')
);

COMMENT ON TABLE organization IS 'Legal entity registry — one record per TIN. Analogous to employee (PINFL UNIQUE) for individuals.';
COMMENT ON COLUMN organization.tin IS 'STIR — unique tax identification number (9 digits in UZ)';
COMMENT ON COLUMN organization.opf IS 'Organizational-legal form code (OPF classifier)';
COMMENT ON COLUMN organization.source IS 'Record origin: api_legal | manual';
COMMENT ON COLUMN organization.api_raw_response IS 'Raw JSON snapshot from legalentity API (full response)';

CREATE INDEX IF NOT EXISTS idx_organization_name      ON organization(name);
CREATE INDEX IF NOT EXISTS idx_organization_synced_at ON organization(synced_at) WHERE synced_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_organization_source    ON organization(source) WHERE source IS NOT NULL;
-- NOTE: `idx_organization_deleted_at ON (deleted_at) WHERE deleted_at IS NULL` removed (always empty).

-- TIN globally unique per LIVING legal entity.
-- Partial UNIQUE: soft-deleted tashkilot TIN'ini qayta ishlatish mumkin (re-registration).
CREATE UNIQUE INDEX IF NOT EXISTS uq_organization_tin
    ON organization(tin)
    WHERE deleted_at IS NULL;

-- =====================================================
-- TABLE 2: university_profile — aloqa, ijtimoiy tarmoq, hujjatlar
-- =====================================================
-- 1:1 with hemishe_e_university
-- Admin yoki univer (230 ta universitet) tomonidan kiritiladi/yangilanadi
-- Fayl saqlash: MinIO (S3-compatible), bazada faqat metadata (file_key)
-- =====================================================
CREATE TABLE IF NOT EXISTS university_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- ON DELETE CASCADE: universitet o'chirilsa profile ham avtomatik o'chadi.
    -- UNIQUE inline'dan olib tashlandi — partial UNIQUE (pastda) soft-delete'da
    -- university_code'ni qayta yaratish imkonini beradi (1:1 active per uni).
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code) ON DELETE CASCADE,

    -- Aloqa
    phone VARCHAR(50),
    email VARCHAR(255),

    -- Ijtimoiy tarmoqlar (JSONB — moslashuvchan, migration keraksiz)
    -- {"website":"https://...","telegram":"https://t.me/...","instagram":"...","youtube":"...","facebook":"..."}
    social_links JSONB,

    -- Branding
    logo_key VARCHAR(500),       -- MinIO object key (universities/{code}/logo.png)
    description TEXT,

    -- Hujjatlar (JSONB massiv — litsenziya, akkreditatsiya, ustav)
    -- [{"type":"LICENSE","name":"Litsenziya.PDF","file_key":"universities/401/license.pdf",
    --   "mime_type":"application/pdf","size":1234567,
    --   "valid_from":"2024-01-01","valid_to":"2029-01-01","uploaded_at":"2026-04-13"}]
    documents JSONB,

    -- Xaritadagi joylashuv (Yandex/Google Maps — aniq manzil + navigatsiya)
    map_url TEXT,                             -- foydalanuvchi paste qiladi (Google/Yandex/OSM link)
    latitude NUMERIC(10, 7),                  -- -90 .. 90 (WGS84)
    longitude NUMERIC(10, 7),                 -- -180 .. 180 (WGS84)

    -- Sync (univer dan kelganda)
    source VARCHAR(50) DEFAULT 'manual',  -- manual, hemis_sync
    source_uid VARCHAR(255),
    hash VARCHAR(64),

    -- Audit (AuditableEntity: 7)
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    CONSTRAINT chk_uprofile_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT chk_uprofile_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180)),
    CONSTRAINT chk_uprofile_source CHECK (source IS NULL OR source IN ('manual', 'hemis_sync')),
    CONSTRAINT chk_uprofile_email CHECK (email IS NULL OR email ~* '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$')
);

COMMENT ON TABLE university_profile IS 'University public profile — contacts, social media, documents, map location.';
COMMENT ON COLUMN university_profile.social_links IS 'JSONB — flexible social media links. New platform = new key, no migration needed.';
COMMENT ON COLUMN university_profile.documents IS 'JSONB array — license, accreditation, charter PDFs. File stored in MinIO, metadata here.';
COMMENT ON COLUMN university_profile.logo_key IS 'MinIO object key for university logo image.';
COMMENT ON COLUMN university_profile.map_url IS 'External map link (Google/Yandex Maps) — pasted by admin.';
COMMENT ON COLUMN university_profile.latitude IS 'WGS84 latitude. Extracted from map_url or entered manually.';
COMMENT ON COLUMN university_profile.longitude IS 'WGS84 longitude. Extracted from map_url or entered manually.';
COMMENT ON COLUMN university_profile.source IS 'Record origin: manual | hemis_sync';
COMMENT ON COLUMN university_profile.source_uid IS 'External UID (e.g. univer.uz record ID)';
COMMENT ON COLUMN university_profile.hash IS 'SHA-256 content hash (hex, 64 chars) — change detection';

CREATE INDEX IF NOT EXISTS idx_uprofile_source ON university_profile(source) WHERE source IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_uprofile_geo    ON university_profile(latitude, longitude) WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
-- NOTE: `idx_uprofile_deleted_at ON (deleted_at) WHERE deleted_at IS NULL` removed (always empty).

-- Partial UNIQUE — 1:1 active record per university (soft-delete-aware).
CREATE UNIQUE INDEX IF NOT EXISTS uq_uprofile_university_code_active
    ON university_profile(university_code)
    WHERE deleted_at IS NULL;
