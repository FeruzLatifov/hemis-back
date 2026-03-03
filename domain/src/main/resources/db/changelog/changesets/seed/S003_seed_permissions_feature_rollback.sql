-- =====================================================
-- Rollback S003: REMOVE FEATURE PERMISSIONS (54)
-- =====================================================
-- Deletes the 54 feature permissions created by S003.
-- Removes role_permissions FK references first, then the permissions.
-- =====================================================

DO $$
DECLARE
    role_perms_exists BOOLEAN;
    perms_exists BOOLEAN;
    _codes text[] := ARRAY[
        -- Institutions (5)
        'institutions.view', 'institutions.universities.view', 'institutions.faculties.view',
        'institutions.departments.view', 'institutions.attached-specialities.view',
        -- Students sub-menus (6)
        'students.list.view', 'students.directions.view', 'students.groups.view',
        'students.diplomas.view', 'students.scholarships.view', 'students.certificates.view',
        -- Teachers sub-menus (3)
        'teachers.list.view', 'teachers.positions.view', 'teachers.qualifications.view',
        -- Science (6)
        'science.view', 'science.researchers.view', 'science.projects.view',
        'science.publications.view', 'science.methodical.view', 'science.intellectual.view',
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
    ];
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permissions'
    ) INTO role_perms_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'permissions'
    ) INTO perms_exists;

    -- Remove FK references first
    IF role_perms_exists AND perms_exists THEN
        DELETE FROM role_permissions WHERE permission_id IN (
            SELECT id FROM permissions WHERE code = ANY(_codes)
        );
        RAISE NOTICE 'S003 Rollback: Deleted role_permissions for 54 feature permissions';
    END IF;

    -- Remove the permissions
    IF perms_exists THEN
        DELETE FROM permissions WHERE code = ANY(_codes);
        RAISE NOTICE 'S003 Rollback: Deleted 54 feature permissions';
    ELSE
        RAISE NOTICE 'S003 Rollback: permissions table does not exist, skipping';
    END IF;
END $$;
