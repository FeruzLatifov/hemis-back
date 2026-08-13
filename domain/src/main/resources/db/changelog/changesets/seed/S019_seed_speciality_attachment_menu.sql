-- =====================================================
-- S019: SEED SPECIALITY-ATTACHMENT MENU ENTRY
-- =====================================================
-- Author: hemis-team
-- Purpose: /institutions/speciality-attachments menu entry — the review card for the
--          MODERN university_speciality_attachment registry (SpecialityAttachmentController,
--          web GET /api/v1/web/registry/speciality-attachments; 2026-2027 data seeded
--          by S018). Distinct from the CUBA-legacy 'University specialities'
--          (inst-attached-specialities → hemishe_e_university_attached_speciality).
-- Parent: institutions (10000000-0000-0000-0000-000000000002)
-- order_number: 8 (after inst-blank-distribution=7)
-- Permission: institutions.speciality-attachments.view (S016 already seeded + granted)
-- i18n: system_message 'Assigned specialities' (menu category, 4 langs) — the menu
--       i18n_key equals system_message.message_key so the menu API resolves the label.
-- Idempotent: ON CONFLICT (code) DO UPDATE + _seed_msg ON CONFLICT.
-- =====================================================

INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at, menu_type)
VALUES (
    '20000002-0000-0000-0000-000000000008',
    'inst-speciality-attachments',
    'Assigned specialities',
    '/institutions/speciality-attachments',
    'graduation-cap',
    'institutions.speciality-attachments.view',
    8,
    true,
    '10000000-0000-0000-0000-000000000002',
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

-- Menu label (uz-UZ / oz-UZ / ru-RU; en-US = message_key). message_key = menu i18n_key.
DO $$ BEGIN
  PERFORM _seed_msg('menu', 'Assigned specialities',
                    'Biriktirilgan mutaxassisliklar',
                    'Бириктирилган мутахассисликлар',
                    'Прикреплённые специальности');
END $$;
