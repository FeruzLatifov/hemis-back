-- Rollback M001: REMOVE MIGRATED USERS
DELETE FROM user_roles WHERE assigned_by = 'migration';
DELETE FROM users WHERE created_by = 'migration';
