-- Rollback S002: REMOVE CORE PERMISSIONS
DELETE FROM permissions WHERE category = 'CORE' AND created_by = 'system';
