-- =====================================================
-- S007: SEED CLASSIFIER MENU ENTRIES
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-15
-- Purpose: Add Financial, Diploma, Speciality category
--          menus under the Classifiers parent menu
-- Parent: classifiers (10000000-0000-0000-0000-000000000008)
-- Existing children: 7 (order 1-7; cls-organizational removed 2026-08-08)
-- New children: 3 (order 8-10)
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE SET)
-- Moved from: S010_seed_classifier_menus.sql
-- =====================================================

-- cls-financial (order 8)
INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000009',
    'cls-financial',
    'Financial',
    '/classifiers/financial',
    'credit-card',
    'classifiers.view',
    8,
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

-- cls-diploma (order 9)
INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000010',
    'cls-diploma',
    'Diploma',
    '/classifiers/diploma',
    'award',
    'classifiers.view',
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

-- cls-speciality (order 10)
INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000011',
    'cls-speciality',
    'Specialities',
    '/classifiers/speciality',
    'book-open',
    'classifiers.view',
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

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    cls_menu_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO cls_menu_count
    FROM menu
    WHERE parent_id = '10000000-0000-0000-0000-000000000008'
      AND deleted_at IS NULL;
    RAISE NOTICE 'S007: Total classifier sub-menus: %', cls_menu_count;
END $$;
