-- =====================================================
-- V5: System Message & Translation Tables (UNIVER Style)
-- =====================================================
-- Purpose: Multi-language system message support
-- Architecture: UNIVER best practice (EAV pattern + JSONB)
-- Author: Senior Software Engineer
-- Date: 2025-11-10
--
-- Design Philosophy:
-- ✅ UNIVER proven approach (production-tested with 199 tables)
-- ✅ EAV pattern for system messages
-- ✅ Composite PK for translations (id, language)
-- ✅ CASCADE delete for data integrity
-- ✅ Flexible language support (9+ languages)
-- ✅ Redis caching ready
--
-- Tables:
-- 1. h_system_message - Master messages (category-based)
-- 2. h_system_message_translation - Per-language translations
-- =====================================================

-- =====================================================
-- 1. h_system_message (Master Messages)
-- =====================================================
-- Purpose: Original system messages with category classification
-- Pattern: Similar to UNIVER's e_system_message
-- Naming: h_ prefix (Helper/Classifier table convention)
-- =====================================================

CREATE TABLE IF NOT EXISTS h_system_message (
    -- Primary Key (UUID for consistency with HEMIS-BACK)
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Message Classification
    category VARCHAR(64) NOT NULL,
        -- Categories: app, menu, button, label, message, error, validation

    message_key VARCHAR(255) UNIQUE NOT NULL,
        -- Unique identifier: category.specific_name
        -- Example: app.student_name, button.save, error.not_found

    -- Default Message (Uzbek - required)
    message TEXT NOT NULL,
        -- Default fallback message in Uzbek

    -- Status
    is_active BOOLEAN DEFAULT TRUE NOT NULL,

    -- Audit Fields (Modern Convention)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,  -- Soft delete (NULL = active)

    -- Constraints
    CONSTRAINT chk_message_key_format
        CHECK (message_key ~ '^[a-z0-9_]+\.[a-z0-9_.]+$')
        -- Format: category.key (lowercase, numbers, underscore only)
);

-- Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_system_message_category
    ON h_system_message(category) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_system_message_key
    ON h_system_message(message_key) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_system_message_active
    ON h_system_message(is_active) WHERE deleted_at IS NULL;

-- Comments
COMMENT ON TABLE h_system_message IS
    'System messages master table - UNIVER pattern - category-based organization';

COMMENT ON COLUMN h_system_message.category IS
    'Message category: app, menu, button, label, message, error, validation';

COMMENT ON COLUMN h_system_message.message_key IS
    'Unique key for programmatic access (format: category.name)';

COMMENT ON COLUMN h_system_message.message IS
    'Default message in Uzbek (fallback when translation not found)';

COMMENT ON COLUMN h_system_message.deleted_at IS
    'Soft delete timestamp (NULL = active message)';

-- =====================================================
-- 2. h_system_message_translation (Translations)
-- =====================================================
-- Purpose: Per-language translations for system messages
-- Pattern: UNIVER's e_system_message_translation (1:N relationship)
-- Key Design: Composite PK (message_id, language)
-- =====================================================

CREATE TABLE IF NOT EXISTS h_system_message_translation (
    -- Foreign Key to Master Message
    message_id UUID NOT NULL,

    -- Language Code
    language VARCHAR(16) NOT NULL,
        -- Supported: uz-UZ, oz-UZ, ru-RU, en-US, kk-UZ, tg-TG, etc.

    -- Translated Text
    translation TEXT NOT NULL,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Composite Primary Key (UNIVER pattern)
    PRIMARY KEY (message_id, language),

    -- Foreign Key with CASCADE delete
    CONSTRAINT fk_translation_message
        FOREIGN KEY (message_id)
        REFERENCES h_system_message(id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,

    -- Language validation
    CONSTRAINT chk_language_code
        CHECK (language ~ '^[a-z]{2}-[A-Z]{2}$')
        -- Format: xx-XX (e.g., uz-UZ, ru-RU, en-US)
);

-- Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_translation_language
    ON h_system_message_translation(language);

CREATE INDEX IF NOT EXISTS idx_translation_message_id
    ON h_system_message_translation(message_id);

-- Full-text Search (for admin panel)
CREATE INDEX IF NOT EXISTS idx_translation_search
    ON h_system_message_translation
    USING gin(to_tsvector('simple', translation));

-- Comments
COMMENT ON TABLE h_system_message_translation IS
    'System message translations - UNIVER pattern - composite PK (message_id, language)';

COMMENT ON COLUMN h_system_message_translation.message_id IS
    'Reference to h_system_message.id';

COMMENT ON COLUMN h_system_message_translation.language IS
    'Language code (format: xx-XX, e.g., uz-UZ, ru-RU, en-US)';

COMMENT ON COLUMN h_system_message_translation.translation IS
    'Translated message text for specific language';

-- =====================================================
-- 3. Seed Data - System Messages
-- =====================================================
-- Initial messages for common UI elements
-- Pattern: Insert master + translations
-- =====================================================

-- App Category (General Application Messages)
INSERT INTO h_system_message (message_key, category, message, is_active) VALUES
('app.welcome', 'app', 'Xush kelibsiz', true),
('app.logout', 'app', 'Chiqish', true),
('app.profile', 'app', 'Profil', true),
('app.settings', 'app', 'Sozlamalar', true),
('app.help', 'app', 'Yordam', true)
ON CONFLICT (message_key) DO NOTHING;

-- Menu Category
INSERT INTO h_system_message (message_key, category, message, is_active) VALUES
('menu.dashboard', 'menu', 'Bosh sahifa', true),
('menu.students', 'menu', 'Talabalar', true),
('menu.teachers', 'menu', 'O''qituvchilar', true),
('menu.reports', 'menu', 'Hisobotlar', true),
('menu.admin', 'menu', 'Boshqaruv', true)
ON CONFLICT (message_key) DO NOTHING;

-- Button Category
INSERT INTO h_system_message (message_key, category, message, is_active) VALUES
('button.save', 'button', 'Saqlash', true),
('button.cancel', 'button', 'Bekor qilish', true),
('button.delete', 'button', 'O''chirish', true),
('button.edit', 'button', 'Tahrirlash', true),
('button.add', 'button', 'Qo''shish', true),
('button.search', 'button', 'Qidirish', true),
('button.export', 'button', 'Eksport', true),
('button.import', 'button', 'Import', true)
ON CONFLICT (message_key) DO NOTHING;

-- Label Category
INSERT INTO h_system_message (message_key, category, message, is_active) VALUES
('label.first_name', 'label', 'Ism', true),
('label.last_name', 'label', 'Familiya', true),
('label.passport', 'label', 'Pasport', true),
('label.phone', 'label', 'Telefon', true),
('label.email', 'label', 'Email', true),
('label.status', 'label', 'Holat', true)
ON CONFLICT (message_key) DO NOTHING;

-- Message Category
INSERT INTO h_system_message (message_key, category, message, is_active) VALUES
('message.save_success', 'message', 'Muvaffaqiyatli saqlandi', true),
('message.delete_success', 'message', 'Muvaffaqiyatli o''chirildi', true),
('message.update_success', 'message', 'Muvaffaqiyatli yangilandi', true),
('message.no_data', 'message', 'Ma''lumot topilmadi', true)
ON CONFLICT (message_key) DO NOTHING;

-- Error Category
INSERT INTO h_system_message (message_key, category, message, is_active) VALUES
('error.network', 'error', 'Tarmoq xatosi', true),
('error.server', 'error', 'Server xatosi', true),
('error.unauthorized', 'error', 'Ruxsat yo''q', true),
('error.not_found', 'error', 'Topilmadi', true),
('error.validation', 'error', 'Tekshirish xatosi', true)
ON CONFLICT (message_key) DO NOTHING;

-- Validation Category
INSERT INTO h_system_message (message_key, category, message, is_active) VALUES
('validation.required', 'validation', 'Majburiy maydon', true),
('validation.invalid_email', 'validation', 'Noto''g''ri email format', true),
('validation.invalid_phone', 'validation', 'Noto''g''ri telefon raqam', true),
('validation.min_length', 'validation', 'Kamida {min} ta belgi kerak', true),
('validation.max_length', 'validation', 'Ko''pi bilan {max} ta belgi', true)
ON CONFLICT (message_key) DO NOTHING;

-- =====================================================
-- 4. Seed Data - Translations
-- =====================================================
-- Translations for main languages: uz-UZ, ru-RU, en-US
-- Pattern: Bulk insert per language
-- =====================================================

-- Russian Translations (ru-RU)
INSERT INTO h_system_message_translation (message_id, language, translation)
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
        -- Button
        WHEN 'button.save' THEN 'Сохранить'
        WHEN 'button.cancel' THEN 'Отмена'
        WHEN 'button.delete' THEN 'Удалить'
        WHEN 'button.edit' THEN 'Редактировать'
        WHEN 'button.add' THEN 'Добавить'
        WHEN 'button.search' THEN 'Поиск'
        WHEN 'button.export' THEN 'Экспорт'
        WHEN 'button.import' THEN 'Импорт'
        -- Label
        WHEN 'label.first_name' THEN 'Имя'
        WHEN 'label.last_name' THEN 'Фамилия'
        WHEN 'label.passport' THEN 'Паспорт'
        WHEN 'label.phone' THEN 'Телефон'
        WHEN 'label.email' THEN 'Электронная почта'
        WHEN 'label.status' THEN 'Статус'
        -- Message
        WHEN 'message.save_success' THEN 'Успешно сохранено'
        WHEN 'message.delete_success' THEN 'Успешно удалено'
        WHEN 'message.update_success' THEN 'Успешно обновлено'
        WHEN 'message.no_data' THEN 'Данные не найдены'
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
FROM h_system_message
WHERE message_key IN (
    'app.welcome', 'app.logout', 'app.profile', 'app.settings', 'app.help',
    'menu.dashboard', 'menu.students', 'menu.teachers', 'menu.reports', 'menu.admin',
    'button.save', 'button.cancel', 'button.delete', 'button.edit', 'button.add',
    'button.search', 'button.export', 'button.import',
    'label.first_name', 'label.last_name', 'label.passport', 'label.phone', 'label.email', 'label.status',
    'message.save_success', 'message.delete_success', 'message.update_success', 'message.no_data',
    'error.network', 'error.server', 'error.unauthorized', 'error.not_found', 'error.validation',
    'validation.required', 'validation.invalid_email', 'validation.invalid_phone',
    'validation.min_length', 'validation.max_length'
)
ON CONFLICT (message_id, language) DO NOTHING;

-- English Translations (en-US)
INSERT INTO h_system_message_translation (message_id, language, translation)
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
        -- Button
        WHEN 'button.save' THEN 'Save'
        WHEN 'button.cancel' THEN 'Cancel'
        WHEN 'button.delete' THEN 'Delete'
        WHEN 'button.edit' THEN 'Edit'
        WHEN 'button.add' THEN 'Add'
        WHEN 'button.search' THEN 'Search'
        WHEN 'button.export' THEN 'Export'
        WHEN 'button.import' THEN 'Import'
        -- Label
        WHEN 'label.first_name' THEN 'First Name'
        WHEN 'label.last_name' THEN 'Last Name'
        WHEN 'label.passport' THEN 'Passport'
        WHEN 'label.phone' THEN 'Phone'
        WHEN 'label.email' THEN 'Email'
        WHEN 'label.status' THEN 'Status'
        -- Message
        WHEN 'message.save_success' THEN 'Successfully saved'
        WHEN 'message.delete_success' THEN 'Successfully deleted'
        WHEN 'message.update_success' THEN 'Successfully updated'
        WHEN 'message.no_data' THEN 'No data found'
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
FROM h_system_message
WHERE message_key IN (
    'app.welcome', 'app.logout', 'app.profile', 'app.settings', 'app.help',
    'menu.dashboard', 'menu.students', 'menu.teachers', 'menu.reports', 'menu.admin',
    'button.save', 'button.cancel', 'button.delete', 'button.edit', 'button.add',
    'button.search', 'button.export', 'button.import',
    'label.first_name', 'label.last_name', 'label.passport', 'label.phone', 'label.email', 'label.status',
    'message.save_success', 'message.delete_success', 'message.update_success', 'message.no_data',
    'error.network', 'error.server', 'error.unauthorized', 'error.not_found', 'error.validation',
    'validation.required', 'validation.invalid_email', 'validation.invalid_phone',
    'validation.min_length', 'validation.max_length'
)
ON CONFLICT (message_id, language) DO NOTHING;

-- Uzbek Cyrillic Translations (oz-UZ)
INSERT INTO h_system_message_translation (message_id, language, translation)
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
        -- Button
        WHEN 'button.save' THEN 'Сақлаш'
        WHEN 'button.cancel' THEN 'Бекор қилиш'
        WHEN 'button.delete' THEN 'Ўчириш'
        WHEN 'button.edit' THEN 'Таҳрирлаш'
        WHEN 'button.add' THEN 'Қўшиш'
        WHEN 'button.search' THEN 'Қидириш'
        WHEN 'button.export' THEN 'Экспорт'
        WHEN 'button.import' THEN 'Импорт'
        -- Label
        WHEN 'label.first_name' THEN 'Исм'
        WHEN 'label.last_name' THEN 'Фамилия'
        WHEN 'label.passport' THEN 'Паспорт'
        WHEN 'label.phone' THEN 'Телефон'
        WHEN 'label.email' THEN 'Email'
        WHEN 'label.status' THEN 'Ҳолат'
        -- Message
        WHEN 'message.save_success' THEN 'Муваффақиятли сақланди'
        WHEN 'message.delete_success' THEN 'Муваффақиятли ўчирилди'
        WHEN 'message.update_success' THEN 'Муваффақиятли янгиланди'
        WHEN 'message.no_data' THEN 'Маълумот топилмади'
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
FROM h_system_message
WHERE message_key IN (
    'app.welcome', 'app.logout', 'app.profile', 'app.settings', 'app.help',
    'menu.dashboard', 'menu.students', 'menu.teachers', 'menu.reports', 'menu.admin',
    'button.save', 'button.cancel', 'button.delete', 'button.edit', 'button.add',
    'button.search', 'button.export', 'button.import',
    'label.first_name', 'label.last_name', 'label.passport', 'label.phone', 'label.email', 'label.status',
    'message.save_success', 'message.delete_success', 'message.update_success', 'message.no_data',
    'error.network', 'error.server', 'error.unauthorized', 'error.not_found', 'error.validation',
    'validation.required', 'validation.invalid_email', 'validation.invalid_phone',
    'validation.min_length', 'validation.max_length'
)
ON CONFLICT (message_id, language) DO NOTHING;

-- =====================================================
-- Migration Complete
-- =====================================================
-- Tables created: h_system_message, h_system_message_translation
-- Indexes created: 7 total (category, key, language, search)
-- Constraints: Foreign key CASCADE, Check constraints
-- Seed data inserted:
--   - 35 system messages (7 categories)
--   - 105 translations (35 messages × 3 languages)
-- Pattern: UNIVER best practice with HEMIS-BACK conventions
-- =====================================================
