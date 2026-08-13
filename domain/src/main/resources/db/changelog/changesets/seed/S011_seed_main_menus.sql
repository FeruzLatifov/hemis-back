-- S011: Seed main menu structure (56 items)
-- V013 da yaratilgan upsert_menu(...) helper ishlatiladi — idempotent.
-- runOnChange: true (master.yaml) — har deploy'da menu tarkibi sinxronlanadi.

-- =====================================================
-- ROOT MENUS (9 items)
-- =====================================================

SELECT upsert_menu('10000000-0000-0000-0000-000000000001'::uuid, 'dashboard',    'Dashboard',    '/dashboard',  'home',         NULL,                  1, NULL);
SELECT upsert_menu('10000000-0000-0000-0000-000000000002'::uuid, 'institutions', 'Institutions', NULL,          'building',     'institutions.view',   2, NULL);
SELECT upsert_menu('10000000-0000-0000-0000-000000000003'::uuid, 'students',     'Students',     NULL,          'graduation-cap','students.view',      3, NULL);
SELECT upsert_menu('10000000-0000-0000-0000-000000000004'::uuid, 'teachers',     'Teachers',     NULL,          'user-check',   'teachers.view',       4, NULL);
SELECT upsert_menu('10000000-0000-0000-0000-000000000005'::uuid, 'science',      'Science',      NULL,          'flask',        'science.view',        5, NULL);
SELECT upsert_menu('10000000-0000-0000-0000-000000000006'::uuid, 'reports',      'Reports',      NULL,          'bar-chart',    'reports.view',        6, NULL);
SELECT upsert_menu('10000000-0000-0000-0000-000000000007'::uuid, 'rating',       'Rating',       NULL,          'line-chart',   'rating.view',         7, NULL);
SELECT upsert_menu('10000000-0000-0000-0000-000000000008'::uuid, 'classifiers',  'Classifiers',  NULL,          'database',     'classifiers.view',    8, NULL);
SELECT upsert_menu('10000000-0000-0000-0000-000000000009'::uuid, 'system',       'System',       NULL,          'settings',     'system.view',         9, NULL, 'system');

-- =====================================================
-- CHILDREN OF: institutions (7 items)
-- =====================================================

SELECT upsert_menu('20000002-0000-0000-0000-000000000001'::uuid, 'inst-universities',           'Universities',             '/institutions/universities',           'building',  'institutions.universities.view',            1, '10000000-0000-0000-0000-000000000002'::uuid);
SELECT upsert_menu('20000002-0000-0000-0000-000000000002'::uuid, 'inst-faculties',              'Faculties',                '/institutions/faculties',              'school',    'institutions.faculties.view',               2, '10000000-0000-0000-0000-000000000002'::uuid);
SELECT upsert_menu('20000002-0000-0000-0000-000000000003'::uuid, 'inst-departments',            'Departments',              '/institutions/departments',            'users',     'institutions.departments.view',             3, '10000000-0000-0000-0000-000000000002'::uuid);
SELECT upsert_menu('20000002-0000-0000-0000-000000000005'::uuid, 'inst-university-specialities','Institution specialities', '/institutions/university-specialities','book-open', 'institutions.university-specialities.view', 5, '10000000-0000-0000-0000-000000000002'::uuid);
SELECT upsert_menu('20000002-0000-0000-0000-000000000006'::uuid, 'inst-diploma-blanks',         'Diploma blanks',           '/institutions/diploma-blanks',          'file-badge','institutions.diploma-blanks.view',          6, '10000000-0000-0000-0000-000000000002'::uuid);
SELECT upsert_menu('20000002-0000-0000-0000-000000000007'::uuid, 'inst-blank-distribution',     'Blank distribution',       '/institutions/diploma-blank-distribution','send',    'institutions.diploma-blank-distribution.view', 7, '10000000-0000-0000-0000-000000000002'::uuid);

-- =====================================================
-- CHILDREN OF: students (6 items)
-- =====================================================

-- Bo'lim darajasidagi (coarse) ruxsat: 'students.view' bo'lgan foydalanuvchi butun Talabalar bo'limini ko'radi va ochadi (menu ⟺ route izchil, sodda rol modeli).
SELECT upsert_menu('20000003-0000-0000-0000-000000000001'::uuid, 'student-list',         'Data',         '/students',              'list',         'students.view', 1, '10000000-0000-0000-0000-000000000003'::uuid);
SELECT upsert_menu('20000003-0000-0000-0000-000000000002'::uuid, 'student-directions',   'Directions',           '/students/directions',   'book-open',    'students.view', 2, '10000000-0000-0000-0000-000000000003'::uuid);
SELECT upsert_menu('20000003-0000-0000-0000-000000000003'::uuid, 'student-groups',       'Study groups',         '/students/groups',       'users',        'students.view', 3, '10000000-0000-0000-0000-000000000003'::uuid);
SELECT upsert_menu('20000003-0000-0000-0000-000000000004'::uuid, 'student-diplomas',     'Diplomas',             '/students/diplomas',     'award',        'students.view', 4, '10000000-0000-0000-0000-000000000003'::uuid);
SELECT upsert_menu('20000003-0000-0000-0000-000000000005'::uuid, 'student-scholarships', 'Scholarship',          '/students/scholarships', 'dollar-sign',  'students.view', 5, '10000000-0000-0000-0000-000000000003'::uuid);
SELECT upsert_menu('20000003-0000-0000-0000-000000000006'::uuid, 'student-certificates', 'Certificates', '/students/certificates', 'file-text',    'students.view', 6, '10000000-0000-0000-0000-000000000003'::uuid);

-- =====================================================
-- CHILDREN OF: teachers (4 items)
-- =====================================================

-- Bo'lim darajasidagi (coarse) ruxsat: 'teachers.view' → butun O'qituvchilar bo'limi.
SELECT upsert_menu('20000004-0000-0000-0000-000000000001'::uuid, 'teacher-list',           'List',           '/teachers',                'list',      'teachers.view', 1, '10000000-0000-0000-0000-000000000004'::uuid);
SELECT upsert_menu('20000004-0000-0000-0000-000000000002'::uuid, 'teacher-positions',      'Positions',              '/teachers/positions',      'briefcase', 'teachers.view', 2, '10000000-0000-0000-0000-000000000004'::uuid);
SELECT upsert_menu('20000004-0000-0000-0000-000000000003'::uuid, 'teacher-qualifications', 'Teacher qualifications', '/teachers/qualifications', 'award',     'teachers.view', 3, '10000000-0000-0000-0000-000000000004'::uuid);
SELECT upsert_menu('20000004-0000-0000-0000-000000000004'::uuid, 'teacher-employee-jobs',  'Employee jobs',          '/teachers/employee-jobs',  'briefcase', 'teachers.view', 4, '10000000-0000-0000-0000-000000000004'::uuid);

-- =====================================================
-- CHILDREN OF: science (7 items)
-- =====================================================

-- Bo'lim darajasidagi (coarse) ruxsat: 'science.view' → butun Fan bo'limi.
SELECT upsert_menu('20000005-0000-0000-0000-000000000001'::uuid, 'sci-researchers',         'Researchers',             '/science/researchers',         'user-check', 'science.view', 1, '10000000-0000-0000-0000-000000000005'::uuid);
SELECT upsert_menu('20000005-0000-0000-0000-000000000002'::uuid, 'sci-projects',            'Projects',     '/science/projects',            'lightbulb',  'science.view', 2, '10000000-0000-0000-0000-000000000005'::uuid);
SELECT upsert_menu('20000005-0000-0000-0000-000000000003'::uuid, 'sci-publications',        'Publications', '/science/publications',        'book',       'science.view', 3, '10000000-0000-0000-0000-000000000005'::uuid);
SELECT upsert_menu('20000005-0000-0000-0000-000000000004'::uuid, 'sci-methodical',          'Methodical publications', '/science/methodical',          'book-open',  'science.view', 4, '10000000-0000-0000-0000-000000000005'::uuid);
SELECT upsert_menu('20000005-0000-0000-0000-000000000005'::uuid, 'sci-intellectual',        'Intellectual property',   '/science/intellectual',        'briefcase',  'science.view', 5, '10000000-0000-0000-0000-000000000005'::uuid);
SELECT upsert_menu('20000005-0000-0000-0000-000000000006'::uuid, 'sci-dissertation-defense','Dissertation defense',    '/science/dissertation-defense','award',      'science.view', 6, '10000000-0000-0000-0000-000000000005'::uuid);
SELECT upsert_menu('20000005-0000-0000-0000-000000000007'::uuid, 'sci-research-activity',   'Research activity',     '/science/research-activity',   'activity',   'science.view', 7, '10000000-0000-0000-0000-000000000005'::uuid);

-- =====================================================
-- CHILDREN OF: reports (6 items)
-- =====================================================

SELECT upsert_menu('20000006-0000-0000-0000-000000000001'::uuid, 'reports-students',     'Student reports',     '/reports/students',     'graduation-cap', 'reports.students.view',     1, '10000000-0000-0000-0000-000000000006'::uuid);
SELECT upsert_menu('20000006-0000-0000-0000-000000000002'::uuid, 'reports-teachers',     'Teacher reports',     '/reports/teachers',     'users',          'reports.teachers.view',     2, '10000000-0000-0000-0000-000000000006'::uuid);
SELECT upsert_menu('20000006-0000-0000-0000-000000000003'::uuid, 'reports-institutions', 'University reports',  '/reports/institutions', 'building',       'reports.institutions.view', 3, '10000000-0000-0000-0000-000000000006'::uuid);
SELECT upsert_menu('20000006-0000-0000-0000-000000000004'::uuid, 'reports-academic',     'Academic reports',    '/reports/academic',     'book',           'reports.academic.view',     4, '10000000-0000-0000-0000-000000000006'::uuid);
SELECT upsert_menu('20000006-0000-0000-0000-000000000005'::uuid, 'reports-research',     'Research reports',    '/reports/research',     'flask',          'reports.research.view',     5, '10000000-0000-0000-0000-000000000006'::uuid);
SELECT upsert_menu('20000006-0000-0000-0000-000000000006'::uuid, 'reports-economic',     'Economic reports',    '/reports/economic',     'dollar-sign',    'reports.economic.view',     6, '10000000-0000-0000-0000-000000000006'::uuid);

-- =====================================================
-- CHILDREN OF: rating (4 items)
-- =====================================================

SELECT upsert_menu('20000007-0000-0000-0000-000000000001'::uuid, 'rating-administrative', 'Administrative rating', '/rating/administrative', 'building',         'rating.administrative.view', 1, '10000000-0000-0000-0000-000000000007'::uuid);
SELECT upsert_menu('20000007-0000-0000-0000-000000000002'::uuid, 'rating-academic',       'Academic rating',       '/rating/academic',       'layout-dashboard', 'rating.academic.view',       2, '10000000-0000-0000-0000-000000000007'::uuid);
SELECT upsert_menu('20000007-0000-0000-0000-000000000003'::uuid, 'rating-scientific',     'Scientific rating',     '/rating/scientific',     'graduation-cap',   'rating.scientific.view',     3, '10000000-0000-0000-0000-000000000007'::uuid);
SELECT upsert_menu('20000007-0000-0000-0000-000000000004'::uuid, 'rating-gpa',            'Student GPA',           '/rating/gpa',            'award',            'rating.gpa.view',            4, '10000000-0000-0000-0000-000000000007'::uuid);

-- =====================================================
-- CHILDREN OF: classifiers (7 items here; cls-organizational removed 2026-08-08; +3 from S007 = 10 total)
-- =====================================================

-- Bo'lim darajasidagi (coarse) ruxsat: 'classifiers.view' → butun Klassifikatorlar bo'limi (backend ClassifierWebController ham classifiers.view enforce qiladi).
SELECT upsert_menu('20000008-0000-0000-0000-000000000001'::uuid, 'cls-general',        'General data',        '/classifiers/general',        'file-text',      'classifiers.view', 1, '10000000-0000-0000-0000-000000000008'::uuid);
SELECT upsert_menu('20000008-0000-0000-0000-000000000002'::uuid, 'cls-structure',      'Structure',           '/classifiers/structure',      'share-2',        'classifiers.view', 2, '10000000-0000-0000-0000-000000000008'::uuid);
SELECT upsert_menu('20000008-0000-0000-0000-000000000003'::uuid, 'cls-employee',       'Employees',           '/classifiers/employee',       'users',          'classifiers.view', 3, '10000000-0000-0000-0000-000000000008'::uuid);
SELECT upsert_menu('20000008-0000-0000-0000-000000000004'::uuid, 'cls-student',        'Students',             '/classifiers/student',        'user-circle',    'classifiers.view', 4, '10000000-0000-0000-0000-000000000008'::uuid);
SELECT upsert_menu('20000008-0000-0000-0000-000000000005'::uuid, 'cls-education',      'Education',           '/classifiers/education',      'book',           'classifiers.view', 5, '10000000-0000-0000-0000-000000000008'::uuid);
SELECT upsert_menu('20000008-0000-0000-0000-000000000006'::uuid, 'cls-study',          'Study process',       '/classifiers/study',          'edit',           'classifiers.view', 6, '10000000-0000-0000-0000-000000000008'::uuid);
SELECT upsert_menu('20000008-0000-0000-0000-000000000007'::uuid, 'cls-science',        'Scientific activity', '/classifiers/science',        'graduation-cap', 'classifiers.view', 7, '10000000-0000-0000-0000-000000000008'::uuid);
-- cls-organizational (Tashkiliy) REMOVED (2026-08-08, user-confirmed): dead link. Backend
-- ClassifierMetadataRegistry has NO 'organizational' category -> /classifiers/organizational 404'd
-- (empty page). Old-hemis 'Tashkiliy' = financial classifiers, now served by 'Moliyaviy' (cls-financial);
-- organizational-structure classifiers live under 'Tuzilma' (cls-structure). Also drop from rollback.

-- =====================================================
-- CHILDREN OF: system (5 items)
-- =====================================================

SELECT upsert_menu('20000009-0000-0000-0000-000000000001'::uuid, 'sys-translations',   'Translations',   '/system/translations',   'languages',    'system.translation.view',    1, '10000000-0000-0000-0000-000000000009'::uuid);
SELECT upsert_menu('20000009-0000-0000-0000-000000000002'::uuid, 'sys-users',          'Users',          '/system/users',          'users',        'users.view',                 2, '10000000-0000-0000-0000-000000000009'::uuid);
SELECT upsert_menu('20000009-0000-0000-0000-000000000003'::uuid, 'sys-roles',          'Roles',          '/system/roles',          'shield',       'roles.manage',               3, '10000000-0000-0000-0000-000000000009'::uuid);
SELECT upsert_menu('20000009-0000-0000-0000-000000000004'::uuid, 'sys-logs',           'Audit Logs',     '/system/logs',           'scroll-text',  'audit.view',                 4, '10000000-0000-0000-0000-000000000009'::uuid);
SELECT upsert_menu('20000009-0000-0000-0000-000000000005'::uuid, 'sys-report-updates', 'Report updates', '/system/report-updates', 'refresh-cw',   'system.report-update.view',  5, '10000000-0000-0000-0000-000000000009'::uuid);

-- =====================================================
-- VERIFICATION
-- =====================================================

DO $$
DECLARE
    menu_count INTEGER;
    root_menu_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO menu_count
      FROM menu
     WHERE deleted_at IS NULL
       AND code IN (
           'dashboard','institutions','students','teachers','science','reports','rating','classifiers','system',
           'inst-universities','inst-faculties','inst-departments','inst-university-specialities','inst-diploma-blanks','inst-blank-distribution',
           'student-list','student-directions','student-groups','student-diplomas','student-scholarships','student-certificates',
           'teacher-list','teacher-positions','teacher-qualifications','teacher-employee-jobs',
           'sci-researchers','sci-projects','sci-publications','sci-methodical','sci-intellectual','sci-dissertation-defense','sci-research-activity',
           'reports-students','reports-teachers','reports-institutions','reports-academic','reports-research','reports-economic',
           'rating-administrative','rating-academic','rating-scientific','rating-gpa',
           'cls-general','cls-structure','cls-employee','cls-student','cls-education','cls-study','cls-science','cls-organizational',
           'sys-translations','sys-users','sys-roles','sys-logs','sys-report-updates'
       );

    SELECT COUNT(*) INTO root_menu_count
      FROM menu
     WHERE parent_id IS NULL AND deleted_at IS NULL
       AND code IN ('dashboard','institutions','students','teachers','science','reports','rating','classifiers','system');

    RAISE NOTICE 'S011: MAIN MENU SEEDED';
    RAISE NOTICE '   Main menu items: % (expected 54)', menu_count;
    RAISE NOTICE '   Root items: % (expected 9)', root_menu_count;

    IF menu_count <> 54 THEN
        RAISE WARNING 'S011: expected 54 main menu items, found %', menu_count;
    END IF;
END $$;
