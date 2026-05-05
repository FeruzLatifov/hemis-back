-- =====================================================
-- V017 rollback — recreate university_legal
-- =====================================================
-- Reverses V017_drop_university_legal.sql by re-creating the table with
-- the EXACT shape from V005_create_university_legal.sql.
-- Constraints, indexes, comments preserved 1:1.
-- =====================================================

CREATE TABLE IF NOT EXISTS university_legal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code) ON DELETE CASCADE,
    organization_id UUID REFERENCES organization(id) ON DELETE SET NULL,

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

    billing_country_code INTEGER,
    billing_soato VARCHAR(20) REFERENCES hemishe_h_soato(code),
    billing_street TEXT,
    billing_postcode VARCHAR(20),
    billing_cadastre VARCHAR(50),

    shipping_addresses JSONB,

    director_employee_id   UUID REFERENCES employee(id) ON DELETE SET NULL,
    accountant_employee_id UUID REFERENCES employee(id) ON DELETE SET NULL,

    bank_accounts JSONB,

    api_raw_response JSONB,
    synced_at TIMESTAMP,

    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    CONSTRAINT chk_ulegal_status CHECK (status BETWEEN 0 AND 9),
    CONSTRAINT chk_ulegal_avg_employees CHECK (avg_employees IS NULL OR avg_employees >= 0),
    CONSTRAINT chk_ulegal_tax_mode CHECK (tax_mode IS NULL OR (tax_mode >= 0 AND tax_mode <= 99)),
    CONSTRAINT chk_ulegal_taxpayer_type CHECK (taxpayer_type IS NULL OR (taxpayer_type >= 0 AND taxpayer_type <= 99)),
    CONSTRAINT chk_ulegal_business_type CHECK (business_type IS NULL OR (business_type >= 0 AND business_type <= 99)),
    CONSTRAINT chk_ulegal_business_structure CHECK (business_structure IS NULL OR (business_structure >= 0 AND business_structure <= 99)),
    CONSTRAINT chk_ulegal_billing_country CHECK (billing_country_code IS NULL OR (billing_country_code >= 0 AND billing_country_code <= 999))
);

CREATE INDEX IF NOT EXISTS idx_ulegal_tin            ON university_legal(tin) WHERE tin IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ulegal_organization   ON university_legal(organization_id) WHERE organization_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ulegal_director_emp   ON university_legal(director_employee_id) WHERE director_employee_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ulegal_accountant_emp ON university_legal(accountant_employee_id) WHERE accountant_employee_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ulegal_billing_soato  ON university_legal(billing_soato) WHERE billing_soato IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ulegal_synced_at      ON university_legal(synced_at);

CREATE UNIQUE INDEX IF NOT EXISTS uq_ulegal_university_code_active
    ON university_legal(university_code)
    WHERE deleted_at IS NULL;
