// TestRunner — test bajaruv mexanizmi
// Auth flow, placeholder resolution, dependency ordering, stored values

class TestRunner {
    /**
     * @param {Object} config
     * @param {string} config.baseUrl - hemis-back URL
     */
    constructor(config) {
        // Bo'sh string = proxy rejim (so'rovlar shu origin ga ketadi)
        this.baseUrl = config.baseUrl != null ? config.baseUrl : '';
        this.accessToken = null;
        this.refreshToken = null;
        this.testData = null;
        this.storedValues = {};
        this.results = {};
        this.isRunning = false;
        this.shouldStop = false;

        // Callbacks
        this.onTestComplete = null;   // (testId, result) => {}
        this.onBootstrapLog = null;   // (message) => {}
        this.onPhaseChange = null;    // (phaseName) => {}
    }

    // ─── 1-BOSQICH: AUTENTIFIKATSIYA ──────────────────────

    /**
     * OAuth2 password grant bilan token olish
     */
    async authenticate(username, password) {
        const url = this.baseUrl + '/app/rest/v2/oauth/token';
        const formData = new URLSearchParams();
        formData.append('grant_type', 'password');
        formData.append('username', username);
        formData.append('password', password);

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Authorization': 'Basic Y2xpZW50OnNlY3JldA==',
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`Auth xatosi (${response.status}): ${errorBody}`);
        }

        const data = await response.json();
        this.accessToken = data.access_token;
        this.refreshToken = data.refresh_token;

        // storedValues ga saqlash (keyingi testlar uchun)
        this.storedValues.accessToken = data.access_token;
        this.storedValues.refreshToken = data.refresh_token;

        return data;
    }

    // ─── 2-BOSQICH: BOOTSTRAP ─────────────────────────────

    /**
     * Test ma'lumotlarini API dan yuklash
     */
    async bootstrap() {
        if (!this.accessToken) {
            throw new Error('Avval authenticate() chaqirilishi kerak');
        }

        const loader = new DataBootstrap(this.baseUrl, this.accessToken);
        this.testData = await loader.loadAll();

        // Log xabarlarini UI ga uzatish
        if (this.onBootstrapLog) {
            for (const msg of loader.log) {
                this.onBootstrapLog(msg);
            }
        }

        return this.testData;
    }

    // ─── 3-BOSQICH: TEST BAJARISH ─────────────────────────

    /**
     * Barcha testlarni dependency tartibida bajarish
     */
    async runAll(tests) {
        this.isRunning = true;
        this.shouldStop = false;
        this.results = {};

        const sorted = this.sortByDependency(tests);

        for (const test of sorted) {
            if (this.shouldStop) break;

            // Dependency tekshirish
            if (!this.checkDependencies(test)) {
                this.results[test.id] = {
                    status: 'skip',
                    reason: 'Dependency bajarilmadi',
                    time: 0
                };
                if (this.onTestComplete) this.onTestComplete(test.id, this.results[test.id]);
                continue;
            }

            // Test bajarish
            const result = await this.runSingle(test);
            this.results[test.id] = result;

            if (this.onTestComplete) this.onTestComplete(test.id, result);
        }

        this.isRunning = false;
        return this.results;
    }

    /**
     * Faqat FAIL bo'lgan testlarni qaytadan bajarish
     */
    async runFailed(tests) {
        this.isRunning = true;
        this.shouldStop = false;

        const failedTests = tests.filter(t => {
            const r = this.results[t.id];
            return r && (r.status === 'fail' || r.status === 'error');
        });

        for (const test of failedTests) {
            if (this.shouldStop) break;

            const result = await this.runSingle(test);
            this.results[test.id] = result;

            if (this.onTestComplete) this.onTestComplete(test.id, result);
        }

        this.isRunning = false;
        return this.results;
    }

    /**
     * Bitta kategoriya testlarini bajarish
     */
    async runCategory(tests, category) {
        this.isRunning = true;
        this.shouldStop = false;

        const categoryTests = tests.filter(t => t.category === category);
        const sorted = this.sortByDependency(categoryTests);

        for (const test of sorted) {
            if (this.shouldStop) break;

            if (!this.checkDependencies(test)) {
                this.results[test.id] = {
                    status: 'skip',
                    reason: 'Dependency bajarilmadi',
                    time: 0
                };
                if (this.onTestComplete) this.onTestComplete(test.id, this.results[test.id]);
                continue;
            }

            const result = await this.runSingle(test);
            this.results[test.id] = result;

            if (this.onTestComplete) this.onTestComplete(test.id, result);
        }

        this.isRunning = false;
        return this.results;
    }

    /**
     * Bajarishni to'xtatish
     */
    stop() {
        this.shouldStop = true;
    }

    /**
     * Bitta testni bajarish
     */
    async runSingle(test) {
        const startTime = Date.now();
        try {
            // Request tuzish
            const request = this.buildRequest(test);
            // HTTP chaqiruv
            const response = await fetch(request.url, request.options);

            let body = null;
            // Avval text sifatida o'qish — bo'sh body ni to'g'ri aniqlash uchun
            const text = await response.text();
            if (text && text.trim().length > 0) {
                try {
                    body = JSON.parse(text);
                } catch (e) {
                    body = text; // Matn bor lekin JSON emas — string sifatida saqlash
                }
            }
            // body === null → bo'sh javob (200 OK bilan ham valid — old-hemis compatible)
            // body is string → matn bor lekin JSON emas

            const elapsed = Date.now() - startTime;

            // Validatsiya
            const validation = ResponseValidator.validate(test, response.status, body, elapsed);

            // StoredValues saqlash
            this.extractStoredValues(test, body);

            return {
                status: validation.pass ? 'pass' : 'fail',
                httpStatus: response.status,
                time: elapsed,
                request: {
                    method: request.options.method,
                    url: request.url,
                    headers: this.sanitizeHeaders(request.options.headers),
                    body: request.options.body ? this.tryParseBody(request.options.body) : null
                },
                response: body,
                validation
            };
        } catch (err) {
            return {
                status: 'error',
                time: Date.now() - startTime,
                error: err.message,
                validation: {
                    pass: false,
                    checks: [{ name: 'Tarmoq xatosi', expected: 'Javob', actual: err.message, pass: false }]
                }
            };
        }
    }

    // ─── REQUEST TUZISH ───────────────────────────────────

    /**
     * Test ta'rifidan HTTP request tuzish
     */
    buildRequest(test) {
        // URL ni resolve qilish
        let url = this.resolvePlaceholders(test.url);
        url = this.baseUrl + url;

        // Query params
        if (test.params) {
            const resolved = {};
            for (const [k, v] of Object.entries(test.params)) {
                resolved[k] = this.resolvePlaceholders(String(v));
            }
            const qs = new URLSearchParams(resolved).toString();
            if (qs) url += (url.includes('?') ? '&' : '?') + qs;
        }

        // Headers
        const headers = {};

        // Auth
        if (test.auth === 'bearer') {
            headers['Authorization'] = 'Bearer ' + this.accessToken;
        } else if (test.auth === 'basic') {
            headers['Authorization'] = 'Basic Y2xpZW50OnNlY3JldA==';
        }

        // Body
        let body = null;

        if (test.contentType === 'multipart' || test.contentType === 'form') {
            // Form-urlencoded body
            headers['Content-Type'] = 'application/x-www-form-urlencoded';
            if (test.body) {
                const formData = new URLSearchParams();
                const resolvedBody = this.resolveObjectPlaceholders(test.body);
                for (const [k, v] of Object.entries(resolvedBody)) {
                    formData.append(k, v);
                }
                body = formData.toString();
            }
        } else {
            // JSON body
            headers['Content-Type'] = 'application/json';
            headers['Accept'] = 'application/json';

            if (test.bodyBuilder && typeof test.bodyBuilder === 'function') {
                body = JSON.stringify(test.bodyBuilder(this.testData, this.storedValues));
            } else if (test.body) {
                body = JSON.stringify(this.resolveObjectPlaceholders(test.body));
            }
        }

        // Faqat body bor bo'lsa headers va body qo'yish
        const options = {
            method: test.method || 'GET',
            headers
        };
        if (body && test.method !== 'GET') {
            options.body = body;
        }

        return { url, options };
    }

    // ─── PLACEHOLDER RESOLUTION ───────────────────────────

    /**
     * String ichidagi {{...}} placeholderlarni real qiymatga almashtirish
     * Masalan: "{{students[0].pinfl}}" → "61911046440040"
     */
    resolvePlaceholders(str) {
        if (typeof str !== 'string') return str;
        return str.replace(/\{\{(.+?)\}\}/g, (_, path) => {
            const context = {
                ...this.testData,
                storedValues: this.storedValues
            };
            const val = getNestedValue(context, path.trim());
            return val != null ? String(val) : '';
        });
    }

    /**
     * Object ichidagi barcha string qiymatlarni resolve qilish (recursive)
     */
    resolveObjectPlaceholders(obj) {
        if (typeof obj === 'string') {
            return this.resolvePlaceholders(obj);
        }
        if (Array.isArray(obj)) {
            return obj.map(item => this.resolveObjectPlaceholders(item));
        }
        if (obj && typeof obj === 'object') {
            const resolved = {};
            for (const [k, v] of Object.entries(obj)) {
                resolved[k] = this.resolveObjectPlaceholders(v);
            }
            return resolved;
        }
        return obj;
    }

    // ─── STORED VALUES ────────────────────────────────────

    /**
     * Test natijasidan keyingi testlar uchun qiymatlar saqlash
     */
    extractStoredValues(test, body) {
        if (!test.storeValues || body == null) return;

        for (const [key, path] of Object.entries(test.storeValues)) {
            if (path === '_body') {
                this.storedValues[key] = body;
            } else {
                const val = getNestedValue(body, path);
                if (val != null) {
                    this.storedValues[key] = val;
                }
            }
        }
    }

    // ─── DEPENDENCY MANAGEMENT ────────────────────────────

    /**
     * Testlarni dependency tartibida saralash (topological sort)
     */
    sortByDependency(tests) {
        const testMap = new Map(tests.map(t => [t.id, t]));
        const visited = new Set();
        const sorted = [];

        const visit = (testId) => {
            if (visited.has(testId)) return;
            visited.add(testId);

            const test = testMap.get(testId);
            if (!test) return;

            // Avval dependencylarni bajarish
            if (test.dependsOn) {
                const deps = Array.isArray(test.dependsOn) ? test.dependsOn : [test.dependsOn];
                for (const dep of deps) {
                    visit(dep);
                }
            }

            sorted.push(test);
        };

        for (const test of tests) {
            visit(test.id);
        }

        return sorted;
    }

    /**
     * Test dependencylari bajarilganmi tekshirish
     */
    checkDependencies(test) {
        if (!test.dependsOn) return true;

        const deps = Array.isArray(test.dependsOn) ? test.dependsOn : [test.dependsOn];
        for (const dep of deps) {
            const result = this.results[dep];
            if (!result || result.status === 'fail' || result.status === 'error' || result.status === 'skip') {
                return false;
            }
        }
        return true;
    }

    // ─── YORDAMCHI METODLAR ───────────────────────────────

    /**
     * Headerlarni sanitize qilish (token ni qisqartirish)
     */
    sanitizeHeaders(headers) {
        const sanitized = { ...headers };
        if (sanitized['Authorization'] && sanitized['Authorization'].startsWith('Bearer ')) {
            const token = sanitized['Authorization'].substring(7);
            sanitized['Authorization'] = 'Bearer ' + token.substring(0, 20) + '...';
        }
        return sanitized;
    }

    /**
     * Body ni parse qilishga harakat qilish
     */
    tryParseBody(body) {
        if (!body) return null;
        try {
            return JSON.parse(body);
        } catch (e) {
            return body;
        }
    }

    /**
     * Natijalar statistikasi
     */
    getStats() {
        const results = Object.values(this.results);
        return {
            total: results.length,
            pass: results.filter(r => r.status === 'pass').length,
            fail: results.filter(r => r.status === 'fail').length,
            error: results.filter(r => r.status === 'error').length,
            skip: results.filter(r => r.status === 'skip').length,
            avgTime: results.length > 0
                ? Math.round(results.reduce((sum, r) => sum + (r.time || 0), 0) / results.length)
                : 0,
            totalTime: results.reduce((sum, r) => sum + (r.time || 0), 0)
        };
    }

    /**
     * Natijalarni tozalash
     */
    reset() {
        this.results = {};
        this.storedValues = {};
        if (this.accessToken) {
            this.storedValues.accessToken = this.accessToken;
            this.storedValues.refreshToken = this.refreshToken;
        }
    }
}
