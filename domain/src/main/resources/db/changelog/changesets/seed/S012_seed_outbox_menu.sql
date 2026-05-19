-- =====================================================
-- S012: SEED OUTBOX MENU ENTRY
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-19
-- Purpose: /system/outbox menu entry (OutboxAdminController admin UI).
-- Parent: system (10000000-0000-0000-0000-000000000009)
-- order_number: 7 (after webhooks=6)
-- Permission: outbox.view (M008)
-- =====================================================

INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at, menu_type)
VALUES (
    '20000009-0000-0000-0000-000000000007',
    'sys-outbox',
    'Outbox Queue',
    '/system/outbox',
    'inbox',
    'outbox.view',
    7,
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
