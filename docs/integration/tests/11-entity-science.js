// 11. Ilmiy faoliyat entity testlari
const tests_11 = [
    // ===== EProject =====
    {
        id: 'project-list',
        category: '11. Science Entity',
        name: 'Loyihalar ro\'yxatini olish (EProject list)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EProject',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'project-single',
        category: '11. Science Entity',
        name: 'Bitta loyihani ID bo\'yicha olish (EProject by ID)',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EProject/{{projects[0].id}}',
        auth: 'bearer',
        params: { view: 'eProject-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'project-create',
        category: '11. Science Entity',
        name: 'Loyiha yaratish (EProject POST)',
        order: 3,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EProject',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            name: 'TEST-PROJECT-' + Date.now(),
            university: { code: ctx.university ? ctx.university.code : '401' },
            projectType: { code: '11' },
            startDate: '2024-01-01',
            endDate: '2025-12-31',
            contractDate: '2024-01-01',
            version: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProject yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'project-update',
        category: '11. Science Entity',
        name: 'Loyihani yangilash (EProject PUT)',
        order: 4,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EProject/{{projects[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.projects ? ctx.projects[0].id : undefined,
            name: 'UPD-PROJECT',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProject yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EProjectMeta =====
    {
        id: 'project-meta-list',
        category: '11. Science Entity',
        name: 'Loyiha metalari ro\'yxati (EProjectMeta list)',
        order: 5,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EProjectMeta',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'project-meta-create',
        category: '11. Science Entity',
        name: 'Loyiha meta yaratish (EProjectMeta POST)',
        order: 6,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EProjectMeta',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            project: ctx.projects ? { id: ctx.projects[0].id } : undefined,
            fiscalYear: 2024,
            budget: 0,
            quantityMembers: 0,
            version: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProjectMeta yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'project-meta-update',
        category: '11. Science Entity',
        name: 'Loyiha metani yangilash (EProjectMeta PUT)',
        order: 7,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EProjectMeta/{{projectMetas[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.projectMetas ? ctx.projectMetas[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProjectMeta yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EProjectExecutor =====
    {
        id: 'project-executor-list',
        category: '11. Science Entity',
        name: 'Loyiha ijrochilari ro\'yxati (EProjectExecutor list)',
        order: 8,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EProjectExecutor',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'project-executor-create',
        category: '11. Science Entity',
        name: 'Loyiha ijrochi yaratish (EProjectExecutor POST)',
        order: 9,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EProjectExecutor',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            project: ctx.projects ? { id: ctx.projects[0].id } : undefined,
            employee: ctx.teachers ? { id: ctx.teachers[0].id } : undefined,
            version: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProjectExecutor yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'project-executor-update',
        category: '11. Science Entity',
        name: 'Loyiha ijrochini yangilash (EProjectExecutor PUT)',
        order: 10,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EProjectExecutor/{{projectExecutors[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.projectExecutors ? ctx.projectExecutors[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProjectExecutor yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EPublicationScientific =====
    {
        id: 'publication-scientific-list',
        category: '11. Science Entity',
        name: 'Ilmiy nashrlar ro\'yxati (EPublicationScientific list)',
        order: 11,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EPublicationScientific',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'publication-scientific-single',
        category: '11. Science Entity',
        name: 'Bitta ilmiy nashrni olish (EPublicationScientific by ID)',
        order: 12,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EPublicationScientific/{{publicationsScientific[0].id}}',
        auth: 'bearer',
        params: { view: 'ePublicationScientific-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'publication-scientific-create',
        category: '11. Science Entity',
        name: 'Ilmiy nashr yaratish (EPublicationScientific POST)',
        order: 13,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EPublicationScientific',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            name: 'TEST-SCI-PUB-' + Date.now(),
            university: { code: ctx.university ? ctx.university.code : '401' },
            country: { code: 'UZ' },
            authors: 'Test Author',
            issueYear: 2024,
            authorCounts: 1,
            isChecked: false,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationScientific yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'publication-scientific-update',
        category: '11. Science Entity',
        name: 'Ilmiy nashrni yangilash (EPublicationScientific PUT)',
        order: 14,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EPublicationScientific/{{publicationsScientific[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.publicationsScientific ? ctx.publicationsScientific[0].id : undefined,
            name: 'UPD-SCI-PUB',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationScientific yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EPublicationMethodical =====
    {
        id: 'publication-methodical-list',
        category: '11. Science Entity',
        name: 'Metodik nashrlar ro\'yxati (EPublicationMethodical list)',
        order: 15,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EPublicationMethodical',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'publication-methodical-single',
        category: '11. Science Entity',
        name: 'Bitta metodik nashrni olish (EPublicationMethodical by ID)',
        order: 16,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EPublicationMethodical/{{publicationsMethodical[0].id}}',
        auth: 'bearer',
        params: { view: 'ePublicationMethodical-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'publication-methodical-create',
        category: '11. Science Entity',
        name: 'Metodik nashr yaratish (EPublicationMethodical POST)',
        order: 17,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EPublicationMethodical',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            name: 'TEST-METH-PUB-' + Date.now(),
            university: { code: ctx.university ? ctx.university.code : '401' },
            country: { code: 'UZ' },
            authors: 'Test Author',
            issueYear: 2024,
            authorCounts: 1,
            isChecked: false,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationMethodical yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'publication-methodical-update',
        category: '11. Science Entity',
        name: 'Metodik nashrni yangilash (EPublicationMethodical PUT)',
        order: 18,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EPublicationMethodical/{{publicationsMethodical[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.publicationsMethodical ? ctx.publicationsMethodical[0].id : undefined,
            name: 'UPD-METH-PUB',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationMethodical yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EPublicationProperty =====
    {
        id: 'publication-property-list',
        category: '11. Science Entity',
        name: 'Intellektual mulk nashrlar ro\'yxati (EPublicationProperty list)',
        order: 19,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EPublicationProperty',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'publication-property-single',
        category: '11. Science Entity',
        name: 'Bitta intellektual mulk nashrni olish (EPublicationProperty by ID)',
        order: 20,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EPublicationProperty/{{publicationsProperty[0].id}}',
        auth: 'bearer',
        params: { view: 'ePublicationProperty-view', dynamicAttributes: 'true', returnNulls: 'true' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['id'],
    },
    {
        id: 'publication-property-create',
        category: '11. Science Entity',
        name: 'Intellektual mulk nashr yaratish (EPublicationProperty POST)',
        order: 21,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EPublicationProperty',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            name: 'TEST-PROP-PUB-' + Date.now(),
            university: { code: ctx.university ? ctx.university.code : '401' },
            country: { code: 'UZ' },
            authors: 'Test Author',
            issueYear: 2024,
            authorCounts: 1,
            isChecked: false,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationProperty yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'publication-property-update',
        category: '11. Science Entity',
        name: 'Intellektual mulk nashrni yangilash (EPublicationProperty PUT)',
        order: 22,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EPublicationProperty/{{publicationsProperty[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.publicationsProperty ? ctx.publicationsProperty[0].id : undefined,
            name: 'UPD-PROP-PUB',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationProperty yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EPublicationAuthorMeta =====
    {
        id: 'publication-author-meta-list',
        category: '11. Science Entity',
        name: 'Nashr mualliflari metalari ro\'yxati (EPublicationAuthorMeta list)',
        order: 23,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EPublicationAuthorMeta',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'publication-author-meta-create',
        category: '11. Science Entity',
        name: 'Nashr muallif meta yaratish (EPublicationAuthorMeta POST)',
        order: 24,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EPublicationAuthorMeta',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            teacher: ctx.teachers ? { id: ctx.teachers[0].id } : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationAuthorMeta yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'publication-author-meta-update',
        category: '11. Science Entity',
        name: 'Nashr muallif metani yangilash (EPublicationAuthorMeta PUT)',
        order: 25,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{{publicationAuthorMetas[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.publicationAuthorMetas ? ctx.publicationAuthorMetas[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationAuthorMeta yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EResearchActivity =====
    {
        id: 'research-activity-list',
        category: '11. Science Entity',
        name: 'Ilmiy faoliyatlar ro\'yxati (EResearchActivity list)',
        order: 26,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EResearchActivity',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'research-activity-create',
        category: '11. Science Entity',
        name: 'Ilmiy faoliyat yaratish (EResearchActivity POST)',
        order: 27,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EResearchActivity',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            name: 'TEST-RESEARCH-' + Date.now(),
            university: { code: ctx.university ? ctx.university.code : '401' },
            version: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EResearchActivity yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'research-activity-update',
        category: '11. Science Entity',
        name: 'Ilmiy faoliyatni yangilash (EResearchActivity PUT)',
        order: 28,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EResearchActivity/{{researchActivities[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.researchActivities ? ctx.researchActivities[0].id : undefined,
            name: 'UPD-RESEARCH',
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EResearchActivity yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EDoctorateStudent =====
    {
        id: 'doctorate-student-list',
        category: '11. Science Entity',
        name: 'Doktorantlar ro\'yxati (EDoctorateStudent list)',
        order: 29,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EDoctorateStudent',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'doctorate-student-create',
        category: '11. Science Entity',
        name: 'Doktorant yaratish (EDoctorateStudent POST)',
        order: 30,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EDoctorateStudent',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            firstName: 'Test',
            secondName: 'Doctorate',
            thirdName: 'Student',
            birthDate: '1990-01-01',
            passportPin: '30101900000001',
            university: { code: ctx.university ? ctx.university.code : '401' },
            version: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EDoctorateStudent yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'doctorate-student-update',
        category: '11. Science Entity',
        name: 'Doktorantni yangilash (EDoctorateStudent PUT)',
        order: 31,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EDoctorateStudent/{{doctorateStudents[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.doctorateStudents ? ctx.doctorateStudents[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EDoctorateStudent yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== EDissertationDefense =====
    {
        id: 'dissertation-defense-list',
        category: '11. Science Entity',
        name: 'Dissertatsiya himoyalari ro\'yxati (EDissertationDefense list)',
        order: 32,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_EDissertationDefense',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'dissertation-defense-create',
        category: '11. Science Entity',
        name: 'Dissertatsiya himoya yaratish (EDissertationDefense POST)',
        order: 33,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_EDissertationDefense',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            doctorateStudent: ctx.doctoralStudents && ctx.doctoralStudents[0] ? { id: ctx.doctoralStudents[0].id } : undefined,
            defenseDate: '2024-06-15',
            approvedDate: '2024-07-01',
            version: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EDissertationDefense yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'dissertation-defense-update',
        category: '11. Science Entity',
        name: 'Dissertatsiya himoyani yangilash (EDissertationDefense PUT)',
        order: 34,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_EDissertationDefense/{{dissertationDefenses[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.dissertationDefenses ? ctx.dissertationDefenses[0].id : undefined,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EDissertationDefense yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== DELETE testlari =====
    {
        id: 'project-delete',
        category: '11. Science Entity',
        name: 'Loyihani o\'chirish (EProject DELETE)',
        order: 35,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EProject/{{projects[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProject o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'project-meta-delete',
        category: '11. Science Entity',
        name: 'Loyiha metani o\'chirish (EProjectMeta DELETE)',
        order: 36,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EProjectMeta/{{projectMetas[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProjectMeta o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'project-executor-delete',
        category: '11. Science Entity',
        name: 'Loyiha ijrochini o\'chirish (EProjectExecutor DELETE)',
        order: 37,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EProjectExecutor/{{projectExecutors[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EProjectExecutor o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'publication-scientific-delete',
        category: '11. Science Entity',
        name: 'Ilmiy nashrni o\'chirish (EPublicationScientific DELETE)',
        order: 38,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EPublicationScientific/{{publicationsScientific[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationScientific o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'publication-methodical-delete',
        category: '11. Science Entity',
        name: 'Metodik nashrni o\'chirish (EPublicationMethodical DELETE)',
        order: 39,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EPublicationMethodical/{{publicationsMethodical[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationMethodical o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'publication-property-delete',
        category: '11. Science Entity',
        name: 'Intellektual mulk nashrni o\'chirish (EPublicationProperty DELETE)',
        order: 40,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EPublicationProperty/{{publicationsProperty[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationProperty o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'publication-author-meta-delete',
        category: '11. Science Entity',
        name: 'Nashr muallif metani o\'chirish (EPublicationAuthorMeta DELETE)',
        order: 41,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{{publicationAuthorMetas[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EPublicationAuthorMeta o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'research-activity-delete',
        category: '11. Science Entity',
        name: 'Ilmiy faoliyatni o\'chirish (EResearchActivity DELETE)',
        order: 42,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EResearchActivity/{{researchActivities[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EResearchActivity o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'doctorate-student-delete',
        category: '11. Science Entity',
        name: 'Doktorantni o\'chirish (EDoctorateStudent DELETE)',
        order: 43,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EDoctorateStudent/{{doctorateStudents[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EDoctorateStudent o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'dissertation-defense-delete',
        category: '11. Science Entity',
        name: 'Dissertatsiya himoyani o\'chirish (EDissertationDefense DELETE)',
        order: 44,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_EDissertationDefense/{{dissertationDefenses[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'EDissertationDefense o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
];
