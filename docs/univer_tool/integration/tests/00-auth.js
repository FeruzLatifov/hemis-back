// 00. Auth tests
const tests_00 = [
    {
        id: 'auth-token',
        category: '00. Auth',
        name: 'OAuth2 Token olish (Password Grant)',
        order: 1,
        method: 'POST',
        url: '/app/rest/v2/oauth/token',
        auth: 'basic',
        contentType: 'form',
        body: {
            grant_type: 'password',
            username: '{{config.username}}',
            password: '{{config.password}}'
        },
        expectedStatus: 200,
        requiredFields: ['access_token', 'refresh_token', 'token_type'],
        storeValues: {
            accessToken: 'access_token',
            refreshToken: 'refresh_token'
        },
        customValidator: (body) => [{
            name: 'token_type = bearer',
            pass: body?.token_type === 'bearer'
        }]
    },
    {
        id: 'auth-refresh',
        category: '00. Auth',
        name: 'Token yangilash (Refresh Grant)',
        order: 2,
        method: 'POST',
        url: '/app/rest/v2/oauth/token',
        auth: 'basic',
        contentType: 'form',
        body: {
            grant_type: 'refresh_token',
            refresh_token: '{{storedValues.refreshToken}}'
        },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['access_token', 'refresh_token'],
        customValidator: (body) => [{
            name: 'Yangi token qaytdi',
            pass: typeof body?.access_token === 'string' && body.access_token.length > 10
        }]
    },
    {
        id: 'auth-userinfo',
        category: '00. Auth',
        name: 'Foydalanuvchi ma\'lumotlari (HemisResponseUserInfo)',
        order: 3,
        method: 'GET',
        url: '/app/rest/v2/userInfo',
        auth: 'bearer',
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['login', 'name', 'locale'],
        customValidator: (body) => [{
            name: 'login mavjud (PHP: $user->login)',
            pass: typeof body?.login === 'string' && body.login.length > 0
        }, {
            name: 'university mavjud (PHP: universitetni tekshiradi)',
            pass: body?.university != null,
            severity: body?.university != null ? 'pass' : 'warn'
        }]
    }
];
