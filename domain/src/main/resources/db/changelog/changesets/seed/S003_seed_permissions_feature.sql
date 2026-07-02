-- =====================================================
-- S003: SEED FEATURE PERMISSIONS (54 permissions)
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-15
-- Purpose: Menu/feature permissions for all modules.
--          These are granular view permissions for sidebar
--          navigation, plus classifier edit and user management.
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE SET)
-- Merged from: S003b (46 menu perms) + S009 (4 classifier perms) + S016 (1 user mgmt perm) + 3 missing controller perms
-- Note: Role assignments are NOT here — they live in S004.
-- =====================================================

-- =====================================================
-- Institutions Module (5 permissions)
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions', 'view', 'institutions.view', 'View Institutions', 'Access to institutions section', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions.universities', 'view', 'institutions.universities.view', 'View Universities', 'View universities list', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions.faculties', 'view', 'institutions.faculties.view', 'View Faculties', 'View faculties list', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions.departments', 'view', 'institutions.departments.view', 'View Departments', 'View departments list', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions.attached-specialities', 'view', 'institutions.attached-specialities.view', 'View Attached Specialities', 'View attached specialities list', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions.attached-specialities', 'create', 'institutions.attached-specialities.create', 'Create Attached Speciality', 'Create attached speciality', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions.attached-specialities', 'edit', 'institutions.attached-specialities.edit', 'Edit Attached Speciality', 'Edit attached speciality', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions.attached-specialities', 'delete', 'institutions.attached-specialities.delete', 'Delete Attached Speciality', 'Delete attached speciality', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('institutions.university-specialities', 'view', 'institutions.university-specialities.view', 'View Institution Specialities', 'View institution specialities list', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Students Module (6 permissions)
-- Note: students.view is in S002 (base) — not duplicated here
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students.list', 'view', 'students.list.view', 'View Students List', 'View students list', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students.directions', 'view', 'students.directions.view', 'View Student Directions', 'View student directions', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students.groups', 'view', 'students.groups.view', 'View Student Groups', 'View student groups', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students.diplomas', 'view', 'students.diplomas.view', 'View Student Diplomas', 'View student diplomas', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students.scholarships', 'view', 'students.scholarships.view', 'View Student Scholarships', 'View student scholarships', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students.certificates', 'view', 'students.certificates.view', 'View Student Certificates', 'View student certificates', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Teachers Module (3 permissions)
-- Note: teachers.view is in S002 (base) — not duplicated here
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers.list', 'view', 'teachers.list.view', 'View Teachers List', 'View teachers list', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers.positions', 'view', 'teachers.positions.view', 'View Teacher Positions', 'View teacher positions', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers.qualifications', 'view', 'teachers.qualifications.view', 'View Teacher Qualifications', 'View teacher qualifications', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers.employee-jobs', 'view', 'teachers.employee-jobs.view', 'View Employee Jobs', 'View employee jobs', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Science Module (6 permissions)
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('science', 'view', 'science.view', 'View Science', 'Access to science section', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('science.researchers', 'view', 'science.researchers.view', 'View Researchers', 'View researchers list', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('science.projects', 'view', 'science.projects.view', 'View Science Projects', 'View science projects', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('science.publications', 'view', 'science.publications.view', 'View Science Publications', 'View science publications', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('science.methodical', 'view', 'science.methodical.view', 'View Methodical Works', 'View methodical works', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('science.intellectual', 'view', 'science.intellectual.view', 'View Intellectual Property', 'View intellectual property', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('science.dissertation-defense', 'view', 'science.dissertation-defense.view', 'View Dissertation Defense', 'View dissertation defense', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('science.research-activity', 'view', 'science.research-activity.view', 'View Scientific Activity', 'View scientific activity', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Reports Module (6 permissions)
-- Note: reports.view is in S002 (base) — not duplicated here
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports.students', 'view', 'reports.students.view', 'View Student Reports', 'View student reports', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports.teachers', 'view', 'reports.teachers.view', 'View Teacher Reports', 'View teacher reports', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports.institutions', 'view', 'reports.institutions.view', 'View Institution Reports', 'View institution reports', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports.academic', 'view', 'reports.academic.view', 'View Academic Reports', 'View academic reports', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports.research', 'view', 'reports.research.view', 'View Research Reports', 'View research reports', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports.economic', 'view', 'reports.economic.view', 'View Economic Reports', 'View economic reports', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Rating Module (5 permissions)
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('rating', 'view', 'rating.view', 'View Rating', 'Access to rating section', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('rating.administrative', 'view', 'rating.administrative.view', 'View Administrative Rating', 'View administrative rating', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('rating.academic', 'view', 'rating.academic.view', 'View Academic Rating', 'View academic rating', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('rating.scientific', 'view', 'rating.scientific.view', 'View Scientific Rating', 'View scientific rating', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('rating.gpa', 'view', 'rating.gpa.view', 'View GPA Rating', 'View GPA rating', 'REPORTS', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Classifiers Module (9 view + 1 edit + 3 new categories = 13 permissions)
-- 9 from S003b + 4 from S009
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers', 'view', 'classifiers.view', 'View Classifiers', 'Access to classifiers section', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers', 'edit', 'classifiers.edit', 'Edit Classifiers', 'Create, update, delete classifier items', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.general', 'view', 'classifiers.general.view', 'View General Classifiers', 'View general classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.structure', 'view', 'classifiers.structure.view', 'View Structure Classifiers', 'View structure classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.employee', 'view', 'classifiers.employee.view', 'View Employee Classifiers', 'View employee classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.student', 'view', 'classifiers.student.view', 'View Student Classifiers', 'View student classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.education', 'view', 'classifiers.education.view', 'View Education Classifiers', 'View education classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.study', 'view', 'classifiers.study.view', 'View Study Classifiers', 'View study classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.science', 'view', 'classifiers.science.view', 'View Science Classifiers', 'View science classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.organizational', 'view', 'classifiers.organizational.view', 'View Organizational Classifiers', 'View organizational classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.financial', 'view', 'classifiers.financial.view', 'View Financial Classifiers', 'View financial classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.diploma', 'view', 'classifiers.diploma.view', 'View Diploma Classifiers', 'View diploma classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers.speciality', 'view', 'classifiers.speciality.view', 'View Speciality Classifiers', 'View speciality classifiers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- System Module (9 permissions)
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('system', 'view', 'system.view', 'View System', 'Access to system section', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('system.translation', 'view', 'system.translation.view', 'View Translations', 'View translation list', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('system.translation', 'manage', 'system.translation.manage', 'Manage Translations', 'Create, update, delete translations', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('system.users', 'view', 'system.users.view', 'View University Users', 'View university user management', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('system.logs', 'view', 'system.logs.view', 'View API Logs', 'View REST API access logs', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('system.report-update', 'view', 'system.report-update.view', 'View Report Updates', 'View report update logs', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('system.menu', 'view', 'system.menu.view', 'View Menu Structure', 'View navigation menu tree', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('system.menus', 'manage', 'system.menus.manage', 'Manage Menus', 'Create, update, delete, reorder menu items', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('audit', 'view', 'audit.view', 'View Audit Logs', 'View system audit and activity logs', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- User Management (1 permission from S016)
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('users', 'manage', 'users.manage', 'Manage Users', 'Create, edit, delete users and manage roles', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- Roles management (moved from S008)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'manage', 'roles.manage', 'Manage roles', 'Create, edit, and delete roles', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Buildings Module (3 permissions — V010 module)
-- =====================================================

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('buildings', 'view', 'buildings.view', 'View Buildings', 'View university buildings (list, detail, history)', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('buildings', 'edit', 'buildings.edit', 'Edit Buildings', 'Create, update, soft-delete buildings', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('buildings', 'sync', 'buildings.sync', 'Sync Buildings from Univer', 'Bulk sync buildings from OTM via api-university', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    feature_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO feature_count FROM permission
    WHERE code IN (
        -- Institutions (9)
        'institutions.view', 'institutions.universities.view', 'institutions.faculties.view',
        'institutions.departments.view', 'institutions.attached-specialities.view',
        'institutions.attached-specialities.create', 'institutions.attached-specialities.edit',
        'institutions.attached-specialities.delete', 'institutions.university-specialities.view',
        -- Students sub-menus (6)
        'students.list.view', 'students.directions.view', 'students.groups.view',
        'students.diplomas.view', 'students.scholarships.view', 'students.certificates.view',
        -- Teachers sub-menus (4)
        'teachers.list.view', 'teachers.positions.view', 'teachers.qualifications.view',
        'teachers.employee-jobs.view',
        -- Science (8)
        'science.view', 'science.researchers.view', 'science.projects.view',
        'science.publications.view', 'science.methodical.view', 'science.intellectual.view',
        'science.dissertation-defense.view', 'science.research-activity.view',
        -- Reports sub-menus (6)
        'reports.students.view', 'reports.teachers.view', 'reports.institutions.view',
        'reports.academic.view', 'reports.research.view', 'reports.economic.view',
        -- Rating (5)
        'rating.view', 'rating.administrative.view', 'rating.academic.view',
        'rating.scientific.view', 'rating.gpa.view',
        -- Classifiers (13: 9 view + 1 edit + 3 new categories)
        'classifiers.view', 'classifiers.edit', 'classifiers.general.view',
        'classifiers.structure.view', 'classifiers.employee.view', 'classifiers.student.view',
        'classifiers.education.view', 'classifiers.study.view', 'classifiers.science.view',
        'classifiers.organizational.view', 'classifiers.financial.view',
        'classifiers.diploma.view', 'classifiers.speciality.view',
        -- System (9)
        'system.view', 'system.translation.view', 'system.translation.manage',
        'system.users.view', 'system.logs.view', 'system.report-update.view',
        'system.menu.view', 'system.menus.manage', 'audit.view',
        -- User Management (1)
        'users.manage'
    );

    IF feature_count < 61 THEN
        RAISE WARNING 'S003: Expected >= 61 feature permissions, found %', feature_count;
    ELSE
        RAISE NOTICE 'S003: % feature permissions seeded', feature_count;
    END IF;
END $$;
