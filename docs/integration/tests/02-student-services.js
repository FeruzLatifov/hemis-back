// 02. Student Services tests
const tests_02 = [
    {
        id: 'student-id',
        category: '02. Student Services',
        name: 'Talaba ID olish (PINFL bo\'yicha)',
        order: 1,
        method: 'POST',
        url: '/app/rest/v2/services/student/id',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            return {
                data: {
                    citizenship: s.citizenship?.code || s.citizenshipCode || '120',
                    pinfl: s.pinfl,
                    serial: s.serialNumber || s.passportNumber || '',
                    year: s.educationYear?.code || s.yearOfEnter || '2020',
                    education_type: s.educationType?.code || s.educationTypeCode || '11'
                }
            };
        },
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'success yoki unique_id qaytdi',
            pass: body?.success !== undefined || body?.unique_id !== undefined || body?.data !== undefined
        }]
    },
    {
        id: 'student-validate',
        category: '02. Student Services',
        name: 'Talaba ma\'lumotlarini tekshirish',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/services/student/validate',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        params: {
            data: '{{students[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'Validatsiya natijasi qaytdi',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'student-update',
        category: '02. Student Services',
        name: 'Talaba ma\'lumotlarini yangilash',
        order: 3,
        method: 'POST',
        url: '/app/rest/v2/services/student/update',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP format: student transfer so'rovi
        // Xavfsiz test: o'z OTM kodini yuboramiz → transfer candidate topilmaydi → bo'sh javob (normal)
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            const uniCode = ctx.university?.code || '401';
            return {
                student: {
                    id: s.id,
                    fathername: s.fatherName || s.fathername || 'TEST',
                    university: { code: uniCode },        // o'z OTM — transfer bo'lmaydi
                    studentStatus: { code: '12' }          // PHP format: "chetlashgan"
                }
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Transfer so\'rovi qabul qilindi (bo\'sh javob = transfer yo\'q — normal)',
            // old-hemis: transfer candidate yo'q bo'lsa 200 OK + bo'sh body qaytaradi
            pass: status === 200
        }]
    },
    {
        id: 'student-gpa-get',
        category: '02. Student Services',
        name: 'Talaba GPA olish (GET)',
        order: 4,
        method: 'GET',
        url: '/app/rest/v2/services/student/gpa',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        params: {
            data: '{{students[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'GPA ma\'lumoti qaytdi',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'student-gpa-post',
        category: '02. Student Services',
        name: 'Talaba GPA olish (POST)',
        order: 5,
        method: 'POST',
        url: '/app/rest/v2/services/student/gpa',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            const g = ctx.gpa?.[0] || {};
            return {
                gpa: {
                    studentId: { id: s.id },
                    educationYear: g.educationYear || s.educationYear || { code: '2020' },
                    level: g.level || { code: '10' },
                    gpa: g.gpa || 3.5,
                    creditSum: g.creditSum || 0,
                    subjects: g.subjects || 0,
                    debtSubjects: g.debtSubjects || 0
                }
            };
        },
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'GPA ma\'lumoti qaytdi',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'student-contract-statistics',
        category: '02. Student Services',
        name: 'Kontrakt statistikasi (PHP: ReportContractUpdater)',
        order: 6,
        method: 'POST',
        url: '/app/rest/v2/services/student/contractStatistics',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: json_encode({contractStatistics: {university, educationYear, educationType, ...}})
        bodyBuilder: (ctx) => {
            const uniCode = ctx.university?.code || ctx.students?.[0]?.university?.code || '401';
            const eduYear = ctx.students?.[0]?.educationYear?.code || '2020';
            return {
                contractStatistics: {
                    university: { code: uniCode },
                    educationYear: { code: eduYear },
                    educationType: { code: '11' },
                    educationForm: { code: '11' },
                    faculty: { code: ctx.departments?.[0]?.code || '001' },
                    course: { code: '1' },
                    semester: { code: '11' },
                    dailyCount: 0,
                    total: 0,
                    date: new Date().toISOString().split('T')[0]
                }
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Kontrakt statistikasi javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'student-check-scholarship',
        category: '02. Student Services',
        name: 'Stipendiya tekshiruvi',
        order: 7,
        method: 'POST',
        url: '/app/rest/v2/services/student/checkScholarship2',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        body: {
            data: '{{students[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'Stipendiya natijasi qaytdi',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'doctoral-student-id',
        category: '02. Student Services',
        name: 'Doktorant ID olish (PINFL bo\'yicha)',
        order: 8,
        method: 'POST',
        url: '/app/rest/v2/services/doctoral-student/id',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            return {
                data: {
                    citizenship: s.citizenship?.code || s.citizenshipCode || '120',
                    pinfl: s.pinfl,
                    serial: s.serialNumber || s.passportNumber || '',
                    year: s.educationYear?.code || s.yearOfEnter || '2020',
                    education_type: s.educationType?.code || s.educationTypeCode || '11'
                }
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Doktorant ID so\'rovi javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    },
    {
        id: 'student-gpa-delete',
        category: '02. Student Services',
        name: 'Talaba GPA o\'chirish (service DELETE)',
        order: 9,
        method: 'DELETE',
        url: '/app/rest/v2/services/student/gpa/{{gpa[0].id}}',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'GPA o\'chirish javob qaytardi',
            pass: status >= 200 && status < 500
        }]
    }
];
