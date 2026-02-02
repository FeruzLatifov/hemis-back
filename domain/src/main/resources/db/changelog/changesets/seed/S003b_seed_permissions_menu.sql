-- =====================================================
-- S003b: SEED MENU PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Updated: 2026-01-31
-- Purpose: Menu-related permissions (49 permissions)
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE SET)
-- =====================================================
-- Contents:
-- STEP 1: Institutions Module (5 permissions)
-- STEP 2: Students Module (7 permissions)
-- STEP 3: Teachers Module (4 permissions)
-- STEP 4: Science Module (6 permissions)
-- STEP 5: Reports Module (7 permissions)
-- STEP 6: Rating Module (5 permissions)
-- STEP 7: Classifiers Module (9 permissions)
-- STEP 8: System Module (6 permissions)
-- STEP 9: Assign Permissions to Roles
-- Total: 49 permissions
-- =====================================================

-- =====================================================
-- STEP 1: Institutions Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('institutions', 'view', 'institutions.view', 'View Institutions', 'Access to institutions section', 'CORE', 'system'),
('institutions.universities', 'view', 'institutions.universities.view', 'View Universities', 'View universities list', 'CORE', 'system'),
('institutions.faculties', 'view', 'institutions.faculties.view', 'View Faculties', 'View faculties list', 'CORE', 'system'),
('institutions.departments', 'view', 'institutions.departments.view', 'View Departments', 'View departments list', 'CORE', 'system'),
('institutions.attached-specialities', 'view', 'institutions.attached-specialities.view', 'View Attached Specialities', 'View attached specialities list', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 2: Students Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('students', 'view', 'students.view', 'View Students', 'Access to students section', 'CORE', 'system'),
('students.list', 'view', 'students.list.view', 'View Students List', 'View students list', 'CORE', 'system'),
('students.directions', 'view', 'students.directions.view', 'View Student Directions', 'View student directions', 'CORE', 'system'),
('students.groups', 'view', 'students.groups.view', 'View Student Groups', 'View student groups', 'CORE', 'system'),
('students.diplomas', 'view', 'students.diplomas.view', 'View Student Diplomas', 'View student diplomas', 'CORE', 'system'),
('students.scholarships', 'view', 'students.scholarships.view', 'View Student Scholarships', 'View student scholarships', 'CORE', 'system'),
('students.certificates', 'view', 'students.certificates.view', 'View Student Certificates', 'View student certificates', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 3: Teachers Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('teachers', 'view', 'teachers.view', 'View Teachers', 'Access to teachers section', 'CORE', 'system'),
('teachers.list', 'view', 'teachers.list.view', 'View Teachers List', 'View teachers list', 'CORE', 'system'),
('teachers.positions', 'view', 'teachers.positions.view', 'View Teacher Positions', 'View teacher positions', 'CORE', 'system'),
('teachers.qualifications', 'view', 'teachers.qualifications.view', 'View Teacher Qualifications', 'View teacher qualifications', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 4: Science Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('science', 'view', 'science.view', 'View Science', 'Access to science section', 'CORE', 'system'),
('science.researchers', 'view', 'science.researchers.view', 'View Researchers', 'View researchers list', 'CORE', 'system'),
('science.projects', 'view', 'science.projects.view', 'View Science Projects', 'View science projects', 'CORE', 'system'),
('science.publications', 'view', 'science.publications.view', 'View Science Publications', 'View science publications', 'CORE', 'system'),
('science.methodical', 'view', 'science.methodical.view', 'View Methodical Works', 'View methodical works', 'CORE', 'system'),
('science.intellectual', 'view', 'science.intellectual.view', 'View Intellectual Property', 'View intellectual property', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 5: Reports Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('reports', 'view', 'reports.view', 'View Reports', 'Access to reports section', 'REPORTS', 'system'),
('reports.students', 'view', 'reports.students.view', 'View Student Reports', 'View student reports', 'REPORTS', 'system'),
('reports.teachers', 'view', 'reports.teachers.view', 'View Teacher Reports', 'View teacher reports', 'REPORTS', 'system'),
('reports.institutions', 'view', 'reports.institutions.view', 'View Institution Reports', 'View institution reports', 'REPORTS', 'system'),
('reports.academic', 'view', 'reports.academic.view', 'View Academic Reports', 'View academic reports', 'REPORTS', 'system'),
('reports.research', 'view', 'reports.research.view', 'View Research Reports', 'View research reports', 'REPORTS', 'system'),
('reports.economic', 'view', 'reports.economic.view', 'View Economic Reports', 'View economic reports', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 6: Rating Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('rating', 'view', 'rating.view', 'View Rating', 'Access to rating section', 'REPORTS', 'system'),
('rating.administrative', 'view', 'rating.administrative.view', 'View Administrative Rating', 'View administrative rating', 'REPORTS', 'system'),
('rating.academic', 'view', 'rating.academic.view', 'View Academic Rating', 'View academic rating', 'REPORTS', 'system'),
('rating.scientific', 'view', 'rating.scientific.view', 'View Scientific Rating', 'View scientific rating', 'REPORTS', 'system'),
('rating.gpa', 'view', 'rating.gpa.view', 'View GPA Rating', 'View GPA rating', 'REPORTS', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 7: Classifiers Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('classifiers', 'view', 'classifiers.view', 'View Classifiers', 'Access to classifiers section', 'CORE', 'system'),
('classifiers.general', 'view', 'classifiers.general.view', 'View General Classifiers', 'View general classifiers', 'CORE', 'system'),
('classifiers.structure', 'view', 'classifiers.structure.view', 'View Structure Classifiers', 'View structure classifiers', 'CORE', 'system'),
('classifiers.employee', 'view', 'classifiers.employee.view', 'View Employee Classifiers', 'View employee classifiers', 'CORE', 'system'),
('classifiers.student', 'view', 'classifiers.student.view', 'View Student Classifiers', 'View student classifiers', 'CORE', 'system'),
('classifiers.education', 'view', 'classifiers.education.view', 'View Education Classifiers', 'View education classifiers', 'CORE', 'system'),
('classifiers.study', 'view', 'classifiers.study.view', 'View Study Classifiers', 'View study classifiers', 'CORE', 'system'),
('classifiers.science', 'view', 'classifiers.science.view', 'View Science Classifiers', 'View science classifiers', 'CORE', 'system'),
('classifiers.organizational', 'view', 'classifiers.organizational.view', 'View Organizational Classifiers', 'View organizational classifiers', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 8: System Module Permissions
-- =====================================================

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES
('system', 'view', 'system.view', 'View System', 'Access to system section', 'ADMIN', 'system'),
('system.translation', 'view', 'system.translation.view', 'View Translations', 'View translation list', 'ADMIN', 'system'),
('system.translation', 'manage', 'system.translation.manage', 'Manage Translations', 'Create, update, delete translations', 'ADMIN', 'system'),
('system.users', 'view', 'system.users.view', 'View University Users', 'View university user management', 'ADMIN', 'system'),
('system.logs', 'view', 'system.logs.view', 'View API Logs', 'View REST API access logs', 'ADMIN', 'system'),
('system.report-update', 'view', 'system.report-update.view', 'View Report Updates', 'View report update logs', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 9: Assign Permissions to Roles
-- =====================================================

-- SUPER_ADMIN: ALL permissions
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN (
    -- Institutions (5)
    'institutions.view', 'institutions.universities.view', 'institutions.faculties.view',
    'institutions.departments.view', 'institutions.attached-specialities.view',
    -- Students (7)
    'students.view', 'students.list.view', 'students.directions.view', 'students.groups.view',
    'students.diplomas.view', 'students.scholarships.view', 'students.certificates.view',
    -- Teachers (4)
    'teachers.view', 'teachers.list.view', 'teachers.positions.view', 'teachers.qualifications.view',
    -- Science (6)
    'science.view', 'science.researchers.view', 'science.projects.view',
    'science.publications.view', 'science.methodical.view', 'science.intellectual.view',
    -- Reports (7)
    'reports.view', 'reports.students.view', 'reports.teachers.view', 'reports.institutions.view',
    'reports.academic.view', 'reports.research.view', 'reports.economic.view',
    -- Rating (5)
    'rating.view', 'rating.administrative.view', 'rating.academic.view',
    'rating.scientific.view', 'rating.gpa.view',
    -- Classifiers (9)
    'classifiers.view', 'classifiers.general.view', 'classifiers.structure.view',
    'classifiers.employee.view', 'classifiers.student.view', 'classifiers.education.view',
    'classifiers.study.view', 'classifiers.science.view', 'classifiers.organizational.view',
    -- System (6)
    'system.view', 'system.translation.view', 'system.translation.manage',
    'system.users.view', 'system.logs.view', 'system.report-update.view'
  )
ON CONFLICT DO NOTHING;

-- OTM_ADMIN (mapped to VIEWER role): students.*, teachers.*, science.*, select reports
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.code IN (
    -- Students (7)
    'students.view', 'students.list.view', 'students.directions.view', 'students.groups.view',
    'students.diplomas.view', 'students.scholarships.view', 'students.certificates.view',
    -- Teachers (4)
    'teachers.view', 'teachers.list.view', 'teachers.positions.view', 'teachers.qualifications.view',
    -- Science (6)
    'science.view', 'science.researchers.view', 'science.projects.view',
    'science.publications.view', 'science.methodical.view', 'science.intellectual.view',
    -- Select Reports
    'reports.students.view', 'reports.teachers.view', 'reports.academic.view'
  )
ON CONFLICT DO NOTHING;

-- MINISTRY (mapped to REPORT_VIEWER role): institutions.*, reports.*, rating.*
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'REPORT_VIEWER'
  AND p.code IN (
    -- Institutions (5)
    'institutions.view', 'institutions.universities.view', 'institutions.faculties.view',
    'institutions.departments.view', 'institutions.attached-specialities.view',
    -- Reports (7)
    'reports.view', 'reports.students.view', 'reports.teachers.view', 'reports.institutions.view',
    'reports.academic.view', 'reports.research.view', 'reports.economic.view',
    -- Rating (5)
    'rating.view', 'rating.administrative.view', 'rating.academic.view',
    'rating.scientific.view', 'rating.gpa.view'
  )
ON CONFLICT DO NOTHING;

-- VIEWER: All *.view permissions (excluding system.* and classifiers.*)
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.action = 'view'
  AND (
    p.code LIKE 'institutions.%'
    OR p.code LIKE 'students.%'
    OR p.code LIKE 'teachers.%'
    OR p.code LIKE 'science.%'
    OR p.code LIKE 'reports.%'
    OR p.code LIKE 'rating.%'
  )
ON CONFLICT DO NOTHING;

-- REPORT_VIEWER: reports.* + rating.* + institutions.*
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'REPORT_VIEWER'
  AND (
    p.code LIKE 'reports.%'
    OR p.code LIKE 'rating.%'
    OR p.code LIKE 'institutions.%'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    perm_count INTEGER;
    core_count INTEGER;
    reports_count INTEGER;
    admin_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO perm_count FROM permissions;
    SELECT COUNT(*) INTO core_count FROM permissions WHERE category = 'CORE';
    SELECT COUNT(*) INTO reports_count FROM permissions WHERE category = 'REPORTS';
    SELECT COUNT(*) INTO admin_count FROM permissions WHERE category = 'ADMIN';
    RAISE NOTICE 'S003b: Total permissions: %, CORE: %, REPORTS: %, ADMIN: %',
        perm_count, core_count, reports_count, admin_count;
END $$;
