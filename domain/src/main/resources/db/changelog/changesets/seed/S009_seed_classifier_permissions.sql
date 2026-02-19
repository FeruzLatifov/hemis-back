-- =====================================================
-- S009: SEED CLASSIFIER EDIT + NEW CATEGORY PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2026-02-14
-- Purpose: Add classifiers.edit permission and new
--          category permissions (financial, diploma, speciality)
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE SET)
-- =====================================================

-- =====================================================
-- STEP 1: New permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
-- Edit permission for CRUD operations
('classifiers', 'edit', 'classifiers.edit', 'Edit Classifiers', 'Create, update, delete classifier items', 'CORE', 'system'),
-- New category view permissions
('classifiers.financial', 'view', 'classifiers.financial.view', 'View Financial Classifiers', 'View financial classifiers', 'CORE', 'system'),
('classifiers.diploma', 'view', 'classifiers.diploma.view', 'View Diploma Classifiers', 'View diploma classifiers', 'CORE', 'system'),
('classifiers.speciality', 'view', 'classifiers.speciality.view', 'View Speciality Classifiers', 'View speciality classifiers', 'CORE', 'system')
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
    'classifiers.edit',
    'classifiers.financial.view',
    'classifiers.diploma.view',
    'classifiers.speciality.view'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    cls_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO cls_count FROM permissions WHERE code LIKE 'classifiers.%';
    RAISE NOTICE 'S009: Total classifier permissions: %', cls_count;
END $$;
