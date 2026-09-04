-- =====================================================
-- M015: permission.action — allow 'restore'
-- =====================================================
-- Author: hemis-team
-- Date: 2026-09-04
--
-- WHY: `universities.restore` (seeded by S038, which runs immediately after this migration) is the
--      first permission whose action is neither a read nor one of the ten verbs V002 froze into
--      chk_permission_action. Restoring a soft-deleted row is deliberately NOT `delete`: the two
--      differ in what they risk — one hides a row, the other returns it — so the recycle bin can be
--      opened to a role that must not be able to empty the registry, and vice versa.
--
-- THE THREE-PLACE COUPLING (miss one and login breaks, not the seed):
--      1. this CHECK constraint          — otherwise the S038 INSERT fails with 23514,
--      2. enum PermissionAction.RESTORE  — otherwise PermissionActionConverter.valueOf() throws
--                                          IllegalArgumentException while loading the holder's
--                                          permissions, i.e. HTTP 500 on sign-in for exactly the
--                                          users who were granted it,
--      3. Permission.isWritePermission() — otherwise a state-changing action is classified read-only.
--      All three ship in this changeset's commit.
--
-- ORDER: registered DIRECTLY BEFORE S038. S038 inserts the permission row; if the constraint still
--        held the old ten-verb list the seed would abort and take the whole deploy with it.
--
-- V002 IS APPLIED IN PRODUCTION and is never edited — hence a new migration that swaps the
-- constraint rather than a change to the table definition.
--
-- DATA SAFETY: verified on the live database — 118 permission rows, 0 of them with an `action`
-- outside the ten V002 verbs. The new CHECK is therefore a pure widening: every existing row
-- already satisfies it, and the ALTER validates without rewriting the table.
--
-- IDEMPOTENT: DROP CONSTRAINT IF EXISTS + ADD; a re-run replaces the constraint with the same
-- definition. NOT NULL VALID by default, so PostgreSQL scans the (tiny) table once.
--
-- LOCK: ALTER TABLE takes ACCESS EXCLUSIVE. `permission` is ~118 rows and read on every login, so
-- the scan is instant, but a lock_timeout keeps a deploy from queueing behind a long reader and
-- blocking every sign-in for the duration.
-- =====================================================

SET LOCAL lock_timeout = '3s';

ALTER TABLE permission DROP CONSTRAINT IF EXISTS chk_permission_action;

ALTER TABLE permission ADD CONSTRAINT chk_permission_action CHECK (
    action IN ('view', 'create', 'edit', 'delete', 'export', 'import', 'manage', 'access', 'sync',
               'approve', 'restore')
);

COMMENT ON COLUMN permission.action IS
    'Permission verb, lowercase. Mirrors enum PermissionAction and chk_permission_action; adding a value means editing BOTH plus Permission.isWritePermission() (M015).';
