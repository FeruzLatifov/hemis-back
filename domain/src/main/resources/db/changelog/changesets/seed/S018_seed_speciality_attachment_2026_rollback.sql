-- =====================================================
-- S018 ROLLBACK: remove the 2026-2027 seeded OTM<->speciality attachments.
-- Targets only seed-provenance rows (created_by tag); user-created attachments untouched.
-- =====================================================
DELETE FROM university_speciality_attachment WHERE created_by = 'seed:S018-2026';
