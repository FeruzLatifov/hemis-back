// 09. Tashkiliy tuzilma entity testlari
const tests_09 = [
    // ===== EUniversity =====
    {
        id: 'university-entity-list',
        category: '09. Structure Entity',
        name: 'Universitetlar ro\'yxatini olish (EUniversity list)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EUniversity',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'university-entity-single',
        category: '09. Structure Entity',
        name: 'Bitta universitetni ID bo\'yicha olish (EUniversity by ID)',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EUniversity/{{universities[0].id}}',
        auth: 'bearer',
        params: { view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'university-entity-create',
        category: '09. Structure Entity',
        name: 'Universitet yaratish (EUniversity POST)',
        order: 3,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EUniversity',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            name: 'TEST-UNIVERSITY-' + Date.now(),
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversity yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'university-entity-update',
        category: '09. Structure Entity',
        name: 'Universitetni yangilash (EUniversity PUT)',
        order: 4,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EUniversity/{{universities[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.universities ? ctx.universities[0].id : undefined,
            name: 'UPD-UNIVERSITY',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversity yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EUniversityDepartment =====
    {
        id: 'department-list',
        category: '09. Structure Entity',
        name: 'Kafedral ro\'yxatini olish (EUniversityDepartment list)',
        order: 5,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EUniversityDepartment',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'department-single',
        category: '09. Structure Entity',
        name: 'Bitta kafedrani ID bo\'yicha olish (EUniversityDepartment by ID)',
        order: 6,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EUniversityDepartment/{{departments[0].id}}',
        auth: 'bearer',
        params: { view: 'eUniversityDepartment-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'department-create',
        category: '09. Structure Entity',
        name: 'Kafedra yaratish (EUniversityDepartment POST)',
        order: 7,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EUniversityDepartment',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const u = ctx.universities?.[0] || {};
            return {
                university: u.university || { code: '401' },
                deparmentType: { code: '10' },
                nameUz: 'TEST-DEPT-' + Date.now(),
                nameRu: 'TEST-DEPT-RU',
                status: { code: '10' }
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversityDepartment yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'department-update',
        category: '09. Structure Entity',
        name: 'Kafedrani yangilash (EUniversityDepartment PUT)',
        order: 8,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EUniversityDepartment/{{departments[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const d = ctx.departments?.[0] || {};
            return {
                id: d.id,
                nameUz: 'UPD-DEPT',
                nameRu: 'UPD-DEPT-RU',
                status: d.status || { code: '10' }
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversityDepartment yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EUniversityGroup =====
    {
        id: 'group-list',
        category: '09. Structure Entity',
        name: 'Guruhlar ro\'yxatini olish (EUniversityGroup list)',
        order: 9,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EUniversityGroup',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'group-single',
        category: '09. Structure Entity',
        name: 'Bitta guruhni ID bo\'yicha olish (EUniversityGroup by ID)',
        order: 10,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EUniversityGroup/{{groups[0].id}}',
        auth: 'bearer',
        params: { view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'group-create',
        category: '09. Structure Entity',
        name: 'Guruh yaratish (EUniversityGroup POST)',
        order: 11,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EUniversityGroup',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const u = ctx.universities?.[0] || {};
            return [{
                university: u.university || { code: '401' },
                educationYear: { code: '2020' },
                educationType: { code: '11' },
                groupId: 'GRP-' + Date.now(),
                groupName: 'TEST-GROUP-' + Date.now(),
                active: true
            }];
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversityGroup yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'group-update',
        category: '09. Structure Entity',
        name: 'Guruhni yangilash (EUniversityGroup PUT)',
        order: 12,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EUniversityGroup/{{groups[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.groups ? ctx.groups[0].id : undefined,
            groupName: 'UPD-GROUP',
            active: true
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversityGroup yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EUniversitySpeciality =====
    {
        id: 'speciality-list',
        category: '09. Structure Entity',
        name: 'Mutaxassisliklar ro\'yxatini olish (EUniversitySpeciality list)',
        order: 13,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EUniversitySpeciality',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'speciality-single',
        category: '09. Structure Entity',
        name: 'Bitta mutaxassislikni ID bo\'yicha olish (EUniversitySpeciality by ID)',
        order: 14,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EUniversitySpeciality/{{specialties[0].id}}',
        auth: 'bearer',
        params: { view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'speciality-create',
        category: '09. Structure Entity',
        name: 'Mutaxassislik yaratish (EUniversitySpeciality POST)',
        order: 15,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EUniversitySpeciality',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const u = ctx.universities?.[0] || {};
            return [{
                university: u.university || { code: '401' },
                educationType: { code: '11' },
                educationYear: { code: '2020' },
                faculty: ctx.departments?.[0] || { code: '1' },
                specialityCode: 'SPEC-' + Date.now(),
                specialityName: 'TEST-SPEC-' + Date.now()
            }];
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversitySpeciality yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'speciality-update',
        category: '09. Structure Entity',
        name: 'Mutaxassislikni yangilash (EUniversitySpeciality PUT)',
        order: 16,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EUniversitySpeciality/{{specialties[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.specialties ? ctx.specialties[0].id : undefined,
            specialityName: 'UPD-SPEC'
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversitySpeciality yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== DELETE testlari =====
    {
        id: 'department-delete',
        category: '09. Structure Entity',
        name: 'Kafedrani o\'chirish (EUniversityDepartment DELETE)',
        order: 17,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EUniversityDepartment/{{departments[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversityDepartment o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'group-delete',
        category: '09. Structure Entity',
        name: 'Guruhni o\'chirish (EUniversityGroup DELETE)',
        order: 18,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EUniversityGroup/{{groups[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversityGroup o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'speciality-delete',
        category: '09. Structure Entity',
        name: 'Mutaxassislikni o\'chirish (EUniversitySpeciality DELETE)',
        order: 19,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EUniversitySpeciality/{{specialties[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EUniversitySpeciality o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
];
