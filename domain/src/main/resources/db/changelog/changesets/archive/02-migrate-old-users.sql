-- =====================================================
-- V2: MIGRATE OLD-HEMIS USERS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Migrate users from sec_user to users
--
-- Contents:
-- PART 1: Copy users from sec_user → users
-- PART 2: Map sec_role → roles (user_roles)
-- PART 3: Create performance indexes
-- PART 4: Verification
--
-- Strategy: CONDITIONAL MIGRATION
-- - Only runs if sec_user table exists
-- - Gracefully skips if no old-hemis data
-- - Maps legacy roles to new role system
-- =====================================================

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 1: MIGRATE USERS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Step 1: Insert all active users with COMPLETE field mapping
INSERT INTO users (
    -- Primary Key & Authentication
    id,
    username,
    username_lowercase,
    password,
    password_encryption,
    email,

    -- Personal Information
    name,
    first_name,
    last_name,
    middle_name,
    full_name,
    position,

    -- User Settings
    language,
    time_zone,
    time_zone_auto,
    locale,

    -- User Context (Multi-tenancy)
    user_type,
    entity_code,
    university_id,
    phone,

    -- Legacy CUBA Relations
    group_id,
    group_names,

    -- Account Status
    enabled,
    active,
    account_non_locked,

    -- Security Settings
    ip_mask,
    change_password_at_logon,

    -- Multi-tenancy
    sys_tenant_id,
    dtype,

    -- Versioning
    version,

    -- Timestamps
    created_at,
    created_by,
    updated_at,
    updated_by,
    deleted_at,
    deleted_by
)
SELECT
    -- Primary Key & Authentication
    su.id,
    su.login,
    su.login_lc,
    su.password,
    su.password_encryption,
    NULLIF(TRIM(su.email), ''),

    -- Personal Information
    su.name,
    su.first_name,
    su.last_name,
    su.middle_name,
    COALESCE(
        NULLIF(TRIM(su.first_name || ' ' || COALESCE(su.last_name, '')), ''),
        NULLIF(TRIM(su.name), ''),
        su.login
    ) as full_name,
    su.position_,

    -- User Settings
    su.language_,
    su.time_zone,
    su.time_zone_auto,
    su.language_ as locale,  -- computed from language_

    -- User Context (Multi-tenancy)
    CASE
        WHEN su._university IS NOT NULL THEN 'UNIVERSITY'::VARCHAR
        ELSE 'SYSTEM'::VARCHAR
    END as user_type,
    su._university as entity_code,
    su._university as university_id,
    NULL as phone,  -- sec_user doesn't have phone

    -- Legacy CUBA Relations
    su.group_id,
    su.group_names,

    -- Account Status
    COALESCE(su.active, TRUE) as enabled,
    COALESCE(su.active, TRUE) as active,
    TRUE as account_non_locked,

    -- Security Settings
    su.ip_mask,
    su.change_password_at_logon,

    -- Multi-tenancy
    su.sys_tenant_id,
    su.dtype,

    -- Versioning
    COALESCE(su.version, 1),

    -- Timestamps
    COALESCE(su.create_ts, CURRENT_TIMESTAMP),
    su.created_by,
    su.update_ts,
    su.updated_by,
    su.delete_ts,
    su.deleted_by
FROM sec_user su
WHERE su.delete_ts IS NULL
  AND su.active = TRUE
  AND su.password IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM users u WHERE u.username = su.login
  );

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 2: MIGRATE USER ROLES
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Map: Administrators → SUPER_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT
    u.id,
    r.id
FROM users u
JOIN sec_user su ON su.login = u.username AND su.id = u.id
JOIN sec_user_role sur ON sur.user_id = su.id
JOIN sec_role sr ON sr.id = sur.role_id
JOIN roles r ON r.code = 'SUPER_ADMIN'
WHERE sr.name = 'Administrators'
  AND sr.delete_ts IS NULL
  AND sur.delete_ts IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- Map: Ministry, vazirlikrole, Ministry_lite → MINISTRY_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT
    u.id,
    r.id
FROM users u
JOIN sec_user su ON su.login = u.username AND su.id = u.id
JOIN sec_user_role sur ON sur.user_id = su.id
JOIN sec_role sr ON sr.id = sur.role_id
JOIN roles r ON r.code = 'MINISTRY_ADMIN'
WHERE sr.name IN ('Ministry', 'vazirlikrole', 'Ministry_lite')
  AND sr.delete_ts IS NULL
  AND sur.delete_ts IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- Map: OTM → UNIVERSITY_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT
    u.id,
    r.id
FROM users u
JOIN sec_user su ON su.login = u.username AND su.id = u.id
JOIN sec_user_role sur ON sur.user_id = su.id
JOIN sec_role sr ON sr.id = sur.role_id
JOIN roles r ON r.code = 'UNIVERSITY_ADMIN'
WHERE sr.name = 'OTM'
  AND sr.delete_ts IS NULL
  AND sur.delete_ts IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- Map: All other users with unmapped roles → VIEWER (safe default)
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT
    u.id,
    r.id
FROM users u
JOIN sec_user su ON su.login = u.username AND su.id = u.id
JOIN sec_user_role sur ON sur.user_id = su.id
JOIN sec_role sr ON sr.id = sur.role_id
CROSS JOIN roles r
WHERE r.code = 'VIEWER'
  AND sr.name NOT IN ('Administrators', 'Ministry', 'vazirlikrole', 'Ministry_lite', 'OTM')
  AND sr.delete_ts IS NULL
  AND sur.delete_ts IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
  );

-- Safety net: Users without any role → VIEWER
INSERT INTO user_roles (user_id, role_id)
SELECT
    u.id,
    r.id
FROM users u
CROSS JOIN roles r
WHERE r.code = 'VIEWER'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
  );

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 3: PERFORMANCE INDEXES
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE INDEX IF NOT EXISTS idx_users_entity_code_v2
    ON users(entity_code) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_user_type_v2
    ON users(user_type) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_enabled_v2
    ON users(enabled) WHERE deleted_at IS NULL;

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 4: VERIFICATION
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

DO $$
DECLARE
    old_user_count INTEGER := 0;
    new_user_count INTEGER;
    role_mapping_count INTEGER;
    super_admin_count INTEGER;
    ministry_admin_count INTEGER;
    university_admin_count INTEGER;
    viewer_count INTEGER;
BEGIN
    -- Count old users (if table exists)
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'sec_user'
    ) THEN
        SELECT COUNT(*) INTO old_user_count
        FROM sec_user
        WHERE delete_ts IS NULL AND active = TRUE AND password IS NOT NULL;
    END IF;

    -- Count new users
    SELECT COUNT(*) INTO new_user_count
    FROM users
    WHERE deleted_at IS NULL;

    -- Count role mappings
    SELECT COUNT(*) INTO role_mapping_count FROM user_roles;

    -- Count users by role
    SELECT COUNT(DISTINCT ur.user_id) INTO super_admin_count
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE r.code = 'SUPER_ADMIN';

    SELECT COUNT(DISTINCT ur.user_id) INTO ministry_admin_count
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE r.code = 'MINISTRY_ADMIN';

    SELECT COUNT(DISTINCT ur.user_id) INTO university_admin_count
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE r.code = 'UNIVERSITY_ADMIN';

    SELECT COUNT(DISTINCT ur.user_id) INTO viewer_count
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE r.code = 'VIEWER';

    -- Success log
    RAISE NOTICE '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
    RAISE NOTICE '✅ V2: OLD-HEMIS USER MIGRATION COMPLETE';
    RAISE NOTICE '   Old users (sec_user): %', old_user_count;
    RAISE NOTICE '   Migrated users: %', new_user_count;
    RAISE NOTICE '   Role mappings: %', role_mapping_count;
    RAISE NOTICE '   ├─ SUPER_ADMIN: %', super_admin_count;
    RAISE NOTICE '   ├─ MINISTRY_ADMIN: %', ministry_admin_count;
    RAISE NOTICE '   ├─ UNIVERSITY_ADMIN: %', university_admin_count;
    RAISE NOTICE '   └─ VIEWER: %', viewer_count;

    -- Validation
    IF old_user_count > 0 AND new_user_count = 0 THEN
        RAISE WARNING '⚠️  Old users exist but no users migrated!';
    END IF;

    IF new_user_count > 0 AND role_mapping_count = 0 THEN
        RAISE WARNING '⚠️  Users exist but no role mappings!';
    END IF;

    RAISE NOTICE '   Status: READY FOR TRANSLATION DATA (V3)';
    RAISE NOTICE '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
END $$;
