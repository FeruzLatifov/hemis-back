-- Rollback S001: REMOVE SEEDED ROLES
DELETE FROM roles WHERE created_by = 'system';
