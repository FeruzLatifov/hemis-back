-- =====================================================
-- V4 ROLLBACK: Delete All Translations
-- =====================================================

DELETE FROM h_system_message_translation;
DELETE FROM h_system_message WHERE category = 'menu';
