-- =====================================================
-- S012 ROLLBACK: Remove student permissions
-- =====================================================

-- Remove role_permissions assignments
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permissions WHERE code IN ('students.view', 'students.edit')
);

-- Remove permissions
DELETE FROM permissions WHERE code IN ('students.view', 'students.edit');
