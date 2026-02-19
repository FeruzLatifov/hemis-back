-- =====================================================
-- S009_seed_classifier_permissions_rollback.sql
-- =====================================================

-- Remove role assignments
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permissions
    WHERE code IN (
        'classifiers.edit',
        'classifiers.financial.view',
        'classifiers.diploma.view',
        'classifiers.speciality.view'
    )
);

-- Remove permissions
DELETE FROM permissions
WHERE code IN (
    'classifiers.edit',
    'classifiers.financial.view',
    'classifiers.diploma.view',
    'classifiers.speciality.view'
);
