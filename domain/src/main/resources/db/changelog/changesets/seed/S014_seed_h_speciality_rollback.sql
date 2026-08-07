-- Rollback S014: remove seeded speciality rows.
-- FK order: attachment (ON DELETE RESTRICT) -> year (CASCADE) -> speciality.
-- Attachments are deleted first or the RESTRICT FK aborts the rollback.
DELETE FROM h_speciality_attachment;
DELETE FROM h_speciality_year;
DELETE FROM h_speciality;
