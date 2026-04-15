-- =====================================================
-- V008: COMPLETE MENU SYSTEM
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Database-driven hierarchical menu system
--
-- Contents:
-- PART 1: Menu table schema
-- PART 2: Seed complete menu structure
-- PART 3: Verification
--
-- Note: user_favorites (V008b) extracted to separate file for granularity (1 file = 1 table)
--
-- Strategy: MENU DEPLOYMENT UNIT
-- - Table schema + seed data together (logical unit)
-- - Hierarchical structure with parent-child
-- - Permission-based access control
-- - i18n support via i18n_key
-- =====================================================

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 1: MENU TABLE SCHEMA
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- =====================================================
-- IDEMPOTENT: CREATE TABLE IF NOT EXISTS
-- =====================================================

CREATE TABLE IF NOT EXISTS menus (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Menu Identity
    code VARCHAR(100) NOT NULL UNIQUE,          -- Unique menu code (e.g., "dashboard", "registry-e-reestr")

    -- Display Information (i18n support)
    i18n_key VARCHAR(200) NOT NULL,             -- Translation key (e.g., "Dashboard")

    -- Navigation
    url VARCHAR(500),                            -- Route URL (e.g., "/dashboard", null for parent menus)
    icon VARCHAR(100),                           -- Icon name (e.g., "home", "database")

    -- Security
    permission VARCHAR(200),                     -- Required permission (e.g., "dashboard.view", null = public)

    -- Hierarchical Structure
    parent_id UUID,                              -- Self-reference for tree structure (NULL = root)

    -- Ordering
    order_number INTEGER NOT NULL DEFAULT 0,    -- Display order within same parent

    -- Classification
    menu_type VARCHAR(20) NOT NULL DEFAULT 'main', -- Menu type: 'main' or 'system'

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,       -- Enable/disable menu item

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,                        -- Soft delete timestamp
    deleted_by VARCHAR(255),                     -- Who soft-deleted
    version INTEGER DEFAULT 1,                  -- Optimistic locking (JPA @Version)

    -- Constraints
    CONSTRAINT fk_menus_parent FOREIGN KEY (parent_id) REFERENCES menus(id) ON DELETE CASCADE,
    CONSTRAINT chk_menus_code_not_empty CHECK (code <> ''),
    CONSTRAINT chk_menus_i18n_key_not_empty CHECK (i18n_key <> ''),
    CONSTRAINT chk_menus_menu_type CHECK (menu_type IN ('main', 'system'))
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Index for hierarchical queries (parent → children)
CREATE INDEX IF NOT EXISTS idx_menus_parent_id ON menus(parent_id) WHERE parent_id IS NOT NULL;

-- Index for active menus filtering
CREATE INDEX IF NOT EXISTS idx_menus_is_active ON menus(is_active) WHERE is_active = true;

-- Index for ordering
CREATE INDEX IF NOT EXISTS idx_menus_order ON menus(parent_id, order_number);

-- Index for soft delete filtering
CREATE INDEX IF NOT EXISTS idx_menus_deleted_at ON menus(deleted_at) WHERE deleted_at IS NULL;

-- Index for permission-based queries
CREATE INDEX IF NOT EXISTS idx_menus_permission ON menus(permission) WHERE permission IS NOT NULL;

-- Covering index: active children by parent, ordered (most common query)
CREATE INDEX IF NOT EXISTS idx_menus_parent_order_active ON menus(parent_id, order_number) WHERE deleted_at IS NULL AND is_active = true;

-- Index for menu type filtering (system vs main)
CREATE INDEX IF NOT EXISTS idx_menus_type_order ON menus(menu_type, order_number) WHERE deleted_at IS NULL AND is_active = true;

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

COMMENT ON TABLE menus IS 'Dynamic menu structure with hierarchical support (Database-driven menu system)';
COMMENT ON COLUMN menus.code IS 'Unique menu identifier (e.g., dashboard, registry-e-reestr)';
COMMENT ON COLUMN menus.i18n_key IS 'Translation key for i18n (e.g., Dashboard)';
COMMENT ON COLUMN menus.url IS 'Navigation URL (null for parent menus with children)';
COMMENT ON COLUMN menus.icon IS 'Icon name for UI rendering (e.g., home, database, users)';
COMMENT ON COLUMN menus.permission IS 'Required permission code (null = public access)';
COMMENT ON COLUMN menus.parent_id IS 'Parent menu ID for hierarchical structure (null = root level)';
COMMENT ON COLUMN menus.order_number IS 'Display order within same parent (lower = first)';
COMMENT ON COLUMN menus.menu_type IS 'Menu type: main (regular) or system (shown below separator)';
COMMENT ON COLUMN menus.is_active IS 'Menu item active status (false = hidden from users)';
COMMENT ON COLUMN menus.deleted_at IS 'Soft delete timestamp (null = not deleted)';
COMMENT ON COLUMN menus.deleted_by IS 'Who performed the soft delete';
COMMENT ON COLUMN menus.version IS 'Optimistic locking version (JPA @Version)';

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

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 2: SEED MENU STRUCTURE (49 items)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- =====================================================
-- ROOT MENUS (9 items)
-- =====================================================

-- dashboard
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000001',
    'dashboard',
    'Dashboard',
    '/dashboard',
    'home',
    NULL,
    1,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- institutions
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000002',
    'institutions',
    'Institutions',
    NULL,
    'building',
    'institutions.view',
    2,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- students
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000003',
    'students',
    'Students',
    NULL,
    'graduation-cap',
    'students.view',
    3,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- teachers
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000004',
    'teachers',
    'Teachers',
    NULL,
    'user-check',
    'teachers.view',
    4,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- science
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000005',
    'science',
    'Science',
    NULL,
    'flask',
    'science.view',
    5,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- reports
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000006',
    'reports',
    'Reports',
    NULL,
    'bar-chart',
    'reports.view',
    6,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- rating
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000007',
    'rating',
    'Rating',
    NULL,
    'line-chart',
    'rating.view',
    7,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- classifiers
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000008',
    'classifiers',
    'Classifiers',
    NULL,
    'database',
    'classifiers.view',
    8,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- system (menu_type = 'system' — shown below separator in sidebar)
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, menu_type, parent_id, created_at, updated_at)
VALUES (
    '10000000-0000-0000-0000-000000000009',
    'system',
    'System',
    NULL,
    'settings',
    'system.view',
    9,
    true,
    'system',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    menu_type = EXCLUDED.menu_type,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- CHILDREN OF: institutions (10000000-0000-0000-0000-000000000002)
-- =====================================================

-- inst-universities
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000002-0000-0000-0000-000000000001',
    'inst-universities',
    'Universities',
    '/institutions/universities',
    'building',
    'institutions.universities.view',
    1,
    true,
    '10000000-0000-0000-0000-000000000002',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- inst-faculties
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000002-0000-0000-0000-000000000002',
    'inst-faculties',
    'Faculties',
    '/institutions/faculties',
    'school',
    'institutions.faculties.view',
    2,
    true,
    '10000000-0000-0000-0000-000000000002',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- inst-departments
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000002-0000-0000-0000-000000000003',
    'inst-departments',
    'Departments',
    '/institutions/departments',
    'users',
    'institutions.departments.view',
    3,
    true,
    '10000000-0000-0000-0000-000000000002',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- inst-attached-specialities
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000002-0000-0000-0000-000000000004',
    'inst-attached-specialities',
    'University specialities',
    '/institutions/attached-specialities',
    'layers',
    'institutions.attached-specialities.view',
    4,
    true,
    '10000000-0000-0000-0000-000000000002',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- CHILDREN OF: students (10000000-0000-0000-0000-000000000003)
-- =====================================================

-- student-list
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000003-0000-0000-0000-000000000001',
    'student-list',
    'Student data',
    '/students',
    'list',
    'students.list.view',
    1,
    true,
    '10000000-0000-0000-0000-000000000003',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- student-directions
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000003-0000-0000-0000-000000000002',
    'student-directions',
    'Directions',
    '/students/directions',
    'book-open',
    'students.directions.view',
    2,
    true,
    '10000000-0000-0000-0000-000000000003',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- student-groups
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000003-0000-0000-0000-000000000003',
    'student-groups',
    'Study groups',
    '/students/groups',
    'users',
    'students.groups.view',
    3,
    true,
    '10000000-0000-0000-0000-000000000003',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- student-diplomas
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000003-0000-0000-0000-000000000004',
    'student-diplomas',
    'Diplomas',
    '/students/diplomas',
    'award',
    'students.diplomas.view',
    4,
    true,
    '10000000-0000-0000-0000-000000000003',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- student-scholarships
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000003-0000-0000-0000-000000000005',
    'student-scholarships',
    'Scholarship',
    '/students/scholarships',
    'dollar-sign',
    'students.scholarships.view',
    5,
    true,
    '10000000-0000-0000-0000-000000000003',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- student-certificates
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000003-0000-0000-0000-000000000006',
    'student-certificates',
    'Student certificates',
    '/students/certificates',
    'file-text',
    'students.certificates.view',
    6,
    true,
    '10000000-0000-0000-0000-000000000003',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- CHILDREN OF: teachers (10000000-0000-0000-0000-000000000004)
-- =====================================================

-- teacher-list
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000004-0000-0000-0000-000000000001',
    'teacher-list',
    'Teacher list',
    '/teachers',
    'list',
    'teachers.list.view',
    1,
    true,
    '10000000-0000-0000-0000-000000000004',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- teacher-positions
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000004-0000-0000-0000-000000000002',
    'teacher-positions',
    'Positions',
    '/teachers/positions',
    'briefcase',
    'teachers.positions.view',
    2,
    true,
    '10000000-0000-0000-0000-000000000004',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- teacher-qualifications
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000004-0000-0000-0000-000000000003',
    'teacher-qualifications',
    'Teacher qualifications',
    '/teachers/qualifications',
    'award',
    'teachers.qualifications.view',
    3,
    true,
    '10000000-0000-0000-0000-000000000004',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- CHILDREN OF: science (10000000-0000-0000-0000-000000000005)
-- =====================================================

-- sci-researchers
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000005-0000-0000-0000-000000000001',
    'sci-researchers',
    'Researchers',
    '/science/researchers',
    'user-check',
    'science.researchers.view',
    1,
    true,
    '10000000-0000-0000-0000-000000000005',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- sci-projects
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000005-0000-0000-0000-000000000002',
    'sci-projects',
    'Scientific projects',
    '/science/projects',
    'lightbulb',
    'science.projects.view',
    2,
    true,
    '10000000-0000-0000-0000-000000000005',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- sci-publications
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000005-0000-0000-0000-000000000003',
    'sci-publications',
    'Scientific publications',
    '/science/publications',
    'book',
    'science.publications.view',
    3,
    true,
    '10000000-0000-0000-0000-000000000005',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- sci-methodical
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000005-0000-0000-0000-000000000004',
    'sci-methodical',
    'Methodical publications',
    '/science/methodical',
    'book-open',
    'science.methodical.view',
    4,
    true,
    '10000000-0000-0000-0000-000000000005',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- sci-intellectual
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000005-0000-0000-0000-000000000005',
    'sci-intellectual',
    'Intellectual property',
    '/science/intellectual',
    'briefcase',
    'science.intellectual.view',
    5,
    true,
    '10000000-0000-0000-0000-000000000005',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- CHILDREN OF: reports (10000000-0000-0000-0000-000000000006)
-- =====================================================

-- reports-students
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000006-0000-0000-0000-000000000001',
    'reports-students',
    'Student reports',
    '/reports/students',
    'graduation-cap',
    'reports.students.view',
    1,
    true,
    '10000000-0000-0000-0000-000000000006',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- reports-teachers
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000006-0000-0000-0000-000000000002',
    'reports-teachers',
    'Teacher reports',
    '/reports/teachers',
    'users',
    'reports.teachers.view',
    2,
    true,
    '10000000-0000-0000-0000-000000000006',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- reports-institutions
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000006-0000-0000-0000-000000000003',
    'reports-institutions',
    'University reports',
    '/reports/institutions',
    'building',
    'reports.institutions.view',
    3,
    true,
    '10000000-0000-0000-0000-000000000006',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- reports-academic
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000006-0000-0000-0000-000000000004',
    'reports-academic',
    'Academic reports',
    '/reports/academic',
    'book',
    'reports.academic.view',
    4,
    true,
    '10000000-0000-0000-0000-000000000006',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- reports-research
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000006-0000-0000-0000-000000000005',
    'reports-research',
    'Research reports',
    '/reports/research',
    'flask',
    'reports.research.view',
    5,
    true,
    '10000000-0000-0000-0000-000000000006',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- reports-economic
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000006-0000-0000-0000-000000000006',
    'reports-economic',
    'Economic reports',
    '/reports/economic',
    'dollar-sign',
    'reports.economic.view',
    6,
    true,
    '10000000-0000-0000-0000-000000000006',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- CHILDREN OF: rating (10000000-0000-0000-0000-000000000007)
-- =====================================================

-- rating-administrative
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000007-0000-0000-0000-000000000001',
    'rating-administrative',
    'Administrative rating',
    '/rating/administrative',
    'building',
    'rating.administrative.view',
    1,
    true,
    '10000000-0000-0000-0000-000000000007',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- rating-academic
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000007-0000-0000-0000-000000000002',
    'rating-academic',
    'Academic rating',
    '/rating/academic',
    'layout-dashboard',
    'rating.academic.view',
    2,
    true,
    '10000000-0000-0000-0000-000000000007',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- rating-scientific
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000007-0000-0000-0000-000000000003',
    'rating-scientific',
    'Scientific rating',
    '/rating/scientific',
    'graduation-cap',
    'rating.scientific.view',
    3,
    true,
    '10000000-0000-0000-0000-000000000007',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- rating-gpa
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000007-0000-0000-0000-000000000004',
    'rating-gpa',
    'Student GPA',
    '/rating/gpa',
    'award',
    'rating.gpa.view',
    4,
    true,
    '10000000-0000-0000-0000-000000000007',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- CHILDREN OF: classifiers (10000000-0000-0000-0000-000000000008)
-- =====================================================

-- cls-general
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000001',
    'cls-general',
    'General data',
    '/classifiers/general',
    'file-text',
    'classifiers.general.view',
    1,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-structure
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000002',
    'cls-structure',
    'Structure',
    '/classifiers/structure',
    'share-2',
    'classifiers.structure.view',
    2,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-employee
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000003',
    'cls-employee',
    'Employees',
    '/classifiers/employee',
    'users',
    'classifiers.employee.view',
    3,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-student
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000004',
    'cls-student',
    'Student classifiers',
    '/classifiers/student',
    'user-circle',
    'classifiers.student.view',
    4,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-education
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000005',
    'cls-education',
    'Education',
    '/classifiers/education',
    'book',
    'classifiers.education.view',
    5,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-study
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000006',
    'cls-study',
    'Study process',
    '/classifiers/study',
    'edit',
    'classifiers.study.view',
    6,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-science
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000007',
    'cls-science',
    'Science classifiers',
    '/classifiers/science',
    'graduation-cap',
    'classifiers.science.view',
    7,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- cls-organizational
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000008-0000-0000-0000-000000000008',
    'cls-organizational',
    'Organizational',
    '/classifiers/organizational',
    'building-2',
    'classifiers.organizational.view',
    8,
    true,
    '10000000-0000-0000-0000-000000000008',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- CHILDREN OF: system (10000000-0000-0000-0000-000000000009)
-- =====================================================

-- sys-translations
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000009-0000-0000-0000-000000000001',
    'sys-translations',
    'Translations',
    '/system/translations',
    'languages',
    'system.translation.view',
    1,
    true,
    '10000000-0000-0000-0000-000000000009',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- sys-users
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000009-0000-0000-0000-000000000002',
    'sys-users',
    'Users',
    '/system/users',
    'users',
    'system.users.view',
    2,
    true,
    '10000000-0000-0000-0000-000000000009',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- sys-roles
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000009-0000-0000-0000-000000000003',
    'sys-roles',
    'Roles',
    '/system/roles',
    'shield',
    'roles.manage',
    3,
    true,
    '10000000-0000-0000-0000-000000000009',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- sys-logs
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000009-0000-0000-0000-000000000004',
    'sys-logs',
    'Audit Logs',
    '/system/logs',
    'scroll-text',
    'audit.view',
    4,
    true,
    '10000000-0000-0000-0000-000000000009',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- sys-report-updates
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000009-0000-0000-0000-000000000005',
    'sys-report-updates',
    'Report updates',
    '/system/report-updates',
    'refresh-cw',
    'system.report-update.view',
    5,
    true,
    '10000000-0000-0000-0000-000000000009',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 3: VERIFICATION
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

DO $$
DECLARE
    menu_count INTEGER;
    root_menu_count INTEGER;
BEGIN
    -- Count menus
    SELECT COUNT(*) INTO menu_count FROM menus WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO root_menu_count FROM menus WHERE parent_id IS NULL AND deleted_at IS NULL;

    -- Success log
    RAISE NOTICE '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
    RAISE NOTICE 'V008: MENU SYSTEM DEPLOYED';
    RAISE NOTICE '   Total menus: %', menu_count;
    RAISE NOTICE '   Root menus: %', root_menu_count;
    RAISE NOTICE '   Child menus: %', menu_count - root_menu_count;

    -- Validation
    IF menu_count < 10 THEN
        RAISE WARNING 'Expected at least 10 menus, found %', menu_count;
    END IF;

    RAISE NOTICE '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
END $$;

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- USER FAVORITES (sevimli menular)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TABLE IF NOT EXISTS user_favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    menu_code VARCHAR(100) NOT NULL,
    order_number INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_favorites_menu FOREIGN KEY (menu_code) REFERENCES menus(code) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_user_favorites UNIQUE (user_id, menu_code)
);

CREATE INDEX IF NOT EXISTS idx_user_favorites_user ON user_favorites(user_id);
