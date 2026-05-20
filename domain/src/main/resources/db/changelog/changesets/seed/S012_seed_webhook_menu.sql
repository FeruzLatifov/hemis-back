-- =====================================================
-- S012: SEED WEBHOOK MENU ENTRY
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-19
-- Purpose: Add 'sys-webhooks' menu under 'system' parent for
--          frontend /system/webhooks (WebhookTargetController admin UI).
-- Parent: system (10000000-0000-0000-0000-000000000009)
-- Existing children: translations(1), users(2), roles(3), logs(4), report-updates(5)
-- New child: webhooks (6)
-- Permission: webhook.view (M004 already seeded)
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE)
-- =====================================================

INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at, menu_type)
VALUES (
    '20000009-0000-0000-0000-000000000006',
    'sys-webhooks',
    'Webhook Targets',
    '/system/webhooks',
    'webhook',
    'webhook.view',
    6,
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
