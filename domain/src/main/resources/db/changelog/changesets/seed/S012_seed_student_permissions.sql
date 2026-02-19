-- =====================================================
-- S012: SEED STUDENT WEB UI PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2026-02-16
-- Purpose: Add students.view and students.edit permissions
--          for the new Web UI Students page
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE SET)
-- =====================================================

-- =====================================================
-- STEP 1: New permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('students', 'view', 'students.view', 'View Students', 'View student list and details in Web UI', 'CORE', 'system'),
('students', 'edit', 'students.edit', 'Edit Students', 'Create, update, delete students in Web UI', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 2: Assign new permissions to SUPER_ADMIN
-- =====================================================

INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN (
    'students.view',
    'students.edit'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    perm_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO perm_count FROM permissions WHERE code LIKE 'students.%';
    RAISE NOTICE 'S012: Total student permissions: %', perm_count;
END $$;
