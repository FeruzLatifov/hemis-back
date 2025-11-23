-- =====================================================
-- V4: MENU STRUCTURE PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Add 60 menu-related permissions
--
-- Contents:
-- STEP 1: Registry Module (4 permissions)
-- STEP 2: Rating Module (14 permissions)
-- STEP 3: Data Management (10 permissions)
-- STEP 4: Reports Module (25 permissions)
-- STEP 5: System Module (7 permissions)
-- STEP 6: Assign to SUPER_ADMIN
--
-- Total: 60 permissions
--
-- Strategy: MENU ACCESS CONTROL
-- - Each menu item requires a permission
-- - All assigned to Super Administrator
-- - Includes system.translation.manage for i18n CRUD
-- =====================================================

-- =====================================================
-- STEP 1: Registry Module Permissions (Reestlar)
-- =====================================================

INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'registry', 'view', 'registry.view', 'View Registry', 'Access to registry section', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'registry.e-reestr', 'view', 'registry.e-reestr.view', 'View E-Reestr', 'View electronic registry navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'registry.scientific', 'view', 'registry.scientific.view', 'View Scientific Registry', 'View scientific registry navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'registry.student-meta', 'view', 'registry.student-meta.view', 'View Student Metadata', 'View student metadata registry', 'CORE', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- STEP 2: Rating Module Permissions (Reyting)
-- =====================================================

-- Main Rating
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'rating', 'view', 'rating.view', 'View Rating', 'Access to rating section', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Administrative Rating
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'rating.administrative', 'view', 'rating.administrative.view', 'View Administrative Rating', 'View administrative rating section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.administrative.employee', 'view', 'rating.administrative.employee.view', 'View Employee Rating', 'View administrative employee rating', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.administrative.students', 'view', 'rating.administrative.students.view', 'View Students Rating', 'View administrative students rating', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.administrative.sport', 'view', 'rating.administrative.sport.view', 'View Sport Rating', 'View administrative sport facilities rating', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Academic Rating
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'rating.academic', 'view', 'rating.academic.view', 'View Academic Rating', 'View academic rating section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.academic.methodical', 'view', 'rating.academic.methodical.view', 'View Methodical Rating', 'View academic methodical publications rating', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.academic.study', 'view', 'rating.academic.study.view', 'View Study Rating', 'View academic study rating', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.academic.verification', 'view', 'rating.academic.verification.view', 'View Verification Rating', 'View verification type rating', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Scientific Rating
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'rating.scientific', 'view', 'rating.scientific.view', 'View Scientific Rating', 'View scientific rating section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.scientific.publications', 'view', 'rating.scientific.publications.view', 'View Publications Rating', 'View scientific publications rating', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.scientific.projects', 'view', 'rating.scientific.projects.view', 'View Projects Rating', 'View scientific projects rating', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'rating.scientific.intellectual', 'view', 'rating.scientific.intellectual.view', 'View Intellectual Property Rating', 'View intellectual property rating', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Student GPA
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'rating.student-gpa', 'view', 'rating.student-gpa.view', 'View Student GPA', 'View student GPA rating', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- STEP 3: Data Management Permissions (Application)
-- =====================================================

INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'data', 'view', 'data.view', 'View Data Management', 'Access to data management section', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.general', 'view', 'data.general.view', 'View General Data', 'View general data navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.structure', 'view', 'data.structure.view', 'View Structure Data', 'View structure data navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.employee', 'view', 'data.employee.view', 'View Employee Data', 'View employee data navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.student', 'view', 'data.student.view', 'View Student Data', 'View student data navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.education', 'view', 'data.education.view', 'View Education Data', 'View education data navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.study', 'view', 'data.study.view', 'View Study Data', 'View study data navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.science', 'view', 'data.science.view', 'View Science Data', 'View science data navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.organizational', 'view', 'data.organizational.view', 'View Organizational Data', 'View organizational data navigation', 'CORE', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'data.contract-category', 'view', 'data.contract-category.view', 'View Contract Categories', 'View university contract categories', 'CORE', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- STEP 4: Reports Module Permissions
-- =====================================================

-- Universities Reports
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'reports.universities', 'view', 'reports.universities.view', 'View University Reports', 'View university reports', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Employees Reports
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'reports.employees', 'view', 'reports.employees.view', 'View Employee Reports', 'View employee reports section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.employees.private', 'view', 'reports.employees.private.view', 'View Teacher Private Reports', 'View teacher private reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.employees.work', 'view', 'reports.employees.work.view', 'View Teacher Work Reports', 'View teacher work reports', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Students Reports
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'reports.students', 'view', 'reports.students.view', 'View Student Reports', 'View student reports section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.students.statistics', 'view', 'reports.students.statistics.view', 'View Student Statistics', 'View student statistics reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.students.education', 'view', 'reports.students.education.view', 'View Student Education', 'View student education reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.students.private', 'view', 'reports.students.private.view', 'View Student Private', 'View student private reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.students.attendance', 'view', 'reports.students.attendance.view', 'View Student Attendance', 'View student attendance reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.students.score', 'view', 'reports.students.score.view', 'View Student Scores', 'View student score reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.students.dynamic', 'view', 'reports.students.dynamic.view', 'View Student Dynamics', 'View student full dynamic reports', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Academic Reports
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'reports.academic', 'view', 'reports.academic.view', 'View Academic Reports', 'View academic reports section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.academic.study', 'view', 'reports.academic.study.view', 'View Academic Study Reports', 'View academic study reports', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Research Reports
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'reports.research', 'view', 'reports.research.view', 'View Research Reports', 'View research reports section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.research.project', 'view', 'reports.research.project.view', 'View Research Projects', 'View research project reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.research.publication', 'view', 'reports.research.publication.view', 'View Research Publications', 'View research publication reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.research.researcher', 'view', 'reports.research.researcher.view', 'View Researchers', 'View researcher reports', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Economic Reports
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'reports.economic', 'view', 'reports.economic.view', 'View Economic Reports', 'View economic reports section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.economic.finance', 'view', 'reports.economic.finance.view', 'View Finance Reports', 'View economic finance reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.economic.xujalik', 'view', 'reports.economic.xujalik.view', 'View Xujalik Reports', 'View economic xujalik reports', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- STEP 5: System Module Permissions
-- =====================================================

INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'system', 'view', 'system.view', 'View System', 'Access to system section', 'ADMIN', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'system.temp', 'view', 'system.temp.view', 'View Temp System', 'View temporary system data', 'ADMIN', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'system.translation', 'view', 'system.translation.view', 'View Translations', 'View translation list', 'ADMIN', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'system.translation', 'manage', 'system.translation.manage', 'Manage Translations', 'Create, update, delete translations', 'ADMIN', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'system.users', 'view', 'system.users.view', 'View University Users', 'View university user management', 'ADMIN', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'system.logs', 'view', 'system.logs.view', 'View API Logs', 'View REST API access logs', 'ADMIN', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'system.report-update', 'view', 'system.report-update.view', 'View Report Updates', 'View report update logs', 'ADMIN', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Add comments
COMMENT ON TABLE permissions IS 'System permissions for role-based access control';
COMMENT ON COLUMN permissions.code IS 'Unique permission code (e.g., system.translation.manage)';
COMMENT ON COLUMN permissions.resource IS 'Resource identifier (e.g., system.translation)';
COMMENT ON COLUMN permissions.action IS 'Action type: view, manage, create, update, delete';

-- =====================================================
-- STEP 6: Assign All New Permissions to Super Administrator
-- =====================================================

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'Super Administrator'
  AND p.code IN (
    -- Registry
    'registry.view',
    'registry.e-reestr.view',
    'registry.scientific.view',
    'registry.student-meta.view',

    -- Rating
    'rating.view',
    'rating.administrative.view',
    'rating.administrative.employee.view',
    'rating.administrative.students.view',
    'rating.administrative.sport.view',
    'rating.academic.view',
    'rating.academic.methodical.view',
    'rating.academic.study.view',
    'rating.academic.verification.view',
    'rating.scientific.view',
    'rating.scientific.publications.view',
    'rating.scientific.projects.view',
    'rating.scientific.intellectual.view',
    'rating.student-gpa.view',

    -- Data
    'data.view',
    'data.general.view',
    'data.structure.view',
    'data.employee.view',
    'data.student.view',
    'data.education.view',
    'data.study.view',
    'data.science.view',
    'data.organizational.view',
    'data.contract-category.view',

    -- Reports
    'reports.universities.view',
    'reports.employees.view',
    'reports.employees.private.view',
    'reports.employees.work.view',
    'reports.students.view',
    'reports.students.statistics.view',
    'reports.students.education.view',
    'reports.students.private.view',
    'reports.students.attendance.view',
    'reports.students.score.view',
    'reports.students.dynamic.view',
    'reports.academic.view',
    'reports.academic.study.view',
    'reports.research.view',
    'reports.research.project.view',
    'reports.research.publication.view',
    'reports.research.researcher.view',
    'reports.economic.view',
    'reports.economic.finance.view',
    'reports.economic.xujalik.view',

    -- System
    'system.view',
    'system.temp.view',
    'system.translation.view',
    'system.translation.manage',
    'system.users.view',
    'system.logs.view',
    'system.report-update.view'
  )
  AND r.deleted_at IS NULL
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Verify insertion
DO $$
DECLARE
    permission_count INT;
    role_permission_count INT;
BEGIN
    -- Count inserted permissions
    SELECT COUNT(*) INTO permission_count
    FROM permissions
    WHERE code IN (
        'registry.view', 'rating.view', 'data.view', 'system.view',
        'system.translation.manage'
    );

    -- Count role-permission assignments
    SELECT COUNT(*) INTO role_permission_count
    FROM role_permissions rp
    INNER JOIN permissions p ON rp.permission_id = p.id
    INNER JOIN roles r ON rp.role_id = r.id
    WHERE r.name = 'Super Administrator'
      AND p.code LIKE 'system.translation.%';

    RAISE NOTICE 'V6 Migration Complete:';
    RAISE NOTICE '  - Total key permissions: %', permission_count;
    RAISE NOTICE '  - Translation permissions assigned to Super Admin: %', role_permission_count;
END $$;
