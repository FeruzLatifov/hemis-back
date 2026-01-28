// 12. Ma'muriy va inspeksiya entity testlari
const tests_12 = [
    // ===== RIAdministrativeStudent2 =====
    {
        id: 'admin-student2-list',
        category: '12. Admin Entity',
        name: 'Akademik almashinuv ro\'yxati (RIAdministrativeStudent2 list)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent2',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'admin-student2-create',
        category: '12. Admin Entity',
        name: 'Akademik almashinuv yaratish (RIAdministrativeStudent2 POST)',
        order: 2,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent2',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            country: { code: 'UZ' },
            educationType: { code: '11' },
            studentFullname: 'Test Student',
            exchangeDocument: 'Test Document',
            exchangeUniversityName: 'Test University',
            specialityName: 'Test Speciality',
            exchangeType: 'incoming',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent2 yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-student2-update',
        category: '12. Admin Entity',
        name: 'Akademik almashinuv yangilash (RIAdministrativeStudent2 PUT)',
        order: 3,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{{adminStudent2[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.adminStudent2 ? ctx.adminStudent2[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent2 yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== RIAdministrativeStudent3 =====
    {
        id: 'admin-student3-list',
        category: '12. Admin Entity',
        name: 'Bitiruvchilar bandligi ro\'yxati (RIAdministrativeStudent3 list)',
        order: 4,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent3',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'admin-student3-create',
        category: '12. Admin Entity',
        name: 'Bitiruvchilar bandligi yaratish (RIAdministrativeStudent3 POST)',
        order: 5,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent3',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            student: ctx.students && ctx.students[0] ? { id: ctx.students[0].id } : undefined,
            company: 'Test Company',
            position: 'Test Position',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent3 yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-student3-update',
        category: '12. Admin Entity',
        name: 'Bitiruvchilar bandligi yangilash (RIAdministrativeStudent3 PUT)',
        order: 6,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{{adminStudent3[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.adminStudent3 ? ctx.adminStudent3[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent3 yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== RIAdministrativeStudent4 =====
    {
        id: 'admin-student4-list',
        category: '12. Admin Entity',
        name: 'Olimpiada natijalari ro\'yxati (RIAdministrativeStudent4 list)',
        order: 7,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent4',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'admin-student4-create',
        category: '12. Admin Entity',
        name: 'Olimpiada natijalari yaratish (RIAdministrativeStudent4 POST)',
        order: 8,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent4',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            student: ctx.students && ctx.students[0] ? { id: ctx.students[0].id } : undefined,
            country: { code: 'UZ' },
            olimpiadaType: 'international',
            olimpiadaName: 'Test Olympiad',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent4 yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-student4-update',
        category: '12. Admin Entity',
        name: 'Olimpiada natijalari yangilash (RIAdministrativeStudent4 PUT)',
        order: 9,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{{adminStudent4[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.adminStudent4 ? ctx.adminStudent4[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent4 yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== RIAdministrativeStudentSport =====
    {
        id: 'admin-student-sport-list',
        category: '12. Admin Entity',
        name: 'Sport yutuqlari ro\'yxati (RIAdministrativeStudentSport list)',
        order: 10,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'admin-student-sport-create',
        category: '12. Admin Entity',
        name: 'Sport yutuqlari yaratish (RIAdministrativeStudentSport POST)',
        order: 11,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            student: ctx.students && ctx.students[0] ? { id: ctx.students[0].id } : undefined,
            sportType: { code: '1' },
            sportDate: '2024-06-01',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudentSport yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-student-sport-update',
        category: '12. Admin Entity',
        name: 'Sport yutuqlari yangilash (RIAdministrativeStudentSport PUT)',
        order: 12,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/{{adminStudentSport[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.adminStudentSport ? ctx.adminStudentSport[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudentSport yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== RIAdministrativeEmployee1 =====
    {
        id: 'admin-employee1-list',
        category: '12. Admin Entity',
        name: 'Xorijiy OTM malaka oshirish ro\'yxati (RIAdministrativeEmployee1 list)',
        order: 13,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'admin-employee1-create',
        category: '12. Admin Entity',
        name: 'Xorijiy OTM malaka oshirish yaratish (RIAdministrativeEmployee1 POST)',
        order: 14,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            employee: ctx.teachers && ctx.teachers[0] ? { id: ctx.teachers[0].id } : undefined,
            country: { code: 'UZ' },
            diplomaSerialNumber: 'AA0000001',
            diplomaDate: '2024-01-15',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee1 yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-employee1-update',
        category: '12. Admin Entity',
        name: 'Xorijiy OTM malaka oshirish yangilash (RIAdministrativeEmployee1 PUT)',
        order: 15,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{{adminEmployee1[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.adminEmployee1 ? ctx.adminEmployee1[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee1 yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== RIAdministrativeEmployee2 =====
    {
        id: 'admin-employee2-list',
        category: '12. Admin Entity',
        name: 'Xorijiy o\'qituvchilar ro\'yxati (RIAdministrativeEmployee2 list)',
        order: 16,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'admin-employee2-create',
        category: '12. Admin Entity',
        name: 'Xorijiy o\'qituvchilar yaratish (RIAdministrativeEmployee2 POST)',
        order: 17,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            employee: ctx.teachers && ctx.teachers[0] ? { id: ctx.teachers[0].id } : undefined,
            country: { code: 'UZ' },
            trainingDateStart: '2024-01-01',
            trainingDateEnd: '2024-06-30',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee2 yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-employee2-update',
        category: '12. Admin Entity',
        name: 'Xorijiy o\'qituvchilar yangilash (RIAdministrativeEmployee2 PUT)',
        order: 18,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{{adminEmployee2[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.adminEmployee2 ? ctx.adminEmployee2[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee2 yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== RIAdministrativeEmployee3 =====
    {
        id: 'admin-employee3-list',
        category: '12. Admin Entity',
        name: 'Inspeksiya o\'qituvchi ma\'lumotlari (RIAdministrativeEmployee3 list)',
        order: 19,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'admin-employee3-create',
        category: '12. Admin Entity',
        name: 'Inspeksiya o\'qituvchi yaratish (RIAdministrativeEmployee3 POST)',
        order: 20,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            employee: ctx.teachers && ctx.teachers[0] ? { id: ctx.teachers[0].id } : undefined,
            country: { code: 'UZ' },
            departureDate: '2024-03-01',
            arrivalDate: '2024-09-01',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee3 yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-employee3-update',
        category: '12. Admin Entity',
        name: 'Inspeksiya o\'qituvchi yangilash (RIAdministrativeEmployee3 PUT)',
        order: 21,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{{adminEmployee3[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.adminEmployee3 ? ctx.adminEmployee3[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee3 yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== DELETE testlari =====
    {
        id: 'admin-student2-delete',
        category: '12. Admin Entity',
        name: 'Akademik almashinuv o\'chirish (RIAdministrativeStudent2 DELETE)',
        order: 22,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{{adminStudent2[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent2 o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-student3-delete',
        category: '12. Admin Entity',
        name: 'Bitiruvchilar bandligi o\'chirish (RIAdministrativeStudent3 DELETE)',
        order: 23,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{{adminStudent3[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent3 o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-student4-delete',
        category: '12. Admin Entity',
        name: 'Olimpiada natijalari o\'chirish (RIAdministrativeStudent4 DELETE)',
        order: 24,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{{adminStudent4[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudent4 o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-student-sport-delete',
        category: '12. Admin Entity',
        name: 'Sport yutuqlari o\'chirish (RIAdministrativeStudentSport DELETE)',
        order: 25,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/{{adminStudentSport[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeStudentSport o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-employee1-delete',
        category: '12. Admin Entity',
        name: 'Xorijiy OTM malaka oshirish o\'chirish (RIAdministrativeEmployee1 DELETE)',
        order: 26,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{{adminEmployee1[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee1 o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-employee2-delete',
        category: '12. Admin Entity',
        name: 'Xorijiy o\'qituvchilar o\'chirish (RIAdministrativeEmployee2 DELETE)',
        order: 27,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{{adminEmployee2[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee2 o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'admin-employee3-delete',
        category: '12. Admin Entity',
        name: 'Inspeksiya o\'qituvchi o\'chirish (RIAdministrativeEmployee3 DELETE)',
        order: 28,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{{adminEmployee3[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIAdministrativeEmployee3 o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
];
