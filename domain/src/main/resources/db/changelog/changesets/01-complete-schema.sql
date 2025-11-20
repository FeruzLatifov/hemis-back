-- =====================================================
-- HEMIS Backend - Authentication & Authorization Tables
-- =====================================================
-- Version: V1
-- Purpose: Create modern, clean authentication system
--
-- Strategy: HYBRID APPROACH
-- - OLD tables (sec_user, sec_role, sec_permission) → READ ONLY, untouched
-- - NEW tables (users, roles, permissions) → READ + WRITE, modern
-- - Login flow checks BOTH tables (new first, fallback to old)
--
-- Design Philosophy:
-- ✅ Clean naming (users NOT app_users, NOT hemishe_user)
-- ✅ Modern timestamps (created_at NOT create_ts)
-- ✅ Best practice constraints
-- ✅ CASCADE deletes for mapping tables
-- ✅ Ready for microservices
--
-- CRITICAL:
-- ❌ NO changes to old tables (sec_user, sec_role, etc.)
-- ❌ NO data migration (old users stay in sec_user)
-- ✅ Zero downtime (old-hemis continues working)
-- =====================================================

-- =====================================================
-- Table 1: users
-- =====================================================
-- Purpose: Modern user authentication table
-- Replaces: hemishe_user (old attempt), sec_user (legacy)
-- Format: Clean Spring Boot style
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Authentication (from sec_user)
    username VARCHAR(255) NOT NULL UNIQUE,  -- from login
    username_lowercase VARCHAR(255),  -- from login_lc
    password VARCHAR(255) NOT NULL,  -- BCrypt hash
    password_encryption VARCHAR(50),  -- from password_encryption
    email VARCHAR(255) UNIQUE,

    -- Personal Information (from sec_user)
    name VARCHAR(255),  -- from name (legacy full name)
    first_name VARCHAR(255),  -- from first_name
    last_name VARCHAR(255),  -- from last_name
    middle_name VARCHAR(255),  -- from middle_name
    full_name VARCHAR(255),  -- computed field for modern usage
    position VARCHAR(255),  -- from position_

    -- User Settings (from sec_user)
    language VARCHAR(20),  -- from language_
    time_zone VARCHAR(50),  -- from time_zone
    time_zone_auto BOOLEAN,  -- from time_zone_auto
    locale VARCHAR(20),  -- computed from language

    -- User Context (Multi-tenancy)
    user_type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
        -- Values: UNIVERSITY, MINISTRY, ORGANIZATION, SYSTEM
    entity_code VARCHAR(255),
        -- OTM code for UNIVERSITY users (TATU, NUUZ, etc.)
        -- NULL for SYSTEM users
    university_id VARCHAR(255),  -- from _university (FK to hemishe_e_university.code)
    phone VARCHAR(50),
        -- Optional phone number

    -- Legacy CUBA Relations (from sec_user)
    group_id UUID,  -- from group_id (FK to sec_group.id)
    group_names VARCHAR(255),  -- from group_names

    -- Account Status
    enabled BOOLEAN NOT NULL DEFAULT TRUE,  -- modern
    active BOOLEAN NOT NULL DEFAULT TRUE,  -- from active (legacy)
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER DEFAULT 0,
        -- Lock account after 5 failed attempts

    -- Security Settings (from sec_user)
    ip_mask VARCHAR(200),  -- from ip_mask
    change_password_at_logon BOOLEAN,  -- from change_password_at_logon

    -- Multi-tenancy (from sec_user)
    sys_tenant_id VARCHAR(255),  -- from sys_tenant_id
    dtype VARCHAR(100),  -- from dtype (discriminator type)

    -- Versioning (Optimistic Locking)
    version INTEGER DEFAULT 1,

    -- Timestamps (Modern naming)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- from create_ts
    created_by VARCHAR(50),  -- from created_by
    updated_at TIMESTAMP,  -- from update_ts
    updated_by VARCHAR(50),  -- from updated_by
    deleted_at TIMESTAMP,  -- Soft delete (NULL = active) from delete_ts
    deleted_by VARCHAR(50),  -- from deleted_by

    -- Constraints
    CONSTRAINT chk_user_type CHECK (
        user_type IN ('UNIVERSITY', 'MINISTRY', 'ORGANIZATION', 'SYSTEM')
    ),
    CONSTRAINT chk_entity_code CHECK (
        (user_type = 'SYSTEM' AND entity_code IS NULL) OR
        (user_type != 'SYSTEM' AND entity_code IS NOT NULL)
    )
);

-- Foreign key constraints for legacy relations
ALTER TABLE users
    ADD CONSTRAINT fk_users_group
    FOREIGN KEY (group_id) REFERENCES sec_group(id) ON DELETE SET NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_university
    FOREIGN KEY (university_id) REFERENCES hemishe_e_university(code) ON DELETE SET NULL;

COMMENT ON TABLE users IS 'Modern user authentication table - JWT-ready - includes all legacy sec_user fields';
COMMENT ON COLUMN users.username IS 'Login username - unique identifier (from login)';
COMMENT ON COLUMN users.password IS 'BCrypt password hash - NEVER store plain text';
COMMENT ON COLUMN users.first_name IS 'User first name (from sec_user.first_name)';
COMMENT ON COLUMN users.last_name IS 'User last name (from sec_user.last_name)';
COMMENT ON COLUMN users.middle_name IS 'User middle name (from sec_user.middle_name)';
COMMENT ON COLUMN users.university_id IS 'University code (from sec_user._university FK to hemishe_e_university.code)';
COMMENT ON COLUMN users.group_id IS 'Legacy CUBA group (from sec_user.group_id FK to sec_group.id)';
COMMENT ON COLUMN users.user_type IS 'User category: UNIVERSITY, MINISTRY, ORGANIZATION, SYSTEM';
COMMENT ON COLUMN users.entity_code IS 'OTM code for university users (e.g., TATU, NUUZ)';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp (NULL = active user)';

-- =====================================================
-- Table 2: roles
-- =====================================================
-- Purpose: Role definitions (RBAC - Role-Based Access Control)
-- Format: Clean, human-readable codes
-- =====================================================

CREATE TABLE IF NOT EXISTS roles (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Role Identification
    code VARCHAR(100) NOT NULL UNIQUE,
        -- Machine-readable: SUPER_ADMIN, MINISTRY_ADMIN, UNIVERSITY_ADMIN, VIEWER
    name VARCHAR(255) NOT NULL,
        -- Human-readable: Super Administrator, University Administrator
    description TEXT,

    -- Role Type
    role_type VARCHAR(50),
        -- Values: SYSTEM, UNIVERSITY, CUSTOM
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Versioning (Optimistic Locking)
    version INTEGER DEFAULT 1,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,  -- Soft delete

    -- Constraints
    CONSTRAINT chk_role_type CHECK (
        role_type IN ('SYSTEM', 'UNIVERSITY', 'CUSTOM') OR role_type IS NULL
    )
);

COMMENT ON TABLE roles IS 'Role definitions - RBAC system';
COMMENT ON COLUMN roles.code IS 'Unique role code: SUPER_ADMIN, MINISTRY_ADMIN, etc.';
COMMENT ON COLUMN roles.role_type IS 'SYSTEM (built-in), UNIVERSITY (per-OTM), CUSTOM (user-defined)';

-- =====================================================
-- Table 3: permissions
-- =====================================================
-- Purpose: Granular permission definitions
-- Format: resource.action (e.g., students.view, reports.create)
-- Replaces: sec_permission (Type 20: hemishe_HStudent:read)
-- =====================================================

CREATE TABLE IF NOT EXISTS permissions (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Permission Structure
    resource VARCHAR(100) NOT NULL,
        -- Resource: students, teachers, reports, universities, users, roles
    action VARCHAR(50) NOT NULL,
        -- Action: view, create, edit, delete, manage, export
    code VARCHAR(200) NOT NULL UNIQUE,
        -- Full code: students.view, reports.create (auto-generated: resource.action)

    -- Human-Readable
    name VARCHAR(255) NOT NULL,
        -- Display name: View Students, Create Reports
    description TEXT,

    -- Categorization
    category VARCHAR(50),
        -- Values: CORE, REPORTS, ADMIN, INTEGRATION

    -- Versioning (Optimistic Locking)
    version INTEGER DEFAULT 1,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,  -- Soft delete

    -- Constraints
    CONSTRAINT uk_resource_action UNIQUE (resource, action),
    CONSTRAINT chk_permission_code CHECK (code = resource || '.' || action),
    CONSTRAINT chk_category CHECK (
        category IN ('CORE', 'REPORTS', 'ADMIN', 'INTEGRATION') OR category IS NULL
    )
);

COMMENT ON TABLE permissions IS 'Granular permissions - resource.action format (e.g., students.view)';
COMMENT ON COLUMN permissions.resource IS 'Resource name: students, teachers, reports, etc.';
COMMENT ON COLUMN permissions.action IS 'Action: view, create, edit, delete, manage, export';
COMMENT ON COLUMN permissions.code IS 'Full permission code: resource.action (e.g., students.view)';
COMMENT ON COLUMN permissions.category IS 'CORE (business), REPORTS, ADMIN (user mgmt), INTEGRATION (APIs)';

-- =====================================================
-- Table 4: user_roles (Many-to-Many Mapping)
-- =====================================================
-- Purpose: Assign roles to users
-- Note: One user can have multiple roles
-- =====================================================

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Primary Key (Composite)
    PRIMARY KEY (user_id, role_id),

    -- Foreign Keys with CASCADE delete
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON DELETE CASCADE
);

COMMENT ON TABLE user_roles IS 'User-Role mapping - many-to-many relationship';

-- =====================================================
-- Table 5: role_permissions (Many-to-Many Mapping)
-- =====================================================
-- Purpose: Assign permissions to roles
-- Note: One role can have multiple permissions
-- =====================================================

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Primary Key (Composite)
    PRIMARY KEY (role_id, permission_id),

    -- Foreign Keys with CASCADE delete
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id)
        REFERENCES permissions(id) ON DELETE CASCADE
);

COMMENT ON TABLE role_permissions IS 'Role-Permission mapping - many-to-many relationship';

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- Users table indexes
CREATE INDEX IF NOT EXISTS idx_users_username
    ON users(username) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_username_lowercase
    ON users(username_lowercase) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users(email) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_entity_code
    ON users(entity_code) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_user_type
    ON users(user_type) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_university_id
    ON users(university_id) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_group_id
    ON users(group_id) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_active
    ON users(active) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_deleted
    ON users(deleted_at);

-- Roles table indexes
CREATE INDEX IF NOT EXISTS idx_roles_code
    ON roles(code) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_roles_type
    ON roles(role_type) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_roles_active
    ON roles(active) WHERE deleted_at IS NULL;

-- Permissions table indexes
CREATE INDEX IF NOT EXISTS idx_permissions_code
    ON permissions(code) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_permissions_resource
    ON permissions(resource) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_permissions_category
    ON permissions(category) WHERE deleted_at IS NULL;

-- Mapping tables indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_user
    ON user_roles(user_id);

CREATE INDEX IF NOT EXISTS idx_user_roles_role
    ON user_roles(role_id);

CREATE INDEX IF NOT EXISTS idx_role_permissions_role
    ON role_permissions(role_id);

CREATE INDEX IF NOT EXISTS idx_role_permissions_permission
    ON role_permissions(permission_id);

-- =====================================================
-- Validation & Statistics
-- =====================================================

DO $$
DECLARE
    tables_created INTEGER;
BEGIN
    -- Count newly created tables
    SELECT COUNT(*) INTO tables_created
    FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_name IN ('users', 'roles', 'permissions', 'user_roles', 'role_permissions');

    RAISE NOTICE '==============================================';
    RAISE NOTICE 'V1 Migration Complete';
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Created % authentication tables', tables_created;
    RAISE NOTICE '';
    RAISE NOTICE 'Tables:';
    RAISE NOTICE '  ✅ users (modern authentication)';
    RAISE NOTICE '  ✅ roles (RBAC)';
    RAISE NOTICE '  ✅ permissions (resource.action format)';
    RAISE NOTICE '  ✅ user_roles (mapping)';
    RAISE NOTICE '  ✅ role_permissions (mapping)';
    RAISE NOTICE '';
    RAISE NOTICE 'Old tables UNTOUCHED:';
    RAISE NOTICE '  ✅ sec_user (343 users) - READ ONLY';
    RAISE NOTICE '  ✅ sec_role - READ ONLY';
    RAISE NOTICE '  ✅ sec_permission (4,349) - READ ONLY';
    RAISE NOTICE '';
    RAISE NOTICE 'Next: V2_Seed_Default_Data.sql';
    RAISE NOTICE '==============================================';

    IF tables_created != 5 THEN
        RAISE WARNING 'Expected 5 tables, got %! Check migration', tables_created;
    END IF;
END $$;

-- =====================================================
-- Migration Complete
-- =====================================================
-- ✅ New clean tables created (users, roles, permissions)
-- ✅ Indexes created for performance
-- ✅ Foreign keys with CASCADE delete
-- ✅ Constraints for data integrity
-- ✅ OLD tables (sec_user, etc.) completely untouched
-- ✅ Ready for V2 (seed default data)
-- =====================================================

-- =====================================================
-- SystemMessage Tables (for i18n/translations)
-- =====================================================

CREATE TABLE IF NOT EXISTS system_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(100) NOT NULL,
    message_key VARCHAR(255) NOT NULL UNIQUE,
    message TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_system_messages_category ON system_messages(category);
CREATE INDEX IF NOT EXISTS idx_system_messages_key ON system_messages(message_key);

CREATE TABLE IF NOT EXISTS system_message_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES system_messages(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,
    translation TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(message_id, language)
);

CREATE INDEX IF NOT EXISTS idx_system_message_translations_message ON system_message_translations(message_id);
CREATE INDEX IF NOT EXISTS idx_system_message_translations_language ON system_message_translations(language);
