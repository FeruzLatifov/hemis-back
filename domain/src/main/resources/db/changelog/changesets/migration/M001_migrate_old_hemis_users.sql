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
        -- Migrate users that don't already exist.
        -- 11 ta legacy CUBA ustun (username_lowercase, password_encryption, name,
        -- time_zone_auto, active, group_id, group_names, ip_mask,
        -- change_password_at_logon, sys_tenant_id, dtype) — V006 dan olib tashlangan,
        -- shu sababli INSERT'da ham ishlatilmaydi.
        -- sec_user.active → users.enabled (unify, dublikat emas).
        INSERT INTO users (
            id,
            username,
            password,
            email,
            first_name,
            last_name,
            middle_name,
            full_name,
            position,
            language,
            time_zone,
            user_type,
            university_id,
            enabled,
            version,
            created_at,
            created_by,
            updated_at,
            updated_by
        )
        SELECT
            COALESCE(old.id, gen_random_uuid()),
            old.login_lc,
            COALESCE(old.password, '$2a$10$DISABLED_ACCOUNT_NO_PASSWORD'),
            old.email,
            old.first_name,
            old.last_name,
            old.middle_name,
            COALESCE(old.first_name || ' ' || old.last_name, old.name),
            old.position_,
            old.language_,
            old.time_zone,
            CASE WHEN old._university IS NOT NULL AND old._university != '' THEN 'UNIVERSITY' ELSE 'SYSTEM' END,
            old._university,
            COALESCE(old.active, TRUE),  -- sec_user.active → users.enabled
            COALESCE(old.version, 1),
            COALESCE(old.create_ts, CURRENT_TIMESTAMP),
            'migration',
            old.update_ts,
            old.updated_by
        FROM sec_user old
        WHERE old.delete_ts IS NULL
        ON CONFLICT (username) WHERE deleted_at IS NULL DO UPDATE SET
            password = EXCLUDED.password,
            email = EXCLUDED.email,
            first_name = EXCLUDED.first_name,
            last_name = EXCLUDED.last_name,
            middle_name = EXCLUDED.middle_name,
            full_name = EXCLUDED.full_name,
            enabled = EXCLUDED.enabled,
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

-- =====================================================
-- PHASE 2 DUAL-WRITE: UNIVERSITY users → oauth_client
-- =====================================================
-- Strangler Fig Pattern (Martin Fowler): the existing {user_type='UNIVERSITY'} rows keep
-- working via the legacy password grant AND gain a parallel oauth_client row so they
-- can opt-in to the new client_credentials grant whenever they're ready.
--
-- Idempotent — safe to re-run with M001 runOnChange=true.
-- Preserves the BCrypt hash (same secret works on both endpoints until OTM rotates it).
-- =====================================================
DO $$
DECLARE
    otm_client_count INTEGER := 0;
BEGIN
    -- 1. Create oauth_client row for every UNIVERSITY user (1:1 with OTM backend).
    --    Skip if already exists (re-run safe).
    INSERT INTO oauth_client (
        id,
        client_id,
        client_secret_hash,
        client_name,
        client_type,
        university_code,
        grant_types,
        is_active,
        contact_email,
        contact_phone,
        secret_version,
        version,
        created_at,
        created_by
    )
    SELECT
        gen_random_uuid(),
        u.username,
        u.password,
        COALESCE(NULLIF(u.full_name, ''), 'OTM ' || u.university_id),
        'UNIVERSITY_BACKEND',
        u.university_id,
        ARRAY['client_credentials', 'password']::TEXT[],
        TRUE,
        u.email,
        u.phone,
        1,
        1,
        CURRENT_TIMESTAMP,
        'migration-phase2'
    FROM users u
    WHERE u.user_type = 'UNIVERSITY'
      AND u.university_id IS NOT NULL
      AND u.deleted_at IS NULL
    ON CONFLICT (client_id) DO NOTHING;

    GET DIAGNOSTICS otm_client_count = ROW_COUNT;
    RAISE NOTICE 'M001 (Phase 2 dual-write): % new oauth_client rows from UNIVERSITY users', otm_client_count;

    -- 2. Bind OTM_API role to every UNIVERSITY_BACKEND client so machine tokens
    --    carry the same permissions as the human-grant tokens did.
    INSERT INTO oauth_client_role (client_id, role_id)
    SELECT c.id, r.id
    FROM oauth_client c
    CROSS JOIN role r
    WHERE c.client_type = 'UNIVERSITY_BACKEND'
      AND c.deleted_at IS NULL
      AND r.code = 'OTM_API'
      AND r.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM oauth_client_role cr
          WHERE cr.client_id = c.id AND cr.role_id = r.id
      );

END $$;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    total_users INTEGER;
    migrated_users INTEGER;
    with_university INTEGER;
    total_clients INTEGER;
    otm_clients INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_users FROM users;
    SELECT COUNT(*) INTO migrated_users FROM users WHERE created_by = 'migration';
    SELECT COUNT(*) INTO with_university FROM users WHERE university_id IS NOT NULL;
    SELECT COUNT(*) INTO total_clients FROM oauth_client WHERE deleted_at IS NULL;
    SELECT COUNT(*) INTO otm_clients FROM oauth_client
        WHERE client_type = 'UNIVERSITY_BACKEND' AND deleted_at IS NULL;

    RAISE NOTICE 'M001 Complete: % total users (% migrated, % with university)',
        total_users, migrated_users, with_university;
    RAISE NOTICE 'M001 Phase 2: % oauth_client rows (% UNIVERSITY_BACKEND)',
        total_clients, otm_clients;
END $$;
