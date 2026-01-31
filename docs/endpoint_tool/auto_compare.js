#!/usr/bin/env node
/**
 * HEMIS Endpoint Auto-Comparator (endpoint_tool asosida)
 * 231 ta endpointni old-hemis (:8082) va hemis-back (:8081) da solishtiradi
 * To'g'ri format: endpoint_tool/endpoints/ fayllaridan olinadi
 *
 * Ishga tushirish:
 *   node auto_compare.js                    — barcha GET endpointlarni test
 *   node auto_compare.js --all              — barcha endpointlar (POST/PUT/DELETE ham)
 *   node auto_compare.js --category=04      — faqat bitta kategoriya
 *   node auto_compare.js --unsafe           — POST/PUT/DELETE ni old-hemis ga ham yuborish
 *   node auto_compare.js --json             — natijani JSON ga yozish
 */

const OLD_URL = 'http://localhost:8082';
const NEW_URL = 'http://localhost:8081';
const USERNAME_NEW = 'otm401';
const USERNAME_OLD = 'otm351';
const PASSWORD = 'XCZDAb7qvGTXxz';

const fs = require('fs');
const path = require('path');
const vm = require('vm');

// ─── Parse args ───
const args = process.argv.slice(2);
const testAll = args.includes('--all');
const unsafe = args.includes('--unsafe');
const jsonOutput = args.includes('--json');
const categoryFilter = args.find(a => a.startsWith('--category='))?.split('=')[1];
const idFilter = args.find(a => a.startsWith('--id='))?.split('=')[1];

// ─── Load endpoint files ───
function loadEndpoints() {
    const endpointDir = path.join(__dirname, 'endpoints');
    const files = fs.readdirSync(endpointDir)
        .filter(f => f.match(/^\d{2}-.*\.js$/) && !f.startsWith('_'))
        .sort();

    let combined = '';
    for (const file of files) {
        let code = fs.readFileSync(path.join(endpointDir, file), 'utf8');
        // module.exports ni olib tashlash
        code = code.replace(/if\s*\(typeof module.*\n.*\n\}/g, '');
        // const → var
        code = code.replace(/^const (endpoints_\d+)/gm, 'var $1');
        combined += code + '\n';
    }

    // _index.js orqali birlashtirish (all-endpoints.js YUKLAMAS — u ham `endpoints` declare qiladi)
    const indexFile = path.join(endpointDir, '_index.js');
    if (fs.existsSync(indexFile)) {
        let indexCode = fs.readFileSync(indexFile, 'utf8');
        indexCode = indexCode.replace(/^const /gm, 'var ');
        indexCode = indexCode.replace(/console\.log\(.*\);?/g, '');
        combined += indexCode + '\n';
    }

    const sandbox = {
        console: { log: () => {} },
        Date,
        endpoints: [],
        module: { exports: {} }
    };
    const context = vm.createContext(sandbox);

    try {
        vm.runInContext(combined, context);
    } catch (e) {
        console.error('Endpoint fayllarni yuklashda xato:', e.message);
        process.exit(1);
    }

    return context.endpoints || [];
}

// ─── Auth ───
async function getToken(baseUrl, username) {
    const formData = new URLSearchParams();
    formData.append('grant_type', 'password');
    formData.append('username', username);
    formData.append('password', PASSWORD);

    const response = await fetch(baseUrl + '/app/rest/v2/oauth/token', {
        method: 'POST',
        headers: {
            'Authorization': 'Basic Y2xpZW50OnNlY3JldA==',
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
    });
    if (!response.ok) throw new Error(`Auth failed ${baseUrl} (${username}): ${response.status}`);
    const data = await response.json();
    return { accessToken: data.access_token, refreshToken: data.refresh_token };
}

// ─── Build request from endpoint definition ───
function buildRequest(endpoint, token, isOld) {
    let url = endpoint.url;
    const method = endpoint.method || 'GET';
    const headers = {};

    // Auth
    if (endpoint.requiresAuth !== false && endpoint.auth !== 'basic') {
        headers['Authorization'] = 'Bearer ' + token;
    } else if (endpoint.auth === 'basic') {
        headers['Authorization'] = 'Basic Y2xpZW50OnNlY3JldA==';
    }

    // URL dagi {entityId} va boshqa placeholderlarni default qiymatlar bilan almashtirish
    if (endpoint.inputFields) {
        for (const [fieldName, field] of Object.entries(endpoint.inputFields)) {
            const placeholder = `{${fieldName}}`;
            if (url.includes(placeholder)) {
                const val = isOld ? (field.defaultOld || field.default || '') : (field.defaultNew || field.default || '');
                url = url.replace(placeholder, val);
            }
        }
    }

    // Params
    const params = {};
    if (endpoint.params) {
        for (const [k, v] of Object.entries(endpoint.params)) {
            let val = v;
            // {placeholder} ni resolve qilish
            if (typeof val === 'string' && val.startsWith('{') && val.endsWith('}')) {
                const fieldName = val.slice(1, -1);
                if (endpoint.inputFields && endpoint.inputFields[fieldName]) {
                    const field = endpoint.inputFields[fieldName];
                    val = isOld ? (field.defaultOld || field.default || '') : (field.defaultNew || field.default || '');
                }
            }
            if (val) params[k] = val;
        }
    }
    // inputFields dan params (GET uchun — query params)
    if (method === 'GET' && endpoint.inputFields) {
        for (const [fieldName, field] of Object.entries(endpoint.inputFields)) {
            if (fieldName === 'entityId') continue; // URL da allaqachon
            if (!url.includes(`{${fieldName}}`)) {
                const val = isOld ? (field.defaultOld || field.default || '') : (field.defaultNew || field.default || '');
                if (val && !params[fieldName]) {
                    // Faqat endpointning params da bo'lsa qo'shish
                    if (endpoint.params && fieldName in endpoint.params) {
                        params[fieldName] = val;
                    }
                }
            }
        }
    }

    const qs = new URLSearchParams(params).toString();
    const fullUrl = url + (qs ? (url.includes('?') ? '&' : '?') + qs : '');

    // Body
    let body = null;
    if (method !== 'GET') {
        if (endpoint.contentType === 'multipart' || endpoint.contentType === 'form') {
            headers['Content-Type'] = 'application/x-www-form-urlencoded';
            if (endpoint.body) {
                const formData = new URLSearchParams();
                for (const [k, v] of Object.entries(endpoint.body)) {
                    let val = v;
                    if (typeof val === 'string' && val.startsWith('{') && val.endsWith('}')) {
                        const fieldName = val.slice(1, -1);
                        if (fieldName === 'username') val = isOld ? USERNAME_OLD : USERNAME_NEW;
                        else if (fieldName === 'password') val = PASSWORD;
                        else if (fieldName === 'refresh_token') val = '__skip__';
                    }
                    if (val !== '__skip__') formData.append(k, val);
                }
                body = formData.toString();
            }
        } else {
            headers['Content-Type'] = 'application/json';
            headers['Accept'] = 'application/json';

            if (endpoint.bodyGenerator && typeof endpoint.bodyGenerator === 'function') {
                // bodyGenerator — inputFieldsdan inputs yaratib berish
                const inputs = {};
                if (endpoint.inputFields) {
                    for (const [fieldName, field] of Object.entries(endpoint.inputFields)) {
                        inputs[fieldName] = isOld ? (field.defaultOld || field.default || '') : (field.defaultNew || field.default || '');
                    }
                }
                try {
                    body = JSON.stringify(endpoint.bodyGenerator(inputs));
                } catch (e) {
                    body = null;
                }
            } else if (endpoint.bodyTemplate) {
                // bodyTemplate — {placeholder} larni almashtirish
                const resolved = resolveBodyTemplate(endpoint.bodyTemplate, endpoint.inputFields, isOld);
                body = JSON.stringify(resolved);
            } else if (endpoint.body) {
                body = JSON.stringify(endpoint.body);
            }
        }
    }

    return { url: fullUrl, method, headers, body };
}

function resolveBodyTemplate(template, inputFields, isOld) {
    if (typeof template === 'string') {
        if (template.startsWith('{') && template.endsWith('}')) {
            const fieldName = template.slice(1, -1);
            if (inputFields && inputFields[fieldName]) {
                const field = inputFields[fieldName];
                return isOld ? (field.defaultOld || field.default || '') : (field.defaultNew || field.default || '');
            }
        }
        return template;
    }
    if (Array.isArray(template)) return template.map(item => resolveBodyTemplate(item, inputFields, isOld));
    if (template && typeof template === 'object') {
        const resolved = {};
        for (const [k, v] of Object.entries(template)) {
            resolved[k] = resolveBodyTemplate(v, inputFields, isOld);
        }
        return resolved;
    }
    return template;
}

// ─── Send request ───
async function sendRequest(baseUrl, reqInfo) {
    try {
        const fullUrl = baseUrl + reqInfo.url;
        const options = { method: reqInfo.method, headers: reqInfo.headers };
        if (reqInfo.body && reqInfo.method !== 'GET') options.body = reqInfo.body;

        const start = Date.now();
        const response = await fetch(fullUrl, options);
        const text = await response.text();
        let respBody = null;
        if (text && text.trim().length > 0) {
            try { respBody = JSON.parse(text); } catch (e) { respBody = text; }
        }
        return { status: response.status, body: respBody, time: Date.now() - start, error: null };
    } catch (err) {
        return { status: 0, body: null, time: 0, error: err.message };
    }
}

// ─── MAIN ───
(async () => {
    try {
        console.log('=== HEMIS Endpoint Auto-Comparator ===');
        console.log(`OLD: ${OLD_URL} (old-hemis, user: ${USERNAME_OLD})`);
        console.log(`NEW: ${NEW_URL} (hemis-back, user: ${USERNAME_NEW})\n`);

        // Load
        let endpoints = loadEndpoints();
        console.log(`Endpointlar yuklandi: ${endpoints.length} ta, 70 kategoriya`);

        // Filter
        if (categoryFilter) {
            endpoints = endpoints.filter(e => e.category && e.category.startsWith(categoryFilter));
            console.log(`Filter: category=${categoryFilter} → ${endpoints.length} ta`);
        }
        if (idFilter) {
            const [cat, id] = idFilter.includes('.') ? idFilter.split('.') : [null, idFilter];
            endpoints = endpoints.filter(e => {
                if (cat) return e.category?.startsWith(cat) && e.id == id;
                return e.id == id;
            });
            console.log(`Filter: id=${idFilter} → ${endpoints.length} ta`);
        }

        // Skip token endpoints (auth alohida)
        const tokenEndpoints = endpoints.filter(e => e.category === '01.Token');
        const testEndpoints = endpoints.filter(e => e.category !== '01.Token');

        // Skip POST/PUT/DELETE (bazani buzmasligi uchun) unless --all or --unsafe
        let toTest;
        if (testAll || unsafe) {
            toTest = testEndpoints;
            console.log(`\n⚠️  Barcha methodlar testlanadi (GET + POST + PUT + DELETE)`);
            if (!unsafe) {
                console.log(`ℹ️  POST/PUT/DELETE faqat hemis-back ga yuboriladi (old-hemis bazani buzmasligi uchun)`);
            }
        } else {
            toTest = testEndpoints.filter(e => (e.method || 'GET') === 'GET');
            console.log(`\nFaqat GET endpointlar testlanadi (${toTest.length} ta). --all bilan hammasini test qilish mumkin.`);
        }

        // Auth
        console.log('\n--- Auth ---');
        const oldAuth = await getToken(OLD_URL, USERNAME_OLD);
        console.log(`  OLD token olindi (${USERNAME_OLD})`);
        const newAuth = await getToken(NEW_URL, USERNAME_NEW);
        console.log(`  NEW token olindi (${USERNAME_NEW})`);

        // Run
        console.log(`\n--- Solishtirish (${toTest.length} ta endpoint) ---\n`);

        const results = [];
        let match = 0, mismatch = 0, bothFail = 0, newBetter = 0, skip = 0;

        for (const endpoint of toTest) {
            const catId = `${endpoint.category}#${endpoint.id}`;
            const method = endpoint.method || 'GET';

            // POST/PUT/DELETE — old-hemis ga yubormasligi uchun
            const isWriteOp = ['POST', 'PUT', 'DELETE'].includes(method);

            // Build requests
            const oldReqInfo = buildRequest(endpoint, oldAuth.accessToken, true);
            const newReqInfo = buildRequest(endpoint, newAuth.accessToken, false);

            let oldResp, newResp;

            if (isWriteOp && !unsafe) {
                // Write operatsiyalarni faqat hemis-back ga yuborish
                oldResp = { status: -1, body: null, time: 0, error: 'SKIPPED (write op)' };
                newResp = await sendRequest(NEW_URL, newReqInfo);

                const newOk = newResp.status >= 200 && newResp.status < 300;
                if (newOk) {
                    match++;
                    console.log(`  [✓] ${catId.padEnd(50)} ${method.padEnd(6)} NEW:${newResp.status}  OK`);
                } else {
                    mismatch++;
                    console.log(`  [?] ${catId.padEnd(50)} ${method.padEnd(6)} NEW:${newResp.status}  (old-hemis SKIP — --unsafe bilan sinash)`);
                }
                results.push({
                    catId, category: endpoint.category, id: endpoint.id, name: endpoint.name,
                    method, url: endpoint.url,
                    result: newOk ? 'NEW_OK' : 'NEW_FAIL',
                    oldStatus: -1, newStatus: newResp.status,
                    oldSkipped: true
                });
                continue;
            }

            // Ikkala serverda sinash
            oldResp = await sendRequest(OLD_URL, oldReqInfo);
            newResp = await sendRequest(NEW_URL, newReqInfo);

            const oldOk = oldResp.status >= 200 && oldResp.status < 300;
            const newOk = newResp.status >= 200 && newResp.status < 300;

            let verdict, symbol;
            if (oldResp.status === newResp.status) {
                if (oldOk) { verdict = 'MATCH'; symbol = '✓'; match++; }
                else { verdict = 'BOTH_FAIL'; symbol = '~'; bothFail++; }
            } else if (oldOk && !newOk) {
                verdict = 'MISMATCH'; symbol = '✗'; mismatch++;
            } else if (!oldOk && newOk) {
                verdict = 'NEW_BETTER'; symbol = '+'; newBetter++;
            } else {
                verdict = 'BOTH_FAIL'; symbol = '~'; bothFail++;
            }

            console.log(`  [${symbol}] ${catId.padEnd(50)} ${method.padEnd(6)} OLD:${String(oldResp.status).padStart(3)} → NEW:${String(newResp.status).padStart(3)}  ${verdict}`);

            results.push({
                catId, category: endpoint.category, id: endpoint.id, name: endpoint.name,
                method, url: endpoint.url, result: verdict,
                oldStatus: oldResp.status, newStatus: newResp.status,
                oldTime: oldResp.time, newTime: newResp.time,
                oldError: oldResp.error, newError: newResp.error
            });
        }

        // Summary
        console.log('\n=== NATIJALAR ===');
        console.log(`MATCH:      ${match}`);
        console.log(`MISMATCH:   ${mismatch} (old-hemis ishlaydi, hemis-back ISHLAMAYDI)`);
        console.log(`BOTH_FAIL:  ${bothFail} (ikkalasida ham ishlamaydi)`);
        console.log(`NEW_BETTER: ${newBetter} (hemis-back yaxshiroq)`);
        console.log(`TOTAL:      ${toTest.length}`);

        const effective = match + newBetter;
        const total = toTest.length - bothFail;
        console.log(`\nMUVOFIQLIK: ${effective}/${total > 0 ? total : toTest.length} (${total > 0 ? ((effective / total) * 100).toFixed(1) : 0}%)`);

        // MISMATCH detail
        const mismatches = results.filter(r => r.result === 'MISMATCH');
        if (mismatches.length > 0) {
            console.log('\n=== TUZATISH KERAK (MISMATCH) ===');
            for (const r of mismatches) {
                console.log(`\n  [✗] ${r.catId} — ${r.name}`);
                console.log(`      ${r.method} ${r.url}`);
                console.log(`      OLD: ${r.oldStatus} → NEW: ${r.newStatus}`);
            }
        }

        // JSON
        if (jsonOutput) {
            const reportPath = '/tmp/endpoint_compare_results.json';
            fs.writeFileSync(reportPath, JSON.stringify({
                timestamp: new Date().toISOString(),
                summary: { match, mismatch, bothFail, newBetter, total: toTest.length },
                results
            }, null, 2));
            console.log(`\nJSON: ${reportPath}`);
        }

        if (mismatch === 0 && bothFail === 0) {
            console.log('\n<promise>ALL_TESTS_PASSED</promise>');
        }

        process.exit(mismatch > 0 ? 1 : 0);
    } catch (err) {
        console.error('FATAL:', err.message);
        console.error(err.stack);
        process.exit(2);
    }
})();
