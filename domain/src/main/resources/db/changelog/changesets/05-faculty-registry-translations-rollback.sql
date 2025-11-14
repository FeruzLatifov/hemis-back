-- liquibase formatted sql

-- changeset ai-assistant:5-faculty-registry-translations-rollback
-- comment: Rollback Faculty Registry translations

-- ========================================
-- ROLLBACK STRATEGY
-- ========================================
-- This rollback removes all Faculty Registry translations
-- in reverse order of creation:
-- 1. Details translations
-- 2. Actions translations
-- 3. Table translations (en-US, ru-RU, oz-UZ)
-- 4. Menu translations
-- 5. System messages
-- ========================================

-- ========================================
-- 1. DELETE DETAILS TRANSLATIONS
-- ========================================
DELETE FROM system_message_translations 
WHERE message_id IN (
    SELECT id FROM system_messages 
    WHERE message_key IN (
        'details.basicInfo',
        'details.auditInfo',
        'details.shortName',
        'details.facultyType',
        'details.createdAt',
        'details.createdBy',
        'details.updatedAt',
        'details.updatedBy'
    )
);

DELETE FROM system_messages 
WHERE message_key IN (
    'details.basicInfo',
    'details.auditInfo',
    'details.shortName',
    'details.facultyType',
    'details.createdAt',
    'details.createdBy',
    'details.updatedAt',
    'details.updatedBy'
);

-- ========================================
-- 2. DELETE ACTIONS TRANSLATIONS
-- ========================================
DELETE FROM system_message_translations 
WHERE message_id IN (
    SELECT id FROM system_messages 
    WHERE message_key IN (
        'actions.view',
        'actions.close'
    )
);

DELETE FROM system_messages 
WHERE message_key IN (
    'actions.view',
    'actions.close'
);

-- ========================================
-- 3. DELETE TABLE TRANSLATIONS
-- ========================================
DELETE FROM system_message_translations 
WHERE message_id IN (
    SELECT id FROM system_messages 
    WHERE message_key IN (
        'table.actions',
        'table.faculty.universityName',
        'table.faculty.universityCode',
        'table.faculty.code',
        'table.faculty.nameUz',
        'table.faculty.nameRu',
        'table.faculty.status',
        'table.faculty.facultyCount'
    )
);

DELETE FROM system_messages 
WHERE message_key IN (
    'table.actions',
    'table.faculty.universityName',
    'table.faculty.universityCode',
    'table.faculty.code',
    'table.faculty.nameUz',
    'table.faculty.nameRu',
    'table.faculty.status',
    'table.faculty.facultyCount'
);

-- ========================================
-- 4. DELETE MENU TRANSLATIONS
-- ========================================
DELETE FROM system_message_translations 
WHERE message_id IN (
    SELECT id FROM system_messages 
    WHERE message_key = 'menu.registry.faculty'
);

DELETE FROM system_messages 
WHERE message_key = 'menu.registry.faculty';

-- ========================================
-- ROLLBACK VERIFICATION
-- ========================================
-- After this rollback:
-- - All Faculty Registry translations removed
-- - No orphaned translation records
-- - system_messages and system_message_translations cleaned
-- ========================================

-- Verification query (should return 0):
-- SELECT COUNT(*) FROM system_messages WHERE message_key LIKE '%faculty%' OR message_key IN ('table.actions', 'actions.view', 'actions.close', 'details.%');

