-- =====================================================
-- V6 Rollback: Remove Menu Structure Permissions
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-15
-- Description: Rollback V6 menu permissions migration
-- =====================================================

-- Remove role-permission assignments first
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permissions
    WHERE code IN (
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
);

-- Remove permissions
DELETE FROM permissions
WHERE code IN (
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
);
