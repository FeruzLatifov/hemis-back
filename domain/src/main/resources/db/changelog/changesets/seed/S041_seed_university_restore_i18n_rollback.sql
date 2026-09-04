-- =====================================================
-- S041 ROLLBACK: remove ONLY the keys S041 introduced
-- =====================================================
-- NOT listed, on purpose — they belong to earlier changesets and are only reused here:
--   • 'Deleted'  — S039 (the audit-log action chip).
--   • 'Restore'  — S036 (the recycle-bin action, reused as the `restore` chip label).
-- system_message_translation rows go with them (FK ON DELETE CASCADE, V011).
DELETE FROM system_message
 WHERE message_key IN ('Deleted items',
                       'Restores a deleted record',
                       'Grants permission');
