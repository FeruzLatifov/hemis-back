#!/usr/bin/env node
/**
 * HEMIS Endpoint Comparator
 * Har bir endpointni old-hemis (:8082) va hemis-back (:8081) da sinab solishtiradi.
 * Natijani JSON faylga yozadi — Ralph Loop uchun.
 */

const OLD_URL = 'http://localhost:8082';
const NEW_URL = 'http://localhost:8081';
const PROXY_URL = 'http://localhost:9001';
const USERNAME = 'otm401';
const PASSWORD = 'XCZDAb7qvGTXxz';

// ─── Helper ───
function getNestedValue(obj, path) {
    const parts = path.replace(/\[(\d+)\]/g, '.$1').split('.');
    let current = obj;
    for (const part of parts) {
        if (current == null) return undefined;
        current = current[part];
    }
    return current;
}

const ResponseValidator = {
    validate(test, httpStatus, body, elapsed) {
        const checks = [];
        let pass = true;
        const expectedStatus = test.expectedStatus || 200;
        const statusOk = Array.isArray(expectedStatus)
            ? expectedStatus.includes(httpStatus)
            : httpStatus === expectedStatus;
        checks.push({ name: 'HTTP Status', expected: expectedStatus, actual: httpStatus, pass: statusOk });
        if (!statusOk) pass = false;
        if (test.validate && typeof test.validate === 'function' && body !== null) {
            try {
                const customChecks = test.validate(body, httpStatus);
                if (Array.isArray(customChecks)) {
                    for (const c of customChecks) {
                        checks.push(c);
                        if (!c.pass) pass = false;
                    }
                }
            } catch (e) {
                checks.push({ name: 'Validate error', expected: 'no error', actual: e.message, pass: false });
                pass = false;
            }
        }
        return { pass, checks };
    }
};

// ─── Auth ───
async function getToken(baseUrl) {
    const formData = new URLSearchParams();
    formData.append('grant_type', 'password');
    formData.append('username', USERNAME);
    formData.append('password', PASSWORD);

    const response = await fetch(baseUrl + '/app/rest/v2/oauth/token', {
        method: 'POST',
        headers: {
            'Authorization': 'Basic Y2xpZW50OnNlY3JldA==',
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
    });
    if (!response.ok) throw new Error(`Auth failed on ${baseUrl}: ${response.status}`);
    const data = await response.json();
    return data.access_token;
}

// ─── Single request ───
async function sendRequest(baseUrl, token, test, testData, storedValues) {
    try {
        // URL
        let url = resolvePlaceholders(test.url, testData, storedValues);
        url = baseUrl + url;
        if (test.params) {
            const resolved = {};
            for (const [k, v] of Object.entries(test.params))
                resolved[k] = resolvePlaceholders(String(v), testData, storedValues);
            const qs = new URLSearchParams(resolved).toString();
            if (qs) url += (url.includes('?') ? '&' : '?') + qs;
        }

        // Headers
        const headers = {};
        if (test.auth === 'bearer') headers['Authorization'] = 'Bearer ' + token;
        else if (test.auth === 'basic') headers['Authorization'] = 'Basic Y2xpZW50OnNlY3JldA==';

        // Body
        let body = null;
        if (test.contentType === 'multipart' || test.contentType === 'form') {
            headers['Content-Type'] = 'application/x-www-form-urlencoded';
            if (test.body) {
                const formData = new URLSearchParams();
                const resolvedBody = resolveObjectPlaceholders(test.body, testData, storedValues);
                for (const [k, v] of Object.entries(resolvedBody)) formData.append(k, v);
                body = formData.toString();
            }
        } else {
            headers['Content-Type'] = 'application/json';
            headers['Accept'] = 'application/json';
            if (test.bodyBuilder && typeof test.bodyBuilder === 'function') {
                body = JSON.stringify(test.bodyBuilder(testData, storedValues));
            } else if (test.body) {
                body = JSON.stringify(resolveObjectPlaceholders(test.body, testData, storedValues));
            }
        }

        const options = { method: test.method || 'GET', headers };
        if (body && test.method !== 'GET') options.body = body;

        const start = Date.now();
        const response = await fetch(url, options);
        const text = await response.text();
        let respBody = null;
        if (text && text.trim().length > 0) {
            try { respBody = JSON.parse(text); } catch (e) { respBody = text; }
        }

        return {
            status: response.status,
            body: respBody,
            time: Date.now() - start,
            url,
            error: null
        };
    } catch (err) {
        return { status: 0, body: null, time: 0, url: '', error: err.message };
    }
}

function resolvePlaceholders(str, testData, storedValues) {
    if (typeof str !== 'string') return str;
    return str.replace(/\{\{(.+?)\}\}/g, (_, path) => {
        const context = { ...testData, storedValues };
        const val = getNestedValue(context, path.trim());
        return val != null ? String(val) : '';
    });
}

function resolveObjectPlaceholders(obj, testData, storedValues) {
    if (typeof obj === 'string') return resolvePlaceholders(obj, testData, storedValues);
    if (Array.isArray(obj)) return obj.map(item => resolveObjectPlaceholders(item, testData, storedValues));
    if (obj && typeof obj === 'object') {
        const resolved = {};
        for (const [k, v] of Object.entries(obj)) resolved[k] = resolveObjectPlaceholders(v, testData, storedValues);
        return resolved;
    }
    return obj;
}

// ─── Load tests ───
function loadTests() {
    const fs = require('fs');
    const path = require('path');
    const testDir = '/home/adm1n/startup/hemis-back/docs/univer_tool/integration/tests';
    const files = [
        '00-auth.js', '01-classifiers.js', '02-student-services.js', '03-teacher-services.js',
        '04-passport.js', '05-university.js', '06-external.js', '07-entity-student.js',
        '08-entity-teacher.js', '09-entity-structure.js', '10-entity-diploma.js',
        '11-entity-science.js', '12-entity-admin.js', '13-entity-stats.js'
    ];

    let combined = '';
    for (const file of files) {
        let code = fs.readFileSync(path.join(testDir, file), 'utf8');
        code = code.replace(/^const (tests_\d+)/gm, 'var $1');
        combined += code + '\n';
    }
    combined += `
        var allIntegrationTests = [
            ...tests_00, ...tests_01, ...tests_02, ...tests_03,
            ...tests_04, ...tests_05, ...tests_06, ...tests_07,
            ...tests_08, ...tests_09, ...tests_10, ...tests_11,
            ...tests_12, ...tests_13
        ];
    `;

    const vm = require('vm');
    const sandbox = { console, getNestedValue, ResponseValidator, allIntegrationTests: [] };
    const context = vm.createContext(sandbox);
    vm.runInContext(combined, context);
    return context.allIntegrationTests;
}

// ─── Sort by dependency ───
function sortByDependency(tests) {
    const testMap = new Map(tests.map(t => [t.id, t]));
    const visited = new Set();
    const sorted = [];
    const visit = (id) => {
        if (visited.has(id)) return;
        visited.add(id);
        const test = testMap.get(id);
        if (!test) return;
        if (test.dependsOn) {
            const deps = Array.isArray(test.dependsOn) ? test.dependsOn : [test.dependsOn];
            for (const d of deps) visit(d);
        }
        sorted.push(test);
    };
    for (const t of tests) visit(t.id);
    return sorted;
}

// ─── API Bootstrap ───
async function apiBootstrap(baseUrl, token) {
    const fetches = [
        { key: 'students', url: '/app/rest/v2/entities/hemishe_EStudent', params: { limit: 5, view: 'eStudent-view' } },
        { key: 'teachers', url: '/app/rest/v2/entities/hemishe_ETeacher', params: { limit: 3, view: 'eTeacher-view' } },
        { key: 'departments', url: '/app/rest/v2/entities/hemishe_EUniversityDepartment', params: { limit: 5, view: '_local' } },
        { key: 'groups', url: '/app/rest/v2/entities/hemishe_EUniversityGroup', params: { limit: 3, view: '_local' } },
        { key: 'specialties', url: '/app/rest/v2/entities/hemishe_EUniversitySpeciality', params: { limit: 3, view: '_local' } },
        { key: 'universities', url: '/app/rest/v2/entities/hemishe_EUniversity', params: { limit: 2, view: '_local' } },
        { key: 'diplomas', url: '/app/rest/v2/entities/hemishe_EStudentDiploma', params: { limit: 2, view: '_local' } },
        { key: 'gpa', url: '/app/rest/v2/entities/hemishe_EStudentGpa', params: { limit: 2, view: '_local' } },
        { key: 'certificates', url: '/app/rest/v2/entities/hemishe_EStudentCertificate', params: { limit: 2, view: '_local' } },
        { key: 'empCertificates', url: '/app/rest/v2/entities/hemishe_EEmpoyeeCertificate', params: { limit: 2, view: '_local' } },
        { key: 'employeeJobs', url: '/app/rest/v2/entities/hemishe_EEmployeeJobs', params: { limit: 2, view: 'eEmployeeJobs-view' } },
        { key: 'doctoralStudents', url: '/app/rest/v2/entities/hemishe_EDoctorateStudent', params: { limit: 2, view: '_local' } },
        { key: 'dissertationDefenses', url: '/app/rest/v2/entities/hemishe_EDissertationDefense', params: { limit: 2, view: '_local' } },
        { key: 'projects', url: '/app/rest/v2/entities/hemishe_EProject', params: { limit: 2, view: '_local' } },
        { key: 'projectMetas', url: '/app/rest/v2/entities/hemishe_EProjectMeta', params: { limit: 2, view: '_local' } },
        { key: 'projectExecutors', url: '/app/rest/v2/entities/hemishe_EProjectExecutor', params: { limit: 2, view: '_local' } },
        { key: 'publicationsScientific', url: '/app/rest/v2/entities/hemishe_EPublicationScientific', params: { limit: 2, view: '_local' } },
        { key: 'publicationsMethodical', url: '/app/rest/v2/entities/hemishe_EPublicationMethodical', params: { limit: 2, view: '_local' } },
        { key: 'publicationsProperty', url: '/app/rest/v2/entities/hemishe_EPublicationProperty', params: { limit: 2, view: '_local' } },
        { key: 'publicationAuthorMetas', url: '/app/rest/v2/entities/hemishe_EPublicationAuthorMeta', params: { limit: 2, view: '_local' } },
        { key: 'researchActivities', url: '/app/rest/v2/entities/hemishe_EResearchActivity', params: { limit: 2, view: '_local' } },
        { key: 'adminStudent2', url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent2', params: { limit: 2, view: '_local' } },
        { key: 'adminStudent3', url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent3', params: { limit: 2, view: '_local' } },
        { key: 'adminStudent4', url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent4', params: { limit: 2, view: '_local' } },
        { key: 'adminStudentSport', url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport', params: { limit: 2, view: '_local' } },
        { key: 'adminEmployee1', url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1', params: { limit: 2, view: '_local' } },
        { key: 'adminEmployee2', url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2', params: { limit: 2, view: '_local' } },
        { key: 'adminEmployee3', url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3', params: { limit: 2, view: '_local' } },
        { key: 'ictEquipment', url: '/app/rest/v2/entities/hemishe_RIctEquipment', params: { limit: 2, view: '_local' } },
        { key: 'laboratories', url: '/app/rest/v2/entities/hemishe_RLaboratories', params: { limit: 2, view: '_local' } },
        { key: 'educationMaterials', url: '/app/rest/v2/entities/hemishe_REducationMaterials', params: { limit: 2, view: '_local' } },
    ];

    const testData = {};
    for (const f of fetches) {
        try {
            const qs = new URLSearchParams(f.params).toString();
            const fullUrl = baseUrl + f.url + (qs ? '?' + qs : '');
            const resp = await fetch(fullUrl, {
                headers: { 'Authorization': 'Bearer ' + token, 'Accept': 'application/json' }
            });
            if (resp.ok) {
                const data = await resp.json();
                testData[f.key] = Array.isArray(data) ? data : [];
            } else {
                testData[f.key] = [];
            }
        } catch (e) {
            testData[f.key] = [];
        }
    }

    // University code (birinchi studentdan yoki universities dan)
    let uniCode = '401';
    if (testData.universities && testData.universities.length > 0) {
        uniCode = testData.universities[0].code || '401';
    }
    testData.university = { code: uniCode, tin: testData.universities?.[0]?.tin || '' };

    console.log(`  API Bootstrap: ${Object.keys(testData).filter(k => Array.isArray(testData[k]) && testData[k].length > 0).length} entities loaded`);
    return testData;
}

// ─── MAIN ───
(async () => {
    const fs = require('fs');
    const args = process.argv.slice(2);
    const filterCategory = args.find(a => a.startsWith('--category='))?.split('=')[1];
    const filterTest = args.find(a => a.startsWith('--test='))?.split('=')[1];
    const jsonOutput = args.includes('--json');

    try {
        console.log('=== HEMIS Endpoint Comparator ===');
        console.log(`OLD: ${OLD_URL} (old-hemis)`);
        console.log(`NEW: ${NEW_URL} (hemis-back)\n`);

        // 1. Load tests
        let tests = loadTests();
        console.log(`Testlar yuklandi: ${tests.length} ta`);

        // 2. Filter
        if (filterTest) {
            tests = tests.filter(t => t.id === filterTest);
            console.log(`Filter: test=${filterTest} → ${tests.length} ta`);
        } else if (filterCategory) {
            tests = tests.filter(t => t.category && t.category.includes(filterCategory));
            console.log(`Filter: category=${filterCategory} → ${tests.length} ta`);
        }

        // 3. Auth — ikkala server
        console.log('\n--- Auth ---');
        const oldToken = await getToken(OLD_URL);
        console.log(`  OLD token olindi`);
        const newToken = await getToken(NEW_URL);
        console.log(`  NEW token olindi`);

        // 4. API Bootstrap (hemis-back dan entity listlar orqali)
        console.log('\n--- API Bootstrap ---');
        const testData = await apiBootstrap(NEW_URL, newToken);
        testData.config = { username: USERNAME, password: PASSWORD };
        console.log(`  Students: ${(testData.students||[]).length}, Teachers: ${(testData.teachers||[]).length}, Groups: ${(testData.groups||[]).length}`);

        // storedValues
        const storedOld = { accessToken: oldToken, refreshToken: null };
        const storedNew = { accessToken: newToken, refreshToken: null };

        // 5. Sort and run
        const sorted = sortByDependency(tests);
        console.log(`\n--- Solishtirish (${sorted.length} ta endpoint) ---\n`);

        const results = [];
        let match = 0, mismatch = 0, bothFail = 0, skip = 0;
        const oldResults = {};
        const newResults = {};

        for (const test of sorted) {
            // auth-token va auth-refresh ni skip qilish (token allaqachon olingan)
            if (test.id === 'auth-token' || test.id === 'auth-refresh') {
                console.log(`  [SKIP] ${test.id} — auth allaqachon bajarildi`);
                oldResults[test.id] = { status: 'pass' };
                newResults[test.id] = { status: 'pass' };
                skip++;
                results.push({ id: test.id, result: 'SKIP', reason: 'auth' });
                continue;
            }

            // Dependency check
            if (test.dependsOn) {
                const deps = Array.isArray(test.dependsOn) ? test.dependsOn : [test.dependsOn];
                const depOk = deps.every(d =>
                    (oldResults[d] && oldResults[d].status === 'pass') ||
                    (newResults[d] && newResults[d].status === 'pass')
                );
                if (!depOk) {
                    console.log(`  [SKIP] ${test.id} — dependency bajarilmadi`);
                    skip++;
                    results.push({ id: test.id, result: 'SKIP', reason: 'dependency' });
                    continue;
                }
            }

            // MUHIM: hemis-back ni AVVAL sinash — agar DELETE/PUT bo'lsa, bazadagi entity
            // o'zgarmasligi uchun hemis-back birinchi bo'lishi kerak (bir xil baza!)
            const newResp = await sendRequest(NEW_URL, newToken, test, testData, storedNew);

            // OLD-HEMIS ga so'rov (keyin)
            const oldResp = await sendRequest(OLD_URL, oldToken, test, testData, storedOld);

            // Store values (ikkala tomon uchun)
            if (test.storeValues && oldResp.body != null) {
                for (const [key, path] of Object.entries(test.storeValues)) {
                    if (path === '_body') storedOld[key] = oldResp.body;
                    else {
                        const val = getNestedValue(oldResp.body, path);
                        if (val != null) storedOld[key] = val;
                    }
                }
            }
            if (test.storeValues && newResp.body != null) {
                for (const [key, path] of Object.entries(test.storeValues)) {
                    if (path === '_body') storedNew[key] = newResp.body;
                    else {
                        const val = getNestedValue(newResp.body, path);
                        if (val != null) storedNew[key] = val;
                    }
                }
            }

            // Natijani baholash
            const oldOk = oldResp.status >= 200 && oldResp.status < 300;
            const newOk = newResp.status >= 200 && newResp.status < 300;

            oldResults[test.id] = { status: oldOk ? 'pass' : 'fail' };
            newResults[test.id] = { status: newOk ? 'pass' : 'fail' };

            let verdict, symbol;

            if (oldOk && newOk) {
                // Ikkalasi ham 2xx — MATCH (201 vs 200 ham MATCH)
                verdict = 'MATCH';
                symbol = '✓';
                match++;
            } else if (oldOk && !newOk) {
                // OLD ishlaydi, NEW ishlamaydi — TUZATISH KERAK
                verdict = 'MISMATCH';
                symbol = '✗';
                mismatch++;
            } else if (!oldOk && newOk) {
                // OLD ishlamaydi, NEW ishlaydi — NEW yaxshiroq
                verdict = 'NEW_BETTER';
                symbol = '+';
                match++;
            } else if (oldResp.status === newResp.status) {
                // Ikkalasi ham xato, bir xil status
                verdict = 'BOTH_FAIL';
                symbol = '~';
                bothFail++;
            } else {
                // Ikkalasi ham xato, lekin status farq
                verdict = 'BOTH_FAIL_DIFF';
                symbol = '~';
                bothFail++;
            }

            const line = `  [${symbol}] ${test.id.padEnd(40)} OLD:${String(oldResp.status).padStart(3)} → NEW:${String(newResp.status).padStart(3)}  ${verdict}`;
            console.log(line);

            results.push({
                id: test.id,
                name: test.name,
                category: test.category,
                method: test.method || 'GET',
                url: test.url,
                result: verdict,
                oldStatus: oldResp.status,
                newStatus: newResp.status,
                oldTime: oldResp.time,
                newTime: newResp.time,
                oldError: oldResp.error,
                newError: newResp.error
            });
        }

        // 6. Summary
        console.log('\n=== NATIJALAR ===');
        console.log(`MATCH:      ${match} (ikkala serverda bir xil ishlaydi)`);
        console.log(`MISMATCH:   ${mismatch} (old-hemis da ishlaydi, hemis-back da YO'Q — TUZATISH KERAK)`);
        console.log(`BOTH_FAIL:  ${bothFail} (ikkalasida ham ishlamaydi — test yoki ikkala serverni tuzatish kerak)`);
        console.log(`SKIP:       ${skip}`);
        console.log(`TOTAL:      ${sorted.length}`);
        console.log(`\nMUVOFIQLIK: ${match}/${sorted.length - skip} (${((match / (sorted.length - skip)) * 100).toFixed(1)}%)`);

        // 7. Mismatch detail
        const mismatches = results.filter(r => r.result === 'MISMATCH');
        if (mismatches.length > 0) {
            console.log('\n=== TUZATISH KERAK (MISMATCH) ===');
            for (const r of mismatches) {
                console.log(`\n[MISMATCH] ${r.id}`);
                console.log(`  ${r.method} ${r.url}`);
                console.log(`  OLD: ${r.oldStatus} (${r.oldTime}ms) — ishlaydi`);
                console.log(`  NEW: ${r.newStatus} (${r.newTime}ms) — XATO`);
                if (r.newError) console.log(`  Error: ${r.newError}`);
            }
        }

        // 8. Both fail detail
        const bothFails = results.filter(r => r.result === 'BOTH_FAIL' || r.result === 'BOTH_FAIL_DIFF');
        if (bothFails.length > 0) {
            console.log('\n=== IKKALASIDA HAM ISHLAMAYDI (BOTH_FAIL) ===');
            for (const r of bothFails) {
                console.log(`  [~] ${r.id.padEnd(40)} OLD:${r.oldStatus} NEW:${r.newStatus}`);
            }
        }

        // 9. JSON chiqarish
        if (jsonOutput) {
            const reportPath = '/tmp/claude-0/-home-adm1n-startup/35402933-7d88-450d-917d-fdd64f23ba4e/scratchpad/compare_results.json';
            fs.writeFileSync(reportPath, JSON.stringify({
                timestamp: new Date().toISOString(),
                summary: { match, mismatch, bothFail, skip, total: sorted.length },
                results
            }, null, 2));
            console.log(`\nJSON natija: ${reportPath}`);
        }

        process.exit(mismatch > 0 ? 1 : 0);
    } catch (err) {
        console.error('FATAL:', err.message);
        process.exit(2);
    }
})();
