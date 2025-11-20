-- =====================================================
-- V11 ROLLBACK: Delete Languages Seed Data
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20
-- Description: Rollback changeset v11 (delete seed data)
--
-- Warning: This will delete all default language records!
-- =====================================================

-- Delete all seed languages
DELETE FROM public.languages
WHERE code IN ('uz-UZ', 'oz-UZ', 'ru-RU', 'en-US');

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Languages seed data deleted successfully (rollback complete)';
END $$;
