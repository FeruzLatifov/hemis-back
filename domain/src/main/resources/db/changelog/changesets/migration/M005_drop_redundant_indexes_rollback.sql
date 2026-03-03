-- =====================================================
-- Rollback M005: Recreate redundant indexes
-- =====================================================
-- Note: These indexes are technically redundant (UNIQUE constraints
-- already provide identical B-tree indexes), but recreated here
-- for clean rollback.
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_roles_code ON roles(code);
CREATE INDEX IF NOT EXISTS idx_permissions_code ON permissions(code);
CREATE INDEX IF NOT EXISTS idx_system_messages_key ON system_messages(message_key);
CREATE INDEX IF NOT EXISTS idx_smt_message_language ON system_message_translations(message_id, language);
CREATE INDEX IF NOT EXISTS idx_languages_code ON languages(code);
