-- =====================================================
-- S004: SEED ROLE-PERMISSION MAPPINGS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Assign permissions to roles
-- Strategy: IDEMPOTENT (ON CONFLICT DO NOTHING for junction)
-- =====================================================

-- =====================================================
-- SUPER_ADMIN: All permissions
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- =====================================================
-- MINISTRY_ADMIN: Core + University view permissions
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MINISTRY_ADMIN'
  AND p.category = 'CORE'
ON CONFLICT DO NOTHING;

-- =====================================================
-- UNIVERSITY_ADMIN: Core permissions (own university scope)
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'UNIVERSITY_ADMIN'
  AND p.category = 'CORE'
  AND p.resource IN ('dashboard', 'students', 'teachers', 'reports')
ON CONFLICT DO NOTHING;

-- =====================================================
-- VIEWER: View-only permissions
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.action = 'view'
ON CONFLICT DO NOTHING;

-- =====================================================
-- REPORT_VIEWER: Reports permissions only
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'REPORT_VIEWER'
  AND p.resource = 'reports'
ON CONFLICT DO NOTHING;

-- Verification
DO $$
DECLARE
    mapping_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO mapping_count FROM role_permissions;
    RAISE NOTICE 'S004: % role-permission mappings created', mapping_count;
END $$;
