-- =====================================================
-- V010: UNIVERSITY INFO — organization + university_legal + university_profile
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-23
-- Purpose: University info extension tables
--   TABLE 1: organization — yuridik shaxslar registri (TIN UNIQUE)
--   TABLE 2: university_legal — soliq/ro'yxat API dan sync
--   TABLE 3: university_profile — aloqa, ijtimoiy tarmoq, hujjatlar (admin/univer kiritadi)
-- =====================================================

-- =====================================================
-- TABLE 1: organization — yuridik shaxslar registri
-- =====================================================
-- TIN UNIQUE = bitta tashkilot = bitta yozuv
-- university_founder (legal), shartnomalar, hamkorlar — hammasi shu jadvalga FK
-- Pattern: employee (PINFL UNIQUE) bilan analogik
-- World equivalent: SAP Business Partner (type=ORG), PeopleSoft VENDOR
-- =====================================================
CREATE TABLE organization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tin VARCHAR(20) NOT NULL UNIQUE,
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
    CONSTRAINT chk_organization_email CHECK (email IS NULL OR email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$')
);

COMMENT ON TABLE organization IS 'Legal entity registry — one record per TIN. Analogous to employee (PINFL UNIQUE) for individuals.';
COMMENT ON COLUMN organization.tin IS 'STIR — unique tax identification number (9 digits in UZ)';
COMMENT ON COLUMN organization.opf IS 'Organizational-legal form code (OPF classifier)';
COMMENT ON COLUMN organization.source IS 'Record origin: api_legal | manual';
COMMENT ON COLUMN organization.api_raw_response IS 'Raw JSON snapshot from legalentity API (full response)';

CREATE INDEX idx_organization_name ON organization(name);
CREATE INDEX idx_organization_deleted_at ON organization(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_organization_synced_at ON organization(synced_at) WHERE synced_at IS NOT NULL;
CREATE INDEX idx_organization_source ON organization(source) WHERE source IS NOT NULL;

-- =====================================================
-- TABLE 2: university_legal — universitet yuridik ma'lumotlari
-- =====================================================
-- 1:1 with hemishe_e_university
-- Tashqi API (172.18.9.171/legalentity/) dan sync qilinadi
-- =====================================================
CREATE TABLE university_legal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code VARCHAR(255) NOT NULL UNIQUE REFERENCES hemishe_e_university(code),

    -- FK to organization registry (TIN UNIQUE). tin kept as API snapshot for historical data.
    organization_id UUID REFERENCES organization(id),

    -- Company info
    short_name VARCHAR(500),
    opf INTEGER,
    kfs INTEGER,
    tin VARCHAR(20),
    oked VARCHAR(20),
    soogu VARCHAR(20),
    soogu_registrator VARCHAR(20),
    registration_date DATE,
    registration_number VARCHAR(100),
    reregistration_date DATE,
    status INTEGER DEFAULT 0,
    status_updated DATE,
    vat_number BIGINT,
    tax_mode INTEGER,
    taxpayer_type INTEGER,
    business_type INTEGER,
    business_fund BIGINT,
    business_structure INTEGER,
    avg_employees INTEGER,

    -- Billing address (flat columns for efficient queries)
    billing_country_code INTEGER,
    billing_soato VARCHAR(20),
    billing_street TEXT,
    billing_postcode VARCHAR(20),
    billing_cadastre VARCHAR(50),

    -- Shipping addresses (JSONB array)
    shipping_addresses JSONB,

    -- Director & Accountant
    -- employee_id = asosiy bog'lanish (PINFL orqali sync da topiladi)
    -- pinfl = fallback (employee da topilmasa, API snapshot sifatida)
    -- name/phone/email alohida SAQLANMAYDI — employee jadvalidan JOIN orqali olinadi
    director_employee_id UUID REFERENCES employee(id),
    accountant_employee_id UUID REFERENCES employee(id),

    -- Bank accounts
    bank_accounts JSONB,

    -- API sync metadata
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
    CONSTRAINT chk_ulegal_status CHECK (status BETWEEN 0 AND 9),
    CONSTRAINT chk_ulegal_avg_employees CHECK (avg_employees IS NULL OR avg_employees >= 0),
    CONSTRAINT chk_ulegal_tax_mode CHECK (tax_mode IS NULL OR (tax_mode >= 0 AND tax_mode <= 99)),
    CONSTRAINT chk_ulegal_taxpayer_type CHECK (taxpayer_type IS NULL OR (taxpayer_type >= 0 AND taxpayer_type <= 99)),
    CONSTRAINT chk_ulegal_business_type CHECK (business_type IS NULL OR (business_type >= 0 AND business_type <= 99)),
    CONSTRAINT chk_ulegal_business_structure CHECK (business_structure IS NULL OR (business_structure >= 0 AND business_structure <= 99)),
    CONSTRAINT chk_ulegal_billing_country CHECK (billing_country_code IS NULL OR (billing_country_code >= 0 AND billing_country_code <= 999))
);

COMMENT ON TABLE university_legal IS 'University legal entity info from tax/registration API (1:1 with university)';
COMMENT ON COLUMN university_legal.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN university_legal.organization_id IS 'FK to organization (TIN UNIQUE registry). tin column kept as API snapshot.';
COMMENT ON COLUMN university_legal.status IS 'Status code: 0=INACTIVE, 1=ACTIVE, 2=SUSPENDED, 3=LIQUIDATED, 4=UNDER_REORGANIZATION';
COMMENT ON COLUMN university_legal.tax_mode IS 'Tax regime code (0-99). Classifier reference TBD.';
COMMENT ON COLUMN university_legal.taxpayer_type IS 'Taxpayer category code (0-99).';
COMMENT ON COLUMN university_legal.business_type IS 'Business activity type code (0-99).';
COMMENT ON COLUMN university_legal.business_structure IS 'Organizational structure code (0-99).';

CREATE INDEX idx_ulegal_tin ON university_legal(tin) WHERE tin IS NOT NULL;
CREATE INDEX idx_ulegal_organization ON university_legal(organization_id) WHERE organization_id IS NOT NULL;
CREATE INDEX idx_ulegal_director_emp ON university_legal(director_employee_id) WHERE director_employee_id IS NOT NULL;
CREATE INDEX idx_ulegal_accountant_emp ON university_legal(accountant_employee_id) WHERE accountant_employee_id IS NOT NULL;
CREATE INDEX idx_ulegal_billing_soato ON university_legal(billing_soato) WHERE billing_soato IS NOT NULL;
CREATE INDEX idx_ulegal_deleted_at ON university_legal(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_ulegal_synced_at ON university_legal(synced_at);

COMMENT ON COLUMN university_legal.director_employee_id IS 'Optional FK to employee — linked by PINFL during sync. NULL if person not in employee table.';
COMMENT ON COLUMN university_legal.accountant_employee_id IS 'Optional FK to employee — linked by PINFL during sync. NULL if person not in employee table.';

-- =====================================================
-- TABLE 3: university_profile — aloqa, ijtimoiy tarmoq, hujjatlar
-- =====================================================
-- 1:1 with hemishe_e_university
-- Admin yoki univer (230 ta universitet) tomonidan kiritiladi/yangilanadi
-- Fayl saqlash: MinIO (S3-compatible), bazada faqat metadata (file_key)
-- =====================================================
CREATE TABLE university_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code VARCHAR(255) NOT NULL UNIQUE REFERENCES hemishe_e_university(code),

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
    CONSTRAINT chk_uprofile_email CHECK (email IS NULL OR email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$')
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

CREATE INDEX idx_uprofile_source ON university_profile(source) WHERE source IS NOT NULL;
CREATE INDEX idx_uprofile_geo ON university_profile(latitude, longitude) WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
CREATE INDEX idx_uprofile_deleted_at ON university_profile(deleted_at) WHERE deleted_at IS NULL;
