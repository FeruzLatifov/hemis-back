-- ═══════════════════════════════════════════════════════════════════
-- M002d: hemishe_e_student_diploma — substring search optimization
--
-- findByDiplomaNumberContainingIgnoreCase uses
--   LOWER(diploma_number) LIKE '%X%'
-- (leading wildcard = full table scan). GIN trigram brings this from
-- ~5s to ~50ms on 1M+ rows.
--
-- CONCURRENTLY ⇒ online build.
-- ═══════════════════════════════════════════════════════════════════

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_diploma_number_trgm
    ON hemishe_e_student_diploma
    USING GIN (LOWER(diploma_number) gin_trgm_ops);

COMMENT ON INDEX idx_diploma_number_trgm IS
    'pg_trgm GIN — substring search on diploma_number (LIKE %X%).';
