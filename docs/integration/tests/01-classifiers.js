// 01. Classifiers tests
// PHP: HemisApi::getClassifiersInfo(), HemisApi::getCaptcha()
// Response modellar: HemisResponseClassifiersInfo, HemisResponseCaptcha
const tests_01 = [
    {
        id: 'classifiers-info',
        category: '01. Classifiers',
        name: 'Klassifikatorlar ro\'yxati (HemisResponseClassifiersInfo)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/services/classifiers/info',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'success = true (PHP: $result->success)',
            pass: body?.success === true
        }, {
            name: 'classifiers massiv mavjud (PHP: $result->classifiers)',
            pass: Array.isArray(body?.classifiers)
        }, {
            name: 'Kamida bitta klassifikator mavjud',
            pass: Array.isArray(body?.classifiers) && body.classifiers.length > 0
        }]
    },
    {
        id: 'classifiers-all-items',
        category: '01. Classifiers',
        name: 'Barcha klassifikator elementlari',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/services/classifiers/allItems',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        customValidator: (body) => [{
            name: 'Javob qaytdi',
            pass: body !== null && body !== undefined
        }]
    },
    {
        id: 'captcha-generate',
        category: '01. Classifiers',
        name: 'Captcha generatsiya (HemisResponseCaptcha)',
        order: 3,
        method: 'GET',
        url: '/app/rest/v2/services/captcha/getNumericCaptcha',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        // PHP response model: public $id; public $image;
        storeValues: {
            captchaId: 'id'
        },
        customValidator: (body) => [{
            name: 'id mavjud (PHP: $captcha->id)',
            pass: body?.id !== null && body?.id !== undefined
        }, {
            name: 'image mavjud (PHP: $captcha->image)',
            pass: typeof body?.image === 'string' && body.image.length > 0,
            severity: (typeof body?.image === 'string' && body.image.length > 0) ? 'pass' : 'warn'
        }]
    }
];
