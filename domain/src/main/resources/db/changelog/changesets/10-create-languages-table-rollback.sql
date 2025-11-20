-- =====================================================
-- V10 ROLLBACK: Drop Languages Table
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20
-- Description: Rollback changeset v10 (drop languages table)
--
-- Warning: This will delete all language configuration data!
-- =====================================================

-- Drop trigger first
DROP TRIGGER IF EXISTS trigger_languages_updated_at ON public.languages;

-- Drop function
DROP FUNCTION IF EXISTS update_languages_updated_at();

-- Drop indexes (will be dropped automatically with table, but explicit for clarity)
DROP INDEX IF EXISTS public.idx_languages_active;
DROP INDEX IF EXISTS public.idx_languages_default;
DROP INDEX IF EXISTS public.idx_languages_deleted_at;

-- Drop table
DROP TABLE IF EXISTS public.languages CASCADE;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Languages table dropped successfully (rollback complete)';
END $$;
