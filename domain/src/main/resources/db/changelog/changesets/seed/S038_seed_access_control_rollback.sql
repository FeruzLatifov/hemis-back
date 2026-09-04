-- =====================================================
-- S038 ROLLBACK: restore the pre-S038 access model (the S001/S004 baseline)
-- =====================================================
-- Restores what the seeds defined, not what operators later clicked: grants added by hand in the
-- role editor are not reconstructable from a changeset and do not come back. CLASSIFIER_MANAGER is
-- outside this seed's scope entirely, so it is neither changed nor restored here.
-- =====================================================

-- ---------- Dashboard: the menu row goes back to permission = NULL ----------
-- M014 (menu.permission NOT NULL) normally rolls back first, since it is registered after this
-- seed. It does NOT when this seed was re-executed by runOnChange and therefore carries a higher
-- orderexecuted than M014 — Liquibase rolls back in execution order, not changelog order. Dropping
-- the constraint here (a no-op when M014 already did) makes the rollback order-proof.
ALTER TABLE menu ALTER COLUMN permission DROP NOT NULL;

UPDATE menu
   SET permission = NULL,
       updated_at = CURRENT_TIMESTAMP,
       updated_by = 'system'
 WHERE code = 'dashboard';

-- The dashboard.view grant is NOT withdrawn: S004 hands it to every human role including
-- CLASSIFIER_MANAGER, so removing it here would push the database below its own baseline.


-- ---------- Role code back to MINISTRY_ADMIN (must run FIRST — everything below names it) -------
UPDATE role SET
    code = 'MINISTRY_ADMIN',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
 WHERE code = 'ADMIN' AND deleted_at IS NULL
   AND NOT EXISTS (SELECT 1 FROM role WHERE code = 'MINISTRY_ADMIN' AND deleted_at IS NULL);

-- ---------- classifiers.delete: grants first (FK), then the permission row ----------
DELETE FROM role_permission
WHERE permission_id IN (SELECT id FROM permission WHERE code = 'classifiers.delete');

DELETE FROM permission WHERE code = 'classifiers.delete';

-- ---------- audit.history.view: the third permission this seed creates ----------
-- Was missing here while its two siblings were removed, so a rollback left an orphan permission
-- and its grants behind — and a re-run of the seed would then UPDATE that leftover row instead of
-- inserting a clean one. A rollback must undo everything the seed created, not most of it.
DELETE FROM role_permission
WHERE permission_id IN (SELECT id FROM permission WHERE code = 'audit.history.view');

DELETE FROM permission WHERE code = 'audit.history.view';

-- ---------- universities.restore: grants first (FK), then the permission row ----------
-- Must be a HARD delete, not a soft one: M015 rolls back after this seed (Liquibase unwinds in
-- execution order) and re-narrows chk_permission_action to the ten V002 verbs. A soft-deleted row
-- still carries action = 'restore' and would make that ALTER fail with 23514. M015's rollback
-- guards against exactly this and names the row if it is still here.
DELETE FROM role_permission
WHERE permission_id IN (SELECT id FROM permission WHERE code = 'universities.restore');

DELETE FROM permission WHERE code = 'universities.restore';

-- ---------- MINISTRY_ADMIN: back to the S004 rule (CORE + admin list + every view) ----------
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code = 'MINISTRY_ADMIN' AND r.deleted_at IS NULL AND p.deleted_at IS NULL
  AND (
    p.category = 'CORE'
    OR p.action = 'view'
    OR p.code IN (
      'users.manage', 'users.view', 'users.create', 'users.edit', 'users.delete',
      'roles.manage', 'roles.view', 'roles.create', 'roles.edit', 'roles.delete',
      'permissions.manage', 'permissions.view',
      'system.menu.view', 'system.menus.manage',
      'system.translation.view', 'system.translation.manage',
      'system.users.view', 'system.view',
      'audit.view', 'settings.view', 'settings.edit'
    )
  )
ON CONFLICT DO NOTHING;

-- …and drop what S039 added beyond that rule.
DELETE FROM role_permission rp
USING role r, permission p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND r.code = 'MINISTRY_ADMIN'
  AND p.category <> 'CORE'
  AND p.action <> 'view'
  AND p.code NOT IN (
      'users.manage', 'users.view', 'users.create', 'users.edit', 'users.delete',
      'roles.manage', 'roles.view', 'roles.create', 'roles.edit', 'roles.delete',
      'permissions.manage', 'permissions.view',
      'system.menu.view', 'system.menus.manage',
      'system.translation.view', 'system.translation.manage',
      'system.users.view', 'system.view',
      'audit.view', 'settings.view', 'settings.edit'
  );

-- pinfl.view stays SUPER_ADMIN-only (S004's read-gate; the `p.action = 'view'` clause above
-- would otherwise hand it back to MINISTRY_ADMIN).
DELETE FROM role_permission rp
USING role r, permission p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND p.code = 'pinfl.view'
  AND r.code <> 'SUPER_ADMIN';

-- ---------- TECH_STAFF: the role this seed created goes away with it ----------
DELETE FROM role_permission
 WHERE role_id IN (SELECT id FROM role WHERE code = 'TECH_STAFF');

DELETE FROM user_role
 WHERE role_id IN (SELECT id FROM role WHERE code = 'TECH_STAFF');

DELETE FROM role WHERE code = 'TECH_STAFF';

-- CLASSIFIER_MANAGER is untouched by S039, so there is nothing to restore for it here.

-- ---------- Role identity: back to the S001 wording ----------
UPDATE role SET
    name = 'Vazirlik Administrator',
    description = 'Ministry-level administrator — Can view all universities, manage reports, edit classifiers.',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE code = 'MINISTRY_ADMIN' AND deleted_at IS NULL;

