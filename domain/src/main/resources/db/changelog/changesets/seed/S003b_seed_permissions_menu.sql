-- =====================================================
-- S003b: SEED MENU PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Menu-related permissions (60 permissions)
-- Source: Archive 04-menu-permissions.sql
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO NOTHING)
-- =====================================================
-- Contents:
-- STEP 1: Registry Module (4 permissions)
-- STEP 2: Rating Module (14 permissions)
-- STEP 3: Data Management (10 permissions)
-- STEP 4: Reports Module (25 permissions)
-- STEP 5: System Module (7 permissions)
-- Total: 60 permissions
-- =====================================================

-- =====================================================
-- STEP 1: Registry Module Permissions (Reestlar)
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('registry', 'view', 'registry.view', 'View Registry', 'Access to registry section', 'CORE', 'system'),
('registry.e-reestr', 'view', 'registry.e-reestr.view', 'View E-Reestr', 'View electronic registry navigation', 'CORE', 'system'),
('registry.scientific', 'view', 'registry.scientific.view', 'View Scientific Registry', 'View scientific registry navigation', 'CORE', 'system'),
('registry.student-meta', 'view', 'registry.student-meta.view', 'View Student Metadata', 'View student metadata registry', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 2: Rating Module Permissions (Reyting)
-- =====================================================

-- Main Rating
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('rating', 'view', 'rating.view', 'View Rating', 'Access to rating section', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Administrative Rating
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('rating.administrative', 'view', 'rating.administrative.view', 'View Administrative Rating', 'View administrative rating section', 'REPORTS', 'system'),
('rating.administrative.employee', 'view', 'rating.administrative.employee.view', 'View Employee Rating', 'View administrative employee rating', 'REPORTS', 'system'),
('rating.administrative.students', 'view', 'rating.administrative.students.view', 'View Students Rating', 'View administrative students rating', 'REPORTS', 'system'),
('rating.administrative.sport', 'view', 'rating.administrative.sport.view', 'View Sport Rating', 'View administrative sport facilities rating', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Academic Rating
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('rating.academic', 'view', 'rating.academic.view', 'View Academic Rating', 'View academic rating section', 'REPORTS', 'system'),
('rating.academic.methodical', 'view', 'rating.academic.methodical.view', 'View Methodical Rating', 'View academic methodical publications rating', 'REPORTS', 'system'),
('rating.academic.study', 'view', 'rating.academic.study.view', 'View Study Rating', 'View academic study rating', 'REPORTS', 'system'),
('rating.academic.verification', 'view', 'rating.academic.verification.view', 'View Verification Rating', 'View verification type rating', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Scientific Rating
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('rating.scientific', 'view', 'rating.scientific.view', 'View Scientific Rating', 'View scientific rating section', 'REPORTS', 'system'),
('rating.scientific.publications', 'view', 'rating.scientific.publications.view', 'View Publications Rating', 'View scientific publications rating', 'REPORTS', 'system'),
('rating.scientific.projects', 'view', 'rating.scientific.projects.view', 'View Projects Rating', 'View scientific projects rating', 'REPORTS', 'system'),
('rating.scientific.intellectual', 'view', 'rating.scientific.intellectual.view', 'View Intellectual Property Rating', 'View intellectual property rating', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Student GPA
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('rating.student-gpa', 'view', 'rating.student-gpa.view', 'View Student GPA', 'View student GPA rating', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 3: Data Management Permissions (Application)
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('data', 'view', 'data.view', 'View Data Management', 'Access to data management section', 'CORE', 'system'),
('data.general', 'view', 'data.general.view', 'View General Data', 'View general data navigation', 'CORE', 'system'),
('data.structure', 'view', 'data.structure.view', 'View Structure Data', 'View structure data navigation', 'CORE', 'system'),
('data.employee', 'view', 'data.employee.view', 'View Employee Data', 'View employee data navigation', 'CORE', 'system'),
('data.student', 'view', 'data.student.view', 'View Student Data', 'View student data navigation', 'CORE', 'system'),
('data.education', 'view', 'data.education.view', 'View Education Data', 'View education data navigation', 'CORE', 'system'),
('data.study', 'view', 'data.study.view', 'View Study Data', 'View study data navigation', 'CORE', 'system'),
('data.science', 'view', 'data.science.view', 'View Science Data', 'View science data navigation', 'CORE', 'system'),
('data.organizational', 'view', 'data.organizational.view', 'View Organizational Data', 'View organizational data navigation', 'CORE', 'system'),
('data.contract-category', 'view', 'data.contract-category.view', 'View Contract Categories', 'View university contract categories', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 4: Reports Module Permissions
-- =====================================================

-- Universities Reports
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('reports.universities', 'view', 'reports.universities.view', 'View University Reports', 'View university reports', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Employees Reports
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('reports.employees', 'view', 'reports.employees.view', 'View Employee Reports', 'View employee reports section', 'REPORTS', 'system'),
('reports.employees.private', 'view', 'reports.employees.private.view', 'View Teacher Private Reports', 'View teacher private reports', 'REPORTS', 'system'),
('reports.employees.work', 'view', 'reports.employees.work.view', 'View Teacher Work Reports', 'View teacher work reports', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Students Reports
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('reports.students', 'view', 'reports.students.view', 'View Student Reports', 'View student reports section', 'REPORTS', 'system'),
('reports.students.statistics', 'view', 'reports.students.statistics.view', 'View Student Statistics', 'View student statistics reports', 'REPORTS', 'system'),
('reports.students.education', 'view', 'reports.students.education.view', 'View Student Education', 'View student education reports', 'REPORTS', 'system'),
('reports.students.private', 'view', 'reports.students.private.view', 'View Student Private', 'View student private reports', 'REPORTS', 'system'),
('reports.students.attendance', 'view', 'reports.students.attendance.view', 'View Student Attendance', 'View student attendance reports', 'REPORTS', 'system'),
('reports.students.score', 'view', 'reports.students.score.view', 'View Student Scores', 'View student score reports', 'REPORTS', 'system'),
('reports.students.dynamic', 'view', 'reports.students.dynamic.view', 'View Student Dynamics', 'View student full dynamic reports', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Academic Reports
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('reports.academic', 'view', 'reports.academic.view', 'View Academic Reports', 'View academic reports section', 'REPORTS', 'system'),
('reports.academic.study', 'view', 'reports.academic.study.view', 'View Academic Study Reports', 'View academic study reports', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Research Reports
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('reports.research', 'view', 'reports.research.view', 'View Research Reports', 'View research reports section', 'REPORTS', 'system'),
('reports.research.project', 'view', 'reports.research.project.view', 'View Research Projects', 'View research project reports', 'REPORTS', 'system'),
('reports.research.publication', 'view', 'reports.research.publication.view', 'View Research Publications', 'View research publication reports', 'REPORTS', 'system'),
('reports.research.researcher', 'view', 'reports.research.researcher.view', 'View Researchers', 'View researcher reports', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- Economic Reports
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('reports.economic', 'view', 'reports.economic.view', 'View Economic Reports', 'View economic reports section', 'REPORTS', 'system'),
('reports.economic.finance', 'view', 'reports.economic.finance.view', 'View Finance Reports', 'View economic finance reports', 'REPORTS', 'system'),
('reports.economic.xujalik', 'view', 'reports.economic.xujalik.view', 'View Xujalik Reports', 'View economic xujalik reports', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 5: System Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('system', 'view', 'system.view', 'View System', 'Access to system section', 'ADMIN', 'system'),
('system.temp', 'view', 'system.temp.view', 'View Temp System', 'View temporary system data', 'ADMIN', 'system'),
('system.translation', 'view', 'system.translation.view', 'View Translations', 'View translation list', 'ADMIN', 'system'),
('system.translation', 'manage', 'system.translation.manage', 'Manage Translations', 'Create, update, delete translations', 'ADMIN', 'system'),
('system.users', 'view', 'system.users.view', 'View University Users', 'View university user management', 'ADMIN', 'system'),
('system.logs', 'view', 'system.logs.view', 'View API Logs', 'View REST API access logs', 'ADMIN', 'system'),
('system.report-update', 'view', 'system.report-update.view', 'View Report Updates', 'View report update logs', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 6: Assign New Permissions to Roles
-- =====================================================
-- Note: S004 runs before this, so we need to manually assign

-- SUPER_ADMIN: All new permissions
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN (
    'registry.view', 'registry.e-reestr.view', 'registry.scientific.view', 'registry.student-meta.view',
    'rating.view', 'rating.administrative.view', 'rating.administrative.employee.view',
    'rating.administrative.students.view', 'rating.administrative.sport.view',
    'rating.academic.view', 'rating.academic.methodical.view', 'rating.academic.study.view',
    'rating.academic.verification.view', 'rating.scientific.view', 'rating.scientific.publications.view',
    'rating.scientific.projects.view', 'rating.scientific.intellectual.view', 'rating.student-gpa.view',
    'data.view', 'data.general.view', 'data.structure.view', 'data.employee.view', 'data.student.view',
    'data.education.view', 'data.study.view', 'data.science.view', 'data.organizational.view',
    'data.contract-category.view',
    'reports.universities.view', 'reports.employees.view', 'reports.employees.private.view',
    'reports.employees.work.view', 'reports.students.view', 'reports.students.statistics.view',
    'reports.students.education.view', 'reports.students.private.view', 'reports.students.attendance.view',
    'reports.students.score.view', 'reports.students.dynamic.view', 'reports.academic.view',
    'reports.academic.study.view', 'reports.research.view', 'reports.research.project.view',
    'reports.research.publication.view', 'reports.research.researcher.view', 'reports.economic.view',
    'reports.economic.finance.view', 'reports.economic.xujalik.view',
    'system.view', 'system.temp.view', 'system.translation.view', 'system.translation.manage',
    'system.users.view', 'system.logs.view', 'system.report-update.view'
  )
ON CONFLICT DO NOTHING;

-- VIEWER: All view permissions from new permissions
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.action = 'view'
  AND p.code LIKE 'registry.%' OR p.code LIKE 'rating.%' OR p.code LIKE 'data.%'
  OR p.code LIKE 'reports.%' OR p.code LIKE 'system.%'
ON CONFLICT DO NOTHING;

-- REPORT_VIEWER: Reports permissions
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'REPORT_VIEWER'
  AND p.code LIKE 'reports.%'
ON CONFLICT DO NOTHING;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    perm_count INTEGER;
    reports_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO perm_count FROM permissions;
    SELECT COUNT(*) INTO reports_count FROM permissions WHERE category = 'REPORTS';
    RAISE NOTICE 'S003b: Total permissions: %, REPORTS category: %', perm_count, reports_count;
END $$;
