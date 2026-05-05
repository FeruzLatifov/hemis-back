-- =====================================================
-- V017: DROP TABLE university_legal
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-05
-- Purpose: Remove university_legal entirely.
--   Reason: All meaningful columns are duplicated in hemishe_e_university
--   (address, cadastre, _soato, tin) or are not used by any consumer
--   (billing_*, business_*, tax_mode, taxpayer_type, soogu_registrator,
--    organization_id, bank_accounts, shipping_addresses).
--   Officials (rector/buxgalter) are managed via employee_job + position_code
--   (UniversityOfficialService), not from legalentity API director/accountant.
--   Tax-authority snapshot (status/registration trail/vat_number) — fetched
--   on-demand via api_mspd, not cached locally.
--
-- Self-contained:
--   - Drops INDEXES first (PostgreSQL drops them with the table, but explicit
--     for the rollback symmetry)
--   - Drops TABLE last
--
-- Depends on: V005 (table created), V006/V008 (no FK to university_legal —
-- safe to drop without cascading impact). Confirmed: no other table references
-- university_legal.id.
-- =====================================================

-- 1. Indexes (PostgreSQL drops with table, but explicit for clarity)
DROP INDEX IF EXISTS uq_ulegal_university_code_active;
DROP INDEX IF EXISTS idx_ulegal_synced_at;
DROP INDEX IF EXISTS idx_ulegal_billing_soato;
DROP INDEX IF EXISTS idx_ulegal_accountant_emp;
DROP INDEX IF EXISTS idx_ulegal_director_emp;
DROP INDEX IF EXISTS idx_ulegal_organization;
DROP INDEX IF EXISTS idx_ulegal_tin;

-- 2. Table (CASCADE not needed — no incoming FK)
DROP TABLE IF EXISTS university_legal;
