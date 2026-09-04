-- =====================================================
-- M015 ROLLBACK: permission.action back to the V002 ten-verb list
-- =====================================================
-- Liquibase rolls back in EXECUTION order, so S038 — registered (and therefore executed) after this
-- migration — is undone first and takes its `universities.restore` row with it. runOnChange only
-- pushes S038's orderexecuted further ahead, never behind, so that ordering holds.
--
-- Still, the constraint is re-narrowed here, and re-narrowing it while a row already violates it
-- fails with an opaque 23514 pointing at a table rather than at the cause. The guard below names
-- the offending codes and says what to do about them, the way M014 does for menu.permission.
-- Deleting them silently is NOT an option: a permission row that roles are granted is data.
-- =====================================================

SET LOCAL lock_timeout = '3s';

DO $$
DECLARE
    offenders TEXT;
BEGIN
    SELECT string_agg(code, ', ' ORDER BY code) INTO offenders
      FROM permission
     WHERE action = 'restore';

    IF offenders IS NOT NULL THEN
        RAISE EXCEPTION USING
            MESSAGE = format('M015 rollback stopped: permission row(s) with action = ''restore'' still exist: %s', offenders),
            HINT    = 'Roll back the seed that created them (S038) first, or revoke and delete those '
                      'permission rows by hand. Re-narrowing the CHECK while they exist would fail '
                      'with a bare 23514.';
    END IF;
END $$;

ALTER TABLE permission DROP CONSTRAINT IF EXISTS chk_permission_action;

ALTER TABLE permission ADD CONSTRAINT chk_permission_action CHECK (
    action IN ('view', 'create', 'edit', 'delete', 'export', 'import', 'manage', 'access', 'sync', 'approve')
);

-- V002 shipped the column without a comment; M015 added one, so it goes too.
COMMENT ON COLUMN permission.action IS NULL;
