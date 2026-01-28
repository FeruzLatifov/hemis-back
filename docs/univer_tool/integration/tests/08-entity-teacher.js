// 08. Teacher entity testlari
const tests_08 = [
    // ===== ETeacher =====
    {
        id: 'teacher-list',
        category: '08. Teacher Entity',
        name: 'O\'qituvchilar ro\'yxatini olish (ETeacher list)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_ETeacher',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'teacher-single',
        category: '08. Teacher Entity',
        name: 'Bitta o\'qituvchini ID bo\'yicha olish (ETeacher by ID)',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_ETeacher/{{teachers[0].id}}',
        auth: 'bearer',
        params: { view: 'eTeacher-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'teacher-create',
        category: '08. Teacher Entity',
        name: 'O\'qituvchi yaratish (ETeacher POST)',
        order: 3,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_ETeacher',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const t = ctx.teachers?.[0] || {};
            return {
                firstname: t.firstname || t.firstName || 'Test',
                lastname: t.lastname || t.lastName || 'Teacher',
                fathername: t.fathername || t.fatherName || '',
                pinfl: '00000000000001',
                gender: t.gender || { code: '11' },
                university: t.university || { code: '401' },
                tag: 'v4'
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'ETeacher yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'teacher-update',
        category: '08. Teacher Entity',
        name: 'O\'qituvchini yangilash (ETeacher PUT)',
        order: 4,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_ETeacher/{{teachers[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const t = ctx.teachers?.[0] || {};
            return {
                id: t.id,
                firstname: t.firstname || t.firstName || 'UpdatedTeacher',
                lastname: t.lastname || t.lastName || '',
                pinfl: t.pinfl || '',
                gender: t.gender || { code: '11' },
                university: t.university || { code: '401' },
                tag: 'v4'
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'ETeacher yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'teacher-delete',
        category: '08. Teacher Entity',
        name: 'O\'qituvchini o\'chirish (ETeacher DELETE)',
        order: 5,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_ETeacher/{{teachers[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'ETeacher o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EEmployeeJobs =====
    {
        id: 'employee-jobs-list',
        category: '08. Teacher Entity',
        name: 'Xodim lavozimlarini olish (EEmployeeJobs list)',
        order: 5,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EEmployeeJobs',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
        storeValues: {
            employeeJobId: '[0].id',
        },
    },
    {
        id: 'employee-jobs-single',
        category: '08. Teacher Entity',
        name: 'Bitta xodim lavozimini olish (EEmployeeJobs by ID)',
        order: 6,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EEmployeeJobs/{{storedValues.employeeJobId}}',
        auth: 'bearer',
        params: { view: 'eEmployeeJobs-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token', 'employee-jobs-list'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'employee-jobs-create',
        category: '08. Teacher Entity',
        name: 'Xodim lavozimi yaratish (EEmployeeJobs POST)',
        order: 7,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EEmployeeJobs',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const t = ctx.teachers?.[0] || {};
            return {
                employee: { id: t.id },
                university: t.university || { code: '401' },
                department: ctx.departments?.[0] || { code: '1' },
                employeeType: { code: '10' },
                staffPosition: { code: '1' },
                employmentForm: { code: '10' },
                employmentStaff: { code: '10' }
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EEmployeeJobs yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'employee-jobs-update',
        category: '08. Teacher Entity',
        name: 'Xodim lavozimini yangilash (EEmployeeJobs PUT)',
        order: 8,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EEmployeeJobs/{{storedValues.employeeJobId}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token', 'employee-jobs-list'],
        bodyBuilder: (ctx, stored) => {
            return {
                id: stored.employeeJobId || undefined,
                employeeType: { code: '10' },
                staffPosition: { code: '1' },
                employmentForm: { code: '10' },
                employmentStaff: { code: '10' }
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EEmployeeJobs yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'employee-jobs-delete',
        category: '08. Teacher Entity',
        name: 'Xodim lavozimini o\'chirish (EEmployeeJobs DELETE)',
        order: 9,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EEmployeeJobs/{{storedValues.employeeJobId}}',
        auth: 'bearer',
        dependsOn: ['auth-token', 'employee-jobs-list'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EEmployeeJobs o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
];
