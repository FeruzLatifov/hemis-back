-- ================================================
-- Migration: V8 - Seed Menu Data (CORRECTED)
-- Description: Insert all 178 menu items from MenuConfig.java
-- Author: System Generated (FIXED parent-child relationships)
-- ================================================

-- dashboard
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'dc7161be-3dbf-2250-c895-4e560cc35060',
    'dashboard',
    'menu.dashboard',
    '/dashboard',
    'home',
    'dashboard.view',
    1,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'a9205dcf-d4a6-f7c2-cbe8-be01566ff84a',
    'registry',
    'menu.registry',
    '/registry',
    'database',
    'registry.view',
    2,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    'registry-e-reestr',
    'menu.registry.e_reestr',
    '/registry/e-reestr',
    'database',
    'registry.e-reestr.view',
    1,
    true,
    'a9205dcf-d4a6-f7c2-cbe8-be01566ff84a',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    'registry-scientific',
    'menu.registry.scientific',
    '/registry/scientific',
    'graduation-cap',
    'registry.scientific.view',
    2,
    true,
    'a9205dcf-d4a6-f7c2-cbe8-be01566ff84a',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-student-meta
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'ec88950e-8210-4bb6-034f-9afd87b1dbdd',
    'registry-student-meta',
    'menu.registry.student_meta',
    '/registry/student-meta',
    'user-circle',
    'registry.student-meta.view',
    3,
    true,
    'a9205dcf-d4a6-f7c2-cbe8-be01566ff84a',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '2c5504ab-9a86-164d-b22a-92dc8793843d',
    'rating',
    'menu.rating',
    '/rating',
    'line-chart',
    'rating.view',
    3,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-university
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '65b5dc40-2cb8-ad8e-c078-f7d4f3799a42',
    'registry-e-reestr-university',
    'menu.registry.e_reestr.university',
    '/registry/e-reestr/university',
    'building',
    'registry.e-reestr.view',
    1,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-doctorate
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'fe1dc8bc-df7d-a503-4510-c6d206ef7643',
    'registry-scientific-doctorate',
    'menu.registry.scientific.doctorate',
    '/registry/scientific/doctorate',
    'user-graduate',
    'registry.scientific.view',
    1,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-administrative
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '7c2c8fa4-97ce-df6a-90b9-5d9e7e064f4f',
    'rating-administrative',
    'menu.rating.administrative',
    '/rating/administrative',
    'building',
    'rating.administrative.view',
    1,
    true,
    '2c5504ab-9a86-164d-b22a-92dc8793843d',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-faculty
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'a87a5f4e-b51c-7526-7c07-c24ae79339e0',
    'registry-e-reestr-faculty',
    'menu.registry.e_reestr.faculty',
    '/registry/e-reestr/faculty',
    'school',
    'registry.e-reestr.view',
    2,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-dissertation
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '4eabb9f2-7784-061b-af21-7db8df69e703',
    'registry-scientific-dissertation',
    'menu.registry.scientific.dissertation',
    '/registry/scientific/dissertation',
    'file-text',
    'registry.scientific.view',
    2,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-academic
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '6f2b5058-fe91-5fd1-752f-4b78ea77687a',
    'rating-academic',
    'menu.rating.academic',
    '/rating/academic',
    'layout-dashboard',
    'rating.academic.view',
    2,
    true,
    '2c5504ab-9a86-164d-b22a-92dc8793843d',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-cathedra
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '654fc9a5-553d-5b15-dc65-fdbd79d7ffcc',
    'registry-e-reestr-cathedra',
    'menu.registry.e_reestr.cathedra',
    '/registry/e-reestr/cathedra',
    'users',
    'registry.e-reestr.view',
    3,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-project
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'e8e6cb01-4d5c-189b-d4fa-a6f65c4934ed',
    'registry-scientific-project',
    'menu.registry.scientific.project',
    '/registry/scientific/project',
    'lightbulb',
    'registry.scientific.view',
    3,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-scientific
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '223fa885-1923-da4a-e99a-d745e612fe16',
    'rating-scientific',
    'menu.rating.scientific',
    '/rating/scientific',
    'graduation-cap',
    'rating.scientific.view',
    3,
    true,
    '2c5504ab-9a86-164d-b22a-92dc8793843d',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-teacher
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '5535c640-4b5e-a689-bff0-547e0f21e360',
    'registry-e-reestr-teacher',
    'menu.registry.e_reestr.teacher',
    '/registry/e-reestr/teacher',
    'user-check',
    'registry.e-reestr.view',
    4,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-executor
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '18df3aed-0f78-324b-aaed-f7bdb083d3bd',
    'registry-scientific-executor',
    'menu.registry.scientific.executor',
    '/registry/scientific/executor',
    'users',
    'registry.scientific.view',
    4,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-student-gpa
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '0a6e5071-a663-3380-9cc0-370f25bacc0e',
    'rating-student-gpa',
    'menu.rating.student_gpa',
    '/rating/student-gpa',
    'award',
    'rating.student-gpa.view',
    4,
    true,
    '2c5504ab-9a86-164d-b22a-92dc8793843d',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    'data',
    'menu.data',
    '/data',
    'database',
    'data.view',
    4,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-student
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '577faf17-f497-919c-6fde-0f2c2c68221a',
    'registry-e-reestr-student',
    'menu.registry.e_reestr.student',
    '/registry/e-reestr/student',
    'graduation-cap',
    'registry.e-reestr.view',
    5,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-project-meta
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'dd70044a-3adb-92b2-6c0b-56b5528f993c',
    'registry-scientific-project-meta',
    'menu.registry.scientific.project_meta',
    '/registry/scientific/project-meta',
    'dollar-sign',
    'registry.scientific.view',
    5,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-diploma
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '6c3775ae-cea6-a347-6a6d-cfcbf953c546',
    'registry-e-reestr-diploma',
    'menu.registry.e_reestr.diploma',
    '/registry/e-reestr/diploma',
    'award',
    'registry.e-reestr.view',
    6,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-publication
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9cd84f5d-3142-9a1f-59ee-e9baceab7caf',
    'registry-scientific-publication',
    'menu.registry.scientific.publication',
    '/registry/scientific/publication',
    'book',
    'registry.scientific.view',
    6,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-speciality-bachelor
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '7a68f80a-a633-8e18-0e8a-dce6e149d32d',
    'registry-e-reestr-speciality-bachelor',
    'menu.registry.e_reestr.speciality_bachelor',
    '/registry/e-reestr/speciality-bachelor',
    'book-open',
    'registry.e-reestr.view',
    7,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-methodical
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'ed8a4265-2212-49bc-ddc7-41fc350bbc2c',
    'registry-scientific-methodical',
    'menu.registry.scientific.methodical',
    '/registry/scientific/methodical',
    'book-open',
    'registry.scientific.view',
    7,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-speciality-master
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'a08338b9-3142-a46a-aac4-2d3c71f962c1',
    'registry-e-reestr-speciality-master',
    'menu.registry.e_reestr.speciality_master',
    '/registry/e-reestr/speciality-master',
    'book-open',
    'registry.e-reestr.view',
    8,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-property
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'f3860d87-ecc2-c066-c63e-6e779f04d4f1',
    'registry-scientific-property',
    'menu.registry.scientific.property',
    '/registry/scientific/property',
    'briefcase',
    'registry.scientific.view',
    8,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-speciality-doctoral
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9b53806c-ef90-b776-4df4-51bf914e1661',
    'registry-e-reestr-speciality-doctoral',
    'menu.registry.e_reestr.speciality_doctoral',
    '/registry/e-reestr/speciality-doctoral',
    'book-open',
    'registry.e-reestr.view',
    9,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-author
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '2b81d337-f268-6ca1-d297-237e3190be8f',
    'registry-scientific-author',
    'menu.registry.scientific.author',
    '/registry/scientific/author',
    'edit',
    'registry.scientific.view',
    9,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-employee-jobs
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '3ee828e6-95a6-56ae-d9cc-493173b30483',
    'registry-e-reestr-employee-jobs',
    'menu.registry.e_reestr.employee_jobs',
    '/registry/e-reestr/employee-jobs',
    'briefcase',
    'registry.e-reestr.view',
    10,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-scientific-activity
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '50785afb-823c-06a0-a49e-c8b0a652c63d',
    'registry-scientific-activity',
    'menu.registry.scientific.activity',
    '/registry/scientific/activity',
    'activity',
    'registry.scientific.view',
    10,
    true,
    '63e32d7d-83bc-88d4-2fca-a7d812f27bbc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-diploma-blank-distribution
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'edf2319b-afcd-bf35-ac6b-4b6b50049e51',
    'registry-e-reestr-diploma-blank-distribution',
    'menu.registry.e_reestr.diploma_blank_distribution',
    '/registry/e-reestr/diploma-blank-distribution',
    'file-text',
    'registry.e-reestr.view',
    11,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-diploma-blank
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '36db38c4-3d50-0f37-0ad4-ffecfc07af5f',
    'registry-e-reestr-diploma-blank',
    'menu.registry.e_reestr.diploma_blank',
    '/registry/e-reestr/diploma-blank',
    'file-text',
    'registry.e-reestr.view',
    12,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-speciality-ordinatura
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9b2db2ee-c16c-d0d8-c513-5695e29c91ff',
    'registry-e-reestr-speciality-ordinatura',
    'menu.registry.e_reestr.speciality_ordinatura',
    '/registry/e-reestr/speciality-ordinatura',
    'book',
    'registry.e-reestr.view',
    13,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-university-speciality
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9db8db56-8152-a9fe-de0e-5428f9983fdd',
    'registry-e-reestr-university-speciality',
    'menu.registry.e_reestr.university_speciality',
    '/registry/e-reestr/university-speciality',
    'layers',
    'registry.e-reestr.view',
    14,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-university-group
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '6de894b9-5d73-6cf3-612b-96aeb9dbe84b',
    'registry-e-reestr-university-group',
    'menu.registry.e_reestr.university_group',
    '/registry/e-reestr/university-group',
    'users',
    'registry.e-reestr.view',
    15,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-student-scholarship
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '55561fd2-d55d-4fee-0f3a-83f68acf9fea',
    'registry-e-reestr-student-scholarship',
    'menu.registry.e_reestr.student_scholarship',
    '/registry/e-reestr/student-scholarship',
    'dollar-sign',
    'registry.e-reestr.view',
    16,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-student-certificate
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '3348c095-2ab4-59ab-69db-f74a19ac28b0',
    'registry-e-reestr-student-certificate',
    'menu.registry.e_reestr.student_certificate',
    '/registry/e-reestr/student-certificate',
    'award',
    'registry.e-reestr.view',
    17,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-employee-certificate
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'd99e2866-ff6d-3011-b621-27199d79e22d',
    'registry-e-reestr-employee-certificate',
    'menu.registry.e_reestr.employee_certificate',
    '/registry/e-reestr/employee-certificate',
    'award',
    'registry.e-reestr.view',
    18,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- registry-e-reestr-student-lite
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '8ceb744d-a9c3-281b-d2b6-cf563145dda8',
    'registry-e-reestr-student-lite',
    'menu.registry.e_reestr.student_lite',
    '/registry/e-reestr/student-lite',
    'user',
    'registry.e-reestr.view',
    19,
    true,
    '8f0ff925-04aa-33fa-99c3-16aa77a7f9cf',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-administrative-employee
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '00b271d5-ca9c-f9fa-5c13-d87ca8bc2956',
    'rating-administrative-employee',
    'menu.rating.administrative.employee',
    '/rating/administrative/employee',
    'users',
    'rating.administrative.employee.view',
    1,
    true,
    '7c2c8fa4-97ce-df6a-90b9-5d9e7e064f4f',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-academic-methodical
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '43f63593-81ef-ae11-1022-ce09250dc4a2',
    'rating-academic-methodical',
    'menu.rating.academic.methodical',
    '/rating/academic/methodical',
    'book',
    'rating.academic.methodical.view',
    1,
    true,
    '6f2b5058-fe91-5fd1-752f-4b78ea77687a',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-scientific-publications
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '89f40c46-5483-0e2b-24d5-c099f5570396',
    'rating-scientific-publications',
    'menu.rating.scientific.publications',
    '/rating/scientific/publications',
    'send',
    'rating.scientific.publications.view',
    1,
    true,
    '223fa885-1923-da4a-e99a-d745e612fe16',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    'data-general',
    'menu.data.general',
    '/data/general',
    'file-text',
    'data.general.view',
    1,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-administrative-students
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'f05d22f8-3edf-6562-53c3-652f343fc1fa',
    'rating-administrative-students',
    'menu.rating.administrative.students',
    '/rating/administrative/students',
    'graduation-cap',
    'rating.administrative.students.view',
    2,
    true,
    '7c2c8fa4-97ce-df6a-90b9-5d9e7e064f4f',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-academic-study
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '0bedb0fd-197c-b8fe-fbf9-33aa30de7ee2',
    'rating-academic-study',
    'menu.rating.academic.study',
    '/rating/academic/study',
    'book',
    'rating.academic.study.view',
    2,
    true,
    '6f2b5058-fe91-5fd1-752f-4b78ea77687a',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-scientific-projects
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '7d52ce4f-0d92-f10a-6dd2-7393eb20c0ae',
    'rating-scientific-projects',
    'menu.rating.scientific.projects',
    '/rating/scientific/projects',
    'lightbulb',
    'rating.scientific.projects.view',
    2,
    true,
    '223fa885-1923-da4a-e99a-d745e612fe16',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-structure
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '36397fe1-2f93-5090-ad15-0c6ce0c258d4',
    'data-structure',
    'menu.data.structure',
    '/data/structure',
    'share-2',
    'data.structure.view',
    2,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-administrative-sport
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '5fc14782-5f5b-57ab-e887-89e466c1cfcd',
    'rating-administrative-sport',
    'menu.rating.administrative.sport',
    '/rating/administrative/sport',
    'trophy',
    'rating.administrative.sport.view',
    3,
    true,
    '7c2c8fa4-97ce-df6a-90b9-5d9e7e064f4f',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-academic-verification
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '76371e67-6c5b-958a-e148-a815f5066744',
    'rating-academic-verification',
    'menu.rating.academic.verification',
    '/rating/academic/verification',
    'shield-check',
    'rating.academic.verification.view',
    3,
    true,
    '6f2b5058-fe91-5fd1-752f-4b78ea77687a',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- rating-scientific-intellectual
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '1b182072-8e09-9847-a4f4-047ec9749a9c',
    'rating-scientific-intellectual',
    'menu.rating.scientific.intellectual',
    '/rating/scientific/intellectual',
    'settings',
    'rating.scientific.intellectual.view',
    3,
    true,
    '223fa885-1923-da4a-e99a-d745e612fe16',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    'data-employee',
    'menu.data.employee',
    '/data/employee',
    'users',
    'data.employee.view',
    3,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    'data-student',
    'menu.data.student',
    '/data/student',
    'user-circle',
    'data.student.view',
    4,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    'data-education',
    'menu.data.education',
    '/data/education',
    'book',
    'data.education.view',
    5,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'a8445719-836f-2d5e-8b51-986410e14728',
    'reports',
    'menu.reports',
    '/reports',
    'bar-chart',
    'reports.view',
    5,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '24246231-3565-aec5-203e-ca5ee793e952',
    'data-study',
    'menu.data.study',
    '/data/study',
    'edit',
    'data.study.view',
    6,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    'data-science',
    'menu.data.science',
    '/data/science',
    'graduation-cap',
    'data.science.view',
    7,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    'data-organizational',
    'menu.data.organizational',
    '/data/organizational',
    'building-2',
    'data.organizational.view',
    8,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-contract-category
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'dbcc65bb-1fb8-bd5f-f1a8-362fd94b617f',
    'data-contract-category',
    'menu.data.contract_category',
    '/data/contract-category',
    'file-signature',
    'data.contract-category.view',
    9,
    true,
    '8d777f38-5d3d-fec8-815d-20f7496026dc',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-country
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '58e1a77d-f2da-be8f-ccdc-5360cea00fde',
    'data-general-country',
    'menu.data.general.country',
    '/data/general/country',
    'globe',
    'data.general.view',
    1,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-structure-university-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'cf94f8c6-0199-f021-472c-cabdc4556262',
    'data-structure-university-type',
    'menu.data.structure.university_type',
    '/data/structure/university-type',
    'building',
    'data.structure.view',
    1,
    true,
    '36397fe1-2f93-5090-ad15-0c6ce0c258d4',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'fbe9c91b-7bd9-478f-2fbe-78d64ed15deb',
    'data-employee-type',
    'menu.data.employee.type',
    '/data/employee/type',
    'user-check',
    'data.employee.view',
    1,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-status
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '7fbd59eb-038e-af05-afc9-41ae61eb5fb5',
    'data-student-status',
    'menu.data.student.status',
    '/data/student/status',
    'circle',
    'data.student.view',
    1,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '13c4ec5f-8223-8d72-7b91-ebe5f9fddd8b',
    'data-education-type',
    'menu.data.education.education_type',
    '/data/education/type',
    'book-open',
    'data.education.view',
    1,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-year
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '81d6db66-9856-1e68-5bca-1ac7cfcd017f',
    'data-study-year',
    'menu.data.study.year',
    '/data/study/year',
    'calendar',
    'data.study.view',
    1,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-project-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '019d35b0-043d-f07b-e580-456dc4e7e617',
    'data-science-project-type',
    'menu.data.science.project_type',
    '/data/science/project-type',
    'flask',
    'data.science.view',
    1,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-payment-form
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '68b51631-f27a-777b-9056-c83a69bbbb65',
    'data-organizational-payment-form',
    'menu.data.organizational.payment_form',
    '/data/organizational/payment-form',
    'credit-card',
    'data.organizational.view',
    1,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-universities
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '4b02066c-a61d-0351-d1e8-1231062053ce',
    'reports-universities',
    'menu.reports.universities',
    '/reports/universities',
    'building',
    'reports.universities.view',
    1,
    true,
    'a8445719-836f-2d5e-8b51-986410e14728',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-soato
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '870770d4-f007-1d42-4ca1-591a9f461eed',
    'data-general-soato',
    'menu.data.general.soato',
    '/data/general/soato',
    'map',
    'data.general.view',
    2,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-structure-ownership
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '2d7b9425-8f86-0a9b-8f14-aae42c1e9719',
    'data-structure-ownership',
    'menu.data.structure.ownership',
    '/data/structure/ownership',
    'building',
    'data.structure.view',
    2,
    true,
    '36397fe1-2f93-5090-ad15-0c6ce0c258d4',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-status
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '38eeea02-9613-7e87-629b-0e118a3248a0',
    'data-employee-status',
    'menu.data.employee.status',
    '/data/employee/status',
    'user-x',
    'data.employee.view',
    2,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-achievement
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '8d4941a6-5d19-c909-ae2c-91586c9e9d31',
    'data-student-achievement',
    'menu.data.student.achievement',
    '/data/student/achievement',
    'award',
    'data.student.view',
    2,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-form
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '3de8a052-472f-529d-740d-b5f453542016',
    'data-education-form',
    'menu.data.education.education_form',
    '/data/education/form',
    'book-open',
    'data.education.view',
    2,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-course
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '53049bd1-a249-ebc7-22f9-d879f7886977',
    'data-study-course',
    'menu.data.study.course',
    '/data/study/course',
    'book-open',
    'data.study.view',
    2,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-project-locality
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'ec674099-8ffc-5759-5bc5-47dbb259c18e',
    'data-science-project-locality',
    'menu.data.science.project_locality',
    '/data/science/project-locality',
    'map',
    'data.science.view',
    2,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-stipend-rate
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '51ea99a9-2e53-4b7a-d3dd-97c257ad7b63',
    'data-organizational-stipend-rate',
    'menu.data.organizational.stipend_rate',
    '/data/organizational/stipend-rate',
    'banknote',
    'data.organizational.view',
    2,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-employees
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '4caf0ef7-046d-b5e3-e366-cc80ef9d509d',
    'reports-employees',
    'menu.reports.employees',
    '/reports/employees',
    'users',
    'reports.employees.view',
    2,
    true,
    'a8445719-836f-2d5e-8b51-986410e14728',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-nationality
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '5844407e-3afb-4bf5-8986-af6778cb7ebf',
    'data-general-nationality',
    'menu.data.general.nationality',
    '/data/general/nationality',
    'flag',
    'data.general.view',
    3,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-structure-department-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '26254c81-5a0a-ae41-ae5b-d097b7e159ca',
    'data-structure-department-type',
    'menu.data.structure.department_type',
    '/data/structure/department-type',
    'building-2',
    'data.structure.view',
    3,
    true,
    '36397fe1-2f93-5090-ad15-0c6ce0c258d4',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-rate
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '529e7563-1a19-1b57-5882-c34250899d28',
    'data-employee-rate',
    'menu.data.employee.rate',
    '/data/employee/rate',
    'percent',
    'data.employee.view',
    3,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-expel
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'b584000c-7ab3-2629-8cba-71b434b8937b',
    'data-student-expel',
    'menu.data.student.expel',
    '/data/student/expel',
    'user-minus',
    'data.student.view',
    3,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-language
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '19256a0f-17b9-276e-4e2e-584e351ac130',
    'data-education-language',
    'menu.data.education.education_language',
    '/data/education/language',
    'languages',
    'data.education.view',
    3,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-semester
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'e3ab9fff-e34c-645b-1542-4fb232a30b19',
    'data-study-semester',
    'menu.data.study.semester',
    '/data/study/semester',
    'calendar-days',
    'data.study.view',
    3,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-currency
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'd73809d7-1131-20e2-7891-8bd3afbd7bc1',
    'data-science-currency',
    'menu.data.science.currency',
    '/data/science/currency',
    'dollar-sign',
    'data.science.view',
    3,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-stipend-category
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'b1606d38-d703-6a7f-130c-e8ca00eb3f45',
    'data-organizational-stipend-category',
    'menu.data.organizational.stipend_category',
    '/data/organizational/stipend-category',
    'tag',
    'data.organizational.view',
    3,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-students
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'a3e459bd-e146-badb-01f6-807e5d8f3851',
    'reports-students',
    'menu.reports.students',
    '/reports/students',
    'graduation-cap',
    'reports.students.view',
    3,
    true,
    'a8445719-836f-2d5e-8b51-986410e14728',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-citizenship
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'f5d43dbb-d9e2-75ca-3e7a-d91abe027433',
    'data-general-citizenship',
    'menu.data.general.citizenship',
    '/data/general/citizenship',
    'passport',
    'data.general.view',
    4,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-structure-locality-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '8ba45abd-b16a-0875-adf3-d24c523cd0c1',
    'data-structure-locality-type',
    'menu.data.structure.locality_type',
    '/data/structure/locality-type',
    'map-pin',
    'data.structure.view',
    4,
    true,
    '36397fe1-2f93-5090-ad15-0c6ce0c258d4',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-form
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '02336965-f7b7-a2b4-0657-0b1dcd5c3afe',
    'data-employee-form',
    'menu.data.employee.form',
    '/data/employee/form',
    'file-text',
    'data.employee.view',
    4,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-accomodation
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '0eb8600c-698c-e246-2089-2f6763cf53b1',
    'data-student-accomodation',
    'menu.data.student.accomodation',
    '/data/student/accomodation',
    'home',
    'data.student.view',
    4,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-grade-system
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '0a6ac5f1-8783-1aee-2488-02346f2512de',
    'data-education-grade-system',
    'menu.data.education.grade_system_type',
    '/data/education/grade-system',
    'award',
    'data.education.view',
    4,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-week-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'c19f18ce-9705-1c2b-17a9-a91342a457cc',
    'data-study-week-type',
    'menu.data.study.week_type',
    '/data/study/week-type',
    'calendar-range',
    'data.study.view',
    4,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-executor-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'e750d22f-8920-c36d-ab88-04c5da6c9791',
    'data-science-executor-type',
    'menu.data.science.executor_type',
    '/data/science/executor-type',
    'users',
    'data.science.view',
    4,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-scholarship-decree
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'ebb587d1-a37e-85a1-f539-b6100db74028',
    'data-organizational-scholarship-decree',
    'menu.data.organizational.scholarship_decree',
    '/data/organizational/scholarship-decree',
    'file-text',
    'data.organizational.view',
    4,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-academic
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '06c8d6cf-d8e9-83ab-8fb3-a620b6da50c6',
    'reports-academic',
    'menu.reports.academic',
    '/reports/academic',
    'book',
    'reports.academic.view',
    4,
    true,
    'a8445719-836f-2d5e-8b51-986410e14728',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-gender
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'ec9ddc6f-d092-82ac-8821-b129837c1f51',
    'data-general-gender',
    'menu.data.general.gender',
    '/data/general/gender',
    'user',
    'data.general.view',
    5,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-structure-activity-status
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '5f695dc5-ea7d-415c-2085-61f914909f1e',
    'data-structure-activity-status',
    'menu.data.structure.activity_status',
    '/data/structure/activity-status',
    'activity',
    'data.structure.view',
    5,
    true,
    '36397fe1-2f93-5090-ad15-0c6ce0c258d4',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-position-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '29129d2d-45d3-e413-28ed-5142fe993c3e',
    'data-employee-position-type',
    'menu.data.employee.position_type',
    '/data/employee/position-type',
    'briefcase',
    'data.employee.view',
    5,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-doctoral-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'b0198d7a-4e24-9384-f07d-50fae3e5f668',
    'data-student-doctoral-type',
    'menu.data.student.doctoral_type',
    '/data/student/doctoral-type',
    'user-graduate',
    'data.student.view',
    5,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-score-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '10ae5415-6b24-0721-69bf-1d3a39fa5890',
    'data-education-score-type',
    'menu.data.education.score_type',
    '/data/education/score-type',
    'star',
    'data.education.view',
    5,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-subject-block
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'e2770ff2-1a2d-fa8c-1a3c-cf850633619a',
    'data-study-subject-block',
    'menu.data.study.subject_block',
    '/data/study/subject-block',
    'grid-3x3',
    'data.study.view',
    5,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-publication-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'a4da0d2d-6ab9-7a98-499b-6491ff6a2a29',
    'data-science-publication-type',
    'menu.data.science.publication_type',
    '/data/science/publication-type',
    'book',
    'data.science.view',
    5,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-contract-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '309749f8-aa9d-4dfa-3116-9847dd322920',
    'data-organizational-contract-type',
    'menu.data.organizational.contract_type',
    '/data/organizational/contract-type',
    'file-contract',
    'data.organizational.view',
    5,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-research
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '0b87bfc4-2e39-39d1-bf50-986d27ea18c2',
    'reports-research',
    'menu.reports.research',
    '/reports/research',
    'flask',
    'reports.research.view',
    5,
    true,
    'a8445719-836f-2d5e-8b51-986410e14728',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-bachelor-specialty
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '860d4ecb-92bb-b785-96fa-a1b77215dfe9',
    'data-general-bachelor-specialty',
    'menu.data.general.bachelor_specialty',
    '/data/general/bachelor-specialty',
    'graduation-cap',
    'data.general.view',
    6,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-structure-belongs-to
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9022c010-93a5-8400-08fc-1858f7d630b2',
    'data-structure-belongs-to',
    'menu.data.structure.belongs_to',
    '/data/structure/belongs-to',
    'link',
    'data.structure.view',
    6,
    true,
    '36397fe1-2f93-5090-ad15-0c6ce0c258d4',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-qualification
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '955d8b8e-7e94-6a40-1e8f-461fa7bc7ca5',
    'data-employee-qualification',
    'menu.data.employee.qualification',
    '/data/employee/qualification',
    'award',
    'data.employee.view',
    6,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-social-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9672bae3-0a2d-23a5-307d-b209001355ac',
    'data-student-social-type',
    'menu.data.student.social_type',
    '/data/student/social-type',
    'users',
    'data.student.view',
    6,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-exam-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '129a76a0-080e-3913-d2d8-6482ac22f664',
    'data-education-exam-type',
    'menu.data.education.exam_type',
    '/data/education/exam-type',
    'clipboard',
    'data.education.view',
    6,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-subject-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '36e4314e-5a3b-4bcf-e030-2068440d48a9',
    'data-study-subject-type',
    'menu.data.study.subject_type',
    '/data/study/subject-type',
    'list',
    'data.study.view',
    6,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-methodical-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '137db14b-b719-8cdb-904e-da759e341d27',
    'data-science-methodical-type',
    'menu.data.science.methodical_type',
    '/data/science/methodical-type',
    'book-open',
    'data.science.view',
    6,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-contract-summa
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'dcbdc5b3-1347-52ed-a3d5-e50e4e2dc77e',
    'data-organizational-contract-summa',
    'menu.data.organizational.contract_summa',
    '/data/organizational/contract-summa',
    'dollar-sign',
    'data.organizational.view',
    6,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-economic
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '275c2574-141e-a5fd-f55c-9f277feaf7a6',
    'reports-economic',
    'menu.reports.economic',
    '/reports/economic',
    'banknote',
    'reports.economic.view',
    6,
    true,
    'a8445719-836f-2d5e-8b51-986410e14728',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- system
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '54b53072-540e-eeb8-f8e9-343e71f28176',
    'system',
    'menu.system',
    '/system',
    'settings',
    'system.view',
    6,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-master-specialty
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '553829a6-eeab-1205-b8b9-2ce24722bac0',
    'data-general-master-specialty',
    'menu.data.general.master_specialty',
    '/data/general/master-specialty',
    'award',
    'data.general.view',
    7,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-achievement
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '21737633-87a0-1f85-ef0e-8e023407cdda',
    'data-employee-achievement',
    'menu.data.employee.achievement',
    '/data/employee/achievement',
    'star',
    'data.employee.view',
    7,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-academic-reason
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '2b3f8cee-602c-fecb-04a6-0504038c0063',
    'data-student-academic-reason',
    'menu.data.student.academic_reason',
    '/data/student/academic-reason',
    'file-text',
    'data.student.view',
    7,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-diploma-blank-category
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '3708ab04-c5d4-ccbb-fb97-c3660a0cbd34',
    'data-education-diploma-blank-category',
    'menu.data.education.diploma_blank_category',
    '/data/education/diploma-blank-category',
    'file-text',
    'data.education.view',
    7,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-class-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'bd7a3f14-dade-003c-0080-83c4d63fddaf',
    'data-study-class-type',
    'menu.data.study.class_type',
    '/data/study/class-type',
    'presentation',
    'data.study.view',
    7,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-patent-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '344efc36-d317-e320-0cd2-b780ecf26d62',
    'data-science-patent-type',
    'menu.data.science.patent_type',
    '/data/science/patent-type',
    'file-badge',
    'data.science.view',
    7,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-contract-types
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '0acf15c1-ecba-ed51-bf6b-086b59b76d39',
    'data-organizational-contract-types',
    'menu.data.organizational.contract_types',
    '/data/organizational/contract-types',
    'list',
    'data.organizational.view',
    7,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-doctoral-specialty
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '48144384-bf81-d706-01b4-12c72971d721',
    'data-general-doctoral-specialty',
    'menu.data.general.doctoral_specialty',
    '/data/general/doctoral-specialty',
    'book-open',
    'data.general.view',
    8,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-degree
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'f9faa0b9-9a00-35d1-cac6-df58fdd61626',
    'data-employee-degree',
    'menu.data.employee.degree',
    '/data/employee/degree',
    'graduation-cap',
    'data.employee.view',
    8,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-doctoral-status
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9b840f4b-9b4b-373b-5278-ed2a25261e91',
    'data-student-doctoral-status',
    'menu.data.student.doctoral_status',
    '/data/student/doctoral-status',
    'check-circle',
    'data.student.view',
    8,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-diploma-blank-status
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '1e0c81ec-a2d6-e2b3-484d-35e4b519b616',
    'data-education-diploma-blank-status',
    'menu.data.education.diploma_blank_status',
    '/data/education/diploma-blank-status',
    'file-check',
    'data.education.view',
    8,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-exam-finish
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '4fedeaef-ac0b-42ac-b089-d5604d972d20',
    'data-study-exam-finish',
    'menu.data.study.exam_finish',
    '/data/study/exam-finish',
    'check-circle',
    'data.study.view',
    8,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-publication-database
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '00bf9b82-e725-0e54-e055-d8f1c3f96197',
    'data-science-publication-database',
    'menu.data.science.publication_database',
    '/data/science/publication-database',
    'database',
    'data.science.view',
    8,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-auditorium-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '7a3358e1-067b-0944-c24f-9c404c6fc989',
    'data-organizational-auditorium-type',
    'menu.data.organizational.auditorium_type',
    '/data/organizational/auditorium-type',
    'door-closed',
    'data.organizational.view',
    8,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-terrain
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'da3d330e-81f5-7832-af9a-dd5006b4ccca',
    'data-general-terrain',
    'menu.data.general.terrain',
    '/data/general/terrain',
    'home',
    'data.general.view',
    9,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-employee-rank
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'ab79493d-2b5f-e12c-0dda-09ee20ac3c7e',
    'data-employee-rank',
    'menu.data.employee.rank',
    '/data/employee/rank',
    'trending-up',
    'data.employee.view',
    9,
    true,
    '6549a74d-f734-7ab1-7f5e-b9e395ac10e6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-graduate-fields
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '0970fb44-0b72-d53a-d409-e039efa0f441',
    'data-student-graduate-fields',
    'menu.data.student.graduate_fields',
    '/data/student/graduate-fields',
    'briefcase',
    'data.student.view',
    9,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-diploma-blank-generate
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '2228529b-110a-6cb1-c238-3bf72ad3cba2',
    'data-education-diploma-blank-generate',
    'menu.data.education.diploma_blank_generate_status',
    '/data/education/diploma-blank-generate',
    'file-plus',
    'data.education.view',
    9,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-final-exam-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '2ed955fc-775d-31d2-73d2-206576288620',
    'data-study-final-exam-type',
    'menu.data.study.final_exam_type',
    '/data/study/final-exam-type',
    'clipboard-check',
    'data.study.view',
    9,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-science-scholar-database
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'fe374c7b-6345-3d20-63e5-334ae79392d6',
    'data-science-scholar-database',
    'menu.data.science.scholar_database',
    '/data/science/scholar-database',
    'database',
    'data.science.view',
    9,
    true,
    '05523a26-1ba3-2a27-ed36-e885a86f9fed',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-device-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'c376f94d-b7ee-0ce3-96ba-aa1f922a7c9f',
    'data-organizational-device-type',
    'menu.data.organizational.device_type',
    '/data/organizational/device-type',
    'monitor',
    'data.organizational.view',
    9,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-general-poverty-level
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'ef9c1de1-a3eb-ee44-604f-9327f51b3989',
    'data-general-poverty-level',
    'menu.data.general.poverty_level',
    '/data/general/poverty-level',
    'trending-down',
    'data.general.view',
    10,
    true,
    '60a2be2c-08ef-bf45-de7e-cc82a2e2c312',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-graduate-inactive
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '11358287-4fbf-b9c0-7165-1faae6ec80ed',
    'data-student-graduate-inactive',
    'menu.data.student.graduate_inactive',
    '/data/student/graduate-inactive',
    'user-x',
    'data.student.view',
    10,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-certificate-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '617e1658-1369-1749-dec3-69fad16265de',
    'data-education-certificate-type',
    'menu.data.education.certificate_type',
    '/data/education/certificate-type',
    'award',
    'data.education.view',
    10,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-semester-list
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '318f96c5-c015-4013-8b6b-5836486bb047',
    'data-study-semester-list',
    'menu.data.study.semester_list',
    '/data/study/semester-list',
    'list-ordered',
    'data.study.view',
    10,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-organizational-grant-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '128e15f0-d57b-3ed1-656a-56ae6c658497',
    'data-organizational-grant-type',
    'menu.data.organizational.grant_type',
    '/data/organizational/grant-type',
    'gift',
    'data.organizational.view',
    10,
    true,
    '3be15047-31a3-ec12-12db-1cadbbcf7753',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'c8cd76cc-4544-7b74-ba05-edc25ec25cfa',
    'data-student-type',
    'menu.data.student.type',
    '/data/student/type',
    'tag',
    'data.student.view',
    11,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-certificate-names
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'b182942a-b784-c137-4c40-3f13f61e394e',
    'data-education-certificate-names',
    'menu.data.education.certificate_names',
    '/data/education/certificate-names',
    'award',
    'data.education.view',
    11,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-decree-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '07f8e1f9-8750-9636-e4d2-ae80b0ec68dc',
    'data-study-decree-type',
    'menu.data.study.decree_type',
    '/data/study/decree-type',
    'file-text',
    'data.study.view',
    11,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-living-status
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '8e45739d-f0fa-9556-f6a4-c3bd64917a87',
    'data-student-living-status',
    'menu.data.student.living_status',
    '/data/student/living-status',
    'home',
    'data.student.view',
    12,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-certificate-subjects
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'd6bfa630-412d-c2f2-f285-1666ea21ec89',
    'data-education-certificate-subjects',
    'menu.data.education.certificate_subjects',
    '/data/education/certificate-subjects',
    'book',
    'data.education.view',
    12,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-sport-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9c7094bb-bd16-f240-a016-362aa80b6a18',
    'data-study-sport-type',
    'menu.data.study.sport_type',
    '/data/study/sport-type',
    'trophy',
    'data.study.view',
    12,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-roommate-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '81c59e6d-0ea3-ec4f-452f-b5cb16351c58',
    'data-student-roommate-type',
    'menu.data.student.roommate_type',
    '/data/student/roommate-type',
    'users',
    'data.student.view',
    13,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-education-certificate-grades
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'e8343183-a20f-3fd4-bf5a-3b71967e4dd2',
    'data-education-certificate-grades',
    'menu.data.education.certificate_grades',
    '/data/education/certificate-grades',
    'star',
    'data.education.view',
    13,
    true,
    'b1a0c717-f646-66ea-558b-b8d31838baae',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-attendance-setting
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '00a85b4c-162b-9bce-fb2e-14930f71c395',
    'data-study-attendance-setting',
    'menu.data.study.attendance_setting',
    '/data/study/attendance-setting',
    'settings',
    'data.study.view',
    13,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-workplace-compatibility
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '7e3bf7be-6359-2b9c-cff6-3f80e3a7ff3c',
    'data-student-workplace-compatibility',
    'menu.data.student.workplace_compatibility',
    '/data/student/workplace-compatibility',
    'check-square',
    'data.student.view',
    14,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-conduction-form
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '60bc0498-a2e2-43d3-5314-dd13acbb90b0',
    'data-study-conduction-form',
    'menu.data.study.conduction_form',
    '/data/study/conduction-form',
    'presentation',
    'data.study.view',
    14,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-student-academic-mobile
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'ecdcfcb4-247b-a4d9-27da-87694e42bd74',
    'data-student-academic-mobile',
    'menu.data.student.academic_mobile',
    '/data/student/academic-mobile',
    'globe',
    'data.student.view',
    15,
    true,
    '63b3d0e7-e102-6bf0-0733-74fe520133a0',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-internship-form
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '4e1d1158-96f7-6947-9b85-3a6e3387d5c9',
    'data-study-internship-form',
    'menu.data.study.internship_form',
    '/data/study/internship-form',
    'briefcase',
    'data.study.view',
    15,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-internship-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '4abaabfb-0ce0-9ff2-9a5b-a21add615a5b',
    'data-study-internship-type',
    'menu.data.study.internship_type',
    '/data/study/internship-type',
    'briefcase',
    'data.study.view',
    16,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-resource-type
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'f0ba9026-5449-48dd-1a27-e568abecddcd',
    'data-study-resource-type',
    'menu.data.study.resource_type',
    '/data/study/resource-type',
    'package',
    'data.study.view',
    17,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- data-study-outside-activities
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'b7eb50f8-9cdb-09d9-658d-c48ce79e65fd',
    'data-study-outside-activities',
    'menu.data.study.outside_activities',
    '/data/study/outside-activities',
    'map-pin',
    'data.study.view',
    18,
    true,
    '24246231-3565-aec5-203e-ca5ee793e952',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-universities-count
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '78a1571b-aadf-efb8-add1-daab84d4e911',
    'reports-universities-count',
    'menu.reports.universities.count',
    '/reports/universities/count',
    'hash',
    'reports.universities.view',
    1,
    true,
    '4b02066c-a61d-0351-d1e8-1231062053ce',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-employees-navigation
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '9ebf9d3d-08a0-5541-11c2-c8a5c9276c97',
    'reports-employees-navigation',
    'menu.reports.employees.navigation',
    '/reports/employees/navigation',
    'line-chart',
    'reports.employees.view',
    1,
    true,
    '4caf0ef7-046d-b5e3-e366-cc80ef9d509d',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-students-statistics
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'c0eb5118-61dc-d840-e1ef-4221b12334d4',
    'reports-students-statistics',
    'menu.reports.students.statistics',
    '/reports/students/statistics',
    'line-chart',
    'reports.students.statistics.view',
    1,
    true,
    'a3e459bd-e146-badb-01f6-807e5d8f3851',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-academic-study
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '3b0a79e9-563e-f6ae-aac8-c904ab5959a5',
    'reports-academic-study',
    'menu.reports.academic.study',
    '/reports/academic/study',
    'pie-chart',
    'reports.academic.study.view',
    1,
    true,
    '06c8d6cf-d8e9-83ab-8fb3-a620b6da50c6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-research-project
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '8fe2195a-987c-91d6-5014-cc401e3f9f9a',
    'reports-research-project',
    'menu.reports.research.project',
    '/reports/research/project',
    'pie-chart',
    'reports.research.project.view',
    1,
    true,
    '0b87bfc4-2e39-39d1-bf50-986d27ea18c2',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-economic-finance
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'f8382c8d-54f9-b66c-ed5e-af3421a251a9',
    'reports-economic-finance',
    'menu.reports.economic.finance',
    '/reports/economic/finance',
    'bar-chart',
    'reports.economic.finance.view',
    1,
    true,
    '275c2574-141e-a5fd-f55c-9f277feaf7a6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- system-temp
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '5d744d8f-b64c-da3c-69a3-e0d6e76fe7b4',
    'system-temp',
    'menu.system.temp',
    '/system/temp',
    'settings',
    'system.temp.view',
    1,
    true,
    '54b53072-540e-eeb8-f8e9-343e71f28176',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-universities-faculty-list
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '34c9ce30-0a84-09d0-869b-22e4bac0ef98',
    'reports-universities-faculty-list',
    'menu.reports.universities.faculty_list',
    '/reports/universities/faculty-list',
    'list',
    'reports.universities.view',
    2,
    true,
    '4b02066c-a61d-0351-d1e8-1231062053ce',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-employees-private
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'b9ab25fd-d10c-7a5e-728d-9a3565dc0756',
    'reports-employees-private',
    'menu.reports.employees.private',
    '/reports/employees/private',
    'line-chart',
    'reports.employees.private.view',
    2,
    true,
    '4caf0ef7-046d-b5e3-e366-cc80ef9d509d',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-students-education
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '5d9bb7ea-6d01-68d9-2c9c-ea3ab52b73dd',
    'reports-students-education',
    'menu.reports.students.education',
    '/reports/students/education',
    'bar-chart',
    'reports.students.education.view',
    2,
    true,
    'a3e459bd-e146-badb-01f6-807e5d8f3851',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-research-publication
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '39c821e1-85da-1f8f-be55-69f8c576751e',
    'reports-research-publication',
    'menu.reports.research.publication',
    '/reports/research/publication',
    'pie-chart',
    'reports.research.publication.view',
    2,
    true,
    '0b87bfc4-2e39-39d1-bf50-986d27ea18c2',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-economic-xujalik
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'e849021e-4cb3-2492-fbe8-578902ee2644',
    'reports-economic-xujalik',
    'menu.reports.economic.xujalik',
    '/reports/economic/xujalik',
    'bar-chart',
    'reports.economic.xujalik.view',
    2,
    true,
    '275c2574-141e-a5fd-f55c-9f277feaf7a6',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- system-translation
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '54af9c83-03d1-9390-b79b-30be3636f97e',
    'system-translation',
    'menu.system.translation',
    '/system/translation',
    'languages',
    'system.translation.view',
    2,
    true,
    '54b53072-540e-eeb8-f8e9-343e71f28176',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-employees-work
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '77442744-000a-b13f-1b10-89f3b54ddb3b',
    'reports-employees-work',
    'menu.reports.employees.work',
    '/reports/employees/work',
    'line-chart',
    'reports.employees.work.view',
    3,
    true,
    '4caf0ef7-046d-b5e3-e366-cc80ef9d509d',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-students-private
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '2856ad9d-99c6-8c29-1cc0-5ae4db93baf1',
    'reports-students-private',
    'menu.reports.students.private',
    '/reports/students/private',
    'bar-chart',
    'reports.students.private.view',
    3,
    true,
    'a3e459bd-e146-badb-01f6-807e5d8f3851',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-research-researcher
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '7c7d9d82-20eb-6330-d7a1-5fe723ae58bf',
    'reports-research-researcher',
    'menu.reports.research.researcher',
    '/reports/research/researcher',
    'pie-chart',
    'reports.research.researcher.view',
    3,
    true,
    '0b87bfc4-2e39-39d1-bf50-986d27ea18c2',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- system-university-users
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '361ffebe-d885-3537-f881-cf387cb0f0aa',
    'system-university-users',
    'menu.system.university_users',
    '/system/university-users',
    'users',
    'system.users.view',
    3,
    true,
    '54b53072-540e-eeb8-f8e9-343e71f28176',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-students-attendance
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '4ec15ad4-93a5-dc2b-a211-93c0c6a32119',
    'reports-students-attendance',
    'menu.reports.students.attendance',
    '/reports/students/attendance',
    'bar-chart',
    'reports.students.attendance.view',
    4,
    true,
    'a3e459bd-e146-badb-01f6-807e5d8f3851',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- system-api-logs
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'f3bd7e5c-d1b8-0c8b-0819-4ebeff6c22e6',
    'system-api-logs',
    'menu.system.api_logs',
    '/system/api-logs',
    'file-text',
    'system.logs.view',
    4,
    true,
    '54b53072-540e-eeb8-f8e9-343e71f28176',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-students-score
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'fdde16c9-92c8-312c-d52a-d18cf1da7098',
    'reports-students-score',
    'menu.reports.students.score',
    '/reports/students/score',
    'bar-chart',
    'reports.students.score.view',
    5,
    true,
    'a3e459bd-e146-badb-01f6-807e5d8f3851',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- system-report-update
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    'be9bff71-f729-630b-b719-6326679c1313',
    'system-report-update',
    'menu.system.report_update',
    '/system/report-update',
    'refresh-cw',
    'system.report-update.view',
    5,
    true,
    '54b53072-540e-eeb8-f8e9-343e71f28176',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

-- reports-students-dynamic
INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, active, parent_id, created_at, updated_at)
VALUES (
    '4844454f-64dc-285c-290c-9752b5126772',
    'reports-students-dynamic',
    'menu.reports.students.dynamic',
    '/reports/students/dynamic',
    'pie-chart',
    'reports.students.dynamic.view',
    6,
    true,
    'a3e459bd-e146-badb-01f6-807e5d8f3851',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO NOTHING;

