-- =====================================================
-- V008: FOUNDER MODULE — university_founder
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-25
-- Purpose: University founders/shareholders
-- Pattern: 1:N with hemishe_e_university
-- Individual → employee_id FK, Legal → legal_tin + legal_name
-- =====================================================

CREATE TABLE university_founder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- ON DELETE CASCADE: universitet o'chirilsa ta'sischilar ham avtomatik o'chadi
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code) ON DELETE CASCADE,

    -- Founder type (stored uppercase to match Java enum STRING serialization)
    founder_type VARCHAR(20) NOT NULL CHECK (founder_type IN ('INDIVIDUAL', 'LEGAL')),

    -- Individual founder (jismoniy shaxs)
    -- employee_id = asosiy bog'lanish (sync da PINFL bo'yicha topiladi yoki yaratiladi)
    -- name/tin alohida SAQLANMAYDI — employee jadvalidan JOIN orqali olinadi
    employee_id UUID REFERENCES employee(id) ON DELETE SET NULL,

    -- Legal founder (yuridik shaxs → organization FK)
    -- ON DELETE SET NULL: tashkilot o'chirilsa founder row qoladi, organization_id NULL bo'ladi
    organization_id UUID REFERENCES organization(id) ON DELETE SET NULL,

    -- Share info
    share_percent NUMERIC(5,2),
    share_sum BIGINT,

    -- Historical tracking
    is_current BOOLEAN NOT NULL DEFAULT true,
    effective_from DATE,
    effective_to DATE,

    -- Audit (AuditableEntity: 7)
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    -- Data integrity: founder_type determines which FK is required
    CONSTRAINT chk_ufounder_xor CHECK (
        (founder_type = 'INDIVIDUAL' AND employee_id IS NOT NULL AND organization_id IS NULL)
        OR
        (founder_type = 'LEGAL'      AND organization_id IS NOT NULL AND employee_id IS NULL)
    ),
    CONSTRAINT chk_ufounder_share_percent CHECK (
        share_percent IS NULL OR (share_percent >= 0 AND share_percent <= 100)
    ),
    CONSTRAINT chk_ufounder_effective_range CHECK (
        effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from
    ),
    CONSTRAINT chk_ufounder_share_sum CHECK (share_sum IS NULL OR share_sum >= 0)
);

COMMENT ON TABLE university_founder IS 'University founders — individual (→ employee) or legal (→ organization)';
COMMENT ON COLUMN university_founder.employee_id IS 'FK to employee — individual founder. PINFL-based lookup during sync.';
COMMENT ON COLUMN university_founder.organization_id IS 'FK to organization — legal founder. TIN-based lookup during sync.';
COMMENT ON COLUMN university_founder.version IS 'Optimistic locking version (JPA @Version)';

CREATE INDEX idx_ufounder_university ON university_founder(university_code);
CREATE INDEX idx_ufounder_employee ON university_founder(employee_id) WHERE employee_id IS NOT NULL;
CREATE INDEX idx_ufounder_org ON university_founder(organization_id) WHERE organization_id IS NOT NULL;
CREATE INDEX idx_ufounder_current ON university_founder(university_code, is_current) WHERE is_current = true;
CREATE INDEX idx_ufounder_deleted_at ON university_founder(deleted_at) WHERE deleted_at IS NULL;

-- Bitta ta'sischi bitta universitet uchun bir vaqtda faqat bitta is_current=true yozuv bo'lishi kerak
CREATE UNIQUE INDEX idx_ufounder_unique_current_individual
    ON university_founder(university_code, employee_id)
    WHERE is_current = true AND deleted_at IS NULL AND founder_type = 'INDIVIDUAL';

CREATE UNIQUE INDEX idx_ufounder_unique_current_legal
    ON university_founder(university_code, organization_id)
    WHERE is_current = true AND deleted_at IS NULL AND founder_type = 'LEGAL';
