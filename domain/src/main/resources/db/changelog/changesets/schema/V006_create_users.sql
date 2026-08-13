-- =====================================================
-- V006: AUTH MODULE — users + password_history + password_reset_token + oauth_client
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-23
-- Purpose: Identity & Access Management tables.
--   1. users             — HUMAN login accounts (ministry, admin, rektor, ...)
--                           employee_id FK → V004 employee(id)
--   2. password_history  — parol qayta ishlatishni oldini olish
--   3. password_reset_token — parol tiklash uchun
--   4. oauth_client      — MACHINE B2B accounts (univer_101..224, MyGov, OneID)
--                           organization_id FK → V005 organization(id)
--   5. oauth_client_role — Machine role binding (same `role` table reused)
--
-- Depends on: V001 role, V004 employee, V005 organization,
--             legacy hemishe_e_university (university_id FK — OTM scope).
--
-- Self-contained: every FK is declared INLINE (no cross-file ALTER TABLE).
-- =====================================================

CREATE TABLE users
(
    -- Primary Key
    id                       UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    -- Authentication (Partial UNIQUE'lar pastda — soft-delete uyg'unligi)
    -- Case-insensitive collision oldini olish — functional UNIQUE INDEX (LOWER(username)) pastda
    username                 VARCHAR(255) NOT NULL,
    password                 VARCHAR(255) NOT NULL,
    email                    VARCHAR(255),

    -- Personal Information (HUMAN account display)
    first_name               VARCHAR(255),
    last_name                VARCHAR(255),
    middle_name              VARCHAR(255),
    full_name                VARCHAR(255),
    position                 VARCHAR(255),

    -- National ID (JSHSHIR/PINFL, 14 raqam) — shaxs identifikatori anchor.
    -- Bu YERDA saqlanadi (employee'da emas): vazirlik/tashkilot admini OTM xodimi EMAS.
    -- PII: hech qachon log'ga, hech qachon audit JSONB'ga tushmaydi (rekursiv redaction).
    -- Ruxsat-gate bilan o'qiladi. Machine/legacy satrlar uchun NULL (OneID/qo'lda — P4).
    pinfl                    VARCHAR(14),

    -- User Settings (UI preferences)
    language                 VARCHAR(20),
    time_zone                VARCHAR(50),
    locale                   VARCHAR(20),

    -- User Context (Multi-tenancy)
    user_type                VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
    -- Legacy CUBA university FK (old-hemis standartiga mos)
    university_id            VARCHAR(255) REFERENCES hemishe_e_university (code)
                                          ON DELETE SET NULL ON UPDATE CASCADE,
    -- Organization tenancy (vazirlik ost-tashkiloti / tashqi organ). RESTRICT: aktiv
    -- admini bor tashkilotni jimgina o'chirib bo'lmaydi. Scope resolver — P3.
    organization_id          UUID         REFERENCES organization (id) ON DELETE RESTRICT,
    phone                    VARCHAR(50),

    -- Account Status
    enabled                  BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_locked       BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts          INTEGER               DEFAULT 0,
    locked_at                TIMESTAMP,

    -- Optional ko'prik → V004 employee(id). FAQAT bu login aynan OTM xodimi bo'lsa
    -- (masalan rektor) o'rnatiladi — o'shanда OTM-HR ma'lumot uchun join. DOIMIY optional:
    -- vazirlik/tashkilot admini xodim emas; PINFL to'g'ridan-to'g'ri users.pinfl'da
    -- (PINFL uchun employee'ga join YO'Q).
    employee_id              UUID         REFERENCES employee(id) ON DELETE RESTRICT,

    -- Security hardening (rules.md #5 — Security by default)
    rate_limit_rpm           INTEGER               DEFAULT 60,
    secret_rotated_at        TIMESTAMP,
    secret_expires_at        TIMESTAMP,

    -- Versioning (Optimistic Locking)
    version                  INTEGER               DEFAULT 1,

    -- Timestamps
    created_at               TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(50),
    updated_at               TIMESTAMP,
    updated_by               VARCHAR(50),
    deleted_at               TIMESTAMP,
    deleted_by               VARCHAR(50),

    -- Constraints
    CONSTRAINT chk_user_type CHECK (
        user_type IN ('UNIVERSITY', 'MINISTRY', 'ORGANIZATION', 'SYSTEM')
    ),
    -- PINFL format: aynan 14 raqam (yoki NULL). Uniqueness — partial index (pastda).
    CONSTRAINT chk_users_pinfl_format CHECK (pinfl IS NULL OR pinfl ~ '^\d{14}$')
);

-- Comments
COMMENT ON TABLE  users IS 'Core user accounts for authentication (humans only — ministry, admin, rektor, ...)';
COMMENT ON COLUMN users.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp (null = active)';
COMMENT ON COLUMN users.locked_at IS 'Timestamp when account was locked (auto-unlock after 15 min)';
COMMENT ON COLUMN users.employee_id IS 'Optional bridge to OTM employee(id). Set only when this login is an OTM employee (rektor). NULL for ministry/org admins.';
COMMENT ON COLUMN users.pinfl IS 'JSHSHIR/PINFL national ID (14 digits). PII — never log, never persist into audit JSONB (recursive redaction). Read-gated. NULL for machine-migrated/legacy rows.';
COMMENT ON COLUMN users.organization_id IS 'FK to organization(id). Ministry sub-org / external body tenancy. NULL for UNIVERSITY/MINISTRY/SYSTEM users. Scope resolution: P3.';

-- Indexes
CREATE INDEX idx_users_email              ON users (email)          WHERE email IS NOT NULL;
CREATE INDEX idx_users_university_id      ON users (university_id)  WHERE university_id IS NOT NULL;
CREATE INDEX idx_users_deleted_at         ON users (deleted_at)     WHERE deleted_at IS NULL;
CREATE INDEX idx_users_enabled            ON users (enabled)        WHERE enabled = TRUE;
CREATE INDEX idx_users_user_type          ON users (user_type);
CREATE INDEX idx_users_employee_id        ON users (employee_id)    WHERE employee_id IS NOT NULL;
CREATE INDEX idx_users_organization_id    ON users (organization_id) WHERE organization_id IS NOT NULL;
CREATE INDEX idx_users_secret_expires     ON users (secret_expires_at) WHERE secret_expires_at IS NOT NULL;

-- Partial UNIQUE indekslar: soft-deleted user'lar username/email'ni qayta ishlatishga ruxsat beradi
CREATE UNIQUE INDEX uq_users_username       ON users (username)        WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_users_email          ON users (email)           WHERE deleted_at IS NULL AND email IS NOT NULL;

-- Functional UNIQUE INDEX: case-insensitive collision oldini olish ("Admin" vs "admin")
-- Avval username_lowercase ustuni shu vazifani bajarar edi — endi functional index, ustun saqlash kerak emas
CREATE UNIQUE INDEX uq_users_username_lower ON users (LOWER(username)) WHERE deleted_at IS NULL;

-- Bitta jismoniy shaxs = bitta akkaunt (D2). Partial: NULL pinfl (machine/legacy) ozod;
-- soft-deleted satrlar pinfl'ni qayta ishlatishi mumkin.
CREATE UNIQUE INDEX uq_users_pinfl          ON users (pinfl) WHERE pinfl IS NOT NULL AND deleted_at IS NULL;

-- =====================================================
-- PASSWORD HISTORY (parol qayta ishlatishni oldini olish)
-- =====================================================
CREATE TABLE password_history
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    created_by    VARCHAR(50)
);

CREATE INDEX idx_password_history_user_id ON password_history (user_id);

-- =====================================================
-- PASSWORD RESET TOKEN (parol tiklash)
-- =====================================================
CREATE TABLE password_reset_token
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP   NOT NULL,
    used       BOOLEAN     NOT NULL DEFAULT false,
    created_at TIMESTAMP   NOT NULL DEFAULT now(),
    created_by VARCHAR(50)
);

CREATE INDEX idx_prt_token         ON password_reset_token (token);
CREATE INDEX idx_prt_user_expires  ON password_reset_token (user_id, expires_at);
CREATE INDEX idx_prt_user_created  ON password_reset_token (user_id, created_at);

-- =====================================================
-- OAUTH_CLIENT — B2B machine accounts (univer_101..224, MyGov, OneID, ...)
-- =====================================================
-- OAuth 2.0 RFC 6749 §4.4 client_credentials grant.
-- Alohida jadval (`users` dan ajratilgan) — machine lifecycle farqli:
--   - Secret rotation (180 kun)
--   - IP whitelist (OTM network'dan kirish)
--   - Rate limit per-client (60-1000 rpm)
--   - No human policy (MFA, password expiry)
-- =====================================================
CREATE TABLE oauth_client (
    id                       UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    -- OAuth 2.0 standard fields
    client_id                VARCHAR(100) NOT NULL UNIQUE,  -- 'univer_101', 'mygov_sync'
    client_secret_hash       VARCHAR(255) NOT NULL,          -- BCrypt (LegacyPasswordEncoder compat)
    client_name              VARCHAR(255) NOT NULL,          -- Display: "Toshkent TAJEDU"

    -- Client type discriminator
    client_type              VARCHAR(30)  NOT NULL,

    -- Tenancy (polymorphic XOR — UNIVERSITY_BACKEND → university_code, boshqalar → organization_id)
    university_code          VARCHAR(255) REFERENCES hemishe_e_university(code) ON DELETE RESTRICT,
    organization_id          UUID         REFERENCES organization(id)            ON DELETE SET NULL,

    -- Grant types (RFC 6749)
    grant_types              TEXT[] NOT NULL DEFAULT ARRAY['client_credentials']::TEXT[],

    -- OAuth 2.0 scopes (RFC 6749 §3.3) — per-client allowed permission codes.
    -- Default 'rest-api' (umbrella) for backward-compat with 224 OTM password flow.
    -- Narrow B2B clients (MyGov, OneID, …) use ['students.view', 'pinfl.lookup', …].
    scopes                   TEXT[] NOT NULL DEFAULT ARRAY['rest-api']::TEXT[],

    -- Network-level security (rules.md #5)
    allowed_ip_cidr          TEXT[],

    -- Rate limiting
    rate_limit_rpm           INTEGER      NOT NULL DEFAULT 60,
    rate_limit_burst         INTEGER      NOT NULL DEFAULT 10,

    -- Token config
    access_token_ttl_seconds INTEGER      NOT NULL DEFAULT 3600,     -- 1 hour
    refresh_token_ttl_seconds INTEGER              DEFAULT 2592000,  -- 30 days

    -- Lifecycle
    is_active                BOOLEAN      NOT NULL DEFAULT TRUE,
    expires_at               TIMESTAMP,                     -- NULL = never
    last_used_at             TIMESTAMP,
    last_used_ip             VARCHAR(45),

    -- Secret rotation tracking
    secret_rotated_at        TIMESTAMP,
    secret_version           INTEGER      NOT NULL DEFAULT 1,

    -- Partner contact (univer IT team, MyGov team, ...)
    contact_email            VARCHAR(255),
    contact_phone            VARCHAR(50),

    -- Audit (AuditableEntity pattern)
    version                  INTEGER      NOT NULL DEFAULT 1,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(50),
    updated_at               TIMESTAMP,
    updated_by               VARCHAR(50),
    deleted_at               TIMESTAMP,
    deleted_by               VARCHAR(50),

    -- Constraints
    CONSTRAINT chk_oauth_client_type CHECK (
        client_type IN ('UNIVERSITY_BACKEND', 'EXTERNAL_SYSTEM', 'INTERNAL_SERVICE')
    ),
    -- Strict XOR: UNIVERSITY_BACKEND faqat university_code, boshqalar faqat organization_id (yoki ikkalasi NULL)
    -- Bu data ambiguity'ni oldini oladi — bir client_type ikkala FK'ga ega bo'lolmaydi
    CONSTRAINT chk_oauth_client_tenancy CHECK (
        (client_type = 'UNIVERSITY_BACKEND' AND university_code IS NOT NULL AND organization_id IS NULL)
        OR
        (client_type <> 'UNIVERSITY_BACKEND' AND university_code IS NULL)
    ),
    CONSTRAINT chk_oauth_grant_types CHECK (
        grant_types <@ ARRAY['client_credentials', 'password', 'refresh_token']::TEXT[]
    )
);

COMMENT ON TABLE  oauth_client IS 'B2B machine accounts (univer backend, MyGov, OneID). OAuth 2.0 RFC 6749.';
COMMENT ON COLUMN oauth_client.client_id IS 'OAuth client identifier — e.g. ''univer_101'', ''mygov_sync''';
COMMENT ON COLUMN oauth_client.client_secret_hash IS 'BCrypt hash of client secret (LegacyPasswordEncoder compatible)';
COMMENT ON COLUMN oauth_client.allowed_ip_cidr IS 'IP whitelist (CIDR blocks). Empty = no restriction (DEV only). Prod: OTM office IPs.';
COMMENT ON COLUMN oauth_client.organization_id IS 'FK to organization(id). NULL for UNIVERSITY_BACKEND clients (which use university_code instead).';
COMMENT ON COLUMN oauth_client.scopes IS 'OAuth scopes (=permission codes). Default ''rest-api'' umbrella. Narrow clients: [''students.view'', ...].';

CREATE INDEX idx_oauth_client_client_id        ON oauth_client (client_id);
CREATE INDEX idx_oauth_client_type             ON oauth_client (client_type);
CREATE INDEX idx_oauth_client_university       ON oauth_client (university_code) WHERE university_code IS NOT NULL;
CREATE INDEX idx_oauth_client_organization     ON oauth_client (organization_id) WHERE organization_id IS NOT NULL;
CREATE INDEX idx_oauth_client_active           ON oauth_client (is_active) WHERE is_active = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_oauth_client_expires          ON oauth_client (expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_oauth_client_secret_expires   ON oauth_client (last_used_at DESC);
CREATE INDEX idx_oauth_client_scopes           ON oauth_client USING GIN (scopes);

-- =====================================================
-- OAUTH_CLIENT_ROLE — Machine role binding (reuses `role` table — Single Source of Truth)
-- =====================================================
-- Human → user_role, Machine → oauth_client_role. Ikkalasi `role_permission` orqali permission'larga.
-- =====================================================
CREATE TABLE oauth_client_role (
    client_id  UUID NOT NULL REFERENCES oauth_client(id) ON DELETE CASCADE,
    role_id    UUID NOT NULL REFERENCES role(id)         ON DELETE CASCADE,
    PRIMARY KEY (client_id, role_id)
);

CREATE INDEX idx_oauth_client_role_client ON oauth_client_role (client_id);
CREATE INDEX idx_oauth_client_role_role   ON oauth_client_role (role_id);

COMMENT ON TABLE oauth_client_role IS 'Machine role binding. Pattern: users ← user_role → role ↔ role_permission ↔ permission → oauth_client_role → oauth_client';
