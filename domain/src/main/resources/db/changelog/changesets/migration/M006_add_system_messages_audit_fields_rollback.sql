-- =====================================================
-- Rollback M006: Remove audit fields from system_messages
-- =====================================================

ALTER TABLE system_messages DROP COLUMN IF EXISTS created_by;
ALTER TABLE system_messages DROP COLUMN IF EXISTS updated_by;
