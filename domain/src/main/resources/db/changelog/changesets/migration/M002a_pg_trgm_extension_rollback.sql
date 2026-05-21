-- pg_trgm intentionally NOT dropped — other tooling (search infra,
-- citext partial matching) may depend on it. Manual drop only if explicitly
-- needed: DROP EXTENSION IF EXISTS pg_trgm CASCADE;
SELECT 1 WHERE FALSE;
