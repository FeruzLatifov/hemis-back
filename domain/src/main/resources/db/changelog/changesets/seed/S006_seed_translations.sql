-- =====================================================
-- S006: SEED TRANSLATIONS (gettext model)
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Consolidated 2026-02-25)
-- Purpose: ALL i18n translations for the entire application
--
-- KEY CONVENTION (gettext model):
--   message_key = English text (UNIQUE, serves as en-US translation)
--   message     = Uzbek Latin default text
--   Translations: uz-UZ, oz-UZ, ru-RU stored in system_message_translations
--   en-US translation = message_key itself
--
-- CATEGORIES:
--   action     - Button/action labels (Save, Cancel, Delete...)
--   status     - Status labels (Active, Inactive...)
--   label      - Form/UI labels (Name, Code, Date...)
--   message    - User messages (No data found, Loading...)
--   validation - Validation messages (Required, Too short...)
--   table      - Table column headers
--   pagination - Pagination labels
--   confirm    - Confirmation dialogs
--   auth       - Authentication related
--   menu       - Navigation menu items
--   error      - Error messages
--
-- CONSOLIDATED FROM: S006, S007, S008, S011, S013, S014, S015, S017
-- =====================================================

DO $$
BEGIN

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Helper function: insert message + 4 translations in one call
-- Args: category, english_key, uzbek_latin, uzbek_cyrillic, russian
-- The english_key is used as both message_key and en-US translation
-- Defined here (first consumer) and NOT dropped — reused by S009 and any future
-- translation seeds that reference it. CREATE OR REPLACE keeps it idempotent.
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE OR REPLACE FUNCTION _seed_msg(
    _cat TEXT, _key TEXT, _uz TEXT, _oz TEXT, _ru TEXT
) RETURNS VOID AS $fn$
DECLARE _id UUID;
BEGIN
    -- Insert or update the base message (default = Uzbek Latin)
    INSERT INTO system_message (id, category, message_key, message, is_active, created_at, updated_at, created_by, updated_by)
    VALUES (gen_random_uuid(), _cat, _key, _uz, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system')
    ON CONFLICT (message_key) WHERE deleted_at IS NULL DO UPDATE SET
        message = EXCLUDED.message,
        category = EXCLUDED.category,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = 'system'
    RETURNING id INTO _id;

    -- en-US (key = English text itself)
    INSERT INTO system_message_translation (id, message_id, language, translation, created_at)
    VALUES (gen_random_uuid(), _id, 'en-US', _key, CURRENT_TIMESTAMP)
    ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

    -- uz-UZ
    INSERT INTO system_message_translation (id, message_id, language, translation, created_at)
    VALUES (gen_random_uuid(), _id, 'uz-UZ', _uz, CURRENT_TIMESTAMP)
    ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

    -- oz-UZ
    INSERT INTO system_message_translation (id, message_id, language, translation, created_at)
    VALUES (gen_random_uuid(), _id, 'oz-UZ', _oz, CURRENT_TIMESTAMP)
    ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

    -- ru-RU
    INSERT INTO system_message_translation (id, message_id, language, translation, created_at)
    VALUES (gen_random_uuid(), _id, 'ru-RU', _ru, CURRENT_TIMESTAMP)
    ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;
END;
$fn$ LANGUAGE plpgsql;


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 1: COMMON UI TRANSLATIONS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- =====================================================
-- Actions (buttons, toolbar actions)
-- =====================================================
--                    category   key(en)              uz                    oz                     ru
PERFORM _seed_msg('action', 'Save',                'Saqlash',            'Сақлаш',              'Сохранить');
PERFORM _seed_msg('action', 'Cancel',              'Bekor qilish',       'Бекор қилиш',         'Отмена');
PERFORM _seed_msg('action', 'Delete',              'O''chirish',         'Ўчириш',              'Удалить');
PERFORM _seed_msg('action', 'Edit',                'Tahrirlash',         'Таҳрирлаш',           'Редактировать');
PERFORM _seed_msg('action', 'Add',                 'Qo''shish',          'Қўшиш',               'Добавить');
PERFORM _seed_msg('action', 'Create',              'Yaratish',           'Яратиш',              'Создать');
PERFORM _seed_msg('action', 'Update',              'Yangilash',          'Янгилаш',             'Обновить');
PERFORM _seed_msg('action', 'Search',              'Qidirish',           'Қидириш',             'Поиск');
PERFORM _seed_msg('action', 'Search...',           'Qidirish...',        'Қидириш...',          'Поиск...');
PERFORM _seed_msg('action', 'Search menu...',      'Menyu qidirish...',  'Меню қидириш...',     'Поиск в меню...');
PERFORM _seed_msg('message', 'Recently visited',   'So''nggi kirilganlar', 'Сўнгги кирилганлар', 'Недавно посещённые');
PERFORM _seed_msg('action', 'Filter',              'Filtr',              'Филтр',               'Фильтр');
PERFORM _seed_msg('action', 'Apply',               'Qo''llash',          'Қўллаш',              'Применить');
PERFORM _seed_msg('action', 'Clear',               'Tozalash',           'Тозалаш',             'Очистить');
PERFORM _seed_msg('action', 'Reset',               'Qayta tiklash',      'Қайта тиклаш',        'Сбросить');
PERFORM _seed_msg('action', 'Close',               'Yopish',             'Ёпиш',                'Закрыть');
PERFORM _seed_msg('action', 'Back',                'Orqaga',             'Орқага',               'Назад');
PERFORM _seed_msg('action', 'Next',                'Keyingi',            'Кейинги',             'Далее');
PERFORM _seed_msg('action', 'Previous',            'Oldingi',            'Олдинги',             'Предыдущий');
PERFORM _seed_msg('action', 'Refresh',             'Qayta yuklash',      'Қайта юклаш',         'Перезагрузить');
PERFORM _seed_msg('action', 'Export',              'Eksport',            'Экспорт',             'Экспорт');
PERFORM _seed_msg('action', 'Import',              'Import',             'Импорт',              'Импорт');
PERFORM _seed_msg('action', 'Download',            'Yuklab olish',       'Юклаб олиш',          'Скачать');
PERFORM _seed_msg('action', 'Upload',              'Yuklash',            'Юклаш',               'Загрузить');
PERFORM _seed_msg('action', 'Print',               'Chop etish',         'Чоп этиш',            'Печать');
PERFORM _seed_msg('action', 'Copy',                'Nusxalash',          'Нусхалаш',            'Копировать');
PERFORM _seed_msg('action', 'View',                'Ko''rish',            'Кўриш',               'Просмотр');
PERFORM _seed_msg('action', 'Select',              'Tanlash',            'Танлаш',              'Выбрать');
PERFORM _seed_msg('action', 'Select all',          'Hammasini tanlash',  'Ҳаммасини танлаш',    'Выбрать все');
PERFORM _seed_msg('action', 'Confirm',             'Tasdiqlash',         'Тасдиқлаш',           'Подтвердить');
PERFORM _seed_msg('action', 'Submit',              'Yuborish',           'Юбориш',              'Отправить');
PERFORM _seed_msg('action', 'Sign in',             'Kirish',             'Кириш',               'Войти');
PERFORM _seed_msg('action', 'Sign out',            'Chiqish',            'Чиқиш',               'Выйти');
PERFORM _seed_msg('action', 'Download Excel',      'Excel yuklab olish', 'Excel юклаб олиш',    'Скачать Excel');
PERFORM _seed_msg('action', 'Download JSON',       'JSON yuklab olish',  'JSON юклаб олиш',     'Скачать JSON');
PERFORM _seed_msg('action', 'Clear cache',         'Cache tozalash',     'Кеш тозалаш',         'Очистить кэш');
PERFORM _seed_msg('action', 'Regenerate',          'Qayta yaratish',     'Қайта яратиш',        'Перегенерировать');
PERFORM _seed_msg('action', 'Toggle',              'Almashtirish',       'Алмаштириш',          'Переключить');
PERFORM _seed_msg('action', 'Show more',           'Ko''proq ko''rsatish','Кўпроқ кўрсатиш',    'Показать ещё');

-- =====================================================
-- Status labels
-- =====================================================
PERFORM _seed_msg('status', 'Active',              'Faol',               'Фаол',                'Активный');
PERFORM _seed_msg('status', 'Inactive',            'Nofaol',             'Нофаол',              'Неактивный');
PERFORM _seed_msg('status', 'Enabled',             'Yoqilgan',           'Ёқилган',             'Включено');
PERFORM _seed_msg('status', 'Disabled',            'O''chirilgan',        'Ўчирилган',           'Отключено');
PERFORM _seed_msg('status', 'Pending',             'Kutilmoqda',         'Кутилмоқда',          'В ожидании');
PERFORM _seed_msg('status', 'Approved',            'Tasdiqlangan',       'Тасдиқланган',        'Одобрено');
PERFORM _seed_msg('status', 'Rejected',            'Rad etilgan',        'Рад этилган',         'Отклонено');
PERFORM _seed_msg('status', 'Draft',               'Qoralama',           'Қоралама',            'Черновик');
PERFORM _seed_msg('status', 'Published',           'Nashr etilgan',      'Нашр этилган',        'Опубликовано');
PERFORM _seed_msg('status', 'Completed',           'Bajarildi',          'Бажарилди',           'Завершено');

-- =====================================================
-- Common labels (form fields, UI elements)
-- =====================================================
PERFORM _seed_msg('label', 'Name',                 'Nomi',               'Номи',                'Название');
PERFORM _seed_msg('label', 'Code',                 'Kod',                'Код',                 'Код');
PERFORM _seed_msg('label', 'Status',               'Holat',              'Ҳолат',               'Статус');
PERFORM _seed_msg('label', 'Category',             'Kategoriya',         'Категория',           'Категория');
PERFORM _seed_msg('label', 'Description',          'Tavsif',             'Тавсиф',              'Описание');
PERFORM _seed_msg('label', 'Date',                 'Sana',               'Сана',                'Дата');
PERFORM _seed_msg('label', 'Type',                 'Turi',               'Тури',                'Тип');
PERFORM _seed_msg('label', 'Actions',              'Amallar',            'Амаллар',             'Действия');
PERFORM _seed_msg('label', 'Details',              'Tafsilotlar',        'Тафсилотлар',         'Детали');
PERFORM _seed_msg('label', 'Settings',             'Sozlamalar',         'Созламалар',          'Настройки');
PERFORM _seed_msg('label', 'Profile',              'Profil',             'Профил',              'Профиль');
PERFORM _seed_msg('label', 'Language',             'Til',                'Тил',                 'Язык');
PERFORM _seed_msg('label', 'Email',                'Email',              'Email',               'Электронная почта');
PERFORM _seed_msg('label', 'Phone',                'Telefon',            'Телефон',             'Телефон');
PERFORM _seed_msg('label', 'Address',              'Manzil',             'Манзил',              'Адрес');
PERFORM _seed_msg('label', 'Region',               'Viloyat',            'Вилоят',              'Регион');
PERFORM _seed_msg('label', 'Total',                'Jami',               'Жами',                'Итого');
PERFORM _seed_msg('label', 'Count',                'Soni',               'Сони',                'Количество');
PERFORM _seed_msg('label', 'Username',             'Foydalanuvchi nomi', 'Фойдаланувчи номи',   'Имя пользователя');
PERFORM _seed_msg('label', 'Password',             'Parol',              'Парол',               'Пароль');
PERFORM _seed_msg('label', 'Columns',              'Ustunlar',           'Устунлар',            'Колонки');
PERFORM _seed_msg('label', 'TIN',                  'INN',                'ИНН',                 'ИНН');
PERFORM _seed_msg('label', 'Ownership',            'Mulkchilik',         'Мулкчилик',           'Собственность');
PERFORM _seed_msg('label', 'Short name',           'Qisqa nomi',         'Қисқа номи',          'Краткое название');
PERFORM _seed_msg('label', 'Created at',           'Yaratilgan',         'Яратилган',           'Создано');
PERFORM _seed_msg('label', 'Created by',           'Yaratuvchi',         'Яратувчи',            'Создал');
PERFORM _seed_msg('label', 'Updated at',           'Yangilangan',        'Янгиланган',          'Обновлено');
PERFORM _seed_msg('label', 'Updated by',           'Yangilovchi',        'Янгиловчи',           'Обновил');
PERFORM _seed_msg('label', 'Basic information',    'Asosiy ma''lumotlar', 'Асосий маълумотлар',  'Основная информация');
PERFORM _seed_msg('label', 'Audit information',    'Audit ma''lumotlari', 'Аудит маълумотлари',  'Аудит');

-- =====================================================
-- Common messages (toasts, empty states, loading)
-- =====================================================
PERFORM _seed_msg('message', 'Loading...',               'Yuklanmoqda...',               'Юкланмоқда...',              'Загрузка...');
PERFORM _seed_msg('message', 'Saving...',                'Saqlanmoqda...',               'Сақланмоқда...',             'Сохранение...');
PERFORM _seed_msg('message', 'Deleting...',              'O''chirilmoqda...',             'Ўчирилмоқда...',            'Удаление...');
PERFORM _seed_msg('message', 'No data found',            'Ma''lumotlar topilmadi',        'Маълумотлар топилмади',     'Данные не найдены');
PERFORM _seed_msg('message', 'No results found',         'Natija topilmadi',             'Натижа топилмади',          'Результаты не найдены');
PERFORM _seed_msg('message', 'No menu items available',  'Menyu elementlari mavjud emas', 'Меню элементлари мавжуд эмас', 'Пункты меню отсутствуют');
PERFORM _seed_msg('message', 'Successfully saved',       'Muvaffaqiyatli saqlandi',      'Муваффақиятли сақланди',    'Успешно сохранено');
PERFORM _seed_msg('message', 'Successfully deleted',     'Muvaffaqiyatli o''chirildi',    'Муваффақиятли ўчирилди',    'Успешно удалено');
PERFORM _seed_msg('message', 'Successfully updated',     'Muvaffaqiyatli yangilandi',    'Муваффақиятли янгиланди',   'Успешно обновлено');
PERFORM _seed_msg('message', 'Something went wrong',     'Xatolik yuz berdi',            'Хатолик юз берди',          'Что-то пошло не так');
PERFORM _seed_msg('message', 'Error',                    'Xatolik',                      'Хатолик',                   'Ошибка');
PERFORM _seed_msg('message', 'Success',                  'Muvaffaqiyatli',               'Муваффақиятли',             'Успешно');
PERFORM _seed_msg('message', 'Warning',                  'Ogohlantirish',                'Огоҳлантириш',              'Предупреждение');
PERFORM _seed_msg('message', 'Are you sure?',            'Ishonchingiz komilmi?',        'Ишончингиз комилми?',       'Вы уверены?');
PERFORM _seed_msg('message', 'This action cannot be undone', 'Bu amalni qaytarib bo''lmaydi', 'Бу амални қайтариб бўлмайди', 'Это действие нельзя отменить');
PERFORM _seed_msg('message', 'Failed to load data',      'Ma''lumotlarni yuklashda xatolik', 'Маълумотларни юклашда хатолик', 'Ошибка загрузки данных');
PERFORM _seed_msg('message', 'Export failed',            'Eksport qilishda xatolik',     'Экспорт қилишда хатолик',   'Ошибка экспорта');
PERFORM _seed_msg('message', 'Network error',           'Tarmoq xatosi',                        'Тармоқ хатоси',                     'Ошибка сети');
PERFORM _seed_msg('message', 'Network connection error', 'Internet bilan bog''lanishda xatolik', 'Интернет билан боғланишда хатолик', 'Ошибка подключения к интернету');
PERFORM _seed_msg('message', 'Backend server is not available', 'Backend server ishlamayapti. Iltimos, keyinroq qayta urinib ko''ring.', 'Бэкенд сервер ишламаяпти. Илтимос, кейинроқ қайта уриниб кўринг.', 'Сервер бэкенда недоступен. Пожалуйста, попробуйте позже.');
PERFORM _seed_msg('message', 'Failed to delete',        'O''chirishda xatolik',          'Ўчиришда хатолик',          'Ошибка удаления');
PERFORM _seed_msg('message', 'Session expired',         'Sessiya muddati tugadi',       'Сессия муддати тугади',     'Сессия истекла');
PERFORM _seed_msg('message', 'Access denied',           'Ruxsat berilmagan',            'Рухсат берилмаган',         'Доступ запрещён');
PERFORM _seed_msg('message', 'Not found',               'Topilmadi',                    'Топилмади',                 'Не найдено');
PERFORM _seed_msg('message', 'Retry',                   'Qayta urinish',                'Қайта уриниш',              'Повторить');

-- =====================================================
-- Validation messages
-- =====================================================
PERFORM _seed_msg('validation', 'This field is required',    'Bu maydon to''ldirilishi shart',     'Бу майдон тўлдирилиши шарт',     'Это поле обязательно');
PERFORM _seed_msg('validation', 'Invalid email address',     'Email manzili noto''g''ri',           'Email манзили нотўғри',           'Неверный адрес email');
PERFORM _seed_msg('validation', 'Too short',                 'Juda qisqa',                         'Жуда қисқа',                     'Слишком короткое');
PERFORM _seed_msg('validation', 'Too long',                  'Juda uzun',                          'Жуда узун',                      'Слишком длинное');
PERFORM _seed_msg('validation', 'Invalid format',            'Noto''g''ri format',                  'Нотўғри формат',                 'Неверный формат');
PERFORM _seed_msg('validation', '{{field}} is required',      '{{field}} maydoni to''ldirilishi shart', '{{field}} майдони тўлдирилиши шарт', 'Поле {{field}} обязательно для заполнения');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters', 'Kamida {{count}} belgidan iborat bo''lishi kerak', 'Камида {{count}} белгидан иборат бўлиши керак', 'Должно содержать минимум {{count}} символов');
PERFORM _seed_msg('validation', 'Passwords do not match',    'Parollar mos kelmaydi',              'Пароллар мос келмайди',          'Пароли не совпадают');
PERFORM _seed_msg('validation', 'Value already exists',      'Bu qiymat allaqachon mavjud',        'Бу қиймат аллақачон мавжуд',     'Значение уже существует');

-- =====================================================
-- Confirmation dialogs
-- =====================================================
PERFORM _seed_msg('confirm', 'Delete confirmation',      'O''chirishni tasdiqlang',       'Ўчиришни тасдиқланг',      'Подтвердите удаление');
PERFORM _seed_msg('confirm', 'Save changes?',           'O''zgarishlarni saqlaysizmi?',  'Ўзгаришларни сақлайсизми?', 'Сохранить изменения?');
PERFORM _seed_msg('confirm', 'Discard changes?',        'O''zgarishlarni bekor qilasizmi?', 'Ўзгаришларни бекор қиласизми?', 'Отменить изменения?');
PERFORM _seed_msg('confirm', 'Yes',                     'Ha',                            'Ҳа',                        'Да');
PERFORM _seed_msg('confirm', 'No',                      'Yo''q',                          'Йўқ',                       'Нет');
PERFORM _seed_msg('confirm', 'OK',                      'OK',                            'OK',                        'OK');

-- =====================================================
-- Pagination
-- =====================================================
PERFORM _seed_msg('pagination', 'Rows per page',         'Sahifadagi qatorlar',          'Саҳифадаги қаторлар',       'Строк на странице');
PERFORM _seed_msg('pagination', 'of',                    'dan',                          'дан',                       'из');
PERFORM _seed_msg('pagination', 'Page',                  'Sahifa',                       'Саҳифа',                    'Страница');
PERFORM _seed_msg('pagination', 'First page',            'Birinchi sahifa',              'Биринчи саҳифа',            'Первая страница');
PERFORM _seed_msg('pagination', 'Last page',             'Oxirgi sahifa',                'Охирги саҳифа',             'Последняя страница');
PERFORM _seed_msg('pagination', 'items',                 'ta',                           'та',                        'элементов');

-- =====================================================
-- Filters
-- =====================================================
PERFORM _seed_msg('label', 'Filters',                    'Filtrlar',                     'Филтрлар',                  'Фильтры');
PERFORM _seed_msg('label', 'Search by code, name or TIN...', 'Kod, nom yoki INN bo''yicha qidirish...', 'Код, ном ёки ИНН бўйича қидириш...', 'Поиск по коду, названию или ИНН...');

-- =====================================================
-- Auth
-- =====================================================
PERFORM _seed_msg('auth', 'HEMIS Admin Panel',          'HEMIS Admin Panel',            'HEMIS Админ Панели',        'HEMIS Админ Панель');
PERFORM _seed_msg('auth', 'Higher Education Management Information System', 'Oliy Ta''lim Boshqaruv Axborot Tizimi', 'Олий Таълим Бошқарув Ахборот Тизими', 'Информационная Система Управления Высшим Образованием');
PERFORM _seed_msg('auth', 'Remember me',               'Eslab qolish',                 'Эслаб қолиш',              'Запомнить меня');
PERFORM _seed_msg('auth', 'Forgot password?',           'Parolni unutdingizmi?',        'Паролни унутдингизми?',     'Забыли пароль?');
PERFORM _seed_msg('auth', 'Sign in to continue',       'Davom etish uchun tizimga kiring', 'Давом этиш учун тизимга киринг', 'Войдите для продолжения');
PERFORM _seed_msg('auth', 'Welcome back!',             'Xush kelibsiz!',               'Хуш келибсиз!',            'Добро пожаловать!');
PERFORM _seed_msg('auth', 'Invalid username or password', 'Foydalanuvchi nomi yoki parol noto''g''ri', 'Фойдаланувчи номи ёки парол нотўғри', 'Неверный логин или пароль');
PERFORM _seed_msg('auth', 'Account is disabled',        'Hisob faol emas',                'Ҳисоб фаол эмас',              'Аккаунт отключён');
PERFORM _seed_msg('auth', 'Too many attempts',          'Juda ko''p urinish. Keyinroq qayta urinib ko''ring.',            'Жуда кўп уриниш. Кейинроқ қайта уриниб кўринг.',          'Слишком много попыток. Попробуйте позже.');
PERFORM _seed_msg('auth', 'User account is disabled',  'Foydalanuvchi hisobi faol emas', 'Фойдаланувчи ҳисоби фаол эмас', 'Учётная запись пользователя неактивна');
PERFORM _seed_msg('auth', 'No university assigned',    'Universitet tayinlanmagan',      'Университет тайинланмаган',     'Университет не назначен');
PERFORM _seed_msg('auth', 'University is inactive',    'Universitet faol emas',          'Университет фаол эмас',        'Университет неактивен');
PERFORM _seed_msg('auth', '2025 HEMIS. All rights reserved.', '2025 HEMIS. Barcha huquqlar himoyalangan.', '2025 HEMIS. Барча ҳуқуқлар ҳимояланган.', '2025 HEMIS. Все права защищены.');
PERFORM _seed_msg('auth', 'Login',                     'Login',                          'Логин',                         'Логин');


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 2: TABLE COLUMN TRANSLATIONS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- University table columns
PERFORM _seed_msg('table', 'Organization type',     'Tashkiliy turi',               'Ташкилий тури',             'Организационная форма');
PERFORM _seed_msg('table', 'Cadastre',              'Kadastr',                      'Кадастр',                   'Кадастр');
PERFORM _seed_msg('table', 'Version type',          'Versiya turi',                 'Версия тури',               'Тип версии');
PERFORM _seed_msg('table', 'SOATO Region',          'SOATO Region',                 'СОАТО Регион',              'СОАТО Регион');
PERFORM _seed_msg('table', 'University URL',        'OTM URL',                      'ОТМ URL',                   'URL ВУЗа');
PERFORM _seed_msg('table', 'Teacher URL',           'O''qituvchi URL',               'Ўқитувчи URL',              'URL Преподавателей');
PERFORM _seed_msg('table', 'Student URL',           'Talaba URL',                   'Талаба URL',                'URL Студентов');
PERFORM _seed_msg('table', 'Contract category',     'Kontrakt kategoriyasi',        'Контракт категорияси',      'Категория контракта');
PERFORM _seed_msg('table', 'Activity status',       'Faollik statusi',              'Фаоллик статуси',           'Статус активности');
PERFORM _seed_msg('table', 'Belongs to',            'Tegishli',                     'Тегишли',                   'Принадлежит');
PERFORM _seed_msg('table', 'Terrain',               'Mahalla',                      'Маҳалла',                   'Махалля');
PERFORM _seed_msg('table', 'Mail address',          'Pochta manzili',               'Почта манзили',             'Почтовый адрес');
PERFORM _seed_msg('table', 'Bank details',          'Bank ma''lumotlari',            'Банк маълумотлари',         'Банковские реквизиты');
PERFORM _seed_msg('table', 'Accreditation info',    'Akkreditatsiya ma''lumotlari',  'Аккредитация маълумотлари', 'Информация об аккредитации');
PERFORM _seed_msg('table', 'GPA edit',              'GPA tahrir',                   'ГПА таҳрир',                'Редактирование GPA');
PERFORM _seed_msg('table', 'Accreditation edit',    'Akkreditatsiya tahrir',        'Аккредитация таҳрир',       'Редактирование аккредитации');
PERFORM _seed_msg('table', 'Add student',           'Talaba qo''shish',              'Талаба қўшиш',              'Добавление студентов');
PERFORM _seed_msg('table', 'Allow grouping',        'Guruhlashga ruxsat',           'Гуруҳлашга рухсат',         'Разрешить группировку');
PERFORM _seed_msg('table', 'Allow external transfer', 'Tashqariga o''tkazishga ruxsat', 'Ташқарига ўтказишга рухсат', 'Разрешить внешний перевод');

-- Faculty table columns
PERFORM _seed_msg('table', 'University name',       'OTM nomi',                     'ОТМ номи',                  'Название ВУЗа');
PERFORM _seed_msg('table', 'University code',       'OTM kodi',                     'ОТМ коди',                  'Код ВУЗа');
PERFORM _seed_msg('table', 'Name (Uzbek)',          'Nomi (o''zbekcha)',              'Номи (ўзбекча)',            'Название (узбекский)');
PERFORM _seed_msg('table', 'Name (Russian)',        'Nomi (ruscha)',                'Номи (русча)',              'Название (русский)');
PERFORM _seed_msg('table', 'Faculty count',         'Fakultetlar soni',             'Факультетлар сони',         'Количество факультетов');
PERFORM _seed_msg('table', 'Faculty type',          'Fakultet turi',                'Факультет тури',            'Тип факультета');

-- Page titles
PERFORM _seed_msg('label', 'Institutions (HEIs)',   'Muassasalar (OTMlar)',         'Муассасалар (ОТМлар)',       'Учреждения (ВУЗы)');
PERFORM _seed_msg('label', 'HEI Registry',          'OTM reestri',                       'ОТМ реестри',                     'Реестр ВУЗов');
PERFORM _seed_msg('label', 'Institution details',   'Muassasa ma''lumotlari',        'Муассаса маълумотлари',     'Информация об учреждении');
PERFORM _seed_msg('label', 'Higher Education Institutions Registry', 'Oliy ta''lim muassasalari reestri', 'Олий таълим муассасалари реестри', 'Реестр высших учебных заведений');
PERFORM _seed_msg('label', 'No faculties found',    'Fakultetlar topilmadi',        'Факультетлар топилмади',    'Факультеты не найдены');

-- Translation admin page
PERFORM _seed_msg('label', 'Translation management',    'Tarjimalarni boshqarish',      'Таржималарни бошқариш',     'Управление переводами');
PERFORM _seed_msg('label', 'Find duplicates',          'Dublikatlarni topish',         'Дубликатларни топиш',       'Найти дубликаты');


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 3: MENU TRANSLATIONS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Top-level menus
PERFORM _seed_msg('menu', 'Dashboard',              'Bosh sahifa',                  'Бош саҳифа',                'Главная');
PERFORM _seed_msg('menu', 'Institutions',           'Muassasalar',                  'Муассасалар',               'Учреждения');
PERFORM _seed_msg('menu', 'Registries',             'Reestlar',                     'Реестрлар',                 'Реестры');
PERFORM _seed_msg('menu', 'Rating',                 'Reyting',                      'Рейтинг',                   'Рейтинг');
PERFORM _seed_msg('menu', 'Database',               'Ma''lumotlar bazasi',           'Маълумотлар базаси',        'База данных');
PERFORM _seed_msg('menu', 'Classifiers',            'Klassifikatorlar',             'Классификаторлар',          'Классификаторы');
PERFORM _seed_msg('menu', 'Reports',                'Hisobotlar',                   'Ҳисоботлар',                'Отчёты');
PERFORM _seed_msg('menu', 'System',                 'Tizim',                        'Тизим',                     'Система');

-- Registry submenus
PERFORM _seed_msg('menu', 'E-Registry',             'E-Reestr',                     'Э-Реестр',                  'Э-Реестр');
PERFORM _seed_msg('menu', 'Scientific registry',    'Ilmiy reestr',                 'Илмий реестр',              'Научный реестр');
PERFORM _seed_msg('menu', 'Student data',           'Talaba ma''lumotlari',          'Талаба маълумотлари',       'Данные студента');

-- Rating submenus
PERFORM _seed_msg('menu', 'Administrative rating',  'Administrativ reyting',        'Административ рейтинг',    'Административный рейтинг');
PERFORM _seed_msg('menu', 'Employee rating',        'Xodimlar reytingi',            'Ходимлар рейтинги',         'Рейтинг сотрудников');
PERFORM _seed_msg('menu', 'Student rating',         'Talabalar reytingi',           'Талабалар рейтинги',        'Рейтинг студентов');
PERFORM _seed_msg('menu', 'Sport rating',           'Sport reytingi',               'Спорт рейтинги',            'Спортивный рейтинг');
PERFORM _seed_msg('menu', 'Academic rating',        'Akademik reyting',             'Академик рейтинг',          'Академический рейтинг');
PERFORM _seed_msg('menu', 'Methodical publications','Uslubiy nashrlar',             'Услубий нашрлар',           'Методические публикации');
PERFORM _seed_msg('menu', 'Study rating',           'O''quv reytingi',               'Ўқув рейтинги',             'Учебный рейтинг');
PERFORM _seed_msg('menu', 'Verification type',      'Tekshirish turi',              'Текшириш тури',             'Тип проверки');
PERFORM _seed_msg('menu', 'Scientific rating',      'Ilmiy reyting',                'Илмий рейтинг',             'Научный рейтинг');
PERFORM _seed_msg('menu', 'Scientific publications','Ilmiy nashrlar',               'Илмий нашрлар',             'Научные публикации');
PERFORM _seed_msg('menu', 'Scientific projects',    'Ilmiy loyihalar',              'Илмий лойиҳалар',           'Научные проекты');
PERFORM _seed_msg('menu', 'Intellectual property',  'Intellektual mulk',            'Интеллектуал мулк',         'Интеллектуальная собственность');
PERFORM _seed_msg('menu', 'Student GPA',            'Talaba GPA',                   'Талаба GPA',                'GPA студента');

-- Data submenus
PERFORM _seed_msg('menu', 'General data',           'Umumiy ma''lumotlar',           'Умумий маълумотлар',        'Общие данные');
PERFORM _seed_msg('menu', 'Structure',              'Tuzilma',                      'Тузилма',                   'Структура');
PERFORM _seed_msg('menu', 'Employees',              'Xodimlar',                     'Ходимлар',                  'Сотрудники');
PERFORM _seed_msg('menu', 'Students',               'Talabalar',                    'Талабалар',                 'Студенты');
PERFORM _seed_msg('menu', 'Education',              'Ta''lim',                       'Таълим',                    'Образование');
PERFORM _seed_msg('menu', 'Study process',          'O''qish jarayoni',              'Ўқиш жараёни',              'Учебный процесс');
PERFORM _seed_msg('menu', 'Science',                'Fan',                          'Фан',                       'Наука');
PERFORM _seed_msg('menu', 'Organizational',         'Tashkiliy',                    'Ташкилий',                  'Организационный');
PERFORM _seed_msg('menu', 'Contract types',         'Shartnoma turlari',            'Шартнома турлари',          'Типы договоров');

-- Reports submenus
PERFORM _seed_msg('menu', 'University reports',     'OTM hisobotlari',              'ОТМ ҳисоботлари',           'Отчёты ВУЗов');
PERFORM _seed_msg('menu', 'Employee reports',       'Xodimlar hisobotlari',         'Ходимлар ҳисоботлари',      'Отчёты сотрудников');
PERFORM _seed_msg('menu', 'Employee list',          'Xodimlar ro''yxati',            'Ходимлар рўйхати',          'Список сотрудников');
PERFORM _seed_msg('menu', 'Employee personal',      'Xodimlar shaxsiy',             'Ходимлар шахсий',           'Личные данные сотрудников');
PERFORM _seed_msg('menu', 'Employee work',          'Xodimlar ish',                 'Ходимлар иш',               'Работа сотрудников');
PERFORM _seed_msg('menu', 'Student reports',        'Talabalar hisobotlari',        'Талабалар ҳисоботлари',     'Отчёты студентов');
PERFORM _seed_msg('menu', 'Student statistics',     'Talabalar statistikasi',       'Талабалар статистикаси',    'Статистика студентов');
PERFORM _seed_msg('menu', 'Student education',      'Talabalar ta''limi',            'Талабалар таълими',         'Образование студентов');
PERFORM _seed_msg('menu', 'Student personal',       'Talabalar shaxsiy',            'Талабалар шахсий',          'Личные данные студентов');
PERFORM _seed_msg('menu', 'Attendance',             'Davomat',                      'Давомат',                   'Посещаемость');
PERFORM _seed_msg('menu', 'Grades',                 'Baholar',                      'Баҳолар',                   'Оценки');
PERFORM _seed_msg('menu', 'Dynamics',               'Dinamika',                     'Динамика',                  'Динамика');
PERFORM _seed_msg('menu', 'Academic reports',        'Akademik hisobotlar',          'Академик ҳисоботлар',       'Академические отчёты');
PERFORM _seed_msg('menu', 'Study report',           'O''quv hisoboti',               'Ўқув ҳисоботи',             'Учебный отчёт');
PERFORM _seed_msg('menu', 'Research reports',       'Tadqiqot hisobotlari',         'Тадқиқот ҳисоботлари',      'Исследовательские отчёты');
PERFORM _seed_msg('menu', 'Research projects',      'Tadqiqot loyihalari',          'Тадқиқот лойиҳалари',       'Исследовательские проекты');
PERFORM _seed_msg('menu', 'Research publications',  'Tadqiqot nashlari',            'Тадқиқот нашлари',          'Исследовательские публикации');
PERFORM _seed_msg('menu', 'Researchers',            'Tadqiqotchilar',               'Тадқиқотчилар',             'Исследователи');
PERFORM _seed_msg('menu', 'Economic reports',       'Iqtisodiy hisobotlar',         'Иқтисодий ҳисоботлар',      'Экономические отчёты');
PERFORM _seed_msg('menu', 'Finance',                'Moliya',                       'Молия',                     'Финансы');
PERFORM _seed_msg('menu', 'Economy',                'Xo''jalik',                     'Хўжалик',                   'Хозяйство');

-- System submenus
PERFORM _seed_msg('menu', 'Temporary',              'Vaqtinchalik',                 'Вақтинчалик',               'Временные');
PERFORM _seed_msg('menu', 'Translations',           'Tarjimalar',                   'Таржималар',                'Переводы');
PERFORM _seed_msg('menu', 'Users',                  'Foydalanuvchilar',             'Фойдаланувчилар',           'Пользователи');
PERFORM _seed_msg('menu', 'Roles',                  'Rollar',                       'Роллар',                    'Роли');
PERFORM _seed_msg('menu', 'Audit Logs',              'Audit loglar',                  'Аудит логлар',              'Журнал аудита');
PERFORM _seed_msg('menu', 'Report updates',         'Hisobot yangilanishlari',      'Ҳисоботларни янгилаш',      'Обновления отчётов');

-- E-Reestr submenus
PERFORM _seed_msg('menu', 'Universities',           'Universitetlar',               'Университетлар',            'Университеты');
PERFORM _seed_msg('menu', 'Faculties',              'Fakultetlar',                  'Факультетлар',              'Факультеты');
PERFORM _seed_msg('menu', 'Departments',            'Kafedralar',                   'Кафедралар',                'Кафедры');
PERFORM _seed_msg('menu', 'Teachers',               'O''qituvchilar',                'Ўқитувчилар',               'Преподаватели');
PERFORM _seed_msg('menu', 'Diplomas',               'Diplomlar',                    'Дипломлар',                 'Дипломы');
PERFORM _seed_msg('menu', 'Bachelor specialities',  'Yo''nalishlar (Bakalavr)',      'Йўналишлар (Бакалавр)',     'Направления (Бакалавриат)');
PERFORM _seed_msg('menu', 'Master specialities',    'Mutaxassisliklar (Magistr)',   'Мутахассисликлар (Магистр)', 'Специальности (Магистратура)');
PERFORM _seed_msg('menu', 'Doctoral specialities',  'Ixtisosliklar (Doktorantura)', 'Ихтисосликлар (Докторантура)', 'Специальности (Докторантура)');
PERFORM _seed_msg('menu', 'Employee jobs',          'Xodimlar ish joylari',         'Ходимлар иш жойлари',       'Рабочие места сотрудников');
PERFORM _seed_msg('menu', 'Diploma blank distribution', 'Diplom blankalarini taqsimlash', 'Диплом бланкаларини тақсимлаш', 'Распределение бланков дипломов');
PERFORM _seed_msg('menu', 'Diploma blanks',         'Diplom blankalar',             'Диплом бланкалар',          'Бланки дипломов');
PERFORM _seed_msg('menu', 'Ordinatura specialities','Ordinatura mutaxassisliklari', 'Ординатура мутахассисликлари', 'Специальности ординатуры');
PERFORM _seed_msg('menu', 'University specialities','OTM mutaxassisliklari',        'ОТМ мутахассисликлари',     'Специальности ВУЗа');
PERFORM _seed_msg('menu', 'Study groups',           'O''quv guruhlari',              'Ўқув гурухлари',            'Учебные группы');
PERFORM _seed_msg('menu', 'Scholarship',            'Stipendiya',                   'Стипендия',                 'Стипендия');
PERFORM _seed_msg('menu', 'Student certificates',   'Talaba sertifikatlari',        'Талаба сертификатлари',     'Сертификаты студентов');
PERFORM _seed_msg('menu', 'Teacher certificates',   'O''qituvchi sertifikatlari',    'Ўқитувчи сертификатлари',   'Сертификаты преподавателей');
PERFORM _seed_msg('menu', 'Students Lite',          'Talabalar Lite',               'Талабалар Lite',            'Студенты Lite');

-- Data > Structure submenus
PERFORM _seed_msg('menu', 'University types',       'OTM tashkiliy shakllari',      'ОТМ ташкилий шакллари',     'Организационные формы ВУЗов');
PERFORM _seed_msg('menu', 'Ownership forms',        'OTM mulkchilik shakllari',     'ОТМ мулкчилик шакллари',    'Формы собственности ВУЗов');
PERFORM _seed_msg('menu', 'Department types',       'OTM bo''linmalari turlari',     'ОТМ бўлинмалари турлари',   'Типы подразделений ВУЗов');
PERFORM _seed_msg('menu', 'Locality type',          'Mahalliylik turi',             'Маҳаллийлик тури',          'Тип местности');
PERFORM _seed_msg('menu', 'University activity status', 'OTM aktivlik statusi',     'ОТМ активлик статуси',      'Статус активности ВУЗа');
PERFORM _seed_msg('menu', 'Ministry affiliation',   'OTMning Vazirlikka tegishliligi', 'ОТМнинг Вазирликка тегишлилиги', 'Принадлежность ВУЗа министерству');

-- Data > Education submenus
PERFORM _seed_msg('menu', 'Education types',        'Ta''lim turlari',               'Таълим турлари',            'Типы образования');
PERFORM _seed_msg('menu', 'Education forms',        'Ta''lim shakllari',             'Таълим шакллари',           'Формы образования');
PERFORM _seed_msg('menu', 'Education languages',    'Ta''lim tillari',               'Таълим тиллари',            'Языки обучения');
PERFORM _seed_msg('menu', 'Grading systems',        'Baholash tizimlari',           'Баҳолаш тизимлари',         'Системы оценивания');
PERFORM _seed_msg('menu', 'Score types',            'Baho turlari',                 'Баҳо турлари',              'Типы оценок');
PERFORM _seed_msg('menu', 'Exam types',             'Nazorat turlari',              'Назорат турлари',           'Типы контроля');
PERFORM _seed_msg('menu', 'Diploma blank categories', 'Diplom blanka kategoriyalari', 'Диплом бланка категориялари', 'Категории бланков дипломов');
PERFORM _seed_msg('menu', 'Diploma blank statuses', 'Diplom blanka statuslari',     'Диплом бланка статуслари',  'Статусы бланков дипломов');
PERFORM _seed_msg('menu', 'Diploma generation statuses', 'Diplom shakllantirish statuslari', 'Диплом шакллантириш статуслари', 'Статусы формирования дипломов');
PERFORM _seed_msg('menu', 'Certificate category',   'Sertifikat toifasi',           'Сертификат тоифаси',        'Категория сертификата');
PERFORM _seed_msg('menu', 'Certificate type',       'Sertifikat turi',              'Сертификат тури',           'Тип сертификата');
PERFORM _seed_msg('menu', 'Certificate subject',    'Sertifikat fani',              'Сертификат фани',           'Предмет сертификата');
PERFORM _seed_msg('menu', 'Certificate grade',      'Sertifikat darajasi',          'Сертификат даражаси',       'Уровень сертификата');

-- Data > Employee submenus
PERFORM _seed_msg('menu', 'Employee categories',    'Xodimlar toifalari',           'Ходимлар тоифалари',        'Категории сотрудников');
PERFORM _seed_msg('menu', 'Teacher statuses',       'O''qituvchi xolatlari',         'Ўқитувчи ҳолатлари',        'Статусы преподавателей');
PERFORM _seed_msg('menu', 'Labor rates',            'Mehnat stavkalari',            'Меҳнат ставкалари',         'Ставки труда');
PERFORM _seed_msg('menu', 'Labor forms',            'Mehnat shakllari',             'Меҳнат шакллари',           'Формы труда');
PERFORM _seed_msg('menu', 'Position types',         'Lavozim turlari',              'Лавозим турлари',           'Типы должностей');
PERFORM _seed_msg('menu', 'Qualification places',   'Malaka oshirish joylari',      'Малака оширш жойлари',      'Места повышения квалификации');
PERFORM _seed_msg('menu', 'Teacher achievements',   'O''qituvchi yutuqlari',         'Ўқитувчи ютуқлари',         'Достижения преподавателей');
PERFORM _seed_msg('menu', 'Academic degrees',       'Ilmiy darajalar',              'Илмий даражалар',           'Учёные степени');
PERFORM _seed_msg('menu', 'Academic ranks',         'Ilmiy unvonlar',               'Илмий унвонлар',            'Учёные звания');

-- Registry > Scientific submenus
PERFORM _seed_msg('menu', 'Doctoral studies',       'Doktorantura',                 'Докторантура',              'Докторантура');
PERFORM _seed_msg('menu', 'Dissertations',          'Dissertatsiyalar',             'Диссертациялар',            'Диссертации');
PERFORM _seed_msg('menu', 'Project executors',      'Loyiha ijrochilari',           'Лойиҳа ижрочилари',         'Исполнители проектов');
PERFORM _seed_msg('menu', 'Project funding',        'Loyiha moliyalash',            'Лойиҳа молиялаш',           'Финансирование проектов');
PERFORM _seed_msg('menu', 'Methodical works',       'Uslubiy ishlar',               'Услубий ишлар',             'Методические работы');
PERFORM _seed_msg('menu', 'Authors',                'Mualliflar',                   'Муаллифлар',                'Авторы');
PERFORM _seed_msg('menu', 'Scientific activity',    'Ilmiy faoliyat',               'Илмий фаолият',             'Научная деятельность');

-- Data > General submenus
PERFORM _seed_msg('menu', 'Countries',              'Davlatlar',                    'Давлатлар',                 'Страны');
PERFORM _seed_msg('menu', 'SOATO regions',          'SOATO hududlar',               'СОАТО ҳудудлар',            'СОАТО регионы');
PERFORM _seed_msg('menu', 'Nationalities',          'Millatlar',                    'Миллатлар',                 'Национальности');
PERFORM _seed_msg('menu', 'Citizenships',           'Fuqaroliklar',                 'Фуқароликлар',              'Гражданства');
PERFORM _seed_msg('menu', 'Genders',                'Jinslar',                      'Жинслар',                   'Пол');
PERFORM _seed_msg('menu', 'Bachelor specialties',   'Bakalavr yo''nalishlari',       'Бакалавр йўналишлари',      'Специальности бакалавриата');
PERFORM _seed_msg('menu', 'Master specialties',     'Magistr mutaxassisliklari',    'Магистр мутахассисликлари',  'Специальности магистратуры');
PERFORM _seed_msg('menu', 'Doctoral specialties',   'Doktorantura ixtisosliklari',  'Докторантура ихтисосликлари','Специальности докторантуры');
PERFORM _seed_msg('menu', 'Terrain types',          'Hududiylik turlari',           'Ҳудудийлик турлари',         'Типы местности');
PERFORM _seed_msg('menu', 'Poverty levels',         'Kam ta''minlanganlik',          'Кам таъминланганлик',       'Уровень малообеспеченности');

-- Data > Student submenus
PERFORM _seed_msg('menu', 'Student statuses',       'Talaba holatlari',             'Талаба ҳолатлари',          'Статусы студентов');
PERFORM _seed_msg('menu', 'Student achievements',   'Talaba yutuqlari',             'Талаба ютуқлари',           'Достижения студентов');
PERFORM _seed_msg('menu', 'Expulsion reasons',      'Chetlashtirish sabablari',     'Четлаштириш сабаблари',     'Причины отчисления');
PERFORM _seed_msg('menu', 'Accommodation types',    'Yotoqxona turlari',            'Ётоқхона турлари',          'Типы общежитий');
PERFORM _seed_msg('menu', 'Doctoral student types', 'Doktorant turlari',            'Докторант турлари',         'Типы докторантов');
PERFORM _seed_msg('menu', 'Social categories',      'Ijtimoiy toifalar',            'Ижтимоий тоифалар',         'Социальные категории');
PERFORM _seed_msg('menu', 'Academic leave reasons', 'Akademik ta''til sabablari',    'Академик таътил сабаблари', 'Причины академического отпуска');
PERFORM _seed_msg('menu', 'Doctoral statuses',      'Doktorant holatlari',          'Докторант ҳолатлари',       'Статусы докторантов');
PERFORM _seed_msg('menu', 'Graduate work fields',   'Bitiruvchi ish sohalari',      'Битирувчи иш соҳалари',    'Направления работы выпускников');
PERFORM _seed_msg('menu', 'Graduate inactive reasons', 'Bitiruvchi nofaollik sabablari', 'Битирувчи нофаоллик сабаблари', 'Причины неактивности выпускников');
PERFORM _seed_msg('menu', 'Student types',          'Talaba turlari',               'Талаба турлари',            'Типы студентов');
PERFORM _seed_msg('menu', 'Living statuses',        'Yashash holatlari',            'Яшаш ҳолатлари',           'Статусы проживания');
PERFORM _seed_msg('menu', 'Roommate types',         'Xonada yashash turlari',       'Хонада яшаш турлари',      'Типы совместного проживания');
PERFORM _seed_msg('menu', 'Workplace compatibility','Ish joyi mosligi',             'Иш жойи мослиги',          'Совместимость с работой');
PERFORM _seed_msg('menu', 'Academic mobility',      'Akademik mobillik',            'Академик мобиллик',        'Академическая мобильность');

-- Data > Study process submenus
PERFORM _seed_msg('menu', 'Academic years',         'O''quv yillari',                'Ўқув йиллари',             'Учебные годы');
PERFORM _seed_msg('menu', 'Courses',                'Kurslar',                      'Курслар',                   'Курсы');
PERFORM _seed_msg('menu', 'Semesters',              'Semestrlar',                   'Семестрлар',                'Семестры');
PERFORM _seed_msg('menu', 'Week types',             'Hafta turlari',                'Ҳафта турлари',             'Типы недель');
PERFORM _seed_msg('menu', 'Subject blocks',         'Fan bloklari',                 'Фан блоклари',              'Блоки предметов');
PERFORM _seed_msg('menu', 'Subject types',          'Fan turlari',                  'Фан турлари',               'Типы предметов');
PERFORM _seed_msg('menu', 'Class types',            'Mashg''ulot turlari',           'Машғулот турлари',          'Типы занятий');
PERFORM _seed_msg('menu', 'Exam completion types',  'Imtihon yakunlash turlari',    'Имтиҳон якунлаш турлари',   'Типы завершения экзаменов');
PERFORM _seed_msg('menu', 'Final exam types',       'Yakuniy nazorat turlari',      'Якуний назорат турлари',    'Типы итогового контроля');
PERFORM _seed_msg('menu', 'Semester list',          'Semestrlar ro''yxati',          'Семестрлар рўйхати',        'Список семестров');
PERFORM _seed_msg('menu', 'Decree types',           'Buyruq turlari',               'Буйруқ турлари',            'Типы приказов');
PERFORM _seed_msg('menu', 'Sport types',            'Sport turlari',                'Спорт турлари',             'Виды спорта');
PERFORM _seed_msg('menu', 'Attendance settings',    'Davomat sozlamalari',          'Давомат созламалари',       'Настройки посещаемости');
PERFORM _seed_msg('menu', 'Conduction forms',       'O''tkazish shakllari',          'Ўтказиш шакллари',          'Формы проведения');
PERFORM _seed_msg('menu', 'Internship forms',       'Amaliyot shakllari',           'Амалиёт шакллари',          'Формы практики');
PERFORM _seed_msg('menu', 'Internship types',       'Amaliyot turlari',             'Амалиёт турлари',           'Типы практики');
PERFORM _seed_msg('menu', 'Resource types',         'Resurs turlari',               'Ресурс турлари',            'Типы ресурсов');
PERFORM _seed_msg('menu', 'Extracurricular activities', 'Darsdan tashqari mashg''ulotlar', 'Дарсдан ташқари машғулотлар', 'Внеучебные мероприятия');

-- Data > Science submenus
PERFORM _seed_msg('menu', 'Science project types',  'Ilmiy loyiha turlari',         'Илмий лойиҳа турлари',      'Типы научных проектов');
PERFORM _seed_msg('menu', 'Project locality',       'Loyiha joylashuvi',            'Лойиҳа жойлашуви',          'Местоположение проекта');
PERFORM _seed_msg('menu', 'Currencies',             'Valyutalar',                   'Валюталар',                 'Валюты');
PERFORM _seed_msg('menu', 'Executor types',         'Ijrochi turlari',              'Ижрочи турлари',            'Типы исполнителей');
PERFORM _seed_msg('menu', 'Publication types',      'Nashr turlari',                'Нашр турлари',              'Типы публикаций');
PERFORM _seed_msg('menu', 'Methodical publication types', 'Uslubiy nashr turlari',  'Услубий нашр турлари',      'Типы методических публикаций');
PERFORM _seed_msg('menu', 'Patent types',           'Patent turlari',               'Патент турлари',            'Типы патентов');
PERFORM _seed_msg('menu', 'Publication databases',  'Nashr bazalari',               'Нашр базалари',             'Базы публикаций');
PERFORM _seed_msg('menu', 'Scholar databases',      'Ilmiy bazalar',                'Илмий базалар',             'Научные базы данных');

-- Data > Organizational submenus
PERFORM _seed_msg('menu', 'Payment forms',          'To''lov shakllari',             'Тўлов шакллари',            'Формы оплаты');
PERFORM _seed_msg('menu', 'Scholarship rates',      'Stipendiya stavkalari',        'Стипендия ставкалари',      'Ставки стипендий');
PERFORM _seed_msg('menu', 'Scholarship categories', 'Stipendiya toifalari',         'Стипендия тоифалари',       'Категории стипендий');
PERFORM _seed_msg('menu', 'Scholarship decrees',    'Stipendiya buyruqlari',        'Стипендия буйруқлари',      'Приказы о стипендиях');
PERFORM _seed_msg('menu', 'Contract payment types', 'Shartnoma to''lov turlari',     'Шартнома тўлов турлари',    'Типы оплаты контрактов');
PERFORM _seed_msg('menu', 'Contract amounts',       'Shartnoma summalari',          'Шартнома суммалари',        'Суммы контрактов');
PERFORM _seed_msg('menu', 'Auditorium types',       'Auditoriya turlari',           'Аудитория турлари',         'Типы аудиторий');
PERFORM _seed_msg('menu', 'Device types',           'Jihoz turlari',                'Жиҳоз турлари',             'Типы оборудования');
PERFORM _seed_msg('menu', 'Grant types',            'Grant turlari',                'Грант турлари',             'Типы грантов');

-- Data > Contract category
PERFORM _seed_msg('menu', 'Contract categories',    'Shartnoma toifalari',          'Шартнома тоифалари',        'Категории контрактов');

-- Reports > Universities submenus
PERFORM _seed_msg('menu', 'University count',       'OTMlar soni',                  'ОТМлар сони',               'Количество ВУЗов');
PERFORM _seed_msg('menu', 'Faculty list report',    'Fakultetlar ro''yxati',         'Факультетлар рўйхати',      'Список факультетов');

-- Missing menu translations (M005 gettext migration)
PERFORM _seed_msg('menu', 'Directions',             'Yo''nalishlar',                 'Йўналишлар',                'Направления');
PERFORM _seed_msg('menu', 'Teacher list',           'O''qituvchilar ro''yxati',       'Ўқитувчилар рўйхати',       'Список преподавателей');
PERFORM _seed_msg('menu', 'Positions',              'Lavozimlar',                   'Лавозимлар',                'Должности');
PERFORM _seed_msg('menu', 'Teacher qualifications', 'Malaka oshirish',              'Малака оширш',              'Повышение квалификации');
PERFORM _seed_msg('menu', 'Teacher reports',        'O''qituvchilar hisobotlari',    'Ўқитувчилар ҳисоботлари',   'Отчёты преподавателей');
PERFORM _seed_msg('menu', 'Student classifiers',    'Talaba klassifikatorlari',     'Талаба классификаторлари',   'Классификаторы студентов');
PERFORM _seed_msg('menu', 'Science classifiers',    'Fan klassifikatorlari',        'Фан классификаторлари',     'Классификаторы науки');


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 4: DASHBOARD, STUDENTS, TEACHERS, REPORTS, FORM TRANSLATIONS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- =====================================================
-- Dashboard
-- =====================================================
PERFORM _seed_msg('label', 'Currently studying',     'Hozir O''qimoqda',             'Ҳозир Ўқимоқда',            'Сейчас обучаются');
PERFORM _seed_msg('label', 'Graduates',              'Bitirganlar',                  'Битирганлар',               'Выпускники');
PERFORM _seed_msg('label', 'Expelled',               'Chetlashganlar',               'Четлашганлар',              'Отчисленные');
PERFORM _seed_msg('label', 'Total Teachers',         'Jami O''qituvchilar',           'Жами Ўқитувчилар',          'Всего преподавателей');
PERFORM _seed_msg('label', 'Academic Leave',         'Akademik Ta''til',              'Академик Таътил',           'Академический отпуск');
PERFORM _seed_msg('label', 'Cancelled',              'Bekor Qilingan',               'Бекор Қилинган',            'Отменённые');
PERFORM _seed_msg('label', 'Total HEIs',             'Jami OTMlar',                  'Жами ОТМлар',               'Всего ВУЗов');
PERFORM _seed_msg('label', 'Issued Diplomas',        'Berilgan Diplomlar',           'Берилган Дипломлар',        'Выданные дипломы');
PERFORM _seed_msg('label', 'unknown',                'noma''lum',                     'номаълум',                  'неизвестно');
PERFORM _seed_msg('action', 'Get report',            'Hisobot olish',                'Ҳисобот олиш',              'Получить отчёт');
-- NB: 'Education types' allaqachon menu kategoriyasida (qator 402) mavjud; dublikat olib tashlandi
PERFORM _seed_msg('label', 'Distribution by education levels', 'Ta''lim bosqichlari bo''yicha taqsimlash', 'Таълим босқичлари бўйича тақсимлаш', 'Распределение по уровням образования');
PERFORM _seed_msg('label', 'student',                'talaba',                       'талаба',                    'студент');
PERFORM _seed_msg('label', 'TOP Universities',       'TOP Universitetlar',           'ТОП Университетлар',        'ТОП Университеты');
PERFORM _seed_msg('label', 'Top 5 by rating',        'Reyting bo''yicha eng yaxshi 5 ta OTM', 'Рейтинг бўйича энг яхши 5 та ОТМ', 'Топ 5 по рейтингу');
PERFORM _seed_msg('label', 'Grant:',                 'Grant:',                       'Грант:',                    'Грант:');
PERFORM _seed_msg('label', 'Recent activity',        'So''nggi faoliyat',             'Сўнгги фаолият',            'Последняя активность');
PERFORM _seed_msg('label', 'Changes in last 24 hours', 'Oxirgi 24 soatdagi o''zgarishlar', 'Охирги 24 соатдаги ўзгаришлар', 'Изменения за последние 24 часа');
PERFORM _seed_msg('label', 'Quick info',             'Tezkor ma''lumotlar',           'Тезкор маълумотлар',        'Быстрая информация');
PERFORM _seed_msg('label', 'Grant students',         'Grant talabalar',              'Грант талабалар',           'Грантовые студенты');
PERFORM _seed_msg('label', 'Contract students',      'Kontrakt talabalar',           'Контракт талабалар',        'Контрактные студенты');
-- NB: 'Scientific projects' va 'Scientific publications' allaqachon menu kategoriyasida (qatorlar 327-328) mavjud; dublikatlar olib tashlandi
PERFORM _seed_msg('message', 'Please refresh the page', 'Iltimos, sahifani yangilang', 'Илтимос, саҳифани янгиланг', 'Пожалуйста, обновите страницу');
PERFORM _seed_msg('label', 'STATISTICS',             'STATISTIKA',                   'СТАТИСТИКА',                'СТАТИСТИКА');
PERFORM _seed_msg('label', 'Monitoring and analysis of higher education system of the Republic of Uzbekistan', 'O''zbekiston Respublikasi Oliy Ta''lim Vazirligi - Oliy ta''lim tizimi monitoring va tahlil', 'Ўзбекистон Республикаси Олий Таълим Вазирлиги - Олий таълим тизими мониторинг ва таҳлил', 'Министерство высшего образования Республики Узбекистан - Мониторинг и анализ системы высшего образования');
PERFORM _seed_msg('label', 'academic year',          'o''quv yili',                   'ўқув йили',                 'учебный год');

-- =====================================================
-- Students page
-- =====================================================
PERFORM _seed_msg('label', 'Student list and management', 'Barcha talabalar ro''yxati va boshqaruv tizimi', 'Барча талабалар рўйхати ва бошқарув тизими', 'Список и управление студентами');
PERFORM _seed_msg('action', 'New student',           'Yangi talaba',                 'Янги талаба',               'Новый студент');
PERFORM _seed_msg('status', 'On leave',              'Ta''tilda',                     'Таътилда',                  'В отпуске');
PERFORM _seed_msg('label', 'Grant recipients',       'Grantlilar',                   'Грантлилар',                'Грантополучатели');
PERFORM _seed_msg('label', 'Search and filter',      'Qidiruv va filtrlash',         'Қидирув ва филтрлаш',       'Поиск и фильтрация');
PERFORM _seed_msg('label', 'All HEIs',               'Barcha OTMlar',                'Барча ОТМлар',              'Все ВУЗы');
PERFORM _seed_msg('label', 'All',                    'Barchasi',                     'Барчаси',                   'Все');
PERFORM _seed_msg('label', 'Bachelor',               'Bakalavr',                     'Бакалавр',                  'Бакалавр');
PERFORM _seed_msg('label', 'Master',                 'Magistr',                      'Магистр',                   'Магистр');
PERFORM _seed_msg('label', 'students found',         'ta talaba topildi',            'та талаба топилди',         'студентов найдено');
PERFORM _seed_msg('action', 'Configure columns',     'Ustunlarni sozlash',           'Устунларни созлаш',         'Настроить колонки');
PERFORM _seed_msg('label', 'Student list',           'Talabalar ro''yxati',           'Талабалар рўйхати',         'Список студентов');
PERFORM _seed_msg('table', 'Full name',              'FIO',                          'ФИО',                       'ФИО');
PERFORM _seed_msg('table', 'Specialty',              'Mutaxassislik',                'Мутахассислик',             'Специальность');
PERFORM _seed_msg('table', 'Course',                 'Kurs',                         'Курс',                      'Курс');
PERFORM _seed_msg('table', 'Education type',         'Ta''lim turi',                  'Таълим тури',               'Тип образования');
PERFORM _seed_msg('table', 'Payment',                'To''lov',                       'Тўлов',                     'Оплата');
PERFORM _seed_msg('table', 'Payment form',           'To''lov shakli',                'Тўлов шакли',               'Форма оплаты');
PERFORM _seed_msg('label', 'Contract',               'Kontrakt',                     'Контракт',                  'Контракт');
PERFORM _seed_msg('label', 'Grant',                  'Grant',                        'Грант',                     'Грант');
PERFORM _seed_msg('label', 'Showing from',           'dan ko''rsatilmoqda',           'дан кўрсатилмоқда',         'показано из');
PERFORM _seed_msg('label', 'Search by full name, code, PINFL...', 'FIO, kod, PINFL bo''yicha qidirish...', 'ФИО, код, ПИНФЛ бўйича қидириш...', 'Поиск по ФИО, коду, ПИНФЛ...');

-- =====================================================
-- Teachers page
-- =====================================================
PERFORM _seed_msg('label', 'Teacher list and monitoring', 'Professor-o''qituvchilar ro''yxati va ilmiy faoliyat monitoring', 'Профессор-ўқитувчилар рўйхати ва илмий фаолият мониторинг', 'Список преподавателей и мониторинг научной деятельности');
PERFORM _seed_msg('action', 'New teacher',           'Yangi o''qituvchi',             'Янги ўқитувчи',             'Новый преподаватель');
PERFORM _seed_msg('label', 'Professors',             'Professorlar',                 'Профессорлар',              'Профессора');
PERFORM _seed_msg('label', 'Associate professors',   'Dotsentlar',                   'Дотсентлар',                'Доценты');
PERFORM _seed_msg('label', 'Doctor of science',      'Fan doktori',                  'Фан доктори',               'Доктор наук');
PERFORM _seed_msg('label', 'years',                  'yil',                          'йил',                       'лет');
PERFORM _seed_msg('label', 'teachers found',         'ta o''qituvchi topildi',        'та ўқитувчи топилди',       'преподавателей найдено');
-- NB: 'Teacher list' allaqachon menu kategoriyasida (qator 516) mavjud; dublikat olib tashlandi
PERFORM _seed_msg('table', 'Department',             'Kafedra',                      'Кафедра',                   'Кафедра');
PERFORM _seed_msg('table', 'Position',               'Lavozim',                      'Лавозим',                   'Должность');
PERFORM _seed_msg('table', 'Academic degree',        'Ilmiy daraja',                 'Илмий даража',              'Учёная степень');
PERFORM _seed_msg('table', 'Academic title',         'Ilmiy unvon',                  'Илмий унвон',               'Учёное звание');
PERFORM _seed_msg('table', 'Experience',             'Tajriba',                      'Тажриба',                   'Стаж');
PERFORM _seed_msg('table', 'Publications',           'Nashrlar',                     'Нашрлар',                   'Публикации');
PERFORM _seed_msg('table', 'Projects',               'Loyihalar',                    'Лойиҳалар',                 'Проекты');
PERFORM _seed_msg('label', 'No degree',              'Ilmiy daraja yo''q',            'Илмий даража йўқ',          'Нет степени');
PERFORM _seed_msg('label', 'Search by full name, PINFL, department...', 'FIO, PINFL, kafedra bo''yicha qidirish...', 'ФИО, ПИНФЛ, кафедра бўйича қидириш...', 'Поиск по ФИО, ПИНФЛ, кафедре...');

-- =====================================================
-- ErrorBoundary
-- =====================================================
PERFORM _seed_msg('message', 'An unexpected error occurred', 'Kutilmagan xatolik yuz berdi', 'Кутилмаган хатолик юз берди', 'Произошла непредвиденная ошибка');
PERFORM _seed_msg('message', 'An unexpected error occurred in the application. Please refresh the page or go to the home page.', 'Dasturda kutilmagan xatolik yuz berdi. Iltimos, sahifani yangilang yoki bosh sahifaga qayting.', 'Дастурда кутилмаган хатолик юз берди. Илтимос, саҳифани янгиланг ёки бош саҳифага қайтинг.', 'В приложении произошла непредвиденная ошибка. Пожалуйста, обновите страницу или вернитесь на главную.');
PERFORM _seed_msg('label', 'Error details',          'Xatolik tafsilotlari',         'Хатолик тафсилотлари',      'Детали ошибки');
PERFORM _seed_msg('label', 'Event ID (for support)', 'Event ID (qo''llab-quvvatlash uchun)', 'Event ID (қўллаб-қувватлаш учун)', 'Event ID (для поддержки)');
PERFORM _seed_msg('label', 'Send this ID to the support team', 'Bu ID''ni texnik yordam xizmatiga yuborishingiz mumkin.', 'Бу ID''ни техник ёрдам хизматига юборишингиз мумкин.', 'Отправьте этот ID в службу поддержки.');
PERFORM _seed_msg('label', 'Technical details (development only)', 'Texnik ma''lumot (faqat development)', 'Техник маълумот (фақат development)', 'Техническая информация (только для разработки)');
PERFORM _seed_msg('action', 'Refresh page',          'Sahifani yangilash',            'Саҳифани янгилаш',          'Обновить страницу');
PERFORM _seed_msg('action', 'Home page',             'Asosiy sahifa',                'Асосий саҳифа',             'Главная страница');

-- =====================================================
-- UniversityDetailDrawer
-- =====================================================
PERFORM _seed_msg('label', 'Address and location',   'Manzil va joylashuv',          'Манзил ва жойлашув',        'Адрес и местоположение');
PERFORM _seed_msg('label', 'Organizational info',    'Tashkiliy ma''lumotlar',        'Ташкилий маълумотлар',      'Организационная информация');
PERFORM _seed_msg('label', 'Websites',               'Veb-saytlar',                  'Веб-сайтлар',               'Веб-сайты');
PERFORM _seed_msg('label', 'Main university',        'Asosiy universitet',           'Асосий университет',        'Основной университет');
PERFORM _seed_msg('label', 'Additional info',        'Qo''shimcha ma''lumotlar',       'Қўшимча маълумотлар',       'Дополнительная информация');

-- =====================================================
-- UniversityFormDialog
-- =====================================================
PERFORM _seed_msg('label', 'Edit HEI',               'OTM tahrirlash',               'ОТМ таҳрирлаш',             'Редактирование ВУЗа');
PERFORM _seed_msg('label', 'Add new HEI',            'Yangi OTM qo''shish',           'Янги ОТМ қўшиш',           'Добавить новый ВУЗ');
PERFORM _seed_msg('label', 'Update data',            'Ma''lumotlarni yangilang',      'Маълумотларни янгиланг',    'Обновить данные');
PERFORM _seed_msg('label', 'Enter new university data', 'Yangi universitet ma''lumotlarini kiriting', 'Янги университет маълумотларини киритинг', 'Введите данные нового университета');
PERFORM _seed_msg('label', 'Basic',                  'Asosiy',                       'Асосий',                    'Основные');
PERFORM _seed_msg('label', 'Links',                  'Havolalar',                    'Ҳаволалар',                 'Ссылки');
PERFORM _seed_msg('label', 'Additional',             'Qo''shimcha',                   'Қўшимча',                   'Дополнительно');
PERFORM _seed_msg('validation', 'Code is required',  'Kod majburiy',                 'Код мажбурий',              'Код обязателен');
PERFORM _seed_msg('validation', 'Name is required',  'Nom majburiy',                 'Ном мажбурий',              'Название обязательно');
PERFORM _seed_msg('validation', 'Ownership type is required', 'Mulkchilik turi majburiy', 'Мулкчилик тури мажбурий', 'Тип собственности обязателен');
PERFORM _seed_msg('validation', 'HEI type is required', 'OTM turi majburiy',           'ОТМ тури мажбурий',         'Тип ВУЗа обязателен');
PERFORM _seed_msg('validation', 'Invalid URL',       'Noto''g''ri URL',                 'Нотўғри URL',               'Некорректный URL');
PERFORM _seed_msg('label', 'Full name of HEI',       'OTM to''liq nomi',                'ОТМ тўлиқ номи',            'Полное название ВУЗа');
PERFORM _seed_msg('label', 'Full address',            'To''liq manzil',                  'Тўлиқ манзил',              'Полный адрес');
-- Note: 'Allow external transfer' already defined at line 276 (table category) — not duplicated here
PERFORM _seed_msg('error', 'Failed to load dictionaries', 'Lug''atlar yuklanmadi',       'Луғатлар юкланмади',        'Не удалось загрузить справочники');
PERFORM _seed_msg('error', 'Failed to update university', 'OTM yangilanmadi',            'ОТМ янгиланмади',           'Не удалось обновить ВУЗ');
PERFORM _seed_msg('error', 'Failed to create university', 'OTM yaratilmadi',             'ОТМ яратилмади',            'Не удалось создать ВУЗ');

-- =====================================================
-- Reports page
-- =====================================================
PERFORM _seed_msg('label', 'Reports subtitle',       'Turli hisobotlar, tahlillar va statistik ma''lumotlar', 'Турли ҳисоботлар, таҳлиллар ва статистик маълумотлар', 'Различные отчёты, анализы и статистические данные');
PERFORM _seed_msg('action', 'Select academic year',  'O''quv yilini tanlash',         'Ўқув йилини танлаш',        'Выбрать учебный год');
PERFORM _seed_msg('action', 'All reports',           'Barcha hisobotlar',            'Барча ҳисоботлар',          'Все отчёты');
PERFORM _seed_msg('label', 'General reports',        'Umumiy hisobotlar',            'Умумий ҳисоботлар',         'Общие отчёты');
PERFORM _seed_msg('label', 'Monthly reports',        'Oy hisobotlari',               'Ой ҳисоботлари',            'Месячные отчёты');
PERFORM _seed_msg('label', 'Downloaded',             'Yuklab olindi',                'Юклаб олинди',              'Скачано');
PERFORM _seed_msg('label', 'Last update',            'Oxirgi yangilanish',            'Охирги янгиланиш',          'Последнее обновление');
PERFORM _seed_msg('label', 'reports',                'ta hisobot',                   'та ҳисобот',                'отчётов');
PERFORM _seed_msg('label', 'parameters',             'parametr',                     'параметр',                  'параметров');

-- =====================================================
-- TranslationFormPage
-- =====================================================
PERFORM _seed_msg('label', 'Edit translation',       'Tarjimani tahrirlash',         'Таржимани таҳрирлаш',       'Редактирование перевода');
PERFORM _seed_msg('label', 'Update existing translation', 'Mavjud tarjimani yangilang', 'Мавжуд таржимани янгиланг', 'Обновите существующий перевод');
PERFORM _seed_msg('label', 'Key',                    'Kalit',                        'Калит',                     'Ключ');
PERFORM _seed_msg('label', 'Translations section',   'Tarjimalar bo''limi',           'Таржималар бўлими',         'Раздел переводов');
PERFORM _seed_msg('validation', 'Category is required', 'Kategoriya majburiy',       'Категория мажбурий',        'Категория обязательна');
PERFORM _seed_msg('validation', 'Key is required',   'Kalit majburiy',               'Калит мажбурий',            'Ключ обязателен');
PERFORM _seed_msg('validation', 'Key must contain only letters, numbers, dots and underscores', 'Kalit faqat harf, raqam, nuqta va pastki chiziqdan iborat bo''lishi kerak', 'Калит фақат ҳарф, рақам, нуқта ва пастки чизиқдан иборат бўлиши керак', 'Ключ может содержать только буквы, цифры, точки и подчёркивания');
PERFORM _seed_msg('validation', 'Uzbek (Latin) is required', 'O''zbek (lotin) majburiy', 'Ўзбек (лотин) мажбурий', 'Узбекский (латиница) обязателен');
PERFORM _seed_msg('label', 'Help section',           'Yordam',                       'Ёрдам',                     'Помощь');
PERFORM _seed_msg('label', 'Similar translations found', 'O''xshash tarjimalar topildi', 'Ўхшаш таржималар топилди', 'Найдены похожие переводы');
PERFORM _seed_msg('label', 'Key cannot be changed',  'Kalitni o''zgartirish mumkin emas', 'Калитни ўзгартириш мумкин эмас', 'Ключ нельзя изменить');
PERFORM _seed_msg('label', 'Active (ready for use)', 'Aktiv (foydalanish uchun tayyor)', 'Актив (фойдаланиш учун тайёр)', 'Активен (готов к использованию)');
PERFORM _seed_msg('label', 'If inactive, frontend will not see this translation', 'Agar faol emas bo''lsa, frontend tarjimani ko''rmaydi', 'Агар фаол эмас бўлса, фронтенд таржимани кўрмайди', 'Если неактивен, фронтенд не увидит этот перевод');
PERFORM _seed_msg('message', 'Translation successfully updated', 'Tarjima muvaffaqiyatli yangilandi!', 'Таржима муваффақиятли янгиланди!', 'Перевод успешно обновлён!');
PERFORM _seed_msg('message', 'Error saving translation', 'Tarjimani saqlashda xatolik', 'Таржимани сақлашда хатолик', 'Ошибка сохранения перевода');
PERFORM _seed_msg('message', 'If existing translation fits your needs, use it instead of adding new', 'Agar mavjud tarjima sizning ehtiyojingizni qoplasa, yangi qo''shmasdan uni ishlating.', 'Агар мавжуд таржима сизнинг эҳтиёжингизни қоплаcа, янги қўшмасдан уни ишлатинг.', 'Если существующий перевод подходит, используйте его вместо добавления нового.');
PERFORM _seed_msg('message', 'Translation cache cleared on all servers', 'Tarjima keshi barcha serverlarda tozalandi', 'Таржима кеши барча серверларда тозаланди', 'Кэш переводов успешно очищен на всех серверах');
PERFORM _seed_msg('label', 'and more',               'va yana',                      'ва яна',                    'и ещё');

-- =====================================================
-- Login page (split layout redesign)
-- =====================================================
PERFORM _seed_msg('auth', 'Sign in to system',      'Tizimga kirish',               'Тизимга кириш',             'Вход в систему');
PERFORM _seed_msg('auth', 'Enter your credentials to continue', 'Davom etish uchun ma''lumotlaringizni kiriting', 'Давом этиш учун маълумотларингизни киритинг', 'Введите данные для продолжения');
PERFORM _seed_msg('label', '226+ higher education institutions', '226+ oliy ta''lim muassasalari', '226+ олий таълим муассасалари', '226+ высших учебных заведений');
PERFORM _seed_msg('label', '1,000,000+ users',      '1,000,000+ foydalanuvchilar',  '1,000,000+ фойдаланувчилар', '1,000,000+ пользователей');
PERFORM _seed_msg('label', '4 management modules',  '4 ta boshqaruv moduli',        '4 та бошқарув модули',      '4 модуля управления');
PERFORM _seed_msg('label', 'Ministry of Higher Education, Science and Innovations', 'Oliy ta''lim, fan va innovatsiyalar vazirligi', 'Олий таълим, фан ва инновациялар вазирлиги', 'Министерство высшего образования, науки и инноваций');

-- =====================================================
-- i18n fixes (2026-02-05)
-- =====================================================
PERFORM _seed_msg('label', 'PhD/DSc',                'PhD/DSc',                      'PhD/DSc',                   'PhD/DSc');
PERFORM _seed_msg('label', 'UZBMB URL',              'UZBMB URL',                    'UZBMB URL',                 'URL UZBMB');
PERFORM _seed_msg('action', 'Search by {{field}}...', '{{field}} bo''yicha qidirish...', '{{field}} бўйича қидириш...', 'Поиск по {{field}}...');




-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 6: AUDIT LOGS (from S007)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Sahifa sarlavhalari
PERFORM _seed_msg('menu', 'Audit Logs', 'Audit loglar', 'Аудит логлар', 'Журнал аудита');
PERFORM _seed_msg('label', 'Activity Log', 'Faoliyat logi', 'Фаолият логи', 'Журнал действий');
PERFORM _seed_msg('label', 'Request Log', 'So''rovlar logi', 'Сўровлар логи', 'Журнал запросов');
PERFORM _seed_msg('label', 'Error Log', 'Xatolar logi', 'Хатолар логи', 'Журнал ошибок');
PERFORM _seed_msg('label', 'Login Log', 'Kirish logi', 'Кириш логи', 'Журнал входов');

-- Jadval ustunlari
PERFORM _seed_msg('table', 'User', 'Foydalanuvchi', 'Фойдаланувчи', 'Пользователь');
PERFORM _seed_msg('table', 'Action', 'Harakat', 'Ҳаракат', 'Действие');
PERFORM _seed_msg('table', 'Entity Type', 'Ob''yekt turi', 'Объект тури', 'Тип объекта');
PERFORM _seed_msg('table', 'Entity Name', 'Ob''yekt nomi', 'Объект номи', 'Название объекта');
PERFORM _seed_msg('table', 'IP Address', 'IP manzil', 'IP манзил', 'IP адрес');
PERFORM _seed_msg('table', 'Method', 'Metod', 'Метод', 'Метод');
PERFORM _seed_msg('table', 'URI', 'URI', 'URI', 'URI');
PERFORM _seed_msg('table', 'Status Code', 'Status kodi', 'Статус коди', 'Код статуса');
PERFORM _seed_msg('table', 'Response Time', 'Javob vaqti', 'Жавоб вақти', 'Время ответа');
PERFORM _seed_msg('table', 'Error Type', 'Xato turi', 'Хато тури', 'Тип ошибки');
PERFORM _seed_msg('table', 'Error Message', 'Xato xabari', 'Хато хабари', 'Сообщение ошибки');
PERFORM _seed_msg('table', 'Event Type', 'Hodisa turi', 'Ҳодиса тури', 'Тип события');
PERFORM _seed_msg('table', 'Failure Reason', 'Xatolik sababi', 'Хатолик сабаби', 'Причина ошибки');
PERFORM _seed_msg('table', 'Timestamp', 'Vaqt', 'Вақт', 'Время');
PERFORM _seed_msg('table', 'User Agent', 'User Agent', 'User Agent', 'User Agent');
PERFORM _seed_msg('table', 'Session ID', 'Sessiya ID', 'Сессия ID', 'ID сессии');
PERFORM _seed_msg('table', 'Request ID', 'So''rov ID', 'Сўров ID', 'ID запроса');

-- Tafsilot
PERFORM _seed_msg('label', 'Old Value', 'Eski qiymat', 'Эски қиймат', 'Старое значение');
PERFORM _seed_msg('label', 'New Value', 'Yangi qiymat', 'Янги қиймат', 'Новое значение');
PERFORM _seed_msg('label', 'Changed Fields', 'O''zgargan maydonlar', 'Ўзгарган майдонлар', 'Изменённые поля');
PERFORM _seed_msg('label', 'Stack Trace', 'Stack trace', 'Stack trace', 'Трассировка стека');
PERFORM _seed_msg('label', 'Request Body', 'So''rov tanasi', 'Сўров танаси', 'Тело запроса');
PERFORM _seed_msg('label', 'Response Body', 'Javob tanasi', 'Жавоб танаси', 'Тело ответа');
PERFORM _seed_msg('label', 'Query String', 'So''rov parametrlari', 'Сўров параметрлари', 'Параметры запроса');

-- Harakatlar
PERFORM _seed_msg('action', 'View Details', 'Tafsilotlarni ko''rish', 'Тафсилотларни кўриш', 'Просмотр деталей');
PERFORM _seed_msg('action', 'Export Logs', 'Loglarni eksport qilish', 'Логларни экспорт қилиш', 'Экспорт журналов');
PERFORM _seed_msg('action', 'Refresh data', 'Yangilash', 'Янгилаш', 'Обновить данные');

-- Filtrlar
PERFORM _seed_msg('label', 'Filter by user', 'Foydalanuvchi bo''yicha filtrlash', 'Фойдаланувчи бўйича фильтрлаш', 'Фильтр по пользователю');
PERFORM _seed_msg('label', 'Filter by action', 'Harakat bo''yicha filtrlash', 'Ҳаракат бўйича фильтрлаш', 'Фильтр по действию');
PERFORM _seed_msg('label', 'Filter by date', 'Sana bo''yicha filtrlash', 'Сана бўйича фильтрлаш', 'Фильтр по дате');
PERFORM _seed_msg('label', 'Date range', 'Sana oralig''i', 'Сана оралиғи', 'Диапазон дат');

-- Statistika
PERFORM _seed_msg('label', 'Statistics', 'Statistika', 'Статистика', 'Статистика');
PERFORM _seed_msg('label', 'Top Users', 'Eng faol foydalanuvchilar', 'Энг фаол фойдаланувчилар', 'Топ пользователей');
PERFORM _seed_msg('label', 'Top Endpoints', 'Eng ko''p so''rovlar', 'Энг кўп сўровлар', 'Топ эндпоинтов');
PERFORM _seed_msg('label', 'Error Rate', 'Xatolar darajasi', 'Хатолар даражаси', 'Частота ошибок');
PERFORM _seed_msg('label', 'Total Activities', 'Jami faoliyatlar', 'Жами фаолиятлар', 'Всего действий');
PERFORM _seed_msg('label', 'Total Requests', 'Jami so''rovlar', 'Жами сўровлар', 'Всего запросов');
PERFORM _seed_msg('label', 'Total Errors', 'Jami xatolar', 'Жами хатолар', 'Всего ошибок');
PERFORM _seed_msg('label', 'Total Logins', 'Jami kirishlar', 'Жами киришлар', 'Всего входов');

-- Login event types
PERFORM _seed_msg('status', 'Login Success', 'Muvaffaqiyatli kirish', 'Муваффақиятли кириш', 'Успешный вход');
PERFORM _seed_msg('status', 'Login Failed', 'Muvaffaqiyatsiz kirish', 'Муваффақиятсиз кириш', 'Неудачный вход');
PERFORM _seed_msg('status', 'Logout', 'Chiqish', 'Чиқиш', 'Выход');
PERFORM _seed_msg('status', 'Token Refresh', 'Token yangilash', 'Токен янгилаш', 'Обновление токена');
PERFORM _seed_msg('status', 'Session Expired', 'Sessiya tugadi', 'Сессия тугади', 'Сессия истекла');

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 7: AUDIT DIFF (from S008)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Diff Viewer labels
-- Note: 'Changed Fields', 'Old Value', 'New Value' already defined in PART 6 — not duplicated here
PERFORM _seed_msg('label', 'Field', 'Maydon', 'Майдон', 'Поле');
PERFORM _seed_msg('label', 'Value', 'Qiymat', 'Қиймат', 'Значение');
PERFORM _seed_msg('label', 'Unchanged Fields', 'O''zgarmagan maydonlar', 'Ўзгармаган майдонлар', 'Неизмененные поля');

-- Entity History
PERFORM _seed_msg('label', 'Entity History', 'O''zgarishlar tarixi', 'Ўзгаришлар тарихи', 'История изменений');

-- Date Range Filter
PERFORM _seed_msg('label', 'Today', 'Bugun', 'Бугун', 'Сегодня');
PERFORM _seed_msg('label', 'Last 7 days', 'Oxirgi 7 kun', 'Охирги 7 кун', 'Последние 7 дней');
PERFORM _seed_msg('label', 'Last 30 days', 'Oxirgi 30 kun', 'Охирги 30 кун', 'Последние 30 дней');
PERFORM _seed_msg('label', 'All time', 'Barcha vaqt', 'Барча вақт', 'Все время');
PERFORM _seed_msg('label', 'Date from', 'Dan', 'Дан', 'С');
PERFORM _seed_msg('label', 'Date to', 'Gacha', 'Гача', 'По');

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 8: CLASSIFIERS (from S011)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- New category menu names
PERFORM _seed_msg('menu', 'Financial', 'Moliyaviy', 'Молиявий', 'Финансовые');
PERFORM _seed_msg('menu', 'Diploma', 'Diplom', 'Диплом', 'Дипломы');
PERFORM _seed_msg('menu', 'Specialities', 'Mutaxassisliklar', 'Мутахассисликлар', 'Специальности');

-- Classifier CRUD messages
PERFORM _seed_msg('message', 'Classifier item created', 'Element muvaffaqiyatli yaratildi', 'Элемент муваффақиятли яратилди', 'Элемент успешно создан');
PERFORM _seed_msg('message', 'Classifier item updated', 'Element muvaffaqiyatli yangilandi', 'Элемент муваффақиятли янгиланди', 'Элемент успешно обновлен');
PERFORM _seed_msg('message', 'Classifier item deleted', 'Element muvaffaqiyatli o''chirildi', 'Элемент муваффақиятли ўчирилди', 'Элемент успешно удален');
PERFORM _seed_msg('message', 'Classifier not found', 'Klasifikator topilmadi', 'Классификатор топилмади', 'Классификатор не найден');
PERFORM _seed_msg('message', 'Item already exists', 'Bu kodli element allaqachon mavjud', 'Бу кодли элемент аллақачон мавжуд', 'Элемент с таким кодом уже существует');
PERFORM _seed_msg('message', 'Read-only classifier', 'Bu klasifikator faqat o''qish uchun', 'Бу классификатор фақат ўқиш учун', 'Этот классификатор только для чтения');

-- Classifier-specific labels
PERFORM _seed_msg('label', 'Editable', 'Tahrirlanadigan', 'Таҳрирланадиган', 'Редактируемый');
PERFORM _seed_msg('label', 'Read only', 'Faqat o''qish', 'Фақат ўқиш', 'Только чтение');
PERFORM _seed_msg('label', 'Hierarchical', 'Ierarxik', 'Иерархик', 'Иерархический');
PERFORM _seed_msg('label', 'Items count', 'Elementlar soni', 'Элементлар сони', 'Количество элементов');

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 9: DUPLICATE DETECTION (from S013)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Page title and description
PERFORM _seed_msg('menu', 'Duplicates', 'Dublikatlar', 'Дубликатлар', 'Дубликаты');
PERFORM _seed_msg('label', 'Students with duplicate PINFL analysis', 'PINFL dublikatli talabalar tahlili', 'ПИНФЛ дубликатли талабалар таҳлили', 'Анализ студентов с дублирующимися ПИНФЛ');

-- Stats cards
PERFORM _seed_msg('label', 'Total duplicate PINFLs', 'Jami dublikat PINFLlar', 'Жами дубликат ПИНФЛлар', 'Всего дублирующихся ПИНФЛ');
PERFORM _seed_msg('label', 'Cross university', 'Boshqa OTMda', 'Бошқа ОТМда', 'В другом ВУЗе');
PERFORM _seed_msg('label', 'Same university', 'Bitta OTMda', 'Битта ОТМда', 'В одном ВУЗе');
PERFORM _seed_msg('label', 'Multi level', 'Turli bosqich', 'Турли босқич', 'Разные уровни');
PERFORM _seed_msg('status', 'Normal', 'Normal', 'Нормал', 'Нормально');

-- Table and groups
PERFORM _seed_msg('label', 'records', 'yozuv', 'ёзув', 'записей');
PERFORM _seed_msg('label', 'universities', 'OTM', 'ОТМ', 'ВУЗов');
PERFORM _seed_msg('message', '{{count}} groups found', '{{count}} guruh topildi', '{{count}} гуруҳ топилди', '{{count}} групп найдено');
PERFORM _seed_msg('message', 'No duplicate records found', 'Dublikat yozuvlar topilmadi', 'Дубликат ёзувлар топилмади', 'Дублирующиеся записи не найдены');

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 10: TRANSFER DETECTION (from S014)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Transfer category labels
PERFORM _seed_msg('label', 'Internal transfer', 'Ichki ko''chirish', 'Ички кўчириш', 'Внутренний перевод');
PERFORM _seed_msg('label', 'External transfer', 'Tashqi ko''chirish', 'Ташқи кўчириш', 'Внешний перевод');

-- Speciality label for detail drawer
PERFORM _seed_msg('label', 'Speciality', 'Mutaxassislik', 'Мутахассислик', 'Специальность');

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 11: DIRECTIONS (from S015)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Page description
-- Note: 'Directions' already defined in PART 3 — not duplicated here
PERFORM _seed_msg('label', 'Speciality statistics by education type', 'Ta''lim turi bo''yicha mutaxassislik statistikasi', 'Таълим тури бўйича мутахассислик статистикаси', 'Статистика специальностей по типу образования');

-- Summary cards
PERFORM _seed_msg('label', 'Total specialities', 'Jami mutaxassisliklar', 'Жами мутахассисликлар', 'Всего специальностей');
PERFORM _seed_msg('label', 'With students', 'Talabasi bor', 'Талабаси бор', 'Со студентами');
PERFORM _seed_msg('label', 'Without students', 'Talabasi yo''q', 'Талабаси йўқ', 'Без студентов');
PERFORM _seed_msg('label', 'Total students in specialities', 'Mutaxassisliklardagi jami talabalar', 'Мутахассисликлардаги жами талабалар', 'Всего студентов в специальностях');

-- Education types
PERFORM _seed_msg('label', 'Ordinatura', 'Ordinatura', 'Ординатура', 'Ординатура');

-- Table columns
-- Note: 'Education type' already defined in PART 4, 'Expelled' already defined in PART 4 — not duplicated here
PERFORM _seed_msg('table', 'Speciality code', 'Mutaxassislik kodi', 'Мутахассислик коди', 'Код специальности');
PERFORM _seed_msg('table', 'Speciality name', 'Mutaxassislik nomi', 'Мутахассислик номи', 'Название специальности');
PERFORM _seed_msg('table', 'Total students', 'Jami talabalar', 'Жами талабалар', 'Всего студентов');
PERFORM _seed_msg('table', 'Active students', 'Faol talabalar', 'Фаол талабалар', 'Активные студенты');
PERFORM _seed_msg('label', 'Graduated', 'Bitirganlar', 'Битирганлар', 'Выпускники');

-- Filters
PERFORM _seed_msg('label', 'Search by speciality...', 'Mutaxassislik bo''yicha qidirish...', 'Мутахассислик бўйича қидириш...', 'Поиск по специальности...');
PERFORM _seed_msg('label', 'Has students', 'Talabasi bor', 'Талабаси бор', 'Есть студенты');
PERFORM _seed_msg('label', 'No students', 'Talabasi yo''q', 'Талабаси йўқ', 'Без студентов');

-- Results
PERFORM _seed_msg('message', '{{count}} specialities found', '{{count}} mutaxassislik topildi', '{{count}} мутахассислик топилди', '{{count}} специальностей найдено');

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 12: USER MANAGEMENT (from S017)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Page & Search
PERFORM _seed_msg('label',   'Search users...',                'Foydalanuvchilarni qidirish...',          'Фойдаланувчиларни қидириш...',             'Поиск пользователей...');
PERFORM _seed_msg('action',  'Create user',                    'Foydalanuvchi yaratish',                  'Фойдаланувчи яратиш',                     'Создать пользователя');
PERFORM _seed_msg('action',  'Edit user',                      'Foydalanuvchini tahrirlash',              'Фойдаланувчини таҳрирлаш',                 'Редактировать пользователя');
PERFORM _seed_msg('label',   'Update user profile and roles',  'Profilni va rollarni yangilash',          'Профилни ва ролларни янгилаш',             'Обновить профиль и роли');
PERFORM _seed_msg('label',   'Create a new system user',       'Yangi tizim foydalanuvchisi yaratish',    'Янги тизим фойдаланувчиси яратиш',         'Создать нового пользователя');

-- Delete Confirmation
PERFORM _seed_msg('action',  'Delete user',                           'O''chirish',                             'Ўчириш',                                  'Удалить пользователя');
PERFORM _seed_msg('confirm', 'Are you sure you want to delete user',  'Haqiqatan ham o''chirmoqchimisiz',       'Ҳақиқатан ҳам ўчирмоқчимисиз',            'Вы уверены что хотите удалить');
PERFORM _seed_msg('confirm', 'This action cannot be undone.',         'Bu amalni ortga qaytarib bo''lmaydi.',   'Бу амални ортга қайтариб бўлмайди.',       'Это действие нельзя отменить.');

-- Empty States
PERFORM _seed_msg('message', 'No users found',                        'Foydalanuvchilar topilmadi',             'Фойдаланувчилар топилмади',                'Пользователи не найдены');
PERFORM _seed_msg('message', 'No users have been created yet',        'Hali foydalanuvchi yaratilmagan',        'Ҳали фойдаланувчи яратилмаган',            'Пользователи еще не созданы');
PERFORM _seed_msg('message', 'Try changing your search or filters',   'Qidiruv yoki filterni o''zgartiring',    'Қидирув ёки филтрни ўзгартиринг',          'Измените поиск или фильтры');

-- Filters
PERFORM _seed_msg('label',   'All roles',                      'Barcha rollar',                           'Барча роллар',                             'Все роли');
PERFORM _seed_msg('label',   'All statuses',                   'Barcha holatlar',                         'Барча ҳолатлар',                           'Все статусы');
PERFORM _seed_msg('label',   'All universities',               'Barcha universitetlar',                   'Барча университетлар',                     'Все университеты');

-- Success Messages
PERFORM _seed_msg('message', 'User successfully created',      'Foydalanuvchi muvaffaqiyatli yaratildi',  'Фойдаланувчи муваффақиятли яратилди',      'Пользователь успешно создан');
PERFORM _seed_msg('message', 'User successfully updated',      'Foydalanuvchi muvaffaqiyatli yangilandi', 'Фойдаланувчи муваффақиятли янгиланди',     'Пользователь успешно обновлён');
PERFORM _seed_msg('message', 'User successfully deleted',      'Foydalanuvchi muvaffaqiyatli o''chirildi','Фойдаланувчи муваффақиятли ўчирилди',      'Пользователь успешно удалён');
PERFORM _seed_msg('message', 'Password successfully changed',  'Parol muvaffaqiyatli o''zgartirildi',     'Парол муваффақиятли ўзгартирилди',         'Пароль успешно изменён');
PERFORM _seed_msg('message', 'Account successfully unlocked',  'Hisob muvaffaqiyatli ochildi',            'Ҳисоб муваффақиятли очилди',               'Аккаунт успешно разблокирован');
PERFORM _seed_msg('message', 'User status updated',            'Holat yangilandi',                        'Ҳолат янгиланди',                          'Статус обновлён');

-- Password Dialog
PERFORM _seed_msg('action',     'Change password',                                                'Parolni o''zgartirish',                 'Паролни ўзгартириш',                    'Изменить пароль');
PERFORM _seed_msg('label',      'Change password for',                                            'Parol o''zgartirish',                   'Парол ўзгартириш',                      'Изменить пароль для');
PERFORM _seed_msg('label',      'New password',                                                   'Yangi parol',                           'Янги парол',                            'Новый пароль');
PERFORM _seed_msg('label',      'Confirm password',                                               'Parolni tasdiqlang',                    'Паролни тасдиқланг',                    'Подтвердите пароль');
PERFORM _seed_msg('validation', 'Password must be at least 8 characters',                         'Parol kamida 8 belgidan iborat',        'Парол камида 8 белгидан иборат',        'Пароль минимум 8 символов');
-- Note: 'Passwords do not match' already defined in PART 1 — not duplicated here

-- Form Fields
PERFORM _seed_msg('validation', 'Username must be 3-50 characters, letters, digits, _, ., -',     '3-50 belgi, harf, raqam, _, ., -',      '3-50 белги, ҳарф, рақам, _, ., -',      '3-50 символов, буквы, цифры, _, ., -');
-- Note: 'University code' already defined in PART 2 — not duplicated here
PERFORM _seed_msg('label',      'Select university',                                              'Universitetni tanlang',                 'Университетни танланг',                  'Выберите университет');
PERFORM _seed_msg('label',      'Leave empty for system administrators',                          'Tizim adminlari uchun bo''sh qoldiring','Тизим админлари учун бўш қолдиринг',    'Оставьте пустым для админов');
PERFORM _seed_msg('message',    'No roles available',                                             'Rollar mavjud emas',                    'Роллар мавжуд эмас',                    'Роли недоступны');
PERFORM _seed_msg('validation', 'At least one role is required',                                  'Kamida bitta rol kerak',                'Камида битта рол керак',                'Требуется минимум одна роль');

-- Status Actions
PERFORM _seed_msg('action', 'Disable',  'O''chirish',       'Ўчириш',          'Отключить');
PERFORM _seed_msg('action', 'Enable',   'Yoqish',           'Ёқиш',            'Включить');
PERFORM _seed_msg('action', 'Unlock',   'Qulfni ochish',    'Қулфни очиш',     'Разблокировать');
PERFORM _seed_msg('status', 'Locked',   'Qulflangan',       'Қулфланган',       'Заблокирован');

-- Helper function _seed_msg remains available for subsequent translation seeds (S009, ...).
-- Intentionally NOT dropped — CREATE OR REPLACE keeps it idempotent on re-runs.

END $$;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE _count INT;
BEGIN
    SELECT COUNT(*) INTO _count FROM system_message
    WHERE category IN (
        'action', 'status', 'label', 'message', 'validation',
        'table', 'pagination', 'confirm', 'auth', 'menu', 'error'
    );
    RAISE NOTICE 'S006: Total translation keys seeded: % (expected: 616)', _count;
END $$;
