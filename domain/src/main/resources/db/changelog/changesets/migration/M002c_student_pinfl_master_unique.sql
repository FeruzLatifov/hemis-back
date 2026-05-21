-- ═══════════════════════════════════════════════════════════════════
-- M002c: PINFL master uniqueness — concurrent-enrollment race fix
--
-- Partial UNIQUE: only one active master record per PINFL.
-- "Duplicate students" (is_duplicate=true) are allowed to keep history.
--
-- DEFENSIVE: master.yaml preCondition checks no existing collisions.
-- If collisions exist → MARK_RAN (skip with warning, cleanup required).
-- FROZEN schema policy (domain/CLAUDE.md): no data writes.
--
-- CONCURRENTLY ⇒ online build, no ACCESS EXCLUSIVE LOCK.
-- ═══════════════════════════════════════════════════════════════════

CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_student_pinfl_master
    ON hemishe_e_student (pinfl)
    WHERE is_duplicate = true AND delete_ts IS NULL;

COMMENT ON INDEX uq_student_pinfl_master IS
    'PINFL master record uniqueness — prevents duplicate master rows on concurrent enrollment.';
