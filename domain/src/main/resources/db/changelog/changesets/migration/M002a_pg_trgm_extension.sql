-- ═══════════════════════════════════════════════════════════════════
-- M002a: pg_trgm extension (prerequisite for GIN trigram indexes)
--
-- M002 monolith split — each subsequent M002b-e uses CONCURRENTLY
-- (transaction-incompatible). This changeset isolates the only
-- transaction-safe DDL.
-- ═══════════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS pg_trgm;
