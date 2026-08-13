-- S013 rollback: 50 ta main menu code'larini o'chirish (children CASCADE bilan ham yo'qoladi)
DELETE FROM menu WHERE code IN (
    'dashboard','institutions','students','teachers','science','reports','rating','classifiers','system',
    'inst-universities','inst-faculties','inst-departments','inst-attached-specialities','inst-university-specialities','inst-diploma-blanks','inst-blank-distribution',
    'student-list','student-directions','student-groups','student-diplomas','student-scholarships','student-certificates',
    'teacher-list','teacher-positions','teacher-qualifications','teacher-employee-jobs',
    'sci-researchers','sci-projects','sci-publications','sci-methodical','sci-intellectual','sci-dissertation-defense','sci-research-activity',
    'reports-students','reports-teachers','reports-institutions','reports-academic','reports-research','reports-economic',
    'rating-administrative','rating-academic','rating-scientific','rating-gpa',
    'cls-general','cls-structure','cls-employee','cls-student','cls-education','cls-study','cls-science',
    'sys-translations','sys-users','sys-roles','sys-logs','sys-report-updates'
);
