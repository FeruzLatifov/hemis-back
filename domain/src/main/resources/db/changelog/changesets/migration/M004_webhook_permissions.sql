-- =====================================================
-- M004: WEBHOOK PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-13
-- ADR: docs/adr/0012-webhook-outbound-infrastructure.md
-- Purpose: Webhook admin RBAC permissions + admin role grant.
--
-- 5 ta permission: webhook.view, webhook.create, webhook.update,
--                  webhook.delete, webhook.manage (secret rotation).
--
-- Idempotent: ON CONFLICT DO UPDATE / DO NOTHING.
-- Schema: permission (resource, action, code, name, ...) — modern naming.
-- =====================================================

-- 1. Webhook permission'lar
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('webhook', 'view', 'webhook.view', 'View Webhooks', 'Webhook target ro''yxati va delivery log ko''rish', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('webhook', 'create', 'webhook.create', 'Create Webhook', 'Yangi webhook target qo''shish + secret generate', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('webhook', 'edit', 'webhook.update', 'Update Webhook', 'Webhook URL/config yangilash (partial update)', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('webhook', 'delete', 'webhook.delete', 'Delete Webhook', 'Webhook target soft delete', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('webhook', 'manage', 'webhook.manage', 'Manage Webhook Secret', 'HMAC secret regenerate (Univer .env yangilash zarur)', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- 2. SUPER_ADMIN role'iga barcha webhook permission'larni grant qilish
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
  FROM role r
 CROSS JOIN permission p
 WHERE r.code = 'SUPER_ADMIN'
   AND p.code IN ('webhook.view', 'webhook.create', 'webhook.update', 'webhook.delete', 'webhook.manage')
ON CONFLICT DO NOTHING;

-- 3. MINISTRY_ADMIN role uchun view + create + update (delete va manage SUPER only)
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
  FROM role r
 CROSS JOIN permission p
 WHERE r.code = 'MINISTRY_ADMIN'
   AND p.code IN ('webhook.view', 'webhook.create', 'webhook.update')
ON CONFLICT DO NOTHING;
