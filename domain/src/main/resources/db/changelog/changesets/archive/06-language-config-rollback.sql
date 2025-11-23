-- =====================================================
-- V6 ROLLBACK: Remove Language Configuration
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Rollback V6 language config
--
-- WARNING: This will DELETE language configuration!
-- - All language records (uz-UZ, oz-UZ, ru-RU, en-US)
-- - Languages table and indexes
-- =====================================================

-- Drop languages table (includes all language data)
DROP TABLE IF EXISTS languages CASCADE;

-- Drop triggers and functions
DROP TRIGGER IF EXISTS trigger_languages_updated_at ON languages;
DROP FUNCTION IF EXISTS update_languages_updated_at();

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ V6 language config rolled back successfully';
    RAISE NOTICE '   Languages table and all language data removed';
END $$;
