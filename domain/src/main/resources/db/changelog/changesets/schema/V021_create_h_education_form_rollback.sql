-- =====================================================
-- V021 ROLLBACK: FK -> CHECK'ni qaytarish, h_education_form o'chirish
-- =====================================================
-- Attachment.education_form'ni asl hard-coded CHECK('11','12','16')'ga qaytaradi, keyin
-- modern klassifikatorni o'chiradi. FK avval olib tashlanishi shart (h_education_form'ga tegadi).
-- =====================================================

ALTER TABLE university_speciality_attachment DROP CONSTRAINT IF EXISTS fk_univ_spec_attach_form;
DROP INDEX IF EXISTS idx_univ_spec_attach_form;

ALTER TABLE university_speciality_attachment
    ADD CONSTRAINT chk_univ_spec_attach_form CHECK (education_form IN ('11', '12', '16'));

DROP TABLE IF EXISTS h_education_form;
