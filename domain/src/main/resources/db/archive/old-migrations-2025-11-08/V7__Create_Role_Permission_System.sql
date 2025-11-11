-- =====================================================
-- HEMIS Backend - Role & Permission System
-- =====================================================
-- Version: V5
-- Purpose: Create modern role-permission system for Spring Security
--
-- Background:
-- OLD-HEMIS uses CUBA Platform's sec_role and sec_permission (complex, CUBA-specific).
-- NEW-HEMIS needs clean, human-readable permission system.
--
-- Strategy: HYBRID APPROACH
-- - Keep OLD tables (sec_user, sec_role, sec_permission) - READ ONLY for backward compatibility
-- - Create NEW tables (hemishe_role, hemishe_permission) - CRUD for new system
-- - Permissions format: "resource.action" (e.g., "students.view", "reports.create")
--
-- MASTER PROMPT Compliance:
-- ✅ NO-RENAME: Old tables NOT modified (sec_user, sec_role remain intact)
-- ✅ NO-DELETE: No data deleted
-- ✅ NO-BREAKING-CHANGES: Old API continues to work
-- ✅ REPLICATION-SAFE: New tables only
-- =====================================================

-- =====================================================
-- Step 1: Create Role Table
-- =====================================================
-- Purpose: Define roles for users (e.g., MINISTRY_ADMIN, UNIVERSITY_ADMIN, VIEWER)
-- =====================================================

CREATE TABLE IF NOT EXISTS hemishe_role (
    -- Primary Key (UUID)
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Role Identification
    code VARCHAR(100) NOT NULL UNIQUE,  -- Machine-readable: MINISTRY_ADMIN, UNIVERSITY_ADMIN
    name VARCHAR(255) NOT NULL,         -- Human-readable: Ministry Administrator
    description TEXT,                   -- Full description

    -- Role Type (for filtering/categorization)
    role_type VARCHAR(50),              -- SYSTEM, UNIVERSITY, CUSTOM

    -- Active flag
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit Fields (from BaseEntity pattern)
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 1
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_hemishe_role_code
ON hemishe_role (code)
WHERE delete_ts IS NULL;

CREATE INDEX IF NOT EXISTS idx_hemishe_role_active
ON hemishe_role (active)
WHERE delete_ts IS NULL;

-- =====================================================
-- Step 2: Create Permission Table
-- =====================================================
-- Purpose: Define granular permissions (resource.action format)
-- Examples:
--   - students.view, students.create, students.edit
--   - reports.view, reports.create
--   - universities.view, universities.manage
-- =====================================================

CREATE TABLE IF NOT EXISTS hemishe_permission (
    -- Primary Key (UUID)
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Permission Identification
    resource VARCHAR(100) NOT NULL,     -- Resource: students, teachers, reports, universities
    action VARCHAR(50) NOT NULL,        -- Action: view, create, edit, delete, manage
    code VARCHAR(200) NOT NULL UNIQUE,  -- Full code: students.view, reports.create (auto-generated)

    -- Description
    name VARCHAR(255) NOT NULL,         -- Human-readable: View Students
    description TEXT,                   -- Full description

    -- Categorization
    category VARCHAR(50),               -- CORE, REPORTS, ADMIN, INTEGRATION

    -- Audit Fields
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 1,

    -- Ensure unique resource.action combination
    CONSTRAINT uk_resource_action UNIQUE (resource, action)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_hemishe_permission_code
ON hemishe_permission (code)
WHERE delete_ts IS NULL;

CREATE INDEX IF NOT EXISTS idx_hemishe_permission_resource
ON hemishe_permission (resource)
WHERE delete_ts IS NULL;

CREATE INDEX IF NOT EXISTS idx_hemishe_permission_category
ON hemishe_permission (category)
WHERE delete_ts IS NULL;

-- =====================================================
-- Step 3: Create User-Role Mapping Table
-- =====================================================
-- Purpose: Many-to-many relationship between users and roles
-- Note: User can have multiple roles (e.g., both MINISTRY_ADMIN and VIEWER)
-- =====================================================

CREATE TABLE IF NOT EXISTS hemishe_user_role (
    -- Composite Primary Key
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    -- Audit Fields (minimal)
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),

    -- Constraints
    PRIMARY KEY (user_id, role_id),

    -- Foreign Keys
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id)
        REFERENCES hemishe_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id)
        REFERENCES hemishe_role(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_user_role_user
ON hemishe_user_role (user_id);

CREATE INDEX IF NOT EXISTS idx_user_role_role
ON hemishe_user_role (role_id);

-- =====================================================
-- Step 4: Create Role-Permission Mapping Table
-- =====================================================
-- Purpose: Many-to-many relationship between roles and permissions
-- Note: Role can have multiple permissions
-- =====================================================

CREATE TABLE IF NOT EXISTS hemishe_role_permission (
    -- Composite Primary Key
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    -- Audit Fields (minimal)
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),

    -- Constraints
    PRIMARY KEY (role_id, permission_id),

    -- Foreign Keys
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id)
        REFERENCES hemishe_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id)
        REFERENCES hemishe_permission(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_role_permission_role
ON hemishe_role_permission (role_id);

CREATE INDEX IF NOT EXISTS idx_role_permission_permission
ON hemishe_role_permission (permission_id);

-- =====================================================
-- Step 5: Insert Default Roles
-- =====================================================

INSERT INTO hemishe_role (code, name, description, role_type, active) VALUES
('SUPER_ADMIN', 'Super Administrator', 'Full system access - Ministry level', 'SYSTEM', TRUE),
('MINISTRY_ADMIN', 'Ministry Administrator', 'Ministry-level administrator with full access', 'SYSTEM', TRUE),
('UNIVERSITY_ADMIN', 'University Administrator', 'University-level administrator', 'UNIVERSITY', TRUE),
('VIEWER', 'Viewer', 'Read-only access to data', 'SYSTEM', TRUE),
('REPORT_VIEWER', 'Report Viewer', 'Can view and generate reports', 'CUSTOM', TRUE)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- Step 6: Insert Default Permissions
-- =====================================================
-- Format: (resource, action, name, description, category)
-- =====================================================

INSERT INTO hemishe_permission (resource, action, code, name, description, category) VALUES
-- Dashboard
('dashboard', 'view', 'dashboard.view', 'View Dashboard', 'Access to main dashboard', 'CORE'),

-- Students
('students', 'view', 'students.view', 'View Students', 'View student list and details', 'CORE'),
('students', 'create', 'students.create', 'Create Students', 'Add new students', 'CORE'),
('students', 'edit', 'students.edit', 'Edit Students', 'Modify student information', 'CORE'),
('students', 'delete', 'students.delete', 'Delete Students', 'Remove students (soft delete)', 'CORE'),

-- Teachers
('teachers', 'view', 'teachers.view', 'View Teachers', 'View teacher list and details', 'CORE'),
('teachers', 'create', 'teachers.create', 'Create Teachers', 'Add new teachers', 'CORE'),
('teachers', 'edit', 'teachers.edit', 'Edit Teachers', 'Modify teacher information', 'CORE'),
('teachers', 'delete', 'teachers.delete', 'Delete Teachers', 'Remove teachers (soft delete)', 'CORE'),

-- Universities
('universities', 'view', 'universities.view', 'View Universities', 'View university list and details', 'CORE'),
('universities', 'create', 'universities.create', 'Create Universities', 'Add new universities', 'ADMIN'),
('universities', 'edit', 'universities.edit', 'Edit Universities', 'Modify university information', 'ADMIN'),
('universities', 'manage', 'universities.manage', 'Manage Universities', 'Full university management', 'ADMIN'),

-- Reports
('reports', 'view', 'reports.view', 'View Reports', 'Access reports section', 'REPORTS'),
('reports', 'create', 'reports.create', 'Create Reports', 'Generate new reports', 'REPORTS'),
('reports', 'export', 'reports.export', 'Export Reports', 'Export reports to Excel/PDF', 'REPORTS'),

-- Users & Roles (Admin)
('users', 'view', 'users.view', 'View Users', 'View user list', 'ADMIN'),
('users', 'create', 'users.create', 'Create Users', 'Add new users', 'ADMIN'),
('users', 'edit', 'users.edit', 'Edit Users', 'Modify user information', 'ADMIN'),
('users', 'manage', 'users.manage', 'Manage Users', 'Full user management', 'ADMIN'),

('roles', 'view', 'roles.view', 'View Roles', 'View role list', 'ADMIN'),
('roles', 'create', 'roles.create', 'Create Roles', 'Add new roles', 'ADMIN'),
('roles', 'edit', 'roles.edit', 'Edit Roles', 'Modify roles', 'ADMIN'),
('roles', 'manage', 'roles.manage', 'Manage Roles', 'Full role management', 'ADMIN'),

('permissions', 'view', 'permissions.view', 'View Permissions', 'View permission list', 'ADMIN'),
('permissions', 'manage', 'permissions.manage', 'Manage Permissions', 'Full permission management', 'ADMIN')

ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- Step 7: Assign Permissions to Roles
-- =====================================================

-- SUPER_ADMIN: All permissions
INSERT INTO hemishe_role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM hemishe_role WHERE code = 'SUPER_ADMIN'),
    id
FROM hemishe_permission
WHERE delete_ts IS NULL
ON CONFLICT DO NOTHING;

-- MINISTRY_ADMIN: All except user/role management
INSERT INTO hemishe_role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM hemishe_role WHERE code = 'MINISTRY_ADMIN'),
    id
FROM hemishe_permission
WHERE code NOT LIKE 'users.%'
  AND code NOT LIKE 'roles.%'
  AND code NOT LIKE 'permissions.%'
  AND delete_ts IS NULL
ON CONFLICT DO NOTHING;

-- UNIVERSITY_ADMIN: View and manage own university data
INSERT INTO hemishe_role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM hemishe_role WHERE code = 'UNIVERSITY_ADMIN'),
    id
FROM hemishe_permission
WHERE code IN (
    'dashboard.view',
    'students.view', 'students.create', 'students.edit',
    'teachers.view', 'teachers.create', 'teachers.edit',
    'reports.view', 'reports.create', 'reports.export'
)
ON CONFLICT DO NOTHING;

-- VIEWER: Read-only access
INSERT INTO hemishe_role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM hemishe_role WHERE code = 'VIEWER'),
    id
FROM hemishe_permission
WHERE action = 'view' AND delete_ts IS NULL
ON CONFLICT DO NOTHING;

-- REPORT_VIEWER: Dashboard + Reports
INSERT INTO hemishe_role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM hemishe_role WHERE code = 'REPORT_VIEWER'),
    id
FROM hemishe_permission
WHERE code LIKE 'dashboard.%'
   OR code LIKE 'reports.%'
   AND delete_ts IS NULL
ON CONFLICT DO NOTHING;

-- =====================================================
-- Step 8: Assign SUPER_ADMIN role to admin user
-- =====================================================

-- Assign SUPER_ADMIN role to existing admin user (from hemishe_user table)
INSERT INTO hemishe_user_role (user_id, role_id)
SELECT
    (SELECT id FROM hemishe_user WHERE username = 'admin'),
    (SELECT id FROM hemishe_role WHERE code = 'SUPER_ADMIN')
WHERE EXISTS (SELECT 1 FROM hemishe_user WHERE username = 'admin')
ON CONFLICT DO NOTHING;

-- =====================================================
-- Step 9: Create Helper Functions
-- =====================================================

-- Function to get user permissions as array
CREATE OR REPLACE FUNCTION get_user_permissions(p_user_id UUID)
RETURNS TEXT[] AS $$
BEGIN
    RETURN ARRAY(
        SELECT DISTINCT p.code
        FROM hemishe_permission p
        INNER JOIN hemishe_role_permission rp ON p.id = rp.permission_id
        INNER JOIN hemishe_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = p_user_id
          AND p.delete_ts IS NULL
        ORDER BY p.code
    );
END;
$$ LANGUAGE plpgsql;

-- Function to check if user has permission
CREATE OR REPLACE FUNCTION user_has_permission(p_user_id UUID, p_permission_code VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM hemishe_permission p
        INNER JOIN hemishe_role_permission rp ON p.id = rp.permission_id
        INNER JOIN hemishe_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = p_user_id
          AND p.code = p_permission_code
          AND p.delete_ts IS NULL
    );
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- Step 10: Validation
-- =====================================================

DO $$
DECLARE
    role_count INTEGER;
    permission_count INTEGER;
    admin_permission_count INTEGER;
BEGIN
    -- Check roles created
    SELECT COUNT(*) INTO role_count FROM hemishe_role WHERE delete_ts IS NULL;
    RAISE NOTICE 'Created % roles', role_count;

    -- Check permissions created
    SELECT COUNT(*) INTO permission_count FROM hemishe_permission WHERE delete_ts IS NULL;
    RAISE NOTICE 'Created % permissions', permission_count;

    -- Check admin user permissions
    IF EXISTS (SELECT 1 FROM hemishe_user WHERE username = 'admin') THEN
        SELECT COUNT(*) INTO admin_permission_count
        FROM get_user_permissions((SELECT id FROM hemishe_user WHERE username = 'admin'));
        RAISE NOTICE 'Admin user has % permissions', admin_permission_count;
    END IF;

    IF role_count = 0 OR permission_count = 0 THEN
        RAISE WARNING 'Role/Permission creation may have failed!';
    END IF;
END $$;

-- =====================================================
-- Migration Complete
-- =====================================================
-- ✅ New role-permission system created
-- ✅ 5 default roles (SUPER_ADMIN, MINISTRY_ADMIN, etc.)
-- ✅ 25+ default permissions (students.view, reports.create, etc.)
-- ✅ Admin user assigned SUPER_ADMIN role
-- ✅ Helper functions created for permission checks
-- ✅ OLD tables (sec_user, sec_role, sec_permission) untouched
-- =====================================================
