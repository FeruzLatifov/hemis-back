-- =====================================================
-- S023 ROLLBACK: SPECIALITY CLASSIFIER DELETE PERMISSION
-- =====================================================
-- Drop the grants first (role_permission FK), then the permission row.
-- =====================================================

DELETE FROM role_permission
WHERE permission_id IN (
    SELECT id FROM permission WHERE code = 'classifiers.speciality.delete'
);

DELETE FROM permission WHERE code = 'classifiers.speciality.delete';
