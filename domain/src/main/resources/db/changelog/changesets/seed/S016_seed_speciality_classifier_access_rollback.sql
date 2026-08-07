-- =====================================================
-- S016 ROLLBACK: SPECIALITY CLASSIFIER ACCESS
-- =====================================================
-- Revert the cls-speciality menu to its S007 state, drop grants, drop permissions.
-- =====================================================

UPDATE menu
SET permission = 'classifiers.view',
    i18n_key = 'Specialities',
    icon = 'book-open',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'cls-speciality';

DELETE FROM role_permission
WHERE permission_id IN (
    SELECT id FROM permission WHERE code IN (
        'classifiers.speciality.view', 'classifiers.speciality.edit',
        'classifiers.speciality.create', 'classifiers.speciality.approve',
        'institutions.speciality-attachments.view',
        'institutions.speciality-attachments.create',
        'institutions.speciality-attachments.delete'
    )
);

DELETE FROM permission WHERE code IN (
    'classifiers.speciality.view', 'classifiers.speciality.edit',
    'classifiers.speciality.create', 'classifiers.speciality.approve',
    'institutions.speciality-attachments.view',
    'institutions.speciality-attachments.create',
    'institutions.speciality-attachments.delete'
);
