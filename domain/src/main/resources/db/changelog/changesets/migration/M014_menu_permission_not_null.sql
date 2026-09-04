-- =====================================================
-- M014: menu.permission NOT NULL (deny by default for navigation)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-28
-- Purpose: MenuService.hasPermission(null, ...) returns TRUE, so a menu row whose permission is NULL is
--          visible to EVERY authenticated user - while the page behind it is still gated by its
--          @PreAuthorize. That mismatch shipped once (S011 'dashboard', repaired in S038) and produced
--          the "menu is there, the API answers 403" bug. This constraint turns the convention - every
--          menu row names the permission its page requires - into an invariant the database enforces,
--          so the hole cannot be re-opened by the next menu seed.
-- Safety: fails loudly, naming the offending rows, instead of silently leaving a permissionless entry;
--         a broken seed is then caught at deploy time rather than by a user clicking a dead menu item.
-- Idempotent: SET NOT NULL on an already NOT NULL column is a no-op.
-- Rollback: DROP NOT NULL.
-- =====================================================

DO $$
DECLARE
    offenders TEXT;
BEGIN
    SELECT string_agg(code, ', ' ORDER BY code) INTO offenders
      FROM menu
     WHERE permission IS NULL;

    IF offenders IS NOT NULL THEN
        RAISE EXCEPTION USING
            MESSAGE = format('M014: menu row(s) without a permission: %s', offenders),
            HINT    = 'Every menu row must name the permission its page requires (deny by default). '
                      'Add the grant/permission in an S### seed, set menu.permission, then re-run.';
    END IF;
END $$;

ALTER TABLE menu ALTER COLUMN permission SET NOT NULL;
