-- =====================================================
-- M004 ROLLBACK: WEBHOOK PERMISSIONS
-- =====================================================
-- Webhook permission'lar va role grant'larni olib tashlash.
-- =====================================================

DELETE FROM role_permission
 WHERE permission_id IN (
     SELECT id FROM permission
      WHERE code IN ('webhook.view', 'webhook.create', 'webhook.update', 'webhook.delete', 'webhook.manage')
 );

DELETE FROM permission
 WHERE code IN ('webhook.view', 'webhook.create', 'webhook.update', 'webhook.delete', 'webhook.manage');
