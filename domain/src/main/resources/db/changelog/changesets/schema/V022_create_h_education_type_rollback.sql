-- =====================================================
-- V022 ROLLBACK: repoint h_speciality FK back to CUBA, drop h_education_type
-- =====================================================
-- h_speciality.education_type FK'ini asl hemishe_h_education_type(code)'ga qaytaradi, keyin modern
-- klassifikatorni o'chiradi. FK avval olib tashlanishi shart (h_education_type'ga tegadi).
-- =====================================================

ALTER TABLE h_speciality DROP CONSTRAINT IF EXISTS fk_h_speciality_edu_type;
ALTER TABLE h_speciality
    ADD CONSTRAINT fk_h_speciality_edu_type FOREIGN KEY (education_type)
    REFERENCES hemishe_h_education_type(code);

DROP TABLE IF EXISTS h_education_type;
