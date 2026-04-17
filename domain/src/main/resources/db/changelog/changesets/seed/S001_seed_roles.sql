-- =====================================================
-- S001: SEED CORE ROLES
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Updated: 2026-03-21 — Role names aligned with old-hemis
-- Purpose: Bootstrap 7 core system roles
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE)
--
-- Old-hemis mapping:
--   OTM (919 perms)         → OTM_API
--   Ministry (420 perms)    → MINISTRY_ADMIN
--   Inspeksiya (166 perms)  → INSPECTOR
--   Student/Xodim/Hokimiyat → EXTERNAL_API
--   Administrators          → SUPER_ADMIN
-- =====================================================

-- Role 1: SUPER_ADMIN — Full system access (old-hemis: Administrators)
INSERT INTO role (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'SUPER_ADMIN',
    'Super Administrator',
    'Full system access — All permissions. Ministry level administration.',
    'SYSTEM',
    TRUE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    role_type = EXCLUDED.role_type,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Role 2: OTM_API — B2B sync for universities (old-hemis: OTM)
-- Univer loyihasi shu rol orqali REST API ga kiradi
INSERT INTO role (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'OTM_API',
    'OTM API',
    'B2B sync for universities — Full CRUD on students, teachers, departments. Old-hemis OTM role equivalent (919 permissions).',
    'UNIVERSITY',
    TRUE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    role_type = EXCLUDED.role_type,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Role 3: MINISTRY_ADMIN — Ministry staff (old-hemis: Ministry + Kadr Vazirlik)
INSERT INTO role (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'MINISTRY_ADMIN',
    'Vazirlik Administrator',
    'Ministry-level administrator — Can view all universities, manage reports, edit classifiers.',
    'SYSTEM',
    TRUE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    role_type = EXCLUDED.role_type,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Role 4: INSPECTOR — Inspection/audit (old-hemis: Inspeksiya)
INSERT INTO role (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'INSPECTOR',
    'Inspeksiya',
    'Inspection and audit — Read-only access to all universities for monitoring and compliance.',
    'SYSTEM',
    TRUE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    role_type = EXCLUDED.role_type,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Role 5: VIEWER — Read-only (old-hemis: Ministry_lite + vazirlikrole)
INSERT INTO role (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'VIEWER',
    'Vazirlik Ko''ruvchi',
    'Read-only access — Can only view data across all universities, no modifications.',
    'SYSTEM',
    TRUE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    role_type = EXCLUDED.role_type,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Role 6: REPORT_VIEWER — Reports only (old-hemis: ReportAdmin)
INSERT INTO role (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'REPORT_VIEWER',
    'Hisobot Ko''ruvchi',
    'Can view and export reports, rating, and institutional data. For statisticians and analysts.',
    'CUSTOM',
    TRUE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    role_type = EXCLUDED.role_type,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Role 7: EXTERNAL_API — External system integration (old-hemis: Student API, Xodim API, Hokimiyat)
INSERT INTO role (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'EXTERNAL_API',
    'Tashqi API',
    'B2B integration for external systems — Limited REST API access (Hokimiyat, Student API, Xodim API).',
    'SYSTEM',
    TRUE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    role_type = EXCLUDED.role_type,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Remove old UNIVERSITY_ADMIN if exists (renamed to OTM_API)
UPDATE role SET
    code = 'OTM_API',
    name = 'OTM API',
    description = 'B2B sync for universities — Full CRUD. Old-hemis OTM role equivalent.',
    role_type = 'UNIVERSITY',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE code = 'UNIVERSITY_ADMIN';

-- Verification
DO $$
DECLARE
    role_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO role_count FROM role WHERE active = TRUE AND deleted_at IS NULL;
    IF role_count < 7 THEN
        RAISE WARNING 'S001: Expected 7 roles, found %', role_count;
    END IF;
    RAISE NOTICE 'S001: % active roles seeded successfully', role_count;
END $$;
