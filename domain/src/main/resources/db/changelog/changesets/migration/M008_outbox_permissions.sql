-- =====================================================
-- M008: OUTBOX ADMIN PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-19
-- Purpose: outbox.view (list/inspect) + outbox.manage (retry/discard)
-- Schema: permission (resource, action, code, name, ...) — modern naming.
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('outbox', 'view', 'outbox.view', 'View Outbox', 'Outbox event ro''yxati va tafsilotini ko''rish', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('outbox', 'manage', 'outbox.manage', 'Manage Outbox', 'Manual retry/discard (DLQ resolution)', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- SUPER_ADMIN: barchasi
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
  FROM role r
 CROSS JOIN permission p
 WHERE r.code = 'SUPER_ADMIN'
   AND p.code IN ('outbox.view', 'outbox.manage')
ON CONFLICT DO NOTHING;

-- MINISTRY_ADMIN: faqat view (manage SUPER only — Kafka topic'ga ta'sir qiladi)
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
  FROM role r
 CROSS JOIN permission p
 WHERE r.code = 'MINISTRY_ADMIN'
   AND p.code = 'outbox.view'
ON CONFLICT DO NOTHING;
