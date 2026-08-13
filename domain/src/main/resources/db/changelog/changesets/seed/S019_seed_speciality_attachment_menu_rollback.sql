-- =====================================================
-- S019 rollback: remove the inst-speciality-attachments menu entry.
-- The 'Speciality attachments' system_message is left in place (idempotent, harmless).
-- =====================================================
DELETE FROM menu WHERE code = 'inst-speciality-attachments';
