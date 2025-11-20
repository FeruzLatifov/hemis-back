-- =====================================================
-- MENU MANAGEMENT - DATABASE-DRIVEN MENU STRUCTURE
-- =====================================================
-- Migration: 07 - Create menus table
-- Author: System
-- Date: 2025-11-16
-- Description: Creates hierarchical menu structure table for dynamic menu management
--              (Google Cloud / AWS / Netflix best practice - Database-driven)
-- =====================================================

-- =====================================================
-- IDEMPOTENT: CREATE TABLE IF NOT EXISTS
-- =====================================================

CREATE TABLE IF NOT EXISTS menus (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Menu Identity
    code VARCHAR(100) NOT NULL UNIQUE,          -- Unique menu code (e.g., "dashboard", "registry-e-reestr")

    -- Display Information (i18n support)
    i18n_key VARCHAR(200) NOT NULL,             -- Translation key (e.g., "menu.dashboard")

    -- Navigation
    url VARCHAR(500),                            -- Route URL (e.g., "/dashboard", null for parent menus)
    icon VARCHAR(100),                           -- Icon name (e.g., "home", "database")

    -- Security
    permission VARCHAR(200),                     -- Required permission (e.g., "dashboard.view", null = public)

    -- Hierarchical Structure
    parent_id UUID,                              -- Self-reference for tree structure (NULL = root)

    -- Ordering
    order_number INTEGER NOT NULL DEFAULT 0,    -- Display order within same parent

    -- Status
    active BOOLEAN NOT NULL DEFAULT true,       -- Enable/disable menu item

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,                        -- Soft delete timestamp

    -- Constraints
    CONSTRAINT fk_menus_parent FOREIGN KEY (parent_id) REFERENCES menus(id) ON DELETE CASCADE,
    CONSTRAINT chk_menus_code_not_empty CHECK (code <> ''),
    CONSTRAINT chk_menus_i18n_key_not_empty CHECK (i18n_key <> '')
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Index for hierarchical queries (parent → children)
CREATE INDEX IF NOT EXISTS idx_menus_parent_id ON menus(parent_id) WHERE parent_id IS NOT NULL;

-- Index for active menus filtering
CREATE INDEX IF NOT EXISTS idx_menus_active ON menus(active) WHERE active = true;

-- Index for ordering
CREATE INDEX IF NOT EXISTS idx_menus_order ON menus(parent_id, order_number);

-- Index for soft delete filtering
CREATE INDEX IF NOT EXISTS idx_menus_deleted_at ON menus(deleted_at) WHERE deleted_at IS NULL;

-- Index for permission-based queries
CREATE INDEX IF NOT EXISTS idx_menus_permission ON menus(permission) WHERE permission IS NOT NULL;

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

COMMENT ON TABLE menus IS 'Dynamic menu structure with hierarchical support (Database-driven menu system)';
COMMENT ON COLUMN menus.code IS 'Unique menu identifier (e.g., dashboard, registry-e-reestr)';
COMMENT ON COLUMN menus.i18n_key IS 'Translation key for i18n (e.g., menu.dashboard)';
COMMENT ON COLUMN menus.url IS 'Navigation URL (null for parent menus with children)';
COMMENT ON COLUMN menus.icon IS 'Icon name for UI rendering (e.g., home, database, users)';
COMMENT ON COLUMN menus.permission IS 'Required permission code (null = public access)';
COMMENT ON COLUMN menus.parent_id IS 'Parent menu ID for hierarchical structure (null = root level)';
COMMENT ON COLUMN menus.order_number IS 'Display order within same parent (lower = first)';
COMMENT ON COLUMN menus.active IS 'Menu item active status (false = hidden from users)';
COMMENT ON COLUMN menus.deleted_at IS 'Soft delete timestamp (null = not deleted)';

-- =====================================================
-- AUDIT TRIGGER FOR updated_at
-- =====================================================

CREATE OR REPLACE FUNCTION update_menus_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_menus_updated_at ON menus;
CREATE TRIGGER trigger_menus_updated_at
    BEFORE UPDATE ON menus
    FOR EACH ROW
    EXECUTE FUNCTION update_menus_updated_at();
