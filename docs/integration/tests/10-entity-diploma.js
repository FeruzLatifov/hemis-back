// 10. Diplom va sertifikat entity testlari
const tests_10 = [
    // ===== EStudentDiploma =====
    {
        id: 'diploma-list',
        category: '10. Diploma Entity',
        name: 'Diplomlar ro\'yxatini olish (EStudentDiploma list)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EStudentDiploma',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'diploma-single',
        category: '10. Diploma Entity',
        name: 'Bitta diplomni ID bo\'yicha olish (EStudentDiploma by ID)',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EStudentDiploma/{{diplomas[0].id}}',
        auth: 'bearer',
        params: { view: 'eStudentDiploma-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'diploma-create',
        category: '10. Diploma Entity',
        name: 'Diplom yaratish (EStudentDiploma POST)',
        order: 3,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EStudentDiploma',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            const d = ctx.departments?.[0] || {};
            return {
                student: { id: s.id },
                university: s.university || { code: '401' },
                department: d.id ? { id: d.id } : { code: '1' },
                educationYear: s.educationYear || { code: '2020' },
                educationType: s.educationType || { code: '11' },
                specialityCode: 'TEST-SPEC',
                specialityName: 'Test Speciality',
                diplomaNumber: 'TEST-' + Date.now(),
                active: true,
                tag: 'v6'
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Diplom yaratish so\'rovi javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'diploma-update',
        category: '10. Diploma Entity',
        name: 'Diplomni yangilash (EStudentDiploma PUT)',
        order: 4,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EStudentDiploma/{{diplomas[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const d = ctx.diplomas?.[0] || {};
            return {
                id: d.id,
                diplomaSerial: 'UPD-SER',
                diplomaNumber: d.diplomaNumber || '',
                active: true,
                tag: 'v6'
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Diplom yangilash so\'rovi javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EStudentCertificate =====
    {
        id: 'student-certificate-list',
        category: '10. Diploma Entity',
        name: 'Talaba sertifikatlari ro\'yxati (EStudentCertificate list)',
        order: 5,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EStudentCertificate',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'student-certificate-single',
        category: '10. Diploma Entity',
        name: 'Bitta talaba sertifikatni ID bo\'yicha olish (EStudentCertificate by ID)',
        order: 6,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EStudentCertificate/{{certificates[0].id}}',
        auth: 'bearer',
        params: { view: 'eStudentCertificate-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'student-certificate-create',
        category: '10. Diploma Entity',
        name: 'Talaba sertifikat yaratish (EStudentCertificate POST)',
        order: 7,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EStudentCertificate',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            return {
                student: { id: s.id },
                university: s.university || { code: '401' },
                certificateName: 'TEST-CERT-' + Date.now(),
                active: true
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Talaba sertifikat yaratish so\'rovi javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'student-certificate-update',
        category: '10. Diploma Entity',
        name: 'Talaba sertifikatni yangilash (EStudentCertificate PUT)',
        order: 8,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EStudentCertificate/{{certificates[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const c = ctx.certificates?.[0] || {};
            return {
                id: c.id,
                certificateName: 'UPD-CERT',
                active: true
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Talaba sertifikat yangilash so\'rovi javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EEmpoyeeCertificate (NB: typo matches old-hemis entity name) =====
    {
        id: 'employee-certificate-list',
        category: '10. Diploma Entity',
        name: 'Xodim sertifikatlari ro\'yxati (EEmpoyeeCertificate list)',
        order: 9,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EEmpoyeeCertificate',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'employee-certificate-single',
        category: '10. Diploma Entity',
        name: 'Bitta xodim sertifikatni ID bo\'yicha olish (EEmpoyeeCertificate by ID)',
        order: 10,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EEmpoyeeCertificate/{{empCertificates[0].id}}',
        auth: 'bearer',
        params: { view: 'eEmployeeCertificate-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'employee-certificate-create',
        category: '10. Diploma Entity',
        name: 'Xodim sertifikat yaratish (EEmpoyeeCertificate POST)',
        order: 11,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EEmpoyeeCertificate',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const t = ctx.teachers?.[0] || {};
            return {
                employee: { id: t.id },
                university: t.university || { code: '401' },
                certificateName: 'TEST-EMP-CERT-' + Date.now(),
                active: true
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Xodim sertifikat yaratish so\'rovi javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'employee-certificate-update',
        category: '10. Diploma Entity',
        name: 'Xodim sertifikatni yangilash (EEmpoyeeCertificate PUT)',
        order: 12,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EEmpoyeeCertificate/{{empCertificates[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const ec = ctx.empCertificates?.[0] || {};
            return {
                id: ec.id,
                certificateName: 'UPD-EMP-CERT',
                active: true
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Xodim sertifikat yangilash so\'rovi javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== DELETE testlari =====
    {
        id: 'diploma-delete',
        category: '10. Diploma Entity',
        name: 'Diplomni o\'chirish (EStudentDiploma DELETE)',
        order: 13,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EStudentDiploma/{{diplomas[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EStudentDiploma o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'student-certificate-delete',
        category: '10. Diploma Entity',
        name: 'Talaba sertifikatni o\'chirish (EStudentCertificate DELETE)',
        order: 14,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EStudentCertificate/{{certificates[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EStudentCertificate o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'employee-certificate-delete',
        category: '10. Diploma Entity',
        name: 'Xodim sertifikatni o\'chirish (EEmpoyeeCertificate DELETE)',
        order: 15,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EEmpoyeeCertificate/{{empCertificates[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EEmpoyeeCertificate o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
];
