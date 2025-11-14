-- =====================================================
-- V3: Migrate Users from sec_user to users
-- =====================================================
-- Strategy: Simple INSERT statements (NO PL/pgSQL blocks)
-- Purpose: Copy all active users from sec_user → users
-- =====================================================

-- Step 1: Insert all active users
INSERT INTO users (
    id,
    username,
    password,
    email,
    full_name,
    user_type,
    entity_code,
    enabled,
    account_non_locked,
    created_at,
    updated_at
)
SELECT
    su.id,
    su.login,
    su.password,
    NULLIF(TRIM(su.email), ''),
    COALESCE(
        NULLIF(TRIM(su.first_name || ' ' || COALESCE(su.last_name, '')), ''),
        NULLIF(TRIM(su.name), ''),
        su.login
    ),
    CASE
        WHEN su._university IS NOT NULL THEN 'UNIVERSITY'::VARCHAR
        ELSE 'SYSTEM'::VARCHAR
    END,
    su._university,
    COALESCE(su.active, TRUE),
    TRUE,
    COALESCE(su.create_ts, CURRENT_TIMESTAMP),
    su.update_ts
FROM sec_user su
WHERE su.delete_ts IS NULL
  AND su.active = TRUE
  AND su.password IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM users u WHERE u.username = su.login
  );

-- Step 2: Migrate user-role mappings

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

-- Step 3: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_entity_code_v3
    ON users(entity_code) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_user_type_v3
    ON users(user_type) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_enabled_v3
    ON users(enabled) WHERE deleted_at IS NULL;

-- =====================================================
-- Migration Summary (using RAISE NOTICE would work but keep it simple)
-- =====================================================
-- Run this query manually to see statistics:
--
-- SELECT
--     (SELECT COUNT(*) FROM sec_user WHERE delete_ts IS NULL AND active = TRUE) as old_users,
--     (SELECT COUNT(*) FROM users WHERE deleted_at IS NULL) as new_users,
--     (SELECT COUNT(*) FROM user_roles) as role_mappings;
-- =====================================================
