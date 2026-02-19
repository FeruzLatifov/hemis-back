-- =====================================================
-- S010: SEED NEW CLASSIFIER MENU ENTRIES
-- =====================================================
-- Author: hemis-team
-- Date: 2026-02-14
-- Purpose: Add Financial, Diploma, Speciality category
--          menus under the Classifiers parent menu
-- Parent: classifiers (10000000-0000-0000-0000-000000000008)
-- Existing children: 8 (order 1-8)
-- New children: 3 (order 9-11)
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE SET)
-- =====================================================

-- cls-financial (order 9)
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000009',
    'cls-financial',
    'Financial',
    '/classifiers/financial',
    'credit-card',
    'classifiers.financial.view',
    9,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-diploma (order 10)
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000010',
    'cls-diploma',
    'Diploma',
    '/classifiers/diploma',
    'award',
    'classifiers.diploma.view',
    10,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-speciality (order 11)
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000011',
    'cls-speciality',
    'Specialities',
    '/classifiers/speciality',
    'book-open',
    'classifiers.speciality.view',
    11,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    cls_menu_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO cls_menu_count
    FROM menus
    WHERE parent_id = '10000000-0000-0000-0000-000000000008'
      AND deleted_at IS NULL;
    RAISE NOTICE 'S010: Total classifier sub-menus: %', cls_menu_count;
END $$;
