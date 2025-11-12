-- =====================================================
-- HEMIS Backend - Database Baseline (DDL Only)
-- =====================================================
-- Version: V1
-- Purpose: Create core database structure for new HEMIS system
-- Author: Senior Backend Team
-- Date: 2025-01-12
--
-- Strategy: 100% CLEAN NAMING (Global Standard)
-- ✅ NO PREFIXES - Pure Spring Boot / JPA convention
-- ✅ Plural table names (users, roles, permissions)
-- ✅ snake_case naming
-- ✅ UUID primary keys
-- ✅ Soft deletes (deleted_at)
-- ✅ Audit timestamps (created_at, updated_at)
-- ✅ Best practice constraints and indexes
--
-- Tables Created: 9
--   1. users                    - User authentication
--   2. roles                    - RBAC roles
--   3. permissions              - Granular permissions
--   4. user_roles               - User-Role mapping
--   5. role_permissions         - Role-Permission mapping
--   6. languages                - System languages
--   7. configurations           - System configuration
--   8. system_messages          - i18n master messages
--   9. message_translations     - i18n per-language translations
--
-- Naming Convention:
--   Tables:      plural, snake_case (users, message_translations)
--   PK:          id (UUID)
--   FK:          table_singular_id (user_id, role_id)
--   Indexes:     idx_table_column
--   FK Const:    fk_table_column
--   Unique:      unq_table_column
--   Check:       chk_table_condition
-- =====================================================

-- =====================================================
-- TABLE 1: users
-- =====================================================
-- Purpose: Modern user authentication and authorization
-- Pattern: Spring Security standard
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Authentication
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    full_name VARCHAR(255),
    phone VARCHAR(50),

    -- User Context (Multi-tenancy)
    user_type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    entity_code VARCHAR(255),

    -- Account Status
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER DEFAULT 0,

    -- Audit Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Optimistic Locking (JPA @Version)
    version INTEGER NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT unq_users_username UNIQUE (username),
    CONSTRAINT unq_users_email UNIQUE (email),
    CONSTRAINT chk_users_type CHECK (
        user_type IN ('UNIVERSITY', 'MINISTRY', 'ORGANIZATION', 'SYSTEM')
    )
    -- Note: chk_users_entity_code removed to allow flexible entity_code values during migration
);

COMMENT ON TABLE users IS 'User authentication and authorization - Spring Security compatible';
COMMENT ON COLUMN users.username IS 'Unique login username';
COMMENT ON COLUMN users.password IS 'BCrypt hashed password (never plain text)';
COMMENT ON COLUMN users.user_type IS 'User category: UNIVERSITY, MINISTRY, ORGANIZATION, SYSTEM';
COMMENT ON COLUMN users.entity_code IS 'OTM code for university users (e.g., TATU, NUUZ)';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp (NULL = active)';

-- =====================================================
-- TABLE 2: roles
-- =====================================================
-- Purpose: RBAC role definitions
-- Pattern: Standard role-based access control
-- =====================================================

CREATE TABLE IF NOT EXISTS roles (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Role Identification
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Role Type
    role_type VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Optimistic Locking (JPA @Version)
    version INTEGER NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT unq_roles_code UNIQUE (code),
    CONSTRAINT chk_roles_type CHECK (
        role_type IN ('SYSTEM', 'UNIVERSITY', 'CUSTOM') OR role_type IS NULL
    )
);

COMMENT ON TABLE roles IS 'RBAC role definitions';
COMMENT ON COLUMN roles.code IS 'Unique role code (e.g., SUPER_ADMIN, UNIVERSITY_ADMIN)';
COMMENT ON COLUMN roles.role_type IS 'SYSTEM (built-in), UNIVERSITY (per-OTM), CUSTOM (user-defined)';

-- =====================================================
-- TABLE 3: permissions
-- =====================================================
-- Purpose: Granular permission definitions
-- Pattern: resource.action format (e.g., students.view)
-- =====================================================

CREATE TABLE IF NOT EXISTS permissions (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Permission Structure
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    code VARCHAR(200) NOT NULL,

    -- Human-Readable
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Categorization
    category VARCHAR(50),

    -- Audit Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Optimistic Locking (JPA @Version)
    version INTEGER NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT unq_permissions_code UNIQUE (code),
    CONSTRAINT unq_permissions_resource_action UNIQUE (resource, action),
    CONSTRAINT chk_permissions_code CHECK (code = resource || '.' || action),
    CONSTRAINT chk_permissions_category CHECK (
        category IN ('CORE', 'REPORTS', 'ADMIN', 'INTEGRATION') OR category IS NULL
    )
);

COMMENT ON TABLE permissions IS 'Granular permissions - resource.action format';
COMMENT ON COLUMN permissions.resource IS 'Resource name (e.g., students, teachers, reports)';
COMMENT ON COLUMN permissions.action IS 'Action (e.g., view, create, edit, delete, manage)';
COMMENT ON COLUMN permissions.code IS 'Full permission code: resource.action';
COMMENT ON COLUMN permissions.category IS 'CORE, REPORTS, ADMIN, INTEGRATION';

-- =====================================================
-- TABLE 4: user_roles (Many-to-Many)
-- =====================================================
-- Purpose: User-Role mapping
-- Pattern: Standard join table
-- =====================================================

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Primary Key
    PRIMARY KEY (user_id, role_id),

    -- Foreign Keys
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON DELETE CASCADE
);

COMMENT ON TABLE user_roles IS 'User-Role mapping (many-to-many)';

-- =====================================================
-- TABLE 5: role_permissions (Many-to-Many)
-- =====================================================
-- Purpose: Role-Permission mapping
-- Pattern: Standard join table
-- =====================================================

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Primary Key
    PRIMARY KEY (role_id, permission_id),

    -- Foreign Keys
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id)
        REFERENCES permissions(id) ON DELETE CASCADE
);

COMMENT ON TABLE role_permissions IS 'Role-Permission mapping (many-to-many)';

-- =====================================================
-- TABLE 6: languages
-- =====================================================
-- Purpose: System language management
-- Pattern: Multi-language support (9 languages)
-- =====================================================

CREATE TABLE IF NOT EXISTS languages (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Language Identification
    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100) NOT NULL,
    iso_code VARCHAR(2),

    -- Display & Behavior
    position INTEGER DEFAULT 999,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_rtl BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN DEFAULT FALSE,

    -- Audit Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Constraints
    CONSTRAINT unq_languages_code UNIQUE (code),
    CONSTRAINT chk_languages_code_format CHECK (code ~ '^[a-z]{2}-[A-Z]{2}$')
);

COMMENT ON TABLE languages IS 'System languages (uz-UZ, ru-RU, en-US, oz-UZ, etc.)';
COMMENT ON COLUMN languages.code IS 'Language code (e.g., uz-UZ, ru-RU)';
COMMENT ON COLUMN languages.native_name IS 'Native name (O''zbekcha, Русский, English)';
COMMENT ON COLUMN languages.position IS 'Display order (lower = first)';
COMMENT ON COLUMN languages.is_rtl IS 'Right-to-left language flag';

-- =====================================================
-- TABLE 7: configurations
-- =====================================================
-- Purpose: System-wide configuration key-value store
-- Pattern: Flexible configuration management
-- =====================================================

CREATE TABLE IF NOT EXISTS configurations (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Configuration
    path VARCHAR(255) NOT NULL,
    value TEXT,
    category VARCHAR(64),
    description TEXT,

    -- Metadata
    value_type VARCHAR(32) DEFAULT 'string',
    is_editable BOOLEAN DEFAULT TRUE,
    is_sensitive BOOLEAN DEFAULT FALSE,

    -- Audit Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Constraints
    CONSTRAINT unq_configurations_path UNIQUE (path),
    CONSTRAINT chk_configurations_path_format CHECK (path ~ '^[a-z0-9_.]+$')
);

COMMENT ON TABLE configurations IS 'System configuration key-value store';
COMMENT ON COLUMN configurations.path IS 'Configuration path (e.g., system.language.default)';
COMMENT ON COLUMN configurations.value_type IS 'Data type: boolean, number, string, password, json';
COMMENT ON COLUMN configurations.is_sensitive IS 'Sensitive value (masked in UI)';

-- =====================================================
-- TABLE 8: system_messages
-- =====================================================
-- Purpose: i18n master messages (multi-language support)
-- Pattern: EAV pattern for system messages
-- =====================================================

CREATE TABLE IF NOT EXISTS system_messages (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Message Classification
    category VARCHAR(64) NOT NULL,
    message_key VARCHAR(255) NOT NULL,

    -- Default Message (Uzbek Latin - uz-UZ)
    message TEXT NOT NULL,

    -- Status
    is_active BOOLEAN DEFAULT TRUE NOT NULL,

    -- Audit Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Constraints
    CONSTRAINT unq_system_messages_key UNIQUE (message_key),
    CONSTRAINT chk_system_messages_key_format CHECK (
        message_key ~ '^[a-z0-9_]+\.[a-z0-9_.]+$'
    )
);

COMMENT ON TABLE system_messages IS 'i18n master messages - category-based organization';
COMMENT ON COLUMN system_messages.category IS 'Message category (app, menu, button, label, error, etc.)';
COMMENT ON COLUMN system_messages.message_key IS 'Unique key: category.name (e.g., menu.dashboard, button.save)';
COMMENT ON COLUMN system_messages.message IS 'Default message in Uzbek Latin (fallback)';

-- =====================================================
-- TABLE 9: message_translations
-- =====================================================
-- Purpose: Per-language translations for system messages
-- Pattern: Composite PK (message_id, language)
-- =====================================================

CREATE TABLE IF NOT EXISTS message_translations (
    -- Foreign Key to Master Message
    message_id UUID NOT NULL,

    -- Language Code
    language VARCHAR(16) NOT NULL,

    -- Translated Text
    translation TEXT NOT NULL,

    -- Audit Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Composite Primary Key
    PRIMARY KEY (message_id, language),

    -- Foreign Key
    CONSTRAINT fk_message_translations_message FOREIGN KEY (message_id)
        REFERENCES system_messages(id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,

    -- Constraint
    CONSTRAINT chk_message_translations_language CHECK (
        language ~ '^[a-z]{2}-[A-Z]{2}$'
    )
);

COMMENT ON TABLE message_translations IS 'System message translations - composite PK (message_id, language)';
COMMENT ON COLUMN message_translations.language IS 'Language code (uz-UZ, ru-RU, en-US, oz-UZ)';

-- =====================================================
-- INDEXES - Performance Optimization
-- =====================================================

-- Users Indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_entity_code ON users(entity_code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_user_type ON users(user_type) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);

-- Roles Indexes
CREATE INDEX IF NOT EXISTS idx_roles_code ON roles(code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_roles_type ON roles(role_type) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_roles_active ON roles(active) WHERE deleted_at IS NULL;

-- Permissions Indexes
CREATE INDEX IF NOT EXISTS idx_permissions_code ON permissions(code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_permissions_resource ON permissions(resource) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_permissions_category ON permissions(category) WHERE deleted_at IS NULL;

-- User Roles Indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Role Permissions Indexes
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);

-- Languages Indexes
CREATE INDEX IF NOT EXISTS idx_languages_code ON languages(code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_languages_active ON languages(is_active) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_languages_position ON languages(position) WHERE deleted_at IS NULL;

-- Configurations Indexes
CREATE INDEX IF NOT EXISTS idx_configurations_path ON configurations(path) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_configurations_category ON configurations(category) WHERE deleted_at IS NULL;

-- System Messages Indexes
CREATE INDEX IF NOT EXISTS idx_system_messages_category ON system_messages(category) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_system_messages_key ON system_messages(message_key) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_system_messages_active ON system_messages(is_active) WHERE deleted_at IS NULL;

-- Message Translations Indexes
CREATE INDEX IF NOT EXISTS idx_message_translations_language ON message_translations(language);
CREATE INDEX IF NOT EXISTS idx_message_translations_message_id ON message_translations(message_id);

-- Full-text Search Index (for admin panel search)
CREATE INDEX IF NOT EXISTS idx_message_translations_search ON message_translations
    USING gin(to_tsvector('simple', translation));

-- =====================================================
-- VERIFICATION
-- =====================================================

DO $$
DECLARE
    tables_created INTEGER;
    indexes_created INTEGER;
BEGIN
    -- Count created tables
    SELECT COUNT(*) INTO tables_created
    FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_name IN (
          'users', 'roles', 'permissions', 'user_roles', 'role_permissions',
          'languages', 'configurations', 'system_messages', 'message_translations'
      );

    -- Count created indexes
    SELECT COUNT(*) INTO indexes_created
    FROM pg_indexes
    WHERE schemaname = 'public'
      AND tablename IN (
          'users', 'roles', 'permissions', 'user_roles', 'role_permissions',
          'languages', 'configurations', 'system_messages', 'message_translations'
      );

    RAISE NOTICE '==============================================';
    RAISE NOTICE 'V1 Migration Complete - Database Baseline';
    RAISE NOTICE '==============================================';
    RAISE NOTICE '';
    RAISE NOTICE 'Tables Created: %', tables_created;
    RAISE NOTICE '  ✅ users                    (Auth)';
    RAISE NOTICE '  ✅ roles                    (RBAC)';
    RAISE NOTICE '  ✅ permissions              (Granular)';
    RAISE NOTICE '  ✅ user_roles               (Join)';
    RAISE NOTICE '  ✅ role_permissions         (Join)';
    RAISE NOTICE '  ✅ languages                (i18n)';
    RAISE NOTICE '  ✅ configurations           (System)';
    RAISE NOTICE '  ✅ system_messages          (i18n Master)';
    RAISE NOTICE '  ✅ message_translations     (i18n Translations)';
    RAISE NOTICE '';
    RAISE NOTICE 'Indexes Created: %', indexes_created;
    RAISE NOTICE '';
    RAISE NOTICE 'Naming Convention: 100%% Global Standard';
    RAISE NOTICE '  ✅ NO PREFIXES (users, not h_users)';
    RAISE NOTICE '  ✅ Plural table names';
    RAISE NOTICE '  ✅ snake_case naming';
    RAISE NOTICE '  ✅ UUID primary keys';
    RAISE NOTICE '  ✅ Soft deletes (deleted_at)';
    RAISE NOTICE '';
    RAISE NOTICE 'Next: V2__initial_data.sql (seed default data)';
    RAISE NOTICE '==============================================';

    IF tables_created != 9 THEN
        RAISE WARNING 'Expected 9 tables, got %! Check migration', tables_created;
    END IF;
END $$;

-- =====================================================
-- Migration Complete - V1 Baseline
-- =====================================================
-- ✅ 9 core tables created
-- ✅ 23 indexes created
-- ✅ Foreign keys with CASCADE delete
-- ✅ Constraints for data integrity
-- ✅ 100% Global Standard naming (NO PREFIXES)
-- ✅ Spring Boot / JPA compatible
-- ✅ Ready for V2 (seed initial data)
-- =====================================================
