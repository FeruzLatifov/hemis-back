// 13. Statistik hisobot entity testlari (GenericStat)
// RIctEquipment, RLaboratories, REducationMaterials
const tests_13 = [
    // ===== RIctEquipment (AKT jihozlari) =====
    {
        id: 'ict-equipment-list',
        category: '13. Stats Entity',
        name: 'AKT jihozlari ro\'yxati (RIctEquipment list)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RIctEquipment',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'ict-equipment-create',
        category: '13. Stats Entity',
        name: 'AKT jihozi yaratish (RIctEquipment POST)',
        order: 2,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RIctEquipment',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            roomCount: 0,
            validProjectorCount: 0,
            invalidProjectorCount: 0,
            totalCount: 0,
            totalGrade: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIctEquipment yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'ict-equipment-update',
        category: '13. Stats Entity',
        name: 'AKT jihozini yangilash (RIctEquipment PUT)',
        order: 3,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RIctEquipment/{{ictEquipment[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.ictEquipment ? ctx.ictEquipment[0].id : undefined,
            roomCount: 1,
            validProjectorCount: 1,
            invalidProjectorCount: 0,
            totalCount: 1,
            totalGrade: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIctEquipment yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'ict-equipment-delete',
        category: '13. Stats Entity',
        name: 'AKT jihozini o\'chirish (RIctEquipment DELETE)',
        order: 4,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RIctEquipment/{{ictEquipment[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RIctEquipment o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== RLaboratories (Laboratoriyalar) =====
    {
        id: 'laboratories-list',
        category: '13. Stats Entity',
        name: 'Laboratoriyalar ro\'yxati (RLaboratories list)',
        order: 5,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_RLaboratories',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'laboratories-create',
        category: '13. Stats Entity',
        name: 'Laboratoriya yaratish (RLaboratories POST)',
        order: 6,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_RLaboratories',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            specialityCode: '',
            specialityName: 'Test',
            studentCount: 0,
            validLaboratoriesCount: 0,
            invalidLaboratoriesCount: 0,
            totalLaboratories: 0,
            totalGrade: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RLaboratories yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'laboratories-update',
        category: '13. Stats Entity',
        name: 'Laboratoriyani yangilash (RLaboratories PUT)',
        order: 7,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_RLaboratories/{{laboratories[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.laboratories ? ctx.laboratories[0].id : undefined,
            specialityName: 'Updated Test',
            studentCount: 1,
            validLaboratoriesCount: 1,
            invalidLaboratoriesCount: 0,
            totalLaboratories: 1,
            totalGrade: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RLaboratories yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'laboratories-delete',
        category: '13. Stats Entity',
        name: 'Laboratoriyani o\'chirish (RLaboratories DELETE)',
        order: 8,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_RLaboratories/{{laboratories[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'RLaboratories o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },

    // ===== REducationMaterials (O'quv materiallari) =====
    {
        id: 'education-materials-list',
        category: '13. Stats Entity',
        name: 'O\'quv materiallari ro\'yxati (REducationMaterials list)',
        order: 9,
        method: 'GET',
        url: '/app/rest/v2/entities/hemishe_REducationMaterials',
        auth: 'bearer',
        params: { limit: '3', view: '_local' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        expectArray: true,
    },
    {
        id: 'education-materials-create',
        category: '13. Stats Entity',
        name: 'O\'quv material yaratish (REducationMaterials POST)',
        order: 10,
        method: 'POST',
        url: '/app/rest/v2/entities/hemishe_REducationMaterials',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            university: { code: ctx.university ? ctx.university.code : '401' },
            educationYear: { code: '2024' },
            specialityCode: '',
            specialityName: 'Test',
            subjectCount: 0,
            textbooksCount: 0,
            createdMaterialsGrade: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'REducationMaterials yaratish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'education-materials-update',
        category: '13. Stats Entity',
        name: 'O\'quv materialni yangilash (REducationMaterials PUT)',
        order: 11,
        method: 'PUT',
        url: '/app/rest/v2/entities/hemishe_REducationMaterials/{{educationMaterials[0].id}}?responseView=_local',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx, stored) => ({
            id: ctx.educationMaterials ? ctx.educationMaterials[0].id : undefined,
            specialityName: 'Updated Test',
            subjectCount: 1,
            textbooksCount: 1,
            createdMaterialsGrade: 1,
        }),
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'REducationMaterials yangilash javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'education-materials-delete',
        category: '13. Stats Entity',
        name: 'O\'quv materialni o\'chirish (REducationMaterials DELETE)',
        order: 12,
        method: 'DELETE',
        url: '/app/rest/v2/entities/hemishe_REducationMaterials/{{educationMaterials[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'REducationMaterials o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
];
