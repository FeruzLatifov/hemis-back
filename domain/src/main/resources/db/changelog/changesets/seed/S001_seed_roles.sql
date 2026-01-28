-- =====================================================
-- S001: SEED CORE ROLES
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Bootstrap 5 core system roles
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE)
-- =====================================================

-- Role 1: SUPER_ADMIN
INSERT INTO roles (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'SUPER_ADMIN',
    'Super Administrator',
    'Full system access - All permissions - Ministry level administration',
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

-- Role 2: MINISTRY_ADMIN
INSERT INTO roles (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'MINISTRY_ADMIN',
    'Ministry Administrator',
    'Ministry-level administrator - Can view all universities, manage reports',
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

-- Role 3: UNIVERSITY_ADMIN
INSERT INTO roles (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'UNIVERSITY_ADMIN',
    'University Administrator',
    'University-level administrator - Manage own university data (students, teachers)',
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

-- Role 4: VIEWER
INSERT INTO roles (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'VIEWER',
    'Read-only Viewer',
    'Read-only access - Can only view data, no modifications',
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

-- Role 5: REPORT_VIEWER
INSERT INTO roles (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'REPORT_VIEWER',
    'Report Viewer',
    'Can view and generate reports - For statisticians and analysts',
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

-- Verification
DO $$
DECLARE
    role_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO role_count FROM roles WHERE active = TRUE;
    IF role_count < 5 THEN
        RAISE EXCEPTION 'S001 Failed: Expected 5 roles, found %', role_count;
    END IF;
    RAISE NOTICE 'S001: % active roles seeded successfully', role_count;
END $$;
