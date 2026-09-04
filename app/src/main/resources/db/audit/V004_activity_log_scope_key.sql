-- =====================================================
-- V004: scope_key — owner-scoped history for records that no longer exist
-- =====================================================
-- An OTM↔speciality attachment is a LINK row and is hard-deleted: after a detach there is no row
-- left to open a per-row history on. The durable question is therefore asked of the OWNER —
-- "everything that happened to OTM 301's attachments, detached ones included" — and the audit log
-- is the only place that can answer it.
--
-- A dedicated column rather than a naming convention over entity_name: the query becomes an
-- equality on an indexed (entity_type, scope_key) pair instead of a LIKE over free text, so it stays
-- cheap as the log grows and survives any change to how the human label is composed.
--
-- Nullable on purpose: most audited entities have no meaningful owner (a role, a user, a
-- classifier row), and a NULL costs nothing in the index.
-- =====================================================

ALTER TABLE activity_log ADD COLUMN IF NOT EXISTS scope_key VARCHAR(64);

COMMENT ON COLUMN activity_log.scope_key IS
    'Owner of the audited record (e.g. OTM code) — what an owner-scoped history query filters on';

-- created_at DESC in the index: every scope query is "newest first", so the sort comes for free.
-- CONCURRENTLY because this runs at pod startup against a table the application is writing to: a
-- plain CREATE INDEX takes a SHARE lock and every audit INSERT queues behind it for as long as the
-- build takes. The initialiser applies each statement on an autocommit connection, which is the one
-- requirement CONCURRENTLY has.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_activity_scope
    ON activity_log (entity_type, scope_key, created_at DESC)
    WHERE scope_key IS NOT NULL;
