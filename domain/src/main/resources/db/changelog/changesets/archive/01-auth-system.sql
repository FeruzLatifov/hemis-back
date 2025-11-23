-- =====================================================
-- V1: COMPLETE AUTHENTICATION & AUTHORIZATION SYSTEM
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Complete auth system in one atomic unit
--
-- Contents:
-- PART 1: Schema (DDL) - users, roles, permissions tables
-- PART 2: Bootstrap Data (DML) - core roles + permissions
--
-- Strategy: ATOMIC DEPLOYMENT
-- - System needs both schema AND data to work
-- - Deployed together, rolled back together
-- - Single unit of work
--
-- Industry Pattern: Google Cloud, Stripe, AWS RDS
-- =====================================================

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 1: DATABASE SCHEMA (DDL)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- =====================================================
-- Table 1: users
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Authentication
    username VARCHAR(255) NOT NULL UNIQUE,
    username_lowercase VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    password_encryption VARCHAR(50),
    email VARCHAR(255) UNIQUE,

    -- Personal Information
    name VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    middle_name VARCHAR(255),
    full_name VARCHAR(255),
    position VARCHAR(255),

    -- User Settings
    language VARCHAR(20),
    time_zone VARCHAR(50),
    time_zone_auto BOOLEAN,
    locale VARCHAR(20),

    -- User Context (Multi-tenancy)
    user_type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    entity_code VARCHAR(255),
    university_id VARCHAR(255),
    phone VARCHAR(50),

    -- Legacy CUBA Relations
    group_id UUID,
    group_names VARCHAR(255),

    -- Account Status
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER DEFAULT 0,

    -- Security Settings
    ip_mask VARCHAR(200),
    change_password_at_logon BOOLEAN,

    -- Multi-tenancy
    sys_tenant_id VARCHAR(255),
    dtype VARCHAR(100),

    -- Versioning (Optimistic Locking)
    version INTEGER DEFAULT 1,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    -- Constraints
    CONSTRAINT chk_user_type CHECK (
        user_type IN ('UNIVERSITY', 'MINISTRY', 'ORGANIZATION', 'SYSTEM')
    )
);

-- Indexes for users table
CREATE INDEX IF NOT EXISTS idx_users_username_lowercase ON users(username_lowercase);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_entity_code ON users(entity_code) WHERE entity_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active) WHERE active = TRUE;
CREATE INDEX IF NOT EXISTS idx_users_user_type ON users(user_type);

-- =====================================================
-- Table 2: roles
-- =====================================================
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    role_type VARCHAR(50) NOT NULL DEFAULT 'CUSTOM',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    version INTEGER DEFAULT 1,

    CONSTRAINT chk_role_type CHECK (
        role_type IN ('SYSTEM', 'UNIVERSITY', 'CUSTOM')
    )
);

COMMENT ON COLUMN roles.version IS 'Optimistic locking version (JPA @Version)';

-- Indexes for roles table
CREATE INDEX IF NOT EXISTS idx_roles_code ON roles(code);
CREATE INDEX IF NOT EXISTS idx_roles_active ON roles(active) WHERE active = TRUE;
CREATE INDEX IF NOT EXISTS idx_roles_role_type ON roles(role_type);

-- =====================================================
-- Table 3: permissions
-- =====================================================
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL DEFAULT 'CUSTOM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    version INTEGER DEFAULT 1,

    CONSTRAINT chk_permission_category CHECK (
        category IN ('CORE', 'ADMIN', 'CUSTOM')
    ),
    CONSTRAINT chk_permission_action CHECK (
        action IN ('view', 'create', 'edit', 'delete', 'export', 'import', 'manage')
    )
);

COMMENT ON COLUMN permissions.deleted_at IS 'Soft delete timestamp (null = not deleted)';
COMMENT ON COLUMN permissions.deleted_by IS 'User who soft deleted this record';
COMMENT ON COLUMN permissions.version IS 'Optimistic locking version (JPA @Version)';

-- Indexes for permissions table
CREATE INDEX IF NOT EXISTS idx_permissions_code ON permissions(code);
CREATE INDEX IF NOT EXISTS idx_permissions_resource ON permissions(resource);
CREATE INDEX IF NOT EXISTS idx_permissions_category ON permissions(category);
CREATE INDEX IF NOT EXISTS idx_permissions_resource_action ON permissions(resource, action);
CREATE INDEX IF NOT EXISTS idx_permissions_deleted_at ON permissions(deleted_at) WHERE deleted_at IS NULL;

-- =====================================================
-- Table 4: user_roles (Many-to-Many)
-- =====================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),

    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ✅ FIX: Missing performance indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- =====================================================
-- Table 5: role_permissions (Many-to-Many)
-- =====================================================
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),

    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ✅ FIX: Missing performance indexes
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);

-- =====================================================
-- Table 6: system_messages (i18n)
-- =====================================================
CREATE TABLE IF NOT EXISTS system_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(100) NOT NULL,
    message_key VARCHAR(255) NOT NULL UNIQUE,
    message TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

COMMENT ON COLUMN system_messages.deleted_at IS 'Soft delete timestamp (null = not deleted)';
COMMENT ON COLUMN system_messages.deleted_by IS 'User who soft deleted this record';

CREATE INDEX IF NOT EXISTS idx_system_messages_category ON system_messages(category);
CREATE INDEX IF NOT EXISTS idx_system_messages_key ON system_messages(message_key);
CREATE INDEX IF NOT EXISTS idx_system_messages_deleted_at ON system_messages(deleted_at) WHERE deleted_at IS NULL;

-- =====================================================
-- Table 7: system_message_translations (i18n)
-- =====================================================
CREATE TABLE IF NOT EXISTS system_message_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES system_messages(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,
    translation TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT uq_message_language UNIQUE (message_id, language)
);

CREATE INDEX IF NOT EXISTS idx_smt_message_id ON system_message_translations(message_id);
CREATE INDEX IF NOT EXISTS idx_smt_language ON system_message_translations(language);
CREATE INDEX IF NOT EXISTS idx_smt_message_language ON system_message_translations(message_id, language);

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 2: BOOTSTRAP DATA (DML)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- =====================================================
-- Bootstrap Roles (5 core roles)
-- =====================================================
INSERT INTO roles (id, code, name, description, role_type, active) VALUES
(
    gen_random_uuid(),
    'SUPER_ADMIN',
    'Super Administrator',
    'Full system access - All permissions - Ministry level administration',
    'SYSTEM',
    TRUE
),
(
    gen_random_uuid(),
    'MINISTRY_ADMIN',
    'Ministry Administrator',
    'Ministry-level administrator - Can view all universities, manage reports',
    'SYSTEM',
    TRUE
),
(
    gen_random_uuid(),
    'UNIVERSITY_ADMIN',
    'University Administrator',
    'University-level administrator - Manage own university data (students, teachers)',
    'UNIVERSITY',
    TRUE
),
(
    gen_random_uuid(),
    'VIEWER',
    'Read-only Viewer',
    'Read-only access - Can only view data, no modifications',
    'SYSTEM',
    TRUE
),
(
    gen_random_uuid(),
    'REPORT_VIEWER',
    'Report Viewer',
    'Can view and generate reports - For statisticians and analysts',
    'CUSTOM',
    TRUE
)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- Bootstrap Permissions (30 core permissions)
-- =====================================================
INSERT INTO permissions (resource, action, code, name, description, category) VALUES

-- Dashboard
('dashboard', 'view', 'dashboard.view', 'View Dashboard', 'Access to main dashboard and statistics overview', 'CORE'),

-- Students
('students', 'view', 'students.view', 'View Students', 'View student list and detailed information', 'CORE'),
('students', 'create', 'students.create', 'Create Students', 'Add new students to the system', 'CORE'),
('students', 'edit', 'students.edit', 'Edit Students', 'Modify existing student information', 'CORE'),
('students', 'delete', 'students.delete', 'Delete Students', 'Soft delete students (mark as deleted)', 'CORE'),
('students', 'export', 'students.export', 'Export Students', 'Export student data to Excel/CSV', 'CORE'),

-- Teachers
('teachers', 'view', 'teachers.view', 'View Teachers', 'View teacher list and detailed information', 'CORE'),
('teachers', 'create', 'teachers.create', 'Create Teachers', 'Add new teachers to the system', 'CORE'),
('teachers', 'edit', 'teachers.edit', 'Edit Teachers', 'Modify existing teacher information', 'CORE'),
('teachers', 'delete', 'teachers.delete', 'Delete Teachers', 'Soft delete teachers', 'CORE'),
('teachers', 'export', 'teachers.export', 'Export Teachers', 'Export teacher data to Excel/CSV', 'CORE'),

-- Universities
('universities', 'view', 'universities.view', 'View Universities', 'View university list and information', 'CORE'),
('universities', 'create', 'universities.create', 'Create Universities', 'Add new universities (Ministry only)', 'ADMIN'),
('universities', 'edit', 'universities.edit', 'Edit Universities', 'Modify university information', 'ADMIN'),
('universities', 'delete', 'universities.delete', 'Delete Universities', 'Soft delete universities', 'ADMIN'),

-- Reports
('reports', 'view', 'reports.view', 'View Reports', 'Access to reports section', 'CORE'),
('reports', 'create', 'reports.create', 'Create Reports', 'Generate new reports', 'CORE'),
('reports', 'export', 'reports.export', 'Export Reports', 'Export reports to various formats', 'CORE'),

-- Users (Admin)
('users', 'view', 'users.view', 'View Users', 'View system users', 'ADMIN'),
('users', 'create', 'users.create', 'Create Users', 'Add new users to system', 'ADMIN'),
('users', 'edit', 'users.edit', 'Edit Users', 'Modify user information', 'ADMIN'),
('users', 'delete', 'users.delete', 'Delete Users', 'Deactivate users', 'ADMIN'),

-- Roles (Admin)
('roles', 'view', 'roles.view', 'View Roles', 'View system roles', 'ADMIN'),
('roles', 'create', 'roles.create', 'Create Roles', 'Create new roles', 'ADMIN'),
('roles', 'edit', 'roles.edit', 'Edit Roles', 'Modify role information', 'ADMIN'),
('roles', 'delete', 'roles.delete', 'Delete Roles', 'Remove roles', 'ADMIN'),

-- Permissions (Admin)
('permissions', 'view', 'permissions.view', 'View Permissions', 'View all permissions', 'ADMIN'),
('permissions', 'manage', 'permissions.manage', 'Manage Permissions', 'Assign permissions to roles', 'ADMIN'),

-- System Settings (Admin)
('settings', 'view', 'settings.view', 'View Settings', 'View system settings', 'ADMIN'),
('settings', 'edit', 'settings.edit', 'Edit Settings', 'Modify system settings', 'ADMIN')

ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- Assign ALL Permissions to SUPER_ADMIN
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- =====================================================
-- Assign Core Permissions to MINISTRY_ADMIN
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MINISTRY_ADMIN'
  AND p.category IN ('CORE')
  AND p.resource IN ('dashboard', 'universities', 'students', 'teachers', 'reports')
ON CONFLICT DO NOTHING;

-- =====================================================
-- Assign View Permissions to VIEWER
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.action = 'view'
ON CONFLICT DO NOTHING;

-- =====================================================
-- Verification (CRITICAL!)
-- =====================================================
DO $$
DECLARE
    table_count INTEGER;
    role_count INTEGER;
    perm_count INTEGER;
    mapping_count INTEGER;
BEGIN
    -- Verify tables created
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_name IN ('users', 'roles', 'permissions', 'user_roles', 'role_permissions', 'system_messages', 'system_message_translations');

    IF table_count != 7 THEN
        RAISE EXCEPTION 'Schema creation failed: Expected 7 tables, found %', table_count;
    END IF;

    -- Verify roles
    SELECT COUNT(*) INTO role_count FROM roles WHERE active = TRUE;
    IF role_count < 5 THEN
        RAISE EXCEPTION 'Bootstrap failed: Expected 5 roles, found %', role_count;
    END IF;

    -- Verify permissions
    SELECT COUNT(*) INTO perm_count FROM permissions;
    IF perm_count < 30 THEN
        RAISE EXCEPTION 'Bootstrap failed: Expected 30 permissions, found %', perm_count;
    END IF;

    -- Verify role-permission mappings
    SELECT COUNT(*) INTO mapping_count FROM role_permissions;
    IF mapping_count < 30 THEN
        RAISE EXCEPTION 'Bootstrap failed: Expected role-permission mappings, found %', mapping_count;
    END IF;

    -- Success log
    RAISE NOTICE '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
    RAISE NOTICE '✅ V1: AUTHENTICATION SYSTEM BOOTSTRAP COMPLETE';
    RAISE NOTICE '   Tables: % (users, roles, permissions, mappings, i18n)', table_count;
    RAISE NOTICE '   Roles: % (SUPER_ADMIN, MINISTRY_ADMIN, UNIVERSITY_ADMIN, VIEWER, REPORT_VIEWER)', role_count;
    RAISE NOTICE '   Permissions: % (dashboard, students, teachers, etc.)', perm_count;
    RAISE NOTICE '   Role-Permission Mappings: %', mapping_count;
    RAISE NOTICE '   Status: READY FOR USER MIGRATION (V2)';
    RAISE NOTICE '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
END $$;
