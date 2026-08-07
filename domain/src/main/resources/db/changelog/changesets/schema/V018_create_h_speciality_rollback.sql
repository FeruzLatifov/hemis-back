-- Rollback V018: drop the unified speciality classifier + its year child +
-- the education-year classifier. FK order: year child (references both
-- h_speciality and h_education_year) -> h_speciality -> h_education_year.
DROP TABLE IF EXISTS h_speciality_year CASCADE;
DROP TABLE IF EXISTS h_speciality CASCADE;
DROP TABLE IF EXISTS h_education_year CASCADE;
-- fold() backs the generated name_search; drop it only after the table that depends on it is gone.
DROP FUNCTION IF EXISTS h_speciality_fold(text);
