-- =====================================================
-- TEST FIXTURE — eski CUBA (old-hemis) jadvallarining stub'i (165 jadval)
-- =====================================================
-- ⚠️ GENERATSIYA QILINGAN — QO'LDA TAHRIRLAMANG.
--    Manba: scripts/generate_legacy_test_stub.py (domain @Entity mapping'laridan)
--    Qayta generatsiya: python3 scripts/generate_legacy_test_stub.py
--
-- FAQAT integratsiya testlari uchun. Prod changelog'iga HECH QACHON qo'shilmaydi —
-- bu fayl app/src/test/resources ostida va uni yagona ishlatuvchi
-- IntegrationTestDatabaseConfig.
--
-- NEGA KERAK:
--   db.changelog-master.yaml o'zini o'zi ta'minlamaydi: u eski CUBA jadvallariga FK /
--   SELECT / CREATE INDEX bilan tayanadi, lekin ularni yaratmaydi (real muhitlarda ular
--   old-hemis dump'idan keladi). Busiz toza PostgreSQL'da migratsiya
--   V004_create_employee da "relation hemishe_h_gender does not exist" bilan to'xtaydi.
--
-- CHEKLOV:
--   Bu real legacy schema'ning nusxasi EMAS — migratsiya va testlar o'tishi uchun
--   yetarli minimum. Yetishmovchilik BALAND OVOZ bilan yiqiladi, jimgina noto'g'ri
--   ishlamaydi.
-- =====================================================

CREATE TABLE IF NOT EXISTS hemishe_e_attendance (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_student"                             UUID,
    "_course"                              UUID,
    "_schedule"                            UUID,
    "_university"                          TEXT,
    "_group"                               UUID,
    attendance_date                        DATE,
    "_attendance_type"                     TEXT,
    academic_year                          TEXT,
    semester                               INTEGER,
    week_number                            INTEGER,
    is_present                             BOOLEAN,
    reason                                 TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_contract (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    contract_number                        TEXT,
    "_student"                             UUID,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_contract_type"                       TEXT,
    contract_sum                           NUMERIC,
    paid_sum                               NUMERIC,
    remaining_sum                          NUMERIC,
    contract_date                          DATE,
    start_date                             DATE,
    end_date                               DATE,
    "_payment_form"                        TEXT,
    "_status"                              TEXT,
    contractor_name                        TEXT,
    contractor_passport                    TEXT,
    contractor_address                     TEXT,
    contractor_phone                       TEXT,
    notes                                  TEXT,
    is_active                              BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_course (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    short_name                             TEXT,
    "_university"                          TEXT,
    "_subject"                             UUID,
    credit_count                           INTEGER,
    total_hours                            INTEGER,
    lecture_hours                          INTEGER,
    practice_hours                         INTEGER,
    lab_hours                              INTEGER,
    semester                               INTEGER,
    "_course_type"                         TEXT,
    "_assessment_type"                     TEXT,
    active                                 BOOLEAN,
    is_elective                            BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_curriculum (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    "_university"                          TEXT,
    "_specialty"                           UUID,
    academic_year                          TEXT,
    total_credits                          INTEGER,
    study_duration                         INTEGER,
    "_education_type"                      TEXT,
    "_education_form"                      TEXT,
    "_curriculum_type"                     TEXT,
    active                                 BOOLEAN,
    is_approved                            BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_department (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    department_code                        TEXT,
    department_name                        TEXT,
    department_name_uz                     TEXT,
    department_name_ru                     TEXT,
    department_name_en                     TEXT,
    "_university"                          TEXT,
    "_faculty"                             UUID,
    "_head"                                UUID,
    "_department_type"                     TEXT,
    phone_number                           TEXT,
    email                                  TEXT,
    room_number                            TEXT,
    building                               TEXT,
    is_active                              BOOLEAN,
    description                            TEXT,
    sort_order                             INTEGER
);

CREATE TABLE IF NOT EXISTS hemishe_e_dissertation_defense (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    "_doctorate_student"                   UUID,
    defense_date                           DATE,
    defense_place                          TEXT,
    approved_date                          DATE,
    diploma_number                         TEXT,
    diploma_given_date                     DATE,
    diploma_given_by_whom                  TEXT,
    register_number                        TEXT,
    filename                               TEXT,
    "_position"                            INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT,
    "_speciality"                          UUID
);

CREATE TABLE IF NOT EXISTS hemishe_e_doctorate_student (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    first_name                             TEXT,
    second_name                            TEXT,
    third_name                             TEXT,
    passport_number                        TEXT,
    passport_pin                           TEXT,
    birth_date                             DATE,
    dissertation_theme                     TEXT,
    home_address                           TEXT,
    accepted_date                          DATE,
    student_id_number                      TEXT,
    "_science_branch"                      TEXT,
    "_payment_form"                        TEXT,
    "_citizenship"                         TEXT,
    "_nationality"                         TEXT,
    "_gender"                              TEXT,
    "_country"                             TEXT,
    "_province"                            TEXT,
    "_district"                            TEXT,
    "_soato"                               TEXT,
    "_doctoral_student_type"               TEXT,
    "_doctorate_student_status"            TEXT,
    "_level"                               TEXT,
    "_university"                          TEXT,
    "_department"                          TEXT,
    "_position"                            INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT,
    "_speciality"                          UUID,
    "_education_year"                      TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_employee_jobs (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_employee"                            UUID,
    "_university"                          TEXT,
    "_department"                          TEXT,
    "_employee_type"                       TEXT,
    "_employee_position"                   TEXT,
    "_employee_rate"                       TEXT,
    "_employee_form"                       TEXT,
    "_employee_status"                     TEXT,
    job_start_date                         DATE,
    job_end_date                           DATE,
    tag                                    TEXT,
    contract_date                          DATE,
    contract_number                        TEXT,
    decree_date                            DATE,
    decree_number                          TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_employment (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    employment_code                        TEXT,
    "_student"                             UUID,
    "_university"                          TEXT,
    "_diploma"                             UUID,
    company_name                           TEXT,
    company_tin                            TEXT,
    company_address                        TEXT,
    company_phone                          TEXT,
    "_employment_type"                     TEXT,
    position                               TEXT,
    employment_date                        DATE,
    contract_number                        TEXT,
    contract_date                          DATE,
    salary                                 NUMERIC,
    "_employment_status"                   TEXT,
    termination_date                       DATE,
    termination_reason                     TEXT,
    "_soato"                               TEXT,
    "_industry_code"                       TEXT,
    is_specialty_related                   BOOLEAN,
    notes                                  TEXT,
    is_active                              BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_empoyee_certificate (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_employee"                            UUID,
    "_certificate_type"                    TEXT,
    "_certificate_name"                    TEXT,
    "_certificate_grade"                   TEXT,
    "_certificate_subject"                 TEXT,
    issue_date                             DATE,
    valid_date                             DATE,
    serial_number                          TEXT,
    active                                 BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_enrollment (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    enrollment_number                      TEXT,
    "_student"                             UUID,
    "_university"                          TEXT,
    "_specialty"                           UUID,
    "_faculty"                             UUID,
    enrollment_date                        DATE,
    academic_year                          TEXT,
    course                                 INTEGER,
    "_education_type"                      TEXT,
    "_education_form"                      TEXT,
    "_payment_form"                        TEXT,
    "_enrollment_status"                   TEXT,
    active                                 BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_exam (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    exam_name                              TEXT,
    "_course"                              UUID,
    "_group"                               UUID,
    "_teacher"                             UUID,
    "_university"                          TEXT,
    "_auditorium"                          UUID,
    exam_date                              DATE,
    start_time                             TIME,
    end_time                               TIME,
    duration_minutes                       INTEGER,
    academic_year                          TEXT,
    semester                               INTEGER,
    "_exam_type"                           TEXT,
    max_score                              INTEGER,
    passing_score                          INTEGER,
    active                                 BOOLEAN,
    is_published                           BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_faculty (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    "_university"                          TEXT,
    "_faculty_type"                        TEXT,
    active                                 TEXT,
    short_name                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_grade (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_student"                             UUID,
    "_course"                              UUID,
    "_university"                          TEXT,
    "_teacher"                             UUID,
    grade_value                            INTEGER,
    grade_letter                           TEXT,
    grade_points                           DOUBLE PRECISION,
    grade_date                             DATE,
    academic_year                          TEXT,
    semester                               INTEGER,
    attempt_number                         INTEGER,
    "_assessment_type"                     TEXT,
    "_grade_type"                          TEXT,
    is_passed                              BOOLEAN,
    is_finalized                           BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_project (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    name                                   TEXT,
    project_number                         TEXT,
    "_university"                          TEXT,
    "_department"                          TEXT,
    "_project_type"                        TEXT,
    "_locality"                            TEXT,
    "_project_currency"                    TEXT,
    contract_number                        TEXT,
    contract_date                          DATE,
    start_date                             DATE,
    end_date                               DATE,
    position                               INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_project_executor (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_project"                             UUID,
    "_project_executor_type"               TEXT,
    "_id_number"                           INTEGER,
    outsider                               TEXT,
    start_date                             DATE,
    end_date                               DATE,
    position                               INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_project_meta (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_project"                             UUID,
    fiscal_year                            INTEGER,
    budget                                 DOUBLE PRECISION,
    quantity_members                       INTEGER,
    position                               INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_publication_author_meta (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    "_employee"                            UUID,
    is_main_author                         INTEGER,
    publication_type_table                 TEXT,
    "_publication_methodical"              UUID,
    "_publication_scientific"              UUID,
    "_publication_property"                UUID,
    is_checked_by_author                   BOOLEAN,
    position                               INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT,
    "_university"                          TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_publication_criteria (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_publication_type_table"              TEXT,
    "_publication_methodical_type"         TEXT,
    "_publication_scientific_type"         TEXT,
    "_publication_property_type"           TEXT,
    "_in_publication_database"             INTEGER,
    mark_value                             INTEGER,
    position                               INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_publication_methodical (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    "_university"                          TEXT,
    name                                   TEXT,
    authors                                TEXT,
    author_counts                          INTEGER,
    publisher                              TEXT,
    issue_year                             INTEGER,
    source_name                            TEXT,
    parameter                              TEXT,
    "_methodical_publication_type"         TEXT,
    "_publication_database"                TEXT,
    "_employee"                            UUID,
    filename                               TEXT,
    position                               INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT,
    is_checked                             BOOLEAN,
    is_checked_date                        TIMESTAMP,
    "_education_year"                      TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_publication_property (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    "_university"                          TEXT,
    name                                   TEXT,
    numbers                                TEXT,
    authors                                TEXT,
    author_counts                          INTEGER,
    parameter                              TEXT,
    property_date                          DATE,
    "_patent_type"                         TEXT,
    "_publication_database"                TEXT,
    "_locality"                            TEXT,
    "_country"                             TEXT,
    "_employee"                            TEXT,
    filename                               TEXT,
    position                               INTEGER,
    active                                 BOOLEAN,
    "_translations"                        TEXT,
    is_checked                             BOOLEAN,
    is_checked_date                        TIMESTAMP,
    "_education_year"                      TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_publication_scientific (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    "_university"                          TEXT,
    name                                   TEXT,
    keywords                               TEXT,
    authors                                TEXT,
    author_counts                          INTEGER,
    source_name                            TEXT,
    issue_year                             INTEGER,
    parameter                              TEXT,
    doi                                    TEXT,
    "_scientific_publication_type"         TEXT,
    "_publication_database"                TEXT,
    "_locality"                            TEXT,
    "_country"                             TEXT,
    "_employee"                            TEXT,
    filename                               TEXT,
    position                               TEXT,
    active                                 BOOLEAN,
    "_translations"                        TEXT,
    is_checked                             BOOLEAN,
    is_checked_date                        TIMESTAMP,
    "_education_year"                      TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_research_activity (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_scholar_database"                    TEXT,
    link                                   TEXT,
    h_index                                TEXT,
    scientific_work_count                  TEXT,
    reference_count                        TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_schedule (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_group"                               UUID,
    "_course"                              UUID,
    "_teacher"                             UUID,
    "_auditorium"                          UUID,
    schedule_date                          DATE,
    start_time                             TIME,
    end_time                               TIME,
    day_of_week                            INTEGER,
    pair_number                            INTEGER,
    academic_year                          TEXT,
    semester                               INTEGER,
    week_number                            INTEGER,
    "_lesson_type"                         TEXT,
    "_schedule_type"                       TEXT,
    active                                 BOOLEAN,
    is_cancelled                           BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_student (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    code                                   TEXT PRIMARY KEY,
    firstname                              TEXT,
    lastname                               TEXT,
    fathername                             TEXT,
    pinfl                                  TEXT,
    is_duplicate                           BOOLEAN,
    birthday                               DATE,
    serial_number                          TEXT,
    phone                                  TEXT,
    address                                TEXT,
    current_address                        TEXT,
    "_soato"                               TEXT,
    "_current_soato"                       TEXT,
    "_university"                          TEXT,
    "_faculty"                             TEXT,
    "_speciality"                          TEXT,
    "_student_status"                      TEXT,
    "_payment_form"                        TEXT,
    "_education_type"                      TEXT,
    "_education_form"                      TEXT,
    "_course"                              TEXT,
    "_education_year"                      TEXT,
    "_current_education_year_code"         TEXT,
    "_gender"                              TEXT,
    "_nationality"                         TEXT,
    "_citizenship"                         TEXT,
    "_country"                             TEXT,
    "_language"                            TEXT,
    "_accomodation"                        TEXT,
    "_living_status"                       TEXT,
    "_roommate_type"                       TEXT,
    "_social_category"                     TEXT,
    "_stipend_rate"                        TEXT,
    "_expel_reason"                        TEXT,
    "_doctoral_student_type"               TEXT,
    "_poverty_level"                       TEXT,
    "_grant_type"                          TEXT,
    "_speciality_bachelor"                 UUID,
    "_speciality_master"                   UUID,
    "_speciality_doctoral"                 UUID,
    status                                 TEXT,
    tag                                    TEXT,
    active                                 BOOLEAN,
    roommate_count                         INTEGER,
    responsible_person_phone               TEXT,
    verified                               BOOLEAN,
    points                                 TEXT,
    email                                  TEXT,
    parent_phone                           TEXT,
    geo_address                            TEXT,
    group_id                               TEXT,
    group_name                             TEXT,
    is_graduate                            TEXT,
    passport_given_date                    DATE,
    enroll_order_name                      TEXT,
    enroll_order_date                      DATE,
    enroll_order_number                    TEXT,
    enroll_order_category                  TEXT,
    status_order_name                      TEXT,
    status_order_date                      DATE,
    status_order_number                    TEXT,
    status_order_category                  TEXT,
    edu_start_date                         DATE,
    "_graduation_year"                     TEXT,
    graduation_date                        DATE,
    "_student_type"                        TEXT,
    study_duration                         TEXT,
    decree_info_name                       TEXT,
    decree_info_number                     TEXT,
    decree_info_date                       DATE,
    "_academic_reason"                     TEXT,
    "_academic_mobile_type"                TEXT,
    "_speciality_ordinatura"               UUID,
    status_education_year_code             TEXT,
    "_terrain"                             TEXT,
    current_terrain_code                   TEXT,
    "_admission_type"                      TEXT,
    "_transfer_country"                    TEXT,
    transfer_university                    TEXT,
    "_transfer_type"                       TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_student_certificate (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_student"                             UUID,
    "_certificate_type"                    TEXT,
    "_certificate_name"                    TEXT,
    "_certificate_grade"                   TEXT,
    "_certificate_subject"                 TEXT,
    issue_date                             DATE,
    valid_date                             DATE,
    serial_number                          TEXT,
    active                                 BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_student_diploma (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_student"                             UUID,
    "_speciality"                          TEXT,
    diploma_number                         TEXT,
    register_number                        TEXT,
    register_date                          DATE,
    translations                           TEXT,
    academic_record                        TEXT,
    active                                 BOOLEAN,
    "_department"                          TEXT,
    total_acload                           TEXT,
    avg_grade                              TEXT,
    speciality_name                        TEXT,
    "_diplom_category"                     TEXT,
    "_education_year"                      TEXT,
    "_education_type"                      TEXT,
    total_credit                           TEXT,
    speciality_code                        TEXT,
    tag                                    TEXT,
    verify                                 TEXT,
    hash                                   TEXT,
    blank_generate_status_code             TEXT,
    study_duration                         REAL,
    graduation_date                        DATE,
    "_admission_year"                      TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_student_gpa (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TEXT,
    created_by                             TEXT,
    update_ts                              TEXT,
    updated_by                             TEXT,
    student_id                             UUID,
    education_year_code                    TEXT,
    gpa                                    TEXT,
    "method_"                              TEXT,
    level_code                             TEXT,
    credit_sum                             TEXT,
    subjects                               INTEGER,
    debt_subjects                          INTEGER
);

CREATE TABLE IF NOT EXISTS hemishe_e_student_meta (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    u_id                                   INTEGER,
    "_university"                          TEXT,
    "_stdent_id_number"                    TEXT,
    "_student"                             UUID,
    "_department"                          UUID,
    "_education_type"                      TEXT,
    "_education_form"                      TEXT,
    "_semester"                            TEXT,
    "_level"                               TEXT,
    "_education_year"                      TEXT,
    "_payment_form"                        TEXT,
    "_student_status"                      TEXT,
    group_id                               INTEGER,
    group_name                             TEXT,
    subgroup_id                            INTEGER,
    subgroup_name                          TEXT,
    diploma_registration                   INTEGER,
    employment_registration                INTEGER,
    order_number                           TEXT,
    order_date                             DATE,
    "_status_change_reason"                TEXT,
    speciality                             TEXT,
    accreditation_accepted                 BOOLEAN,
    decree_number                          TEXT,
    decree_name                            TEXT,
    decree_date                            DATE,
    "_academic_mobile"                     TEXT,
    "_grant_type"                          TEXT,
    "_student_data_contract"               INTEGER,
    "_restore_meta_id"                     INTEGER,
    active                                 BOOLEAN,
    university_created_at                  TIMESTAMP,
    university_updated_at                  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hemishe_e_student_scholarship_amount (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_student_scholarship"                 UUID,
    "month_"                               DATE,
    summa                                  DOUBLE PRECISION,
    local_id                               TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_student_scholarship_full (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_student"                             UUID,
    "_university"                          TEXT,
    "_education_type"                      TEXT,
    "_education_form"                      TEXT,
    "_payment_form"                        TEXT,
    "_semester"                            TEXT,
    "_education_year"                      TEXT,
    "_stipend_category"                    TEXT,
    "_stipend_type"                        TEXT,
    decree                                 TEXT,
    "group_"                               TEXT,
    curriculum                             TEXT,
    start_date                             DATE,
    end_date                               DATE,
    local_id                               TEXT,
    semester_number                        TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_teacher (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    firstname                              TEXT,
    lastname                               TEXT,
    fathername                             TEXT,
    birthday                               DATE,
    pinfl                                  TEXT,
    "_citizenship"                         TEXT,
    "_gender"                              TEXT,
    code                                   TEXT PRIMARY KEY,
    serial_number                          TEXT,
    address                                TEXT,
    phone                                  TEXT,
    employee_year                          TEXT,
    tag                                    TEXT,
    "_university"                          TEXT,
    "_academic_degree"                     TEXT,
    "_academic_rank"                       TEXT,
    "_department"                          TEXT,
    "_position"                            TEXT,
    "_employee_type"                       TEXT,
    "_employment_form"                     TEXT,
    "_university_employment_form"          TEXT,
    "_soato_region"                        TEXT,
    "_soato_district"                      TEXT,
    "_nationality"                         TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_university (
    code                                   TEXT PRIMARY KEY,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    tin                                    TEXT,
    name                                   TEXT,
    address                                TEXT,
    cadastre                               TEXT,
    university_url                         TEXT,
    student_url                            TEXT,
    teacher_url                            TEXT,
    uzbmb_url                              TEXT,
    "_soato"                               TEXT,
    "_soato_region"                        TEXT,
    "_university_type"                     TEXT,
    "_ownership"                           TEXT,
    "_university_version"                  TEXT,
    "_university_activity_status"          TEXT,
    "_university_belongs_to"               TEXT,
    "_university_contract_category"        TEXT,
    "_parent_university"                   TEXT,
    active                                 BOOLEAN,
    gpa_edit                               BOOLEAN,
    accreditation_edit                     BOOLEAN,
    add_student                            BOOLEAN,
    allow_grouping                         BOOLEAN,
    allow_transfer_outside                 BOOLEAN,
    one_id                                 BOOLEAN,
    grading_system                         BOOLEAN,
    add_foreign_student                    BOOLEAN,
    "_terrain"                             TEXT,
    add_transfer_student                   BOOLEAN,
    add_academic_mobile_student            BOOLEAN,
    allow_academic_import                  BOOLEAN,
    is_financial_independent               BOOLEAN,
    mail_address                           TEXT,
    bank_info                              TEXT,
    accreditation_info                     TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_university_attached_speciality (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_form"                      TEXT,
    "_speciality_bachelor"                 UUID,
    "_speciality_master"                   UUID,
    "_speciality_ordinatura"               UUID,
    "_speciality_doctoral"                 UUID,
    "_education_type"                      TEXT,
    active                                 BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_university_department (
    code                                   TEXT PRIMARY KEY,
    name_uz                                TEXT,
    name_ru                                TEXT,
    university_code                        TEXT,
    parent_code                            TEXT,
    path                                   TEXT,
    "_deparment_type"                      TEXT,
    status                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_university_group (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "_university"                          TEXT,
    "_education_type"                      TEXT,
    "_education_year"                      TEXT,
    group_id                               TEXT,
    group_name                             TEXT,
    active                                 BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_e_university_speciality (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "_university"                          TEXT,
    "_education_type"                      TEXT,
    "_education_year"                      TEXT,
    speciality_code                        TEXT,
    speciality_name                        TEXT,
    active                                 BOOLEAN,
    "_faculty"                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_e_verification (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    pinfl                                  TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_education_type"                      TEXT,
    "_payment_form"                        TEXT,
    "_category"                            TEXT,
    points                                 TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_absence_reason (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_academic_degree (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_academic_mobile_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_academic_rank (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_academic_reason (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_accomodation (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_admission_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_attandance_setting (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_auditorium_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_certificate_grades (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_certificate_language (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_certificate_names (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_certificate_subjects (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_certificate_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_citizenship (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_class_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_contract_summa_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_contract_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_contract_types (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_country (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_course (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_currency (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_decree_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_decree_type_param (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_decree_type"                         TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_device_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_diplom_blank_category (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_diplom_blank_generate_status (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_diplom_blank_status (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_doctoral_student_status (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_doctoral_student_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_education_form (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_education_language (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_education_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_education_week_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_education_year (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_employee_age_range (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_exam_finish (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_exam_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_expel (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_external_service_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_final_exam_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_gender (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_grade_system_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_graduate_fields_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_graduate_inactive_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_grant_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_hemis_version_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_internship_form (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_internship_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_language_certificate (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_locality_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_methodical_publication_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_nationality (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_outside_activities (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_ownership (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_patient_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_payment_form (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_poverty_level (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_project_executor_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_project_locality (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_project_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_publication_database (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_publication_locality (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_publication_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_qualification (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_resource_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_scholar_database (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_scholarship_decree_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_science_branch (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_scientific_project_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_score_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_semester (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_semester_list (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_soato (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    parent_code                            TEXT,
    name_uz                                TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_speciality_bachelor (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_en                                TEXT,
    name_ru                                TEXT,
    active                                 BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_h_speciality_doctoral (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    active                                 BOOLEAN,
    name_ru                                TEXT,
    name_en                                TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_speciality_master (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_en                                TEXT,
    name_ru                                TEXT,
    active                                 BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_h_speciality_ordinatura (
    id                                     UUID,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_en                                TEXT,
    name_ru                                TEXT,
    active                                 BOOLEAN
);

CREATE TABLE IF NOT EXISTS hemishe_h_sport_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_stipend_rate (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_stipend_rate_category (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_student_achievement_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_student_living_status (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_student_room_mate_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_student_social_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_student_status_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_student_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_study_schedule_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_subject_block (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_subject_choose_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_subject_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_teacher_achievement_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_teacher_conduction_form (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_terrain (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    soato_code                             TEXT,
    "_soato"                               TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_transfer_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_activity_status (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_belongs_to (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_contract_category (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_department_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_employee_form (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_employee_rate (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_employee_status_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_employee_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_university_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_verification_type (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_h_workplace_compatibility (
    code                                   TEXT PRIMARY KEY,
    name                                   TEXT,
    name_ru                                TEXT,
    name_en                                TEXT,
    active                                 BOOLEAN,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_r_academic_attendance (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code                        TEXT,
    university_name                        TEXT,
    faculty_code                           TEXT,
    faculty_name                           TEXT,
    education_type_code                    TEXT,
    education_type_name                    TEXT,
    education_year_code                    TEXT,
    education_year_name                    TEXT,
    semester_type_code                     TEXT,
    semester_type_name                     TEXT,
    course_code                            TEXT,
    course_name                            TEXT,
    update_date                            DATE,
    attendance_percent                     DOUBLE PRECISION,
    bad_attendance_student_count           INTEGER,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_r_academic_group (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code                        TEXT,
    university_name                        TEXT,
    education_type_code                    TEXT,
    education_type_name                    TEXT,
    education_form_code                    TEXT,
    education_form_name                    TEXT,
    education_year_code                    TEXT,
    education_year_name                    TEXT,
    group_count                            INTEGER,
    update_date                            DATE,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_r_academic_score (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    university_code                        TEXT,
    university_name                        TEXT,
    faculty_code                           TEXT,
    faculty_name                           TEXT,
    education_type_code                    TEXT,
    education_type_name                    TEXT,
    education_year_code                    TEXT,
    education_year_name                    TEXT,
    semester_type_code                     TEXT,
    semester_type_name                     TEXT,
    course_code                            TEXT,
    course_name                            TEXT,
    table_type                             TEXT,
    score_percent                          DOUBLE PRECISION,
    score_type                             TEXT,
    debitor_count                          DOUBLE PRECISION,
    update_date                            DATE
);

CREATE TABLE IF NOT EXISTS hemishe_r_academic_subjects (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code                        TEXT,
    university_name                        TEXT,
    education_type_code                    TEXT,
    education_type_name                    TEXT,
    education_year_code                    TEXT,
    education_year_name                    TEXT,
    curriculum_code                        TEXT,
    curriculum_name                        TEXT,
    block_code                             TEXT,
    block_name                             TEXT,
    subject_count                          INTEGER,
    update_date                            DATE,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_r_contract_statistics (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_date"                                DATE,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_education_type"                      TEXT,
    "_education_form"                      TEXT,
    "_faculty"                             TEXT,
    "_course"                              TEXT,
    "_semester"                            TEXT,
    daily_count                            INTEGER,
    total                                  INTEGER
);

CREATE TABLE IF NOT EXISTS hemishe_r_education_materials (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    university_code                        TEXT,
    education_year_code                    TEXT,
    speciality_id                          TEXT,
    speciality_code                        TEXT,
    speciality_name                        TEXT,
    subject_count                          INTEGER,
    textbooks_count                        INTEGER,
    created_materials_grade                INTEGER
);

CREATE TABLE IF NOT EXISTS hemishe_r_employment (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    u_id                                   BIGINT,
    qty                                    INTEGER,
    "_university"                          TEXT,
    "_department"                          TEXT,
    "_education_year"                      TEXT,
    "_education_type"                      TEXT,
    "_education_form"                      TEXT,
    "_payment_form"                        TEXT,
    "_gender"                              TEXT,
    "_workplace_compatibility"             TEXT,
    "_graduate_fields_type"                TEXT,
    "_graduate_inactive_type"              TEXT,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    name                                   TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_r_expel (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code                        TEXT,
    university_name                        TEXT,
    faculty_code                           TEXT,
    faculty_name                           TEXT,
    education_type_code                    TEXT,
    education_type_name                    TEXT,
    education_year_code                    TEXT,
    education_year_name                    TEXT,
    semester_type_code                     TEXT,
    semester_type_name                     TEXT,
    course_code                            TEXT,
    course_name                            TEXT,
    update_date                            DATE,
    expel_reason_code                      TEXT,
    expel_reason_name                      TEXT,
    expel_count                            INTEGER,
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_r_ict_equipment (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    university_code                        TEXT,
    education_year_code                    TEXT,
    room_count                             INTEGER,
    valid_projector_count                  INTEGER,
    invalid_projector_count                INTEGER,
    total_count                            INTEGER,
    total_grade                            INTEGER
);

CREATE TABLE IF NOT EXISTS hemishe_r_laboratories (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    university_code                        TEXT,
    education_year_code                    TEXT,
    speciality_id                          TEXT,
    speciality_code                        TEXT,
    speciality_name                        TEXT,
    student_count                          INTEGER,
    valid_laboratories_count               INTEGER,
    valid_workshops_count                  INTEGER,
    invalid_laboratories_count             INTEGER,
    invalid_workshops_count                INTEGER,
    total_laboratories                     INTEGER,
    total_workshops                        INTEGER,
    total_grade                            INTEGER,
    name                                   TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_ri_academic_educational_work (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    speciality_code                        TEXT,
    speciality_name                        TEXT,
    document                               TEXT,
    subjects                               TEXT,
    language_name                          TEXT,
    "_course"                              TEXT,
    student_count                          INTEGER
);

CREATE TABLE IF NOT EXISTS hemishe_ri_academic_methodologic_publications (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    author_fullname                        TEXT,
    speciality_code                        TEXT,
    speciality_name                        TEXT,
    book_type                              TEXT,
    book_name                              TEXT,
    certificate_date                       DATE,
    certificate_number                     TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_employee1 (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_employee"                            UUID,
    "_country"                             TEXT,
    foreign_university                     TEXT,
    "_degree"                              TEXT,
    "_rank"                                TEXT,
    diploma_type                           TEXT,
    diploma_serial_number                  TEXT,
    diploma_date                           DATE,
    speciality_code                        TEXT,
    speciality_name                        TEXT,
    council_date                           DATE,
    council_number                         TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_employee2 (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_employee"                            TEXT,
    "_country"                             TEXT,
    "_internship_form"                     TEXT,
    "_internship_type"                     TEXT,
    foreign_university                     TEXT,
    speciality_code                        TEXT,
    speciality_name                        TEXT,
    training_type_name                     TEXT,
    training_contract                      TEXT,
    training_date_start                    DATE,
    training_date_end                      DATE,
    "year_"                                TEXT,
    subject                                TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_employee3 (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_country"                             TEXT,
    fullname                               TEXT,
    work_place                             TEXT,
    speciality_name                        TEXT,
    subject                                TEXT,
    contract_data                          TEXT,
    "_employee"                            UUID,
    "_employee_form"                       TEXT,
    "_condution_form"                      TEXT,
    arrival_date                           DATE,
    departure_date                         DATE,
    lesson_time                            INTEGER,
    "year_"                                TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_sport_facilities (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    square                                 DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_student2 (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    exchange_document                      TEXT,
    student_fullname                       TEXT,
    "_country"                             TEXT,
    exchange_university_name               TEXT,
    "_education_type"                      TEXT,
    speciality_code                        TEXT,
    speciality_name                        TEXT,
    exchange_type                          TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_student3 (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          TEXT,
    "_education_year"                      TEXT,
    "_student"                             UUID,
    company                                TEXT,
    "position_"                            TEXT,
    masters_university_name                TEXT,
    "_education_type"                      TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_student4 (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          UUID,
    "_education_year"                      UUID,
    "_country"                             UUID,
    "_student"                             UUID,
    olimpiada_type                         TEXT,
    olimpiada_place                        TEXT,
    olimpiada_name                         TEXT,
    olimpiada_section_name                 TEXT,
    olimpiada_place_date                   TEXT,
    olimpiada_subject                      TEXT,
    taken_position                         TEXT,
    diploma_serial                         TEXT,
    diploma_number                         TEXT
);

CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_student_sport (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                                INTEGER,
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    "_university"                          UUID,
    "_education_year"                      UUID,
    "_student"                             UUID,
    "_sport_type"                          UUID,
    sport_date                             DATE,
    sport_type_rank                        TEXT,
    sport_type_rank_document               TEXT
);

CREATE TABLE IF NOT EXISTS sec_user (
    id                                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    create_ts                              TIMESTAMP,
    created_by                             TEXT,
    version                                INTEGER,
    update_ts                              TIMESTAMP,
    updated_by                             TEXT,
    delete_ts                              TIMESTAMP,
    deleted_by                             TEXT,
    login                                  TEXT,
    login_lc                               TEXT,
    password                               TEXT,
    password_encryption                    TEXT,
    name                                   TEXT,
    first_name                             TEXT,
    last_name                              TEXT,
    middle_name                            TEXT,
    "position_"                            TEXT,
    email                                  TEXT,
    "language_"                            TEXT,
    time_zone                              TEXT,
    time_zone_auto                         BOOLEAN,
    active                                 BOOLEAN,
    group_id                               UUID,
    ip_mask                                TEXT,
    change_password_at_logon               BOOLEAN,
    group_names                            TEXT,
    sys_tenant_id                          TEXT,
    dtype                                  TEXT,
    "_university"                          TEXT
);

-- --- OTM kodlari (98 ta) — S018 seed'i uchun FK ma'lumot bazasi ---
-- S018_seed_speciality_attachment_2026 fk_univ_spec_attach_univ orqali
-- hemishe_e_university(code) ga bog'lanadi; bo'sh jadval bilan seed FK buzilishi
-- bilan to'xtaydi. Kodlar seed'ning O'ZIDAN olinadi (generator).
--
-- version = 0 MAJBURIY: University.version @Version (optimistic locking). NULL version
-- bilan Hibernate qatorni TRANSIENT deb hisoblaydi va unga bog'langan oauth_client
-- saqlanganda "references an unsaved transient instance" bilan yiqiladi.
INSERT INTO hemishe_e_university (code, version, name) VALUES
    ('301', 0, 'OTM 301'), ('302', 0, 'OTM 302'), ('304', 0, 'OTM 304'), ('306', 0, 'OTM 306'), ('307', 0, 'OTM 307'), ('308', 0, 'OTM 308'), ('309', 0, 'OTM 309'), ('312', 0, 'OTM 312'),
    ('313', 0, 'OTM 313'), ('314', 0, 'OTM 314'), ('315', 0, 'OTM 315'), ('316', 0, 'OTM 316'), ('318', 0, 'OTM 318'), ('319', 0, 'OTM 319'), ('320', 0, 'OTM 320'), ('322', 0, 'OTM 322'),
    ('323', 0, 'OTM 323'), ('324', 0, 'OTM 324'), ('325', 0, 'OTM 325'), ('326', 0, 'OTM 326'), ('327', 0, 'OTM 327'), ('328', 0, 'OTM 328'), ('329', 0, 'OTM 329'), ('330', 0, 'OTM 330'),
    ('331', 0, 'OTM 331'), ('334', 0, 'OTM 334'), ('337', 0, 'OTM 337'), ('338', 0, 'OTM 338'), ('340', 0, 'OTM 340'), ('341', 0, 'OTM 341'), ('342', 0, 'OTM 342'), ('344', 0, 'OTM 344'),
    ('345', 0, 'OTM 345'), ('346', 0, 'OTM 346'), ('347', 0, 'OTM 347'), ('348', 0, 'OTM 348'), ('349', 0, 'OTM 349'), ('350', 0, 'OTM 350'), ('351', 0, 'OTM 351'), ('352', 0, 'OTM 352'),
    ('353', 0, 'OTM 353'), ('354', 0, 'OTM 354'), ('355', 0, 'OTM 355'), ('357', 0, 'OTM 357'), ('358', 0, 'OTM 358'), ('361', 0, 'OTM 361'), ('362', 0, 'OTM 362'), ('363', 0, 'OTM 363'),
    ('365', 0, 'OTM 365'), ('368', 0, 'OTM 368'), ('369', 0, 'OTM 369'), ('371', 0, 'OTM 371'), ('372', 0, 'OTM 372'), ('373', 0, 'OTM 373'), ('376', 0, 'OTM 376'), ('377', 0, 'OTM 377'),
    ('378', 0, 'OTM 378'), ('380', 0, 'OTM 380'), ('382', 0, 'OTM 382'), ('383', 0, 'OTM 383'), ('384', 0, 'OTM 384'), ('385', 0, 'OTM 385'), ('386', 0, 'OTM 386'), ('387', 0, 'OTM 387'),
    ('393', 0, 'OTM 393'), ('395', 0, 'OTM 395'), ('396', 0, 'OTM 396'), ('398', 0, 'OTM 398'), ('399', 0, 'OTM 399'), ('401', 0, 'OTM 401'), ('402', 0, 'OTM 402'), ('403', 0, 'OTM 403'),
    ('406', 0, 'OTM 406'), ('414', 0, 'OTM 414'), ('443', 0, 'OTM 443'), ('446', 0, 'OTM 446'), ('450', 0, 'OTM 450'), ('451', 0, 'OTM 451'), ('452', 0, 'OTM 452'), ('453', 0, 'OTM 453'),
    ('454', 0, 'OTM 454'), ('510', 0, 'OTM 510'), ('512', 0, 'OTM 512'), ('515', 0, 'OTM 515'), ('521', 0, 'OTM 521'), ('523', 0, 'OTM 523'), ('528', 0, 'OTM 528'), ('537', 0, 'OTM 537'),
    ('538', 0, 'OTM 538'), ('539', 0, 'OTM 539'), ('540', 0, 'OTM 540'), ('541', 0, 'OTM 541'), ('542', 0, 'OTM 542'), ('543', 0, 'OTM 543'), ('546', 0, 'OTM 546'), ('547', 0, 'OTM 547'),
    ('561', 0, 'OTM 561'), ('562', 0, 'OTM 562')
ON CONFLICT (code) DO NOTHING;
