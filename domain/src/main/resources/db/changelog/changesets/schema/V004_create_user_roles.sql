-- =====================================================
-- V004: CREATE USER_ROLES JUNCTION TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Many-to-many relationship between users and roles
-- =====================================================

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),

    -- Composite Primary Key
    PRIMARY KEY (user_id, role_id),

    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Comments
COMMENT ON TABLE user_roles IS 'Junction table for user-role assignments';

-- Indexes for JOIN performance
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
