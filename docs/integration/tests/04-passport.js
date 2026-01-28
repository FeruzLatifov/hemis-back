// 04. Passport Data tests
// PHP: HemisApi::getPassportData(), HemisApi::getPassportAddressData()
// PHP getDataBySN params: pinfl, seriaNumber, captchaValue, captchaId
// PHP getDataByPinflBirthdate params: birthdate, pinfl, captchaValue, captchaId
// PHP getAddress params: pinfl
const tests_04 = [
    {
        id: 'passport-by-sn',
        category: '04. Passport',
        name: 'Passport (seria+raqam, PHP: getDataBySN)',
        order: 1,
        method: 'GET',
        url: '/app/rest/v2/services/passport-data/getDataBySN',
        auth: 'bearer',
        dependsOn: ['auth-token', 'captcha-generate'],
        // PHP: ['pinfl'=>$pin, 'seriaNumber'=>$serial, 'captchaValue'=>$captcha, 'captchaId'=>$captchaId]
        params: {
            pinfl: '{{students[0].pinfl}}',
            seriaNumber: '{{students[0].serialNumber}}',
            captchaValue: '000000',
            captchaId: '{{storedValues.captchaId}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Passport so\'rovi javob qaytardi (200/400/403)',
            pass: status === 200 || status === 400 || status === 403
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }, {
            name: 'result yoki data mavjud (PHP: $result->result, $result->data)',
            pass: body?.result !== undefined || body?.data !== undefined || body?.error !== undefined,
            severity: 'warn'
        }]
    },
    {
        id: 'passport-by-pinfl-birthdate',
        category: '04. Passport',
        name: 'Passport (PINFL+sana, PHP: getDataByPinflBirthdate)',
        order: 2,
        method: 'GET',
        url: '/app/rest/v2/services/passport-data/getDataByPinflBirthdate',
        auth: 'bearer',
        dependsOn: ['auth-token', 'captcha-generate'],
        // PHP: ['birthdate'=>$birthDate, 'pinfl'=>$pin, 'captchaValue'=>$captcha, 'captchaId'=>$captchaId]
        params: {
            birthdate: '{{students[0].birthday}}',
            pinfl: '{{students[0].pinfl}}',
            captchaValue: '000000',
            captchaId: '{{storedValues.captchaId}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Passport so\'rovi javob qaytardi (200/400/403)',
            pass: status === 200 || status === 400 || status === 403
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }, {
            name: 'result yoki data mavjud (PHP: $result->result, $result->data)',
            pass: body?.result !== undefined || body?.data !== undefined || body?.error !== undefined,
            severity: 'warn'
        }]
    },
    {
        id: 'passport-address',
        category: '04. Passport',
        name: 'Manzil (PINFL, PHP: getPassportAddressData)',
        order: 3,
        method: 'GET',
        url: '/app/rest/v2/services/passport-data/getAddress',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        // PHP: ['pinfl' => $pin]
        params: {
            pinfl: '{{students[0].pinfl}}'
        },
        expectedStatus: 200,
        customValidator: (body, status) => [{
            name: 'Manzil so\'rovi javob qaytardi (200/400)',
            pass: status === 200 || status === 400
        }, {
            name: 'Javob mavjud',
            pass: body !== null && body !== undefined
        }, {
            name: 'AnswereMessage yoki Data mavjud (PHP: HemisResponsePassportAddress)',
            pass: body?.AnswereMessage !== undefined || body?.Data !== undefined || body?.result !== undefined,
            severity: 'warn'
        }]
    }
];
