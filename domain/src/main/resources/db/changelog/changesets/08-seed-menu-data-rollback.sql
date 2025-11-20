-- =====================================================
-- ROLLBACK: Delete seeded menu data
-- =====================================================
-- Migration: 08 - Rollback seed menu data
-- Author: System
-- Date: 2025-11-16
-- Description: Removes all seeded menu data
-- =====================================================

-- Delete all seeded menus (CASCADE will handle children)
DELETE FROM menus WHERE id IN (
    '00000000-0000-0000-0000-000000000001'::uuid,  -- dashboard
    '00000000-0000-0000-0000-000000000002'::uuid,  -- registry
    '00000000-0000-0000-0000-000000000003'::uuid,  -- rating
    '00000000-0000-0000-0000-000000000004'::uuid,  -- data-management
    '00000000-0000-0000-0000-000000000005'::uuid,  -- reports
    '00000000-0000-0000-0000-000000000006'::uuid,  -- system
    '00000000-0000-0000-0000-000000000021'::uuid,  -- registry-e-reestr
    '00000000-0000-0000-0000-000000000211'::uuid,  -- registry-e-reestr-university
    '00000000-0000-0000-0000-000000000212'::uuid,  -- registry-e-reestr-faculty
    '00000000-0000-0000-0000-000000000213'::uuid,  -- registry-e-reestr-cathedra
    '00000000-0000-0000-0000-000000000214'::uuid,  -- registry-e-reestr-teacher
    '00000000-0000-0000-0000-000000000215'::uuid,  -- registry-e-reestr-student
    '00000000-0000-0000-0000-000000000216'::uuid,  -- registry-e-reestr-diploma
    '00000000-0000-0000-0000-000000000217'::uuid,  -- registry-e-reestr-speciality-bachelor
    '00000000-0000-0000-0000-000000000218'::uuid,  -- registry-e-reestr-speciality-master
    '00000000-0000-0000-0000-000000000061'::uuid,  -- system-users
    '00000000-0000-0000-0000-000000000062'::uuid,  -- system-roles
    '00000000-0000-0000-0000-000000000063'::uuid,  -- system-permissions
    '00000000-0000-0000-0000-000000000064'::uuid,  -- system-translations
    '00000000-0000-0000-0000-000000000065'::uuid   -- system-menus
);

-- Note: Children are automatically deleted via ON DELETE CASCADE
