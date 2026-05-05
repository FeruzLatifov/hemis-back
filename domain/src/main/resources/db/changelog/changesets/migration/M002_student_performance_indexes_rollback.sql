-- Rollback M002: drop all index migrations on hemishe_e_* tables.
-- pg_trgm extension NOT dropped (other tooling may depend on it).

-- StudentMeta race-fix
DROP INDEX IF EXISTS uq_student_meta_uid_university_active;

-- StudentDiploma trigram
DROP INDEX IF EXISTS idx_diploma_number_trgm;

-- Attendance
DROP INDEX IF EXISTS idx_attendance_uni_date;
DROP INDEX IF EXISTS idx_attendance_student_active;

-- Grade
DROP INDEX IF EXISTS idx_grade_student_active;
DROP INDEX IF EXISTS idx_grade_university_active;

-- Student keyset paging + PINFL master uniqueness
DROP INDEX IF EXISTS uq_student_pinfl_master;
DROP INDEX IF EXISTS idx_student_university_status_createts;

-- Student baseline (existing M002)
DROP INDEX IF EXISTS idx_student_active_code_desc;
DROP INDEX IF EXISTS idx_student_delts_code;
DROP INDEX IF EXISTS idx_student_univ_status_delts;
DROP INDEX IF EXISTS idx_student_pinfl_prefix;
DROP INDEX IF EXISTS idx_student_code_prefix;
DROP INDEX IF EXISTS idx_student_pinfl_trgm;
DROP INDEX IF EXISTS idx_student_code_trgm;
DROP INDEX IF EXISTS idx_student_firstname_trgm;
DROP INDEX IF EXISTS idx_student_lastname_trgm;
DROP INDEX IF EXISTS idx_student_lastname;
DROP INDEX IF EXISTS idx_student_pinfl;
DROP INDEX IF EXISTS idx_student_code;
DROP INDEX IF EXISTS idx_student_gender;
DROP INDEX IF EXISTS idx_student_education_year;
DROP INDEX IF EXISTS idx_student_education_form;
DROP INDEX IF EXISTS idx_student_education_type;
DROP INDEX IF EXISTS idx_student_faculty;
DROP INDEX IF EXISTS idx_student_course;
DROP INDEX IF EXISTS idx_student_payment_form;
DROP INDEX IF EXISTS idx_student_status;
DROP INDEX IF EXISTS idx_student_university;
DROP INDEX IF EXISTS idx_student_delete_ts;
