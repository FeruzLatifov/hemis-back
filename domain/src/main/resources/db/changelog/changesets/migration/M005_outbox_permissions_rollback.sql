-- =====================================================
-- M005 rollback
-- =====================================================
DELETE FROM role_permission WHERE permission_id IN (
    SELECT id FROM permission WHERE code IN ('outbox.view', 'outbox.manage')
);
DELETE FROM permission WHERE code IN ('outbox.view', 'outbox.manage');
