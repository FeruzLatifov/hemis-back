-- =====================================================
-- S020: SEED OTM API CLIENT (oauth_client) ADMIN — permission + role grant + menu + i18n
-- =====================================================
-- Author: hemis-team
-- Purpose: RBAC + sidebar entry for the OTM API-client admin screen
--          (OAuthClientController, /api/v1/web/admin/oauth-clients; FE /system/oauth-clients).
--          Manages OAuth2 client_credentials machine accounts (oauth_client) for the Univer API —
--          SEPARATE from the user-admin (users table). Tables already exist (V006); seed only.
-- Permissions: oauth-clients.view, oauth-clients.manage (category ADMIN — no new PermissionAction).
-- Roles: SUPER_ADMIN + MINISTRY_ADMIN -> both.
-- Menu: sys-oauth-clients under 'system' parent (10000000-...-000000000009), order 8
--       (after sys-outbox=7). Permission oauth-clients.view.
-- i18n: system_message 'OTM API clients' (menu category, 4 langs); menu i18n_key = message_key.
-- Idempotent: ON CONFLICT. Runs after S006 (_seed_msg), S011 (system parent).
-- =====================================================

-- 1. Permissions
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('oauth-clients', 'view', 'oauth-clients.view', 'View OTM API clients',
        'View OTM/organization API machine accounts', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('oauth-clients', 'manage', 'oauth-clients.manage', 'Manage OTM API clients',
        'Create, edit, delete OTM/organization API machine accounts', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- 2. Role grants (SUPER_ADMIN + MINISTRY_ADMIN -> both)
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code IN ('SUPER_ADMIN', 'MINISTRY_ADMIN')
  AND p.code IN ('oauth-clients.view', 'oauth-clients.manage')
ON CONFLICT DO NOTHING;

-- 3. Menu (system parent, order 6)
INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at, menu_type)
VALUES (
    '20000009-0000-0000-0000-000000000008',
    'sys-oauth-clients',
    'Integration accounts',
    '/system/oauth-clients',
    'key-round',
    'oauth-clients.view',
    8,
    true,
    '10000000-0000-0000-0000-000000000009',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'main'
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    is_active = EXCLUDED.is_active,
    menu_type = EXCLUDED.menu_type,
    updated_at = CURRENT_TIMESTAMP;

-- 4. Menu label i18n (uz-UZ / oz-UZ / ru-RU; en-US = message_key). message_key = menu i18n_key.
DO $$ BEGIN
  PERFORM _seed_msg('menu', 'Integration accounts',
                    'Integratsiya hisoblari',
                    'Integratsiya hisoblari',
                    'Учётные записи интеграций');
END $$;
