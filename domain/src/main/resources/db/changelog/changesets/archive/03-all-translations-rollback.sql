-- =====================================================
-- V3 ROLLBACK: Remove All Translations
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Rollback V3 translations
--
-- WARNING: This will DELETE all i18n translations!
-- - All menu translations
-- - All faculty registry translations
-- - All action translations
-- - All details translations
-- =====================================================

-- Delete all translations (cascade will handle translations)
DELETE FROM system_message_translations
WHERE message_id IN (
    SELECT id FROM system_messages
    WHERE category IN ('menu', 'table', 'actions', 'details')
);

-- Delete all messages
DELETE FROM system_messages
WHERE category IN ('menu', 'table', 'actions', 'details');

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ V3 translations rolled back successfully';
    RAISE NOTICE '   All menu, table, actions, and details translations removed';
END $$;
