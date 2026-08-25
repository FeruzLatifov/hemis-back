-- =====================================================
-- S032: SEED TRANSLATIONS — Role permission editor (RBAC) redesign
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-24
-- Purpose:
--   New i18n keys for the redesigned role -> permissions editor (RoleFormPage "Permissions" tab):
--   a collapsible domain tree whose rows carry coloured action chips, plus a capability-summary
--   panel. Each permission's `action` verb now renders with a human label + a "what it grants"
--   microcopy, and permissions are grouped by top-level domain.
--     • Action verbs missing until now:  Access / Approve / Sync / Manage.
--     • Domain label:                    Buildings (other domains already seeded in S006/S010).
--     • Grant microcopy (10):            one line per action — what the permission actually allows.
--     • Summary tiers (3):               Full access / Can edit / View only.
--     • UI chrome:                       expand/collapse/clear, empty states, select-count.
--   Keys already present in S006/S010 (View/Create/Edit/Delete/Export/Import, Select all, Clear,
--   Actions, Permissions, the other domain labels, "Search permissions...", "No permissions
--   available") are intentionally NOT re-seeded here.
--
--   NEW seed because S006/S010 are already applied in production (central_hemis) — applied
--   changesets are never edited. system_message is the source of truth; `sync:translations`
--   regenerates the frontend JSON (en/oz/ru/uz) from here, so without this seed a future sync
--   would drop these keys and the editor would show raw English keys.
-- Pattern: S031 (5-arg _seed_msg helper, persistent — defined in S006, not dropped; en-US = key).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                 category    key(en)                             uz                                        oz                                          ru
-- Action verbs (chip labels)
PERFORM _seed_msg('action', 'Access',                            'Kirish',                                  'Кириш',                                    'Доступ');
PERFORM _seed_msg('action', 'Approve',                           'Tasdiqlash',                              'Тасдиқлаш',                                'Утверждение');
PERFORM _seed_msg('action', 'Sync',                              'Sinxronlash',                             'Синхронлаш',                               'Синхронизация');
PERFORM _seed_msg('action', 'Manage',                            'Boshqarish',                              'Бошқариш',                                 'Управление');

-- Domain label (top-level group header)
PERFORM _seed_msg('menu',   'Buildings',                         'Binolar',                                 'Бинолар',                                  'Здания');

-- Grant microcopy (one line per action — "what it grants")
PERFORM _seed_msg('label',  'Views data (read-only)',            'Ma''lumotni faqat o''qiydi',              'Маълумотни фақат ўқийди',                  'Просмотр данных (только чтение)');
PERFORM _seed_msg('label',  'Grants access to the section',      'Bo''limga kirish huquqini beradi',        'Бўлимга кириш ҳуқуқини беради',             'Предоставляет доступ к разделу');
PERFORM _seed_msg('label',  'Adds new records',                  'Yangi yozuv qo''shadi',                   'Янги ёзув қўшади',                         'Добавляет новые записи');
PERFORM _seed_msg('label',  'Edits existing records',            'Mavjud yozuvni o''zgartiradi',            'Мавжуд ёзувни ўзгартиради',                'Изменяет существующие записи');
PERFORM _seed_msg('label',  'Approves & publishes records',      'Yozuvni tasdiqlab kuchga kiritadi',       'Ёзувни тасдиқлаб кучга киритади',          'Утверждает и публикует записи');
PERFORM _seed_msg('label',  'Syncs with external system',        'Tashqi tizim bilan sinxronlaydi',         'Ташқи тизим билан синхронлайди',           'Синхронизирует с внешней системой');
PERFORM _seed_msg('label',  'Imports data from a file',          'Fayldan ma''lumot yuklaydi',              'Файлдан маълумот юклайди',                 'Импортирует данные из файла');
PERFORM _seed_msg('label',  'Exports data to a file',            'Ma''lumotni faylga chiqaradi',            'Маълумотни файлга чиқаради',               'Экспортирует данные в файл');
PERFORM _seed_msg('label',  'Full control (all actions)',        'To''liq boshqaradi (barcha amallar)',     'Тўлиқ бошқаради (барча амаллар)',          'Полный контроль (все действия)');
PERFORM _seed_msg('label',  'Deletes records (irreversible)',    'Yozuvni butunlay o''chiradi (qaytmas)',   'Ёзувни бутунлай ўчиради (қайтмас)',        'Удаляет записи (необратимо)');

-- Capability-summary tiers
PERFORM _seed_msg('label',  'Full access',                       'To''liq huquq',                           'Тўлиқ ҳуқуқ',                              'Полный доступ');
PERFORM _seed_msg('label',  'Can edit',                          'Tahrirlashi mumkin',                      'Таҳрирлаши мумкин',                        'Может редактировать');
PERFORM _seed_msg('label',  'View only',                         'Faqat ko''rish',                          'Фақат кўриш',                              'Только просмотр');

-- UI chrome
PERFORM _seed_msg('action', 'Expand all',                        'Barchasini ochish',                       'Барчасини очиш',                           'Раскрыть все');
PERFORM _seed_msg('action', 'Collapse all',                      'Barchasini yig''ish',                     'Барчасини йиғиш',                          'Свернуть все');
PERFORM _seed_msg('action', 'Clear all',                         'Barchasini tozalash',                     'Барчасини тозалаш',                        'Очистить все');
PERFORM _seed_msg('action', 'Select all view permissions',       'Barcha ko''rish ruxsatlarini tanlash',    'Барча кўриш рухсатларини танлаш',          'Выбрать все права на просмотр');
PERFORM _seed_msg('label',  'No permissions granted',            'Hali ruxsat berilmagan',                  'Ҳали рухсат берилмаган',                   'Права не назначены');
PERFORM _seed_msg('label',  'No permissions match your search',  'Qidiruvga mos ruxsat topilmadi',          'Қидирувга мос рухсат топилмади',           'Нет прав по запросу');
PERFORM _seed_msg('label',  '{{count}} of {{total}} selected',   '{{count}} / {{total}} tanlangan',         '{{count}} / {{total}} танланган',          'Выбрано {{count}} из {{total}}');

END $$;
