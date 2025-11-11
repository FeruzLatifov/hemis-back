-- Flyway Migration: Create Administrative Tables
-- Version: V6
-- Description: Create RIAdministrative tables for students and employees

-- Table: hemishe_ri_administrative_student2
-- Description: Foreign university academic exchange programs (by students)
CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_student2 (
    id UUID PRIMARY KEY,
    _university UUID NOT NULL,
    _education_year UUID NOT NULL,
    exchange_document VARCHAR(255),
    student_fullname VARCHAR(255),
    _country UUID,
    exchange_university_name VARCHAR(255),
    _education_type UUID,
    speciality_code VARCHAR(255),
    speciality_name VARCHAR(2048),
    exchange_type VARCHAR(255),
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 0
);

-- Table: hemishe_ri_administrative_student3
-- Description: Bachelor's graduate employment information (within 6 months after graduation)
CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_student3 (
    id UUID PRIMARY KEY,
    _university UUID NOT NULL,
    _education_year UUID NOT NULL,
    _student UUID,
    company VARCHAR(2048),
    position_ VARCHAR(255),
    masters_university_name VARCHAR(1048),
    _education_type UUID,
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 0
);

-- Table: hemishe_ri_administrative_student4
-- Description: Students who won awards in international olympiads, prestigious competitions and sports competitions
CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_student4 (
    id UUID PRIMARY KEY,
    _university UUID NOT NULL,
    _education_year UUID NOT NULL,
    _country UUID,
    _student UUID,
    olimpiada_type VARCHAR(255),
    olimpiada_place VARCHAR(255),
    olimpiada_name VARCHAR(1024),
    olimpiada_section_name VARCHAR(255),
    olimpiada_place_date VARCHAR(255),
    olimpiada_subject VARCHAR(255),
    taken_position VARCHAR(255),
    diploma_serial VARCHAR(255),
    diploma_number VARCHAR(255),
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 0
);

-- Table: hemishe_ri_administrative_student_sport
-- Description: Student sports achievements and rankings
CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_student_sport (
    id UUID PRIMARY KEY,
    _university UUID NOT NULL,
    _education_year UUID NOT NULL,
    _student UUID,
    _sport_type UUID,
    sport_date DATE,
    sport_type_rank VARCHAR(255),
    sport_type_rank_document VARCHAR(255),
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 0
);

-- Table: hemishe_ri_administrative_employee1
-- Description: Teachers with PhD or DSc degrees from prestigious top-1000 universities in the world
CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_employee1 (
    id UUID PRIMARY KEY,
    _university UUID NOT NULL,
    _education_year UUID NOT NULL,
    _employee UUID,
    _country UUID,
    foreign_university VARCHAR(1024),
    _degree UUID,
    _rank UUID,
    diploma_type VARCHAR(255),
    diploma_serial_number VARCHAR(255),
    diploma_date DATE,
    speciality_code VARCHAR(255),
    speciality_name VARCHAR(1024),
    council_date DATE,
    council_number VARCHAR(255),
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 0
);

-- Table: hemishe_ri_administrative_employee2
-- Description: Professors teaching at prestigious top-1000 universities in the world during the rating year
CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_employee2 (
    id UUID PRIMARY KEY,
    _university UUID NOT NULL,
    _education_year UUID NOT NULL,
    _employee UUID,
    _country UUID,
    foreign_university VARCHAR(1024),
    speciality_code VARCHAR(255),
    speciality_name VARCHAR(1024),
    training_type_name VARCHAR(512),
    training_contract VARCHAR(512),
    training_date_start DATE,
    training_date_end DATE,
    year_ VARCHAR(255),
    subject LONGTEXT,
    _internship_form UUID,
    _internship_type UUID,
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 0
);

-- Table: hemishe_ri_administrative_employee3
-- Description: Professors with DSc degree or professor title without scientific degree
CREATE TABLE IF NOT EXISTS hemishe_ri_administrative_employee3 (
    id UUID PRIMARY KEY,
    _university UUID NOT NULL,
    _education_year UUID NOT NULL,
    _country UUID,
    fullname VARCHAR(255),
    work_place VARCHAR(255),
    speciality_name VARCHAR(1024),
    subject VARCHAR(512),
    contract_data VARCHAR(512),
    _employee UUID,
    _employee_form UUID,
    _condution_form UUID,
    arrival_date DATE,
    departure_date DATE,
    lesson_time INTEGER,
    year_ VARCHAR(255),
    create_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 0
);

-- Create indexes for primary keys and foreign keys
CREATE INDEX IF NOT EXISTS idx_ri_admin_student2_university ON hemishe_ri_administrative_student2(_university);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student2_education_year ON hemishe_ri_administrative_student2(_education_year);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student2_country ON hemishe_ri_administrative_student2(_country);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student2_education_type ON hemishe_ri_administrative_student2(_education_type);

CREATE INDEX IF NOT EXISTS idx_ri_admin_student3_university ON hemishe_ri_administrative_student3(_university);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student3_education_year ON hemishe_ri_administrative_student3(_education_year);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student3_student ON hemishe_ri_administrative_student3(_student);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student3_education_type ON hemishe_ri_administrative_student3(_education_type);

CREATE INDEX IF NOT EXISTS idx_ri_admin_student4_university ON hemishe_ri_administrative_student4(_university);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student4_education_year ON hemishe_ri_administrative_student4(_education_year);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student4_country ON hemishe_ri_administrative_student4(_country);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student4_student ON hemishe_ri_administrative_student4(_student);

CREATE INDEX IF NOT EXISTS idx_ri_admin_student_sport_university ON hemishe_ri_administrative_student_sport(_university);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student_sport_education_year ON hemishe_ri_administrative_student_sport(_education_year);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student_sport_student ON hemishe_ri_administrative_student_sport(_student);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student_sport_sport_type ON hemishe_ri_administrative_student_sport(_sport_type);

CREATE INDEX IF NOT EXISTS idx_ri_admin_employee1_university ON hemishe_ri_administrative_employee1(_university);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee1_education_year ON hemishe_ri_administrative_employee1(_education_year);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee1_employee ON hemishe_ri_administrative_employee1(_employee);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee1_country ON hemishe_ri_administrative_employee1(_country);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee1_degree ON hemishe_ri_administrative_employee1(_degree);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee1_rank ON hemishe_ri_administrative_employee1(_rank);

CREATE INDEX IF NOT EXISTS idx_ri_admin_employee2_university ON hemishe_ri_administrative_employee2(_university);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee2_education_year ON hemishe_ri_administrative_employee2(_education_year);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee2_employee ON hemishe_ri_administrative_employee2(_employee);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee2_country ON hemishe_ri_administrative_employee2(_country);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee2_internship_form ON hemishe_ri_administrative_employee2(_internship_form);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee2_internship_type ON hemishe_ri_administrative_employee2(_internship_type);

CREATE INDEX IF NOT EXISTS idx_ri_admin_employee3_university ON hemishe_ri_administrative_employee3(_university);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee3_education_year ON hemishe_ri_administrative_employee3(_education_year);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee3_country ON hemishe_ri_administrative_employee3(_country);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee3_employee ON hemishe_ri_administrative_employee3(_employee);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee3_employee_form ON hemishe_ri_administrative_employee3(_employee_form);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee3_condution_form ON hemishe_ri_administrative_employee3(_condution_form);

-- Create indexes on audit fields for better query performance
CREATE INDEX IF NOT EXISTS idx_ri_admin_student2_created_by ON hemishe_ri_administrative_student2(created_by);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student3_created_by ON hemishe_ri_administrative_student3(created_by);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student4_created_by ON hemishe_ri_administrative_student4(created_by);
CREATE INDEX IF NOT EXISTS idx_ri_admin_student_sport_created_by ON hemishe_ri_administrative_student_sport(created_by);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee1_created_by ON hemishe_ri_administrative_employee1(created_by);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee2_created_by ON hemishe_ri_administrative_employee2(created_by);
CREATE INDEX IF NOT EXISTS idx_ri_admin_employee3_created_by ON hemishe_ri_administrative_employee3(created_by);
