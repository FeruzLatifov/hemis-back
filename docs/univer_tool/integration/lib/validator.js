// ResponseValidator — test natijalarini tekshirish
// Pure logic — DOM ga bog'liq emas

/**
 * Nested object ichidan qiymat olish
 * Masalan: getNestedValue({a: {b: 1}}, 'a.b') => 1
 *          getNestedValue([{id:1}], '[0].id') => 1
 */
function getNestedValue(obj, path) {
    if (!obj || !path) return undefined;
    const parts = path.replace(/\[(\d+)\]/g, '.$1').split('.');
    let current = obj;
    for (const part of parts) {
        if (current == null) return undefined;
        current = current[part];
    }
    return current;
}

class ResponseValidator {
    /**
     * Test natijasini validatsiya qiladi
     * @param {Object} test - test ta'rifi
     * @param {number} httpStatus - HTTP status kodi
     * @param {*} body - response body (parsed JSON yoki null)
     * @param {number} elapsed - javob vaqti (ms)
     * @returns {{ pass: boolean, checks: Array }}
     */
    static validate(test, httpStatus, body, elapsed) {
        const checks = [];

        // 1. HTTP status tekshirish
        const expectedStatus = test.expectedStatus || 200;
        checks.push({
            name: 'HTTP Status',
            expected: expectedStatus,
            actual: httpStatus,
            pass: httpStatus === expectedStatus
        });

        // 2. Javob vaqti (< 5s = pass, 5-30s = warn, > 30s = fail)
        const timeSeverity = elapsed < 5000 ? 'pass' : (elapsed < 30000 ? 'warn' : 'fail');
        checks.push({
            name: 'Javob vaqti',
            expected: '< 5000ms',
            actual: elapsed + 'ms',
            pass: elapsed < 30000,
            severity: timeSeverity
        });

        // 3. JSON parseable tekshiruvi
        // body === null → bo'sh javob (200 OK bilan ham valid — old-hemis compatible)
        // body is string → matn bor lekin JSON emas → FAIL
        if (httpStatus !== 204 && body !== null && typeof body === 'string') {
            checks.push({
                name: 'JSON format',
                expected: 'Valid JSON',
                actual: 'Parse xatosi',
                pass: false
            });
        }

        // 4. Majburiy fieldlarni tekshirish
        if (test.requiredFields && body != null) {
            for (const field of test.requiredFields) {
                const val = getNestedValue(body, field);
                checks.push({
                    name: 'Field: ' + field,
                    expected: 'mavjud',
                    actual: val != null ? typeof val : "yo'q",
                    pass: val != null
                });
            }
        }

        // 5. Array javob kutilganmi
        if (test.expectArray && body != null) {
            const isArr = Array.isArray(body);
            checks.push({
                name: 'Array javob',
                expected: 'Array',
                actual: isArr ? `Array[${body.length}]` : typeof body,
                pass: isArr
            });
        }

        // 6. Minimum array uzunligi
        if (test.minArrayLength != null && Array.isArray(body)) {
            checks.push({
                name: 'Array uzunligi',
                expected: '>= ' + test.minArrayLength,
                actual: body.length,
                pass: body.length >= test.minArrayLength
            });
        }

        // 7. Custom validator
        if (test.customValidator && typeof test.customValidator === 'function') {
            try {
                const customChecks = test.customValidator(body, httpStatus);
                if (Array.isArray(customChecks)) {
                    checks.push(...customChecks);
                }
            } catch (err) {
                checks.push({
                    name: 'Custom validator xatosi',
                    expected: 'Xatosiz',
                    actual: err.message,
                    pass: false
                });
            }
        }

        // Umumiy natija: barcha checklar pass yoki warn bo'lsa → pass
        const pass = checks.every(c => c.pass || c.severity === 'warn');
        return { pass, checks };
    }

    /**
     * Natijani qisqacha matn sifatida qaytaradi
     */
    static summary(validation) {
        const total = validation.checks.length;
        const passed = validation.checks.filter(c => c.pass).length;
        const warned = validation.checks.filter(c => c.severity === 'warn').length;
        const failed = total - passed;
        return `${passed}/${total} PASS` + (warned ? `, ${warned} WARN` : '') + (failed ? `, ${failed} FAIL` : '');
    }
}
