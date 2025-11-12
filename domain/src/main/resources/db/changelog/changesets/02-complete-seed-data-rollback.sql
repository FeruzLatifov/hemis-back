-- =====================================================
-- V2 ROLLBACK: Delete All Seed Data
-- =====================================================

DELETE FROM user_roles;
DELETE FROM role_permissions;
DELETE FROM users;
DELETE FROM permissions;
DELETE FROM roles;
