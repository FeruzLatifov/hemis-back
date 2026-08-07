-- =====================================================
-- S016: SEED SPECIALITY CLASSIFIER ACCESS (permissions + grants + menu)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-07-18
-- Purpose: Access wiring for the unified speciality classifier bespoke card
--          (api-web /api/v1/web/classifiers/speciality + /registry/speciality-attachments,
--          frontend /classifiers/speciality). The generic cls-speciality menu (S007) is
--          re-pointed from classifiers.view to the dedicated classifiers.speciality.view
--          so menu visibility matches the bespoke route/@PreAuthorize gate.
-- Idempotent: ON CONFLICT (permissions, grants, menu).
-- =====================================================

-- ---------- Permissions ----------
-- NOTE: action='approve' is permitted by V002's chk_permission_action (folded in there).
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES
    ('classifiers.speciality', 'view',   'classifiers.speciality.view',   'View Speciality Classifier',   'View the unified bachelor/master speciality classifier', 'CORE', 'system'),
    ('classifiers.speciality', 'edit',   'classifiers.speciality.edit',   'Edit Speciality Classifier',   'Curate/promote speciality classifier rows',              'CORE', 'system'),
    ('classifiers.speciality', 'create', 'classifiers.speciality.create', 'Create Speciality Classifier', 'Manually add a new speciality classifier row',           'CORE', 'system'),
    ('classifiers.speciality', 'approve','classifiers.speciality.approve','Approve Speciality Classifier','Promote a speciality NEEDS_REVIEW to APPROVED (triggers OTM distribution)', 'CORE', 'system'),
    ('institutions.speciality-attachments', 'view',   'institutions.speciality-attachments.view',   'View Speciality Attachments',   'View OTM speciality attachments',   'CORE', 'system'),
    ('institutions.speciality-attachments', 'create', 'institutions.speciality-attachments.create', 'Create Speciality Attachment',  'Attach a speciality to an OTM',     'CORE', 'system'),
    ('institutions.speciality-attachments', 'delete', 'institutions.speciality-attachments.delete', 'Delete Speciality Attachment',  'Detach a speciality from an OTM',   'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- ---------- Grants ----------
-- SUPER_ADMIN: all five (S004's CROSS JOIN already ran, so grant the new codes explicitly).
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN ('classifiers.speciality.view', 'classifiers.speciality.edit',
                 'institutions.speciality-attachments.view',
                 'institutions.speciality-attachments.create',
                 'institutions.speciality-attachments.delete')
ON CONFLICT DO NOTHING;

-- Preserve existing visibility: every role that can see classifiers gets the new view perm;
-- every role that can edit classifiers gets the new edit perm.
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT rp.role_id, np.id, 'system'
FROM role_permission rp
JOIN permission cv ON cv.id = rp.permission_id AND cv.code = 'classifiers.view'
CROSS JOIN permission np
WHERE np.code = 'classifiers.speciality.view'
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT rp.role_id, np.id, 'system'
FROM role_permission rp
JOIN permission ce ON ce.id = rp.permission_id AND ce.code = 'classifiers.edit'
CROSS JOIN permission np
WHERE np.code = 'classifiers.speciality.edit'
ON CONFLICT DO NOTHING;

-- Speciality attachments: ministry-managed CRUD → MINISTRY_ADMIN (+ SUPER_ADMIN above).
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code = 'MINISTRY_ADMIN'
  AND p.code IN ('classifiers.speciality.view', 'classifiers.speciality.edit',
                 'institutions.speciality-attachments.view',
                 'institutions.speciality-attachments.create',
                 'institutions.speciality-attachments.delete')
ON CONFLICT DO NOTHING;

-- Create + Approve: ministry curation roles ONLY. Granted EXPLICITLY — NOT via the
-- "classifiers.edit preserve" clause above — so machine roles can neither INSERT new rows
-- (.create) nor promote NEEDS_REVIEW → APPROVED (.approve). OTM distribution is fully sealed
-- on the write side: S004 grants OTM_API classifier VIEW only and revokes every classifier
-- write (incl. classifiers.edit and classifiers.speciality.edit). Because S004 runs first,
-- the preserve clause above finds no classifiers.edit on OTM_API and grafts no
-- speciality.edit onto it — OTMs stay read-only consumers; all curation is web-admin-panel.
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code IN ('SUPER_ADMIN', 'MINISTRY_ADMIN', 'CLASSIFIER_MANAGER')
  AND p.code IN ('classifiers.speciality.create', 'classifiers.speciality.approve')
ON CONFLICT DO NOTHING;

-- ---------- Menu ----------
-- Re-point the existing cls-speciality menu (S007) to the dedicated permission + icon.
UPDATE menu
SET permission = 'classifiers.speciality.view',
    i18n_key = 'Speciality classifier',
    icon = 'graduation-cap',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'cls-speciality';
