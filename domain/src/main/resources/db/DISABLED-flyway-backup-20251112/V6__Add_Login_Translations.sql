-- =====================================================
-- V6: Add Login Page Translations
-- =====================================================
-- Author: Claude AI
-- Date: 2025-11-10
-- Purpose: Add 39 translation keys for login page
-- Languages: uz-UZ, oz-UZ, ru-RU, en-US
-- =====================================================

-- =====================================================
-- STEP 1: INSERT SYSTEM MESSAGES (39 keys)
-- =====================================================

-- Login Page Main Labels
INSERT INTO h_system_message (id, category, message_key, message, is_active) VALUES
(gen_random_uuid(), 'login', 'login.title', 'HEMIS Admin Panel', TRUE),
(gen_random_uuid(), 'login', 'login.subtitle', 'Oliy ta''lim boshqaruv axborot tizimi', TRUE),
(gen_random_uuid(), 'login', 'login.powered_by', 'AI va Analytics asosida', TRUE),
(gen_random_uuid(), 'login', 'login.username', 'Foydalanuvchi nomi', TRUE),
(gen_random_uuid(), 'login', 'login.username_placeholder', 'Foydalanuvchi nomingizni kiriting', TRUE),
(gen_random_uuid(), 'login', 'login.password', 'Parol', TRUE),
(gen_random_uuid(), 'login', 'login.password_placeholder', 'Parolingizni kiriting', TRUE),
(gen_random_uuid(), 'login', 'login.language', 'Til', TRUE),
(gen_random_uuid(), 'login', 'login.login_button', 'Kirish', TRUE),
(gen_random_uuid(), 'login', 'login.logging_in', 'Autentifikatsiya...', TRUE),
(gen_random_uuid(), 'login', 'login.welcome_back', 'Xush kelibsiz!', TRUE);

-- Statistics Section
INSERT INTO h_system_message (id, category, message_key, message, is_active) VALUES
(gen_random_uuid(), 'login', 'login.stats.universities', 'Universitetlar', TRUE),
(gen_random_uuid(), 'login', 'login.stats.students', 'Talabalar', TRUE),
(gen_random_uuid(), 'login', 'login.stats.analytics', 'AI Tahlil', TRUE),
(gen_random_uuid(), 'login', 'login.stats.reports', 'Hisobotlar', TRUE);

-- Features Section
INSERT INTO h_system_message (id, category, message_key, message, is_active) VALUES
(gen_random_uuid(), 'login', 'login.features.realtime', 'Real vaqt tahlili', TRUE),
(gen_random_uuid(), 'login', 'login.features.ai', 'AI Bashorat', TRUE),
(gen_random_uuid(), 'login', 'login.features.digital', 'Raqamlashtirish', TRUE),
(gen_random_uuid(), 'login', 'login.features.secure', 'Xavfsiz tizim', TRUE);

-- Error Messages
INSERT INTO h_system_message (id, category, message_key, message, is_active) VALUES
(gen_random_uuid(), 'login', 'login.errors.required', 'Ushbu maydon to''ldirilishi shart', TRUE),
(gen_random_uuid(), 'login', 'login.errors.invalid_credentials', 'Noto''g''ri foydalanuvchi nomi yoki parol', TRUE),
(gen_random_uuid(), 'login', 'login.errors.min_length', 'Kamida {count} ta belgi bo''lishi kerak', TRUE),
(gen_random_uuid(), 'login', 'login.errors.user_disabled', 'Foydalanuvchi hisobi o''chirilgan', TRUE),
(gen_random_uuid(), 'login', 'login.errors.no_university', 'Universitet biriktirilmagan', TRUE),
(gen_random_uuid(), 'login', 'login.errors.university_inactive', 'Universitet faol emas', TRUE),
(gen_random_uuid(), 'login', 'login.errors.network_error', 'Tarmoq xatoligi', TRUE),
(gen_random_uuid(), 'login', 'login.errors.required_field', '{field} to''ldirilishi shart', TRUE);

-- Common Messages
INSERT INTO h_system_message (id, category, message_key, message, is_active) VALUES
(gen_random_uuid(), 'common', 'common.loading', 'Yuklanmoqda...', TRUE),
(gen_random_uuid(), 'common', 'common.error', 'Xatolik', TRUE),
(gen_random_uuid(), 'common', 'common.success', 'Muvaffaqiyatli', TRUE),
(gen_random_uuid(), 'common', 'common.retry', 'Qayta urinish', TRUE);

-- =====================================================
-- STEP 2: INSERT TRANSLATIONS (oz-UZ - Uzbek Cyrillic)
-- =====================================================

-- Login Page Main Labels (Cyrillic)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.title'), 'oz-UZ', 'HEMIS Админ Панели'),
((SELECT id FROM h_system_message WHERE message_key = 'login.subtitle'), 'oz-UZ', 'Олий таълим бошқарув ахборот тизими'),
((SELECT id FROM h_system_message WHERE message_key = 'login.powered_by'), 'oz-UZ', 'AI ва Analytics асосида'),
((SELECT id FROM h_system_message WHERE message_key = 'login.username'), 'oz-UZ', 'Фойдаланувчи номи'),
((SELECT id FROM h_system_message WHERE message_key = 'login.username_placeholder'), 'oz-UZ', 'Фойдаланувчи номингизни киритинг'),
((SELECT id FROM h_system_message WHERE message_key = 'login.password'), 'oz-UZ', 'Парол'),
((SELECT id FROM h_system_message WHERE message_key = 'login.password_placeholder'), 'oz-UZ', 'Паролингизни киритинг'),
((SELECT id FROM h_system_message WHERE message_key = 'login.language'), 'oz-UZ', 'Тил'),
((SELECT id FROM h_system_message WHERE message_key = 'login.login_button'), 'oz-UZ', 'Кириш'),
((SELECT id FROM h_system_message WHERE message_key = 'login.logging_in'), 'oz-UZ', 'Аутентификатсия...'),
((SELECT id FROM h_system_message WHERE message_key = 'login.welcome_back'), 'oz-UZ', 'Хуш келибсиз!');

-- Statistics (Cyrillic)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.universities'), 'oz-UZ', 'Университетлар'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.students'), 'oz-UZ', 'Талабалар'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.analytics'), 'oz-UZ', 'AI Таҳлил'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.reports'), 'oz-UZ', 'Ҳисоботлар');

-- Features (Cyrillic)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.features.realtime'), 'oz-UZ', 'Реал вақт таҳлили'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.ai'), 'oz-UZ', 'AI Башорат'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.digital'), 'oz-UZ', 'Рақамлаштириш'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.secure'), 'oz-UZ', 'Хавфсиз тизим');

-- Errors (Cyrillic)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.required'), 'oz-UZ', 'Ушбу майдон тўлдирилиши шарт'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.invalid_credentials'), 'oz-UZ', 'Нотўғри фойдаланувчи номи ёки парол'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.min_length'), 'oz-UZ', 'Камида {count} та белги бўлиши керак'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.user_disabled'), 'oz-UZ', 'Фойдаланувчи ҳисоби ўчирилган'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.no_university'), 'oz-UZ', 'Университет бириктирилмаган'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.university_inactive'), 'oz-UZ', 'Университет фаол эмас'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.network_error'), 'oz-UZ', 'Тармоқ хатолиги'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.required_field'), 'oz-UZ', '{field} тўлдирилиши шарт');

-- Common (Cyrillic)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'common.loading'), 'oz-UZ', 'Юкланмоқда...'),
((SELECT id FROM h_system_message WHERE message_key = 'common.error'), 'oz-UZ', 'Хатолик'),
((SELECT id FROM h_system_message WHERE message_key = 'common.success'), 'oz-UZ', 'Муваффақиятли'),
((SELECT id FROM h_system_message WHERE message_key = 'common.retry'), 'oz-UZ', 'Қайта уриниш');

-- =====================================================
-- STEP 3: INSERT TRANSLATIONS (ru-RU - Russian)
-- =====================================================

-- Login Page Main Labels (Russian)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.title'), 'ru-RU', 'HEMIS Админ Панель'),
((SELECT id FROM h_system_message WHERE message_key = 'login.subtitle'), 'ru-RU', 'Информационная система управления высшим образованием'),
((SELECT id FROM h_system_message WHERE message_key = 'login.powered_by'), 'ru-RU', 'На основе AI и Analytics'),
((SELECT id FROM h_system_message WHERE message_key = 'login.username'), 'ru-RU', 'Имя пользователя'),
((SELECT id FROM h_system_message WHERE message_key = 'login.username_placeholder'), 'ru-RU', 'Введите имя пользователя'),
((SELECT id FROM h_system_message WHERE message_key = 'login.password'), 'ru-RU', 'Пароль'),
((SELECT id FROM h_system_message WHERE message_key = 'login.password_placeholder'), 'ru-RU', 'Введите пароль'),
((SELECT id FROM h_system_message WHERE message_key = 'login.language'), 'ru-RU', 'Язык'),
((SELECT id FROM h_system_message WHERE message_key = 'login.login_button'), 'ru-RU', 'Войти'),
((SELECT id FROM h_system_message WHERE message_key = 'login.logging_in'), 'ru-RU', 'Аутентификация...'),
((SELECT id FROM h_system_message WHERE message_key = 'login.welcome_back'), 'ru-RU', 'Добро пожаловать!');

-- Statistics (Russian)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.universities'), 'ru-RU', 'Университеты'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.students'), 'ru-RU', 'Студенты'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.analytics'), 'ru-RU', 'AI Аналитика'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.reports'), 'ru-RU', 'Отчеты');

-- Features (Russian)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.features.realtime'), 'ru-RU', 'Анализ в реальном времени'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.ai'), 'ru-RU', 'AI Прогнозы'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.digital'), 'ru-RU', 'Цифровизация'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.secure'), 'ru-RU', 'Безопасная система');

-- Errors (Russian)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.required'), 'ru-RU', 'Это поле обязательно для заполнения'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.invalid_credentials'), 'ru-RU', 'Неверное имя пользователя или пароль'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.min_length'), 'ru-RU', 'Минимум {count} символов'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.user_disabled'), 'ru-RU', 'Учетная запись пользователя отключена'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.no_university'), 'ru-RU', 'Университет не назначен'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.university_inactive'), 'ru-RU', 'Университет неактивен'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.network_error'), 'ru-RU', 'Ошибка сети'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.required_field'), 'ru-RU', 'Поле {field} обязательно');

-- Common (Russian)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'common.loading'), 'ru-RU', 'Загрузка...'),
((SELECT id FROM h_system_message WHERE message_key = 'common.error'), 'ru-RU', 'Ошибка'),
((SELECT id FROM h_system_message WHERE message_key = 'common.success'), 'ru-RU', 'Успешно'),
((SELECT id FROM h_system_message WHERE message_key = 'common.retry'), 'ru-RU', 'Повторить');

-- =====================================================
-- STEP 4: INSERT TRANSLATIONS (en-US - English)
-- =====================================================

-- Login Page Main Labels (English)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.title'), 'en-US', 'HEMIS Admin Panel'),
((SELECT id FROM h_system_message WHERE message_key = 'login.subtitle'), 'en-US', 'Higher Education Management Information System'),
((SELECT id FROM h_system_message WHERE message_key = 'login.powered_by'), 'en-US', 'Powered by AI & Analytics'),
((SELECT id FROM h_system_message WHERE message_key = 'login.username'), 'en-US', 'Username'),
((SELECT id FROM h_system_message WHERE message_key = 'login.username_placeholder'), 'en-US', 'Enter your username'),
((SELECT id FROM h_system_message WHERE message_key = 'login.password'), 'en-US', 'Password'),
((SELECT id FROM h_system_message WHERE message_key = 'login.password_placeholder'), 'en-US', 'Enter your password'),
((SELECT id FROM h_system_message WHERE message_key = 'login.language'), 'en-US', 'Language'),
((SELECT id FROM h_system_message WHERE message_key = 'login.login_button'), 'en-US', 'Sign In'),
((SELECT id FROM h_system_message WHERE message_key = 'login.logging_in'), 'en-US', 'Authenticating...'),
((SELECT id FROM h_system_message WHERE message_key = 'login.welcome_back'), 'en-US', 'Welcome back!');

-- Statistics (English)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.universities'), 'en-US', 'Universities'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.students'), 'en-US', 'Students'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.analytics'), 'en-US', 'AI Analytics'),
((SELECT id FROM h_system_message WHERE message_key = 'login.stats.reports'), 'en-US', 'Reports');

-- Features (English)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.features.realtime'), 'en-US', 'Real-time Analytics'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.ai'), 'en-US', 'AI Predictions'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.digital'), 'en-US', 'Digitalization'),
((SELECT id FROM h_system_message WHERE message_key = 'login.features.secure'), 'en-US', 'Secure System');

-- Errors (English)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.required'), 'en-US', 'This field is required'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.invalid_credentials'), 'en-US', 'Invalid username or password'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.min_length'), 'en-US', 'Must be at least {count} characters'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.user_disabled'), 'en-US', 'User account is disabled'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.no_university'), 'en-US', 'No university assigned'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.university_inactive'), 'en-US', 'University is inactive'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.network_error'), 'en-US', 'Network connection error'),
((SELECT id FROM h_system_message WHERE message_key = 'login.errors.required_field'), 'en-US', '{field} is required');

-- Common (English)
INSERT INTO h_system_message_translation (message_id, language, translation) VALUES
((SELECT id FROM h_system_message WHERE message_key = 'common.loading'), 'en-US', 'Loading...'),
((SELECT id FROM h_system_message WHERE message_key = 'common.error'), 'en-US', 'Error'),
((SELECT id FROM h_system_message WHERE message_key = 'common.success'), 'en-US', 'Success'),
((SELECT id FROM h_system_message WHERE message_key = 'common.retry'), 'en-US', 'Retry');

-- =====================================================
-- VERIFICATION
-- =====================================================

-- Check inserted counts
DO $$
DECLARE
    msg_count INTEGER;
    trans_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO msg_count FROM h_system_message WHERE category IN ('login', 'common');
    SELECT COUNT(*) INTO trans_count FROM h_system_message_translation
    WHERE message_id IN (SELECT id FROM h_system_message WHERE category IN ('login', 'common'));

    RAISE NOTICE '✅ Migration V6 completed successfully:';
    RAISE NOTICE '   - System Messages: % keys', msg_count;
    RAISE NOTICE '   - Translations: % entries', trans_count;
    RAISE NOTICE '   - Expected: 39 keys x 3 languages = 117 translations';

    IF trans_count < 117 THEN
        RAISE WARNING '⚠️ Expected 117 translations but found %', trans_count;
    END IF;
END $$;
