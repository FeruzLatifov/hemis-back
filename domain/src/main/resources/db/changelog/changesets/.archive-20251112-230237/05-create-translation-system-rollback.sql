-- ================================================================
-- V6 ROLLBACK: Remove Complete Menu & Translation System
-- ================================================================

-- 1. Remove role permissions for new menu permissions
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permissions 
    WHERE code LIKE 'dashboard.view'
       OR code LIKE 'registry%'
       OR code LIKE 'rating%'
       OR code LIKE 'data.%'
       OR code LIKE 'reports%'
       OR code LIKE 'system.%'
);

-- 2. Delete new menu permissions
DELETE FROM permissions
WHERE code LIKE 'dashboard.view'
   OR code LIKE 'registry%'
   OR code LIKE 'rating%'
   OR code LIKE 'data.%'
   OR code LIKE 'reports%'
   OR code LIKE 'system.%';

-- 3. Delete menu translations
DELETE FROM h_system_message_translation
WHERE message_id IN (
    SELECT id FROM h_system_message
    WHERE category = 'menu'
);

-- 4. Delete menu messages
DELETE FROM h_system_message
WHERE category = 'menu';
