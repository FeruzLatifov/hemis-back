--
-- Default Admin User
-- Username: admin
-- Password: admin123 (BCrypt hashed)
--

-- Create admin user if not exists
INSERT INTO hemishe_user (
    id,
    username,
    password,
    roles,
    full_name,
    email,
    enabled,
    account_non_locked,
    failed_attempts,
    create_ts,
    created_by
)
SELECT
    gen_random_uuid(),
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_ADMIN',
    'System Administrator',
    'admin@hemis.uz',
    true,
    true,
    0,
    NOW(),
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM hemishe_user WHERE username = 'admin'
);

