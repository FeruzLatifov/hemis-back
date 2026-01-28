// TestUI — Dashboard rendering va interaktiv boshqaruv
// DOM bilan ishlash, filtrlash, progress bar, detail panel

class TestUI {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.activeFilter = 'all';
        this.expandedTests = new Set();
    }

    // ─── DASHBOARD RENDERING ──────────────────────────────

    /**
     * Boshlang'ich dashboard layout
     */
    renderDashboard(tests) {
        // Kategoriyalarni guruhlash
        const categories = this.groupByCategory(tests);

        let html = '';

        // Summary bar
        html += `<div id="summary-bar" class="summary-bar mb-3">
            <div class="d-flex align-items-center gap-3 flex-wrap">
                <span class="badge bg-secondary" id="stat-total">Jami: ${tests.length}</span>
                <span class="badge bg-success" id="stat-pass">PASS: 0</span>
                <span class="badge bg-danger" id="stat-fail">FAIL: 0</span>
                <span class="badge bg-warning text-dark" id="stat-skip">SKIP: 0</span>
                <span class="badge bg-info text-dark" id="stat-error">ERROR: 0</span>
                <span class="text-muted" id="stat-time">0.0s</span>
            </div>
            <div class="progress mt-2" style="height: 8px;">
                <div class="progress-bar bg-success" id="progress-pass" style="width: 0%"></div>
                <div class="progress-bar bg-danger" id="progress-fail" style="width: 0%"></div>
                <div class="progress-bar bg-warning" id="progress-skip" style="width: 0%"></div>
            </div>
        </div>`;

        // Action buttons
        html += `<div class="d-flex gap-2 mb-3 flex-wrap">
            <button class="btn btn-primary btn-sm" id="btn-run-all" onclick="app.runAll()">
                <i class="fas fa-play"></i> Barchasini bajarish
            </button>
            <button class="btn btn-outline-warning btn-sm" id="btn-run-failed" onclick="app.runFailed()" disabled>
                <i class="fas fa-redo"></i> FAIL larni qaytarish
            </button>
            <button class="btn btn-outline-danger btn-sm" id="btn-stop" onclick="app.stop()" disabled>
                <i class="fas fa-stop"></i> To'xtatish
            </button>
            <button class="btn btn-outline-secondary btn-sm" id="btn-reset" onclick="app.reset()">
                <i class="fas fa-eraser"></i> Tozalash
            </button>
        </div>`;

        // Category filter buttons
        html += `<div class="d-flex gap-1 mb-3 flex-wrap" id="filter-bar">`;
        html += `<button class="btn btn-sm btn-primary filter-btn" onclick="app.filterCategory('all')">Barchasi</button>`;
        for (const [cat, catTests] of Object.entries(categories)) {
            const shortName = cat.replace(/^\d+-/, '').replace(/^\d+\.\s*/, '');
            html += `<button class="btn btn-sm btn-outline-secondary filter-btn"
                        onclick="app.filterCategory('${this.escapeAttr(cat)}')"
                        data-category="${this.escapeAttr(cat)}">${shortName} (${catTests.length})</button>`;
        }
        html += `</div>`;

        // Test cards by category
        for (const [cat, catTests] of Object.entries(categories)) {
            html += this.renderCategory(cat, catTests);
        }

        this.container.innerHTML = html;
    }

    /**
     * Kategoriya accordion
     */
    renderCategory(category, tests) {
        const catId = this.slugify(category);
        return `
        <div class="card category-card mb-2" data-category="${this.escapeAttr(category)}">
            <div class="card-header category-header p-2 d-flex justify-content-between align-items-center"
                 onclick="app.toggleCategory('${catId}')" style="cursor:pointer;">
                <div>
                    <i class="fas fa-chevron-down me-1" id="icon-${catId}"></i>
                    <strong>${category}</strong>
                    <span class="badge bg-secondary ms-1" id="cat-count-${catId}">${tests.length} test</span>
                    <span id="cat-status-${catId}"></span>
                </div>
                <button class="btn btn-sm btn-outline-light"
                        onclick="event.stopPropagation(); app.runCategoryByName('${this.escapeAttr(category)}')">
                    <i class="fas fa-play"></i>
                </button>
            </div>
            <div class="card-body p-0" id="cat-body-${catId}">
                <div class="list-group list-group-flush">
                    ${tests.map(t => this.renderTestCard(t)).join('')}
                </div>
            </div>
        </div>`;
    }

    /**
     * Bitta test kartasi
     */
    renderTestCard(test) {
        const methodClass = this.getMethodClass(test.method);
        return `
        <div class="list-group-item test-card p-2" id="test-${test.id}" data-category="${this.escapeAttr(test.category)}">
            <div class="d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                    <span class="badge bg-secondary test-status-badge" id="badge-${test.id}">KUTILMOQDA</span>
                    <span class="badge ${methodClass}">${test.method}</span>
                    <code class="small text-truncate" style="max-width:400px;" title="${this.escapeAttr(test.url)}">${test.url}</code>
                    <small class="text-muted">${test.name || ''}</small>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <span class="text-muted small" id="time-${test.id}"></span>
                    <button class="btn btn-sm btn-outline-secondary py-0 px-1"
                            onclick="app.toggleTestDetail('${test.id}')" title="Tafsilotlar">
                        <i class="fas fa-chevron-down" id="detail-icon-${test.id}"></i>
                    </button>
                </div>
            </div>
            ${test.dependsOn ? `<small class="text-muted"><i class="fas fa-link"></i> Bog'liq: ${Array.isArray(test.dependsOn) ? test.dependsOn.join(', ') : test.dependsOn}</small>` : ''}
            <div class="test-detail mt-2" id="detail-${test.id}" style="display:none;">
            </div>
        </div>`;
    }

    // ─── YANGILASH ────────────────────────────────────────

    /**
     * Bitta test natijasini yangilash
     */
    updateTestCard(testId, result) {
        // Badge
        const badge = document.getElementById(`badge-${testId}`);
        if (badge) {
            badge.textContent = this.statusText(result.status);
            badge.className = `badge test-status-badge ${this.statusBadgeClass(result.status)}`;
        }

        // Vaqt
        const time = document.getElementById(`time-${testId}`);
        if (time && result.time != null) {
            time.textContent = result.time + 'ms';
        }

        // Detail kontentini yangilash
        const detail = document.getElementById(`detail-${testId}`);
        if (detail) {
            detail.innerHTML = this.renderTestDetail(result);
        }

        // Agar expand qilingan bo'lsa, ko'rsatish
        if (this.expandedTests.has(testId)) {
            if (detail) detail.style.display = 'block';
        }
    }

    /**
     * Test tafsilotlari (request/response/validation)
     */
    renderTestDetail(result) {
        if (!result) return '';

        let html = '';

        // Validation checks
        if (result.validation && result.validation.checks) {
            html += `<div class="mb-2"><strong>Tekshiruvlar:</strong></div>`;
            html += `<div class="mb-2">`;
            for (const check of result.validation.checks) {
                const icon = check.pass
                    ? (check.severity === 'warn' ? '<i class="fas fa-exclamation-triangle text-warning"></i>' : '<i class="fas fa-check text-success"></i>')
                    : '<i class="fas fa-times text-danger"></i>';
                html += `<div class="small">${icon} <strong>${check.name}</strong>: `;
                if (check.expected != null) html += `kutilgan: <code>${check.expected}</code>, `;
                if (check.actual != null) html += `kelgan: <code>${check.actual}</code>`;
                html += `</div>`;
            }
            html += `</div>`;
        }

        // Error
        if (result.error) {
            html += `<div class="alert alert-danger py-1 px-2 small">${this.escapeHtml(result.error)}</div>`;
        }

        // Skip reason
        if (result.reason) {
            html += `<div class="alert alert-warning py-1 px-2 small">${this.escapeHtml(result.reason)}</div>`;
        }

        // Request
        if (result.request) {
            html += `<div class="mb-1"><strong>Request:</strong></div>`;
            html += `<pre class="request-box small mb-2">${result.request.method} ${this.escapeHtml(result.request.url)}
Headers: ${this.escapeHtml(JSON.stringify(result.request.headers, null, 2))}${result.request.body ? '\nBody: ' + this.escapeHtml(typeof result.request.body === 'string' ? result.request.body : JSON.stringify(result.request.body, null, 2)) : ''}</pre>`;
        }

        // Response
        if (result.response != null) {
            html += `<div class="mb-1"><strong>Response (${result.httpStatus}):</strong></div>`;
            const responseStr = typeof result.response === 'string'
                ? result.response
                : JSON.stringify(result.response, null, 2);
            // Katta response ni qisqartirish
            const truncated = responseStr.length > 3000
                ? responseStr.substring(0, 3000) + '\n... (qisqartirildi)'
                : responseStr;
            html += `<pre class="response-box small mb-0">${this.escapeHtml(truncated)}</pre>`;
        }

        return html;
    }

    /**
     * Summary barni yangilash
     */
    updateSummary(stats, total) {
        const el = (id) => document.getElementById(id);
        el('stat-total').textContent = `Jami: ${total}`;
        el('stat-pass').textContent = `PASS: ${stats.pass}`;
        el('stat-fail').textContent = `FAIL: ${stats.fail}`;
        el('stat-skip').textContent = `SKIP: ${stats.skip}`;
        el('stat-error').textContent = `ERROR: ${stats.error}`;
        el('stat-time').textContent = (stats.totalTime / 1000).toFixed(1) + 's';

        const completed = stats.pass + stats.fail + stats.skip + stats.error;
        if (total > 0) {
            el('progress-pass').style.width = (stats.pass / total * 100) + '%';
            el('progress-fail').style.width = ((stats.fail + stats.error) / total * 100) + '%';
            el('progress-skip').style.width = (stats.skip / total * 100) + '%';
        }

        // Kategoriya statuslarini yangilash
        this.updateCategoryStatuses();
    }

    /**
     * Kategoriya statuslarini yangilash
     */
    updateCategoryStatuses() {
        const categories = document.querySelectorAll('.category-card');
        categories.forEach(card => {
            const cat = card.dataset.category;
            const tests = card.querySelectorAll('.test-card');
            let pass = 0, fail = 0, total = tests.length;

            tests.forEach(tc => {
                const badge = tc.querySelector('.test-status-badge');
                if (badge) {
                    if (badge.textContent === 'PASS') pass++;
                    else if (badge.textContent === 'FAIL' || badge.textContent === 'ERROR') fail++;
                }
            });

            const catId = this.slugify(cat);
            const statusEl = document.getElementById(`cat-status-${catId}`);
            if (statusEl) {
                if (pass + fail > 0) {
                    statusEl.innerHTML = `<span class="badge ${fail > 0 ? 'bg-danger' : 'bg-success'} ms-1">${pass}/${total}</span>`;
                }
            }
        });
    }

    // ─── INTERAKTIV BOSHQARUV ─────────────────────────────

    /**
     * Test detail panelni toggle
     */
    toggleTestDetail(testId) {
        const detail = document.getElementById(`detail-${testId}`);
        const icon = document.getElementById(`detail-icon-${testId}`);
        if (!detail) return;

        if (detail.style.display === 'none') {
            detail.style.display = 'block';
            this.expandedTests.add(testId);
            if (icon) icon.className = 'fas fa-chevron-up';
        } else {
            detail.style.display = 'none';
            this.expandedTests.delete(testId);
            if (icon) icon.className = 'fas fa-chevron-down';
        }
    }

    /**
     * Kategoriya accordion toggle
     */
    toggleCategory(catId) {
        const body = document.getElementById(`cat-body-${catId}`);
        const icon = document.getElementById(`icon-${catId}`);
        if (!body) return;

        if (body.style.display === 'none') {
            body.style.display = 'block';
            if (icon) icon.className = 'fas fa-chevron-down me-1';
        } else {
            body.style.display = 'none';
            if (icon) icon.className = 'fas fa-chevron-right me-1';
        }
    }

    /**
     * Kategoriya bo'yicha filtrlash
     */
    filterCategory(category) {
        this.activeFilter = category;

        // Filter buttonsni yangilash
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.className = `btn btn-sm ${
                (category === 'all' && !btn.dataset.category) || btn.dataset.category === category
                    ? 'btn-primary' : 'btn-outline-secondary'
            } filter-btn`;
        });

        // Kategoriya cardlarni ko'rsatish/yashirish
        document.querySelectorAll('.category-card').forEach(card => {
            if (category === 'all' || card.dataset.category === category) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });
    }

    /**
     * Running holatini UI ga aks ettirish
     */
    setRunning(isRunning) {
        const btnRun = document.getElementById('btn-run-all');
        const btnStop = document.getElementById('btn-stop');
        const btnFailed = document.getElementById('btn-run-failed');

        if (btnRun) btnRun.disabled = isRunning;
        if (btnStop) btnStop.disabled = !isRunning;
        if (btnFailed) btnFailed.disabled = isRunning;
    }

    /**
     * Bootstrap log xabarlarini ko'rsatish
     */
    showBootstrapLog(messages) {
        const el = document.getElementById('bootstrap-log');
        if (!el) return;
        el.innerHTML = messages.map(m => `<div class="small text-muted">${this.escapeHtml(m)}</div>`).join('');
        el.style.display = 'block';
    }

    // ─── YORDAMCHI ────────────────────────────────────────

    groupByCategory(tests) {
        const groups = {};
        for (const test of tests) {
            const cat = test.category || 'Boshqa';
            if (!groups[cat]) groups[cat] = [];
            groups[cat].push(test);
        }
        return groups;
    }

    slugify(str) {
        return str.replace(/[^a-zA-Z0-9]/g, '-').replace(/-+/g, '-').toLowerCase();
    }

    escapeHtml(str) {
        if (typeof str !== 'string') str = String(str);
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    escapeAttr(str) {
        return this.escapeHtml(str).replace(/'/g, '&#39;');
    }

    statusText(status) {
        const map = { pass: 'PASS', fail: 'FAIL', error: 'ERROR', skip: 'SKIP' };
        return map[status] || 'KUTILMOQDA';
    }

    statusBadgeClass(status) {
        const map = {
            pass: 'bg-success',
            fail: 'bg-danger',
            error: 'bg-danger',
            skip: 'bg-warning text-dark'
        };
        return map[status] || 'bg-secondary';
    }

    getMethodClass(method) {
        const map = {
            GET: 'bg-info text-dark',
            POST: 'bg-primary',
            PUT: 'bg-warning text-dark',
            DELETE: 'bg-danger',
            PATCH: 'bg-secondary'
        };
        return map[method] || 'bg-secondary';
    }
}
