-- =====================================================
-- Rollback V005: DROP organization + university_profile
-- =====================================================
-- Self-contained: only drops own tables. oauth_client.organization_id FK
-- (declared in V006) is dropped CASCADE-style with DROP TABLE organization.
-- Drop order: child tables first (university_profile → organization).
-- =====================================================

DROP TABLE IF EXISTS university_profile CASCADE;
DROP TABLE IF EXISTS organization       CASCADE;
