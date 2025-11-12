-- ================================================
-- V3: Migrate Users from OLD-HEMIS to NEW-HEMIS
-- ================================================
-- Purpose: Copy active users from sec_user to users table
-- Strategy: Preserve existing passwords (bcrypt), map roles
-- Safety: Does NOT modify sec_user table
-- ================================================

-- Step 1: Insert users from sec_user (excluding already migrated users like 'admin')
INSERT INTO users (
    id,
    username,
    password,
    email,
    full_name,
    phone,
    user_type,
    entity_code,
    enabled,
    account_non_locked,
    created_at,
    updated_at
)
SELECT
    su.id,                              -- Preserve UUID
    su.login,                           -- username
    su.password,                        -- Already bcrypt encrypted
    NULLIF(TRIM(COALESCE(su.email, '')), ''), -- email (null if empty)
    TRIM(COALESCE(su.first_name || ' ' || su.last_name, su.name, su.login)), -- full_name
    NULL,                               -- phone (not in old schema)
    CASE
        WHEN su._university IS NOT NULL THEN 'UNIVERSITY'
        ELSE 'SYSTEM'
    END,                                -- user_type
    su._university,                     -- entity_code (university code)
    COALESCE(su.active, true),         -- enabled
    true,                               -- account_non_locked (default)
    COALESCE(su.create_ts, CURRENT_TIMESTAMP), -- created_at
    COALESCE(su.update_ts, CURRENT_TIMESTAMP)  -- updated_at
FROM sec_user su
WHERE su.delete_ts IS NULL              -- Only active users
  AND su.active = true                  -- Only enabled users
  AND su.login != 'admin'               -- Skip admin (already exists)
  AND su.password IS NOT NULL           -- Only users with passwords
  AND NOT EXISTS (
      SELECT 1 FROM users u WHERE u.username = su.login
  )                                     -- Avoid duplicates
ON CONFLICT (username) DO NOTHING;       -- Safety: skip if exists

-- Step 2: Role Mapping Strategy
-- OLD-HEMIS roles → NEW-HEMIS roles mapping
-- Administrators (role_type=10) → SUPER_ADMIN
-- OTM, Ministry → UNIVERSITY_ADMIN
-- Others → VIEWER (safe default)

-- Get role IDs for mapping
DO $$
DECLARE
    super_admin_role_id UUID;
    ministry_admin_role_id UUID;
    university_admin_role_id UUID;
    viewer_role_id UUID;
BEGIN
    -- Fetch new role IDs
    SELECT id INTO super_admin_role_id FROM roles WHERE code = 'SUPER_ADMIN';
    SELECT id INTO ministry_admin_role_id FROM roles WHERE code = 'MINISTRY_ADMIN';
    SELECT id INTO university_admin_role_id FROM roles WHERE code = 'UNIVERSITY_ADMIN';
    SELECT id INTO viewer_role_id FROM roles WHERE code = 'VIEWER';

    -- Migrate user-role mappings
    -- Map: Administrators → SUPER_ADMIN
    INSERT INTO user_roles (user_id, role_id)
    SELECT DISTINCT u.id, super_admin_role_id
    FROM users u
    INNER JOIN sec_user su ON su.login = u.username
    INNER JOIN sec_user_role sur ON sur.user_id = su.id
    INNER JOIN sec_role sr ON sr.id = sur.role_id
    WHERE sr.name = 'Administrators'
      AND sr.delete_ts IS NULL
      AND sur.delete_ts IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM user_roles ur
          WHERE ur.user_id = u.id AND ur.role_id = super_admin_role_id
      )
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Map: Ministry, vazirlikrole → MINISTRY_ADMIN
    INSERT INTO user_roles (user_id, role_id)
    SELECT DISTINCT u.id, ministry_admin_role_id
    FROM users u
    INNER JOIN sec_user su ON su.login = u.username
    INNER JOIN sec_user_role sur ON sur.user_id = su.id
    INNER JOIN sec_role sr ON sr.id = sur.role_id
    WHERE sr.name IN ('Ministry', 'vazirlikrole', 'Ministry_lite')
      AND sr.delete_ts IS NULL
      AND sur.delete_ts IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM user_roles ur
          WHERE ur.user_id = u.id AND ur.role_id = ministry_admin_role_id
      )
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Map: OTM → UNIVERSITY_ADMIN
    INSERT INTO user_roles (user_id, role_id)
    SELECT DISTINCT u.id, university_admin_role_id
    FROM users u
    INNER JOIN sec_user su ON su.login = u.username
    INNER JOIN sec_user_role sur ON sur.user_id = su.id
    INNER JOIN sec_role sr ON sr.id = sur.role_id
    WHERE sr.name = 'OTM'
      AND sr.delete_ts IS NULL
      AND sur.delete_ts IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM user_roles ur
          WHERE ur.user_id = u.id AND ur.role_id = university_admin_role_id
      )
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Map: All other roles → VIEWER (safe default)
    INSERT INTO user_roles (user_id, role_id)
    SELECT DISTINCT u.id, viewer_role_id
    FROM users u
    INNER JOIN sec_user su ON su.login = u.username
    INNER JOIN sec_user_role sur ON sur.user_id = su.id
    INNER JOIN sec_role sr ON sr.id = sur.role_id
    WHERE sr.name NOT IN ('Administrators', 'Ministry', 'vazirlikrole', 'Ministry_lite', 'OTM')
      AND sr.delete_ts IS NULL
      AND sur.delete_ts IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
      )  -- Only if user has no role yet
    ON CONFLICT (user_id, role_id) DO NOTHING;

    -- Assign VIEWER role to users without any role (safety net)
    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, viewer_role_id
    FROM users u
    WHERE NOT EXISTS (
        SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
    )
    AND u.username != 'admin'  -- Skip admin
    ON CONFLICT (user_id, role_id) DO NOTHING;

END $$;

-- Step 3: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_entity_code ON users(entity_code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_user_type ON users(user_type) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users(enabled) WHERE deleted_at IS NULL;

-- Step 4: Verification - Print migration statistics
DO $$
DECLARE
    old_user_count INTEGER;
    new_user_count INTEGER;
    migrated_count INTEGER;
    role_mapping_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO old_user_count FROM sec_user WHERE delete_ts IS NULL AND active = true;
    SELECT COUNT(*) INTO new_user_count FROM users WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO migrated_count FROM users WHERE deleted_at IS NULL AND username != 'admin';
    SELECT COUNT(*) INTO role_mapping_count FROM user_roles;

    RAISE NOTICE '========================================';
    RAISE NOTICE 'USER MIGRATION COMPLETED';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Old active users (sec_user): %', old_user_count;
    RAISE NOTICE 'New users (users): %', new_user_count;
    RAISE NOTICE 'Migrated users: %', migrated_count;
    RAISE NOTICE 'User-role mappings: %', role_mapping_count;
    RAISE NOTICE '========================================';
END $$;
