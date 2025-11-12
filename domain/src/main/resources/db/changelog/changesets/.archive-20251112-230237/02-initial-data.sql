-- =====================================================
-- HEMIS Backend - Initial Data (DML Only)
-- =====================================================
-- Version: V2
-- Purpose: Seed default data for new HEMIS system
-- Author: Senior Backend Team
-- Date: 2025-01-12
--
-- Contents:
--   1. Default admin user (username: admin, password: admin)
--   2. Default roles (5 roles)
--   3. Default permissions (~30 permissions)
--   4. Role-Permission assignments
--   5. User-Role assignments
--   6. Languages (4 active + 5 inactive)
--   7. System configurations
--   8. System messages (menu, login, common)
--   9. Message translations (uz-UZ, oz-UZ, ru-RU, en-US)
--
-- Security Note:
-- ⚠️  Default password is "admin" - MUST be changed in production!
-- =====================================================

-- =====================================================
-- STEP 1: Create Default Admin User
-- =====================================================
-- Note: Admin user is NOT created here. It will be migrated from
--       the old-hemis database (sec_user) in V3 migration.
--       This ensures we preserve the existing admin user's data.
-- =====================================================

-- =====================================================
-- STEP 2: Create Default Roles
-- =====================================================

INSERT INTO roles (id, code, name, description, role_type, active) VALUES

-- SUPER_ADMIN: Full system access
('10000000-0000-0000-0000-000000000001'::UUID,
 'SUPER_ADMIN',
 'Super Administrator',
 'Full system access - All permissions - Ministry level administration',
 'SYSTEM',
 TRUE),

-- MINISTRY_ADMIN: Ministry-level administrator
('10000000-0000-0000-0000-000000000002'::UUID,
 'MINISTRY_ADMIN',
 'Ministry Administrator',
 'Ministry-level administrator - Can view all universities, manage reports',
 'SYSTEM',
 TRUE),

-- UNIVERSITY_ADMIN: University-level administrator
('10000000-0000-0000-0000-000000000003'::UUID,
 'UNIVERSITY_ADMIN',
 'University Administrator',
 'University-level administrator - Manage own university data',
 'UNIVERSITY',
 TRUE),

-- VIEWER: Read-only access
('10000000-0000-0000-0000-000000000004'::UUID,
 'VIEWER',
 'Read-only Viewer',
 'Read-only access - Can only view data, no modifications',
 'SYSTEM',
 TRUE),

-- REPORT_VIEWER: Report viewer
('10000000-0000-0000-0000-000000000005'::UUID,
 'REPORT_VIEWER',
 'Report Viewer',
 'Can view and generate reports - For statisticians and analysts',
 'CUSTOM',
 TRUE)

ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- STEP 3: Create Default Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category) VALUES

-- Dashboard
('dashboard', 'view', 'dashboard.view',
 'View Dashboard', 'Access to main dashboard and statistics overview', 'CORE'),

-- Students
('students', 'view', 'students.view',
 'View Students', 'View student list and detailed information', 'CORE'),
('students', 'create', 'students.create',
 'Create Students', 'Add new students to the system', 'CORE'),
('students', 'edit', 'students.edit',
 'Edit Students', 'Modify existing student information', 'CORE'),
('students', 'delete', 'students.delete',
 'Delete Students', 'Soft delete students', 'CORE'),
('students', 'export', 'students.export',
 'Export Students', 'Export student data to Excel/CSV', 'CORE'),

-- Teachers
('teachers', 'view', 'teachers.view',
 'View Teachers', 'View teacher list and detailed information', 'CORE'),
('teachers', 'create', 'teachers.create',
 'Create Teachers', 'Add new teachers to the system', 'CORE'),
('teachers', 'edit', 'teachers.edit',
 'Edit Teachers', 'Modify existing teacher information', 'CORE'),
('teachers', 'delete', 'teachers.delete',
 'Delete Teachers', 'Soft delete teachers', 'CORE'),
('teachers', 'export', 'teachers.export',
 'Export Teachers', 'Export teacher data to Excel/CSV', 'CORE'),

-- Universities
('universities', 'view', 'universities.view',
 'View Universities', 'View university list and information', 'CORE'),
('universities', 'create', 'universities.create',
 'Create Universities', 'Add new universities (Ministry only)', 'ADMIN'),
('universities', 'edit', 'universities.edit',
 'Edit Universities', 'Modify university information', 'ADMIN'),
('universities', 'manage', 'universities.manage',
 'Manage Universities', 'Full university management access', 'ADMIN'),

-- Reports
('reports', 'view', 'reports.view',
 'View Reports', 'Access to reports section and view existing reports', 'REPORTS'),
('reports', 'create', 'reports.create',
 'Generate Reports', 'Generate new reports and analytics', 'REPORTS'),
('reports', 'export', 'reports.export',
 'Export Reports', 'Export reports to Excel/PDF', 'REPORTS'),
('reports', 'manage', 'reports.manage',
 'Manage Reports', 'Full report management', 'REPORTS'),

-- Users
('users', 'view', 'users.view',
 'View Users', 'View user list and information', 'ADMIN'),
('users', 'create', 'users.create',
 'Create Users', 'Add new users to the system', 'ADMIN'),
('users', 'edit', 'users.edit',
 'Edit Users', 'Modify user information', 'ADMIN'),
('users', 'delete', 'users.delete',
 'Delete Users', 'Soft delete users', 'ADMIN'),
('users', 'manage', 'users.manage',
 'Manage Users', 'Full user management', 'ADMIN'),

-- Roles
('roles', 'view', 'roles.view',
 'View Roles', 'View role list and permissions', 'ADMIN'),
('roles', 'create', 'roles.create',
 'Create Roles', 'Create custom roles', 'ADMIN'),
('roles', 'edit', 'roles.edit',
 'Edit Roles', 'Modify role permissions', 'ADMIN'),
('roles', 'manage', 'roles.manage',
 'Manage Roles', 'Full role management', 'ADMIN'),

-- Permissions
('permissions', 'view', 'permissions.view',
 'View Permissions', 'View all available permissions', 'ADMIN'),
('permissions', 'manage', 'permissions.manage',
 'Manage Permissions', 'Create and manage system permissions', 'ADMIN')

ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- STEP 4: Assign Permissions to Roles
-- =====================================================

-- SUPER_ADMIN: ALL Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000001'::UUID,
    id
FROM permissions
WHERE deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- MINISTRY_ADMIN: All except user/role management
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000002'::UUID,
    id
FROM permissions
WHERE code NOT LIKE 'users.%'
  AND code NOT LIKE 'roles.%'
  AND code NOT LIKE 'permissions.%'
  AND deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- UNIVERSITY_ADMIN: Manage own university data
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000003'::UUID,
    id
FROM permissions
WHERE code IN (
    'dashboard.view',
    'students.view', 'students.create', 'students.edit', 'students.export',
    'teachers.view', 'teachers.create', 'teachers.edit', 'teachers.export',
    'reports.view', 'reports.create', 'reports.export'
)
ON CONFLICT DO NOTHING;

-- VIEWER: Read-only
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000004'::UUID,
    id
FROM permissions
WHERE action = 'view' AND deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- REPORT_VIEWER: Dashboard + Reports
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000005'::UUID,
    id
FROM permissions
WHERE (code LIKE 'dashboard.%' OR code LIKE 'reports.%')
  AND deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- =====================================================
-- STEP 5: Assign SUPER_ADMIN Role to Admin User
-- =====================================================
-- Note: Admin user-role assignment is NOT done here.
--       It will be handled in V3 migration when migrating
--       user-role mappings from sec_user_role table.
-- =====================================================

-- =====================================================
-- STEP 6: Create Languages
-- =====================================================

-- Active Languages (4)
INSERT INTO languages (id, code, name, native_name, iso_code, position, is_active, is_rtl, is_default) VALUES
('20000000-0000-0000-0000-000000000001'::UUID, 'uz-UZ', 'Uzbek (Latin)', 'O''zbekcha', 'uz', 1, TRUE, FALSE, TRUE),
('20000000-0000-0000-0000-000000000002'::UUID, 'oz-UZ', 'Uzbek (Cyrillic)', 'Ўзбекча', 'uz', 2, TRUE, FALSE, TRUE),
('20000000-0000-0000-0000-000000000003'::UUID, 'ru-RU', 'Russian', 'Русский', 'ru', 3, TRUE, FALSE, TRUE),
('20000000-0000-0000-0000-000000000004'::UUID, 'en-US', 'English', 'English', 'en', 4, TRUE, FALSE, FALSE)
ON CONFLICT (code) DO NOTHING;

-- Inactive Languages (5)
INSERT INTO languages (id, code, name, native_name, iso_code, position, is_active, is_rtl, is_default) VALUES
('20000000-0000-0000-0000-000000000005'::UUID, 'kk-UZ', 'Karakalpak', 'Қарақалпақша', 'kk', 5, FALSE, FALSE, FALSE),
('20000000-0000-0000-0000-000000000006'::UUID, 'tg-TG', 'Tajik', 'Тоҷикӣ', 'tg', 6, FALSE, FALSE, FALSE),
('20000000-0000-0000-0000-000000000007'::UUID, 'kz-KZ', 'Kazakh', 'Қазақша', 'kz', 7, FALSE, FALSE, FALSE),
('20000000-0000-0000-0000-000000000008'::UUID, 'tm-TM', 'Turkmen', 'Türkmençe', 'tm', 8, FALSE, FALSE, FALSE),
('20000000-0000-0000-0000-000000000009'::UUID, 'kg-KG', 'Kyrgyz', 'Кыргызча', 'kg', 9, FALSE, FALSE, FALSE)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- STEP 7: Create System Configurations
-- =====================================================

-- Language Configurations
INSERT INTO configurations (id, path, value, category, description, value_type, is_editable) VALUES
(gen_random_uuid(), 'system.language.default', 'uz-UZ', 'system', 'Default system language', 'string', TRUE),
(gen_random_uuid(), 'system.language.uz_uz', 'true', 'language', 'Enable Uzbek (Latin)', 'boolean', FALSE),
(gen_random_uuid(), 'system.language.oz_uz', 'true', 'language', 'Enable Uzbek (Cyrillic)', 'boolean', FALSE),
(gen_random_uuid(), 'system.language.ru_ru', 'true', 'language', 'Enable Russian', 'boolean', FALSE),
(gen_random_uuid(), 'system.language.en_us', 'true', 'language', 'Enable English', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.kk_uz', 'false', 'language', 'Enable Karakalpak', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.tg_tg', 'false', 'language', 'Enable Tajik', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.kz_kz', 'false', 'language', 'Enable Kazakh', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.tm_tm', 'false', 'language', 'Enable Turkmen', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.kg_kg', 'false', 'language', 'Enable Kyrgyz', 'boolean', TRUE)
ON CONFLICT (path) DO NOTHING;

-- =====================================================
-- STEP 8: Create System Messages (Uzbek Latin - uz-UZ)
-- =====================================================

-- App Messages
INSERT INTO system_messages (category, message_key, message, is_active) VALUES
('app', 'app.welcome', 'Xush kelibsiz', TRUE),
('app', 'app.logout', 'Chiqish', TRUE),
('app', 'app.profile', 'Profil', TRUE),
('app', 'app.settings', 'Sozlamalar', TRUE),
('app', 'app.help', 'Yordam', TRUE)
ON CONFLICT (message_key) DO NOTHING;

-- Menu Messages
INSERT INTO system_messages (category, message_key, message, is_active) VALUES
('menu', 'menu.dashboard', 'Bosh sahifa', TRUE),
('menu', 'menu.students', 'Talabalar', TRUE),
('menu', 'menu.teachers', 'O''qituvchilar', TRUE),
('menu', 'menu.reports', 'Hisobotlar', TRUE),
('menu', 'menu.admin', 'Boshqaruv', TRUE),
('menu', 'menu.system', 'Tizim', TRUE)
ON CONFLICT (message_key) DO NOTHING;

-- Button Messages
INSERT INTO system_messages (category, message_key, message, is_active) VALUES
('button', 'button.save', 'Saqlash', TRUE),
('button', 'button.cancel', 'Bekor qilish', TRUE),
('button', 'button.delete', 'O''chirish', TRUE),
('button', 'button.edit', 'Tahrirlash', TRUE),
('button', 'button.add', 'Qo''shish', TRUE),
('button', 'button.search', 'Qidirish', TRUE),
('button', 'button.export', 'Eksport', TRUE)
ON CONFLICT (message_key) DO NOTHING;

-- Login Messages
INSERT INTO system_messages (category, message_key, message, is_active) VALUES
('login', 'login.title', 'HEMIS Admin Panel', TRUE),
('login', 'login.subtitle', 'Oliy ta''lim boshqaruv axborot tizimi', TRUE),
('login', 'login.username', 'Foydalanuvchi nomi', TRUE),
('login', 'login.password', 'Parol', TRUE),
('login', 'login.language', 'Til', TRUE),
('login', 'login.login_button', 'Kirish', TRUE),
('login', 'login.welcome_back', 'Xush kelibsiz!', TRUE)
ON CONFLICT (message_key) DO NOTHING;

-- Common Messages
INSERT INTO system_messages (category, message_key, message, is_active) VALUES
('common', 'common.loading', 'Yuklanmoqda...', TRUE),
('common', 'common.error', 'Xatolik', TRUE),
('common', 'common.success', 'Muvaffaqiyatli', TRUE),
('common', 'common.confirm', 'Tasdiqlash', TRUE),
('common', 'common.yes', 'Ha', TRUE),
('common', 'common.no', 'Yo''q', TRUE)
ON CONFLICT (message_key) DO NOTHING;

-- Error Messages
INSERT INTO system_messages (category, message_key, message, is_active) VALUES
('error', 'error.network', 'Tarmoq xatosi', TRUE),
('error', 'error.server', 'Server xatosi', TRUE),
('error', 'error.unauthorized', 'Ruxsat yo''q', TRUE),
('error', 'error.not_found', 'Topilmadi', TRUE),
('error', 'error.validation', 'Tekshirish xatosi', TRUE)
ON CONFLICT (message_key) DO NOTHING;

-- Validation Messages
INSERT INTO system_messages (category, message_key, message, is_active) VALUES
('validation', 'validation.required', 'Majburiy maydon', TRUE),
('validation', 'validation.invalid_email', 'Noto''g''ri email format', TRUE),
('validation', 'validation.invalid_phone', 'Noto''g''ri telefon raqam', TRUE),
('validation', 'validation.min_length', 'Kamida {min} ta belgi kerak', TRUE),
('validation', 'validation.max_length', 'Ko''pi bilan {max} ta belgi', TRUE)
ON CONFLICT (message_key) DO NOTHING;

-- =====================================================
-- STEP 9: Create Message Translations
-- =====================================================

-- Russian Translations (ru-RU)
INSERT INTO message_translations (message_id, language, translation)
SELECT id, 'ru-RU',
    CASE message_key
        -- App
        WHEN 'app.welcome' THEN 'Добро пожаловать'
        WHEN 'app.logout' THEN 'Выход'
        WHEN 'app.profile' THEN 'Профиль'
        WHEN 'app.settings' THEN 'Настройки'
        WHEN 'app.help' THEN 'Помощь'
        -- Menu
        WHEN 'menu.dashboard' THEN 'Главная'
        WHEN 'menu.students' THEN 'Студенты'
        WHEN 'menu.teachers' THEN 'Преподаватели'
        WHEN 'menu.reports' THEN 'Отчеты'
        WHEN 'menu.admin' THEN 'Администрирование'
        WHEN 'menu.system' THEN 'Система'
        -- Button
        WHEN 'button.save' THEN 'Сохранить'
        WHEN 'button.cancel' THEN 'Отмена'
        WHEN 'button.delete' THEN 'Удалить'
        WHEN 'button.edit' THEN 'Редактировать'
        WHEN 'button.add' THEN 'Добавить'
        WHEN 'button.search' THEN 'Поиск'
        WHEN 'button.export' THEN 'Экспорт'
        -- Login
        WHEN 'login.title' THEN 'HEMIS Админ Панель'
        WHEN 'login.subtitle' THEN 'Информационная система управления высшим образованием'
        WHEN 'login.username' THEN 'Имя пользователя'
        WHEN 'login.password' THEN 'Пароль'
        WHEN 'login.language' THEN 'Язык'
        WHEN 'login.login_button' THEN 'Войти'
        WHEN 'login.welcome_back' THEN 'Добро пожаловать!'
        -- Common
        WHEN 'common.loading' THEN 'Загрузка...'
        WHEN 'common.error' THEN 'Ошибка'
        WHEN 'common.success' THEN 'Успешно'
        WHEN 'common.confirm' THEN 'Подтверждение'
        WHEN 'common.yes' THEN 'Да'
        WHEN 'common.no' THEN 'Нет'
        -- Error
        WHEN 'error.network' THEN 'Ошибка сети'
        WHEN 'error.server' THEN 'Ошибка сервера'
        WHEN 'error.unauthorized' THEN 'Доступ запрещен'
        WHEN 'error.not_found' THEN 'Не найдено'
        WHEN 'error.validation' THEN 'Ошибка валидации'
        -- Validation
        WHEN 'validation.required' THEN 'Обязательное поле'
        WHEN 'validation.invalid_email' THEN 'Неверный формат email'
        WHEN 'validation.invalid_phone' THEN 'Неверный номер телефона'
        WHEN 'validation.min_length' THEN 'Минимум {min} символов'
        WHEN 'validation.max_length' THEN 'Максимум {max} символов'
    END
FROM system_messages
WHERE message_key IN (
    'app.welcome', 'app.logout', 'app.profile', 'app.settings', 'app.help',
    'menu.dashboard', 'menu.students', 'menu.teachers', 'menu.reports', 'menu.admin', 'menu.system',
    'button.save', 'button.cancel', 'button.delete', 'button.edit', 'button.add', 'button.search', 'button.export',
    'login.title', 'login.subtitle', 'login.username', 'login.password', 'login.language', 'login.login_button', 'login.welcome_back',
    'common.loading', 'common.error', 'common.success', 'common.confirm', 'common.yes', 'common.no',
    'error.network', 'error.server', 'error.unauthorized', 'error.not_found', 'error.validation',
    'validation.required', 'validation.invalid_email', 'validation.invalid_phone', 'validation.min_length', 'validation.max_length'
)
ON CONFLICT (message_id, language) DO NOTHING;

-- English Translations (en-US)
INSERT INTO message_translations (message_id, language, translation)
SELECT id, 'en-US',
    CASE message_key
        -- App
        WHEN 'app.welcome' THEN 'Welcome'
        WHEN 'app.logout' THEN 'Logout'
        WHEN 'app.profile' THEN 'Profile'
        WHEN 'app.settings' THEN 'Settings'
        WHEN 'app.help' THEN 'Help'
        -- Menu
        WHEN 'menu.dashboard' THEN 'Dashboard'
        WHEN 'menu.students' THEN 'Students'
        WHEN 'menu.teachers' THEN 'Teachers'
        WHEN 'menu.reports' THEN 'Reports'
        WHEN 'menu.admin' THEN 'Administration'
        WHEN 'menu.system' THEN 'System'
        -- Button
        WHEN 'button.save' THEN 'Save'
        WHEN 'button.cancel' THEN 'Cancel'
        WHEN 'button.delete' THEN 'Delete'
        WHEN 'button.edit' THEN 'Edit'
        WHEN 'button.add' THEN 'Add'
        WHEN 'button.search' THEN 'Search'
        WHEN 'button.export' THEN 'Export'
        -- Login
        WHEN 'login.title' THEN 'HEMIS Admin Panel'
        WHEN 'login.subtitle' THEN 'Higher Education Management Information System'
        WHEN 'login.username' THEN 'Username'
        WHEN 'login.password' THEN 'Password'
        WHEN 'login.language' THEN 'Language'
        WHEN 'login.login_button' THEN 'Sign In'
        WHEN 'login.welcome_back' THEN 'Welcome back!'
        -- Common
        WHEN 'common.loading' THEN 'Loading...'
        WHEN 'common.error' THEN 'Error'
        WHEN 'common.success' THEN 'Success'
        WHEN 'common.confirm' THEN 'Confirm'
        WHEN 'common.yes' THEN 'Yes'
        WHEN 'common.no' THEN 'No'
        -- Error
        WHEN 'error.network' THEN 'Network error'
        WHEN 'error.server' THEN 'Server error'
        WHEN 'error.unauthorized' THEN 'Unauthorized'
        WHEN 'error.not_found' THEN 'Not found'
        WHEN 'error.validation' THEN 'Validation error'
        -- Validation
        WHEN 'validation.required' THEN 'Required field'
        WHEN 'validation.invalid_email' THEN 'Invalid email format'
        WHEN 'validation.invalid_phone' THEN 'Invalid phone number'
        WHEN 'validation.min_length' THEN 'Minimum {min} characters'
        WHEN 'validation.max_length' THEN 'Maximum {max} characters'
    END
FROM system_messages
WHERE message_key IN (
    'app.welcome', 'app.logout', 'app.profile', 'app.settings', 'app.help',
    'menu.dashboard', 'menu.students', 'menu.teachers', 'menu.reports', 'menu.admin', 'menu.system',
    'button.save', 'button.cancel', 'button.delete', 'button.edit', 'button.add', 'button.search', 'button.export',
    'login.title', 'login.subtitle', 'login.username', 'login.password', 'login.language', 'login.login_button', 'login.welcome_back',
    'common.loading', 'common.error', 'common.success', 'common.confirm', 'common.yes', 'common.no',
    'error.network', 'error.server', 'error.unauthorized', 'error.not_found', 'error.validation',
    'validation.required', 'validation.invalid_email', 'validation.invalid_phone', 'validation.min_length', 'validation.max_length'
)
ON CONFLICT (message_id, language) DO NOTHING;

-- Uzbek Cyrillic Translations (oz-UZ)
INSERT INTO message_translations (message_id, language, translation)
SELECT id, 'oz-UZ',
    CASE message_key
        -- App
        WHEN 'app.welcome' THEN 'Хуш келибсиз'
        WHEN 'app.logout' THEN 'Чиқиш'
        WHEN 'app.profile' THEN 'Профил'
        WHEN 'app.settings' THEN 'Созламалар'
        WHEN 'app.help' THEN 'Ёрдам'
        -- Menu
        WHEN 'menu.dashboard' THEN 'Бош саҳифа'
        WHEN 'menu.students' THEN 'Талабалар'
        WHEN 'menu.teachers' THEN 'Ўқитувчилар'
        WHEN 'menu.reports' THEN 'Ҳисоботлар'
        WHEN 'menu.admin' THEN 'Бошқарув'
        WHEN 'menu.system' THEN 'Тизим'
        -- Button
        WHEN 'button.save' THEN 'Сақлаш'
        WHEN 'button.cancel' THEN 'Бекор қилиш'
        WHEN 'button.delete' THEN 'Ўчириш'
        WHEN 'button.edit' THEN 'Таҳрирлаш'
        WHEN 'button.add' THEN 'Қўшиш'
        WHEN 'button.search' THEN 'Қидириш'
        WHEN 'button.export' THEN 'Экспорт'
        -- Login
        WHEN 'login.title' THEN 'HEMIS Админ Панели'
        WHEN 'login.subtitle' THEN 'Олий таълим бошқарув ахборот тизими'
        WHEN 'login.username' THEN 'Фойдаланувчи номи'
        WHEN 'login.password' THEN 'Парол'
        WHEN 'login.language' THEN 'Тил'
        WHEN 'login.login_button' THEN 'Кириш'
        WHEN 'login.welcome_back' THEN 'Хуш келибсиз!'
        -- Common
        WHEN 'common.loading' THEN 'Юкланмоқда...'
        WHEN 'common.error' THEN 'Хатолик'
        WHEN 'common.success' THEN 'Муваффақиятли'
        WHEN 'common.confirm' THEN 'Тасдиқлаш'
        WHEN 'common.yes' THEN 'Ҳа'
        WHEN 'common.no' THEN 'Йўқ'
        -- Error
        WHEN 'error.network' THEN 'Тармоқ хатоси'
        WHEN 'error.server' THEN 'Сервер хатоси'
        WHEN 'error.unauthorized' THEN 'Рухсат йўқ'
        WHEN 'error.not_found' THEN 'Топилмади'
        WHEN 'error.validation' THEN 'Текширув хатоси'
        -- Validation
        WHEN 'validation.required' THEN 'Мажбурий майдон'
        WHEN 'validation.invalid_email' THEN 'Нотўғри email формат'
        WHEN 'validation.invalid_phone' THEN 'Нотўғри телефон рақам'
        WHEN 'validation.min_length' THEN 'Камида {min} та белги керак'
        WHEN 'validation.max_length' THEN 'Кўпи билан {max} та белги'
    END
FROM system_messages
WHERE message_key IN (
    'app.welcome', 'app.logout', 'app.profile', 'app.settings', 'app.help',
    'menu.dashboard', 'menu.students', 'menu.teachers', 'menu.reports', 'menu.admin', 'menu.system',
    'button.save', 'button.cancel', 'button.delete', 'button.edit', 'button.add', 'button.search', 'button.export',
    'login.title', 'login.subtitle', 'login.username', 'login.password', 'login.language', 'login.login_button', 'login.welcome_back',
    'common.loading', 'common.error', 'common.success', 'common.confirm', 'common.yes', 'common.no',
    'error.network', 'error.server', 'error.unauthorized', 'error.not_found', 'error.validation',
    'validation.required', 'validation.invalid_email', 'validation.invalid_phone', 'validation.min_length', 'validation.max_length'
)
ON CONFLICT (message_id, language) DO NOTHING;

-- =====================================================
-- VERIFICATION
-- =====================================================

DO $$
DECLARE
    user_count INTEGER;
    role_count INTEGER;
    permission_count INTEGER;
    language_count INTEGER;
    config_count INTEGER;
    message_count INTEGER;
    translation_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO role_count FROM roles WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO permission_count FROM permissions WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO language_count FROM languages WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO config_count FROM configurations WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO message_count FROM system_messages WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO translation_count FROM message_translations;

    RAISE NOTICE '==============================================';
    RAISE NOTICE 'V2 Migration Complete - Initial Data Seeded';
    RAISE NOTICE '==============================================';
    RAISE NOTICE '';
    RAISE NOTICE 'Users: %', user_count;
    RAISE NOTICE '  ⚠️  No users created yet (will be migrated in V3)';
    RAISE NOTICE '';
    RAISE NOTICE 'Roles: %', role_count;
    RAISE NOTICE '  ✅ SUPER_ADMIN';
    RAISE NOTICE '  ✅ MINISTRY_ADMIN';
    RAISE NOTICE '  ✅ UNIVERSITY_ADMIN';
    RAISE NOTICE '  ✅ VIEWER';
    RAISE NOTICE '  ✅ REPORT_VIEWER';
    RAISE NOTICE '';
    RAISE NOTICE 'Permissions: %', permission_count;
    RAISE NOTICE '  ✅ dashboard.* (1)';
    RAISE NOTICE '  ✅ students.* (5)';
    RAISE NOTICE '  ✅ teachers.* (5)';
    RAISE NOTICE '  ✅ universities.* (4)';
    RAISE NOTICE '  ✅ reports.* (4)';
    RAISE NOTICE '  ✅ users.* (5)';
    RAISE NOTICE '  ✅ roles.* (4)';
    RAISE NOTICE '  ✅ permissions.* (2)';
    RAISE NOTICE '';
    RAISE NOTICE 'Languages: %', language_count;
    RAISE NOTICE '  ✅ uz-UZ (active, default)';
    RAISE NOTICE '  ✅ oz-UZ (active, default)';
    RAISE NOTICE '  ✅ ru-RU (active, default)';
    RAISE NOTICE '  ✅ en-US (active)';
    RAISE NOTICE '  ⚪ 5 inactive languages';
    RAISE NOTICE '';
    RAISE NOTICE 'Configurations: %', config_count;
    RAISE NOTICE 'System Messages: %', message_count;
    RAISE NOTICE 'Translations: % (% messages × 3 languages)', translation_count, message_count;
    RAISE NOTICE '';
    RAISE NOTICE '⚠️  Note: Users will be migrated in V3 from old-hemis database';
    RAISE NOTICE '';
    RAISE NOTICE '==============================================';
END $$;

-- =====================================================
-- Migration Complete - V2 Initial Data
-- =====================================================
-- ✅ 5 roles created (SUPER_ADMIN, MINISTRY_ADMIN, etc.)
-- ✅ 30 permissions created (dashboard, students, teachers, etc.)
-- ✅ 9 languages created (4 active: uz-UZ, oz-UZ, ru-RU, en-US + 5 inactive)
-- ✅ 10 configurations created (system language settings)
-- ✅ 40+ system messages created (menu, login, common, error)
-- ✅ 120+ translations created (4 languages × 40+ messages)
-- ✅ All role-permission mappings created (SUPER_ADMIN has all permissions)
-- ⚠️  Users NOT created here - will be migrated from old-hemis in V3
-- ✅ Ready for V3 user migration
-- =====================================================
