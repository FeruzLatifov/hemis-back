-- =====================================================
-- V4 ROLLBACK: Remove Menu Permissions
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Rollback V4 menu permissions
--
-- WARNING: This will DELETE 60 menu permissions!
-- - All registry permissions
-- - All rating permissions
-- - All data management permissions
-- - All reports permissions
-- - All system permissions (including translation.manage)
-- =====================================================

-- Delete role-permission mappings for menu permissions
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permissions
    WHERE resource IN (
        'registry', 'registry.e-reestr', 'registry.scientific', 'registry.student-meta',
        'rating', 'rating.administrative', 'rating.academic', 'rating.scientific', 'rating.student-gpa',
        'data', 'data.general', 'data.structure', 'data.employee', 'data.student',
        'data.education', 'data.study', 'data.science', 'data.organizational', 'data.contract-category',
        'reports.universities', 'reports.employees', 'reports.students', 'reports.academic',
        'reports.research', 'reports.economic',
        'system', 'system.temp', 'system.translation', 'system.users', 'system.logs', 'system.report-update'
    )
);

-- Delete menu permissions
DELETE FROM permissions
WHERE resource IN (
    'registry', 'registry.e-reestr', 'registry.scientific', 'registry.student-meta',
    'rating', 'rating.administrative', 'rating.academic', 'rating.scientific', 'rating.student-gpa',
    'rating.administrative.employee', 'rating.administrative.students', 'rating.administrative.sport',
    'rating.academic.methodical', 'rating.academic.study', 'rating.academic.verification',
    'rating.scientific.publications', 'rating.scientific.projects', 'rating.scientific.intellectual',
    'data', 'data.general', 'data.structure', 'data.employee', 'data.student',
    'data.education', 'data.study', 'data.science', 'data.organizational', 'data.contract-category',
    'reports.universities', 'reports.employees', 'reports.employees.private', 'reports.employees.work',
    'reports.students', 'reports.students.statistics', 'reports.students.education',
    'reports.students.private', 'reports.students.attendance', 'reports.students.score', 'reports.students.dynamic',
    'reports.academic', 'reports.academic.study',
    'reports.research', 'reports.research.project', 'reports.research.publication', 'reports.research.researcher',
    'reports.economic', 'reports.economic.finance', 'reports.economic.xujalik',
    'system', 'system.temp', 'system.translation', 'system.users', 'system.logs', 'system.report-update'
);

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ V4 menu permissions rolled back successfully';
    RAISE NOTICE '   All 60 menu permissions and their mappings removed';
END $$;
