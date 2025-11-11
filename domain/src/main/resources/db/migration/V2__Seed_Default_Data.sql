-- =====================================================
-- HEMIS Backend - Seed Default Data
-- =====================================================
-- Version: V2
-- Purpose: Insert default admin user, roles, and permissions
--
-- Contents:
-- 1. Default admin user (username: admin, password: admin)
-- 2. Default roles (SUPER_ADMIN, MINISTRY_ADMIN, UNIVERSITY_ADMIN, VIEWER)
-- 3. Default permissions (students.view, reports.create, etc.)
-- 4. Role-Permission assignments
-- 5. User-Role assignments
--
-- Security Note:
-- ⚠️  Default password is "admin" - MUST be changed in production!
-- =====================================================

-- =====================================================
-- Step 1: Create Default Admin User
-- =====================================================

INSERT INTO users (
    id,
    username,
    password,
    email,
    full_name,
    user_type,
    entity_code,
    enabled,
    account_non_locked
) VALUES (
    '00000000-0000-0000-0000-000000000001'::UUID,  -- Fixed UUID for admin
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- BCrypt hash of "admin"
    'admin@hemis.uz',
    'System Administrator',
    'SYSTEM',
    NULL,  -- System user (all universities)
    TRUE,
    TRUE
)
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- Step 2: Create Default Roles
-- =====================================================

INSERT INTO roles (id, code, name, description, role_type, active) VALUES

-- SUPER_ADMIN: Full system access (Ministry level)
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
 'University-level administrator - Manage own university data (students, teachers)',
 'UNIVERSITY',
 TRUE),

-- VIEWER: Read-only access
('10000000-0000-0000-0000-000000000004'::UUID,
 'VIEWER',
 'Read-only Viewer',
 'Read-only access - Can only view data, no modifications',
 'SYSTEM',
 TRUE),

-- REPORT_VIEWER: Report viewer (for external organizations)
('10000000-0000-0000-0000-000000000005'::UUID,
 'REPORT_VIEWER',
 'Report Viewer',
 'Can view and generate reports - For statisticians and analysts',
 'CUSTOM',
 TRUE)

ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- Step 3: Create Default Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category) VALUES

-- ────────────────────────────────────────
-- Dashboard Permissions
-- ────────────────────────────────────────
('dashboard', 'view', 'dashboard.view',
 'View Dashboard',
 'Access to main dashboard and statistics overview',
 'CORE'),

-- ────────────────────────────────────────
-- Student Permissions
-- ────────────────────────────────────────
('students', 'view', 'students.view',
 'View Students',
 'View student list and detailed information',
 'CORE'),

('students', 'create', 'students.create',
 'Create Students',
 'Add new students to the system',
 'CORE'),

('students', 'edit', 'students.edit',
 'Edit Students',
 'Modify existing student information',
 'CORE'),

('students', 'delete', 'students.delete',
 'Delete Students',
 'Soft delete students (mark as deleted)',
 'CORE'),

('students', 'export', 'students.export',
 'Export Students',
 'Export student data to Excel/CSV',
 'CORE'),

-- ────────────────────────────────────────
-- Teacher Permissions
-- ────────────────────────────────────────
('teachers', 'view', 'teachers.view',
 'View Teachers',
 'View teacher list and detailed information',
 'CORE'),

('teachers', 'create', 'teachers.create',
 'Create Teachers',
 'Add new teachers to the system',
 'CORE'),

('teachers', 'edit', 'teachers.edit',
 'Edit Teachers',
 'Modify existing teacher information',
 'CORE'),

('teachers', 'delete', 'teachers.delete',
 'Delete Teachers',
 'Soft delete teachers',
 'CORE'),

('teachers', 'export', 'teachers.export',
 'Export Teachers',
 'Export teacher data to Excel/CSV',
 'CORE'),

-- ────────────────────────────────────────
-- University Permissions
-- ────────────────────────────────────────
('universities', 'view', 'universities.view',
 'View Universities',
 'View university list and information',
 'CORE'),

('universities', 'create', 'universities.create',
 'Create Universities',
 'Add new universities (Ministry only)',
 'ADMIN'),

('universities', 'edit', 'universities.edit',
 'Edit Universities',
 'Modify university information',
 'ADMIN'),

('universities', 'manage', 'universities.manage',
 'Manage Universities',
 'Full university management access',
 'ADMIN'),

-- ────────────────────────────────────────
-- Report Permissions
-- ────────────────────────────────────────
('reports', 'view', 'reports.view',
 'View Reports',
 'Access to reports section and view existing reports',
 'REPORTS'),

('reports', 'create', 'reports.create',
 'Generate Reports',
 'Generate new reports and analytics',
 'REPORTS'),

('reports', 'export', 'reports.export',
 'Export Reports',
 'Export reports to Excel/PDF',
 'REPORTS'),

('reports', 'manage', 'reports.manage',
 'Manage Reports',
 'Full report management (create, edit, delete templates)',
 'REPORTS'),

-- ────────────────────────────────────────
-- User Management Permissions (Admin)
-- ────────────────────────────────────────
('users', 'view', 'users.view',
 'View Users',
 'View user list and information',
 'ADMIN'),

('users', 'create', 'users.create',
 'Create Users',
 'Add new users to the system',
 'ADMIN'),

('users', 'edit', 'users.edit',
 'Edit Users',
 'Modify user information (except password)',
 'ADMIN'),

('users', 'delete', 'users.delete',
 'Delete Users',
 'Soft delete users (deactivate accounts)',
 'ADMIN'),

('users', 'manage', 'users.manage',
 'Manage Users',
 'Full user management (create, edit, delete, assign roles)',
 'ADMIN'),

-- ────────────────────────────────────────
-- Role Management Permissions (Admin)
-- ────────────────────────────────────────
('roles', 'view', 'roles.view',
 'View Roles',
 'View role list and permissions',
 'ADMIN'),

('roles', 'create', 'roles.create',
 'Create Roles',
 'Create custom roles',
 'ADMIN'),

('roles', 'edit', 'roles.edit',
 'Edit Roles',
 'Modify role permissions',
 'ADMIN'),

('roles', 'manage', 'roles.manage',
 'Manage Roles',
 'Full role management (create, edit, delete, assign permissions)',
 'ADMIN'),

-- ────────────────────────────────────────
-- Permission Management (Super Admin only)
-- ────────────────────────────────────────
('permissions', 'view', 'permissions.view',
 'View Permissions',
 'View all available permissions',
 'ADMIN'),

('permissions', 'manage', 'permissions.manage',
 'Manage Permissions',
 'Create and manage system permissions (Super Admin only)',
 'ADMIN')

ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- Step 4: Assign Permissions to Roles
-- =====================================================

-- ────────────────────────────────────────
-- SUPER_ADMIN: ALL Permissions
-- ────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000001'::UUID,  -- SUPER_ADMIN
    id
FROM permissions
WHERE deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- ────────────────────────────────────────
-- MINISTRY_ADMIN: All except user/role management
-- ────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000002'::UUID,  -- MINISTRY_ADMIN
    id
FROM permissions
WHERE code NOT LIKE 'users.%'
  AND code NOT LIKE 'roles.%'
  AND code NOT LIKE 'permissions.%'
  AND deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- ────────────────────────────────────────
-- UNIVERSITY_ADMIN: Manage own university data
-- ────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000003'::UUID,  -- UNIVERSITY_ADMIN
    id
FROM permissions
WHERE code IN (
    'dashboard.view',
    'students.view', 'students.create', 'students.edit', 'students.export',
    'teachers.view', 'teachers.create', 'teachers.edit', 'teachers.export',
    'reports.view', 'reports.create', 'reports.export'
)
ON CONFLICT DO NOTHING;

-- ────────────────────────────────────────
-- VIEWER: Read-only (only view permissions)
-- ────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000004'::UUID,  -- VIEWER
    id
FROM permissions
WHERE action = 'view' AND deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- ────────────────────────────────────────
-- REPORT_VIEWER: Dashboard + Reports
-- ────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    '10000000-0000-0000-0000-000000000005'::UUID,  -- REPORT_VIEWER
    id
FROM permissions
WHERE (code LIKE 'dashboard.%' OR code LIKE 'reports.%')
  AND deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- =====================================================
-- Step 5: Assign SUPER_ADMIN Role to Admin User
-- =====================================================

INSERT INTO user_roles (user_id, role_id)
VALUES (
    '00000000-0000-0000-0000-000000000001'::UUID,  -- admin user
    '10000000-0000-0000-0000-000000000001'::UUID   -- SUPER_ADMIN role
)
ON CONFLICT DO NOTHING;

-- =====================================================
-- Validation & Statistics
-- =====================================================

DO $$
DECLARE
    user_count INTEGER;
    role_count INTEGER;
    perm_count INTEGER;
    admin_perm_count INTEGER;
BEGIN
    -- Count created data
    SELECT COUNT(*) INTO user_count FROM users WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO role_count FROM roles WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO perm_count FROM permissions WHERE deleted_at IS NULL;

    -- Count admin user's permissions
    SELECT COUNT(DISTINCT p.id) INTO admin_perm_count
    FROM permissions p
    JOIN role_permissions rp ON p.id = rp.permission_id
    JOIN user_roles ur ON rp.role_id = ur.role_id
    WHERE ur.user_id = '00000000-0000-0000-0000-000000000001'::UUID;

    RAISE NOTICE '==============================================';
    RAISE NOTICE 'V2 Migration Complete - Default Data Seeded';
    RAISE NOTICE '==============================================';
    RAISE NOTICE '';
    RAISE NOTICE 'Users created: %', user_count;
    RAISE NOTICE '  ✅ admin (SUPER_ADMIN)';
    RAISE NOTICE '';
    RAISE NOTICE 'Roles created: %', role_count;
    RAISE NOTICE '  ✅ SUPER_ADMIN';
    RAISE NOTICE '  ✅ MINISTRY_ADMIN';
    RAISE NOTICE '  ✅ UNIVERSITY_ADMIN';
    RAISE NOTICE '  ✅ VIEWER';
    RAISE NOTICE '  ✅ REPORT_VIEWER';
    RAISE NOTICE '';
    RAISE NOTICE 'Permissions created: %', perm_count;
    RAISE NOTICE '  ✅ dashboard.* (1)';
    RAISE NOTICE '  ✅ students.* (5)';
    RAISE NOTICE '  ✅ teachers.* (5)';
    RAISE NOTICE '  ✅ universities.* (4)';
    RAISE NOTICE '  ✅ reports.* (4)';
    RAISE NOTICE '  ✅ users.* (5)';
    RAISE NOTICE '  ✅ roles.* (4)';
    RAISE NOTICE '  ✅ permissions.* (2)';
    RAISE NOTICE '';
    RAISE NOTICE 'Admin user has % permissions', admin_perm_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Test Login:';
    RAISE NOTICE '  Username: admin';
    RAISE NOTICE '  Password: admin';
    RAISE NOTICE '  ⚠️  CHANGE DEFAULT PASSWORD IN PRODUCTION!';
    RAISE NOTICE '';
    RAISE NOTICE '==============================================';
END $$;

-- =====================================================
-- Migration Complete
-- =====================================================
-- ✅ Default admin user created (username: admin)
-- ✅ 5 default roles created
-- ✅ 30 default permissions created
-- ✅ Permissions assigned to roles
-- ✅ SUPER_ADMIN role assigned to admin user
-- ✅ Ready for V3 (additional indexes if needed)
-- =====================================================
