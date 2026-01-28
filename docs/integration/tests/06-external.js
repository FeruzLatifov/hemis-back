// 06. External Services tests
// PHP manbalar: HemisApi.php, StudentDataSyncUpdater.php, StudentDataStipendUpdater.php,
//               ReportContractUpdater.php, ReportEmploymentUpdater.php, DiplomaBlankUpdater.php
const tests_06 = [
    // ===== BIMM xizmatlari =====
    {
        id: 'bimm-certificate',
        category: '06. External Services',
        name: 'BIMM sertifikat (PHP: bimm/certificate)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/services/bimm/certificate',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['pinfl' => $pinfl]
        params: {
            pinfl: '{{students[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'BIMM sertifikat so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'status yoki data mavjud (PHP: HemisResponseCertificate)',
            pass: body?.status !== undefined || body?.data !== undefined || body?.message !== undefined,
            severity: 'warn'
        }]
    },
    {
        id: 'bimm-academic-degree',
        category: '06. External Services',
        name: 'BIMM ilmiy daraja (PHP: bimm/academicDegree)',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/services/bimm/academicDegree',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['pinfl' => $pinfl]
        params: {
            pinfl: '{{teachers[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'BIMM ilmiy daraja so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'status yoki data mavjud',
            pass: body?.status !== undefined || body?.data !== undefined || body?.message !== undefined,
            severity: 'warn'
        }]
    },
    {
        id: 'bimm-poverty-register',
        category: '06. External Services',
        name: 'BIMM kam ta\'minlanganlar (PHP: bimm/provertyRegister)',
        order: 3,
        method: 'GET',
        url: '/app/rest/v2/services/bimm/provertyRegister',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['pinfl' => $pinfl] — "provertyRegister" typo PHP kodda ham shunday
        params: {
            pinfl: '{{students[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Kam ta\'minlanganlar so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },

    // ===== Ijtimoiy xizmatlar =====
    {
        id: 'social-single-register',
        category: '06. External Services',
        name: 'Ijtimoiy yagona reestr (PHP: social/singleRegister)',
        order: 4,
        method: 'GET',
        url: '/app/rest/v2/services/social/singleRegister',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['pinfl' => $pinfl]
        params: {
            pinfl: '{{students[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Ijtimoiy reestr so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'social-women',
        category: '06. External Services',
        name: 'Ayollar daftari (PHP: social/women)',
        order: 5,
        method: 'GET',
        url: '/app/rest/v2/services/social/women',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['pinfl' => $student->passport_pin, 'sn' => $student->passport_number]
        params: {
            pinfl: '{{students[0].pinfl}}',
            sn: '{{students[0].serialNumber}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Ayollar daftari so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },

    // ===== Bandlik xizmatlari =====
    {
        id: 'employment-workbook',
        category: '06. External Services',
        name: 'Mehnat daftarchasi (PHP: employment/workbook)',
        order: 6,
        method: 'GET',
        url: '/app/rest/v2/services/employment/workbook',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['pinfl' => $pinfl]
        params: {
            pinfl: '{{teachers[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Mehnat daftarchasi so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'employment-graduate-list',
        category: '06. External Services',
        name: 'Bitiruvchilar ro\'yxati (PHP: employment/graduateList)',
        order: 7,
        method: 'POST',
        url: '/app/rest/v2/services/employment/graduateList',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: json_encode(['employments' => [...sync data...]])
        bodyBuilder: (ctx) => {
            const uniCode = ctx.university?.code || '401';
            return {
                employments: [{
                    university: { code: uniCode },
                    department: { code: ctx.departments?.[0]?.code || '001' },
                    educationYear: { code: ctx.students?.[0]?.educationYear?.code || '2020' },
                    educationType: { code: '11' },
                    educationForm: { code: '11' },
                    paymentForm: { code: '11' },
                    gender: { code: '11' },
                    workplaceCompatibility: { code: '11' },
                    graduateInactiveType: null,
                    graduateFieldsType: null,
                    qty: 0
                }]
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Bitiruvchilar ro\'yxati so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },

    // ===== Yuridik shaxs =====
    {
        id: 'legal-entity-bank-requisites',
        category: '06. External Services',
        name: 'Bank rekvizitlari (PHP: legalentity/bankRequisites)',
        order: 8,
        method: 'GET',
        url: '/app/rest/v2/services/legalentity/bankRequisites',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['inn' => $tin] — university TIN (INN)
        params: {
            inn: '{{universities[0].tin}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Bank rekvizitlari so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'tin yoki name mavjud (PHP: HemisResponseLegalEntity)',
            pass: body?.tin !== undefined || body?.name !== undefined || body?.success !== undefined,
            severity: 'warn'
        }]
    },

    // ===== Billing xizmatlari =====
    {
        id: 'billing-scholarship',
        category: '06. External Services',
        name: 'Billing stipendiya (PHP: billing/scholarship)',
        order: 9,
        method: 'POST',
        url: '/app/rest/v2/services/billing/scholarship',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: json_encode(['tin' => $university->tin, 'pinfls' => $pins])
        bodyBuilder: (ctx) => {
            const uni = ctx.universities?.[0] || {};
            const s = ctx.students?.[0] || {};
            return {
                tin: uni.tin || '',
                pinfls: [s.pinfl || '00000000000000']
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Billing stipendiya so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'billing-invoice',
        category: '06. External Services',
        name: 'Billing invoice (PHP: billing/invoice)',
        order: 10,
        method: 'POST',
        url: '/app/rest/v2/services/billing/invoice',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: {params: {OrganizationId, EduFacultyId, EduYearId, EduTypeId}}
        bodyBuilder: (ctx) => {
            const uniCode = ctx.university?.code || '401';
            return {
                params: {
                    OrganizationId: uniCode,
                    EduFacultyId: '',
                    EduYearId: ctx.students?.[0]?.educationYear?.code || '2020',
                    EduTypeId: '11'
                }
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Billing invoice so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },

    // ===== UzASBO =====
    {
        id: 'uzasbo-scholarship',
        category: '06. External Services',
        name: 'UzASBO stipendiya (PHP: uzasbo/scholarship)',
        order: 11,
        method: 'GET',
        url: '/app/rest/v2/services/uzasbo/scholarship',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['inn' => $university->tin, 'year' => $educationYear, 'month' => $month]
        params: {
            inn: '{{universities[0].tin}}',
            year: '2024',
            month: '01'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'UzASBO stipendiya so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },

    // ===== Mutaxassislik va Guruh =====
    {
        id: 'speciality-get',
        category: '06. External Services',
        name: 'Mutaxassislik (PHP: speciality/get)',
        order: 12,
        method: 'GET',
        url: '/app/rest/v2/services/speciality/get',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['university' => $code, 'type' => $educationType, 'year' => $educationYear]
        params: {
            university: '{{university.code}}',
            type: '11',
            year: '2020'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Mutaxassislik so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'group-get',
        category: '06. External Services',
        name: 'Guruh (PHP: group/get)',
        order: 13,
        method: 'GET',
        url: '/app/rest/v2/services/group/get',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['university' => $code, 'type' => $educationType, 'year' => $educationYear]
        params: {
            university: '{{university.code}}',
            type: '11',
            year: '2020'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Guruh so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },

    // ===== Diplom blank =====
    {
        id: 'diplom-blank-get',
        category: '06. External Services',
        name: 'Diplom blank (PHP: diplom-blank/get)',
        order: 14,
        method: 'GET',
        url: '/app/rest/v2/services/diplom-blank/get',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['university' => self::getUniversity(), 'year' => $educationYear->code]
        params: {
            university: '{{university.code}}',
            year: '2020'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Diplom blank so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'diplom-blank-set-status',
        category: '06. External Services',
        name: 'Diplom blank status (PHP: diplom-blank/setStatus)',
        order: 15,
        method: 'GET',
        url: '/app/rest/v2/services/diplom-blank/setStatus',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['blankCode' => $model->number, 'statusCode' => $model->status]
        params: {
            blankCode: '{{diplomas[0].blankNumber}}',
            statusCode: '1'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Diplom blank status so\'rovi javob qaytardi',
            pass: status >= 200 && status < 600
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }]
    }
];
