// 05. University tests
// PHP: HemisApi::getUniversityApiConfig(), HemisApi::getUniversityProfile()
// Response modellar: HemisResponseApiConfig, HemisResponseVerifyCode
const tests_05 = [
    {
        id: 'university-config',
        category: '05. University',
        name: 'Universitet konfiguratsiyasi (HemisResponseApiConfig)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/services/university/config',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        // PHP HemisResponseApiConfig fieldlari:
        // gpaEdit, accreditationEdit, addStudent, allowGrouping, allowTransferOutside, oneId, gradingSystem
        customValidator: (body) => [{
            name: 'Javob qaytdi',
            pass: body !== null && body !== undefined
        }, {
            name: 'gpaEdit mavjud (PHP: $config->gpaEdit)',
            pass: body?.gpaEdit !== undefined,
            severity: body?.gpaEdit !== undefined ? 'pass' : 'warn'
        }, {
            name: 'addStudent mavjud (PHP: $config->addStudent)',
            pass: body?.addStudent !== undefined,
            severity: body?.addStudent !== undefined ? 'pass' : 'warn'
        }, {
            name: 'allowGrouping mavjud (PHP: $config->allowGrouping)',
            pass: body?.allowGrouping !== undefined,
            severity: body?.allowGrouping !== undefined ? 'pass' : 'warn'
        }, {
            name: 'oneId mavjud (PHP: $config->oneId)',
            pass: body?.oneId !== undefined,
            severity: body?.oneId !== undefined ? 'pass' : 'warn'
        }, {
            name: 'gradingSystem mavjud (PHP: $config->gradingSystem)',
            pass: body?.gradingSystem !== undefined,
            severity: body?.gradingSystem !== undefined ? 'pass' : 'warn'
        }]
    },
    {
        id: 'university-get',
        category: '05. University',
        name: 'Universitet ma\'lumotlari (PHP: university/get)',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/services/university/get',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['code' => $university->code]
        params: {
            code: '{{university.code}}'
        },
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'Universitet ma\'lumoti qaytdi',
            pass: body !== null && body !== undefined
        }, {
            name: 'status yoki data mavjud',
            pass: body?.status !== undefined || body?.data !== undefined || body?.university !== undefined,
            severity: 'warn'
        }]
    },
    {
        id: 'verify-code-send',
        category: '05. University',
        name: 'Tasdiqlash kodi yuborish (PHP: send/verifyCode)',
        order: 3,
        method: 'POST',
        url: '/app/rest/v2/services/send/verifyCode',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: {id, email, phone, verify_code, hash}
        bodyBuilder: (ctx) => {
            const s = ctx.students?.[0] || {};
            return {
                id: ctx.config?.username || 'otm401',
                email: s.email || 'test@example.com',
                phone: s.phone || '',
                verify_code: '1234',
                hash: ''
            };
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Kod yuborildi yoki xatolik qaytdi (200/400)',
            pass: status === 200 || status === 400
        }, {
            name: 'Javob qaytdi',
            pass: body !== null && body !== undefined
        }]
    }
];
