-- =====================================================
-- V4 ROLLBACK: Delete All Translations
-- =====================================================

DELETE FROM system_message_translations;
DELETE FROM system_messages WHERE category = 'menu';
