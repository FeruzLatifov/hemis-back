-- =====================================================
-- S033: SEED TRANSLATIONS — Role templates (one-click presets) for the permission editor
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-25
-- Purpose:
--   The role -> permissions editor (RoleFormPage "Permissions" tab) gains a row of one-click
--   role templates (Salesforce Profiles / AWS managed-policy style): applying a template REPLACES
--   the current selection with a ready profile, which the admin then fine-tunes per chip.
--     • Template labels:  Operator (view+create+edit+…), Approver (view+edit+approve).
--                         "View only" and "Full access" reuse S032 keys — NOT re-seeded here.
--     • Row label:        "Role template".
--     • Grant tooltips:   one line each for Operator / Approver
--                         (viewer + full reuse S032 "Views data (read-only)" / "Full control (all actions)").
--     • View-density:     Compact / Detailed — the editor's chip-vs-checkbox+description toggle.
--     • Role-first layout: Custom selection + "Advanced: fine-tune permissions" — templates are
--                          the primary cards; the granular list collapses behind an Advanced disclosure.
--   Total: 9 NEW keys.
--
--   NEW seed because S006/S010/S032 are already applied in production (central_hemis) — applied
--   changesets are never edited. system_message is the source of truth; `sync:translations`
--   regenerates the frontend JSON (en/oz/ru/uz) from here, so without this seed a future sync
--   would drop these keys and the editor would show raw English keys.
-- Pattern: S032 (5-arg _seed_msg helper, persistent — defined in S006; en-US = key).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                 category   key(en)                          uz                                   oz                                    ru
-- Role-template labels
PERFORM _seed_msg('label',  'Operator',                       'Operator',                          'Оператор',                           'Оператор');
PERFORM _seed_msg('label',  'Approver',                       'Tasdiqlovchi',                      'Тасдиқловчи',                        'Утверждающий');
PERFORM _seed_msg('label',  'Role template',                  'Tayyor shablon',                    'Тайёр шаблон',                       'Шаблон роли');

-- Grant tooltips (what a template gives)
PERFORM _seed_msg('label',  'View, create & edit records',    'Ko''rish, yaratish va tahrirlash',  'Кўриш, яратиш ва таҳрирлаш',          'Просмотр, создание и редактирование');
PERFORM _seed_msg('label',  'View, edit & approve records',   'Ko''rish, tahrirlash va tasdiqlash','Кўриш, таҳрирлаш ва тасдиқлаш',       'Просмотр, редактирование и утверждение');

-- View-density toggle (Detailed = checkbox + description [GitLab style]; Compact = colour chips)
PERFORM _seed_msg('action', 'Compact',                        'Ixcham',                            'Ихчам',                              'Компактно');
PERFORM _seed_msg('action', 'Detailed',                       'Izohli',                            'Изоҳли',                             'Подробно');

-- Role-first layout (Google/GCP style): templates promoted to primary cards; the granular
-- per-permission list moves behind an "Advanced" disclosure (collapsed by default).
PERFORM _seed_msg('label',  'Custom selection',                'Maxsus sozlama',                    'Махсус созлама',                     'Особый набор');
PERFORM _seed_msg('label',  'Advanced: fine-tune permissions', 'Ilg''or: ruxsatlarni aniq sozlash', 'Илғор: рухсатларни аниқ созлаш',      'Расширенно: точная настройка прав');

END $$;
