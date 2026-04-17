-- =====================================================
-- M001: MIGRATE OLD HEMIS USERS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Updated: 2025-11-27 - Added all missing fields from sec_user
-- Purpose: One-time migration of users from old HEMIS (sec_user)
-- Strategy: IDEMPOTENT - safe to re-run
-- =====================================================

-- Migrate users from sec_user if table exists
DO $$
DECLARE
    migrated_count INTEGER := 0;
    old_table_exists BOOLEAN;
BEGIN
    -- Check if old table exists
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'sec_user'
    ) INTO old_table_exists;

    IF old_table_exists THEN
        -- Migrate users that don't already exist
        INSERT INTO users (
            id,
            username,
            username_lowercase,
            password,
            password_encryption,
            email,
            name,
            first_name,
            last_name,
            middle_name,
            full_name,
            position,
            language,
            time_zone,
            time_zone_auto,
            user_type,
            university_id,
            group_id,
            group_names,
            enabled,
            active,
            ip_mask,
            change_password_at_logon,
            sys_tenant_id,
            dtype,
            version,
            created_at,
            created_by,
            updated_at,
            updated_by
        )
        SELECT
            COALESCE(old.id, gen_random_uuid()),
            old.login_lc,
            old.login_lc,
            COALESCE(old.password, '$2a$10$DISABLED_ACCOUNT_NO_PASSWORD'),
            old.password_encryption,
            old.email,
            old.name,
            old.first_name,
            old.last_name,
            old.middle_name,
            COALESCE(old.first_name || ' ' || old.last_name, old.name),
            old.position_,
            old.language_,
            old.time_zone,
            old.time_zone_auto,
            CASE WHEN old._university IS NOT NULL AND old._university != '' THEN 'UNIVERSITY' ELSE 'SYSTEM' END,
            old._university,
            old.group_id,
            old.group_names,
            COALESCE(old.active, TRUE),
            COALESCE(old.active, TRUE),
            old.ip_mask,
            old.change_password_at_logon,
            old.sys_tenant_id,
            old.dtype,
            COALESCE(old.version, 1),
            COALESCE(old.create_ts, CURRENT_TIMESTAMP),
            'migration',
            old.update_ts,
            old.updated_by
        FROM sec_user old
        WHERE old.delete_ts IS NULL
        ON CONFLICT (username) DO UPDATE SET
            password = EXCLUDED.password,
            password_encryption = EXCLUDED.password_encryption,
            email = EXCLUDED.email,
            name = EXCLUDED.name,
            first_name = EXCLUDED.first_name,
            last_name = EXCLUDED.last_name,
            middle_name = EXCLUDED.middle_name,
            full_name = EXCLUDED.full_name,
            enabled = EXCLUDED.enabled,
            active = EXCLUDED.active,
            university_id = EXCLUDED.university_id,
            user_type = CASE WHEN EXCLUDED.university_id IS NOT NULL AND EXCLUDED.university_id != '' THEN 'UNIVERSITY' ELSE users.user_type END,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = 'migration-sync';

        GET DIAGNOSTICS migrated_count = ROW_COUNT;
        RAISE NOTICE 'M001: Migrated % users from sec_user', migrated_count;
    ELSE
        RAISE NOTICE 'M001: sec_user table not found - skipping migration';
    END IF;
END $$;

-- =====================================================
-- ROLE ASSIGNMENT (old-hemis rollariga mos)
-- =====================================================

-- 1. UNIVERSITY type users → OTM_API role (old-hemis "OTM" roli)
--    Bu 258 ta otm* userlar — Univer B2B sync uchun CRUD kerak
INSERT INTO user_role (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'migration'
FROM users u CROSS JOIN role r
WHERE u.user_type = 'UNIVERSITY'
  AND r.code = 'OTM_API'
  AND NOT EXISTS (
      SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  )
ON CONFLICT DO NOTHING;

-- 2. SYSTEM type users → VIEWER role (default for ministry staff)
INSERT INTO user_role (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'migration'
FROM users u CROSS JOIN role r
WHERE u.user_type = 'SYSTEM'
  AND u.username NOT IN ('admin', 'anonymous')
  AND r.code = 'VIEWER'
  AND NOT EXISTS (
      SELECT 1 FROM user_role ur WHERE ur.user_id = u.id
  )
ON CONFLICT DO NOTHING;

-- 3. admin → SUPER_ADMIN
INSERT INTO user_role (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'migration'
FROM users u CROSS JOIN role r
WHERE u.username = 'admin'
  AND r.code = 'SUPER_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- DATA FIX: Sync user_type for migrated users
-- Migrated users have university_id set but user_type = 'SYSTEM'
-- even when they belong to a university
-- =====================================================
UPDATE users
SET user_type = 'UNIVERSITY',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'migration-sync'
WHERE university_id IS NOT NULL
  AND user_type = 'SYSTEM';

-- Verification
DO $$
DECLARE
    total_users INTEGER;
    migrated_users INTEGER;
    with_university INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_users FROM users;
    SELECT COUNT(*) INTO migrated_users FROM users WHERE created_by = 'migration';
    SELECT COUNT(*) INTO with_university FROM users WHERE university_id IS NOT NULL;
    RAISE NOTICE 'M001 Complete: % total users (% migrated, % with university)',
        total_users, migrated_users, with_university;
END $$;
