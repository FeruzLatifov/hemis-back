-- =====================================================
-- S040 ROLLBACK: remove the university recycle-bin translation keys
-- =====================================================
-- 'Deleted' is NOT listed: it is seeded by S039 (the audit action chip) and only reused here.
DELETE FROM system_message
 WHERE message_key IN ('Deleted universities',
                       'The university is hidden everywhere and can be restored later',
                       'University restored',
                       'Are you sure you want to delete this university? It will be hidden everywhere and can be restored later.');
