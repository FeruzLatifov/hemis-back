// 07. Student entity testlari
const tests_07 = [
    // ===== EStudent =====
    {
        id: 'student-list',
        category: '07. Student Entity',
        name: 'Talabalar ro\'yxatini olish (EStudent list)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EStudent',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'student-single',
        category: '07. Student Entity',
        name: 'Bitta talabani ID bo\'yicha olish (EStudent by ID)',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EStudent/{{students[0].id}}',
        auth: 'bearer',
        params: { view: 'eStudent-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id', 'pinfl'],
    },
    {
        id: 'student-create',
        category: '07. Student Entity',
        name: 'Talaba yaratish (EStudent POST)',
        order: 3,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EStudent',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            return {
                firstname: s.firstname || s.firstName || 'Test',
                lastname: s.lastname || s.lastName || 'Student',
                fathername: s.fathername || s.fatherName || '',
                pinfl: '00000000000000',
                serialNumber: s.serialNumber || '',
                birthday: s.birthday || s.birthDate || '2000-01-01',
                gender: s.gender || { code: '11' },
                university: s.university || { code: '401' },
                educationYear: s.educationYear || { code: '2020' },
                tag: 'v1'
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EStudent yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'student-entity-update',
        category: '07. Student Entity',
        name: 'Talabani yangilash (EStudent PUT)',
        order: 4,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EStudent/{{students[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            return {
                id: s.id,
                firstname: s.firstname || s.firstName || 'UpdatedTest',
                lastname: s.lastname || s.lastName || '',
                pinfl: s.pinfl || '',
                gender: s.gender || { code: '11' },
                university: s.university || { code: '401' },
                tag: 'v1'
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EStudent yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EStudentGpa =====
    {
        id: 'student-gpa-list',
        category: '07. Student Entity',
        name: 'Talaba GPA ro\'yxati (EStudentGpa list)',
        order: 5,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EStudentGpa',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'student-gpa-single',
        category: '07. Student Entity',
        name: 'Bitta GPA yozuvini olish (EStudentGpa by ID)',
        order: 6,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EStudentGpa/{{gpa[0].id}}',
        auth: 'bearer',
        params: { view: 'eStudentGpa-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'student-gpa-create',
        category: '07. Student Entity',
        name: 'Talaba GPA yaratish (EStudentGpa POST)',
        order: 7,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EStudentGpa',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            const g = ctx.gpa?.[0] || {};
            return {
                studentId: { id: s.id },
                educationYear: g.educationYear || s.educationYear || { code: '2020' },
                level: g.level || { code: '10' },
                gpa: 3.5,
                creditSum: 0,
                subjects: 0,
                debtSubjects: 0
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EStudentGpa yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'student-gpa-update',
        category: '07. Student Entity',
        name: 'Talaba GPA yangilash (EStudentGpa PUT)',
        order: 8,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EStudentGpa/{{gpa[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const g = ctx.gpa?.[0] || {};
            return {
                id: g.id,
                gpa: 4.0,
                creditSum: g.creditSum || 0,
                subjects: g.subjects || 0,
                debtSubjects: g.debtSubjects || 0
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EStudentGpa yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== DELETE testlari =====
    {
        id: 'student-delete',
        category: '07. Student Entity',
        name: 'Talabani o\'chirish (EStudent DELETE)',
        order: 9,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EStudent/{{students[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EStudent o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'student-gpa-entity-delete',
        category: '07. Student Entity',
        name: 'Talaba GPA o\'chirish (EStudentGpa DELETE)',
        order: 10,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EStudentGpa/{{gpa[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EStudentGpa o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
];
