-- =====================================================
-- M005: DROP REDUNDANT INDEXES
-- =====================================================
-- Author: hemis-team
-- Date: 2025-02-24
-- Purpose: Remove indexes that duplicate UNIQUE constraints
--
-- PostgreSQL automatically creates B-tree index for every
-- UNIQUE constraint. These manual indexes are redundant and
-- waste disk space + slow down INSERT/UPDATE operations.
-- =====================================================

-- roles.code has UNIQUE constraint -> auto-index exists
DROP INDEX IF EXISTS idx_roles_code;

-- permissions.code has UNIQUE constraint -> auto-index exists
DROP INDEX IF EXISTS idx_permissions_code;

-- system_messages.message_key has UNIQUE constraint -> auto-index exists
DROP INDEX IF EXISTS idx_system_messages_key;

-- system_message_translations (message_id, language) has
-- UNIQUE constraint uq_message_language -> auto-index exists
DROP INDEX IF EXISTS idx_smt_message_language;

-- languages.code has UNIQUE constraint -> auto-index exists
DROP INDEX IF EXISTS idx_languages_code;
