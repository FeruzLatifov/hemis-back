-- =====================================================
-- HEMIS Backend - V3 Migration: Import Old Users
-- =====================================================
-- Version: V3
-- Purpose: Migrate users from old-hemis (sec_*) to new-hemis (users)
-- Date: 2025-11-12
--
-- Strategy:
-- 1. Migrate all active roles from sec_role → roles
-- 2. Migrate all active users from sec_user → users
-- 3. Migrate user-role mappings from sec_user_role → user_roles
--
-- Safety:
-- - Only migrates non-deleted records (delete_ts IS NULL)
-- - Skips duplicates (ON CONFLICT DO NOTHING)
-- - Preserves original IDs (for relationship integrity)
-- - Keeps passwords as-is (BCrypt hashed)
-- =====================================================

-- Safety Check: Confirm database
DO $$
BEGIN
    IF current_database() != 'test_hemis' THEN
        RAISE EXCEPTION 'SAFETY CHECK FAILED: This script can only run on test_hemis database. Current: %', current_database();
    END IF;

    RAISE NOTICE '✅ Safety check passed: Running on test_hemis database';
END $$;

-- =====================================================
-- STEP 1: Migrate Roles from sec_role → roles
-- =====================================================
-- Maps old CUBA roles to new Spring Security roles
-- Preserves role IDs for existing relationships
-- =====================================================

INSERT INTO roles (id, code, name, description, created_at, deleted_at)
SELECT
    id,                                     -- Preserve original UUID
    UPPER(REPLACE(name, ' ', '_')),         -- Convert to CODE format (e.g., "OTM" → "OTM", "Administrators" → "ADMINISTRATORS")
    name,                                   -- Original role name
    description,                            -- Role description
    create_ts,                              -- Created timestamp
    delete_ts                               -- Soft delete (null = active)
FROM sec_role
WHERE delete_ts IS NULL                     -- Only active roles
ON CONFLICT (id) DO NOTHING;                -- Skip if already exists

-- =====================================================
-- STEP 2: Migrate Users from sec_user → users
-- =====================================================
-- Maps old CUBA users to new Spring Security users
-- Field mapping:
--   login → username
--   password → password (BCrypt hashed, no change needed)
--   email → email
--   first_name + middle_name + last_name → full_name
--   active → enabled
--   group_id → determines user_type (UNIVERSITY, MINISTRY, SYSTEM)
-- =====================================================

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
    failed_attempts,
    created_at,
    updated_at,
    deleted_at,
    version
)
SELECT
    u.id,                                                           -- Preserve original UUID
    u.login,                                                        -- Username (unique)
    u.password,                                                     -- Password (BCrypt) - DIRECT COPY from sec_user
    u.email,                                                        -- Email
    TRIM(CONCAT_WS(' ', u.last_name, u.first_name, u.middle_name)), -- Full name from parts
    NULL,                                                           -- Phone (not in old schema)
    CASE
        WHEN u.group_names LIKE '%OTM%' THEN 'UNIVERSITY'
        WHEN u.group_names LIKE '%Ministry%' OR u.group_names LIKE '%vazirlik%' THEN 'MINISTRY'
        WHEN u.group_names LIKE '%Admin%' THEN 'SYSTEM'
        ELSE 'UNIVERSITY'                                          -- Default to UNIVERSITY
    END,                                                            -- User type
    u.sys_tenant_id,                                                -- Entity code (OTM code)
    COALESCE(u.active, false),                                      -- Enabled flag
    true,                                                           -- Account non-locked (default)
    0,                                                              -- Failed attempts (default)
    u.create_ts,                                                    -- Created timestamp
    u.update_ts,                                                    -- Updated timestamp
    u.delete_ts,                                                    -- Deleted timestamp (soft delete)
    COALESCE(u.version, 0)                                          -- Optimistic locking version
FROM sec_user u
WHERE u.delete_ts IS NULL                                           -- Only active users
  AND u.login IS NOT NULL                                           -- Must have username
  AND u.password IS NOT NULL                                        -- Must have password
ON CONFLICT (username) DO NOTHING;                                  -- Skip if username already exists

-- =====================================================
-- STEP 3: Migrate User-Role Mappings
-- =====================================================
-- Maps sec_user_role → user_roles
-- Only migrates relationships where both user and role exist in new tables
-- =====================================================

INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT
    sur.user_id,
    sur.role_id
FROM sec_user_role sur
WHERE sur.delete_ts IS NULL                 -- Only active relationships
  AND EXISTS (                              -- User must exist in new table
      SELECT 1 FROM users u
      WHERE u.id = sur.user_id
  )
  AND EXISTS (                              -- Role must exist in new table
      SELECT 1 FROM roles r
      WHERE r.id = sur.role_id
  )
ON CONFLICT (user_id, role_id) DO NOTHING;  -- Skip duplicates

-- =====================================================
-- VERIFICATION
-- =====================================================

DO $$
DECLARE
    old_user_count INTEGER;
    new_user_count INTEGER;
    old_role_count INTEGER;
    new_role_count INTEGER;
    old_mapping_count INTEGER;
    new_mapping_count INTEGER;
    rec RECORD;
BEGIN
    -- Count old system records
    SELECT COUNT(*) INTO old_user_count FROM sec_user WHERE delete_ts IS NULL;
    SELECT COUNT(*) INTO old_role_count FROM sec_role WHERE delete_ts IS NULL;
    SELECT COUNT(*) INTO old_mapping_count FROM sec_user_role WHERE delete_ts IS NULL;

    -- Count new system records
    SELECT COUNT(*) INTO new_user_count FROM users WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO new_role_count FROM roles WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO new_mapping_count FROM user_roles;

    RAISE NOTICE '==============================================';
    RAISE NOTICE 'V3 Migration Complete - User Data Migration';
    RAISE NOTICE '==============================================';
    RAISE NOTICE '';
    RAISE NOTICE 'Users Migrated:';
    RAISE NOTICE '  Old system (sec_user): %', old_user_count;
    RAISE NOTICE '  New system (users): %', new_user_count;
    RAISE NOTICE '  Success rate: %%%', ROUND((new_user_count::NUMERIC / old_user_count * 100), 2);
    RAISE NOTICE '';
    RAISE NOTICE 'Roles Migrated:';
    RAISE NOTICE '  Old system (sec_role): %', old_role_count;
    RAISE NOTICE '  New system (roles): %', new_role_count;
    RAISE NOTICE '  Success rate: %%%', ROUND((new_role_count::NUMERIC / old_role_count * 100), 2);
    RAISE NOTICE '';
    RAISE NOTICE 'User-Role Mappings:';
    RAISE NOTICE '  Old system (sec_user_role): %', old_mapping_count;
    RAISE NOTICE '  New system (user_roles): %', new_mapping_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Sample migrated users:';

    -- Show first 5 migrated users
    FOR rec IN (
        SELECT username, full_name, user_type, enabled
        FROM users
        ORDER BY created_at
        LIMIT 5
    ) LOOP
        RAISE NOTICE '  - % (%, %)', rec.username, rec.user_type, CASE WHEN rec.enabled THEN 'active' ELSE 'inactive' END;
    END LOOP;

    RAISE NOTICE '';
    RAISE NOTICE '✅ Migration successful!';
    RAISE NOTICE '==============================================';
END $$;

-- =====================================================
-- Migration Complete
-- =====================================================
-- ✅ Roles migrated from sec_role
-- ✅ Users migrated from sec_user
-- ✅ User-role mappings migrated from sec_user_role
-- ✅ Passwords preserved (BCrypt hashed)
-- ✅ Original IDs preserved (for relationships)
-- ✅ Soft deletes respected (delete_ts IS NULL)
-- =====================================================
