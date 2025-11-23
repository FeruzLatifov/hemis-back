-- Rollback S003: REMOVE ADMIN PERMISSIONS
DELETE FROM permissions WHERE category = 'ADMIN' AND created_by = 'system';
