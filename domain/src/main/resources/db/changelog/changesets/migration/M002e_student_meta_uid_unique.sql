-- ═══════════════════════════════════════════════════════════════════
-- M002e: hemishe_e_student_meta — (u_id, _university) race-fix UNIQUE
--
-- StudentMetaService.create generates max(u_id)+1 then INSERT — two
-- concurrent transactions can read the same max → both insert with same
-- u_id within the same university. Partial UNIQUE allows reuse after
-- soft-delete (legitimate transfer scenarios).
--
-- DEFENSIVE: master.yaml preCondition checks no existing collisions.
-- If collisions exist → MARK_RAN. FROZEN schema policy → no data writes.
--
-- CONCURRENTLY ⇒ online build.
-- ═══════════════════════════════════════════════════════════════════

CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_student_meta_uid_university_active
    ON hemishe_e_student_meta (u_id, "_university")
    WHERE delete_ts IS NULL;

COMMENT ON INDEX uq_student_meta_uid_university_active IS
    'Per-(u_id, university) uniqueness — prevents concurrent-insert duplicate.';
