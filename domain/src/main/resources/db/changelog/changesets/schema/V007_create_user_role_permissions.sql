-- =====================================================
-- V007: RBAC JUNCTIONS — user_role + role_permission
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-23
-- Purpose: Many-to-many junction tables for RBAC.
--   Depends on: V001 role, V002 permission, V005 users.
-- =====================================================

-- =====================================================
-- user_role — N:N between users and role
-- =====================================================
CREATE TABLE user_role (
    user_id     UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     UUID    NOT NULL REFERENCES role(id)  ON DELETE CASCADE,
    assigned_by VARCHAR(50),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_role_user_id ON user_role(user_id);
CREATE INDEX idx_user_role_role_id ON user_role(role_id);

-- =====================================================
-- role_permission — N:N between role and permission
-- =====================================================
CREATE TABLE role_permission (
    role_id       UUID    NOT NULL REFERENCES role(id)       ON DELETE CASCADE,
    permission_id UUID    NOT NULL REFERENCES permission(id) ON DELETE CASCADE,
    assigned_by   VARCHAR(50),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permission_role_id       ON role_permission(role_id);
CREATE INDEX idx_role_permission_permission_id ON role_permission(permission_id);
