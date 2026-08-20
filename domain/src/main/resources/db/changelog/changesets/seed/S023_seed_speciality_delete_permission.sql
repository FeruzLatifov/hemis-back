-- =====================================================
-- S023: SEED SPECIALITY CLASSIFIER DELETE PERMISSION (permission + grants)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-20
-- Purpose: Access wiring for DELETE /api/v1/web/classifiers/speciality/{id}
--          (@PreAuthorize('classifiers.speciality.delete')). The endpoint only ever removes a
--          NEEDS_REVIEW leaf that no OTM is attached to — SPECIALITY_DELETE_APPROVED_FORBIDDEN /
--          SPECIALITY_HAS_CHILDREN_DELETE_FIRST / SPECIALITY_ATTACHED_TO_UNIVERSITY (422) are
--          enforced in HSpecialityService; this seed only opens the door for the curation roles.
--          NEW seed because S016 is already applied in production (central_hemis) —
--          applied changesets are never edited.
-- Idempotent: ON CONFLICT (permission upsert, grant DO NOTHING).
-- =====================================================

-- ---------- Permission ----------
-- NOTE: action='delete' is permitted by V002's chk_permission_action.
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES
    ('classifiers.speciality', 'delete', 'classifiers.speciality.delete', 'Delete Speciality Classifier', 'Delete a NEEDS_REVIEW speciality classifier row (no sub-directions, not attached to any OTM)', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- ---------- Grants ----------
-- EXACTLY the S016 .create/.approve set: deleting a classifier row is ministry curation.
-- Granted EXPLICITLY — deliberately NOT grafted onto classifiers.edit holders (the S016
-- "preserve" pattern), so machine roles never inherit it: S004 grants OTM_API classifier
-- VIEW only, and OTMs stay read-only consumers of the distribution.
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code IN ('SUPER_ADMIN', 'MINISTRY_ADMIN', 'CLASSIFIER_MANAGER')
  AND p.code = 'classifiers.speciality.delete'
ON CONFLICT DO NOTHING;
