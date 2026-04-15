-- =====================================================
-- V011: FOUNDER MODULE — university_founder
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-25
-- Purpose: University founders/shareholders
-- Pattern: 1:N with hemishe_e_university
-- Individual → employee_id FK, Legal → legal_tin + legal_name
-- =====================================================

CREATE TABLE university_founder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code),

    -- Founder type
    founder_type VARCHAR(20) NOT NULL CHECK (founder_type IN ('individual', 'legal')),

    -- Individual founder (jismoniy shaxs)
    -- employee_id = asosiy bog'lanish (sync da PINFL bo'yicha topiladi yoki yaratiladi)
    -- pinfl = fallback (employee da topilmasa, API snapshot sifatida)
    -- name/tin alohida SAQLANMAYDI — employee jadvalidan JOIN orqali olinadi
    employee_id UUID REFERENCES employee(id),

    -- Legal founder (yuridik shaxs → organization FK)
    organization_id UUID REFERENCES organization(id),

    -- Share info
    share_percent NUMERIC(5,2),
    share_sum BIGINT,

    -- Historical tracking
    is_current BOOLEAN NOT NULL DEFAULT true,
    effective_from DATE,
    effective_to DATE,

    -- Audit
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

COMMENT ON TABLE university_founder IS 'University founders — individual (→ employee) or legal (→ organization)';
COMMENT ON COLUMN university_founder.employee_id IS 'FK to employee — individual founder. PINFL-based lookup during sync.';
COMMENT ON COLUMN university_founder.organization_id IS 'FK to organization — legal founder. TIN-based lookup during sync.';
COMMENT ON COLUMN university_founder.version IS 'Optimistic locking version (JPA @Version)';

CREATE INDEX idx_ufounder_university ON university_founder(university_code);
CREATE INDEX idx_ufounder_employee ON university_founder(employee_id) WHERE employee_id IS NOT NULL;
CREATE INDEX idx_ufounder_org ON university_founder(organization_id) WHERE organization_id IS NOT NULL;
CREATE INDEX idx_ufounder_current ON university_founder(university_code, is_current) WHERE is_current = true;
