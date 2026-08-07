-- V013: menu + user_favorite schema (DDL only)
-- Seed data S011_seed_main_menus.sql da.

-- =====================================================
-- PART 1: MENU TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS menu (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(100) NOT NULL UNIQUE,
    i18n_key VARCHAR(200) NOT NULL,
    url VARCHAR(500),
    icon VARCHAR(100),
    permission VARCHAR(200),
    parent_id UUID,
    order_number INTEGER NOT NULL DEFAULT 0,
    menu_type VARCHAR(20) NOT NULL DEFAULT 'main',
    is_active BOOLEAN NOT NULL DEFAULT true,

    version INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES menu(id) ON DELETE CASCADE,
    CONSTRAINT chk_menu_code_not_empty CHECK (code <> ''),
    CONSTRAINT chk_menu_i18n_key_not_empty CHECK (i18n_key <> ''),
    CONSTRAINT chk_menu_menu_type CHECK (menu_type IN ('main', 'system'))
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_menu_parent_id ON menu(parent_id) WHERE parent_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_menu_is_active ON menu(is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_menu_order ON menu(parent_id, order_number);
CREATE INDEX IF NOT EXISTS idx_menu_deleted_at ON menu(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_menu_permission ON menu(permission) WHERE permission IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_menu_parent_order_active ON menu(parent_id, order_number) WHERE deleted_at IS NULL AND is_active = true;
CREATE INDEX IF NOT EXISTS idx_menu_type_order ON menu(menu_type, order_number) WHERE deleted_at IS NULL AND is_active = true;

-- Audit trigger for updated_at
CREATE OR REPLACE FUNCTION update_menu_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_menu_updated_at ON menu;
CREATE TRIGGER trigger_menu_updated_at
    BEFORE UPDATE ON menu
    FOR EACH ROW
    EXECUTE FUNCTION update_menu_updated_at();

-- =====================================================
-- PART 2: upsert_menu() — idempotent helper for seed migrations
-- =====================================================
-- S011_seed_main_menus.sql va boshqa menu seed'lar (S012, S013)
-- shu funksiyani ishlatadi. ON CONFLICT (code) DO UPDATE strategiya:
-- yangi qiymatlar ustun (i18n_key, url, icon, permission, order, parent, menu_type).

CREATE OR REPLACE FUNCTION upsert_menu(
    p_id            UUID,
    p_code          VARCHAR,
    p_i18n_key      VARCHAR,
    p_url           VARCHAR,
    p_icon          VARCHAR,
    p_permission    VARCHAR,
    p_order_number  INTEGER,
    p_parent_id     UUID,
    p_menu_type     VARCHAR DEFAULT 'main'
) RETURNS VOID AS $$
BEGIN
    INSERT INTO menu (
        id, code, i18n_key, url, icon, permission,
        order_number, is_active, menu_type, parent_id,
        created_at, updated_at
    ) VALUES (
        p_id, p_code, p_i18n_key, p_url, p_icon, p_permission,
        p_order_number, true, p_menu_type, p_parent_id,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    )
    ON CONFLICT (code) DO UPDATE SET
        i18n_key     = EXCLUDED.i18n_key,
        url          = EXCLUDED.url,
        icon         = EXCLUDED.icon,
        permission   = EXCLUDED.permission,
        order_number = EXCLUDED.order_number,
        menu_type    = EXCLUDED.menu_type,
        parent_id    = EXCLUDED.parent_id,
        updated_at   = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- PART 3: USER FAVORITES
-- =====================================================

CREATE TABLE IF NOT EXISTS user_favorite (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    menu_code VARCHAR(100) NOT NULL,
    order_number INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_favorite_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_favorite_menu FOREIGN KEY (menu_code) REFERENCES menu(code) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_user_favorite UNIQUE (user_id, menu_code)
);

CREATE INDEX IF NOT EXISTS idx_user_favorite_user ON user_favorite(user_id);
