// 03. Teacher Services tests
const tests_03 = [
    {
        id: 'teacher-id',
        category: '03. Teacher Services',
        name: 'O\'qituvchi ID olish (PINFL bo\'yicha)',
        order: 1,
        method: 'POST',
        url: '/app/rest/v2/services/teacher/id',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const t = ctx.teachers?.[0] || {};
            return {
                data: {
                    citizenship: t.citizenship?.code || t.citizenshipCode || '120',
                    pinfl: t.pinfl,
                    serial: t.serialNumber || t.passportNumber || '',
                    gender: t.gender?.code || t.genderCode || '11',
                    year: t.employeeYear || t.yearOfEnter || '2020'
                }
            };
        },
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'unique_id yoki teacher qaytdi',
            pass: body?.unique_id !== undefined || body?.teacher !== undefined || body?.data !== undefined
        }]
    },
    {
        id: 'teacher-add-job',
        category: '03. Teacher Services',
        name: 'O\'qituvchiga ish qo\'shish',
        order: 2,
        method: 'POST',
        url: '/app/rest/v2/services/teacher/addJob',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const t = ctx.teachers?.[0] || {};
            const j = ctx.employeeJobs?.[0] || {};
            return {
                job: {
                    employee: { id: t.id },
                    university: t.university || { code: '401' },
                    department: j.department || t.department || { code: '001' },
                    employeeForm: j.employeeForm || { code: '10' },
                    employeeStatus: j.employeeStatus || { code: '10' },
                    employeeType: j.employeeType || null,
                    employeeRate: j.employeeRate || { code: '10' },
                    employeePosition: j.employeePosition || { code: '10' },
                    jobStartDate: j.jobStartDate || '2020-01-01',
                    tag: 'v5'
                }
            };
        },
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'Ish qo\'shish natijasi qaytdi',
            pass: body !== null && body !== undefined
        }]
    }
];
