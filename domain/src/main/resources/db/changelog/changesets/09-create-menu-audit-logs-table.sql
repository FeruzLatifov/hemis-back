-- ================================================
-- Migration: V9 - Create Menu Audit Logs Table
-- Description: Audit trail for menu CRUD operations
-- Author: System Architect
-- Date: 2025-01-19
-- ================================================

CREATE TABLE IF NOT EXISTS menu_audit_logs (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Audit Fields
    menu_id UUID,  -- NULL for bulk operations
    action VARCHAR(50) NOT NULL,  -- CREATE, UPDATE, DELETE, REORDER, ACTIVATE, DEACTIVATE, RESTORE
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Change Snapshots (JSONB for flexible schema)
    old_value JSONB,  -- Before state (NULL for CREATE)
    new_value JSONB,  -- After state (NULL for DELETE)

    -- Additional Context
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    notes TEXT,

    -- Constraints
    CONSTRAINT menu_audit_logs_action_check CHECK (action IN (
        'CREATE', 'UPDATE', 'DELETE', 'REORDER',
        'ACTIVATE', 'DEACTIVATE', 'RESTORE',
        'BULK_UPDATE', 'IMPORT_CREATE', 'IMPORT_UPDATE'
    ))
);

-- Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_menu_audit_menu_id ON menu_audit_logs(menu_id);
CREATE INDEX IF NOT EXISTS idx_menu_audit_action ON menu_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_menu_audit_changed_by ON menu_audit_logs(changed_by);
CREATE INDEX IF NOT EXISTS idx_menu_audit_changed_at ON menu_audit_logs(changed_at DESC);

-- JSONB Indexes for Fast Queries
CREATE INDEX IF NOT EXISTS idx_menu_audit_old_value_gin ON menu_audit_logs USING gin(old_value);
CREATE INDEX IF NOT EXISTS idx_menu_audit_new_value_gin ON menu_audit_logs USING gin(new_value);

-- Comments
COMMENT ON TABLE menu_audit_logs IS 'Audit trail for menu structure changes (WHO changed WHAT and WHEN)';
COMMENT ON COLUMN menu_audit_logs.menu_id IS 'Menu ID (NULL for bulk operations)';
COMMENT ON COLUMN menu_audit_logs.action IS 'Action type: CREATE, UPDATE, DELETE, REORDER, ACTIVATE, DEACTIVATE, RESTORE';
COMMENT ON COLUMN menu_audit_logs.changed_by IS 'Username from SecurityContext';
COMMENT ON COLUMN menu_audit_logs.changed_at IS 'Timestamp of change';
COMMENT ON COLUMN menu_audit_logs.old_value IS 'Before snapshot (JSONB) - NULL for CREATE';
COMMENT ON COLUMN menu_audit_logs.new_value IS 'After snapshot (JSONB) - NULL for DELETE';
COMMENT ON COLUMN menu_audit_logs.ip_address IS 'IP address of requester (optional)';
COMMENT ON COLUMN menu_audit_logs.user_agent IS 'User agent string (optional)';
COMMENT ON COLUMN menu_audit_logs.notes IS 'Admin notes or reason for change';
